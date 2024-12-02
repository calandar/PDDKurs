package com.example.kurs2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class TicketAdapter(private val tickets: List<Ticket>, private val onTicketClick: (Ticket) -> Unit) :
    RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ticketNumber: TextView = itemView.findViewById(R.id.ticketNumber)
        private val ticketStatus: TextView = itemView.findViewById(R.id.ticketStatus)

        fun bind(ticket: Ticket, isStarted: Boolean, isCompleted: Boolean, correctAnswers: Int, totalQuestions: Int) {
            ticketNumber.text = "Билет №${ticket.id}"
            if (isCompleted) {
                itemView.setBackgroundColor(if (correctAnswers == totalQuestions) Color.GREEN else Color.RED)
                ticketStatus.text = if (correctAnswers == totalQuestions) "Решено" else "$correctAnswers/$totalQuestions"
            } else if (isStarted) {
                itemView.setBackgroundColor(Color.YELLOW)
                ticketStatus.text = "Начато"
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
                ticketStatus.text = ""
            }
            itemView.setOnClickListener { onTicketClick(ticket) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        // Здесь нужно получить статус билета из базы данных
        val isStarted = false // Замените на логику получения статуса
        val isCompleted = false // Замените на логику получения статуса
        val correctAnswers = 0 // Замените на логику получения количества правильных ответов
        val totalQuestions = ticket.questions.size
        holder.bind(ticket, isStarted, isCompleted, correctAnswers, totalQuestions)
    }

    override fun getItemCount(): Int = tickets.size
}