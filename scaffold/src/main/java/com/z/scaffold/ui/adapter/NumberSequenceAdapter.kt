package com.z.scaffold.ui.adapter

import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.z.arc.recyclerview.viewholder.ViewHolderWithBinding
import com.z.scaffold.databinding.ScaffoldCellTextOnBottomWheelBinding


/**
 * 数字序列适配器
 *
 * 调用 [updateData] 更新数据, 会自动计算出差异并进行局部刷新
 *
 * Created by Blate on 2023/11/6
 */
class NumberSequenceAdapter(private var _offset: Int = 0, private var _count: Int = 0) :
    RecyclerView.Adapter<ViewHolderWithBinding<ScaffoldCellTextOnBottomWheelBinding>>() {

    interface NumberFormatter {
        fun format(value: Int?): CharSequence?

    }

    private var _umberFormatter: NumberFormatter? = null

    val offset: Int
        get() = _offset

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderWithBinding<ScaffoldCellTextOnBottomWheelBinding> {
        return ViewHolderWithBinding.create(parent, ScaffoldCellTextOnBottomWheelBinding::inflate)
    }

    override fun getItemCount(): Int {
        return _count
    }

    override fun onBindViewHolder(
        holder: ViewHolderWithBinding<ScaffoldCellTextOnBottomWheelBinding>,
        position: Int
    ) {
        holder.viewBinding.text.text = _umberFormatter.let {
            if (it == null) {
                getRealValue(position).toString()
            } else {
                it.format(getRealValue(position))
            }
        }
    }

    fun updateData(offset: Int, @IntRange(from = 0) count: Int) {
        if (_count > 0 && count > 0) {
            val oldOffset: Int = _offset
            val oldCount: Int = _count
            val oldStart: Int = oldOffset
            val oldEnd: Int = oldOffset + oldCount - 1
            _offset = offset
            _count = count
            val newStart: Int = offset
            val newEnd: Int = offset + count - 1
            if (oldStart > newEnd || oldEnd < newStart) {
                // 分离
                val countDiff = count - oldCount
                if (countDiff > 0) {
                    notifyItemRangeChanged(0, oldCount)
                    notifyItemRangeInserted(oldCount, countDiff)
                } else if (countDiff < 0) {
                    notifyItemRangeChanged(0, count)
                    notifyItemRangeRemoved(count, -countDiff)
                } else {
                    notifyItemRangeChanged(0, oldCount)
                }
            } else {
                // 重合
                val endDiff: Int = newEnd - oldEnd
                if (endDiff > 0) {
                    notifyItemRangeInserted(oldCount, endDiff)
                } else if (endDiff < 0) {
                    notifyItemRangeRemoved(oldCount + endDiff, -endDiff)
                }
                val startDiff: Int = newStart - oldStart
                if (startDiff > 0) {
                    notifyItemRangeRemoved(0, startDiff)
                } else if (startDiff < 0) {
                    notifyItemRangeInserted(0, -startDiff)
                }
            }
        } else if (_count > 0) {
            // count <= 0
            _offset = offset
            val oldCount = _count
            _count = count
            notifyItemRangeRemoved(0, oldCount)
        } else if (count > 0) {
            // _count <= 0
            _offset = offset
            _count = count
            notifyItemRangeInserted(0, count)
        } else {
            // _count <=0 && count <=0
            // do nothing
        }
    }

    @Suppress("unused")
    fun setNumberFormatter(formatter: NumberFormatter?) {
        _umberFormatter = formatter
        notifyItemRangeChanged(0, itemCount)
    }

    fun getRealValue(@IntRange(from = 0) position: Int): Int? {
        return if (position in 0 until _count) {
            position + _offset
        } else {
            null
        }
    }

}