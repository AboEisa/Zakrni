package com.example.zakrni.clean.data.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrayerApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DuaApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HadithApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class QuranApi
