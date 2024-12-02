package com.example.kurs2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TicketViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val ticketDao = db.ticketDao()
    private val questionDao = db.questionDao()
    private val userAnswerDao = db.userAnswerDao()

    private val _questions = MutableLiveData<List<Question>>()
    val questions: LiveData<List<Question>> get() = _questions

    private val _userAnswers = MutableLiveData<MutableMap<Int, UserAnswer>>()
    val userAnswers: LiveData<MutableMap<Int, UserAnswer>> get() = _userAnswers

    private val _currentQuestionIndex = MutableLiveData<Int>()
    val currentQuestionIndex: LiveData<Int> get() = _currentQuestionIndex

    fun loadTicket(ticketId: Int) {
        viewModelScope.launch {
            val ticket = ticketDao.getTicketById(ticketId)
            val questions = questionDao.getQuestionsByIds(ticket.questions)
            val userAnswers = userAnswerDao.getAnswersByTicketId(ticketId).associate { it.questionId to it }

            // Преобразуем userAnswers в карту с индексами, начинающимися с 0
            val answeredQuestionsMap = mutableMapOf<Int, UserAnswer>()
            for (i in 0 until questions.size) {
                answeredQuestionsMap[i] = userAnswers[ticket.questions[i]] ?: UserAnswer(ticketId, ticket.questions[i], false, "")
            }

            _questions.value = questions
            _userAnswers.value = answeredQuestionsMap
            _currentQuestionIndex.value = 0
        }
    }

    fun updateAnswer(questionIndex: Int, isCorrect: Boolean, userAnswer: String) {
        val currentAnswers = _userAnswers.value ?: mutableMapOf()
        currentAnswers[questionIndex] = UserAnswer(currentAnswers[questionIndex]?.ticketId ?: 0, currentAnswers[questionIndex]?.questionId ?: 0, isCorrect, userAnswer)
        _userAnswers.value = currentAnswers
    }

    fun setCurrentQuestion(questionIndex: Int) {
        _currentQuestionIndex.value = questionIndex
    }
}