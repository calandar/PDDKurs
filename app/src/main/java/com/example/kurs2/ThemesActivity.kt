package com.example.kurs2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ThemesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_themes)

        val db = DatabaseProvider.getDatabase(this)
        val questionDao = db.questionDao()

        lifecycleScope.launch {
            val themes = questionDao.getAllThemes()
            val recyclerViewThemes: RecyclerView = findViewById(R.id.recyclerViewThemes)
            recyclerViewThemes.layoutManager = LinearLayoutManager(this@ThemesActivity)
            recyclerViewThemes.adapter = ThemesAdapter(themes) { theme ->
                val intent = Intent(this@ThemesActivity, ThemeTicketActivity::class.java)
                intent.putExtra("THEME", theme)
                startActivity(intent)
            }
        }
    }
}