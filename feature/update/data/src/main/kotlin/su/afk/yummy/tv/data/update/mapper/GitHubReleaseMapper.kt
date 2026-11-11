package su.afk.yummy.tv.data.update.mapper

import su.afk.yummy.tv.data.update.dto.GitHubReleaseDto
import su.afk.yummy.tv.domain.update.model.AppRelease
import su.afk.yummy.tv.domain.update.model.AppReleaseNotes
import su.afk.yummy.tv.domain.update.utils.PRERELEASE_VERSION_SUFFIX

private const val STABLE_TAG_PREFIX = 'v'
private const val BETA_TAG_PREFIX = 'b'

/** Длина даты `yyyy-MM-dd` в начале ISO-времени `published_at` (`2026-09-20T12:00:00Z`). */
private const val ISO_DATE_LENGTH = 10

private val CHANGELOG_LIST_ITEM = Regex("""^(\s*)(?:[-*+]|\d+[.)])\s+""")
private val CHANGELOG_TAG = Regex("""^\[([A-Z][A-Z0-9_-]*)]""", RegexOption.IGNORE_CASE)

/**
 * Убирает внутренние записи из текста релиза. Запись [INTERNAL] и её продолжение скрываются
 * до следующего элемента того же или более высокого уровня списка, нового тега или заголовка.
 * Обычный текст и пользовательские записи сохраняются.
 */
private fun String?.withoutInternalChangelogEntries(): String {
    if (isNullOrBlank()) return orEmpty()

    val visibleLines = mutableListOf<String>()
    var hiddenIndent: Int? = null

    for (line in lines()) {
        val trimmed = line.trimStart()
        val isHeading = trimmed.startsWith("#")
        val listMatch = CHANGELOG_LIST_ITEM.find(line)
        val content = when {
            isHeading -> trimmed.dropWhile { it == '#' }.trimStart()
            listMatch != null -> line.substring(listMatch.range.last + 1).trimStart()
            else -> trimmed
        }
        val tag = CHANGELOG_TAG.find(content)?.groupValues?.getOrNull(1)

        if (isHeading) {
            hiddenIndent = if (tag.equals("INTERNAL", ignoreCase = true)) -1 else null
            if (!tag.equals("INTERNAL", ignoreCase = true)) visibleLines += line
            continue
        }

        if (listMatch != null) {
            val itemIndent = listMatch.groupValues[1].length
            if (hiddenIndent != null && itemIndent <= hiddenIndent) hiddenIndent = null
            if (tag.equals("INTERNAL", ignoreCase = true)) {
                hiddenIndent = itemIndent
                continue
            }
            if (hiddenIndent != null) continue
            visibleLines += line
            continue
        }

        if (tag != null) {
            if (tag.equals("INTERNAL", ignoreCase = true)) {
                hiddenIndent = -1
                continue
            }
            hiddenIndent = null
            visibleLines += line
            continue
        }

        if (hiddenIndent == null) visibleLines += line
    }

    return visibleLines.joinToString("\n").trim()
}

/**
 * Релиз без приложенного APK установить нельзя, поэтому такой релиз (как и черновик) маппится
 * в null — для вызывающего это «обновления нет».
 *
 * Бета публикуется тегом `b1.21.1.2` с флагом pre-release; её версия получает суффикс `-beta`
 * (`1.21.1.2-beta`), как `versionName` бета-сборки, чтобы стабильная той же версии считалась новее.
 */
internal fun GitHubReleaseDto.toDomain(): AppRelease? {
    if (draft) return null
    val apkUrl = (assets.firstOrNull { it.browserDownloadUrl.endsWith(".apk", ignoreCase = true) } ?: assets.firstOrNull())
        ?.browserDownloadUrl
        ?: return null
    return AppRelease(
        version = domainVersion(),
        changelog = body.withoutInternalChangelogEntries(),
        apkUrl = apkUrl,
        isPrerelease = isPrerelease(),
    )
}

/** Список изменений релиза для истории версий; APK не требуется, черновик маппится в null. */
internal fun GitHubReleaseDto.toReleaseNotes(): AppReleaseNotes? {
    if (draft) return null
    return AppReleaseNotes(
        version = domainVersion(),
        publishedDate = publishedAt?.takeIf { it.length >= ISO_DATE_LENGTH }?.take(ISO_DATE_LENGTH),
        changelog = body.withoutInternalChangelogEntries(),
        isPrerelease = isPrerelease(),
    )
}

private fun GitHubReleaseDto.isPrerelease(): Boolean = prerelease || tagName.startsWith(BETA_TAG_PREFIX)

private fun GitHubReleaseDto.domainVersion(): String {
    val version = tagName.removePrefix(STABLE_TAG_PREFIX.toString()).removePrefix(BETA_TAG_PREFIX.toString())
    return if (isPrerelease() && '-' !in version) version + PRERELEASE_VERSION_SUFFIX else version
}
