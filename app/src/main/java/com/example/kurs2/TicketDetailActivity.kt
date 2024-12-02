package com.example.kurs2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class TicketDetailActivity : AppCompatActivity() {
    private lateinit var questionSelectorFragment: QuestionSelectorFragment
    private lateinit var questionDisplayFragment: QuestionDisplayFragment
    private lateinit var db: AppDatabase
    private lateinit var ticketDao: TicketDao
    private lateinit var questionDao: QuestionDao
    private lateinit var userAnswerDao: UserAnswerDao
    private var ticketId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_detail)

        ticketId = intent.getIntExtra("TICKET_ID", -1)
        if (ticketId == -1) {
            finish()
            return
        }

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

        // Передаем ticketId в QuestionDisplayFragment
        questionDisplayFragment.setTicketId(ticketId)

        lifecycleScope.launch {
            val ticket = ticketDao.getTicketById(ticketId)
            val questions = questionDao.getQuestionsByIds(ticket.questions)
            val userAnswers = userAnswerDao.getAnswersByTicketId(ticketId).associate { it.questionId to it }

            val questionNumbers = (1..questions.size).toList()

            questionSelectorFragment.setQuestionNumbers(questionNumbers) { number ->
                val position = number - 1
                questionDisplayFragment.setCurrentQuestion(position)
                questionSelectorFragment.setCurrentQuestion(position)
            }

            // Преобразуем userAnswers в карту с индексами, начинающимися с 0
            val answeredQuestionsMap = userAnswers.mapKeys { ticket.questions.indexOf(it.key) }
            questionSelectorFragment.setAnsweredQuestions(answeredQuestionsMap.mapValues { it.value.isCorrect })

            questionDisplayFragment.setQuestions(questions, answeredQuestionsMap, onAnswerSaved = { questionIndex, isCorrect, userAnswer ->
                lifecycleScope.launch {
                    val questionId = ticket.questions[questionIndex]
                    userAnswerDao.insert(UserAnswer(ticketId, questionId, isCorrect, userAnswer))
                    questionSelectorFragment.updateQuestionColor(number = questionIndex + 1, isCorrect = isCorrect)
                }
            }, onAllQuestionsAnswered = {
                questionDisplayFragment.showResults()
            })

            // Display the first question initially
            questionDisplayFragment.displayQuestion(questions[0])
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
                    val db = DatabaseProvider.getDatabase(this@TicketDetailActivity)
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