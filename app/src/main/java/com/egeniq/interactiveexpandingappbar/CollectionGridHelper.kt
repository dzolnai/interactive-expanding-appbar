package com.egeniq.interactiveexpandingappbar

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources

object CollectionGridHelper {

    @JvmStatic
    var columnsInLandscape = 0
        private set

    @JvmStatic
    var columnsInPortrait = 0
        private set

    @JvmStatic
    fun init(context: Context) {
        val resources = context.resources
        val display = Resources.getSystem().displayMetrics
        val minPosterSize = resources.getDimensionPixelOffset(R.dimen.movie_poster_default_width)
        val spacing = resources.getDimensionPixelOffset(R.dimen.grid_spacing)
        val realWidth = if (display.widthPixels > display.heightPixels) display.widthPixels else display.heightPixels
        val realHeight = if (display.widthPixels > display.heightPixels) display.heightPixels else display.widthPixels
        val margin = context.resources.getDimensionPixelSize(R.dimen.grid_side_margin).toFloat()
        var availableWidth = realWidth - 2 * margin
        var availableHeight = realHeight - 2 * margin

        columnsInLandscape = 0
        columnsInPortrait = 0

        while (availableWidth - (minPosterSize + spacing) >= 0) {
            columnsInLandscape++
            availableWidth -= minPosterSize + spacing
        }
        while (availableHeight - (minPosterSize + spacing) >= 0) {
            columnsInPortrait++
            availableHeight -= minPosterSize + spacing
        }
    }

    fun columnsForOrientation(context: Context): Int {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnsInPortrait
        } else {
            columnsInLandscape
        }
    }


}