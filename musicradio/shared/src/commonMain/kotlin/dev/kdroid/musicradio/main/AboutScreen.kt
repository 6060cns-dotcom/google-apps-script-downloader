package dev.kdroid.musicradio.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import dev.kdroid.musicradio.platform.Platform
import dev.kdroid.musicradio.ui.GitHubMark
import dev.kdroid.musicradio.ui.LinkedText
import dev.kdroid.musicradio.ui.SectionHeader
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.about_credit
import musicradio.shared.generated.resources.about_licenses
import musicradio.shared.generated.resources.about_original
import musicradio.shared.generated.resources.about_repo
import musicradio.shared.generated.resources.about_version
import musicradio.shared.generated.resources.app_icon
import musicradio.shared.generated.resources.app_name
import musicradio.shared.generated.resources.app_tagline
import musicradio.shared.generated.resources.dev_avatar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val DEVELOPER = "abaye"
private const val DEVELOPER_URL = "https://github.com/abaye123"
private const val REPO_LABEL = "github.com/abaye123/MusicRadio"
private const val REPO_URL = "https://github.com/abaye123/MusicRadio"
private const val ORIGINAL_LABEL = "KdroidRadioDesktop"
private const val ORIGINAL_URL = "https://github.com/kdroidFilter/KdroidRadioDesktop"
private const val ORIGINAL_AUTHOR = "kdroidFilter"
private const val ORIGINAL_AUTHOR_URL = "https://github.com/kdroidFilter"

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val libraries by produceLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    Box(modifier.fillMaxSize()) {
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier.widthIn(max = 860.dp).fillMaxSize().align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            header = {
                item { AboutHeader() }
            },
        )
    }
}

@Composable
private fun AboutHeader() {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Image(
                painterResource(Res.drawable.app_icon),
                null,
                Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)),
            )
            Column {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(Res.string.app_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Only shown when the platform actually reports a version, which a packaged build always
        // does and a run from source never does. A hardcoded fallback used to stand in here, so a
        // build that stopped carrying its version claimed to be 1.0.0 for as long as nobody
        // noticed - and the version line is the one thing a user reads to check exactly that.
        // The sidebar hides itself the same way.
        val version = Platform.appVersion
        if (version.isNotEmpty()) {
            Text(
                stringResource(Res.string.about_version, version),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        CreditRow {
            Image(
                painterResource(Res.drawable.dev_avatar),
                null,
                Modifier.size(26.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            LinkedText(
                text = stringResource(Res.string.about_credit, DEVELOPER),
                links = listOf(DEVELOPER to DEVELOPER_URL),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        CreditRow {
            Icon(
                GitHubMark,
                null,
                Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinkedText(
                text = stringResource(Res.string.about_repo, REPO_LABEL),
                links = listOf(REPO_LABEL to REPO_URL),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        LinkedText(
            text = stringResource(Res.string.about_original, ORIGINAL_LABEL, ORIGINAL_AUTHOR),
            links = listOf(ORIGINAL_LABEL to ORIGINAL_URL, ORIGINAL_AUTHOR to ORIGINAL_AUTHOR_URL),
            style = MaterialTheme.typography.bodySmall,
        )

        SectionHeader(stringResource(Res.string.about_licenses))
    }
}

@Composable
private fun CreditRow(content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = { content() },
    )
}
