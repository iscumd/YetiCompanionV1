package com.example.yeticompanionv1;

import android.view.View;
import android.widget.Button;

import java.nio.ByteBuffer;

public class BaseFeedback {

    private MainActivity activity;
    private View layout;
    private Button btn1, btn2;

    BaseFeedback (MainActivity activity){
        this.activity = activity;
        //layout = activity.findViewById(R.id._layout);
        //btn1 = activity.findViewById(R.id.button1);
        //btn2 = activity.findViewById(R.id.button2);
        initCallback();
    }

    private void initCallback(){
        /*btn1.setOnClickListener(new View.OnClickListener() {
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
        });*/

    }

    public void show(){
        layout.setVisibility(View.VISIBLE);
    }

    public void hide(){
        layout.setVisibility(View.INVISIBLE);
    }

}
