package su.afk.yummy.tv.data.update.repository

import su.afk.yummy.tv.domain.update.model.ReleasesUnavailableException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import su.afk.yummy.tv.core.network.di.UnauthenticatedJsonClient
import su.afk.yummy.tv.data.update.UpdateConfig
import su.afk.yummy.tv.data.update.dto.GitHubReleaseDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import su.afk.yummy.tv.data.update.mapper.toDomain
import su.afk.yummy.tv.data.update.mapper.toReleaseNotes
import su.afk.yummy.tv.domain.update.model.AppRelease
import su.afk.yummy.tv.domain.update.model.AppReleaseNotes
import su.afk.yummy.tv.domain.update.repository.UpdateRepository
import su.afk.yummy.tv.domain.update.utils.compareVersions
import su.afk.yummy.tv.domain.update.utils.isVersionNewer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import su.afk.yummy.tv.core.featuretoggle.api.FeatureFlags
import su.afk.yummy.tv.core.featuretoggle.api.FeatureToggleProvider
import javax.inject.Inject

/**
 * Релизы из GitHub Releases: самый новый для проверки обновлений и история изменений
 * (pre-release — только для бета-канала).
 * Клиент намеренно неавторизованный: api.github.com — публичный сторонний сервис, токен приложения ему отправлять незачем.
 */
internal class GitHubUpdateRepository @Inject constructor(
    @param:UnauthenticatedJsonClient private val httpClient: HttpClient,
    private val featureToggleProvider: FeatureToggleProvider,
) : UpdateRepository {

    override suspend fun latestRelease(currentVersion: String, includePrerelease: Boolean): AppRelease? {
        // Стабильный канал: один маленький запрос вместо списка из сотни релизов (на слабых приставках список не успевал).
        if (!includePrerelease) {
            latestStableRelease(currentVersion)?.let { return it }
        }
        // GitHub недоступен (ошибка, таймаут, пустой ответ) — резервный релиз по прямой ссылке из флагов.
        val releases = fetchReleasesOrNull()
            ?.mapNotNull { it.toDomain() }
            ?.filter { includePrerelease || !it.isPrerelease }
            ?.takeIf { it.isNotEmpty() }
            ?: return fallbackRelease(currentVersion)
        val latest = releases.maxWithOrNull { a, b -> compareVersions(a.version, b.version) }
            ?: return fallbackRelease(currentVersion)
        val updatesCount = releases.count { isVersionNewer(currentVersion, it.version) }
        return latest.copy(updatesCount = updatesCount)
    }

    private suspend fun latestStableRelease(currentVersion: String): AppRelease? {
        val release = fetchLatestReleaseOrNull()?.toDomain()?.takeUnless { it.isPrerelease } ?: return null
        return release.copy(updatesCount = if (isVersionNewer(currentVersion, release.version)) 1 else 0)
    }

    private suspend fun fetchLatestReleaseOrNull(): GitHubReleaseDto? =
        try {
            withTimeoutOrNull(GITHUB_TIMEOUT_MS) { fetchLatestRelease() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private suspend fun fetchReleaseHistoryOrNull(url: String? = RELEASES_URL): List<GitHubReleaseDto>? =
        try {
            withTimeoutOrNull(GITHUB_TIMEOUT_MS) { fetchReleases(url) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    override suspend fun releaseHistory(currentVersion: String, includePrerelease: Boolean): List<AppReleaseNotes> {
        val history = fetchReleaseHistoryOrNull()
            ?.mapNotNull { it.toReleaseNotes() }
            ?.filter { (includePrerelease || !it.isPrerelease) && !isVersionNewer(currentVersion, it.version) }
            ?.sortedWith { a, b -> compareVersions(b.version, a.version) }
            .orEmpty()
        if (history.isNotEmpty()) return history

        // Если список релизов временно недоступен (например, GitHub API rate limit),
        // пробуем получить хотя бы последний опубликованный релиз отдельным endpoint.
        val latest = fetchLatestReleaseOrNull()
            ?.toReleaseNotes()
            ?.takeIf { (includePrerelease || !it.isPrerelease) && !isVersionNewer(currentVersion, it.version) }
        return latest?.let(::listOf) ?: throw ReleasesUnavailableException()
    }

    /** История оригинального проекта загружается непосредственно из Helandy/YummyTV. */
    override suspend fun originalReleaseHistory(
        currentVersion: String,
        includePrerelease: Boolean,
    ): List<AppReleaseNotes> {
        val history = fetchReleaseHistoryOrNull(ORIGINAL_RELEASES_URL)
            ?.mapNotNull { it.toReleaseNotes() }
            ?.filter { (includePrerelease || !it.isPrerelease) && !isVersionNewer(currentVersion, it.version) }
            ?.sortedWith { a, b -> compareVersions(b.version, a.version) }
        return history?.takeIf { it.isNotEmpty() } ?: throw ReleasesUnavailableException()
    }

    override suspend fun yanimeReleaseHistory(
        currentVersion: String,
        includePrerelease: Boolean,
    ): List<AppReleaseNotes> {
        val changelog = fetchYanimeChangelogOrNull() ?: throw ReleasesUnavailableException()
        return changelog.releases
            .asSequence()
            .filter { includePrerelease || !it.prerelease }
            .filter { !isVersionNewer(currentVersion, it.version) }
            .map { release ->
                val curatedNotes = release.notes.map { "- $it" }
                val overriddenCommits = release.commitOverrides.map { override ->
                    buildString {
                        override.tag?.takeIf { it.isNotBlank() }?.let { tag ->
                            append("[${tag.trim('[', ']')}] ")
                        }
                        append(override.title.trim())
                        override.description?.takeIf { it.isNotBlank() }?.let { description ->
                            append("\n  ")
                            append(description.trim().replace("\n", "\n  "))
                        }
                    }.let { "- $it" }
                }
                AppReleaseNotes(
                    version = release.version,
                    publishedDate = release.publishedAt,
                    changelog = (curatedNotes + overriddenCommits).joinToString("\n"),
                    isPrerelease = release.prerelease,
                )
            }
            .sortedWith { a, b -> compareVersions(b.version, a.version) }
            .toList()
    }

    private suspend fun fetchYanimeChangelogOrNull(): YanimeChangelogDto? =
        try {
            withTimeoutOrNull(GITHUB_TIMEOUT_MS) {
                val response = httpClient.get(YANIME_CHANGELOG_URL) {
                    header("Accept", "application/json")
                    header("User-Agent", GITHUB_USER_AGENT)
                }
                if (response.status.isSuccess()) response.body<YanimeChangelogDto>() else null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    /** Короче общего таймаута проверки в MainSideEffectsHandler, чтобы на резерв осталось время. */
    private suspend fun fetchReleasesOrNull(): List<GitHubReleaseDto>? =
        try {
            withTimeoutOrNull(GITHUB_TIMEOUT_MS) { fetchReleases() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private fun fallbackRelease(currentVersion: String): AppRelease? {
        val url = featureToggleProvider.getString(FeatureFlags.updateFallbackApkUrl).trim()
        val version = featureToggleProvider.getString(FeatureFlags.updateFallbackVersion).trim()
        if (!url.startsWith("https://", ignoreCase = true) || version.isBlank()) return null
        return AppRelease(
            version = version,
            changelog = featureToggleProvider.getString(FeatureFlags.updateFallbackChangelog).trim(),
            apkUrl = url,
            updatesCount = if (isVersionNewer(currentVersion, version)) 1 else 0,
        )
    }

    /** null, когда репозиторий обновлений не сконфигурирован или GitHub ответил ошибкой. */
    private suspend fun fetchReleases(url: String? = RELEASES_URL): List<GitHubReleaseDto>? {
        url ?: return null
        val response: HttpResponse = httpClient.get(url) {
            header("Accept", GITHUB_ACCEPT)
            header("User-Agent", GITHUB_USER_AGENT)
            header("X-GitHub-Api-Version", GITHUB_API_VERSION)
            parameter("per_page", RELEASES_PER_PAGE)
        }
        if (!response.status.isSuccess()) return null
        return response.body<List<GitHubReleaseDto>>()
    }

    /** `/releases/latest` — последний опубликованный не-prerelease и не-draft релиз. */
    private suspend fun fetchLatestRelease(): GitHubReleaseDto? {
        val url = RELEASES_URL?.plus("/latest") ?: return null
        val response: HttpResponse = httpClient.get(url) {
            header("Accept", GITHUB_ACCEPT)
            header("User-Agent", GITHUB_USER_AGENT)
            header("X-GitHub-Api-Version", GITHUB_API_VERSION)
        }
        if (!response.status.isSuccess()) return null
        return response.body<GitHubReleaseDto>()
    }

    private companion object {
        const val GITHUB_TIMEOUT_MS = 10_000L
        const val GITHUB_ACCEPT = "application/vnd.github+json"
        const val GITHUB_USER_AGENT = "YANIME-Android"
        const val GITHUB_API_VERSION = "2022-11-28"
        const val YANIME_CHANGELOG_URL = "https://raw.githubusercontent.com/MraK0ff/YANIME/main/docs/changelog/yanime.json"

        /** Максимум GitHub на страницу: беты идут часто и не должны вытеснять последний стабильный релиз. */
        const val RELEASES_PER_PAGE = 100

        /** null, когда репозиторий обновлений не сконфигурирован — проверка просто выключена. */
        val ORIGINAL_RELEASES_URL = "https://api.github.com/repos/Helandy/YummyTV/releases"

        val RELEASES_URL: String? =
            if (UpdateConfig.GITHUB_OWNER.isNotBlank() && UpdateConfig.GITHUB_REPO.isNotBlank()) {
                "https://api.github.com/repos/" +
                        "${UpdateConfig.GITHUB_OWNER}/${UpdateConfig.GITHUB_REPO}/releases"
            } else {
                null
            }
    }
}


@Serializable
private data class YanimeChangelogDto(
    val releases: List<Release> = emptyList(),
) {
    @Serializable
    data class Release(
        val version: String,
        @SerialName("published_at") val publishedAt: String? = null,
        val prerelease: Boolean = false,
        val notes: List<String> = emptyList(),
        @SerialName("commit_overrides") val commitOverrides: List<CommitOverride> = emptyList(),
    )

    @Serializable
    data class CommitOverride(
        val sha: String,
        val tag: String? = null,
        val title: String,
        val description: String? = null,
    )
}
