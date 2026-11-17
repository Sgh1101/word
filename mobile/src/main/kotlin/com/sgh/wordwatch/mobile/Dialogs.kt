package com.sgh.wordwatch.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sgh.wordwatch.core.Word
import com.sgh.wordwatch.core.WordBook
import com.sgh.wordwatch.core.WordParser

/** 여러 줄 붙여넣기 → 미리보기 → 추가. */
@Composable
fun PasteDialog(
    initial: String,
    existing: List<Word>,
    onDismiss: () -> Unit,
    onConfirm: (List<Word>) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(initial) }
    val parsed = remember(text) { WordParser.parse(text, System.currentTimeMillis()) }
    val preview = remember(parsed, existing) { WordBook.preview(existing, parsed) }
    val newCount = preview.count { it.status == WordBook.Status.NEW }
    val updCount = preview.count { it.status == WordBook.Status.UPDATE }
    val sameCount = preview.count { it.status == WordBook.Status.SAME }
    val toAdd = preview.filter { it.status != WordBook.Status.SAME }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("목록 붙여넣기") },
        text = {
            Column {
                Text(
                    "한 줄에 단어 하나. 예) apple - 사과 / apple: 사과 / apple 사과 / 엑셀 복사",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 220.dp),
                    placeholder = { Text("apple - 사과\nbanana - 바나나\ngive up 포기하다") },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "새 단어 $newCount · 뜻 갱신 $updCount · 이미 있음 $sameCount",
                    style = MaterialTheme.typography.labelLarge,
                )
                LazyColumn(Modifier.heightIn(max = 180.dp)) {
                    items(preview) { item ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                when (item.status) {
                                    WordBook.Status.NEW -> "+ "
                                    WordBook.Status.UPDATE -> "↻ "
                                    WordBook.Status.SAME -> "= "
                                },
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(item.word.term, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.45f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.word.meaning, modifier = Modifier.weight(0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(toAdd.map { it.word }) }, enabled = toAdd.isNotEmpty()) {
                Text("${toAdd.size}개 추가")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

/** 단어 수정 / 삭제. */
@Composable
fun EditDialog(word: Word, onDismiss: () -> Unit, onSave: (Word) -> Unit, onDelete: () -> Unit) {
    var term by rememberSaveable { mutableStateOf(word.term) }
    var meaning by rememberSaveable { mutableStateOf(word.meaning) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("단어 수정") },
        text = {
            Column {
                OutlinedTextField(value = term, onValueChange = { term = it }, label = { Text("영어") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = meaning, onValueChange = { meaning = it }, label = { Text("뜻") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = term.isNotBlank(),
                onClick = { onSave(word.copy(term = term.trim(), meaning = meaning.trim())) },
            ) { Text("저장") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
