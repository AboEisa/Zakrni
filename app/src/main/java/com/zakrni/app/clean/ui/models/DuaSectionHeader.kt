package com.zakrni.app.clean.ui.models

import com.zakrni.app.clean.ui.utils.DuaType

sealed class DuaSection

data class DuaSectionHeader(
    val title: String,
    val duaType: DuaType,
    val duaList: List<Any>
) : DuaSection()