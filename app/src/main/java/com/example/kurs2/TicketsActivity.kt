package com.example.kurs2

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class TicketsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tickets)

        val db = DatabaseProvider.getDatabase(this)
        val ticketDao = db.ticketDao()
        val userAnswerDao = db.userAnswerDao()

        lifecycleScope.launch {
            val tickets = ticketDao.getAllTickets().filter { !it.isMarathon && it.theme == null }
            val adapter = TicketAdapter(tickets) { ticket ->
                lifecycleScope.launch {
                    val userAnswers = userAnswerDao.getAnswersByTicketId(ticket.id)
                    if (userAnswers.isNotEmpty()) {
                        showTicketOptionsDialog(ticket)
                    } else {
                        startTicketDetailActivity(ticket.id)
                    }
                }
            }
            val recyclerViewTickets: RecyclerView = findViewById(R.id.recyclerViewTickets)
            recyclerViewTickets.layoutManager = LinearLayoutManager(this@TicketsActivity)
            recyclerViewTickets.adapter = adapter
        }
    }

    private fun showTicketOptionsDialog(ticket: Ticket) {
        AlertDialog.Builder(this)
            .setTitle("Выберите действие")
            .setMessage("Выберите, как вы хотите продолжить решение билета")
            .setPositiveButton("Продолжить") { _, _ ->
                startTicketDetailActivity(ticket.id)
            }
            .setNegativeButton("Начать заново") { _, _ ->
                lifecycleScope.launch {
                    val db = DatabaseProvider.getDatabase(this@TicketsActivity)
                    db.userAnswerDao().deleteAnswersByTicketId(ticket.id)
                    startTicketDetailActivity(ticket.id)
                }
            }
            .show()
    }

    private fun startTicketDetailActivity(ticketId: Int) {
        val intent = Intent(this, TicketDetailActivity::class.java)
        intent.putExtra("TICKET_ID", ticketId)
        startActivity(intent)
    }
}