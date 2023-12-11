package com.z.scaffold.ui.activity

import android.animation.ValueAnimator
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.z.arc.recyclerview.decoration.ItemSpacingDecoration
import com.z.scaffold.R
import com.z.scaffold.databinding.ScaffoldActivityMediaPickerBinding
import com.z.scaffold.sugar.attachOnItemClickListener
import com.z.scaffold.sugar.dp
import com.z.scaffold.ui.adapter.MediaAdapter
import com.z.scaffold.ui.adapter.MediaBucketAdapter
import com.z.scaffold.viewmode.MediaPickerViewModel

/**
 *
 *
 * Created by Blate on 2023/12/6
 */
class MediaPickerActivity : AppCompatActivity() {

    private val mViewBinding: ScaffoldActivityMediaPickerBinding by lazy {
        ScaffoldActivityMediaPickerBinding.inflate(layoutInflater)
    }

    private val mViewModel: MediaPickerViewModel by lazy {
        ViewModelProvider(this)[MediaPickerViewModel::class.java]
    }

    private val mBucketAdapter: MediaBucketAdapter = MediaBucketAdapter()

    private val mBucketArrowAnim: ValueAnimator = ValueAnimator()

    private val mMediaAdapter: MediaAdapter = MediaAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mViewBinding.root)

        initializeUi()
        subscribeData()
        setViewListener()

    }

    private fun initializeUi() {
        mViewBinding.topBar.setNavigationOnClickListener { finish() }
        mViewBinding.btBucket.compoundDrawables.let {
            mViewBinding.btBucket.setCompoundDrawables(
                it[0],
                it[1],
                ContextCompat.getDrawable(
                    this@MediaPickerActivity,
                    R.drawable.scaffold_anim_circle_down_filled_on_primary_24dp
                )?.apply {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                }?.mutate(),
                it[3]
            )
        }
        mBucketArrowAnim.addUpdateListener { va ->
            (va.animatedValue as? Int)?.let {
                mViewBinding.btBucket.compoundDrawables[2]?.level = it
            }
        }

        mViewBinding.rvBuckets.clipToOutline = true
        mViewBinding.rvBuckets.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, -20, view.width, view.height, 20f)
            }
        }
        mViewBinding.rvBuckets.layoutManager = LinearLayoutManager(this)
        mViewBinding.rvBuckets.adapter = mBucketAdapter

        mViewBinding.rvMedia.layoutManager = GridLayoutManager(this, 4)
        mViewBinding.rvMedia.addItemDecoration(ItemSpacingDecoration(2.dp(this)))
        mViewBinding.rvMedia.adapter = mMediaAdapter

    }

    private fun subscribeData() {
        mViewModel.bucketsLive.observe(this) { buckets ->
            mBucketAdapter.submitList(buckets)
        }
        mViewModel.bucketLive.observe(this) { bucket ->
            mViewBinding.btBucket.text = bucket?.displayName
        }
        mViewModel.mediasLive.observe(this) { medias ->
            mMediaAdapter.updateMediaList(medias)
        }
    }

    private fun setViewListener() {
        mViewBinding.btBucket.setOnClickListener { flipBucketListVisibility() }
        mViewBinding.vContentMask.setOnClickListener { closeBucketList() }
        mViewBinding.rvBuckets.attachOnItemClickListener { _, position, _ ->
            mViewModel.selectBucket(mBucketAdapter.getItemSafe(position)?.id)
            closeBucketList()
        }
    }

    private fun flipBucketListVisibility() {
        if (mViewBinding.rvBuckets.visibility == View.VISIBLE) {
            closeBucketList()
        } else {
            openBucketList()
        }
    }

    private fun openBucketList() {
        mViewBinding.rvBuckets.animation =
            AnimationUtils.loadAnimation(this, R.anim.scaffold_anim_top_in)
        mViewBinding.vContentMask.animation =
            AnimationUtils.loadAnimation(this, R.anim.scaffold_anim_mask_in)

        mBucketArrowAnim.setIntValues(0, 5000)
        mBucketArrowAnim.duration =
            resources.getInteger(R.integer.scaffold_anim_standard_decelerate).toLong()
        mBucketArrowAnim.interpolator = DecelerateInterpolator()
        mBucketArrowAnim.start()

        mViewBinding.vContentMask.visibility = View.VISIBLE
        mViewBinding.rvBuckets.visibility = View.VISIBLE
    }

    private fun closeBucketList() {
        mViewBinding.rvBuckets.animation =
            AnimationUtils.loadAnimation(this, R.anim.scaffold_anim_top_out)
        mViewBinding.vContentMask.animation =
            AnimationUtils.loadAnimation(this, R.anim.scaffold_anim_mask_out)

        mBucketArrowAnim.setIntValues(5000, 10000)
        mBucketArrowAnim.duration =
            resources.getInteger(R.integer.scaffold_anim_standard_accelerate).toLong()
        mBucketArrowAnim.interpolator = AccelerateInterpolator()
        mBucketArrowAnim.start()

        mViewBinding.vContentMask.visibility = View.GONE
        mViewBinding.rvBuckets.visibility = View.GONE

    }

}