package com.cardbookvr.modelviewer;

import android.os.Bundle;

import com.cardbook.renderbox.IRenderBox;
import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.Transform;
import com.cardbook.renderbox.math.MathUtils;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

public class MainActivity extends CardboardActivity implements IRenderBox {
    static String TAG = "MainActivity";

    Transform model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(new RenderBox(this, this));
        setCardboardView(cardboardView);
    }

    @Override
    public void setup() {
        RenderBox.instance.mainLight.transform.setLocalPosition(0,0,-10);
        model = new Transform()
                .setLocalPosition(0, -2, -3)
                .setLocalScale(0.01f, 0.01f, 0.01f)
                .addComponent(new ModelObject(R.raw.teapot));
    }

    @Override
    public void preDraw() {
        float[] hAngles = RenderBox.instance.headAngles;
        model.setLocalRotation(hAngles[0] * MathUtils.radiansToDegrees, hAngles[1] * MathUtils.radiansToDegrees, hAngles[2] * MathUtils.radiansToDegrees);
    }

    @Override
    public void postDraw() {

    }
}
