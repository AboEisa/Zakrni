package com.zakrni.app.clean.ui.utils

data class ReciterOption(
    val identifier: String,
    val arabicName: String,
    val englishName: String,
    val primaryServer: String,
    val cloudEdition: String? = null,
    val backupServers: List<String> = emptyList()
)

object ReciterCatalog {
    const val DEFAULT_RECITER_ID = "ar.alafasy"

    // Stable, production-safe reciters only.
    val supportedReciters: List<ReciterOption> = listOf(
        ReciterOption(
            identifier = "ar.alafasy",
            arabicName = "مشاري راشد العفاسي",
            englishName = "Mishary Rashid Alafasy",
            primaryServer = "https://server8.mp3quran.net/afs/"
        ),
        ReciterOption(
            identifier = "ar.husary",
            arabicName = "محمود خليل الحصري",
            englishName = "Mahmoud Khalil Al-Husary",
            primaryServer = "https://server13.mp3quran.net/husr/"
        ),
        ReciterOption(
            identifier = "ar.sudais",
            arabicName = "عبد الرحمن السديس",
            englishName = "Abdul Rahman Al-Sudais",
            primaryServer = "https://server11.mp3quran.net/sds/"
        ),
        ReciterOption(
            identifier = "ar.abdulsamad",
            arabicName = "عبد الباسط عبد الصمد",
            englishName = "Abdul Basit Abdul Samad",
            primaryServer = "https://server7.mp3quran.net/basit/"
        ),
        ReciterOption(
            identifier = "ar.abdullahbasfar",
            arabicName = "عبد الله بصفر",
            englishName = "Abdullah Basfar",
            primaryServer = "https://server6.mp3quran.net/bsfr/"
        ),
        ReciterOption(
            identifier = "ar.shaatree",
            arabicName = "أبو بكر الشاطري",
            englishName = "Abu Bakr Ash-Shaatree",
            primaryServer = "https://server11.mp3quran.net/shatri/"
        ),
        ReciterOption(
            identifier = "ar.ahmedajamy",
            arabicName = "أحمد بن علي العجمي",
            englishName = "Ahmed Al-Ajamy",
            primaryServer = "https://server10.mp3quran.net/ajm/"
        ),
        ReciterOption(
            identifier = "ar.husarymujawwad",
            arabicName = "محمود خليل الحصري (المجود)",
            englishName = "Husary (Mujawwad)",
            primaryServer = "https://server13.mp3quran.net/husr/Almusshaf-Al-Mojawwad/",
            backupServers = listOf("https://server13.mp3quran.net/husr/")
        ),
        ReciterOption(
            identifier = "ar.hudhaify",
            arabicName = "علي بن عبد الرحمن الحذيفي",
            englishName = "Ali Al-Hudhaify",
            primaryServer = "https://server9.mp3quran.net/hthfi/"
        ),
        ReciterOption(
            identifier = "ar.mahermuaiqly",
            arabicName = "ماهر المعيقلي",
            englishName = "Maher Al Muaiqly",
            primaryServer = "https://server12.mp3quran.net/maher/",
            backupServers = listOf(
                "https://server12.mp3quran.net/maher/Almusshaf-Al-Mojawwad/",
                "https://server12.mp3quran.net/maher/Almusshaf-Al-Mo-lim/"
            )
        ),
        ReciterOption(
            identifier = "ar.muhammadjibreel",
            arabicName = "محمد جبريل",
            englishName = "Muhammad Jibreel",
            primaryServer = "https://server8.mp3quran.net/jbrl/"
        )
    )

    private val reciterMap: Map<String, ReciterOption> = supportedReciters.associateBy { it.identifier }

    fun normalize(identifier: String?): String {
        val candidate = identifier ?: DEFAULT_RECITER_ID
        return if (reciterMap.containsKey(candidate)) candidate else DEFAULT_RECITER_ID
    }

    fun getById(identifier: String?): ReciterOption? {
        return reciterMap[normalize(identifier)]
    }

    fun getDisplayName(identifier: String?, isArabic: Boolean): String {
        val reciter = getById(identifier)
        return if (isArabic) {
            reciter?.arabicName ?: "مشاري راشد العفاسي"
        } else {
            reciter?.englishName ?: "Mishary Rashid Alafasy"
        }
    }
}
