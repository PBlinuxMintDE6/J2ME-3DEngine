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
public class World {
    public float rotX = 0, rotY = 0, rotZ = 0;

    // Rotate all objects in world space
    public float[] applyWorldRotation(float px, float py, float pz) {
        float cosX = (float)Math.cos(rotX), sinX = (float)Math.sin(rotX);
        float cosY = (float)Math.cos(rotY), sinY = (float)Math.sin(rotY);
        float cosZ = (float)Math.cos(rotZ), sinZ = (float)Math.sin(rotZ);

        float x = px, y = py, z = pz;

        // Rotate X
        float ry = y * cosX - z * sinX;
        float rz = y * sinX + z * cosX;
        y = ry; z = rz;

        // Rotate Y
        float rx = x * cosY + z * sinY;
        rz = -x * sinY + z * cosY;
        x = rx; z = rz;

        // Rotate Z
        rx = x * cosZ - y * sinZ;
        ry = x * sinZ + y * cosZ;
        x = rx; y = ry;

        return new float[]{ x, y, z };
    }

    // Translate a point from polygon space to world space
    public float[] applyWorldTranslation(float[] point, float[] position) {
        return new float[]{
            point[0] + position[0],
            point[1] + position[1],
            point[2] + position[2]
        };
    }

    // Update world rotation (delta)
    public void rotate(float dX, float dY, float dZ) {
        rotX += dX;
        rotY += dY;
        rotZ += dZ;
    }
}

