/** TranslucentPanel.java */
package com.gvsu.socnet.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/****************************************************************
 * com.gvsu.socnet.views.TranslucentPanel
 * @author Caleb Gomer
 * @version 1.0
 ***************************************************************/
public class TranslucentPanel extends LinearLayout {
  private Paint backgroundPaint, borderPaint;

  /****************************************************************
   * @param context
   ***************************************************************/
  public TranslucentPanel(Context context) {
    super(context);
  }

  /****************************************************************
   * @param context
   * @param attributes
   ***************************************************************/
  public TranslucentPanel(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  @Override
    protected void dispatchDraw(Canvas canvas) {
    backgroundPaint = new Paint();
    backgroundPaint.setARGB(225, 75, 75, 75);
    borderPaint = new Paint();
    borderPaint.setARGB(255, 10, 10, 10);
    borderPaint.setAntiAlias(true);
    borderPaint.setStyle(Style.STROKE);
    borderPaint.setStrokeWidth(2);

    RectF drawRectangle = new RectF();
    drawRectangle.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    canvas.drawRoundRect(drawRectangle, 5, 5, backgroundPaint);
    canvas.drawRoundRect(drawRectangle, 5, 5, borderPaint);

    super.dispatchDraw(canvas);
  }

}
