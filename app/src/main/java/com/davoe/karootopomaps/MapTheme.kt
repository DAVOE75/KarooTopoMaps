package com.davoe.karootopomaps

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MapTheme(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val assetFileName: String,
    @DrawableRes val previewDrawableRes: Int? = null,
    @StringRes val badgeRes: Int? = null
)
