package com.example.yeticompanionv1;

import android.view.View;
import android.widget.Button;

import java.nio.ByteBuffer;

public class Controller {

    private MainActivity activity;
    private JoystickView joystickView;
    private View layout;
    private Button btn1, btn2;

    Controller (MainActivity activity){
        this.activity = activity;
        joystickView = activity.findViewById(R.id.Joystick);
        layout = activity.findViewById(R.id.controller_layout);
        btn1 = activity.findViewById(R.id.button1);
        btn2 = activity.findViewById(R.id.button2);
        initCallback();
    }

    private void initCallback(){

        joystickView.setJoystickListener(new JoystickView.JoystickListener() {
            @Override
            public void onJoystickMoved(float xPercent, float yPercent) {
                byte[] bufferX = ByteBuffer.allocate(9).putFloat(1,xPercent).array();
                byte[] bufferY = ByteBuffer.allocate(9).putFloat(1,yPercent).array();
                bufferX[0] = 1;
                bufferY[0] = 2;
                activity.writeToPI(bufferX.clone());
                activity.writeToPI(bufferY.clone());
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] buffer = ByteBuffer.allocate(9).putFloat(1,-2).array();
                buffer[0]=3;
                activity.writeToPI(buffer.clone());
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] buffer = ByteBuffer.allocate(9).putFloat(1,2).array();
                buffer[0]=3;
                activity.writeToPI(buffer.clone());
            }
        });

    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    public void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

}
