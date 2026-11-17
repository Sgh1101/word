package com.sgh.wordwatch.wear

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.sgh.wordwatch.core.WordIndex

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { WordWatchApp() }
        }
    }
}

@androidx.compose.runtime.Composable
fun WordWatchApp() {
    val ctx = LocalContext.current
    val words by WatchWordStore.words.collectAsStateWithLifecycle()
    val hide by WatchWordStore.hideMeaning.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        WatchWordStore.load(ctx)
        WatchWordStore.pullLatest(ctx)
    }
    val list = words ?: emptyList()
    val nav = rememberSwipeDismissableNavController()

    fun open(key: String, allowSplit: Boolean) {
        val group = WordIndex.firstGroups(list).firstOrNull { it.key == key }
        val route = if (allowSplit && group != null && WordIndex.shouldSplit(group)) "sub" else "list"
        nav.navigate("$route/${Uri.encode(key)}")
    }

    SwipeDismissableNavHost(navController = nav, startDestination = "index") {
        composable("index") {
            IndexScreen(
                title = "단어 ${list.size}",
                groups = WordIndex.firstGroups(list),
                loading = words == null,
                onAll = { nav.navigate("list/${Uri.encode(WordIndex.ALL)}") },
                onGroup = { open(it, allowSplit = true) },
            )
        }
        composable("sub/{key}") { entry ->
            val key = entry.arguments?.getString("key").orEmpty()
            IndexScreen(
                title = "$key ${WordIndex.filter(list, key).size}",
                groups = WordIndex.secondGroups(list, key),
                loading = false,
                onAll = { nav.navigate("list/${Uri.encode(key)}") },
                onGroup = { nav.navigate("list/${Uri.encode(it)}") },
                allLabel = "$key 전체",
            )
        }
        composable("list/{key}") { entry ->
            val key = entry.arguments?.getString("key").orEmpty()
            WordListScreen(
                title = if (key == WordIndex.ALL) "전체" else key,
                words = WordIndex.filter(list, key),
                hideMeaning = hide,
                onToggleHide = { WatchWordStore.setHideMeaning(ctx, !hide) },
            )
        }
    }
}
