package dev.drzepka.arduino.rc_car.controller.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import dev.drzepka.arduino.rc_car.controller.R
import kotlin.math.*

class Joystick(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var positionListener: PositionListener? = null

    private val horizontalLayout =
        context!!.theme.obtainStyledAttributes(attrs!!, R.styleable.Joystick, 0, 0).let {
            val value = it.getBoolean(R.styleable.Joystick_horizontalLayout, false)
            it.recycle()
            value
        }

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GRAY
    }

    private val knobPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private var knobPosition = PointF()
    private var knobRadius = 0f
    private var backgroundRadius = 0f

    init {
        isFocusableInTouchMode = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dimensions = if (horizontalLayout) heightMeasureSpec else widthMeasureSpec
        super.onMeasure(dimensions, dimensions)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetKnobPosition()

        knobRadius = width * KNOB_SIZE / 2f
        backgroundRadius = width / 2f - knobRadius
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawOval(
            left.toFloat() + knobRadius,
            top.toFloat() + knobRadius,
            right.toFloat() - knobRadius,
            bottom.toFloat() - knobRadius,
            backgroundPaint
        )

        canvas.translate(knobPosition.x, knobPosition.y)
        canvas.drawOval(
            -knobRadius,
            -knobRadius,
            knobRadius,
            knobRadius,
            knobPaint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val carthesianX = event.x - width / 2f
                val carthesianY = event.y - width / 2f

                val angle = atan2(carthesianY, carthesianX)
                var radius = hypot(carthesianX, carthesianY)

                radius = min(radius, backgroundRadius)

                knobPosition.x = radius * cos(angle) + width / 2f
                knobPosition.y = radius * sin(angle) + width / 2f

                invalidate()
                notifyListener(angle)
            }

            MotionEvent.ACTION_UP -> {
                resetKnobPosition()
                invalidate()
                notifyListener(0f)
            }
        }

        return true
    }

    private fun notifyListener(angle: Float) {
        if (positionListener == null)
            return

        val maxPossibleX = abs(backgroundRadius * cos(angle))
        val maxPossibleY = abs(backgroundRadius * sin(angle))

        val fractionX = min(1f, max(-1f, (knobPosition.x - width / 2f) / maxPossibleX))
        val fractionY = min(1f, max(-1f, (knobPosition.y - width / 2f) / maxPossibleY))

        positionListener?.onPositionChanged(fractionX, fractionY)
    }

    private fun resetKnobPosition() {
        knobPosition.x = (right - left) / 2f
        knobPosition.y = (bottom - top) / 2f
    }

    interface PositionListener {
        fun onPositionChanged(x: Float, y: Float)
    }

    companion object {
        private const val KNOB_SIZE = 0.22f
    }
}