package com.sgh.wordwatch.wear

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.sgh.wordwatch.core.Word
import com.sgh.wordwatch.core.WordBook
import com.sgh.wordwatch.core.WordCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 시계 쪽 단어장 저장소. 폰에서 받은 단어장을 파일에 캐시해서
 * 폰이 꺼져 있어도 볼 수 있게 한다. (프로세스 안에서 하나만 존재)
 */
object WatchWordStore {
    private const val TAG = "WatchWordStore"
    const val PATH_WORDS = "/words"
    private const val KEY_WORDS = "words"
    private const val PREFS = "settings"
    private const val KEY_HIDE = "hideMeaning"

    private val _words = MutableStateFlow<List<Word>?>(null) // null = 아직 로딩 전
    val words: StateFlow<List<Word>?> = _words.asStateFlow()

    private val _hideMeaning = MutableStateFlow(false)
    val hideMeaning: StateFlow<Boolean> = _hideMeaning.asStateFlow()

    private fun file(ctx: Context) = File(ctx.filesDir, "words.json")

    suspend fun load(ctx: Context) = withContext(Dispatchers.IO) {
        _hideMeaning.value = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_HIDE, false)
        if (_words.value == null) {
            val f = file(ctx)
            _words.value = runCatching { if (f.exists()) WordBook.sort(WordCodec.decode(f.readText())) else emptyList() }
                .getOrElse { emptyList() }
        }
    }

    fun setHideMeaning(ctx: Context, hide: Boolean) {
        _hideMeaning.value = hide
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_HIDE, hide).apply()
    }

    /** 폰이 이미 올려둔 최신 단어장을 가져온다 (앱 시작 시). */
    suspend fun pullLatest(ctx: Context) {
        try {
            val client = Wearable.getDataClient(ctx)
            val buffer = client.getDataItems(Uri.parse("wear://*$PATH_WORDS")).await()
            try {
                buffer.maxByOrNull { DataMapItem.fromDataItem(it).dataMap.getLong("ts") }?.let { importItem(ctx, it) }
            } finally {
                buffer.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "pull failed", e)
        }
    }

    /** 폰에서 받은 DataItem 을 읽어 저장. */
    suspend fun importItem(ctx: Context, item: DataItem) = withContext(Dispatchers.IO) {
        val asset = DataMapItem.fromDataItem(item).dataMap.getAsset(KEY_WORDS) ?: return@withContext
        val response = Wearable.getDataClient(ctx).getFdForAsset(asset).await()
        val text = response.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        val list = WordBook.sort(WordCodec.decode(text))
        file(ctx).writeText(text)
        _words.value = list
    }
}
