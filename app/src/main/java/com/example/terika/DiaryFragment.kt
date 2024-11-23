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
        // Inflate the layout for this fragment
        firestore = FirebaseFirestore.getInstance()
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Отобразите дату в заголовке
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        dateTextView.text = selectedDate?.toString() // Или отформатируйте дату по желанию

        val sumOfDigits = selectedDate?.let { calculateDayNumber(it) }
        if (sumOfDigits != null) {
            fetchDescriptionForDay(sumOfDigits)
        } // Получите описание для дня

        // Здесь можно добавить код для отображения информации о дне
    }

    fun updateDiaryData(date: LocalDate) {
        selectedDate = date
        // Обновите UI с новой датой
        val dateTextView = view?.findViewById<TextView>(R.id.dateTextView)
        dateTextView?.text = selectedDate?.toString() // Или отформатируйте дату по желанию

        // Здесь можно добавить код для обновления информации о дне
    }

    private fun calculateDayNumber(date: LocalDate): Int {
        val day = date.dayOfMonth.toString().map { it.toString().toInt() }.sum()
        val month = date.monthValue.toString().map { it.toString().toInt() }.sum()
        val year = date.year.toString().map { it.toString().toInt() }.sum()
        var result = day + month + year
        if (result > 9) {
            result = result.toString().map { it.toString().toInt() }.sum()
        }
        return result
    }

    private fun fetchDescriptionForDay(day: Int) {
        firestore.collection("numbers")
            .document(day.toString()) // Используем сумму как идентификатор документа
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val description = document.getString("long") // Получаем текстовое поле "long"
                    updateUI(description) // Обновляем пользовательский интерфейс
                } else {
                    Log.d("DiaryFragment", "No such document")
                    updateUI("Описание не найдено") // Обработка случая, если документ не найден
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DiaryFragment", "Error getting document: ", exception)
                updateUI("Ошибка при получении описания") // Обработка ошибки
            }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUI(description: String?) {
        val textView: TextView = view?.findViewById(R.id.diaryContentTextView) ?: return
        textView.text = description ?: "Описание не найдено" // Обработка случая, если описание равно null
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
