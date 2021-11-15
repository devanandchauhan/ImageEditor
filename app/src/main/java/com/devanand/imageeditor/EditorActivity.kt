package com.devanand.imageeditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_editor.*

class EditorActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        var bundle = intent.extras
        if(bundle != null){
            Log.e("Editor Activity","Found Bundle in Editor")
        }

    }
}