package su.afk.yummy.tv.data.update.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import su.afk.yummy.tv.data.update.dto.GitHubReleaseDto

class GitHubReleaseMapperTest {

    @Test
    fun `internal bullet and its continuation are hidden from release notes`() {
        val body = """
            ## Изменения
            - [FEATURE] Добавлена настройка разделов главной
            - [INTERNAL] Перенесена внутренняя конфигурация
              Подробности технической реализации
              - Вложенная техническая запись
            - [BUGFIX] Исправлено воспроизведение после паузы
        """.trimIndent()

        val notes = GitHubReleaseDto(tagName = "v1.2.3", body = body).toReleaseNotes()

        assertEquals(
            """
            ## Изменения
            - [FEATURE] Добавлена настройка разделов главной
            - [BUGFIX] Исправлено воспроизведение после паузы
            """.trimIndent(),
            notes?.changelog,
        )
    }

    @Test
    fun `internal entries are also hidden from update dialog changelog`() {
        val dto = GitHubReleaseDto(
            tagName = "v1.2.3",
            body = "- [INTERNAL] Внутреннее изменение\n- [FEATURE] Новая функция",
            assets = listOf(GitHubReleaseDto.Asset("https://example.invalid/YANIME.apk")),
        )

        assertEquals("- [FEATURE] Новая функция", dto.toDomain()?.changelog)
    }

    @Test
    fun `plain release text is preserved`() {
        val body = "В этом релизе улучшена стабильность приложения."

        assertEquals(
            body,
            GitHubReleaseDto(tagName = "v1.2.3", body = body).toReleaseNotes()?.changelog,
        )
    }
}
