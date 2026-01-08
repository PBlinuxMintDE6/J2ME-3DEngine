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
public class Camera {

    public float x, y, z;
    public Quaternion orientation = new Quaternion(0, 0, 0, 1);

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float[] rotateVector(Quaternion q, float x, float y, float z) {
        float qx = q.x, qy = q.y, qz = q.z, qw = q.w;

        // t = 2 * cross(q.xyz, v)
        float tx = 2 * (qy * z - qz * y);
        float ty = 2 * (qz * x - qx * z);
        float tz = 2 * (qx * y - qy * x);

        // v' = v + qw * t + cross(q.xyz, t)
        return new float[]{
            x + qw * tx + (qy * tz - qz * ty),
            y + qw * ty + (qz * tx - qx * tz),
            z + qw * tz + (qx * ty - qy * tx)
        };
    }

    float[] getForwardVector() {
        return rotateVector(orientation, 0, 0, -1);
    }

    float[] getRightVector() {
        return rotateVector(orientation, 1, 0, 0);
    }

    float[] getUpVector() {
        return rotateVector(orientation, 0, 1, 0);
    }

    public void rotate(float yawDelta, float pitchDelta) {

        Quaternion yawQ
                = Quaternion.fromAxisAngle(0, 1, 0, yawDelta);

        // Camera's current right vector
        float[] right = getRightVector();

        Quaternion pitchQ
                = Quaternion.fromAxisAngle(
                        right[0], right[1], right[2],
                        pitchDelta
                );

        orientation = yawQ.multiply(orientation);
        orientation = pitchQ.multiply(orientation);
    }

    // Move the camera
    public void setPosition(float posX, float posY, float posZ) {
        x = posX;
        y = posY;
        z = posZ;
    }
}
