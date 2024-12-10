package com.example.kurs2

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class QuestionDisplayFragment : Fragment() {
    private lateinit var questionText: TextView
    private lateinit var questionImage: ImageView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var hintButton: Button
    private lateinit var answerButton: Button
    private lateinit var debugInfo: TextView
    private var currentQuestionIndex = 0
    private lateinit var questions: List<Question>
    private var userAnswers: MutableMap<Int, UserAnswer> = mutableMapOf() // Используем UserAnswer для хранения ответов
    private var onAnswerSaved: ((Int, Boolean, String) -> Unit)? = null
    private var onAllQuestionsAnswered: (() -> Unit)? = null
    private var ticketId: Int = -1
    private var questionSelectorFragment: QuestionSelectorFragment? = null // Добавляем ссылку на QuestionSelectorFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question_display, container, false)
        questionText = view.findViewById(R.id.questionText)
        questionImage = view.findViewById(R.id.questionImage)
        optionsGroup = view.findViewById(R.id.optionsGroup)
        hintButton = view.findViewById(R.id.hintButton)
        answerButton = view.findViewById(R.id.answerButton)
        //debugInfo = view.findViewById(R.id.debugInfo)

        return view
    }

    fun setQuestionSelectorFragment(fragment: QuestionSelectorFragment) {
        this.questionSelectorFragment = fragment
    }

    fun setQuestions(questions: List<Question>, answeredQuestions: Map<Int, UserAnswer>, onAnswerSaved: (Int, Boolean, String) -> Unit, onAllQuestionsAnswered: () -> Unit) {
        this.questions = questions
        this.userAnswers.putAll(answeredQuestions.mapValues { it.value })
        this.onAnswerSaved = onAnswerSaved
        this.onAllQuestionsAnswered = onAllQuestionsAnswered
        displayQuestion(questions[currentQuestionIndex])
    }

    fun setCurrentQuestion(currentQuestionIndex: Int) {
        this.currentQuestionIndex = currentQuestionIndex
        displayQuestion(questions[currentQuestionIndex])
    }

    fun setTicketId(ticketId: Int) {
        this.ticketId = ticketId
    }

    @SuppressLint("DiscouragedApi")
    fun displayQuestion(question: Question) {
        questionImage.visibility = View.GONE
        questionText.text = question.question
        question.imageUrl?.let {
            questionImage.visibility = View.VISIBLE
            val resourceId = resources.getIdentifier(it, "drawable", requireContext().packageName)
            Glide.with(this).load(resourceId).into(questionImage)
        }
        optionsGroup.removeAllViews()
        //debugInfo.text = userAnswers.toString()
        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(requireContext()).apply {
                text = option
                id = index
                textSize = 14f // Increase font size
                isEnabled = userAnswers[currentQuestionIndex]?.userAnswer == null
                if (userAnswers[currentQuestionIndex] != null) { // если есть ответ
                    isChecked = option == userAnswers[currentQuestionIndex]?.userAnswer
                    setBackgroundColor(when {
                        option == question.correctAnswer -> Color.GREEN
                        option == userAnswers[currentQuestionIndex]?.userAnswer && option != question.correctAnswer -> Color.RED
                        else -> Color.TRANSPARENT
                    })
                }
                tag = index // Установим тег для поиска по индексу
            }
            optionsGroup.addView(radioButton)
        }

        hintButton.setOnClickListener {
            question.hint?.let { hint ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Подсказка")
                    .setMessage(hint)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        answerButton.isEnabled = userAnswers[currentQuestionIndex] == null

        answerButton.setOnClickListener {
            if (userAnswers[currentQuestionIndex] == null) {
                val selectedOptionId = optionsGroup.checkedRadioButtonId
                if (selectedOptionId != -1) {
                    val selectedOption = optionsGroup.findViewById<RadioButton>(selectedOptionId).text.toString()
                    val isCorrect = selectedOption == question.correctAnswer
                    userAnswers[currentQuestionIndex] = UserAnswer(ticketId, question.id, isCorrect, selectedOption)
                    onAnswerSaved?.invoke(currentQuestionIndex, isCorrect, selectedOption)
                    optionsGroup.findViewById<RadioButton>(selectedOptionId).setBackgroundColor(if (isCorrect) Color.GREEN else Color.RED)

                    // Блокируем возможность ответить заново только для текущего вопроса
                    optionsGroup.clearCheck()
                    optionsGroup.isEnabled = false
                    answerButton.isEnabled = false

                    if (!isCorrect) {
                        // Показываем правильный ответ зеленым цветом
                        val correctOptionIndex = question.options.indexOf(question.correctAnswer)
                        optionsGroup.findViewWithTag<RadioButton>("$correctOptionIndex")?.setBackgroundColor(Color.GREEN)

                        // Показываем подсказку при неправильном ответе
                        AlertDialog.Builder(requireContext())
                            .setTitle("Неправильный ответ")
                            .setMessage(question.hint ?: "Попробуйте еще раз")
                            .setPositiveButton("OK") { _, _ ->
                                // Переход к следующему вопросу
                                moveToNextQuestion()
                            }
                            .show()
                    } else {
                        // Переход к следующему вопросу
                        moveToNextQuestion()
                    }
                } else {
                    Toast.makeText(requireContext(), "Пожалуйста, выберите вариант ответа", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun moveToNextQuestion() {
        // Проверяем, есть ли следующий вопрос
        if (currentQuestionIndex < questions.size - 1) {
            // Проверяем, отвечен ли следующий вопрос
            if (userAnswers[currentQuestionIndex + 1] == null) {
                // Переходим к следующему вопросу
                currentQuestionIndex++
            } else {
                // Ищем первый неотвеченный вопрос
                val nextUnansweredQuestionIndex = questions.indexOfFirst { userAnswers[questions.indexOf(it)] == null }
                if (nextUnansweredQuestionIndex != -1) {
                    currentQuestionIndex = nextUnansweredQuestionIndex
                } else {
                    onAllQuestionsAnswered?.invoke()
                    return
                }
            }
        } else {
            // Ищем первый неотвеченный вопрос
            val nextUnansweredQuestionIndex = questions.indexOfFirst { userAnswers[questions.indexOf(it)] == null }
            if (nextUnansweredQuestionIndex != -1) {
                currentQuestionIndex = nextUnansweredQuestionIndex
            } else {
                onAllQuestionsAnswered?.invoke()
                return
            }
        }

        // Обновляем состояние верхнего фрагмента навигации
        questionSelectorFragment?.setCurrentQuestion(currentQuestionIndex)
        // Отображаем следующий вопрос
        displayQuestion(questions[currentQuestionIndex])
    }

    fun showResults() {
        val correctAnswers = userAnswers.entries.count { it.value.userAnswer == questions[it.key].correctAnswer }
        AlertDialog.Builder(requireContext())
            .setTitle("Результаты")
            .setMessage("Правильных ответов: $correctAnswers из ${questions.size}")
            .setPositiveButton("OK", null)
            .show()
    }
}