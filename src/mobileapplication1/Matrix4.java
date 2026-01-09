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
public final class Matrix4 {

    public final float[] m = new float[16];

    public static Matrix4 identity() {
        Matrix4 r = new Matrix4();
        r.m[0] = r.m[5] = r.m[10] = r.m[15] = 1f;
        return r;
    }

    public float[] multiplyVec(float x, float y, float z, float w) {
        float[] r = new float[4];
        r[0] = m[0] * x + m[4] * y + m[8] * z + m[12] * w;
        r[1] = m[1] * x + m[5] * y + m[9] * z + m[13] * w;
        r[2] = m[2] * x + m[6] * y + m[10] * z + m[14] * w;
        r[3] = m[3] * x + m[7] * y + m[11] * z + m[15] * w;
        return r;
    }

    public static Matrix4 perspective(
            float fovDeg,
            float aspect,
            float near,
            float far
    ) {
        float f = (float) (1.0 / Math.tan(fovDeg * 0.5 * Math.PI / 180.0));

        Matrix4 p = new Matrix4();

        p.m[0] = f / aspect;
        p.m[5] = f;
        p.m[10] = (far + near) / (near - far);
        p.m[11] = -1f;
        p.m[14] = (2f * far * near) / (near - far);

        return p;
    }
}
