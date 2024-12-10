package com.example.kurs2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ThemesAdapter(private val themes: List<String>, private val onThemeClick: (String) -> Unit) :
    RecyclerView.Adapter<ThemesAdapter.ThemeViewHolder>() {

    inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val themeName: TextView = itemView.findViewById(R.id.themeName)

        fun bind(theme: String, onThemeClick: (String) -> Unit) {
            themeName.text = theme
            itemView.setOnClickListener { onThemeClick(theme) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]
        holder.bind(theme, onThemeClick)
    }

    override fun getItemCount(): Int = themes.size
}