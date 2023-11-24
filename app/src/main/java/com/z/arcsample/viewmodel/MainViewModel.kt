package com.z.arcsample.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.z.arcsample.bean.FunctionBean
import com.z.arcsample.repo.FunctionRepository


/**
 *
 *
 * Created by Blate on 2023/11/17
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _functionRepo: FunctionRepository = FunctionRepository()

    private val _functionsLiveData: MutableLiveData<List<FunctionBean>> =
        MutableLiveData<List<FunctionBean>>()

    val functionsLiveData: LiveData<List<FunctionBean>>
        get() = _functionsLiveData

    init {
        _functionsLiveData.value = _functionRepo.queryFunctions(application)
    }

}