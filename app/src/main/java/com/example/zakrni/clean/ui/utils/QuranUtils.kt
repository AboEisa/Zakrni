package com.example.zakrni.clean.ui.utils

object QuranUtils {

    fun getAyahCount(surahNumber: Int): Int {
        return ayahCounts[surahNumber] ?: 0
    }



    fun getRevelationTypeArabic(surahNumber: Int): String {
        return if (surahNumber in madaniSurahs) "مدنية" else "مكية"
    }

    private val ayahCounts = mapOf(
        1 to 7,       // Al-Fatiha
        2 to 286,     // Al-Baqarah
        3 to 200,     // Ali 'Imran
        4 to 176,     // An-Nisa
        5 to 120,     // Al-Ma'idah
        6 to 165,     // Al-An'am
        7 to 206,     // Al-A'raf
        8 to 75,      // Al-Anfal
        9 to 129,     // At-Tawbah
        10 to 109,    // Yunus
        11 to 123,    // Hud
        12 to 111,    // Yusuf
        13 to 43,     // Ar-Ra'd
        14 to 52,     // Ibrahim
        15 to 99,     // Al-Hijr
        16 to 128,    // An-Nahl
        17 to 111,    // Al-Isra
        18 to 110,    // Al-Kahf
        19 to 98,     // Maryam
        20 to 135,    // Taha
        21 to 112,    // Al-Anbya
        22 to 78,     // Al-Hajj
        23 to 118,    // Al-Mu'minun
        24 to 64,     // An-Nur
        25 to 77,     // Al-Furqan
        26 to 227,    // Ash-Shu'ara
        27 to 93,     // An-Naml
        28 to 88,     // Al-Qasas
        29 to 69,     // Al-Ankabut
        30 to 60,     // Ar-Rum
        31 to 34,     // Luqman
        32 to 30,     // As-Sajdah
        33 to 73,     // Al-Ahzab
        34 to 54,     // Saba
        35 to 45,     // Fatir
        36 to 83,     // Ya-Sin
        37 to 182,    // As-Saffat
        38 to 88,     // Sad
        39 to 75,     // Az-Zumar
        40 to 85,     // Ghafir
        41 to 54,     // Fussilat
        42 to 53,     // Ash-Shuraa
        43 to 89,     // Az-Zukhruf
        44 to 59,     // Ad-Dukhan
        45 to 37,     // Al-Jathiyah
        46 to 35,     // Al-Ahqaf
        47 to 38,     // Muhammad
        48 to 29,     // Al-Fath
        49 to 18,     // Al-Hujurat
        50 to 45,     // Qaf
        51 to 60,     // Adh-Dhariyat
        52 to 49,     // At-Tur
        53 to 62,     // An-Najm
        54 to 55,     // Al-Qamar
        55 to 78,     // Ar-Rahman
        56 to 96,     // Al-Waqi'ah
        57 to 29,     // Al-Hadid
        58 to 22,     // Al-Mujadila
        59 to 24,     // Al-Hashr
        60 to 13,     // Al-Mumtahanah
        61 to 14,     // As-Saf
        62 to 11,     // Al-Jumu'ah
        63 to 11,     // Al-Munafiqun
        64 to 18,     // At-Taghabun
        65 to 12,     // At-Talaq
        66 to 12,     // At-Tahrim
        67 to 30,     // Al-Mulk
        68 to 52,     // Al-Qalam
        69 to 52,     // Al-Haqqah
        70 to 44,     // Al-Ma'arij
        71 to 28,     // Nuh
        72 to 28,     // Al-Jinn
        73 to 20,     // Al-Muzzammil
        74 to 56,     // Al-Muddaththir
        75 to 40,     // Al-Qiyamah
        76 to 31,     // Al-Insan
        77 to 50,     // Al-Mursalat
        78 to 40,     // An-Naba
        79 to 46,     // An-Nazi'at
        80 to 42,     // Abasa
        81 to 29,     // At-Takwir
        82 to 19,     // Al-Infitar
        83 to 36,     // Al-Mutaffifin
        84 to 25,     // Al-Inshiqaq
        85 to 22,     // Al-Buruj
        86 to 17,     // At-Tariq
        87 to 19,     // Al-A'la
        88 to 26,     // Al-Ghashiyah
        89 to 30,     // Al-Fajr
        90 to 20,     // Al-Balad
        91 to 15,     // Ash-Shams
        92 to 21,     // Al-Layl
        93 to 11,     // Ad-Duhaa
        94 to 8,      // Ash-Sharh
        95 to 8,      // At-Tin
        96 to 19,     // Al-Alaq
        97 to 5,      // Al-Qadr
        98 to 8,      // Al-Bayyinah
        99 to 8,      // Az-Zalzalah
        100 to 11,    // Al-Adiyat
        101 to 11,    // Al-Qari'ah
        102 to 8,     // At-Takathur
        103 to 3,     // Al-Asr
        104 to 9,     // Al-Humazah
        105 to 5,     // Al-Fil
        106 to 4,     // Quraysh
        107 to 7,     // Al-Ma'un
        108 to 3,     // Al-Kawthar
        109 to 6,     // Al-Kafirun
        110 to 3,     // An-Nasr
        111 to 5,     // Al-Masad
        112 to 4,     // Al-Ikhlas
        113 to 5,     // Al-Falaq
        114 to 6      // An-Nas
    )

    // Medinan surahs (the rest are Meccan)
    private val madaniSurahs = setOf(
        2, 3, 4, 5, 8, 9, 13, 22, 24, 33, 47, 48, 49,
        55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 76, 98, 99, 110
    )
}