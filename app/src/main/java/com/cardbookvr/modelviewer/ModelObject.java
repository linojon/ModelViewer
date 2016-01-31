package com.cardbookvr.modelviewer;

import android.util.Log;

import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.components.RenderObject;
import com.cardbook.renderbox.materials.Material;
import com.cardbook.renderbox.materials.SolidColorLightingMaterial;
import com.cardbook.renderbox.materials.SolidColorMaterial;
import com.cardbook.renderbox.math.Vector3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

/**
 * Created by Jonathan on 1/30/2016.
 */
public class ModelObject extends RenderObject {
    private static FloatBuffer vertexBuffer;
    private static FloatBuffer colorBuffer;
    private static FloatBuffer normalBuffer;
    private static ShortBuffer indexBuffer;
    private int numIndices;

    Vector<Short> faces = new Vector<Short>();
    Vector<Short> vtPointer = new Vector<Short>();
    Vector<Short> vnPointer = new Vector<Short>();
    Vector<Float> v = new Vector<Float>();
    Vector<Float> vn = new Vector<Float>();
    Vector<Material> materials = null;

    public Vector3 extentsMin, extentsMax;

    public ModelObject(int objFile) {
        super();
        extentsMin = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        extentsMax = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        parseObj(objFile);
        createSolidColorMaterial(true);
    }

    public ModelObject createSolidColorMaterial(boolean lighting) {
        if (lighting) {
            SolidColorLightingMaterial scm = new SolidColorLightingMaterial(new float[]{0.5f, 0.5f, 0.5f, 1});
            scm.setBuffers(vertexBuffer, normalBuffer, indexBuffer, numIndices);
            material = scm;
        } else {
            SolidColorMaterial scm = new SolidColorMaterial(new float[]{0.5f, 0.5f, 0.5f, 1});
            scm.setBuffers(vertexBuffer, indexBuffer, numIndices);
            material = scm;
        }
        return this;
    }

    void parseObj(int objFile) {
        BufferedReader reader = null;
        String line = null;

        InputStream inputStream = RenderBox.instance.mainActivity.getResources().openRawResource(objFile);
        if (inputStream == null)
            return; // error

        reader = new BufferedReader(new InputStreamReader(inputStream));
        if (reader == null)
            return; // error

        try { // try to read lines of the file
            while ((line = reader.readLine()) != null) {
                parseLine(line);
                buildBuffers();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseLine(String line) {
        Log.v("obj", line);
        if (line.startsWith("f")) {//a polygonal face
            processFLine(line);
        } else if (line.startsWith("vn")) {
            processVNLine(line);
        } else if (line.startsWith("v")) { //line having geometric position of single vertex
            processVLine(line);
        }
    }

    private void processVLine(String line) {
        String[] tokens = line.split("[ ]+"); //split the line at the spaces
        int c = tokens.length;
        for (int i = 1; i < c; i++) { //add the vertex to the vertex array
            Float value = Float.valueOf(tokens[i]);
            v.add(value);
            setExtents(i, value);
        }
    }

    private void processVNLine(String line) {
        String[] tokens = line.split("[ ]+"); //split the line at the spaces
        int c = tokens.length;
        for (int i = 1; i < c; i++) { //add the vertex to the vertex array
            vn.add(Float.valueOf(tokens[i]));
        }
    }

    private void processFLine(String line) {
        String[] tokens = line.split("[ ]+");
        int c = tokens.length;

        if (tokens[1].matches("[0-9]+")) {//f: v
            if (c == 4) {//3 faces
                for (int i = 1; i < c; i++) {
                    Short s = Short.valueOf(tokens[i]);
                    s--;
                    faces.add(s);
                }
            } else {//more faces
                Vector<Short> polygon = new Vector<Short>();
                for (int i = 1; i < tokens.length; i++) {
                    Short s = Short.valueOf(tokens[i]);
                    s--;
                    polygon.add(s);
                }
                faces.addAll(triangulate(polygon));//triangulate the polygon and add the resulting faces
            }
        }

        if (tokens[1].matches("[0-9]+/[0-9]+")) {//if: v/vt
            if (c == 4) {//3 faces
                for (int i = 1; i < c; i++) {
                    Short s = Short.valueOf(tokens[i].split("/")[0]);
                    s--;
                    faces.add(s);
                    s = Short.valueOf(tokens[i].split("/")[1]);
                    s--;
                    vtPointer.add(s);
                }
            } else {//triangulate
                Vector<Short> tmpFaces = new Vector<Short>();
                Vector<Short> tmpVt = new Vector<Short>();
                for (int i = 1; i < tokens.length; i++) {
                    Short s = Short.valueOf(tokens[i].split("/")[0]);
                    s--;
                    tmpFaces.add(s);
                    s = Short.valueOf(tokens[i].split("/")[1]);
                    s--;
                    tmpVt.add(s);
                }
                faces.addAll(triangulate(tmpFaces));
                vtPointer.addAll(triangulate(tmpVt));
            }
        }

        if (tokens[1].matches("[0-9]+//[0-9]+")) {//f: v//vn
            if (c == 4) {//3 faces
                for (int i = 1; i < c; i++) {
                    Short s = Short.valueOf(tokens[i].split("//")[0]);
                    s--;
                    faces.add(s);
                    s = Short.valueOf(tokens[i].split("//")[1]);
                    s--;
                    vnPointer.add(s);
                }
            } else {//triangulate
                Vector<Short> tmpFaces = new Vector<Short>();
                Vector<Short> tmpVn = new Vector<Short>();
                for (int i = 1; i < tokens.length; i++) {
                    Short s = Short.valueOf(tokens[i].split("//")[0]);
                    s--;
                    tmpFaces.add(s);
                    s = Short.valueOf(tokens[i].split("//")[1]);
                    s--;
                    tmpVn.add(s);
                }
                faces.addAll(triangulate(tmpFaces));
                vnPointer.addAll(triangulate(tmpVn));
            }
        }

        if (tokens[1].matches("[0-9]+/[0-9]+/[0-9]+")) {//f: v/vt/vn

            if (c == 4) {//3 faces
                for (int i = 1; i < c; i++) {
                    Short s = Short.valueOf(tokens[i].split("/")[0]);
                    s--;
                    faces.add(s);
                    s = Short.valueOf(tokens[i].split("/")[1]);
                    s--;
                    vtPointer.add(s);
                    s = Short.valueOf(tokens[i].split("/")[2]);
                    s--;
                    vnPointer.add(s);
                }
            } else {//triangulate
                Vector<Short> tmpFaces = new Vector<Short>();
                Vector<Short> tmpVn = new Vector<Short>();
                for (int i = 1; i < tokens.length; i++) {
                    Short s = Short.valueOf(tokens[i].split("/")[0]);
                    s--;
                    tmpFaces.add(s);
                    s=Short.valueOf(tokens[i].split("/")[1]);
                    s=Short.valueOf(tokens[i].split("/")[2]);
                    s--;
                    tmpVn.add(s);
                }
                faces.addAll(triangulate(tmpFaces));
                vnPointer.addAll(triangulate(tmpVn));
            }

        }
    }

    private void buildBuffers() {
        numIndices = faces.size();
        float[] tmp = new float[v.size()];
        int i = 0;
        for(Float f : v)
            tmp[i++] = (f != null ? f : Float.NaN);
        vertexBuffer = allocateFloatBuffer(tmp);

        i = 0;
        tmp = new float[vn.size()];
        for(Float f : vn)
            tmp[i++] = -(f != null ? f : Float.NaN); //invert the normals
        normalBuffer = allocateFloatBuffer(tmp);

        i = 0;
        short[] indicices = new short[faces.size()];
        for(Short s : faces)
            indicices[i++] = (s != null ? s : 0);
        indexBuffer = allocateShortBuffer(indicices);
    }

    public static Vector<Short> triangulate(Vector<Short> polygon) {
        Vector<Short> triangles = new Vector<Short>();
        for (int i = 1; i < polygon.size() - 1; i++) {
            triangles.add(polygon.get(0));
            triangles.add(polygon.get(i));
            triangles.add(polygon.get(i + 1));
        }
        return triangles;
    }

    private void setExtents(int coord, Float value) {
        switch (coord) {
            case 1:
                if (value < extentsMin.x)
                    extentsMin.x = value;
                if (value > extentsMax.x)
                    extentsMax.x = value;
            case 2:
                if (value < extentsMin.y)
                    extentsMin.y = value;
                if (value > extentsMax.y)
                    extentsMax.y = value;
            case 3:
                if (value < extentsMin.z)
                    extentsMin.z = value;
                if (value > extentsMax.z)
                    extentsMax.z = value;
        }
    }

    public Vector3 center() {
        float centerX = (extentsMax.x + extentsMin.x)/2.0f;
        float centerY = (extentsMax.y + extentsMin.y)/2.0f;
        float centerZ = (extentsMax.z + extentsMin.z)/2.0f;
        return new Vector3(-centerX, -centerY, -centerZ);
    }

    public Vector3 normalScale() {
        float sizeX = (extentsMax.x - extentsMin.x);
        float sizeY = (extentsMax.y - extentsMin.y);
        float sizeZ = (extentsMax.z - extentsMin.z);
        float scalar = Math.max(sizeX, Math.max(sizeY, sizeZ)) * 10f;
        return new Vector3(sizeX/scalar, sizeY/scalar, sizeZ/scalar);
    }
}
