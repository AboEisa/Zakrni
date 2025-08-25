package com.example.zakrni.clean.ui.models

import com.example.zakrni.clean.ui.utils.DuaType

sealed class DuaSection

data class DuaSectionHeader(
    val title: String,
    val duaType: DuaType,
    val duaList: List<Any>
) : DuaSection()