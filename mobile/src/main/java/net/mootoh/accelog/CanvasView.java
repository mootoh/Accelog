package net.mootoh.accelog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by takayama.motohiro on 7/6/15.
 */
public class CanvasView extends View {
    private static final String TAG = "CanvasView";
    private final Paint paint;
    private float x, y, z;
    private int W, H;
    float maxXYZ = 10.0f;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.paint = new Paint();
        this.paint.setColor(Color.RED);
        this.paint.setStyle(Paint.Style.FILL);
    }

    public void setXYZ(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float hh = getHeight() / 3.0f;

        // X
        this.paint.setColor(Color.RED);
        drawRect(canvas, this.x, 0);

        // Y
        this.paint.setColor(Color.GREEN);
        drawRect(canvas, this.y, hh);

        // Z
        this.paint.setColor(Color.BLUE);
        drawRect(canvas, this.z, hh*2);
    }

    void drawRect(Canvas canvas, float width, float y) {
        this.W = getWidth();
        this.H = getHeight();

        float hw = this.W / 2.0f;
        float hh = this.H / 3.0f;

        float xw = width / this.maxXYZ * hw;
        float left = hw;
        float right = hw + xw;
        if (xw < 0) {
            left = hw + xw;
            right = hw;
        }

        canvas.drawRect(left, y, right, y + hh, this.paint);
    }
}
