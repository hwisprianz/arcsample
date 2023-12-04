package com.z.arcsample.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.z.arcsample.databinding.ActivityMainBinding
import com.z.arcsample.bean.FunctionBean
import com.z.arcsample.ui.adapter.FunctionAdapter
import com.z.arcsample.viewmodel.MainViewModel
import com.z.scaffold.sugar.attachOnItemClickListener
import com.z.scaffold.ui.bottomsheet.DatePickerBottomSheet
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "MainActivity"
    }

    private val mViewBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val mFunctionAdapter: FunctionAdapter = FunctionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        initializeUi()
        setViewListener()
        subscribeData()
    }

    private fun initializeUi() {
        mViewBinding.rvFunctions.layoutManager = LinearLayoutManager(this)
        mViewBinding.rvFunctions.adapter = mFunctionAdapter
    }

    private fun setViewListener() {
        mViewBinding.rvFunctions.attachOnItemClickListener { _, position, _ ->
            when (mFunctionAdapter.getItemSafety(position)?.key) {
                FunctionBean.KEY_DATE_PICKER -> showDatePicker()
            }
        }
    }

    private fun subscribeData() {
        mViewModel.functionsLiveData.observe(this) { mFunctionAdapter.submitList(it) }
    }

    private fun showDatePicker() {
        startActivity(Intent(this, DatePickerSampleActivity::class.java))
    }

}