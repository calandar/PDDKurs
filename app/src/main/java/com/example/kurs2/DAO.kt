package com.example.kurs2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Insert
    suspend fun insert(question: Question)

    @Query("SELECT * FROM questions WHERE theme = :theme")
    suspend fun getQuestionsByTheme(theme: String): List<Question>

    @Query("SELECT * FROM questions WHERE id IN (:questionIds)")
    suspend fun getQuestionsByIds(questionIds: List<Int>): List<Question>

    @Query("SELECT EXISTS(SELECT 1 FROM questions WHERE question = :questionText LIMIT 1)")
    suspend fun isQuestionExists(questionText: String): Boolean
}

@Dao
interface TicketDao {
    @Insert
    suspend fun insert(ticket: Ticket)

    @Query("SELECT * FROM tickets")
    suspend fun getAllTickets(): List<Ticket>

    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    suspend fun getTicketById(ticketId: Int): Ticket

    @Query("SELECT EXISTS(SELECT 1 FROM tickets WHERE questions = :questions)")
    suspend fun isTicketExists(questions: List<Int>): Boolean
}

@Dao
interface UserAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userAnswer: UserAnswer)

    @Query("SELECT * FROM UserAnswer WHERE ticketId = :ticketId")
    suspend fun getAnswersByTicketId(ticketId: Int): List<UserAnswer>

    @Query("DELETE FROM UserAnswer WHERE ticketId = :ticketId")
    suspend fun deleteAnswersByTicketId(ticketId: Int)
}