package com.example.kurs2

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuestionNumberAdapter(
    private val questionNumbers: List<Int>,
    private var userAnswers: Map<Int, UserAnswer>, // Изменяем на var
    private val onQuestionClick: (Int) -> Unit
) : RecyclerView.Adapter<QuestionNumberAdapter.QuestionNumberViewHolder>() {

    private var currentQuestionIndex: Int = 0

    inner class QuestionNumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionNumber: TextView = itemView.findViewById(R.id.questionNumber)

        fun bind(number: Int, isCurrent: Boolean, isCorrect: Boolean?) {
            questionNumber.text = number.toString()
            itemView.setOnClickListener { onQuestionClick(number) }
            if (isCurrent) {
                itemView.setBackgroundResource(R.drawable.current_question_background) // Используйте свой ресурс для текущего вопроса
            } else {
                itemView.setBackgroundResource(0) // Сброс фона
//                isCorrect?.let {
//                    itemView.setBackgroundColor(if (it) Color.GREEN else Color.RED)
//                }
            }
            //Log.d("QuestionNumberAdapter", "Binding question number: $number, isCurrent: $isCurrent, isCorrect: $isCorrect")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionNumberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_question_number, parent, false)
        return QuestionNumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionNumberViewHolder, position: Int) {
        val number = questionNumbers[position]
        val isCorrect = userAnswers[number - 1]?.isCorrect
        holder.bind(number, currentQuestionIndex + 1 == number, isCorrect)
        //Log.d("QuestionNumberAdapter", "Binding view holder at position: $position, number: $number, isCorrect: $isCorrect")

    }

    override fun getItemCount(): Int = questionNumbers.size

    fun setCurrentQuestion(currentQuestionIndex: Int) {
        this.currentQuestionIndex = currentQuestionIndex
        notifyDataSetChanged()
    }

    fun updateAnswers(userAnswers: Map<Int, UserAnswer>) {
        this.userAnswers = userAnswers // Обновляем состояние ответов
        notifyDataSetChanged()
    }
}