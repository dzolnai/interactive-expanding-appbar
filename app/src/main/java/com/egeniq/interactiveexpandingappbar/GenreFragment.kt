package com.egeniq.interactiveexpandingappbar

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.TextPaint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.egeniq.interactiveexpandingappbar.CollectionGridHelper.columnsInLandscape
import com.egeniq.interactiveexpandingappbar.CollectionGridHelper.columnsInPortrait
import com.egeniq.interactiveexpandingappbar.adapter.CollectionAdapter
import com.egeniq.interactiveexpandingappbar.adapter.GenreAdapter
import com.egeniq.interactiveexpandingappbar.adapter.MovieAdapter
import com.egeniq.interactiveexpandingappbar.adapter.decoration.GridSpacingItemDecoration
import com.egeniq.interactiveexpandingappbar.binding.BindingFragment
import com.egeniq.interactiveexpandingappbar.databinding.FragmentGenreBinding
import com.egeniq.interactiveexpandingappbar.databinding.PopupGenreChooserBinding
import com.egeniq.interactiveexpandingappbar.model.Collection
import com.egeniq.interactiveexpandingappbar.model.Genre
import com.egeniq.interactiveexpandingappbar.model.Movie
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenreFragment : BindingFragment<FragmentGenreBinding>() {
    override val layout = R.layout.fragment_genre

    private val viewModel by viewModels<GenreViewModel>()

    private val collapsedTextPaint = TextPaint()
    private val expandedTextPaint = TextPaint()

    private var collapsedTextWidth = 0f
    private var expandedTextWidth = 0f

    private var collapsedMargin = 0f
    private var expandedMargin = 0f

    private var lastOffset = Int.MIN_VALUE

    private lateinit var genreAdapter: GenreAdapter

    private var genreChooserPopupWindow: PopupWindow? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.collapsingToolbar.title = getString(R.string.genre_all_genres)
        if (arguments?.containsKey(KEY_GENRE) == true) {
            val genre = requireArguments().getParcelable<Genre>(KEY_GENRE)!!
            viewModel.selectGenre(genre)
        } else {
            viewModel.loadCollections()
        }
        binding.viewModel = viewModel
        val movieAdapter = MovieAdapter(object : MovieAdapter.ClickListener {
            override fun onMovieClicked(movie: Movie) {
                openMovie(movie)
            }
        }, false)
        val slotAdapter = CollectionAdapter(object : CollectionAdapter.ClickListener {
            override fun onMovieClicked(movie: Movie) {
                openMovie(movie)
            }

            override fun onCollectionTitleClicked(collection: Collection) {
                openCollection(collection)

            }
        })
        genreAdapter = GenreAdapter(object : GenreAdapter.OnClickListener {
            override fun onGenreClicked(genre: Genre) {
                hidePopupWindow()
                onGenreSelected(genre)
            }
        })
        viewModel.genres.observe(viewLifecycleOwner) {
            genreAdapter.submitList(it)
        }

        viewModel.selectedGenreMovies.observe(viewLifecycleOwner) {
            movieAdapter.submitList(it)
        }
        viewModel.allGenreCollections.observe(viewLifecycleOwner) {
            slotAdapter.submitList(it)
        }
        viewModel.isInAllGenresMode.observe(viewLifecycleOwner) { isInAllGenresMode ->
            if (isInAllGenresMode) {
                binding.content.adapter = slotAdapter
                attachLinearLayoutManager()
            } else {
                binding.content.adapter = movieAdapter
                attachGridLayoutManager(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            }
        }

        viewModel.title.observe(viewLifecycleOwner) {
            binding.collapsingToolbar.title = it
            recalculateTextWidth()
            lastOffset = Int.MIN_VALUE // Will make sure that the offset callback is triggered
            binding.collapsingToolbar.requestLayout()
        }
        binding.clickableLayout.setOnClickListener {
            if (!hidePopupWindow()) {
                displayGenreChooser()
            }
        }
        binding.content.setHasFixedSize(true)
        setupOffsetListener()
    }

    private fun openMovie(movie: Movie) {
        Toast.makeText(requireContext(), "Clicked on movie: ${movie.title}", Toast.LENGTH_LONG).show()
    }

    private fun openCollection(collection: Collection) {
        onGenreSelected(collection.genre)
    }

    fun onGenreSelected(genre: Genre) {
        when {
            viewModel.isCurrentGenre(genre) -> {
                // Do nothing
            }
            genre == viewModel.ALL_GENRES_GENRE -> {
                (activity as GenreActivity).goToAllGenres()
            }
            else -> {
                requireActivity().startActivityForResult(GenreActivity.getIntent(requireContext(), genre), GenreActivity.REQUEST_CODE_GENRE_DETAIL)
            }
        }
    }

    private fun hidePopupWindow(): Boolean {
        return if (genreChooserPopupWindow != null) {
            genreChooserPopupWindow?.dismiss()
            true
        } else {
            false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (binding.content.layoutManager is GridLayoutManager) {
            attachGridLayoutManager(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        }
    }

    private fun attachLinearLayoutManager() {
        binding.content.layoutManager = LinearLayoutManager(requireContext())
        binding.content.updatePadding(left = 0, right = 0)
    }

    private fun attachGridLayoutManager(isPortrait: Boolean) {
        while (binding.content.itemDecorationCount > 0) binding.content.removeItemDecorationAt(0)
        val itemSpacing = resources.getDimensionPixelOffset(R.dimen.grid_spacing)
        val numColumns = if (isPortrait) columnsInPortrait else columnsInLandscape
        binding.content.addItemDecoration(GridSpacingItemDecoration(itemSpacing))
        binding.content.layoutManager = GridLayoutManager(requireContext(), numColumns, GridLayoutManager.VERTICAL, false)
        val sidePadding = resources.getDimensionPixelOffset(R.dimen.grid_side_margin)
        binding.content.updatePadding(left = sidePadding, right = sidePadding)
    }

    private fun setupOffsetListener() {
        val maxOffset = (binding.expandedSize.layoutParams.height - binding.toolbar.layoutParams.height).toFloat()
        val sizeFontAttributes = intArrayOf(android.R.attr.textSize, android.R.attr.fontFamily)

        @StyleableRes // Not really, but fixes an incorrect IDE warning
        var index = 0
        val collapsedAttributes = requireContext().obtainStyledAttributes(R.style.AppTheme_CollapsingToolbarTitle_Collapsed, sizeFontAttributes)
        val collapsedTextSize = collapsedAttributes.getDimension(index++, 0f)
        val collapsedFontId = collapsedAttributes.getResourceId(index, -1)
        val collapsedFont = ResourcesCompat.getFont(requireContext(), collapsedFontId)
        collapsedAttributes.recycle()

        index = 0

        val expandedAttributes = requireContext().obtainStyledAttributes(R.style.AppTheme_CollapsingToolbarTitle_Expanded, sizeFontAttributes)
        val expandedTextSize = expandedAttributes.getDimension(index++, 0f)
        val expandedFontId = expandedAttributes.getResourceId(index, -1)
        val expandedFont = ResourcesCompat.getFont(requireContext(), expandedFontId)
        expandedAttributes.recycle()

        collapsedTextPaint.typeface = collapsedFont
        collapsedTextPaint.textSize = collapsedTextSize

        expandedTextPaint.typeface = expandedFont
        expandedTextPaint.textSize = expandedTextSize

        recalculateTextWidth()

        val extraWidth = resources.getDimension(R.dimen.genre_clickable_layout_extra_width) // For the icon and the margin
        val interpolator = DecelerateInterpolator(0.9f)

        val collapsedBottomPadding = resources.getDimension(R.dimen.clickable_layout_bottom_padding_collapsed)
        val expandedBottomPadding = resources.getDimension(R.dimen.clickable_layout_bottom_padding_expanded)

        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            // By updating the LayoutParams, we trigger the offset change listener, which then results in a somewhat infinite loop.
            // This flag makes sure that we discard the next callback after updating the layout.
            if (verticalOffset == lastOffset) {
                return@OnOffsetChangedListener
            }
            val offsetPercentage = verticalOffset / -maxOffset // 0 is fully expanded, 1 is fully collapsed
            val interpolatedPercentage = interpolator.getInterpolation(offsetPercentage)
            val baseWidth = expandedTextWidth - (expandedTextWidth - collapsedTextWidth) * interpolatedPercentage
            val marginExtra = expandedMargin - (expandedMargin - collapsedMargin) * offsetPercentage
            val fullWidth = baseWidth + marginExtra + extraWidth
            lastOffset = verticalOffset
            binding.clickableLayout.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                width = fullWidth.toInt()
            }
            binding.clickableLayout.updatePadding(bottom = ((expandedBottomPadding - (expandedBottomPadding - collapsedBottomPadding) * offsetPercentage).toInt()))
        })
    }

    private fun recalculateTextWidth() {
        collapsedTextWidth = collapsedTextPaint.measureText(binding.collapsingToolbar.title.toString())
        expandedTextWidth = expandedTextPaint.measureText(binding.collapsingToolbar.title.toString())

        collapsedMargin = (Resources.getSystem().displayMetrics.widthPixels - collapsedTextWidth) / 2f
        expandedMargin = resources.getDimension(R.dimen.genre_clickable_title_left_margin)
    }

    private fun displayGenreChooser() {
        binding.chevron.animate().rotation(180f).setDuration(CHEVRON_ROTATION_DURATION_MS).start()
        val popupBinding = PopupGenreChooserBinding.inflate(requireActivity().layoutInflater, null, false)
        popupBinding.genreList.adapter = genreAdapter
        popupBinding.genreList.layoutManager = LinearLayoutManager(requireContext())

        val targetWidth = binding.content.width
        val targetHeight = (binding.root.height - binding.appBar.height) - lastOffset
        popupBinding.genreList.layoutParams = ViewGroup.LayoutParams(targetWidth, targetHeight)
        genreChooserPopupWindow = PopupWindow(popupBinding.root, targetWidth, targetHeight, true).apply {
            setOnDismissListener {
                binding.chevron.animate().rotation(0f).setDuration(CHEVRON_ROTATION_DURATION_MS).start()
                genreChooserPopupWindow = null
            }
        }
        PopupWindowCompat.showAsDropDown(genreChooserPopupWindow!!, binding.appBar, 0, 0, Gravity.BOTTOM)
    }

    internal fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    internal fun getTouchForwarder(): View {
        return binding.backButtonTouchForwarder
    }

    internal fun disallowToolbarExpand() {
        // Taken from: https://stackoverflow.com/a/49218710/1395437
        binding.appBar.setExpanded(false, false)
        ViewCompat.setNestedScrollingEnabled(binding.content, false)
        val params = binding.appBar.layoutParams as CoordinatorLayout.LayoutParams
        if (params.behavior == null) {
            params.behavior = AppBarLayout.Behavior()
        }
        val behaviour = params.behavior as AppBarLayout.Behavior
        behaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })
        binding.chevron.isVisible = false
        binding.clickableLayout.isVisible = false
    }


    companion object {
        private const val CHEVRON_ROTATION_DURATION_MS = 250L

        private const val KEY_GENRE = "genre"

        operator fun invoke(genre: Genre?): GenreFragment {
            val fragment = GenreFragment()
            val args = Bundle()
            if (genre != null) {
                args.putParcelable(KEY_GENRE, genre)
            }
            fragment.arguments = args
            return fragment
        }
    }
}