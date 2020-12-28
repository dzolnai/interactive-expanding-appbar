package com.egeniq.interactiveexpandingappbar.adapter.decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.egeniq.interactiveexpandingappbar.R

class ListSpacingItemDecoration(context: Context): RecyclerView.ItemDecoration() {

    private val space = context.resources.getDimensionPixelOffset(R.dimen.list_spacing)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.right = space
    }
}