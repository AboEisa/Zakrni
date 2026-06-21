package com.zakrni.app.clean.ui.compose.quran

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.clean.domain.models.DomainSurah
import com.zakrni.app.clean.ui.theme.AyahTextStyle
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.QuranFamily
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.LoadingState
import com.zakrni.app.clean.ui.theme.components.ZTopBar
import com.zakrni.app.clean.ui.utils.QuranUtils
import com.zakrni.app.clean.ui.utils.SurahTafsirProvider
import com.zakrni.app.clean.ui.viewmodels.QuranViewModel

/**
 * Mushaf reader for a single surah: a list view (with per-ayah long-press options) or a
 * continuous "Mushaf" page view, an audio player bar, a reciter picker, and a tafsir sheet.
 */
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuranViewModel = hiltViewModel(),
) {
    val verses by viewModel.verses.collectAsStateWithLifecycle()
    val currentSurah by viewModel.currentSurah.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isAudioLoading by viewModel.isAudioLoading.collectAsStateWithLifecycle()
    val audioProgress by viewModel.audioProgress.collectAsStateWithLifecycle()
    val audioDuration by viewModel.audioDuration.collectAsStateWithLifecycle()
    val playingSurah by viewModel.currentPlayingSurah.collectAsStateWithLifecycle()
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()

    val isArabic = isArabicUi()
    val context = LocalContext.current

    var showReciterSheet by remember { mutableStateOf(false) }
    var showTafsirSheet by remember { mutableStateOf(false) }
    var mushafMode by remember { mutableStateOf(true) }
    var optionsAyah by remember { mutableStateOf<DomainAyah?>(null) }

    LaunchedEffect(surahNumber) { viewModel.loadQuranVerses(surahNumber) }
    DisposableEffect(Unit) { onDispose { viewModel.stopAudio() } }

    val isThisSurahPlaying = isPlaying && playingSurah == surahNumber
    val reciterName = remember(selectedReciter, isArabic) { viewModel.getCurrentReciterName() }

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(
            title = surahTitle(currentSurah, surahNumber, isArabic),
            onBack = onBack,
            action = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (mushafMode) Icons.Filled.ViewAgenda else Icons.Filled.AutoStories,
                        contentDescription = stringResource(if (mushafMode) R.string.qrn_list_mode else R.string.qrn_mushaf_mode),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp).clickable { mushafMode = !mushafMode }.padding(7.dp),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = stringResource(R.string.qrn_tafsir),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp).clickable { showTafsirSheet = true }.padding(7.dp),
                    )
                }
            },
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                error != null && verses.isEmpty() && !isLoading -> ErrorState(
                    message = error ?: stringResource(R.string.qrn_loading_error),
                    onRetry = { viewModel.clearError(); viewModel.loadQuranVerses(surahNumber) },
                    retryLabel = stringResource(R.string.qrn_retry),
                )

                isLoading && verses.isEmpty() -> LoadingState()

                verses.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.qrn_reader_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                mushafMode -> MushafView(surahNumber = surahNumber, verses = verses, isArabic = isArabic)

                else -> AyahList(
                    surahNumber = surahNumber,
                    verses = verses,
                    isArabic = isArabic,
                    highlighted = optionsAyah?.number,
                    onAyahClick = { optionsAyah = it },
                )
            }
        }

        AnimatedVisibility(
            visible = verses.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            AudioPlayerBar(
                isPlaying = isThisSurahPlaying,
                isLoading = isAudioLoading && playingSurah == surahNumber,
                progressMs = if (playingSurah == surahNumber) audioProgress else 0,
                durationMs = if (playingSurah == surahNumber) audioDuration else 0,
                reciterName = reciterName,
                isArabic = isArabic,
                onPlayPause = {
                    if (isThisSurahPlaying) viewModel.pauseAudio()
                    else viewModel.playSurahAudio(surahNumber)
                },
                onSeek = { viewModel.seekToPosition(it) },
                onPickReciter = { showReciterSheet = true },
            )
        }
    }

    // Per-ayah options sheet.
    optionsAyah?.let { ayah ->
        AyahOptionsSheet(
            ayahNumber = if (ayah.numberInSurah > 0) ayah.numberInSurah else 1,
            isArabic = isArabic,
            onListen = {
                viewModel.playAyahAudio(ayah.number, surahNumber)
                optionsAyah = null
            },
            onTafsir = { optionsAyah = null; showTafsirSheet = true },
            onCopy = {
                Toast.makeText(context, R.string.qrn_ayah_copied, Toast.LENGTH_SHORT).show()
                optionsAyah = null
            },
            onShare = {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, ayah.text),
                        null,
                    ),
                )
                optionsAyah = null
            },
            ayahText = ayah.text,
            onDismiss = { optionsAyah = null },
        )
    }

    if (showReciterSheet) {
        ReciterPickerSheet(
            reciters = remember(isArabic) { viewModel.getAvailableReciters() },
            selectedId = selectedReciter,
            onSelect = { id -> viewModel.setReciter(id); showReciterSheet = false },
            onDismiss = { showReciterSheet = false },
        )
    }

    if (showTafsirSheet) {
        val tafsirText = remember(surahNumber, isArabic) {
            SurahTafsirProvider.getTafsirText(context, surahNumber, isArabic)
        }
        TafsirSheet(
            title = stringResource(R.string.qrn_tafsir_title),
            tafsirText = tafsirText,
            onDismiss = { showTafsirSheet = false },
        )
    }
}

@Composable
private fun AyahList(
    surahNumber: Int,
    verses: List<DomainAyah>,
    isArabic: Boolean,
    highlighted: Int?,
    onAyahClick: (DomainAyah) -> Unit,
) {
    val showBismillah = surahNumber != 1 && surahNumber != 9
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(key = "header") {
            SurahReaderHeader(surahNumber = surahNumber, isArabic = isArabic, showBismillah = showBismillah)
        }
        items(verses, key = { it.number }) { ayah ->
            AyahRow(
                ayah = ayah,
                isArabic = isArabic,
                highlighted = highlighted == ayah.number,
                onClick = { onAyahClick(ayah) },
            )
        }
    }
}

@Composable
private fun MushafView(surahNumber: Int, verses: List<DomainAyah>, isArabic: Boolean) {
    val showBismillah = surahNumber != 1 && surahNumber != 9
    // Continuous justified Mushaf text with ornate ayah-end markers ﴿n﴾.
    val pageText = remember(verses, isArabic) {
        buildString {
            verses.forEach { ayah ->
                append(ayah.text.trim())
                append("  ﴿")
                append(localizeNumber(if (ayah.numberInSurah > 0) ayah.numberInSurah else 1, isArabic))
                append("﴾  ")
            }
        }.trim()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                if (showBismillah) {
                    Text(
                        text = stringResource(R.string.qrn_bismillah),
                        style = AyahTextStyle.copy(fontFamily = QuranFamily),
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    )
                }
                Text(
                    text = pageText,
                    style = AyahTextStyle.copy(fontFamily = QuranFamily),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SurahReaderHeader(surahNumber: Int, isArabic: Boolean, showBismillah: Boolean) {
    val revelation = QuranUtils.getRevelationType(surahNumber, isArabic)
    val ayahCount = QuranUtils.getAyahCount(surahNumber)
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$revelation  •  ${stringResource(R.string.qrn_verses_count, ayahCount)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        if (showBismillah) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.qrn_bismillah),
                style = AyahTextStyle.copy(fontFamily = QuranFamily),
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
        } else {
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AyahRow(ayah: DomainAyah, isArabic: Boolean, highlighted: Boolean, onClick: () -> Unit) {
    val number = if (ayah.numberInSurah > 0) ayah.numberInSurah else 1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (highlighted) BrandGold.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(12.dp),
            )
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = ayah.text,
            style = AyahTextStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 4.dp),
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(34.dp).background(BrandGold.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = localizeNumber(number, isArabic),
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = QuranFamily),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyahOptionsSheet(
    ayahNumber: Int,
    isArabic: Boolean,
    ayahText: String,
    onListen: () -> Unit,
    onTafsir: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text(
                text = "${stringResource(R.string.qrn_ayah_options_title)} • ${localizeNumber(ayahNumber, isArabic)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            OptionRow(Icons.Filled.PlayArrow, stringResource(R.string.qrn_ayah_listen), onListen)
            OptionRow(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.qrn_ayah_tafsir), onTafsir)
            OptionRow(Icons.Filled.ContentCopy, stringResource(R.string.qrn_ayah_copy)) {
                clipboard.setText(AnnotatedString(ayahText)); onCopy()
            }
            OptionRow(Icons.Filled.Share, stringResource(R.string.qrn_ayah_share), onShare)
        }
    }
}

@Composable
private fun OptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun surahTitleFallback(surahNumber: Int, isArabic: Boolean): String =
    if (isArabic) "سورة $surahNumber" else "Surah $surahNumber"

private fun surahTitle(surah: DomainSurah?, surahNumber: Int, isArabic: Boolean): String {
    if (surah == null || surah.number != surahNumber) return surahTitleFallback(surahNumber, isArabic)
    return if (isArabic) surah.name else surah.englishName.ifBlank { surahTitleFallback(surahNumber, false) }
}
