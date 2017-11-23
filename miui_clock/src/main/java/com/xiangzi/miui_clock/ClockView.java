package com.xiangzi.miui_clock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/11/16.
 */

public class ClockView extends TileView {

    private Paint red = new Paint();
    private final static int MAXALPHA =  255;
    private final static int MINALPHA =  100;
    private final static int SALPHA =  150;
    private final static int MALPHA =  200;

    private Paint centerPaint,centerPaint2,secondPaint,minutePaint,hourPaint,numberPaint,linePaint,backGroupPaint;
    private float mWidth,mHeight;
    private float circleRadius ;
    private float circleX,circleY;
    private double hour,second,minute;
    private int angle = -1;
    private float centerStrokeWidth,secondStrokeWidth,minuteStrokeWidth,hourStrokeWidth,numberStrokeWidth,lineStrokeWidth;

    private int colorPaint = Color.WHITE;
    private int colorBG = getResources().getColor(R.color.colorPrimaryDark);
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==0){
                invalidate();
            }
        }
    };

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    private void initPaint(){

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(colorPaint);
        centerPaint.setAlpha(MAXALPHA);
        centerPaint.setStyle(Paint.Style.STROKE);
        centerPaint.setStrokeWidth(hourStrokeWidth);

        centerPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint2.setColor(colorPaint);
        centerPaint2.setStrokeWidth(lineStrokeWidth);
        centerPaint2.setStyle(Paint.Style.STROKE);

        //秒
        secondPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondPaint.setColor(colorPaint);
        secondPaint.setAlpha(MALPHA);
        secondPaint.setStrokeWidth(secondStrokeWidth);

        //分
        minutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minutePaint.setColor(colorPaint);
        minutePaint.setStrokeWidth(minuteStrokeWidth);

        //时
        hourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourPaint.setColor(colorPaint);
        hourPaint.setAlpha(SALPHA);
        hourPaint.setStrokeWidth(hourStrokeWidth);

        //数字
        numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setColor(colorPaint);
        numberPaint.setAlpha(100);
        numberPaint.setStrokeWidth(numberStrokeWidth);


        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(colorPaint);
        linePaint.setAlpha(MINALPHA);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(lineStrokeWidth);

        backGroupPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backGroupPaint.setColor(colorBG);
        backGroupPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        if(mWidth<mHeight){
            //圆的半径为view的宽度的一半再减9，防止贴边
            lineStrokeWidth = mWidth/700<0.1f? 0.1f :mWidth/700;
            circleRadius = mWidth/2-20;
            circleX = mWidth/2;
            circleY = mHeight/2;

        } else{
            lineStrokeWidth = mWidth/700<0.1?  0.1f :mWidth/700;
            circleRadius = mHeight/2-20;
            circleX = mWidth/2;
            circleY = mHeight/2;
        }
        numberStrokeWidth = lineStrokeWidth;
        secondStrokeWidth = numberStrokeWidth*2;
        minuteStrokeWidth = secondStrokeWidth*3;
        hourStrokeWidth = secondStrokeWidth*4;
        centerStrokeWidth = minuteStrokeWidth*2;
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPaint(backGroupPaint);
        setTimes();
        drawCirclePoint(canvas);
        drawPointer(canvas);
    }

    private void drawCirclePoint(Canvas canvas){
        canvas.drawCircle(circleX,circleY,circleRadius*0.05f,centerPaint);
        canvas.drawCircle(circleX,circleY,circleRadius*0.06f,centerPaint2);
        canvas.drawCircle(circleX,circleY,circleRadius,linePaint);

        PointF startPoint2 = new PointF(circleX, circleY-circleRadius*0.7f);
        PointF endPoint2 = new PointF(circleX, circleY-circleRadius*0.85f);
        for(int i=0;i<360;i++){
            Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(colorPaint);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(lineStrokeWidth*2);
            if(angle == -1){
                linePaint.setAlpha(MINALPHA);
            }else {
                if(0<(angle -i)&&(angle -i)<90){
                    float d = 1-(angle -i)/90f;
                    linePaint.setAlpha((int) (MINALPHA+(MAXALPHA-MINALPHA)*d));
                }else {
                    if(angle<90&&i>=270){
                        float d = (angle - (i-360) )/90f;
                        linePaint.setAlpha((int) (MINALPHA+(MAXALPHA-MINALPHA)*(1-(d>1?1:d))));
                    }else {
                        linePaint.setAlpha(MINALPHA);
                    }

                }

            }

            //画分针刻度
//
            canvas.drawLine(startPoint2.x,startPoint2.y,endPoint2.x,endPoint2.y,linePaint);
            canvas.rotate(1,circleX,circleY);
        }


        numberPaint.setTextSize(circleRadius/10);
        canvas.drawCircle(circleX,circleY-circleRadius,numberPaint.getTextSize(),backGroupPaint);
        canvas.drawText("12",circleX-numberPaint.getTextSize()/2,circleY-circleRadius+numberPaint.getTextSize()/4,numberPaint);

        canvas.drawCircle(circleX-circleRadius,circleY,numberPaint.getTextSize(),backGroupPaint);
        canvas.drawText("9",circleX-circleRadius-numberPaint.getTextSize()/4,circleY+numberPaint.getTextSize()/4,numberPaint);

        canvas.drawCircle(circleX,circleY+circleRadius,numberPaint.getTextSize(),backGroupPaint);
        canvas.drawText("6",circleX-numberPaint.getTextSize()/4,circleY+circleRadius+numberPaint.getTextSize()/4,numberPaint);

        canvas.drawCircle(circleX+circleRadius,circleY,numberPaint.getTextSize(),backGroupPaint);
        canvas.drawText("3",circleX+circleRadius-numberPaint.getTextSize()/4,circleY+numberPaint.getTextSize()/4,numberPaint);
    }


    private void drawPointer(Canvas canvas){
        canvas.translate(circleX,circleY);
        float hourAngle = (float) Math.toRadians(hour*30+270);
        float fHourX = (float) Math.cos(hourAngle)*circleRadius;
        float fHourY = (float) Math.sin(hourAngle)*circleRadius;
        float hourStartX = fHourX * 0.5f;
        float hourStartY = fHourY *0.5f;
        double hSR1 =Math.sqrt(hourStartX*hourStartX+hourStartY*hourStartY);
        float hourA1 = (float) (hourAngle+((hourPaint.getStrokeWidth()/3)/hSR1));
        float hourA1_ = (float) (hourAngle-((hourPaint.getStrokeWidth()/3)/hSR1));
        float hourR1 = (float) Math.abs(hSR1/Math.cos((hourPaint.getStrokeWidth()/3)/hSR1));
        float hourX1 = (float) (Math.cos(hourA1)*hourR1);
        float hourY1 = (float) (Math.sin(hourA1)*hourR1);
        float hourX1_ = (float) (Math.cos(hourA1_)*hourR1);
        float hourY1_ = (float) (Math.sin(hourA1_)*hourR1);
        float hourEndX = fHourX*0.06f;
        float hourEndY = fHourY*0.06f;
        double hSR2 = Math.sqrt(hourEndX*hourEndX+hourEndY*hourEndY);
        float hourA2 = (float) (hourAngle+(hourPaint.getStrokeWidth()/2)/hSR2);
        float hourA2_ = (float) (hourAngle-(hourPaint.getStrokeWidth()/2)/hSR2);
        float hourR2 = (float) Math.abs(hSR2/Math.cos((hourPaint.getStrokeWidth()/4)/hSR2));
        float hourX2 = (float) (Math.cos(hourA2)*hourR2);
        float hourY2 = (float) (Math.sin(hourA2)*hourR2);
        float hourX2_ = (float) (Math.cos(hourA2_)*hourR2);
        float hourY2_ = (float) (Math.sin(hourA2_)*hourR2);


        float minuteAngle = (float) Math.toRadians((minute*6+270));
        float fMinuteX = (float) Math.cos(minuteAngle)*circleRadius;
        float fMinuteY = (float) Math.sin(minuteAngle)*circleRadius;
        float minuteStartX = fMinuteX*0.6f;
        float minuteStartY = fMinuteY*0.6f;
        float minuteA1 = (float) (minuteAngle+((minutePaint.getStrokeWidth()/3)/Math.sqrt(minuteStartX*minuteStartX+minuteStartY*minuteStartY)));
        float minuteA1_ = (float) (minuteAngle-((minutePaint.getStrokeWidth()/3)/Math.sqrt(minuteStartX*minuteStartX+minuteStartY*minuteStartY)));
        float minuteR1 = (float)  Math.abs(Math.sqrt(minuteStartX*minuteStartX+minuteStartY*minuteStartY)/Math.cos((minutePaint.getStrokeWidth()/2)/Math.sqrt(minuteStartX*minuteStartX+minuteStartY*minuteStartY)));
        float minuteX1 = (float) (Math.cos(minuteA1)*minuteR1);
        float minuteY1 = (float) (Math.sin(minuteA1)*minuteR1);
        float minuteX1_ = (float) (Math.cos(minuteA1_)*minuteR1);
        float minuteY1_ = (float) (Math.sin(minuteA1_)*minuteR1);
        float minuteEndX = fMinuteX*0.06f;
        float minuteEndY = fMinuteY*0.06f;
        float minuteA2 = (float) (minuteAngle+((minutePaint.getStrokeWidth()/2)/Math.sqrt(minuteEndX*minuteEndX+minuteEndY*minuteEndY)));
        float minuteA2_ = (float) (minuteAngle-((minutePaint.getStrokeWidth()/2)/Math.sqrt(minuteEndX*minuteEndX+minuteEndY*minuteEndY)));
        float minuteR2 = (float) Math.abs(Math.sqrt(minuteEndX*minuteEndX+minuteEndY*minuteEndY)/Math.cos((minutePaint.getStrokeWidth()/2)/Math.sqrt(minuteEndX*minuteEndX+minuteEndY*minuteEndY)));
        float minuteX2 = (float) (Math.cos(minuteA2)*minuteR2);
        float minuteY2 = (float) (Math.sin(minuteA2)*minuteR2);
        float minuteX2_ = (float) (Math.cos(minuteA2_)*minuteR2);
        float minuteY2_ = (float) (Math.sin(minuteA2_)*minuteR2);


        float secondStartX = (float) Math.cos(Math.toRadians(second*6+270))*circleRadius*0.68f;
        float secondStartY = (float) Math.sin(Math.toRadians(second*6+270))*circleRadius*0.68f;
        float anglea = (float) Math.atan(Math.tan(Math.PI/7)/6);
        float c = (float) ((circleRadius*0.6)*(1/Math.cos(anglea)));
        float secondX1 = (float) Math.cos(Math.toRadians(second*6+270)+anglea)*c;
        float secondY1 = (float) Math.sin(Math.toRadians(second*6+270)+anglea)*c;
        float secondX2 = (float) Math.cos(Math.toRadians(second*6+270)-anglea)*c;
        float secondY2 = (float) Math.sin(Math.toRadians(second*6+270)-anglea)*c;
        Path hourPath = new Path();
        hourPath.moveTo(hourX2,hourY2);
        hourPath.lineTo(hourX1,hourY1);
        hourPath.quadTo(fHourX*0.52f,fHourY*0.52f,hourX1_,hourY1_);
        hourPath.lineTo(hourX2_,hourY2_);
        hourPath.close();
        canvas.drawPath(hourPath,hourPaint);
        Path minutePath = new Path();
        minutePath.moveTo(minuteX2,minuteY2);
        minutePath.lineTo(minuteX1,minuteY1);
        minutePath.quadTo(fMinuteX*0.62f,fMinuteY*0.62f,minuteX1_,minuteY1_);
        minutePath.lineTo(minuteX2_,minuteY2_);
        minutePath.close();
        canvas.drawPath(minutePath,minutePaint);
        Path secondPath = new Path();
        secondPath.moveTo(secondStartX,secondStartY);
        secondPath.lineTo(secondX1,secondY1);
        secondPath.lineTo(secondX2,secondY2);
        secondPath.close();
        canvas.drawPath(secondPath,secondPaint);

        handler.sendEmptyMessageDelayed(0,10);
    }


    private void setTimes(){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        double m = getTimes(date,Calendar.MILLISECOND);
        second = getTimes(date,Calendar.SECOND) + m/1000;
        angle = (int) (second*6);
        minute = getTimes(date,Calendar.MINUTE) +second/60;
        hour = getTimes(date,Calendar.HOUR)+minute/60;

    }

    private int getTimes(Date date,int calendarField){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(calendarField);
    }

    public void stopClock(){
        handler.removeMessages(0);
    }

    public void setColorPaint(int colorPaint) {
        this.colorPaint = colorPaint;
    }

    public void setColorBG(int colorBG) {
        this.colorBG = colorBG;
    }
}