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

    fun setQuestionNumbers(questionNumbers: List<Int>, userAnswers: Map<Int, UserAnswer>, onQuestionClick: (Int) -> Unit) {
        this.onQuestionClick = onQuestionClick
        questionNumberAdapter = QuestionNumberAdapter(questionNumbers, userAnswers, onQuestionClick)
        recyclerViewQuestions.adapter = questionNumberAdapter
    }

    fun setCurrentQuestion(currentQuestionIndex: Int) {
        questionNumberAdapter.setCurrentQuestion(currentQuestionIndex)
    }

    fun updateAnswers(userAnswers: Map<Int, UserAnswer>) {
        questionNumberAdapter.updateAnswers(userAnswers)
    }
}