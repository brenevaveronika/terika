package com.example.terika

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiaryFragment : Fragment() {

    private var selectedDate: LocalDate? = LocalDate.now()
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDate = it.getSerializable(ARG_DATE) as? LocalDate
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        dateTextView.text = selectedDate?.format(formatter)

        val sumOfDigits = selectedDate?.let { calculateDayNumber(it) }
        if (sumOfDigits != null) {
            fetchDescriptionForDay(sumOfDigits)
        }
        val numTextView = view.findViewById<TextView>(R.id.numText)
        numTextView.text = "Число дня: $sumOfDigits"
    }

    fun updateDiaryData(date: LocalDate) {
        selectedDate = date
        //обновляем UI
        val dateTextView = view?.findViewById<TextView>(R.id.dateTextView)
        dateTextView?.text = selectedDate?.toString()
    }

    private fun calculateDayNumber(date: LocalDate): Int {
        val day = date.dayOfMonth.toString().map { it.toString().toInt() }.sum()
        val month = date.monthValue.toString().map { it.toString().toInt() }.sum()
        val year = date.year.toString().map { it.toString().toInt() }.sum()
        var result = day + month + year
        if (result > 9) {
            result = result.toString().map { it.toString().toInt() }.sum()
        }
        if (result > 9) {
            result = result.toString().map { it.toString().toInt() }.sum()
        }
        return result
    }

    private fun fetchDescriptionForDay(day: Int) {
        firestore.collection("numbers")
            .document(day.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val description = document.getString("long")
                    updateUI(description)
                } else {
                    Log.d("DiaryFragment", "No such document")
                    updateUI("Описание не найдено")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DiaryFragment", "Error getting document: ", exception)
                updateUI("Ошибка при получении описания")
            }
    }

    private fun updateUI(description: String?) {
        val textView: TextView = view?.findViewById(R.id.diaryContentTextView) ?: return
        textView.text = description ?: "Описание не найдено"
    }

    companion object {
        private const val ARG_DATE = "selected_date"

        @JvmStatic
        fun newInstance(date: LocalDate) =
            DiaryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATE, date)
                }
            }
    }
}
