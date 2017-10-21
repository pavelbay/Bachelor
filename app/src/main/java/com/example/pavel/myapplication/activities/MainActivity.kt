package com.example.pavel.myapplication.activities

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.pavel.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = Color.GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Join.ROUND
        paint.strokeCap = Cap.ROUND
        paint.strokeWidth = 12F

        mDrawingView.paint = paint
    }
}