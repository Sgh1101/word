package com.sgh.wordwatch.mobile

import android.app.Application

class WordApp : Application() {
    lateinit var repository: WordRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = WordRepository(this)
    }
}
