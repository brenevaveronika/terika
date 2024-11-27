package com.example.terika.diary

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.adapter.DiaryAdapter
import com.google.firebase.firestore.FirebaseFirestore

class SwipeToDeleteCallback(
    private val adapter: DiaryAdapter,
    private val db: FirebaseFirestore
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // чтоб не двигалось
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val entryToDelete = adapter.diaryEntries[position]

        // удаляем из БД
        db.collection("diary").document(entryToDelete.id)
            .delete()
            .addOnSuccessListener {
                // удаляем и обновляем адаптер
                adapter.diaryEntries.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
            .addOnFailureListener { e ->
                Log.w("SwipeToDelete", "Error deleting document", e)
            }
    }
}
