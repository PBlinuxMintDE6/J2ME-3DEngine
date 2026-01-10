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
    // Translate a point from polygon space to world space
    public float[] applyWorldTranslation(float[] point, float[] position) {
        return new float[]{
            point[0] + position[0],
            point[1] + position[1],
            point[2] + position[2]
        };
    }
}

