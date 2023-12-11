package com.z.scaffold.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.z.arc.media.bean.IMediaList
import com.z.arc.media.bean.MediaBean
import com.z.arc.recyclerview.viewholder.ViewHolderWithBinding
import com.z.scaffold.R
import com.z.scaffold.databinding.ScaffoldCellMediaBinding
import com.z.scaffold.tools.TimeTools


/**
 *
 *
 * Created by Blate on 2023/12/8
 */
class MediaAdapter : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private var mMediaList: IMediaList? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        return MediaViewHolder(
            ScaffoldCellMediaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mMediaList?.count ?: 0
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        mMediaList?.fill(position, holder.media)
        holder.render()
    }

    fun updateMediaList(mediaList: IMediaList?) {
        val oldSize: Int = mMediaList?.count ?: 0
        val newSize: Int = mediaList?.count ?: 0

        mMediaList = mediaList

        val diff: Int = newSize - oldSize

        notifyItemRangeChanged(0, oldSize.coerceAtMost(newSize))
        if (diff > 0) {
            notifyItemRangeInserted(oldSize, diff)
        } else if (diff < 0) {
            notifyItemRangeRemoved(newSize, -diff)
        }

    }

    class MediaViewHolder(binding: ScaffoldCellMediaBinding) :
        ViewHolderWithBinding<ScaffoldCellMediaBinding>(binding) {

        val media: MediaBean = MediaBean()

        fun render() {
            Glide.with(itemView.context)
                .load(media.uri)
                .placeholder(R.drawable.scaffold_anim_circle_down_filled_on_primary_24dp)
                .into(viewBinding.ivMedia)

            if (media.mimeType.startsWith("video")) {
                viewBinding.groupVideo.visibility = View.VISIBLE
                viewBinding.tvVideoDuration.text = TimeTools.formatVideoDuration(media.duration)
            } else {
                viewBinding.groupVideo.visibility = View.GONE
                viewBinding.tvVideoDuration.text = null
            }

        }

    }

}