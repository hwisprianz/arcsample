package com.z.scaffold.sugar

import android.content.Context


/**
 *
 *
 * Created by Blate on 2023/12/11
 */
fun Number.dp(context: Context): Int {
    return (this.toFloat() * context.resources.displayMetrics.density + 0.5f).toInt()
}