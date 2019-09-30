package com.example.yeticompanionv1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float centerX,centerY,baseRadius,sliderRadius;
    private JoystickListener joystickCallback;

    private void setupDimensions(){
        centerX = getWidth() / 2 ;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(),getHeight()) / 3;
        sliderRadius = Math.min(getWidth(),getHeight()) / 6;
    }

    private void drawJoyStick(float newX, float newY){

        if(getHolder().getSurface().isValid()){
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            colors.setARGB(255, 50, 50, 50);
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
            colors.setARGB(255, 255, 255, 255);
            myCanvas.drawCircle(newX, newY, sliderRadius, colors);
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        setupDimensions();
        drawJoyStick(centerX,centerY);
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    public JoystickView (Context c){
        super(c);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    public JoystickView (Context c, AttributeSet a){
        super(c,a);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }

    public JoystickView (Context c, AttributeSet a, int style){
        super(c,a,style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
    }


    public void setJoystickListener(JoystickListener listener) {
        joystickCallback = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent e){ //do the math here
        if(v.equals(this)){
            if(e.getAction() != MotionEvent.ACTION_UP) {
                float x = e.getX();
                float y = e.getY();
                float distance = (float) Math.sqrt(Math.pow(x-centerX,2) + Math.pow(y-centerY,2));
                if(distance > baseRadius)
                {
                    x = centerX + ((x-centerX) * (baseRadius/distance));
                    y = centerY + ((y-centerY) * (baseRadius/distance));
                }
                drawJoyStick(x, y);
                updateJoyStick(x-centerX, centerY-y);//inverted here for value reasons only
            }
            else {
                drawJoyStick(centerX,centerY);
                updateJoyStick(0,0);
            }
        }
        return true;
    }


    private void updateJoyStick(float newX,float newY){
        if(joystickCallback != null)
            joystickCallback.onJoystickMoved(newX/baseRadius,newY/baseRadius);
    }

    public interface JoystickListener{
        void onJoystickMoved(float xPercent, float yPercent);
    }
}
