package com.z.arcsample.repo

import android.content.Context
import com.z.arcsample.R
import com.z.arcsample.bean.FunctionBean


/**
 *
 *
 * Created by Blate on 2023/11/17
 */
class FunctionRepository {

    fun queryFunctions(context: Context): List<FunctionBean> {
        return listOf(
            FunctionBean(
                R.drawable.ic_calendar_on_surface_24dp,
                context.getString(R.string.func_date_picker),
                FunctionBean.KEY_DATE_PICKER
            ),
        )
    }


}