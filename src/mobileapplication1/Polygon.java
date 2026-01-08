/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobileapplication1;

import java.util.Vector;

/**
 *
 * @author c
 */
public class Polygon {
    private final Vector triangles;
    private final float[] position; // Position of the polygon in world space (x, y, z)

    public Polygon(float x, float y, float z) {
        this.position = new float[]{x, y, z};
        this.triangles = new Vector();
    }

    public void addTriangle(Triangle triangle) {
        triangles.addElement(triangle);
    }

    public Vector getTrianglesInWorldSpace(World world) {
        Vector worldTriangles = new Vector();

        for (int i = 0; i < triangles.size(); i++) {
            Triangle triangle = (Triangle) triangles.elementAt(i);
            float[] worldV0 = world.applyWorldTranslation(triangle.v0, position);
            float[] worldV1 = world.applyWorldTranslation(triangle.v1, position);
            float[] worldV2 = world.applyWorldTranslation(triangle.v2, position);

            worldTriangles.addElement(new Triangle(worldV0, worldV1, worldV2, triangle.colour));
        }

        return worldTriangles;
    }

    public float[] getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position[0] = x;
        this.position[1] = y;
        this.position[2] = z;
    }
}
