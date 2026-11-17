package com.sgh.wordwatch.mobile

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.sgh.wordwatch.core.Word
import com.sgh.wordwatch.core.WordBook
import com.sgh.wordwatch.core.WordCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.io.File

/** 단어장 하나를 파일(words.json)에 저장하고, 바뀔 때마다 시계로 보낸다. */
class WordRepository(context: Context) {

    enum class SyncState { IDLE, SENDING, SENT, FAILED }

    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, "words.json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _sync = MutableStateFlow(SyncState.IDLE)
    val sync: StateFlow<SyncState> = _sync.asStateFlow()

    init {
        scope.launch {
            val loaded = runCatching { if (file.exists()) WordCodec.decode(file.readText()) else emptyList() }
                .getOrElse { Log.e(TAG, "load failed", it); emptyList() }
            _words.value = WordBook.sort(loaded)
        }
    }

    fun addAll(incoming: List<Word>) = update { WordBook.merge(it, incoming) }

    fun replace(old: Word, new: Word) = update { list ->
        WordBook.merge(list.filterNot { it.key == old.key }, listOf(new))
    }

    fun delete(word: Word) = update { list -> list.filterNot { it.key == word.key } }

    fun clear() = update { emptyList() }

    /** 시계로 다시 보내기 (수동). */
    fun resync() = scope.launch { pushToWatch(_words.value) }

    private fun update(block: (List<Word>) -> List<Word>) = scope.launch {
        val next = mutex.withLock {
            val n = block(_words.value)
            _words.value = n
            file.writeText(WordCodec.encode(n))
            n
        }
        pushToWatch(next)
    }

    private suspend fun pushToWatch(list: List<Word>) {
        _sync.value = SyncState.SENDING
        try {
            val bytes = WordCodec.encode(list).toByteArray(Charsets.UTF_8)
            val request = PutDataMapRequest.create(PATH_WORDS).apply {
                dataMap.putAsset(KEY_WORDS, Asset.createFromBytes(bytes))
                dataMap.putLong(KEY_TS, System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            Wearable.getDataClient(appContext).putDataItem(request).await()
            _sync.value = SyncState.SENT
        } catch (e: Exception) {
            Log.w(TAG, "sync failed", e)
            _sync.value = SyncState.FAILED
        }
    }

    companion object {
        private const val TAG = "WordRepository"
        const val PATH_WORDS = "/words"
        const val KEY_WORDS = "words"
        const val KEY_TS = "ts"
    }
}
