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
public class Triangle {

    public float[] v0, v1, v2;
    public int[] colour = {255, 255, 255};

    public Triangle(float[] v0, float[] v1, float[] v2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    public Triangle(float[] v0, float[] v1, float[] v2, int[] colour) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.colour = new int[]{colour[0], colour[1], colour[2]};
    }

    public void changeColour(int[] colour) {
        this.colour = new int[]{colour[0], colour[1], colour[2]};
    }

    public void transformAll(VertexTransformer transformer) {
        v0 = transformer.apply(v0);
        v1 = transformer.apply(v1);
        v2 = transformer.apply(v2);
    }

    public int[][] project(Camera cam, World world, float aspect, float focal, Projector projector) {
        int[][] screenPoints = new int[3][2];

        float[] wv0 = world.applyWorldRotation(v0[0], v0[1], v0[2]);
        float[] wv1 = world.applyWorldRotation(v1[0], v1[1], v1[2]);
        float[] wv2 = world.applyWorldRotation(v2[0], v2[1], v2[2]);

        float[] c0 = cam.worldToCamera(wv0[0], wv0[1], wv0[2]);
        float[] c1 = cam.worldToCamera(wv1[0], wv1[1], wv1[2]);
        float[] c2 = cam.worldToCamera(wv2[0], wv2[1], wv2[2]);

        if (c0 == null || c1 == null || c2 == null) {
            return null;
        }

        float ax = c1[0] - c0[0];
        float ay = c1[1] - c0[1];

        float bx = c2[0] - c0[0];
        float by = c2[1] - c0[1];

        // 2D cross product (screen-facing test)
        float cross = ax * by - ay * bx;

        if (cross < 0 && Configuration.CULL_REAR) {
            return null; // triangle facing away
        }

        screenPoints[0] = projector.project(c0[0], c0[1], c0[2]);
        screenPoints[1] = projector.project(c1[0], c1[1], c1[2]);
        screenPoints[2] = projector.project(c2[0], c2[1], c2[2]);

        return screenPoints;
    }
}
