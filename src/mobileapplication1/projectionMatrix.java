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
public class projectionMatrix {
    public float[] matrix;
    
    public projectionMatrix(float fovY, float aspectRatio, float near, float far) {
        matrix = new float[16];
        
        float t = (float) Math.tan(fovY * 0.5f);
        
        matrix[0] = 1f / (aspectRatio * t);
        matrix[5] = 1f / t;
        matrix[10] = -(far + near) / (far - near);
        matrix[11] = -(2f * far * near) / (far - near);
        matrix[14] = -1f;
        matrix[15] = 0f;
    }
}
