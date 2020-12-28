package com.egeniq.interactiveexpandingappbar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.egeniq.interactiveexpandingappbar.R
import com.egeniq.interactiveexpandingappbar.databinding.ItemMovieBinding
import com.egeniq.interactiveexpandingappbar.model.Movie
import com.egeniq.interactiveexpandingappbar.util.formatAsPosterUrl

class MovieAdapter(
        private var clickListener: ClickListener? = null,
        private var fixedSize: Boolean
) : ListAdapter<Movie, MovieAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Movie>() {
    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }
}) {


    interface ClickListener {
        fun onMovieClicked(movie: Movie)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = getItem(position)
        holder.bind(movie)
        holder.itemView.setOnClickListener {
            clickListener?.onMovieClicked(movie)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMovieBinding.inflate(inflater, parent, false)
        if (fixedSize) {
            val resources = binding.root.resources
            binding.root.layoutParams.apply {
                width = resources.getDimensionPixelOffset(R.dimen.movie_poster_small_width)
                height = resources.getDimensionPixelOffset(R.dimen.movie_poster_small_height)
            }
        }
        return ViewHolder(binding)
    }

    class ViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {

        private val cornerRadius = itemView.resources.getDimensionPixelOffset(R.dimen.movie_poster_corner_radius)

        fun bind(movie: Movie) {
            Glide.with(binding.poster).load(movie.posterPath.formatAsPosterUrl())
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(cornerRadius)))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.poster)
        }
    }
}