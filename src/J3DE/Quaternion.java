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
public class Quaternion {

    float x, y, z, w;

    Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }

    static Quaternion fromAxisAngle(float ax, float ay, float az, float angle) {
        float half = angle * 0.5f;
        float s = (float) Math.sin(half);
        return new Quaternion(ax * s, ay * s, az * s, (float) Math.cos(half));
    }

    Quaternion multiply(Quaternion q) {
        return new Quaternion(
                w * q.x + x * q.w + y * q.z - z * q.y,
                w * q.y - x * q.z + y * q.w + z * q.x,
                w * q.z + x * q.y - y * q.x + z * q.w,
                w * q.w - x * q.x - y * q.y - z * q.z
        );
    }
}
