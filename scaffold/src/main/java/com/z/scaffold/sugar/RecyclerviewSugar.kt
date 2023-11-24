package com.z.scaffold.sugar

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.z.arc.recyclerview.listener.RecyclerViewListenerTools


/**
 * Sugar for RecyclerView, so sweet!
 *
 * Created by Blate on 2023/11/17
 */

/**
 * Attach a [RecyclerViewListenerTools.OnItemClickListener] to this RecyclerView.
 */
fun RecyclerView.attachOnItemClickListener(listener: RecyclerViewListenerTools.OnItemClickListener): OnItemTouchListener {
    return RecyclerViewListenerTools.attachOnItemClickListener(this, listener)
}