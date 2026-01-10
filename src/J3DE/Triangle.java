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
}
