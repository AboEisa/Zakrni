package com.zakrni.app.clean.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zakrni.app.clean.data.local.CacheManager
import com.zakrni.app.clean.data.local.ILocalDataSource
import com.zakrni.app.clean.data.models.QuranResponse
import com.zakrni.app.clean.data.models.mapToDomain
import com.zakrni.app.clean.data.remote.IRemoteDataSource
import com.zakrni.app.clean.domain.IRepo
import com.zakrni.app.clean.domain.models.DomainArticleResponse
import com.zakrni.app.clean.domain.models.DomainAsmaAlHusnaResponse
import com.zakrni.app.clean.domain.models.DomainAudioEditionsResponse
import com.zakrni.app.clean.domain.models.DomainAyah
import com.zakrni.app.clean.domain.models.DomainAzkarResponse
import com.zakrni.app.clean.domain.models.DomainHadithResponse
import com.zakrni.app.clean.domain.models.DomainHisnDua
import com.zakrni.app.clean.domain.models.DomainHisnDuasResponse
import com.zakrni.app.clean.domain.models.DomainHisnSection
import com.zakrni.app.clean.domain.models.DomainPrayerTimesResponse
import com.zakrni.app.clean.domain.models.DomainQuranAudioResponse
import com.zakrni.app.clean.domain.models.DomainSurah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class Repo @Inject constructor(
    private val remoteDataSource: IRemoteDataSource,
    private val localDataSource: ILocalDataSource,
    private val cacheManager: CacheManager,
    private val context: Context
) : IRepo {

    private val gson = Gson()

    // Cached local Hisn data (loaded from assets/hisn.json once)
    private var localHisnDuas: List<DomainHisnDua>? = null
    private val englishAzkarAssetCache = mutableMapOf<String, EnglishAzkarAsset?>()

    private val morningAzkarEnglishAsset = "azkar_sabah_en.json"
    private val eveningAzkarEnglishAsset = "azkar_massa_en.json"
    private val postPrayerAzkarEnglishAsset = "PostPrayer_azkar_en.json"

    private fun loadLocalHisnDuas(): List<DomainHisnDua> {
        localHisnDuas?.let { return it }

        val json = readAssetTextOrNull("hisn.json") ?: return emptyList()
        val type = object : TypeToken<LinkedHashMap<String, HisnJsonSection>>() {}.type
        val sectionsMap: LinkedHashMap<String, HisnJsonSection> = gson.fromJson(json, type)
        val englishSectionsMap: LinkedHashMap<String, HisnJsonSection> =
            readAssetTextOrNull("hisn_en.json")
                ?.let { gson.fromJson(it, type) }
                ?: linkedMapOf()

        var idCounter = 1
        val allDuas = mutableListOf<DomainHisnDua>()
        for ((sectionName, sectionData) in sectionsMap) {
            val englishSection = englishSectionsMap[sectionName]
            val sectionEnglishName = englishSection?.section_en?.trim()?.takeIf { it.isNotBlank() }
            sectionData.text.forEachIndexed { index, text ->
                allDuas.add(
                    DomainHisnDua(
                        id = idCounter++,
                        section = sectionName,
                        arabic = text,
                        transliteration = null,
                        translation = englishSection?.text?.getOrNull(index),
                        reference = englishSection?.footnote?.getOrNull(index)
                            ?: sectionData.footnote?.getOrNull(index),
                        count = 1,
                        sectionEnglish = sectionEnglishName
                    )
                )
            }
        }
        localHisnDuas = allDuas
        return allDuas
    }

    // Helper class matching hisn.json structure
    private data class HisnJsonSection(
        val section_en: String? = null,
        val text: List<String> = emptyList(),
        val footnote: List<String>? = null
    )

    private data class EnglishAzkarAsset(
        val title: String = "",
        val content: List<EnglishAzkarItem> = emptyList()
    )

    private data class EnglishAzkarItem(
        val zekr: String = "",
        val repeat: Int = 1,
        val bless: String = ""
    )

    private fun readAssetTextOrNull(fileName: String): String? {
        return runCatching {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        }.getOrNull()
    }

    private fun isArabicUi(): Boolean {
        val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
        return locale.language.equals("ar", ignoreCase = true)
    }

    private fun loadEnglishAzkarAsset(fileName: String): EnglishAzkarAsset? {
        if (englishAzkarAssetCache.containsKey(fileName)) {
            return englishAzkarAssetCache[fileName]
        }

        val parsed = readAssetTextOrNull(fileName)
            ?.let { json ->
                runCatching { gson.fromJson(json, EnglishAzkarAsset::class.java) }.getOrNull()
            }

        englishAzkarAssetCache[fileName] = parsed
        return parsed
    }

    private fun localizeAzkarResponse(
        response: DomainAzkarResponse,
        englishAssetFileName: String? = null
    ): DomainAzkarResponse {
        if (isArabicUi()) return response
        val fileName = englishAssetFileName ?: return response
        val englishAsset = loadEnglishAzkarAsset(fileName) ?: return response

        val localizedContent = response.content.mapIndexed { index, item ->
            val translatedItem = englishAsset.content.getOrNull(index) ?: return@mapIndexed item
            item.copy(
                zekr = translatedItem.zekr.takeIf { it.isNotBlank() } ?: item.zekr,
                bless = translatedItem.bless.takeIf { it.isNotBlank() } ?: item.bless,
                repeat = translatedItem.repeat.takeIf { it > 0 } ?: item.repeat
            )
        }

        return response.copy(
            title = englishAsset.title.takeIf { it.isNotBlank() } ?: response.title,
            content = localizedContent
        )
    }
    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<DomainPrayerTimesResponse> {
        return try {
            // Check cache first
            val cacheKey = cacheManager.getPrayerTimesKey(latitude, longitude)
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainPrayerTimesResponse>(cacheKey)
                if (cached != null) return Result.success(cached)
            }
            
            // Fetch from API
            val data = remoteDataSource.getPrayerTimes(latitude, longitude)
            val result = data.getOrThrow().mapToDomain()
            
            // Save to cache
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(result)
        } catch (e: Exception) {
            // Try to return cached data on error
            val cacheKey = cacheManager.getPrayerTimesKey(latitude, longitude)
            val cached = cacheManager.getFromCache<DomainPrayerTimesResponse>(cacheKey)
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getAllahNames(): Result<DomainAsmaAlHusnaResponse> {
        return try {
            // Check cache first
            val cacheKey = cacheManager.getAllahNamesKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAsmaAlHusnaResponse>(cacheKey)
                if (cached != null) return Result.success(cached)
            }
            
            // Fetch from API
            val data = remoteDataSource.getAllahNames()
            val result = data.getOrThrow().mapToDomain()
            
            // Save to cache
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(result)
        } catch (e: Exception) {
            // Try to return cached data on error
            val cacheKey = cacheManager.getAllahNamesKey()
            val cached = cacheManager.getFromCache<DomainAsmaAlHusnaResponse>(cacheKey)
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getHadiths(page: Int, limit: Int): Result<DomainHadithResponse> {
        return try {
            // Check cache first
            val cacheKey = cacheManager.getHadithsKey(page)
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainHadithResponse>(cacheKey)
                if (cached != null) return Result.success(cached)
            }
            
            // Fetch from API
            val data = remoteDataSource.getHadiths(page, limit)
            val result = data.getOrThrow().mapToDomain()
            
            // Save to cache
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(result)
        } catch (e: Exception) {
            // Try to return cached data on error
            val cacheKey = cacheManager.getHadithsKey(page)
            val cached = cacheManager.getFromCache<DomainHadithResponse>(cacheKey)
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    override suspend fun getAzkarSabah(): Result<DomainAzkarResponse> {
        return try {
            // Check cache first
            val cacheKey = cacheManager.getAzkarSabahKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
                if (cached != null) {
                    return Result.success(localizeAzkarResponse(cached, morningAzkarEnglishAsset))
                }
            }
            
            // Fetch from API
            val data = remoteDataSource.getAzkarSabah()
            val result = data.getOrThrow().mapToDomain()
            
            // Save to cache
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(localizeAzkarResponse(result, morningAzkarEnglishAsset))
        } catch (e: Exception) {
            // Try to return cached data on error
            val cacheKey = cacheManager.getAzkarSabahKey()
            val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
            if (cached != null) {
                Result.success(localizeAzkarResponse(cached, morningAzkarEnglishAsset))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAzkarMasaa(): Result<DomainAzkarResponse> {
        return try {
            // Check cache first
            val cacheKey = cacheManager.getAzkarMasaaKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
                if (cached != null) {
                    return Result.success(localizeAzkarResponse(cached, eveningAzkarEnglishAsset))
                }
            }
            
            // Fetch from API
            val data = remoteDataSource.getAzkarMasaa()
            val result = data.getOrThrow().mapToDomain()
            
            // Save to cache
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(localizeAzkarResponse(result, eveningAzkarEnglishAsset))
        } catch (e: Exception) {
            val cacheKey = cacheManager.getAzkarMasaaKey()
            val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
            if (cached != null) {
                Result.success(localizeAzkarResponse(cached, eveningAzkarEnglishAsset))
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAzkarPostPlayer(): Result<DomainAzkarResponse> {
       return try {
           // Check cache first
           val cacheKey = cacheManager.getAzkarPostPrayerKey()
           if (cacheManager.hasCache(cacheKey)) {
               val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
               if (cached != null) {
                   return Result.success(localizeAzkarResponse(cached, postPrayerAzkarEnglishAsset))
               }
           }
           
           val data = remoteDataSource.getAzkarPostPlayer()
           val result = data.getOrThrow().mapToDomain()
           
           cacheManager.saveToCache(cacheKey, result)
           
           Result.success(localizeAzkarResponse(result, postPrayerAzkarEnglishAsset))
         } catch (e: Exception) {
           val cacheKey = cacheManager.getAzkarPostPrayerKey()
           val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
           if (cached != null) {
               Result.success(localizeAzkarResponse(cached, postPrayerAzkarEnglishAsset))
           } else {
               Result.failure(e)
           }
       }
    }

    override suspend fun getAzkarNoom(): Result<DomainAzkarResponse> {
        return try {
            val cacheKey = cacheManager.getAzkarNoomKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
                if (cached != null) return Result.success(localizeAzkarResponse(cached, "azkar_noom_en.json"))
            }
            
            val data = remoteDataSource.getAzkarNoom()
            val result = data.getOrThrow().mapToDomain()
            
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(localizeAzkarResponse(result, "azkar_noom_en.json"))
        } catch (e: Exception) {
            val cacheKey = cacheManager.getAzkarNoomKey()
            val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
            if (cached != null) Result.success(localizeAzkarResponse(cached, "azkar_noom_en.json")) else Result.failure(e)
        }
    }

    override suspend fun getAzkarWake(): Result<DomainAzkarResponse> {
        return try {
            val cacheKey = cacheManager.getAzkarWakeKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
                if (cached != null) return Result.success(localizeAzkarResponse(cached, "azkar_wake_en.json"))
            }
            
            val data = remoteDataSource.getAzkarWake()
            val result = data.getOrThrow().mapToDomain()
            
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(localizeAzkarResponse(result, "azkar_wake_en.json"))
        } catch (e: Exception) {
            val cacheKey = cacheManager.getAzkarWakeKey()
            val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
            if (cached != null) Result.success(localizeAzkarResponse(cached, "azkar_wake_en.json")) else Result.failure(e)
        }
    }

    override suspend fun getAzkarMosque(): Result<DomainAzkarResponse> {
        return try {
            val cacheKey = cacheManager.getAzkarMosqueKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
                if (cached != null) return Result.success(localizeAzkarResponse(cached, "azkar_mosque_en.json"))
            }
            
            val data = remoteDataSource.getAzkarMosque()
            val result = data.getOrThrow().mapToDomain()
            
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(localizeAzkarResponse(result, "azkar_mosque_en.json"))
        } catch (e: Exception) {
            val cacheKey = cacheManager.getAzkarMosqueKey()
            val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
            if (cached != null) Result.success(localizeAzkarResponse(cached, "azkar_mosque_en.json")) else Result.failure(e)
        }
    }

    override suspend fun getAzkarEating(): Result<DomainAzkarResponse> {
        return try {
            val cacheKey = cacheManager.getAzkarEatingKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
                if (cached != null) return Result.success(localizeAzkarResponse(cached, "azkar_eating_en.json"))
            }
            
            val data = remoteDataSource.getAzkarEating()
            val result = data.getOrThrow().mapToDomain()
            
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(localizeAzkarResponse(result, "azkar_eating_en.json"))
        } catch (e: Exception) {
            val cacheKey = cacheManager.getAzkarEatingKey()
            val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
            if (cached != null) Result.success(localizeAzkarResponse(cached, "azkar_eating_en.json")) else Result.failure(e)
        }
    }

    override suspend fun getAzkarMisc(): Result<DomainAzkarResponse> {
        return try {
            val cacheKey = cacheManager.getAzkarMiscKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
                if (cached != null) return Result.success(localizeAzkarResponse(cached, "azkar_misc_en.json"))
            }
            
            val data = remoteDataSource.getAzkarMisc()
            val result = data.getOrThrow().mapToDomain()
            
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(localizeAzkarResponse(result, "azkar_misc_en.json"))
        } catch (e: Exception) {
            val cacheKey = cacheManager.getAzkarMiscKey()
            val cached = cacheManager.getFromCache<DomainAzkarResponse>(cacheKey)
            if (cached != null) Result.success(localizeAzkarResponse(cached, "azkar_misc_en.json")) else Result.failure(e)
        }
    }


    // In-memory LRU cache — instant second access, no Room IO
    private val versesMemoryCache = android.util.LruCache<Int, List<DomainAyah>>(10)

    override suspend fun getQuranVerses(suraNumber: Int): List<DomainAyah> = withContext(Dispatchers.IO) {
        try {
            // 1) Check in-memory cache (instant)
            versesMemoryCache.get(suraNumber)?.let { cached ->
                return@withContext cached
            }

            // 2) Check Room DB cache
            val cachedAyahs = localDataSource.getAyahsBySurah(suraNumber)
            if (cachedAyahs.isNotEmpty()) {
                versesMemoryCache.put(suraNumber, cachedAyahs)
                return@withContext cachedAyahs
            }

            // 3) Fetch from API
            val remoteAyahs = remoteDataSource.getQuranVerses(suraNumber)
            if (remoteAyahs.isNotEmpty()) {
                localDataSource.saveAyahs(suraNumber, remoteAyahs)
                versesMemoryCache.put(suraNumber, remoteAyahs)
            }

            remoteAyahs
        } catch (e: Exception) {
            val fallback = localDataSource.getAyahsBySurah(suraNumber)
            if (fallback.isNotEmpty()) {
                versesMemoryCache.put(suraNumber, fallback)
            }
            fallback
        }
    }

    suspend fun preloadAllQuranData(onProgress: (Int, Int) -> Unit = { _, _ -> }) {
        withContext(Dispatchers.IO) {
            try {
                if (localDataSource.isQuranDataCached()) {
                    return@withContext
                }

                // Load and cache all surahs
                val surahs = remoteDataSource.getSurahList()
                localDataSource.saveSurahs(surahs)
                // Load verses for each surah
                surahs.forEachIndexed { index, surah ->
                    onProgress(index + 1, 114)

                    val verses = remoteDataSource.getQuranVerses(surah.number)
                    if (verses.isNotEmpty()) {
                        localDataSource.saveAyahs(surah.number, verses)
                    }

                    // Small delay to avoid overwhelming the API
                    kotlinx.coroutines.delay(2000)
                }
            } catch (e: Exception) {
                // Preload failed, data will be fetched on-demand
            }
        }
    }

    override suspend fun getAllSurahs(): List<DomainSurah> = withContext(Dispatchers.IO) {
        try {
            // Check local cache first
            val cachedSurahs = localDataSource.getAllSurahs()
            if (cachedSurahs.isNotEmpty()) {
                return@withContext cachedSurahs
            }

            // If no cache, fetch from API
            val remoteSurahs = remoteDataSource.getSurahList()

            // Save to cache if successful
            if (remoteSurahs.isNotEmpty()) {
                localDataSource.saveSurahs(remoteSurahs)
            }

            remoteSurahs
        } catch (e: Exception) {
            // Try to return cached data on error
            localDataSource.getAllSurahs()
        }
    }

    override suspend fun getSurahAudio(
        surahNumber: Int,
        reciter: String
    ): Result<QuranResponse> {
        return remoteDataSource.getSurahAudio(surahNumber, reciter)
    }

    override suspend fun getAudioEditions(language: String): Result<DomainAudioEditionsResponse> {
        return try {
            val response = remoteDataSource.getAudioEditions(language)
            if (response.isSuccess) {
                val audioEditionsResponse = response.getOrThrow()
                Result.success(audioEditionsResponse.mapToDomain())
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAyahAudio(reference: String, edition: String): Result<DomainQuranAudioResponse> {
        return try {
            val response = remoteDataSource.getAyahAudio(reference, edition)
            if (response.isSuccess) {
                val quranAudioResponse = response.getOrThrow()
                Result.success(quranAudioResponse.mapToDomain())
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //articles
    override suspend fun getArticles(): Result<DomainArticleResponse> {
        return try {
            val cacheKey = cacheManager.getArticlesKey()
            if (cacheManager.hasCache(cacheKey)) {
                val cached = cacheManager.getFromCache<DomainArticleResponse>(cacheKey)
                if (cached != null) return Result.success(cached)
            }
            
            val data = remoteDataSource.getArticles()
            val result = data.getOrThrow().mapToDomain()
            
            cacheManager.saveToCache(cacheKey, result)
            
            Result.success(result)
        } catch (e: Exception) {
            val cacheKey = cacheManager.getArticlesKey()
            val cached = cacheManager.getFromCache<DomainArticleResponse>(cacheKey)
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    // Hisn Duas — loaded locally from assets/hisn.json (instant, no network)
    override suspend fun getHisnSections(): Result<List<DomainHisnSection>> {
        return try {
            val allDuas = withContext(Dispatchers.IO) { loadLocalHisnDuas() }
            val sections = allDuas
                .groupBy { it.section }
                .map { (section, items) ->
                    DomainHisnSection(
                        name = section,
                        count = items.size,
                        englishName = items.firstNotNullOfOrNull { it.sectionEnglish }
                    )
                }
            Result.success(sections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHisnDuas(section: String?, query: String?, page: Int, limit: Int): Result<DomainHisnDuasResponse> {
        return try {
            val allDuas = withContext(Dispatchers.IO) { loadLocalHisnDuas() }

            // Apply section filter
            var filtered = allDuas
            if (!section.isNullOrBlank()) {
                filtered = filtered.filter { it.section == section }
            }

            // Apply search query
            if (!query.isNullOrBlank()) {
                val normalizedQuery = query.trim()
                filtered = filtered.filter { dua ->
                    dua.arabic.contains(normalizedQuery, ignoreCase = true) ||
                            (dua.translation?.contains(normalizedQuery, ignoreCase = true) == true) ||
                            dua.section.contains(normalizedQuery, ignoreCase = true) ||
                            (dua.sectionEnglish?.contains(normalizedQuery, ignoreCase = true) == true)
                }
            }

            // Pagination
            val total = filtered.size
            val start = (page - 1) * limit
            val pageItems = filtered.drop(start).take(limit)
            val totalPages = if (total == 0) 1 else (total + limit - 1) / limit

            Result.success(
                DomainHisnDuasResponse(
                    duas = pageItems,
                    page = page,
                    limit = limit,
                    total = total,
                    totalPages = totalPages
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get ayah with translation
    override suspend fun getAyahWithTranslation(surahNumber: Int, ayahNumber: Int): Result<QuranResponse> {
        return remoteDataSource.getAyahWithTranslation(surahNumber, ayahNumber)
    }

}
