/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication1;

/**
 *
 * @author c
 */
public class CameraFuncs {
    public static float[] worldToCamera(float px, float py, float pz, Camera cam) {

        // 1. Translate world point relative to camera
        float x = px - cam.x;
        float y = py - cam.y;
        float z = pz - cam.z;

        // 2. Rotate by inverse camera orientation
        Quaternion inv = cam.orientation.conjugate();

        float[] v = cam.rotateVector(inv, x, y, z);

        x = v[0];
        y = v[1];
        z = v[2];

        // 3. Clipping: behind camera
        if (z >= -0.01f) {
            return null;
        }

        return new float[]{x, y, z};
    }

}
