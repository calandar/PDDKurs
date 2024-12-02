package com.example.kurs2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuestionNumberAdapter(private val questionNumbers: List<Int>, private val onQuestionClick: (Int) -> Unit) :
    RecyclerView.Adapter<QuestionNumberAdapter.QuestionNumberViewHolder>() {

    private val answeredQuestions = mutableMapOf<Int, Boolean>()
    private var currentQuestionIndex: Int = 0

    inner class QuestionNumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionNumber: TextView = itemView.findViewById(R.id.questionNumber)

        fun bind(number: Int, isCorrect: Boolean?, isCurrent: Boolean) {
            questionNumber.text = number.toString()
            itemView.setOnClickListener { onQuestionClick(number) }
            if (isCorrect != null) {
                itemView.setBackgroundColor(if (isCorrect) Color.GREEN else Color.RED)
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }
            if (isCurrent) {
                itemView.setBackgroundResource(R.drawable.current_question_background) // Используйте свой ресурс для текущего вопроса
            } else {
                itemView.setBackgroundResource(0) // Сброс фона
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionNumberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question_number, parent, false)
        return QuestionNumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionNumberViewHolder, position: Int) {
        val number = questionNumbers[position]
        holder.bind(number, answeredQuestions[number], currentQuestionIndex + 1 == number)
    }

    override fun getItemCount(): Int = questionNumbers.size

    fun updateQuestionColor(number: Int, isCorrect: Boolean) {
        answeredQuestions[number] = isCorrect
        notifyDataSetChanged()
    }

    fun setAnsweredQuestions(answeredQuestions: Map<Int, Boolean>) {
        this.answeredQuestions.clear()
        this.answeredQuestions.putAll(answeredQuestions)
        notifyDataSetChanged()
    }

    fun setCurrentQuestion(currentQuestionIndex: Int) {
        this.currentQuestionIndex = currentQuestionIndex
        notifyDataSetChanged()
    }
}