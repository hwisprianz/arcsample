package com.z.scaffold.ui.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.z.arc.media.bean.MediaBucketBean
import com.z.arc.recyclerview.viewholder.ViewHolderWithBinding
import com.z.scaffold.databinding.ScaffoldCellMediaBucketBinding


/**
 *
 *
 * Created by Blate on 2023/12/6
 */
class MediaBucketAdapter :
    ListAdapter<MediaBucketBean, ViewHolderWithBinding<ScaffoldCellMediaBucketBinding>>(object :
        DiffUtil.ItemCallback<MediaBucketBean>() {
        override fun areItemsTheSame(
            oldItem: MediaBucketBean,
            newItem: MediaBucketBean
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MediaBucketBean,
            newItem: MediaBucketBean
        ): Boolean {
            return oldItem.cover == newItem.cover && oldItem.displayName == newItem.displayName && oldItem.count == newItem.count
        }

    }) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderWithBinding<ScaffoldCellMediaBucketBinding> {
        return ViewHolderWithBinding.create(parent, ScaffoldCellMediaBucketBinding::inflate)
    }

    override fun onBindViewHolder(
        holder: ViewHolderWithBinding<ScaffoldCellMediaBucketBinding>,
        position: Int
    ) {
        val item: MediaBucketBean = getItem(position)
        holder.viewBinding.tvName.text = item.displayName
        holder.viewBinding.tvCount.text = if (item.count != null) {
            "(${item.count})"
        } else {
            ""
        }
        Glide.with(holder.itemView.context).load(item.cover).into(holder.viewBinding.ivCover)

        holder.viewBinding.vLineTop.visibility = if (position == 0) {
            View.GONE
        } else {
            View.VISIBLE
        }
        holder.viewBinding.vLineBottom.visibility = if (position == itemCount - 1) {
            View.GONE
        } else {
            View.VISIBLE
        }

    }

    fun getItemSafe(position: Int): MediaBucketBean? {
        return if (position in currentList.indices) {
            return currentList[position]
        } else {
            null
        }
    }

}