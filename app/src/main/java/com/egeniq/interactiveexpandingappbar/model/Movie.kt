package com.egeniq.interactiveexpandingappbar.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
        val id: String,
        val title: String,
        @SerializedName("poster_path")
        val posterPath: String?
) : Parcelable