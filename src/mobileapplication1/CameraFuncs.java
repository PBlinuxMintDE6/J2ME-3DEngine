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

            // 2. Apply inverse camera rotation
        // If camera rotates +Y, world rotates -Y
        float cosX = (float) Math.cos(-cam.rotX), sinX = (float) Math.sin(-cam.rotX);
        float cosY = (float) Math.cos(-cam.rotY), sinY = (float) Math.sin(-cam.rotY);
        float cosZ = (float) Math.cos(-cam.rotZ), sinZ = (float) Math.sin(-cam.rotZ);

        // Rotate X
        float ry = y * cosX - z * sinX;
        float rz = y * sinX + z * cosX;
        y = ry;
        z = rz;

        // Rotate Y
        float rx = x * cosY + z * sinY;
        rz = -x * sinY + z * cosY;
        x = rx;
        z = rz;
        
        if (z >= -0.01f) {
            return null; // Point is behind camera do not render
        }

        // Rotate Z
        rx = x * cosZ - y * sinZ;
        ry = x * sinZ + y * cosZ;
        x = rx;
        y = ry;

        return new float[]{x, y, z};
    }
}
