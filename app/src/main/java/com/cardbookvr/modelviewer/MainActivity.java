package com.cardbookvr.modelviewer;

import android.os.Bundle;
import android.util.Log;

import com.cardbook.renderbox.IRenderBox;
import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.Transform;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

public class MainActivity extends CardboardActivity implements IRenderBox {
    static String TAG = "MainActivity";

    Transform modelRoot;

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
        //RenderBox.instance.mainLight.transform.setLocalPosition(2,2,0);
        ModelObject model = new ModelObject(R.raw.teapot);
        Log.d(TAG, "extentsMin: " + model.extentsMin.toString() );
        Log.d(TAG, "extentsMax: " + model.extentsMax.toString() );
        Log.d(TAG, "centerOffset: " + model.center().toString());
        Log.d(TAG, "normalScale: " + model.normalScalar());

        //Vector3 center = model.center();
        float scalar = model.normalScalar();
        new Transform()
                .setParent(modelRoot, true)
                //.setLocalPosition(-center.x, -center.y, -center.z)
                .setLocalPosition(0, -2, -3)
                .setLocalScale(scalar, scalar, scalar)
//                .setLocalRotation(90, 0, 0)
                .addComponent(model);
    }

    @Override
    public void preDraw() {
//        float[] hAngles = RenderBox.instance.headAngles;
//        model.setLocalRotation(hAngles[0] * MathUtils.radiansToDegrees, hAngles[1] * MathUtils.radiansToDegrees, hAngles[2] * MathUtils.radiansToDegrees);
    }

    @Override
    public void postDraw() {

    }
}
