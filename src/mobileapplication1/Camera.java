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
    // Camera position in world space
    public float x, y, z;

    // Rotation angles (radians) around each axis
    public float rotX, rotY, rotZ;

    public Camera(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotX = 0;
        this.rotY = 0;
        this.rotZ = 0;
    }

    // Set rotation
    public void setRotation(float rotX, float rotY, float rotZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
    }

    // Move the camera
    public void setPosition(float posX, float posY, float posZ) {
        x = posX;
        y = posY;
        z = posZ;
    }
}

