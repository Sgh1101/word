package com.sgh.wordwatch.mobile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgh.wordwatch.core.Word
import com.sgh.wordwatch.core.WordIndex
import com.sgh.wordwatch.core.WordParser

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WordScreen(repo: WordRepository, sharedText: String?, onSharedTextConsumed: () -> Unit) {
    val words by repo.words.collectAsStateWithLifecycle()
    val sync by repo.sync.collectAsStateWithLifecycle()

    var pasteText by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<Word?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var filterKey by rememberSaveable { mutableStateOf(WordIndex.ALL) }

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            pasteText = sharedText
            onSharedTextConsumed()
        }
    }

    val groups = remember(words) { WordIndex.firstGroups(words) }
    if (filterKey != WordIndex.ALL && groups.none { it.key == filterKey }) filterKey = WordIndex.ALL
    val shown = remember(words, filterKey) { WordIndex.filter(words, filterKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("워치 단어장 · ${words.size}개")
                        Text(
                            when (sync) {
                                WordRepository.SyncState.IDLE -> ""
                                WordRepository.SyncState.SENDING -> "시계로 보내는 중…"
                                WordRepository.SyncState.SENT -> "시계로 보냄 ✓"
                                WordRepository.SyncState.FAILED -> "시계 연결 안 됨 (나중에 자동 전송)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { menuOpen = true }) { Text("⋮") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("시계로 다시 보내기") }, onClick = { menuOpen = false; repo.resync() })
                        DropdownMenuItem(text = { Text("전체 삭제") }, onClick = { menuOpen = false; confirmClear = true })
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
        ) {
            AddWordRow(onAdd = { repo.addAll(listOf(it)) }, onPasteClick = { pasteText = "" })

            if (groups.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        FilterChip(
                            selected = filterKey == WordIndex.ALL,
                            onClick = { filterKey = WordIndex.ALL },
                            label = { Text("전체") },
                        )
                    }
                    items(groups, key = { it.key }) { g ->
                        FilterChip(
                            selected = filterKey == g.key,
                            onClick = { filterKey = g.key },
                            label = { Text("${g.key} ${g.count}") },
                        )
                    }
                }
            }

            if (words.isEmpty()) {
                Text(
                    "단어를 입력하거나 '목록 붙여넣기'로 한 번에 넣어보세요.\n\n예)\napple - 사과\ngive up 포기하다\nrun: 달리다",
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val listState = rememberLazyListState()
            val sections = remember(shown) { shown.groupBy { WordIndex.firstKey(it.term) } }
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                sections.forEach { (letter, list) ->
                    stickyHeader(key = "h-$letter") {
                        Text(
                            letter,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    items(list, key = { "w-" + it.key }) { w ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { editing = w }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            Text(w.term, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.4f))
                            Text(
                                w.meaning,
                                modifier = Modifier.weight(0.6f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    pasteText?.let { initial ->
        PasteDialog(
            initial = initial,
            existing = words,
            onDismiss = { pasteText = null },
            onConfirm = { repo.addAll(it); pasteText = null },
        )
    }

    editing?.let { w ->
        EditDialog(
            word = w,
            onDismiss = { editing = null },
            onSave = { repo.replace(w, it); editing = null },
            onDelete = { repo.delete(w); editing = null },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("전체 삭제") },
            text = { Text("단어 ${words.size}개를 모두 지울까요? 시계에서도 지워져요.") },
            confirmButton = { TextButton(onClick = { repo.clear(); confirmClear = false }) { Text("삭제") } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("취소") } },
        )
    }
}

@Composable
private fun AddWordRow(onAdd: (Word) -> Unit, onPasteClick: () -> Unit) {
    var term by rememberSaveable { mutableStateOf("") }
    var meaning by rememberSaveable { mutableStateOf("") }
    val termFocus = remember { FocusRequester() }

    fun submit() {
        val parsed = WordParser.parseLine(if (meaning.isBlank()) term else "${term.trim()}\t${meaning.trim()}")
        if (parsed != null) {
            onAdd(parsed.copy(addedAt = System.currentTimeMillis()))
            term = ""; meaning = ""
            termFocus.requestFocus()
        }
    }

    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = term,
                onValueChange = { term = it },
                label = { Text("영어") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(termFocus),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                label = { Text("뜻") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { submit() }, enabled = term.isNotBlank(), modifier = Modifier.weight(1f)) { Text("추가") }
            OutlinedButton(onClick = onPasteClick, modifier = Modifier.weight(1f)) { Text("목록 붙여넣기") }
        }
    }
}
