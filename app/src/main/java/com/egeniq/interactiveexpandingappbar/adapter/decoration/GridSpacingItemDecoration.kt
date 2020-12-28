package com.egeniq.interactiveexpandingappbar.adapter.decoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

/**
 * Normally we would apply spacing between items, and no spacing on the edges, so we do not need to make extra calculations on the margins.
 * However, this has one downside, which is that when items switch places, their spacing might be wrong and they would jump.
 */
class GridSpacingItemDecoration(@Px preferredSpace: Int): RecyclerView.ItemDecoration() {

    private val space = if (preferredSpace % 3 == 0) preferredSpace else (preferredSpace + (3 - preferredSpace % 3))

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = space / 2
        outRect.right = space / 2
        outRect.top = space / 2
        outRect.bottom = space / 2
    }
}