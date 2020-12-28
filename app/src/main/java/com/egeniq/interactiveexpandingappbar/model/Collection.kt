package com.egeniq.interactiveexpandingappbar.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Collection(
        val genre: Genre,
        val movies: List<Movie>
) : Parcelable