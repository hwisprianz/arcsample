package com.z.arcsample.bean

import androidx.annotation.DrawableRes


/**
 *
 *
 * Created by Blate on 2023/11/17
 */
data class FunctionBean(
    @DrawableRes val iconRes: Int?,
    val title: CharSequence?,
    val key: String
) {

    companion object {

        const val KEY_DATE_PICKER = "date_picker"

    }

}