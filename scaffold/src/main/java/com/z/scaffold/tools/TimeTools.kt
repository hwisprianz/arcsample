package com.z.scaffold.tools


/**
 *
 *
 * Created by Blate on 2023/12/11
 */
object TimeTools {

    fun formatVideoDuration(durationMs: Long): String {
        val second = durationMs / 1000
        val minute = second / 60
        val hour = minute / 60
        return if (hour > 0) {
            String.format("%02d:%02d:%02d", hour, minute % 60, second % 60)
        } else {
            String.format("%02d:%02d", minute, second % 60)
        }
    }

}