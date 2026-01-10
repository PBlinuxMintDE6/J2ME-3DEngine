/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package J3DE;

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

    public float[] rotateVector(Quaternion q, float cx, float cy, float cz) {
        float qx = q.x, qy = q.y, qz = q.z, qw = q.w;

        // t = 2 * cross(q.xyz, v)
        float tx = 2 * (qy * cz - qz * cy);
        float ty = 2 * (qz * cx - qx * cz);
        float tz = 2 * (qx * cy - qy * cx);

        // v' = v + qw * t + cross(q.xyz, t)
        return new float[]{
            cx + qw * tx + (qy * tz - qz * ty),
            cy + qw * ty + (qz * tx - qx * tz),
            cz + qw * tz + (qx * ty - qy * tx)
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

    public float[] worldToCamera(float px, float py, float pz) {

        // 1. Translate world point relative to camera
        float tx = px - this.x;
        float ty = py - this.y;
        float tz = pz - this.z;

        // 2. Rotate by inverse camera orientation
        Quaternion inv = this.orientation.conjugate();

        float[] v = this.rotateVector(inv, tx, ty, tz);

        tx = v[0];
        ty = v[1];
        tz = v[2];

        // 3. Clipping: behind camera
        if (tz >= -0.01f) {
            return null;
        }

        return new float[]{tx, ty, tz};
    }

    public Matrix4 getViewMatrix() {
        Quaternion q = orientation.conjugate();

        float qx = q.x, qy = q.y, qz = q.z, w = q.w;

        Matrix4 v = Matrix4.identity();

        v.m[0] = 1 - 2 * qy * qy - 2 * qz * qz;
        v.m[1] = 2 * qx * qy + 2 * w * qz;
        v.m[2] = 2 * qx * qz - 2 * w * qy;

        v.m[4] = 2 * qx * qy - 2 * w * qz;
        v.m[5] = 1 - 2 * qx * qx - 2 * qz * qz;
        v.m[6] = 2 * qy * qz + 2 * w * qx;

        v.m[8] = 2 * qx * qz + 2 * w * qy;
        v.m[9] = 2 * qy * qz - 2 * w * qx;
        v.m[10] = 1 - 2 * qx * qx - 2 * qy * qy;

        v.m[12] = -(v.m[0] * qx + v.m[4] * qy + v.m[8] * qz);
        v.m[13] = -(v.m[1] * qx + v.m[5] * qy + v.m[9] * qz);
        v.m[14] = -(v.m[2] * qx + v.m[6] * qy + v.m[10] * qz);

        return v;
    }

    // Move the camera
    public void setPosition(float posX, float posY, float posZ) {
        x = posX;
        y = posY;
        z = posZ;
    }
}
