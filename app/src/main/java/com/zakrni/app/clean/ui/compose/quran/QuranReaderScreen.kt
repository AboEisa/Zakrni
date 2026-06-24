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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zakrni.app.R
import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.clean.domain.models.DomainSurah
import com.zakrni.app.clean.ui.theme.AyahTextStyle
import com.zakrni.app.clean.ui.theme.BrandGold
import com.zakrni.app.clean.ui.theme.QuranFamily
import com.zakrni.app.clean.ui.theme.components.ErrorState
import com.zakrni.app.clean.ui.theme.components.ShimmerBox
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
    val ayahTafsir by viewModel.ayahTafsir.collectAsStateWithLifecycle()
    val ayahTafsirLoading by viewModel.ayahTafsirLoading.collectAsStateWithLifecycle()
    val playingAyahNumber by viewModel.playingAyahNumber.collectAsStateWithLifecycle()

    val isArabic = isArabicUi()
    val context = LocalContext.current

    var showReciterSheet by remember { mutableStateOf(false) }
    var showTafsirSheet by remember { mutableStateOf(false) }
    var showAyahTafsir by remember { mutableStateOf(false) }
    var mushafMode by remember { mutableStateOf(readReaderMushaf(context)) }
    var fontScale by remember { mutableStateOf(readReaderFontScale(context)) }
    var readingMode by remember { mutableStateOf(readReaderMode(context)) }
    var showReaderSettings by remember { mutableStateOf(false) }
    var optionsAyah by remember { mutableStateOf<DomainAyah?>(null) }

    LaunchedEffect(surahNumber) { viewModel.loadQuranVerses(surahNumber) }
    DisposableEffect(Unit) { onDispose { viewModel.stopAudio() } }

    // Remember this surah as the last-read one for the Home "continue reading" card.
    LaunchedEffect(currentSurah, surahNumber) {
        val s = currentSurah
        if (s != null && s.number == surahNumber) {
            writeLastRead(context, surahNumber, s.name, s.englishName)
        }
    }

    val isThisSurahPlaying = isPlaying && playingSurah == surahNumber
    val reciterName = remember(selectedReciter, isArabic) { viewModel.getCurrentReciterName() }

    // One continuous progress across the whole surah (which ayah + how far into it),
    // instead of a per-ayah bar that resets each verse.
    val playingIndex = remember(playingAyahNumber, verses) {
        if (playingAyahNumber == null) -1 else verses.indexOfFirst { it.number == playingAyahNumber }
    }
    val surahProgress = if (playingIndex >= 0 && verses.isNotEmpty()) {
        val within = if (audioDuration > 0) (audioProgress.toFloat() / audioDuration).coerceIn(0f, 1f) else 0f
        ((playingIndex + within) / verses.size).coerceIn(0f, 1f)
    } else -1f
    val progressLabel = if (playingIndex >= 0) {
        stringResource(R.string.qrn_ayah_of, localizeNumber(playingIndex + 1, isArabic), localizeNumber(verses.size, isArabic))
    } else ""

    Column(modifier = modifier.fillMaxSize()) {
        ZTopBar(
            title = surahTitle(currentSurah, surahNumber, isArabic),
            onBack = onBack,
            action = {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = stringResource(R.string.rd_reader_settings),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp).clickable { showReaderSettings = true }.padding(7.dp),
                )
            },
        )

        Box(modifier = Modifier.weight(1f)) {
            when {
                error != null && verses.isEmpty() -> ErrorState(
                    message = error ?: stringResource(R.string.qrn_loading_error),
                    onRetry = { viewModel.clearError(); viewModel.loadQuranVerses(surahNumber) },
                    retryLabel = stringResource(R.string.qrn_retry),
                )

                // A valid surah always has verses, so "empty" means it's still loading —
                // show an animated Mushaf-page skeleton instead of an "unavailable" message.
                verses.isEmpty() -> QuranLoadingState()

                mushafMode -> MushafView(
                    surahNumber = surahNumber,
                    verses = verses,
                    isArabic = isArabic,
                    highlightedNumber = playingAyahNumber ?: optionsAyah?.number,
                    playingAyahNumber = playingAyahNumber,
                    onAyahClick = { optionsAyah = it },
                    fontScale = fontScale,
                    readingMode = readingMode,
                )

                else -> AyahList(
                    surahNumber = surahNumber,
                    verses = verses,
                    isArabic = isArabic,
                    highlighted = playingAyahNumber ?: optionsAyah?.number,
                    playingAyahNumber = playingAyahNumber,
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
                surahProgress = surahProgress,
                progressLabel = progressLabel,
                reciterName = reciterName,
                isArabic = isArabic,
                onPlayPause = {
                    if (isThisSurahPlaying) viewModel.pauseAudio()
                    else if (playingAyahNumber != null) viewModel.resumeAudio()
                    else viewModel.playFollow(surahNumber, verses, 0)
                },
                onSeek = { viewModel.seekToPosition(it) },
                onSeekToAyah = { frac ->
                    val idx = (frac * verses.size).toInt().coerceIn(0, verses.lastIndex)
                    viewModel.playFollow(surahNumber, verses, idx)
                },
                onPickReciter = { showReciterSheet = true },
            )
        }
    }

    // Per-ayah options sheet.
    optionsAyah?.let { ayah ->
        val ayahInSurah = if (ayah.numberInSurah > 0) ayah.numberInSurah else 1
        var favState by remember(ayah) { mutableStateOf(isFavorite(context, surahNumber, ayahInSurah)) }
        AyahOptionsSheet(
            ayahNumber = ayahInSurah,
            isArabic = isArabic,
            isFavorite = favState,
            onFavorite = {
                val now = toggleFavorite(
                    context,
                    FavoriteAyah(surahNumber, ayahInSurah, surahTitle(currentSurah, surahNumber, isArabic), ayah.text),
                )
                favState = now
                Toast.makeText(
                    context,
                    if (now) R.string.rd_fav_saved else R.string.rd_fav_removed,
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onListen = {
                viewModel.playAyahAudio(ayah.number, surahNumber)
                optionsAyah = null
            },
            onTafsir = {
                viewModel.loadAyahTafsir(surahNumber, ayah.numberInSurah.coerceAtLeast(1))
                showAyahTafsir = true
                optionsAyah = null
            },
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
            onShareImage = {
                shareVerseAsImage(
                    context,
                    ayah.text,
                    "${surahTitle(currentSurah, surahNumber, isArabic)} • ${localizeNumber(ayahInSurah, isArabic)}",
                    context.getString(R.string.app_name),
                )
                optionsAyah = null
            },
            ayahText = ayah.text,
            onDismiss = { optionsAyah = null },
        )
    }

    if (showReaderSettings) {
        ReaderSettingsSheet(
            mushafMode = mushafMode,
            fontScale = fontScale,
            readingMode = readingMode,
            onMushafModeChange = { mushafMode = it; writeReaderMushaf(context, it) },
            onFontScaleChange = { fontScale = it; writeReaderFontScale(context, it) },
            onReadingModeChange = { readingMode = it; writeReaderMode(context, it) },
            onDismiss = { showReaderSettings = false },
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

    // Per-ayah tafsir (Tafsir al-Muyassar), shown when an ayah's "Tafsir" option is tapped.
    if (showAyahTafsir) {
        val text = if (ayahTafsirLoading) stringResource(R.string.rd_common_loading)
        else (ayahTafsir ?: stringResource(R.string.qrn_tafsir_unavailable))
        TafsirSheet(
            title = stringResource(R.string.qrn_ayah_tafsir),
            tafsirText = text,
            onDismiss = { showAyahTafsir = false; viewModel.clearAyahTafsir() },
        )
    }
}

@Composable
private fun AyahList(
    surahNumber: Int,
    verses: List<DomainAyah>,
    isArabic: Boolean,
    highlighted: Int?,
    playingAyahNumber: Int?,
    onAyahClick: (DomainAyah) -> Unit,
) {
    val showBismillah = surahNumber != 1 && surahNumber != 9
    val listState = rememberLazyListState()
    LaunchedEffect(playingAyahNumber) {
        val pin = playingAyahNumber ?: return@LaunchedEffect
        val idx = verses.indexOfFirst { it.number == pin }
        if (idx >= 0) listState.animateScrollToItem(idx + 1)
    }
    LazyColumn(
        state = listState,
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
private fun MushafView(
    surahNumber: Int,
    verses: List<DomainAyah>,
    isArabic: Boolean,
    highlightedNumber: Int?,
    playingAyahNumber: Int?,
    onAyahClick: (DomainAyah) -> Unit,
    fontScale: Float,
    readingMode: Int,
) {
    val showBismillah = surahNumber != 1 && surahNumber != 9
    val palette = readingPalette(readingMode)
    val highlight = palette.accent.copy(alpha = 0.14f)
    val mushafStyle = AyahTextStyle.copy(
        fontFamily = QuranFamily,
        fontSize = (26 * fontScale).sp,
        lineHeight = (46 * fontScale).sp,
    )

    // Continuous justified Mushaf text. Each ayah is tracked by its char range so a tap
    // can resolve which ayah was touched, and the selected ayah gets a highlight span.
    val data = remember(verses, isArabic, highlightedNumber, highlight) {
        val ranges = ArrayList<IntRange>(verses.size)
        val str = buildAnnotatedString {
            verses.forEach { ayah ->
                val start = length
                append(ayah.text.trim())
                append("  ﴿")
                append(localizeNumber(if (ayah.numberInSurah > 0) ayah.numberInSurah else 1, isArabic))
                append("﴾  ")
                val end = length
                if (ayah.number == highlightedNumber) {
                    // Tighten the highlight to the ayah itself (drop the trailing spaces) and
                    // tint the text in the accent colour so it reads as a clean "now playing" mark.
                    val hlEnd = (end - 2).coerceAtLeast(start)
                    addStyle(SpanStyle(color = palette.accent, background = highlight), start, hlEnd)
                }
                ranges.add(start until end)
            }
        }
        str to ranges
    }
    val annotated = data.first
    val ranges = data.second
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()
    LaunchedEffect(playingAyahNumber, layout) {
        val ln = layout ?: return@LaunchedEffect
        val pin = playingAyahNumber ?: return@LaunchedEffect
        val idx = verses.indexOfFirst { it.number == pin }
        if (idx in ranges.indices) {
            val top = ln.getBoundingBox(ranges[idx].first).top
            scrollState.animateScrollTo((top.toInt() - 180).coerceAtLeast(0))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.bg,
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.border),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                if (showBismillah) {
                    Text(
                        text = stringResource(R.string.qrn_bismillah),
                        style = mushafStyle,
                        color = palette.accent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    )
                }
                Text(
                    text = annotated,
                    style = mushafStyle,
                    color = palette.text,
                    textAlign = TextAlign.Justify,
                    onTextLayout = { layout = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(ranges) {
                            detectTapGestures { pos ->
                                val lr = layout ?: return@detectTapGestures
                                val offset = lr.getOffsetForPosition(pos)
                                val idx = ranges.indexOfFirst { offset in it }
                                if (idx in verses.indices) onAyahClick(verses[idx])
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun QuranLoadingState() {
    val widths = listOf(0.92f, 0.72f, 0.86f, 0.62f, 0.80f, 0.70f, 0.50f)
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        widths.forEach { w ->
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(w).height(20.dp),
                shape = RoundedCornerShape(8.dp),
            )
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
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    ayahText: String,
    onListen: () -> Unit,
    onTafsir: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onShareImage: () -> Unit,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text(
                text = ayahText,
                style = AyahTextStyle.copy(fontFamily = QuranFamily),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            )
            Text(
                text = "${stringResource(R.string.qrn_ayah_options_title)} • ${localizeNumber(ayahNumber, isArabic)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            )
            OptionRow(
                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                stringResource(if (isFavorite) R.string.rd_fav_remove else R.string.rd_fav_add),
                onFavorite,
            )
            OptionRow(Icons.Filled.PlayArrow, stringResource(R.string.qrn_ayah_listen), onListen)
            OptionRow(Icons.AutoMirrored.Filled.MenuBook, stringResource(R.string.qrn_ayah_tafsir), onTafsir)
            OptionRow(Icons.Filled.ContentCopy, stringResource(R.string.qrn_ayah_copy)) {
                clipboard.setText(AnnotatedString(ayahText)); onCopy()
            }
            OptionRow(Icons.Filled.Share, stringResource(R.string.qrn_ayah_share), onShare)
            OptionRow(Icons.Filled.Image, stringResource(R.string.rd_share_image), onShareImage)
        }
    }
}

@Composable
private fun OptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    mushafMode: Boolean,
    fontScale: Float,
    readingMode: Int,
    onMushafModeChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onReadingModeChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Text(
                text = stringResource(R.string.rd_reader_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Text(stringResource(R.string.rd_reader_view), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceChip(stringResource(R.string.rd_reader_view_mushaf), mushafMode, Modifier.weight(1f)) { onMushafModeChange(true) }
                ChoiceChip(stringResource(R.string.rd_reader_view_list), !mushafMode, Modifier.weight(1f)) { onMushafModeChange(false) }
            }

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.rd_reader_font), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StepButton(Icons.Filled.Remove) { onFontScaleChange((fontScale - 0.1f).coerceIn(0.8f, 1.6f)) }
                Text("${Math.round(fontScale * 100f)}%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                StepButton(Icons.Filled.Add) { onFontScaleChange((fontScale + 0.1f).coerceIn(0.8f, 1.6f)) }
            }

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.rd_reader_background), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceChip(stringResource(R.string.rd_reader_bg_normal), readingMode == 0, Modifier.weight(1f)) { onReadingModeChange(0) }
                ChoiceChip(stringResource(R.string.rd_reader_bg_sepia), readingMode == 1, Modifier.weight(1f)) { onReadingModeChange(1) }
                ChoiceChip(stringResource(R.string.rd_reader_bg_night), readingMode == 2, Modifier.weight(1f)) { onReadingModeChange(2) }
            }
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(10.dp).size(24.dp),
        )
    }
}

private data class ReadingPalette(val bg: Color, val text: Color, val border: Color, val accent: Color)

@Composable
private fun readingPalette(mode: Int): ReadingPalette = when (mode) {
    1 -> ReadingPalette(Color(0xFFF4E8CE), Color(0xFF4A3B26), Color(0x33000000), Color(0xFF8A6D3B))
    2 -> ReadingPalette(Color(0xFF15120D), Color(0xFFE9D9B6), Color(0x22FFFFFF), Color(0xFF8FD3C8))
    else -> ReadingPalette(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.outlineVariant,
        MaterialTheme.colorScheme.primary,
    )
}

private fun surahTitleFallback(surahNumber: Int, isArabic: Boolean): String =
    if (isArabic) "سورة $surahNumber" else "Surah $surahNumber"

private fun surahTitle(surah: DomainSurah?, surahNumber: Int, isArabic: Boolean): String {
    if (surah == null || surah.number != surahNumber) return surahTitleFallback(surahNumber, isArabic)
    return if (isArabic) surah.name else surah.englishName.ifBlank { surahTitleFallback(surahNumber, false) }
}
