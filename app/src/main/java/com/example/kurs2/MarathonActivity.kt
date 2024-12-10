package com.example.kurs2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MarathonActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var ticketDao: TicketDao
    private lateinit var questionDao: QuestionDao
    private lateinit var userAnswerDao: UserAnswerDao
    private var ticketId: Int = -1
    private lateinit var questionSelectorFragment: QuestionSelectorFragment
    private lateinit var questionDisplayFragment: QuestionDisplayFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marathon)

        db = DatabaseProvider.getDatabase(this)
        ticketDao = db.ticketDao()
        questionDao = db.questionDao()
        userAnswerDao = db.userAnswerDao()

        questionSelectorFragment = QuestionSelectorFragment()
        questionDisplayFragment = QuestionDisplayFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainerTop, questionSelectorFragment)
            .add(R.id.fragmentContainerBottom, questionDisplayFragment)
            .commit()

        lifecycleScope.launch {
            ticketId = DatabaseProvider.createMarathonTicket(this@MarathonActivity)
            val ticket = DatabaseProvider.getTicketById(this@MarathonActivity, ticketId)
            if (ticket == null) {
                throw IllegalStateException("Ticket with ID $ticketId not found")
            }
            val questions = questionDao.getQuestionsByIds(ticket.questions)
            val userAnswers = userAnswerDao.getAnswersByTicketId(ticketId).associate { it.questionId to it }

            val questionNumbers = (1..questions.size).toList()

            // Передаем ticketId в QuestionDisplayFragment
            questionDisplayFragment.setTicketId(ticketId)

            // Передаем ссылку на QuestionSelectorFragment в QuestionDisplayFragment
            questionDisplayFragment.setQuestionSelectorFragment(questionSelectorFragment)

            // Инициализация фрагментов и передача данных
            questionSelectorFragment.setQuestionNumbers(questionNumbers, userAnswers) { number ->
                val position = number - 1
                questionDisplayFragment.setCurrentQuestion(position)
                questionSelectorFragment.setCurrentQuestion(position)
            }

            val answeredQuestionsMap = userAnswers.mapKeys { ticket.questions.indexOf(it.key) }
            questionDisplayFragment.setQuestions(questions, answeredQuestionsMap, onAnswerSaved = { questionIndex, isCorrect, userAnswer ->
                lifecycleScope.launch {
                    val questionId = ticket.questions[questionIndex]
                    userAnswerDao.insert(UserAnswer(ticketId, questionId, isCorrect, userAnswer))
                    val updatedAnswers = userAnswerDao.getAnswersByTicketId(ticketId).associate { it.questionId to it }
                    questionSelectorFragment.updateAnswers(updatedAnswers)
                }
            }, onAllQuestionsAnswered = {
                questionDisplayFragment.showResults()
                lifecycleScope.launch {
                    DatabaseProvider.deleteMarathonTicket(this@MarathonActivity, ticketId)
                }
            })

            questionDisplayFragment.displayQuestion(questions[0])
        }
    }
}