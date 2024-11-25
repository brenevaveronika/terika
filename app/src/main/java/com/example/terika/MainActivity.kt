package com.example.terika

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
// import com.example.terika.habit_tracker.HabitGenerator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fs = Firebase.firestore
        val diaryFragment = DiaryFragment() // Создаем экземпляр DiaryFragment

        loadFragment(HomeFragment())
        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.menu.findItem(R.id.home).setChecked(true)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    loadFragment(HomeFragment())

                    true
                }
                R.id.diary -> {
                    loadFragment(DiaryFragment())
                    true
                }
                R.id.habits -> {
                    loadFragment(HabitTrackerFragment())
                    true
                }

                R.id.articles -> {
                    loadFragment(ArticlesFragment())
                    true
                }

                else -> {false}
            }
        }

    }
    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.commit()
    }
}
