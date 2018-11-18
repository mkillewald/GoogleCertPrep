package com.gameaholix.customfancontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class DialView extends View {
    // The SELECTION_COUNT defines the total number of selections for this custom view. The code is
    // designed so that you can change this value to create a control with more or fewer selections.
    private static int SELECTION_COUNT = 4; // Total number of selections.
    private static final int LABEL_DISTANCE = 20; // Distance of labels relative to the dial
    private static final int INDICATOR_DISTANCE = 35; // Distance of indicator mark relative to dial
    private static final int INDICATOR_RADIUS = 20; // Radius of indicator mark

    private float mWidth;                   // Custom view width.
    private float mHeight;                  // Custom view height.
    private Paint mTextPaint;               // For text in the view.
    private Paint mDialPaint;               // For dial circle in the view.
    private int mFanOnColor;              // Color of dial indicator when fan is on.
    private int mFanOffColor;             // Color of dial indicator when fan is off.
    private float mRadius;                  // Radius of the circle.
    private int mActiveSelection;           // The active selection.

    // The mTempLabel and mTempResult member variables provide temporary storage for the result of
    // calculations, and are used to reduce the memory allocations while drawing.
    //
    // String buffer for dial labels and float for ComputeXY result.
    private final StringBuffer mTempLabel = new StringBuffer(8);
    private final float[] mTempResult = new float[2];

    public DialView(Context context) {
        super(context);
        init(context, null);
    }

    public DialView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DialView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // The onSizeChanged() method is called when the layout is inflated and when the view has
        // changed. Its parameters are the current width and height of the view, and the
        // "old" (previous) width and height.

        // Calculate the radius from the width and height
        mWidth = w;
        mHeight = h;
        mRadius = (float) (Math.min(mWidth, mHeight) / 2 * 0.8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the dial
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, mDialPaint);

        // Draw the text labels
        final float labelRadius = mRadius + LABEL_DISTANCE;
        StringBuffer label = mTempLabel;
        for (int i = 0; i < SELECTION_COUNT; i++) {
            float[] xyData = computeXYForPosition(i, labelRadius);
            float x = xyData[0];
            float y = xyData[1];
            label.setLength(0);
            label.append(i);
            canvas.drawText(label, 0, label.length(), x, y, mTextPaint);
        }

        // Draw the indicator mark
        final float markerRadius = mRadius - INDICATOR_DISTANCE;
        float[] xyData = computeXYForPosition(mActiveSelection, markerRadius);
        float x = xyData[0];
        float y = xyData[1];
        canvas.drawCircle(x, y, INDICATOR_RADIUS, mTextPaint);
    }

    private void init(Context context, AttributeSet attrs) {
        // Set default fan on and off colors
        mFanOnColor = Color.CYAN;
        mFanOffColor = Color.GRAY;

        // Get the custom attributes fanOnColor and fanOffColor if available
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.DialView, 0,0);

            // Set the fan on and off colors from the attribute values
            mFanOnColor = typedArray.getColor(R.styleable.DialView_fanOnColor, mFanOnColor);
            mFanOffColor = typedArray.getColor(R.styleable.DialView_fanOffColor, mFanOffColor);
            typedArray.recycle();
        }

        // Paint styles for rendering the custom view are created in the init() method rather than
        // at render-time with onDraw(). This is to improve performance, because onDraw() is called
        // frequently.
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(40f);

        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDialPaint.setColor(mFanOffColor);

        // Initialize current selection
        mActiveSelection = 0;

        // Setup onClickListener for this view
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Rotate selection to the next valid choice
                mActiveSelection = (mActiveSelection + 1) % SELECTION_COUNT;

                // Set dial background color to green if the selection is >= 1
                if (mActiveSelection >= 1) {
                    mDialPaint.setColor(mFanOnColor);
                } else {
                    mDialPaint.setColor(mFanOffColor);
                }

                // Redraw the view
                invalidate();
            }
        });
    }

    private float[] computeXYForPosition(final int pos, final float radius) {
        float[] result = mTempResult;
        Double startAngle = Math.PI * (9 / 8d);  // Angles are in radians
        Double angle = startAngle + (pos * (Math.PI / 4));
        result[0] = (float) (radius * Math.cos(angle)) + (mWidth / 2);
        result[1] = (float) (radius * Math.sin(angle)) + (mHeight / 2);
        return result;
    }
}
