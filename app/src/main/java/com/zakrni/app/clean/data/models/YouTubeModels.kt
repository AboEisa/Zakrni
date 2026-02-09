package com.zakrni.app.clean.data.models

import com.google.gson.annotations.SerializedName

/**
 * Simplified video model for app use
 */
data class YouTubeVideo(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val publishedAt: String,
    val channelName: String,
    val duration: String,
    val viewCount: String
)

/**
 * Channel info
 */
data class YouTubeChannel(
    val id: String,
    val name: String,
    val nameAr: String
)

/**
 * Predefined Islamic videos - hardcoded for reliability
 * Using real YouTube video IDs from famous Islamic scholars, categorized by type
 */
object IslamicVideos {
    
    // محاضرات ودروس إسلامية
    private val lectureVideos = listOf(
        YouTubeVideo(
            videoId = "LS3GQk9ETRU",
            title = "محاضرة - أثر الإيمان في حياة المسلم",
            description = "محاضرة عن أثر الإيمان بالله في حياة المسلم اليومية",
            thumbnailUrl = "https://img.youtube.com/vi/LS3GQk9ETRU/hqdefault.jpg",
            publishedAt = "منذ سنة",
            channelName = "طريق الإسلام",
            duration = "45:30",
            viewCount = "2.1M"
        ),
        YouTubeVideo(
            videoId = "yaAJz9QQCbU",
            title = "أصلح نفسك وتخلص من قلقك ومخاوفك",
            description = "إجعل اطمئنانك بالله - محاضرة إيمانية",
            thumbnailUrl = "https://img.youtube.com/vi/yaAJz9QQCbU/hqdefault.jpg",
            publishedAt = "منذ 6 أشهر",
            channelName = "الدكتور نبيل العوضي",
            duration = "52:15",
            viewCount = "1.5M"
        ),
        YouTubeVideo(
            videoId = "3lGilGBRbME",
            title = "كيف تتوب إلى الله توبة نصوحا",
            description = "محاضرة عن التوبة والرجوع إلى الله",
            thumbnailUrl = "https://img.youtube.com/vi/3lGilGBRbME/hqdefault.jpg",
            publishedAt = "منذ 3 أشهر",
            channelName = "قناة الرسالة",
            duration = "38:20",
            viewCount = "890K"
        ),
        YouTubeVideo(
            videoId = "Vfl0gLc4mJg",
            title = "قصص الأنبياء - قصة آدم عليه السلام",
            description = "سلسلة قصص الأنبياء والمرسلين",
            thumbnailUrl = "https://img.youtube.com/vi/Vfl0gLc4mJg/hqdefault.jpg",
            publishedAt = "منذ سنتين",
            channelName = "الشيخ نبيل العوضي",
            duration = "1:15:40",
            viewCount = "15M"
        ),
        YouTubeVideo(
            videoId = "hBPjGqJMcD8",
            title = "فضل ذكر الله تعالى",
            description = "محاضرة عن أهمية الذكر وفوائده",
            thumbnailUrl = "https://img.youtube.com/vi/hBPjGqJMcD8/hqdefault.jpg",
            publishedAt = "منذ 8 أشهر",
            channelName = "قناة المجد",
            duration = "28:45",
            viewCount = "3.2M"
        ),
        YouTubeVideo(
            videoId = "cYChJORpp4Y",
            title = "الصبر على البلاء - دروس وعبر",
            description = "كيف نتعامل مع الابتلاءات في حياتنا",
            thumbnailUrl = "https://img.youtube.com/vi/cYChJORpp4Y/hqdefault.jpg",
            publishedAt = "منذ سنة",
            channelName = "الشيخ محمد العريفي",
            duration = "42:10",
            viewCount = "2.8M"
        )
    )
    
    // تلاوات قرآنية
    private val quranVideos = listOf(
        YouTubeVideo(
            videoId = "V1Z3-oAoqqc",
            title = "سورة البقرة كاملة - بصوت عبد الباسط عبد الصمد",
            description = "تلاوة خاشعة لسورة البقرة",
            thumbnailUrl = "https://img.youtube.com/vi/V1Z3-oAoqqc/hqdefault.jpg",
            publishedAt = "منذ 4 سنوات",
            channelName = "القرآن الكريم",
            duration = "2:25:30",
            viewCount = "50M"
        ),
        YouTubeVideo(
            videoId = "WxTXGKgQuns",
            title = "سورة الكهف كاملة - ماهر المعيقلي",
            description = "تلاوة هادئة لسورة الكهف",
            thumbnailUrl = "https://img.youtube.com/vi/WxTXGKgQuns/hqdefault.jpg",
            publishedAt = "منذ سنة",
            channelName = "القرآن الكريم",
            duration = "48:15",
            viewCount = "25M"
        ),
        YouTubeVideo(
            videoId = "dRJIFj4BRQM",
            title = "سورة يس - عبد الرحمن السديس",
            description = "تلاوة مباركة لسورة يس",
            thumbnailUrl = "https://img.youtube.com/vi/dRJIFj4BRQM/hqdefault.jpg",
            publishedAt = "منذ سنتين",
            channelName = "إذاعة القرآن الكريم",
            duration = "25:40",
            viewCount = "18M"
        ),
        YouTubeVideo(
            videoId = "JH8yUfBwEgM",
            title = "سورة الرحمن - مشاري العفاسي",
            description = "تلاوة خاشعة لسورة الرحمن",
            thumbnailUrl = "https://img.youtube.com/vi/JH8yUfBwEgM/hqdefault.jpg",
            publishedAt = "منذ 3 سنوات",
            channelName = "مشاري راشد العفاسي",
            duration = "15:30",
            viewCount = "30M"
        ),
        YouTubeVideo(
            videoId = "ypCb-iHtVCE",
            title = "سورة الملك - أحمد العجمي",
            description = "تلاوة هادئة لسورة الملك",
            thumbnailUrl = "https://img.youtube.com/vi/ypCb-iHtVCE/hqdefault.jpg",
            publishedAt = "منذ سنة",
            channelName = "أحمد العجمي",
            duration = "12:20",
            viewCount = "22M"
        ),
        YouTubeVideo(
            videoId = "w7u1EJNDx_o",
            title = "سورة آل عمران كاملة - سعد الغامدي",
            description = "تلاوة كاملة لسورة آل عمران",
            thumbnailUrl = "https://img.youtube.com/vi/w7u1EJNDx_o/hqdefault.jpg",
            publishedAt = "منذ سنتين",
            channelName = "سعد الغامدي",
            duration = "1:35:00",
            viewCount = "12M"
        )
    )
    
    // فيديوهات إسلامية متنوعة
    private val islamicVideos = listOf(
        YouTubeVideo(
            videoId = "DsBVLbCjb2I",
            title = "على طريق الله - الحلقة الأولى",
            description = "برنامج على طريق الله",
            thumbnailUrl = "https://img.youtube.com/vi/DsBVLbCjb2I/hqdefault.jpg",
            publishedAt = "منذ 4 سنوات",
            channelName = "مصطفى حسني",
            duration = "25:30",
            viewCount = "5.1M"
        ),
        YouTubeVideo(
            videoId = "1VJQ7XrBHbQ",
            title = "كنوز - فضل بر الوالدين",
            description = "حلقة عن فضل بر الوالدين وأثره",
            thumbnailUrl = "https://img.youtube.com/vi/1VJQ7XrBHbQ/hqdefault.jpg",
            publishedAt = "منذ سنة",
            channelName = "مصطفى حسني",
            duration = "18:45",
            viewCount = "2.3M"
        ),
        YouTubeVideo(
            videoId = "glPz7lxcTD8",
            title = "صناعة الحياة - كيف تصنع نجاحك",
            description = "من برنامج صناعة الحياة",
            thumbnailUrl = "https://img.youtube.com/vi/glPz7lxcTD8/hqdefault.jpg",
            publishedAt = "منذ 3 سنوات",
            channelName = "عمرو خالد",
            duration = "42:15",
            viewCount = "7.8M"
        ),
        YouTubeVideo(
            videoId = "oQAHn9bE_a4",
            title = "نهاية العالم - علامات الساعة الصغرى",
            description = "سلسلة نهاية العالم",
            thumbnailUrl = "https://img.youtube.com/vi/oQAHn9bE_a4/hqdefault.jpg",
            publishedAt = "منذ سنتين",
            channelName = "محمد العريفي",
            duration = "35:20",
            viewCount = "12M"
        ),
        YouTubeVideo(
            videoId = "pPXcNFD7ROA",
            title = "رحلة اليقين - الحلقة ١",
            description = "رحلة البحث عن اليقين",
            thumbnailUrl = "https://img.youtube.com/vi/pPXcNFD7ROA/hqdefault.jpg",
            publishedAt = "منذ سنة",
            channelName = "إياد قنيبي",
            duration = "22:10",
            viewCount = "4.5M"
        ),
        YouTubeVideo(
            videoId = "HIIqjFXPexU",
            title = "آداب الإسلام في التعامل مع الناس",
            description = "كيف نتعامل مع الناس بأخلاق الإسلام",
            thumbnailUrl = "https://img.youtube.com/vi/HIIqjFXPexU/hqdefault.jpg",
            publishedAt = "منذ 6 أشهر",
            channelName = "طريق الإسلام",
            duration = "30:00",
            viewCount = "1.8M"
        )
    )
    
    // خطب الجمعة
    private val sermonVideos = listOf(
        YouTubeVideo(
            videoId = "FJa-QMK_BEo",
            title = "خطبة الجمعة - الصبر والتوكل على الله",
            description = "خطبة جمعة مؤثرة عن الصبر",
            thumbnailUrl = "https://img.youtube.com/vi/FJa-QMK_BEo/hqdefault.jpg",
            publishedAt = "منذ شهرين",
            channelName = "الشيخ محمد راتب النابلسي",
            duration = "32:00",
            viewCount = "1.2M"
        ),
        YouTubeVideo(
            videoId = "vTN2mJIDTgA",
            title = "خطبة - أهمية الصلاة في حياة المسلم",
            description = "خطبة عن فضل الصلاة والمحافظة عليها",
            thumbnailUrl = "https://img.youtube.com/vi/vTN2mJIDTgA/hqdefault.jpg",
            publishedAt = "منذ 3 أشهر",
            channelName = "وزارة الأوقاف المصرية",
            duration = "28:15",
            viewCount = "650K"
        ),
        YouTubeVideo(
            videoId = "tHZPBe9ykNk",
            title = "خطبة الجمعة - حسن الخلق",
            description = "خطبة عن أهمية حسن الخلق في الإسلام",
            thumbnailUrl = "https://img.youtube.com/vi/tHZPBe9ykNk/hqdefault.jpg",
            publishedAt = "منذ شهر",
            channelName = "الأزهر الشريف",
            duration = "35:40",
            viewCount = "980K"
        ),
        YouTubeVideo(
            videoId = "X1hk5U4LJVw",
            title = "خطبة - فضل الدعاء وآدابه",
            description = "خطبة جمعة عن الدعاء وفضله",
            thumbnailUrl = "https://img.youtube.com/vi/X1hk5U4LJVw/hqdefault.jpg",
            publishedAt = "منذ 4 أشهر",
            channelName = "الحرم المكي",
            duration = "25:30",
            viewCount = "2.1M"
        ),
        YouTubeVideo(
            videoId = "qZPmMwHO8Zo",
            title = "خطبة الجمعة - التوبة والاستغفار",
            description = "خطبة مؤثرة عن التوبة",
            thumbnailUrl = "https://img.youtube.com/vi/qZPmMwHO8Zo/hqdefault.jpg",
            publishedAt = "منذ أسبوعين",
            channelName = "الحرم المدني",
            duration = "30:00",
            viewCount = "1.5M"
        ),
        YouTubeVideo(
            videoId = "iS7H1rXQ3v0",
            title = "خطبة - بر الوالدين في الإسلام",
            description = "خطبة جمعة عن حقوق الوالدين",
            thumbnailUrl = "https://img.youtube.com/vi/iS7H1rXQ3v0/hqdefault.jpg",
            publishedAt = "منذ 5 أشهر",
            channelName = "قناة الناس",
            duration = "27:45",
            viewCount = "750K"
        )
    )
    
    // All videos combined (for backward compatibility)
    private val allVideos = lectureVideos + quranVideos + islamicVideos + sermonVideos
    val videos: List<YouTubeVideo>
        get() = getAllVideos(isArabic = true)

    fun getAllVideos(isArabic: Boolean = true): List<YouTubeVideo> {
        return if (isArabic) allVideos else localizeVideos(allVideos, "all")
    }

    /**
     * Get videos filtered by content type
     */
    fun getVideosByType(contentType: String, isArabic: Boolean = true): List<YouTubeVideo> {
        val source = when (contentType) {
            "lectures" -> lectureVideos
            "quran" -> quranVideos
            "videos" -> islamicVideos
            "sermons" -> sermonVideos
            else -> allVideos.shuffled()
        }

        return if (isArabic) source else localizeVideos(source, contentType)
    }

    private fun localizeVideos(source: List<YouTubeVideo>, category: String): List<YouTubeVideo> {
        return source.mapIndexed { index, video ->
            val order = index + 1
            video.copy(
                title = if (containsArabic(video.title)) englishTitleForCategory(category, order) else video.title,
                description = if (containsArabic(video.description)) {
                    englishDescriptionForCategory(category)
                } else {
                    video.description
                },
                channelName = localizeChannelName(video.channelName),
                publishedAt = toEnglishRelativeTime(video.publishedAt)
            )
        }
    }

    private fun localizeChannelName(channel: String): String {
        if (!containsArabic(channel)) return channel
        return when (channel) {
            "طريق الإسلام" -> "Islamway"
            "الدكتور نبيل العوضي" -> "Dr. Nabil Al-Awadi"
            "الشيخ نبيل العوضي" -> "Sheikh Nabil Al-Awadi"
            "قناة الرسالة" -> "Al-Resalah Channel"
            "قناة المجد" -> "Al-Majd Channel"
            "الشيخ محمد العريفي" -> "Sheikh Mohammed Al-Arefe"
            "القرآن الكريم" -> "Holy Quran"
            "إذاعة القرآن الكريم" -> "Quran Radio"
            "مشاري راشد العفاسي" -> "Mishary Alafasy"
            "أحمد العجمي" -> "Ahmed Al-Ajmi"
            "سعد الغامدي" -> "Saad Al-Ghamdi"
            "مصطفى حسني" -> "Mostafa Hosny"
            "عمرو خالد" -> "Amr Khaled"
            "محمد العريفي" -> "Mohammed Al-Arefe"
            "إياد قنيبي" -> "Eyad Qunaibi"
            "الشيخ محمد راتب النابلسي" -> "Sheikh Mohammad Al-Nabulsi"
            "وزارة الأوقاف المصرية" -> "Egyptian Ministry of Endowments"
            "الأزهر الشريف" -> "Al-Azhar"
            "الحرم المكي" -> "Grand Mosque in Makkah"
            "الحرم المدني" -> "Prophet's Mosque in Madinah"
            "قناة الناس" -> "Al-Nas Channel"
            else -> "Islamic Channel"
        }
    }

    private fun englishTitleForCategory(category: String, index: Int): String {
        return when (category) {
            "lectures" -> "Islamic Lecture #$index"
            "quran" -> "Quran Recitation #$index"
            "videos" -> "Islamic Video #$index"
            "sermons" -> "Friday Sermon #$index"
            else -> "Islamic Content #$index"
        }
    }

    private fun englishDescriptionForCategory(category: String): String {
        return when (category) {
            "lectures" -> "An Islamic lecture to strengthen faith and knowledge."
            "quran" -> "A beautiful Quran recitation."
            "videos" -> "An Islamic reminder and beneficial content."
            "sermons" -> "A Friday sermon with practical Islamic guidance."
            else -> "Islamic educational content."
        }
    }

    private fun toEnglishRelativeTime(text: String): String {
        if (!containsArabic(text)) return text

        val normalizedDigits = text.map {
            when (it) {
                '٠' -> '0'
                '١' -> '1'
                '٢' -> '2'
                '٣' -> '3'
                '٤' -> '4'
                '٥' -> '5'
                '٦' -> '6'
                '٧' -> '7'
                '٨' -> '8'
                '٩' -> '9'
                else -> it
            }
        }.joinToString("")

        val number = Regex("""\d+""").find(normalizedDigits)?.value?.toIntOrNull() ?: 1
        return when {
            normalizedDigits.contains("الآن") -> "Just now"
            normalizedDigits.contains("سنتين") -> "2 years ago"
            normalizedDigits.contains("سنة") -> "$number year${if (number == 1) "" else "s"} ago"
            normalizedDigits.contains("شهرين") -> "2 months ago"
            normalizedDigits.contains("شهر") -> "$number month${if (number == 1) "" else "s"} ago"
            normalizedDigits.contains("أسبوعين") -> "2 weeks ago"
            normalizedDigits.contains("أسبوع") -> "$number week${if (number == 1) "" else "s"} ago"
            normalizedDigits.contains("يومين") -> "2 days ago"
            normalizedDigits.contains("يوم") -> "$number day${if (number == 1) "" else "s"} ago"
            normalizedDigits.contains("ساعتين") -> "2 hours ago"
            normalizedDigits.contains("ساعة") -> "$number hour${if (number == 1) "" else "s"} ago"
            normalizedDigits.contains("دقيقتين") -> "2 minutes ago"
            normalizedDigits.contains("دقيقة") -> "$number minute${if (number == 1) "" else "s"} ago"
            else -> "Recently"
        }
    }

    private fun containsArabic(text: String): Boolean {
        return Regex("[\\u0600-\\u06FF]").containsMatchIn(text)
    }
    
    val channels = listOf(
        YouTubeChannel("UCvwFsGTKhIJfxRKbLMbcPeA", "Nabulsi", "الشيخ محمد راتب النابلسي"),
        YouTubeChannel("UCjmlyM9IzKhsAi6Y9FJd0gg", "Omar Abdelkafy", "الشيخ عمر عبد الكافي"),
        YouTubeChannel("UCR7ychSPxMalNSbN4VWxkjg", "Saleh AlMaghamsi", "الشيخ صالح المغامسي"),
        YouTubeChannel("UCQohz2YHDG_xeebI-WKffUA", "Mustafa Hosny", "الداعية مصطفى حسني"),
        YouTubeChannel("UC1KBkDnhCLKL_vC3RABvIwQ", "Amr Khaled", "الداعية عمرو خالد")
    )
}
