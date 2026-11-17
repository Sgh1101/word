package com.sgh.wordwatch.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.sgh.wordwatch.core.Word
import com.sgh.wordwatch.core.WordIndex

private val ChipBg = Color(0xFF263238)
private val ChipAllBg = Color(0xFF1E3A5F)
private val Accent = Color(0xFF8AB4F8)
private val Dim = Color(0xFFB0B0B0)

private const val CHIPS_PER_ROW = 4

/** 베젤/크라운 회전으로 스크롤 + 오른쪽 스크롤 표시. */
@Composable
private fun RotaryList(
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val focus = remember { FocusRequester() }
    Scaffold(positionIndicator = { PositionIndicator(lazyListState = state) }) {
        LazyColumn(
            state = state,
            contentPadding = contentPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .rotaryScrollable(RotaryScrollableDefaults.behavior(scrollableState = state), focusRequester = focus)
                .focusRequester(focus)
                .focusable(),
            content = content,
        )
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
}

/** 첫 글자(또는 두 번째 글자) 칩 그리드. */
@Composable
fun IndexScreen(
    title: String,
    groups: List<WordIndex.Group>,
    loading: Boolean,
    onAll: () -> Unit,
    onGroup: (String) -> Unit,
    allLabel: String = "전체",
) {
    RotaryList(contentPadding = PaddingValues(top = 22.dp, bottom = 34.dp, start = 14.dp, end = 14.dp)) {
        item {
            Text(
                title,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = Dim,
            )
        }
        if (!loading && groups.isEmpty()) {
            item {
                Text(
                    "폰 앱에서 단어를\n추가하세요",
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                )
            }
            return@RotaryList
        }
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.Center) {
                SmallChip(label = allLabel, count = null, bg = ChipAllBg, onClick = onAll, modifier = Modifier.width(96.dp))
            }
        }
        items(groups.chunked(CHIPS_PER_ROW)) { row ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            ) {
                row.forEach { g ->
                    SmallChip(
                        label = g.key,
                        count = g.count,
                        bg = ChipBg,
                        onClick = { onGroup(g.key) },
                        modifier = Modifier.width(40.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallChip(label: String, count: Int?, bg: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(32.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)) { append(label) }
                if (count != null) withStyle(SpanStyle(fontSize = 9.sp, color = Dim)) { append(" $count") }
            },
            maxLines = 1,
        )
    }
}

/** 단어 목록: 작은 글씨, 한 줄에 단어+뜻 → 화면당 최대한 많이. */
@Composable
fun WordListScreen(title: String, words: List<Word>) {
    RotaryList(contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp, start = 24.dp, end = 18.dp)) {
        item {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Accent, fontWeight = FontWeight.Bold)) { append(title) }
                    withStyle(SpanStyle(color = Dim)) { append(" ${words.size}") }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
            )
        }
        items(words, key = { it.key }) { w ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)) { append(w.term) }
                    if (w.meaning.isNotEmpty()) {
                        append("  ")
                        withStyle(SpanStyle(color = Dim, fontSize = 12.sp)) { append(w.meaning) }
                    }
                },
                lineHeight = 15.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            )
        }
    }
}
