package com.pavel.augmented.customviews

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.pavel.augmented.R
import android.graphics.Bitmap



private const val DEFAULT_STROKE_WIDTH = 12F
private const val ERASER_STROKE_WIDTH = 50F

class DrawingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var mWidth: Int = 0
    var mHeight: Int = 0
    var paint: Paint = Paint()
    var bitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mPath: Path = Path()
    private val mBitmapPaint: Paint = Paint(Paint.DITHER_FLAG)
    private val circlePaint: Paint = Paint()
    private val circlePath: Path = Path()

    var pictureAvailable = false

    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()

    init {
        circlePaint.isAntiAlias = true
        circlePaint.color = Color.BLUE
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeJoin = Paint.Join.MITER
        circlePaint.strokeWidth = 4f

        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = DEFAULT_STROKE_WIDTH
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        bitmap = if (bitmap == null) {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        } else {
            val temporary = Bitmap.createScaledBitmap(bitmap, w, h, true)
            temporary
        }

        mCanvas = Canvas(bitmap)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        Log.d(TAG, "OnMeasure")
    }

    fun updateBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        this.bitmap = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, true)

        mCanvas = Canvas(this.bitmap)
        pictureAvailable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(bitmap, 0F, 0F, mBitmapPaint)
        canvas.drawPath(mPath, paint)
        canvas.drawPath(circlePath, circlePaint)
    }

    //fun clear() = bitmap?.eraseColor(ContextCompat.getColor(context, R.color.white))

    fun setColor(color: Int) {
        paint.color = color
    }

    fun getColor(): Int = paint.color

//    fun enableEraser() {
//        paint.color = ContextCompat.getColor(context, R.color.white)
//        paint.strokeWidth = ERASER_STROKE_WIDTH
//    }

    private fun touchStart(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y

            circlePath.reset()
            circlePath.addCircle(mX, mY, 30F, Path.Direction.CW)
        }
    }

    private fun touchUp() {
        mPath.lineTo(mX, mY)
        circlePath.reset()
        // commit the path to our offscreen
        mCanvas?.drawPath(mPath, paint)
        // kill this so we don't double draw
        mPath.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = super.onSaveInstanceState()
        val savedState = SavedState(bundle)
        savedState.mBitmap = this.bitmap
        savedState.isPictureAvailable = this.pictureAvailable
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            this.bitmap = state.mBitmap
            this.pictureAvailable = state.isPictureAvailable
            Log.d("Deb", "Bitmap restored")
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    companion object {
        private val TOUCH_TOLERANCE = 4f
        private val TAG = DrawingView::class.java.simpleName
    }

    internal class SavedState : BaseSavedState {
        var mBitmap: Bitmap? = null
        var isPictureAvailable: Boolean = false

        constructor(bundle: Parcelable) : super(bundle)

        private constructor(parcel: Parcel) : super(parcel) {
            mBitmap = parcel.readParcelable(Parcelable::class.java.classLoader)
            isPictureAvailable = parcel.readInt() == 1
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeParcelable(mBitmap, flags)
            val available = if (isPictureAvailable) 1 else 0
            dest.writeInt(available)
        }
    }
}