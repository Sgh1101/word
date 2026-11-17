package com.sgh.wordwatch.wear

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.runBlocking

/** 폰에서 단어장이 바뀌면 호출된다. */
class WordListenerService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        val items = events
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == WatchWordStore.PATH_WORDS }
            .map { it.dataItem.freeze() }
        events.release()
        // onDataChanged 는 백그라운드 스레드에서 호출되므로 여기서 기다려도 된다.
        runBlocking {
            for (item in items) {
                runCatching { WatchWordStore.importItem(applicationContext, item) }
                    .onFailure { Log.w("WordListener", "import failed", it) }
            }
        }
    }
}
