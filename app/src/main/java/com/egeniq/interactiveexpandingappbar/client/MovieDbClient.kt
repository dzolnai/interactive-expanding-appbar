package com.egeniq.interactiveexpandingappbar.client

import com.egeniq.interactiveexpandingappbar.model.Genre
import com.egeniq.interactiveexpandingappbar.model.GenreList
import com.egeniq.interactiveexpandingappbar.model.Page
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieDbClient {

    @GET("genre/movie/list")
    fun getGenres() : Single<GenreList>

    @GET("discover/movie")
    fun discoverMoviesInGenre(
            @Query("with_genres") genreId: String,
    ) : Single<Page>
}