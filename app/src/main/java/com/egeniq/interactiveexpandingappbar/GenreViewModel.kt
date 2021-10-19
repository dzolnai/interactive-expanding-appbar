package com.egeniq.interactiveexpandingappbar

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.egeniq.interactiveexpandingappbar.model.Collection
import com.egeniq.interactiveexpandingappbar.model.Genre
import com.egeniq.interactiveexpandingappbar.model.Movie
import com.egeniq.interactiveexpandingappbar.repository.MovieDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class GenreViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val movieDbRepository: MovieDbRepository
) : ViewModel() {

    companion object {
        private val TAG = GenreViewModel::class.java.name

        private const val ID_ALL_GENRES = "all_genres"
        private const val SLOT_ITEM_COUNT = 100
    }

    private val compositeDisposable = CompositeDisposable()

    val ALL_GENRES_GENRE = Genre(id = ID_ALL_GENRES, name = context.getString(R.string.genre_all_genres))


    val genres = MutableLiveData<List<Genre>>()

    private val selectedGenre = MutableLiveData(ALL_GENRES_GENRE)

    val allGenreCollections = MutableLiveData<List<Collection>>(emptyList())

    val isInAllGenresMode = selectedGenre.map { it.id == ID_ALL_GENRES }.distinctUntilChanged()

    val title = selectedGenre.map {
        it.name
    }

    val selectedGenreMovies = MutableLiveData<List<Movie>>(emptyList())

    val isLoading = isInAllGenresMode.switchMap { isInAllGenresMode ->
        allGenreCollections.switchMap { allGenreSlots ->
            selectedGenreMovies.map { selectedGenreMovies ->
                if (isInAllGenresMode) {
                    allGenreSlots.isEmpty()
                } else {
                    selectedGenreMovies.isEmpty()
                }
            }
        }
    }

    fun isCurrentGenre(genre: Genre): Boolean {
        return selectedGenre.value?.id == genre.id
    }

    fun loadCollections() {
        compositeDisposable += movieDbRepository.getAllGenres().subscribeBy(
                onSuccess = {
                    genres.value = listOf(ALL_GENRES_GENRE) + it
                    fetchCollections(it)
                },
                onError = {
                    Log.e(TAG, "Unable to fetch all genres!", it)
                    // TODO show errror
                }
        )
    }

    private fun fetchCollections(genres: List<Genre>) {
        Observable.just(genres)
                .flatMapIterable { it }
                .flatMap { genre ->
                    movieDbRepository.searchMoviesInGenre(genre).toObservable()
                            .map { Collection(genre, it) }
                }
                .toList()
                .subscribeBy(
                        onSuccess = {
                            allGenreCollections.value = it
                        },
                        onError = { ex ->
                            Log.e(TAG, "Unable to get collections based on genres.", ex)
                        }
                )
    }

    fun selectGenre(genre: Genre) {
        selectedGenre.value = genre
        selectedGenreMovies.value = emptyList()
        if (genre.id != ID_ALL_GENRES) {
            compositeDisposable += movieDbRepository.searchMoviesInGenre(genre)
                    .subscribeBy(
                            onSuccess = {
                                selectedGenreMovies.value = it
                            },
                            onError = {
                                Log.w(TAG, "Unable to load genre movies!", it)
                                selectedGenreMovies.value = emptyList()
                            }
                    )
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}