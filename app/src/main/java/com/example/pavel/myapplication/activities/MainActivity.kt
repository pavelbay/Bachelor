package com.example.pavel.myapplication.activities

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.pavel.myapplication.R
import com.pes.androidmaterialcolorpickerdialog.ColorPicker
import kotlinx.android.synthetic.main.activity_main.*

private const val DEFAULT_COLOR = Color.GREEN

class MainActivity : AppCompatActivity() {

    private var mColor = DEFAULT_COLOR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = mColor
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Join.ROUND
        paint.strokeCap = Cap.ROUND
        paint.strokeWidth = 12F

        mDrawingView.paint = paint
        val colorPicker = ColorPicker(this, Color.alpha(mColor), Color.red(mColor), Color.green(mColor), Color.blue(mColor))
        colorPicker.setCallback { color ->
            paint.color = color
            colorPicker.dismiss()
        }
        mFloatingActionButton.setOnClickListener { colorPicker.show() }
    }
}