package com.example.kurs2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class QuestionSelectorFragment : Fragment() {
    private lateinit var recyclerViewQuestions: RecyclerView
    private lateinit var questionNumberAdapter: QuestionNumberAdapter
    private var onQuestionClick: ((Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question_selector, container, false)
        recyclerViewQuestions = view.findViewById(R.id.recyclerViewQuestions)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewQuestions.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    fun setQuestionNumbers(questionNumbers: List<Int>, onQuestionClick: (Int) -> Unit) {
        this.onQuestionClick = onQuestionClick
        questionNumberAdapter = QuestionNumberAdapter(questionNumbers, onQuestionClick)
        recyclerViewQuestions.adapter = questionNumberAdapter
    }

    fun updateQuestionColor(number: Int, isCorrect: Boolean) {
        questionNumberAdapter.updateQuestionColor(number, isCorrect)
    }

    fun setAnsweredQuestions(answeredQuestions: Map<Int, Boolean>) {
        questionNumberAdapter.setAnsweredQuestions(answeredQuestions)
    }

    fun setCurrentQuestion(currentQuestionIndex: Int) {
        questionNumberAdapter.setCurrentQuestion(currentQuestionIndex)
    }
}