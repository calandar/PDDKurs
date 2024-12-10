package com.example.kurs2

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val theme: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val imageUrl: String? = null,
    val hint: String? = null
)

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questions: List<Int>, // Список ID вопросов
    val isMarathon: Boolean = false, // Поле для марафонного билета
    val theme: String? = null // Новое поле для названия темы
)

@Entity(
    primaryKeys = ["ticketId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = Ticket::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserAnswer(
    val ticketId: Int,
    val questionId: Int,
    val isCorrect: Boolean,
    val userAnswer: String? = null //
)