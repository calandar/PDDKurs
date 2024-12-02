package com.example.kurs2
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kurs2.DatabaseProvider
import com.example.kurs2.R
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRules: Button = findViewById(R.id.btnRules)
        val btnTickets: Button = findViewById(R.id.btnTickets)
        val btnThemes: Button = findViewById(R.id.btnThemes)
        val btnMarathon: Button = findViewById(R.id.btnMarathon)


        lifecycleScope.launch {
            // Добавление начальных вопросов
            DatabaseProvider.addInitialQuestionsIfNotExist(this@MainActivity)

            // Добавление начальных билетов
            DatabaseProvider.addInitialTicketsIfNotExist(this@MainActivity)
        }

        btnRules.setOnClickListener {
            // Переход к изучению правил дорожного движения
        }

        btnTickets.setOnClickListener {
            val intent = Intent(this, TicketsActivity::class.java)
            startActivity(intent)
        }

        btnThemes.setOnClickListener {
            // Переход к тренировке по темам
        }

        btnMarathon.setOnClickListener {
            // Переход к марафону
        }

        val db = DatabaseProvider.getDatabase(this)
        val questionDao = db.questionDao()
        val ticketDao = db.ticketDao()

    }


}