package su.afk.yummy.tv.feature.settings.mapper

import su.afk.yummy.tv.domain.update.model.AppReleaseNotes
import su.afk.yummy.tv.domain.update.utils.compareVersions
import su.afk.yummy.tv.feature.settings.model.ReleaseNoteItem

private val GENERATED_CARD = Regex(
    """<!-- YANIME-CARD\|([^|]*)\|([^|]*)\|([^|]*) -->\s*(.*?)\s*<!-- /YANIME-CARD -->""",
    setOf(RegexOption.DOT_MATCHES_ALL),
)
private const val GENERATED_START = "<!-- YANIME-AUTO-CHANGELOG:START -->"
private const val GENERATED_END = "<!-- YANIME-AUTO-CHANGELOG:END -->"

internal fun buildReleaseNoteItems(
    currentVersion: String,
    yanimeHistory: List<AppReleaseNotes>,
    originalHistory: List<AppReleaseNotes>,
    latestYanimeRelease: AppReleaseNotes?,
): List<ReleaseNoteItem> {
    val generatedCards = latestYanimeRelease?.changelog
        ?.substringAfter(GENERATED_START, "")
        ?.substringBefore(GENERATED_END)
        ?.let { body -> GENERATED_CARD.findAll(body) }
        ?.map { match ->
            GeneratedCard(
                title = match.groupValues[1].trim(),
                date = match.groupValues[2].trim().ifBlank { null },
                version = match.groupValues[3].trim(),
                notes = match.groupValues[4].trim(),
            )
        }
        .orEmpty()

    val yanimeItems = yanimeHistory.map { release ->
        val version = release.version.substringBefore('-')
        ReleaseNoteItem(
            version = version,
            title = "YANIME · $version",
            date = release.publishedDate?.toDisplayDate(),
            notes = release.changelog.trim(),
            isPrerelease = release.isPrerelease,
            isCurrent = compareVersions(version, currentVersion) == 0,
        )
    }

    val fallbackYanimeItems = buildList<ReleaseNoteItem> {
        if (yanimeItems.isEmpty()) {
            generatedCards
                .filter { it.title == "YANIME" && it.version.isNotBlank() }
                .forEach { card ->
                    add(
                        ReleaseNoteItem(
                            version = card.version,
                            title = "YANIME · ${card.version}",
                            date = card.date,
                            notes = card.notes,
                            isPrerelease = false,
                            isCurrent = compareVersions(card.version, currentVersion) == 0,
                        ),
                    )
                }
        }
    }

    val generatedOriginalByVersion = generatedCards
        .filter { it.title == "YummyTV" && it.version.isNotBlank() }
        .associateBy { it.version }

    val originalItems = originalHistory.map { release ->
        val version = release.version.substringBefore('-')
        val generated = generatedOriginalByVersion[version]
        ReleaseNoteItem(
            version = version,
            title = "YummyTV · $version",
            date = release.publishedDate?.toDisplayDate() ?: generated?.date,
            notes = (generated?.notes ?: release.changelog).withoutIssueLinks().trim(),
            isPrerelease = release.isPrerelease,
            isCurrent = false,
        )
    }

    return buildList {
        addAll(yanimeItems.ifEmpty { fallbackYanimeItems })
        addAll(originalItems)
    }
}

private data class GeneratedCard(
    val title: String,
    val date: String?,
    val version: String,
    val notes: String,
)

private val ISSUE_MARKDOWN_LINK = Regex(
    """\[([^\]]+)]\(https?://github\.com/[^/]+/[^/]+/issues/[^)]+\)""",
    RegexOption.IGNORE_CASE,
)
private val ISSUE_URL = Regex(
    """https?://github\.com/[^/]+/[^/]+/issues/\d+""",
    RegexOption.IGNORE_CASE,
)

private fun String.withoutIssueLinks(): String =
    ISSUE_URL.replace(ISSUE_MARKDOWN_LINK.replace(this) { it.groupValues[1] }, "")
        .replace(Regex("""[ \t]+\n"""), "")
        .replace(Regex("""\n{3,}"""), "\n\n")

private fun String.toDisplayDate(): String {
    val parts = split('-')
    return if (parts.size == 3) parts.reversed().joinToString(".") else this
}
