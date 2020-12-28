package com.egeniq.interactiveexpandingappbar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.egeniq.interactiveexpandingappbar.databinding.ItemGenreBinding
import com.egeniq.interactiveexpandingappbar.model.Genre

class GenreAdapter(private val onClickListener: OnClickListener) : ListAdapter<Genre, GenreAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Genre>() {
    override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
        return oldItem.id == newItem.id
    }
}) {

    interface OnClickListener {
        fun onGenreClicked(genre: Genre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemGenreBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genre = getItem(position)
        holder.bind(genre)
        holder.itemView.setOnClickListener {
            onClickListener.onGenreClicked(genre)
        }
    }

    class ViewHolder(private val binding: ItemGenreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: Genre) {
            binding.genreName.text = genre.name
        }

    }
}