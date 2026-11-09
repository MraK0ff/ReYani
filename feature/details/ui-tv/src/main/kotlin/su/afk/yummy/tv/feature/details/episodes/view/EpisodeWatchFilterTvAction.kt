package su.afk.yummy.tv.feature.details.episodes.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.feature.details.episodes.model.EpisodeWatchFilter
import su.afk.yummy.tv.feature.details.presentation.R

@Composable
internal fun EpisodeWatchFilterTvAction(
    selected: EpisodeWatchFilter,
    onSelected: (EpisodeWatchFilter) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.FilterList, contentDescription = null)
            Text(stringResource(selected.labelRes()))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EpisodeWatchFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(stringResource(filter.labelRes())) },
                    onClick = {
                        expanded = false
                        onSelected(filter)
                    },
                )
            }
        }
    }
}

private fun EpisodeWatchFilter.labelRes(): Int = when (this) {
    EpisodeWatchFilter.All -> R.string.details_episodes_filter_all
    EpisodeWatchFilter.Unwatched -> R.string.details_episodes_filter_unwatched
    EpisodeWatchFilter.Watched -> R.string.details_episodes_filter_watched
}
