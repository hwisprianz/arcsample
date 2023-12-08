package com.z.scaffold.viewmode

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.z.arc.media.bean.MediaBucketBean
import com.z.arc.media.repo.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

/**
 *
 *
 * Created by Blate on 2023/12/6
 */
class MediaPickerViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MediaPickerViewModel"
    }

    private val mMediaRepository: MediaRepository = MediaRepository(
        application.contentResolver,
        MediaRepository.IncludeDef.INCLUDE_ALL,
        MediaRepository.SortDef.SORT_DESCENDING
    )

    private val _bucketsLive: MutableLiveData<List<MediaBucketBean>?> =
        MutableLiveData()

    private val _bucketLive: MutableLiveData<MediaBucketBean?> = MutableLiveData()

    val bucketsLive: LiveData<List<MediaBucketBean>?>
        get() = _bucketsLive

    val bucketLive: LiveData<MediaBucketBean?>
        get() = _bucketLive

    init {
        viewModelScope.launch { queryBuckets() }
    }

    private suspend fun queryBuckets() {
        flow<List<MediaBucketBean>> {
            emit(mMediaRepository.queryBuckets())
        }.transform {
            _bucketsLive.postValue(it.toMutableList())
            it.firstOrNull()?.let { first -> _bucketLive.postValue(first) }
            it.forEach { bucket -> emit(bucket.id) }
        }.flowOn(Dispatchers.IO).collect {
            viewModelScope.launch { queryBucketsDetail(it) }
        }
    }

    private suspend fun queryBucketsDetail(id: Long?) {
        flow {
            emit(mMediaRepository.queryBucketDetail(id))
        }.flowOn(Dispatchers.IO).collect {
            val oldList: List<MediaBucketBean> = _bucketsLive.value ?: return@collect
            val index: Int = oldList.indexOfFirst { bucket -> bucket.id == it.id }
            if (index in oldList.indices) {
                val newList: MutableList<MediaBucketBean> = ArrayList(oldList)
                newList[index] = it
                _bucketsLive.value = newList
            }
        }
    }

    fun selectBucket(id: Long?) {
        _bucketsLive.value?.firstOrNull { bucket -> bucket.id == id }?.let { bucket ->
            _bucketLive.value = bucket
        }
    }

}