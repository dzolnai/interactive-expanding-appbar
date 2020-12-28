package com.egeniq.interactiveexpandingappbar.repository

import com.egeniq.interactiveexpandingappbar.client.MovieDbClient
import com.egeniq.interactiveexpandingappbar.model.Genre
import com.egeniq.interactiveexpandingappbar.model.Movie
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit

class MovieDbRepository(retrofit: Retrofit) {

    private val client = retrofit.create(MovieDbClient::class.java)

    fun getAllGenres(): Single<List<Genre>> {
        return client.getGenres()
                .map { it.genres }
                .applySchedulers()
    }

    fun searchMoviesInGenre(genre: Genre): Single<List<Movie>> {
        return client.discoverMoviesInGenre(genreId = genre.id)
                .map { it.results }
                .applySchedulers()

    }

    private fun <T> Single<T>.applySchedulers(): Single<T> = compose {
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

}