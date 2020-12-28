package com.egeniq.interactiveexpandingappbar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.egeniq.interactiveexpandingappbar.adapter.decoration.ListSpacingItemDecoration
import com.egeniq.interactiveexpandingappbar.databinding.ItemCollectionBinding
import com.egeniq.interactiveexpandingappbar.model.Collection
import com.egeniq.interactiveexpandingappbar.model.Movie

class CollectionAdapter(private var clickListener: ClickListener? = null) : ListAdapter<Collection, CollectionAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Collection>() {
    override fun areContentsTheSame(oldItem: Collection, newItem: Collection): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Collection, newItem: Collection): Boolean {
        return oldItem.genre.id == newItem.genre.id
    }
}) {

    interface ClickListener {
        fun onMovieClicked(movie: Movie)
        fun onCollectionTitleClicked(collection: Collection)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickListener)
    }


    class ViewHolder(private val binding: ItemCollectionBinding,
                     private val clickListener: ClickListener?) : RecyclerView.ViewHolder(binding.root) {

        fun bind(collection: Collection) {
            binding.title.text = collection.genre.name
            binding.title.setOnClickListener { clickListener?.onCollectionTitleClicked(collection) }
            val layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
            val movieAdapter = MovieAdapter(object : MovieAdapter.ClickListener {
                override fun onMovieClicked(movie: Movie) {
                    clickListener?.onMovieClicked(movie)
                }
            }, true)
            movieAdapter.submitList(collection.movies)
            while (binding.items.itemDecorationCount > 0) {
                binding.items.removeItemDecorationAt(0)
            }
            binding.items.addItemDecoration(ListSpacingItemDecoration(binding.root.context))
            binding.items.layoutManager = layoutManager
            binding.items.adapter = movieAdapter
        }

    }
}