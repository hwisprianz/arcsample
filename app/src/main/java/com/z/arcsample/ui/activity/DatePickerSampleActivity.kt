package com.z.arcsample.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.z.arcsample.databinding.ActivitySampleDatePickerBinding
import com.z.scaffold.ui.bottomsheet.DatePickerBottomSheet
import java.util.Calendar


/**
 *
 *
 * Created by Blate on 2023/11/24
 */
class DatePickerSampleActivity : AppCompatActivity() {

    private val mViewBinding: ActivitySampleDatePickerBinding by lazy {
        ActivitySampleDatePickerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        initializeUi()
        setViewListener()
    }

    @SuppressLint("SetTextI18n")
    private fun initializeUi() {
        mViewBinding.etFromYear.setText("1970")
        mViewBinding.etFromMonth.setText("1")
        mViewBinding.etFromDay.setText("1")

        mViewBinding.etToYear.setText("2100")
        mViewBinding.etToMonth.setText("12")
        mViewBinding.etToDay.setText("31")

        val calendar: Calendar = Calendar.getInstance()
        mViewBinding.tvDate.text = "${calendar.get(Calendar.YEAR)}" +
                "/${calendar.get(Calendar.MONTH) + 1}" +
                "/${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun setViewListener() {
        mViewBinding.topBar.setNavigationOnClickListener { finish() }
        mViewBinding.btPickDate.setOnClickListener {
            val fromYear: Int = try {
                mViewBinding.etFromYear.text.toString().toInt()
            } catch (e: Exception) {
                1970
            }
            val fromMonth: Int = try {
                mViewBinding.etFromMonth.text.toString().toInt()
            } catch (e: Exception) {
                1
            }
            val fromDay: Int = try {
                mViewBinding.etFromDay.text.toString().toInt()
            } catch (e: Exception) {
                1
            }

            val toYear: Int = try {
                mViewBinding.etToYear.text.toString().toInt()
            } catch (e: Exception) {
                2100
            }
            val toMonth: Int = try {
                mViewBinding.etToMonth.text.toString().toInt()
            } catch (e: Exception) {
                12
            }
            val toDay: Int = try {
                mViewBinding.etToDay.text.toString().toInt()
            } catch (e: Exception) {
                31
            }

            val defaultYear = try {
                mViewBinding.tvDate.text.toString().split("/")[0].toInt()
            } catch (e: Exception) {
                Calendar.getInstance().get(Calendar.YEAR)
            }
            val defaultMonth = try {
                mViewBinding.tvDate.text.toString().split("/")[1].toInt()
            } catch (e: Exception) {
                Calendar.getInstance().get(Calendar.MONTH) + 1
            }
            val defaultDay = try {
                mViewBinding.tvDate.text.toString().split("/")[2].toInt()
            } catch (e: Exception) {
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            }

            DatePickerBottomSheet.Builder(this)
                .setFrom(fromYear, fromMonth, fromDay)
                .setTo(toYear, toMonth, toDay)
                .setDefault(defaultYear, defaultMonth, defaultDay)
                .setOnViewClickListener(object : DatePickerBottomSheet.OnViewClickListener {

                    override fun onTopStartButtonClick(dialog: DatePickerBottomSheet) {
                        dialog.dismiss()
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onTopEndButtonClick(dialog: DatePickerBottomSheet) {
                        val calendar: Calendar = dialog.getSelectedDate()
                        mViewBinding.tvDate.text = "${calendar.get(Calendar.YEAR)}" +
                                "/${calendar.get(Calendar.MONTH) + 1}" +
                                "/${calendar.get(Calendar.DAY_OF_MONTH)}"
                        dialog.dismiss()
                    }

                })
                .build()
                .show()

        }
    }

}