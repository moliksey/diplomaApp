package com.example.diplomapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val takePhotoButton: ImageButton= findViewById(R.id.main_page_take_photo_button)
        val processingModSwitch: Switch= findViewById(R.id.processing_mod_switch)
        val imageProcType: ImageView=findViewById(R.id.image_proc_type)
        takePhotoButton.setOnClickListener{
            Toast.makeText(this, "Button", Toast.LENGTH_LONG).show()
        }
        processingModSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                imageProcType.setImageResource(R.drawable.wifi_on_pic)
                Toast.makeText(applicationContext, "Switch On", Toast.LENGTH_SHORT).show()
            } else {
                imageProcType.setImageResource(R.drawable.wifi_off_pic)
                Toast.makeText(applicationContext, "Switch Off", Toast.LENGTH_SHORT).show()
            }
        }

    }
}