package com.example.diplomapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DiseaseView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_disease_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val backButton: ImageButton = findViewById(R.id.back_button)
        this.intent.getIntExtra("id", -1)
        val plantNameTxt = this.intent.getStringExtra("plantName")
        val diseaseNameTxt =this.intent.getStringExtra("diseaseName")
        val careTipsTxt =this.intent.getStringExtra("careTips")
        val photoId =this.intent.getIntExtra("photo", 0)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val imageOfDisease: ImageView = findViewById(R.id.disease_img)
        val plantName: TextView = findViewById(R.id.plant_name)
        val diseaseName: TextView  = findViewById(R.id.disease_name)
        val careText: TextView  = findViewById(R.id.care_info)

        imageOfDisease.setImageResource(photoId)
        plantName.setText(plantNameTxt)
        diseaseName.setText(diseaseNameTxt)
        careText.setText(careTipsTxt)
    }
}