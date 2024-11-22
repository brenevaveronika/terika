package com.example.terika

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.terika.ArticlesFragment
import com.example.terika.DiaryFragment
import com.example.terika.HabitTrackerFragment
import com.example.terika.HomeFragment
import com.example.terika.SettingFragment
import com.example.terika.adapter.HabitAdapter
import com.example.terika.aesthetic_cards.AestheticCard
import com.example.terika.habit_tracker.Habit
// import com.example.terika.habit_tracker.HabitGenerator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fs = Firebase.firestore

        loadFragment(HomeFragment())
        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.menu.findItem(R.id.home).setChecked(true)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.diary -> {
                    loadFragment(DiaryFragment())
                    true
                }
                R.id.habits -> {
                    loadFragment(HabitTrackerFragment())
                    true
                }
                R.id.home -> {
                    loadFragment(HomeFragment())

                    true
                }
                R.id.articles -> {
                    loadFragment(ArticlesFragment())
                    true
                }
                R.id.settings -> {
                    loadFragment(SettingFragment())
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
