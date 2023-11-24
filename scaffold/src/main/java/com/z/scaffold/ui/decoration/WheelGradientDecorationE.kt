package com.z.scaffold.ui.decoration

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.TypedValue

import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.R


/**
 * E型滚轮渐变装饰器
 *
 * 这是一个使用图层混合模式的用于RecyclerView的装饰器:
 * * 区域被纵向均分为5份 (A,B,C,D,E; 所以叫E型...)
 * * 中心区域为主色纯色填充
 * * 中心到边缘位置是表面前景色不同透明度的线性渐变
 * * 将使用 [PorterDuff.Mode.SRC_IN] 模式将渐变层与主色层混合. 强制将前景着色为渐变色
 *
 * **这要求RecyclerView关闭硬件加速 [RecyclerView.setLayerType] ([android.view.View.LAYER_TYPE_SOFTWARE]); 也许会损失一些性能**
 *
 * Created by Blate on 2023/11/10
 */
class WheelGradientDecorationE : ItemDecoration() {

    private val _paint = Paint()

    init {
        _paint.isAntiAlias = true
        _paint.style = Paint.Style.FILL
        _paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        c.save()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        c.restore()
        @Suppress("RedundantExplicitType") val typedValue: TypedValue = TypedValue()

        parent.context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val highlight: Int = ContextCompat.getColor(parent.context, typedValue.resourceId)

        parent.context.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
        typedValue.data
        val text: Int = ContextCompat.getColor(parent.context, typedValue.resourceId)

        val center: Int = ColorUtils.setAlphaComponent(text, 0x99)
        val edge: Int = ColorUtils.setAlphaComponent(text, 0x33)

        val shader: Shader = LinearGradient(
            0f,
            0f,
            0f,
            parent.height.toFloat(),
            intArrayOf(edge, center, highlight, highlight, center, edge),
            floatArrayOf(0f, 0.4f, 0.4f, 0.6f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        _paint.shader = shader
        c.drawRect(0f, 0f, parent.width.toFloat(), parent.height.toFloat(), _paint)
    }

}