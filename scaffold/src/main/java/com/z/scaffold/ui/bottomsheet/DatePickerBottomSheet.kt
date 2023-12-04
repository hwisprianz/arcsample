package com.z.scaffold.ui.bottomsheet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.IntRange
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.z.arc.wheel2d.Wheel2DLayoutManager
import com.z.arc.wheel2d.Wheel2DSnapHelper

import com.z.scaffold.R
import com.z.scaffold.databinding.ScaffoldBottomsheetDatePickerBinding
import com.z.scaffold.ui.adapter.NumberSequenceAdapter
import com.z.scaffold.ui.decoration.WheelGradientDecorationE
import java.util.Calendar


/**
 * 日期选择器
 *
 * 调用 [DatePickerBottomSheet.Builder] 构造器构造实例
 * 调用 [getSelectedDate] 获取当前选中的日期
 *
 * Created by Blate on 2023/11/16
 */
class DatePickerBottomSheet private constructor(context: Context) :
    BottomSheetDialog(
        context,
        R.style.ScaffoldBottomSheetDialog_SurfaceTopRadius_DisableDraggable
    ) {

    companion object {

        private const val TAG = "DatePickerBottomSheet"

        private fun w(provider: () -> String) {
            Log.w(TAG, provider.invoke())
        }

    }

    private val mViewBinding: ScaffoldBottomsheetDatePickerBinding by lazy {
        ScaffoldBottomsheetDatePickerBinding.inflate(layoutInflater)
    }

    private val mStartDate = Calendar.getInstance().apply { set(1970, 6, 1) }

    private val mEndDate = Calendar.getInstance().apply { set(2100, 11, 31) }

    /**
     * 日期选择器默认选中的日期
     */
    private val mDefaultDate = Calendar.getInstance()

    /**
     * 日期计算器
     */
    private val mCalculationDate = Calendar.getInstance()

    private val mYearWheelLayoutManager: Wheel2DLayoutManager = Wheel2DLayoutManager()

    private val mMonthWheelLayoutManager: Wheel2DLayoutManager = Wheel2DLayoutManager()

    private val mDayWheelLayoutManager: Wheel2DLayoutManager = Wheel2DLayoutManager()

    private val mYearAdapter: NumberSequenceAdapter = NumberSequenceAdapter()

    private val mMonthAdapter: NumberSequenceAdapter = NumberSequenceAdapter()

    private val mDayAdapter: NumberSequenceAdapter = NumberSequenceAdapter()

    private var mViewListener: OnViewClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        checkParams()
        initializeUi()
        setViewListener()
    }

    /**
     * 检查参数
     *
     * 1. 将时间参数的时分秒毫秒部分清零
     * 2. 检查开始时间是否在结束时间之前, 如果不是, 则交换两个时间
     * 3. 检查默认时间是否在开始时间之后, 如果不是, 则将默认时间设置为开始时间
     * 4. 检查默认时间是否在结束时间之前, 如果不是, 则将默认时间设置为结束时间
     */
    private fun checkParams() {
        mStartDate.set(Calendar.HOUR_OF_DAY, 0)
        mStartDate.set(Calendar.MINUTE, 0)
        mStartDate.set(Calendar.SECOND, 0)
        mStartDate.set(Calendar.MILLISECOND, 0)

        mEndDate.set(Calendar.HOUR_OF_DAY, 0)
        mEndDate.set(Calendar.MINUTE, 0)
        mEndDate.set(Calendar.SECOND, 0)
        mEndDate.set(Calendar.MILLISECOND, 0)

        mDefaultDate.set(Calendar.HOUR_OF_DAY, 0)
        mDefaultDate.set(Calendar.MINUTE, 0)
        mDefaultDate.set(Calendar.SECOND, 0)
        mDefaultDate.set(Calendar.MILLISECOND, 0)

        if (mStartDate.after(mEndDate)) {
            // swap
            w {
                "checkParams: startDate must be before endDate; but startDate is " +
                        "${mStartDate.get(Calendar.YEAR)}/" +
                        "${mStartDate.get(Calendar.MONTH)}/" +
                        "${mStartDate.get(Calendar.DAY_OF_MONTH)}; " +
                        "endDate is " +
                        "${mEndDate.get(Calendar.YEAR)}/" +
                        "${mEndDate.get(Calendar.MONTH)}/" +
                        "${mEndDate.get(Calendar.DAY_OF_MONTH)}; " +
                        "has swapped them"
            }
            mStartDate.timeInMillis = mEndDate.timeInMillis.also {
                mEndDate.timeInMillis = mStartDate.timeInMillis
            }
        }

        if (mDefaultDate.before(mStartDate)) {
            w {
                "checkParams: defaultDate must be after startDate; but defaultDate is " +
                        "${mDefaultDate.get(Calendar.YEAR)}/" +
                        "${mDefaultDate.get(Calendar.MONTH)}/" +
                        "${mDefaultDate.get(Calendar.DAY_OF_MONTH)}; " +
                        "startDate is " +
                        "${mStartDate.get(Calendar.YEAR)}/" +
                        "${mStartDate.get(Calendar.MONTH)}/" +
                        "${mStartDate.get(Calendar.DAY_OF_MONTH)}; " +
                        "has reset defaultDate to startDate"
            }
            mDefaultDate.timeInMillis = mStartDate.timeInMillis
        }

        if (mDefaultDate.after(mEndDate)) {
            w {
                "checkParams: defaultDate must be before endDate; but defaultDate is " +
                        "${mDefaultDate.get(Calendar.YEAR)}/" +
                        "${mDefaultDate.get(Calendar.MONTH)}/" +
                        "${mDefaultDate.get(Calendar.DAY_OF_MONTH)}; " +
                        "endDate is " +
                        "${mEndDate.get(Calendar.YEAR)}/" +
                        "${mEndDate.get(Calendar.MONTH)}/" +
                        "${mEndDate.get(Calendar.DAY_OF_MONTH)}; " +
                        "has reset defaultDate to endDate"
            }
            mDefaultDate.timeInMillis = mEndDate.timeInMillis
        }
    }

    private fun initializeUi() {
        mViewBinding.rvYear.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mViewBinding.rvYear.layoutManager = mYearWheelLayoutManager
        mViewBinding.rvYear.addItemDecoration(WheelGradientDecorationE())
        Wheel2DSnapHelper().attachToRecyclerView(mViewBinding.rvYear)

        mViewBinding.rvMonth.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mViewBinding.rvMonth.layoutManager = mMonthWheelLayoutManager
        mViewBinding.rvMonth.addItemDecoration(WheelGradientDecorationE())
        Wheel2DSnapHelper().attachToRecyclerView(mViewBinding.rvMonth)

        mViewBinding.rvDay.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        mViewBinding.rvDay.layoutManager = mDayWheelLayoutManager
        mViewBinding.rvDay.addItemDecoration(WheelGradientDecorationE())
        Wheel2DSnapHelper().attachToRecyclerView(mViewBinding.rvDay)

        mViewBinding.rvYear.adapter = mYearAdapter
        mViewBinding.rvMonth.adapter = mMonthAdapter
        mViewBinding.rvDay.adapter = mDayAdapter

        updateYearWheel()
        mViewBinding.rvYear.scrollToPosition(mDefaultDate.get(Calendar.YEAR) - mYearAdapter.offset)
        updateMonthWheel(mDefaultDate.get(Calendar.YEAR))
        mViewBinding.rvMonth.scrollToPosition(mDefaultDate.get(Calendar.MONTH) + 1 - mMonthAdapter.offset)
        updateDayWheel(mDefaultDate.get(Calendar.YEAR), mDefaultDate.get(Calendar.MONTH) + 1)
        mViewBinding.rvDay.scrollToPosition(mDefaultDate.get(Calendar.DAY_OF_MONTH) - mDayAdapter.offset)

        linkWheel()
    }

    private fun setViewListener() {
        mViewBinding.btTopStart.setOnClickListener {
            mViewListener?.onTopStartButtonClick(this)
        }
        mViewBinding.btTopEnd.setOnClickListener {
            mViewListener?.onTopEndButtonClick(this)
        }
    }

    private fun linkWheel() {
        mYearWheelLayoutManager.setOnCenterYPositionChangeListener { position ->
            mYearAdapter.getRealValue(position)?.let { year ->
                updateMonthWheel(year)
                mMonthAdapter.getRealValue(mMonthWheelLayoutManager.findCenterChildPosition())
                    ?.let { month -> updateDayWheel(year, month) }
            }
        }

        mMonthWheelLayoutManager.setOnCenterYPositionChangeListener { position ->
            mMonthAdapter.getRealValue(position)?.let { month ->
                mYearAdapter.getRealValue(mYearWheelLayoutManager.findCenterChildPosition())
                    ?.let { year -> updateDayWheel(year, month) }
            }
        }
    }

    private fun updateYearWheel() {
        val yearStart: Int = mStartDate.get(Calendar.YEAR)
        val yearEnd: Int = mEndDate.get(Calendar.YEAR)
        mYearAdapter.updateData(yearStart, yearEnd - yearStart + 1)
    }

    private fun updateMonthWheel(year: Int) {
        val monthStart: Int = if (year <= mStartDate.get(Calendar.YEAR)) {
            mStartDate.get(Calendar.MONTH) + 1
        } else {
            1
        }
        val monthEnd: Int = if (year >= mEndDate.get(Calendar.YEAR)) {
            mEndDate.get(Calendar.MONTH) + 1
        } else {
            12
        }
        mMonthAdapter.updateData(monthStart, monthEnd - monthStart + 1)
    }

    private fun updateDayWheel(year: Int, month: Int) {
        val dayStart: Int =
            if (year <= mStartDate.get(Calendar.YEAR) && month - 1 <= mStartDate.get(Calendar.MONTH)) {
                mStartDate.get(Calendar.DAY_OF_MONTH)
            } else {
                1
            }
        val dayEnd: Int =
            if (year >= mEndDate.get(Calendar.YEAR) && month - 1 >= mEndDate.get(Calendar.MONTH)) {
                mEndDate.get(Calendar.DAY_OF_MONTH)
            } else {
                mCalculationDate.set(year, month - 1, 1)
                mCalculationDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
        mDayAdapter.updateData(dayStart, dayEnd - dayStart + 1)
    }

    /**
     * 获取当前选中的日期
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getSelectedDate(): Calendar {
        val year = mYearAdapter.getRealValue(mYearWheelLayoutManager.findCenterChildPosition())
            ?: return mDefaultDate
        val month = mMonthAdapter.getRealValue(mMonthWheelLayoutManager.findCenterChildPosition())
            ?: return mDefaultDate
        val day = mDayAdapter.getRealValue(mDayWheelLayoutManager.findCenterChildPosition())
            ?: return mDefaultDate
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
        }
    }

    interface OnViewClickListener {
        fun onTopStartButtonClick(dialog: DatePickerBottomSheet)

        fun onTopEndButtonClick(dialog: DatePickerBottomSheet)

    }

    class Builder(context: Context) {

        private val mDialog: DatePickerBottomSheet = DatePickerBottomSheet(context)

        /**
         *  Set the start date of the date picker. The default value is 1970-01-01.
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun setFrom(
            @IntRange(from = 1) year: Int,
            @IntRange(from = 1, to = 12) month: Int,
            @IntRange(from = 1, to = 31) day: Int
        ): Builder {
            val checkedYear: Int = if (year >= 1) {
                year
            } else {
                w { "setFrom: year must be greater than 0; but set year is $year; has reset year to 1970" }
                1970
            }

            val checkedMonth: Int = if (month < 1) {
                w { "setFrom: month must be greater than 0; but set month is $month; has reset month to 1" }
                1
            } else if (month > 12) {
                w { "setFrom: month must be no greater 12; but set month is $month; has reset month to 12" }
                12
            } else {
                month
            }

            mDialog.mCalculationDate.set(Calendar.YEAR, checkedYear)
            mDialog.mCalculationDate.set(Calendar.MONTH, checkedMonth - 1)
            val dayMax: Int = mDialog.mCalculationDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            val checkedDay: Int = if (day < 1) {
                w { "setFrom: day must be greater than 0; but set day is $day; has reset day to 1" }
                1
            } else if (day > dayMax) {
                w { "setFrom: When $checkedYear/$checkedMonth, day must be no greater $dayMax; but set day is $day; has reset day to $dayMax" }
                dayMax
            } else {
                day
            }

            mDialog.mStartDate.set(checkedYear, checkedMonth - 1, checkedDay)
            return this
        }

        /**
         *  Set the start date of the date picker. The default value is 1970-01-01.
         */

        @Suppress("unused")
        fun setFrom(from: Calendar): Builder {
            setFrom(
                from.get(Calendar.YEAR),
                from.get(Calendar.MONTH) + 1,
                from.get(Calendar.DAY_OF_MONTH)
            )
            return this
        }

        /**
         * Set the end date of the date picker. The default value is 2100-12-31.
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun setTo(
            @IntRange(from = 1) year: Int,
            @IntRange(from = 1, to = 12) month: Int,
            @IntRange(from = 1, to = 31) day: Int
        ): Builder {
            val checkedYear: Int = if (year >= 1) {
                year
            } else {
                w { "setTo: year must be greater than 0; but set year is $year; has reset year to 1970" }
                1970
            }

            val checkedMonth: Int = if (month < 1) {
                w { "setTo: month must be greater than 0; but set month is $month; has reset month to 1" }
                1
            } else if (month > 12) {
                w { "setTo: month must be no greater 12; but set month is $month; has reset month to 12" }
                12
            } else {
                month
            }

            mDialog.mCalculationDate.set(Calendar.YEAR, checkedYear)
            mDialog.mCalculationDate.set(Calendar.MONTH, checkedMonth - 1)
            val dayMax: Int = mDialog.mCalculationDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            val checkedDay: Int = if (day < 1) {
                w { "setTo: day must be greater than 0; but set day is $day; has reset day to 1" }
                1
            } else if (day > dayMax) {
                w { "setTo: When $checkedYear/$checkedMonth, day must be no greater $dayMax; but set day is $day; has reset day to $dayMax" }
                dayMax
            } else {
                day
            }

            mDialog.mEndDate.set(checkedYear, checkedMonth - 1, checkedDay)
            return this
        }


        /**
         * Set the end date of the date picker. The default value is 2100-12-31.
         */
        @Suppress("unused")
        fun setTo(to: Calendar): Builder {
            setTo(to.get(Calendar.YEAR), to.get(Calendar.MONTH) + 1, to.get(Calendar.DAY_OF_MONTH))
            return this
        }

        /**
         * Set the default date of the date picker. The default value is current date, if current date range is not in range, the default date is the date in the range closest to the current date.
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun setDefault(
            @IntRange(from = 1) year: Int,
            @IntRange(from = 1, to = 12) month: Int,
            @IntRange(from = 1, to = 31) day: Int
        ): Builder {
            val checkedYear: Int = if (year >= 1) {
                year
            } else {
                w { "setDefault: year must be greater than 0; but set year is $year; has reset year to 1970" }
                1970
            }

            val checkedMonth: Int = if (month < 1) {
                w { "setDefault: month must be greater than 0; but set month is $month; has reset month to 1" }
                1
            } else if (month > 12) {
                w { "setDefault: month must be no greater 13; but set month is $month; has reset month to 12" }
                12
            } else {
                month
            }

            mDialog.mCalculationDate.set(Calendar.YEAR, checkedYear)
            mDialog.mCalculationDate.set(Calendar.MONTH, checkedMonth - 1)
            val dayMax: Int = mDialog.mCalculationDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            val checkedDay: Int = if (day < 1) {
                w { "setDefault: day must be greater than 0; but set day is $day; has reset day to 1" }
                1
            } else if (day > dayMax) {
                w { "setDefault: When $checkedYear/$checkedMonth, day must be no greater $dayMax; but set day is $day; has reset day to $dayMax" }
                dayMax
            } else {
                day
            }

            mDialog.mDefaultDate.set(checkedYear, checkedMonth - 1, checkedDay)
            return this
        }

        /**
         * Set the default date of the date picker. The default value is current date, if current date range is not in range, the default date is the date in the range closest to the current date.
         */
        @Suppress("unused")
        fun setDefault(default: Calendar): Builder {
            setDefault(
                default.get(Calendar.YEAR),
                default.get(Calendar.MONTH) + 1,
                default.get(Calendar.DAY_OF_MONTH)
            )
            return this
        }

        @Suppress("unused")
        fun setTopStartButtonText(text: CharSequence?): Builder {
            mDialog.mViewBinding.btTopStart.text = text
            return this
        }

        @Suppress("unused")
        fun setTopEndButtonText(text: CharSequence?): Builder {
            mDialog.mViewBinding.btTopEnd.text = text
            return this
        }

        @Suppress("unused")
        fun setTopTitleText(text: CharSequence?): Builder {
            mDialog.mViewBinding.tvTopTitle.text = text
            return this
        }

        @Suppress("unused")
        fun setOnViewClickListener(listener: OnViewClickListener): Builder {
            mDialog.mViewListener = listener
            return this
        }

        @Suppress("unused")
        fun build(): DatePickerBottomSheet {
            return mDialog
        }

    }

}