package com.example.kurs2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btnTickets: Button = findViewById(R.id.btnTickets)
        val btnThemes: Button = findViewById(R.id.btnThemes)
        val btnMarathon: Button = findViewById(R.id.btnMarathon)

        lifecycleScope.launch {
            DatabaseProvider.addInitialQuestionsIfNotExist(this@MainActivity)
            DatabaseProvider.addInitialTicketsIfNotExist(this@MainActivity)
        }


        btnTickets.setOnClickListener {
            val intent = Intent(this, TicketsActivity::class.java)
            startActivity(intent)
        }

        btnThemes.setOnClickListener {
            val intent = Intent(this, ThemesActivity::class.java)
            startActivity(intent)
        }

        btnMarathon.setOnClickListener {
            val intent = Intent(this, MarathonActivity::class.java)
            startActivity(intent)
        }
    }
}