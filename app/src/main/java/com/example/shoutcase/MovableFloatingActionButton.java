package com.example.shoutcase;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MovableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private static final float CLICK_DRAG_TOLERANCE = 10;
    private float downRawX, downRawY;
    private float dX, dY;
    private String buttonText = "!!!"; // Variable to hold button text
    private Paint textPaint;

    private int textColor, textSize;
    private String text;
    public static final String TAG = "MovableFloatingActionButton";

    public MovableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MovableFloatingActionButton(Context context, int color, int textSize) {
        super(context);
        init(color, textSize);
    }

    private void init(int color, int textSize) {

        textColor = color;
        this.textSize = textSize;

        setOnTouchListener(this);

        textPaint = new Paint();
        textPaint.setColor(color); // Text color
        textPaint.setTextSize(textSize); // Adjust text size as needed
        textPaint.setTextAlign(Paint.Align.CENTER); // Center align the text
    }

    private void init() {
        setOnTouchListener(this);

        textPaint = new Paint();
        textPaint.setColor(textColor); // Text color
        textPaint.setTextSize(textSize); // Adjust text size as needed
        textPaint.setTextAlign(Paint.Align.CENTER); // Center align the text
    }

    public void setButtonText(String text){
        this.buttonText = text;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the text on the button
        if (!buttonText.isEmpty()) {
            float x = (float) getWidth() / 2; // Center x position
            float y = (float) getHeight() / 2 - ((textPaint.descent() + textPaint.ascent()) / 2); // Center y position
            canvas.drawText(buttonText, x, y, textPaint);
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int action = motionEvent.getAction();

//        Log.d(TAG, "onTouch: Touch triggered");
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downRawX = motionEvent.getRawX();
                downRawY = motionEvent.getRawY();
                // Calculate the difference between the button's position and the touch position
                dX = view.getX() - downRawX;
                dY = view.getY() - downRawY;
                return true;

            case MotionEvent.ACTION_MOVE:
                // Calculate the new position
                float newX = motionEvent.getRawX() + dX;
                float newY = motionEvent.getRawY() + dY;

                // Ensure the button doesn't go out of bounds
                if (view.getParent() instanceof View) {
                    View parentView = (View) view.getParent();
                    int parentWidth = parentView.getWidth();
                    int parentHeight = parentView.getHeight();

                    newX = Math.max(0, Math.min(newX, parentWidth - view.getWidth()));
                    newY = Math.max(0, Math.min(newY, parentHeight - view.getHeight()));
                }

                view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start();

                return true;

            case MotionEvent.ACTION_UP:
                float upRawX = motionEvent.getRawX();
                float upRawY = motionEvent.getRawY();
                float upDX = upRawX - downRawX;
                float upDY = upRawY - downRawY;

                // Check if it's a click
                if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) {
                    return performClick();
                } else {
                    return true;
                }

            default:
                return super.onTouchEvent(motionEvent);
        }
    }

}
