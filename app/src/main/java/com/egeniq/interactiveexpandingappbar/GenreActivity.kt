package com.egeniq.interactiveexpandingappbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.egeniq.interactiveexpandingappbar.binding.BindingActivity
import com.egeniq.interactiveexpandingappbar.databinding.ActivityGenreBinding
import com.egeniq.interactiveexpandingappbar.model.Genre
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenreActivity : BindingActivity<ActivityGenreBinding>() {

    override val layout = R.layout.activity_genre

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val genre = intent.getParcelableExtra<Genre>(KEY_GENRE)
        val fragment = GenreFragment(genre)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResumeFragments() {
        super.onResumeFragments()
        val genreFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id) as? GenreFragment ?: return
        genreFragment.disallowToolbarExpand()
        val toolbar = genreFragment.getToolbar()
        toolbar.setNavigationIcon(R.drawable.ic_back)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayHomeAsUpEnabled(true)
        }
        // We have a layout issue here: if the text is in the same row as the toolbar, we want the toolbar back button to take precedence.
        // Normally this would work if we place the toolbar on top, but the toolbar has a weird implementation that it 'eats' all touch events which are not directed to one of its children.
        // But if we place the text on top, it will consume the touch from the back button, because it overlaps that part.
        // So we place the toolbar on the bottom, the text on top of it, and on the top of that a touch forwarder, which is at the place where the back button is always.
        // When the touch forwarder is touched, it will forward the touch to the toolbar, which is below it, so triggering the touch of the back button.
        genreFragment.getTouchForwarder().setOnTouchListener { _, event ->
            return@setOnTouchListener toolbar.dispatchTouchEvent(event)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // When the user selects 'All genres', we close all detail pages and go back to the main activity, where it is already open
        if (requestCode == REQUEST_CODE_GENRE_DETAIL) {
            if (resultCode == RESULT_CODE_GO_TO_ALL_GENRES) {
                setResult(RESULT_CODE_GO_TO_ALL_GENRES)
                finish()
            }
        }
    }

    fun goToAllGenres() {
        setResult(RESULT_CODE_GO_TO_ALL_GENRES)
        finish()
    }


    companion object {
        private const val KEY_GENRE = "genre"

        const val REQUEST_CODE_GENRE_DETAIL = 700
        const val RESULT_CODE_GO_TO_ALL_GENRES = 100

        fun getIntent(context: Context, genre: Genre) : Intent {
            val intent = Intent(context, GenreActivity::class.java)
            intent.putExtra(KEY_GENRE, genre)
            return intent
        }
    }
}