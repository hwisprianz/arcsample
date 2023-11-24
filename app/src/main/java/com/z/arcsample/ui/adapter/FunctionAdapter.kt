package com.z.arcsample.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.z.arc.recyclerview.viewholder.ViewHolderWithBinding
import com.z.arcsample.databinding.CellFunctionBinding
import com.z.arcsample.bean.FunctionBean


/**
 *
 *
 * Created by Blate on 2023/11/17
 */
class FunctionAdapter :
    ListAdapter<FunctionBean, ViewHolderWithBinding<CellFunctionBinding>>(object :
        DiffUtil.ItemCallback<FunctionBean>() {

        override fun areItemsTheSame(oldItem: FunctionBean, newItem: FunctionBean): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: FunctionBean, newItem: FunctionBean): Boolean {
            return oldItem.iconRes == newItem.iconRes
                    && oldItem.title == newItem.title
        }

    }) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderWithBinding<CellFunctionBinding> {
        return ViewHolderWithBinding.create(parent, CellFunctionBinding::inflate)
    }

    override fun onBindViewHolder(
        holder: ViewHolderWithBinding<CellFunctionBinding>,
        position: Int
    ) {
        val item: FunctionBean = getItem(position)
        holder.viewBinding.ivIcon.setImageResource(item.iconRes ?: 0)
        holder.viewBinding.tvTitle.text = item.title
    }

    fun getItemSafety(position: Int): FunctionBean? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

}