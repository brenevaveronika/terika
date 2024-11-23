package com.example.terika

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.adapter.AffirmationAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ArticlesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AffirmationAdapter
    private val affirmationsList = mutableListOf<Affirmation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_articles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchAffirmations() // Загружаем аффирмации
    }

    private fun fetchAffirmations() {
        val db = FirebaseFirestore.getInstance()
        db.collection("affirmations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    affirmationsList.clear() // Очищаем список перед добавлением новых данных
                    for (document in snapshot.documents) {
                        val affirmation = document.toObject(Affirmation::class.java)
                        affirmation?.let {
                            it.id = document.id // Получаем ID документа
                            affirmationsList.add(it) // Добавляем аффирмацию в список
                        }
                    }
                    // Устанавливаем адаптер с обновленным списком аффирмаций
                    adapter = AffirmationAdapter(affirmationsList)
                    recyclerView.adapter = adapter
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }
}
