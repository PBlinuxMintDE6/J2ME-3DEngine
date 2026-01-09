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
public final class Projection {

    private Projection() {
    } // utility class

    public static int[] projectVertex(
            float[] worldPos,
            Matrix4 view,
            Matrix4 proj,
            int screenW,
            int screenH
    ) {
        float[] v = view.multiplyVec(
                worldPos[0], worldPos[1], worldPos[2], 1f
        );

        float[] p = proj.multiplyVec(v[0], v[1], v[2], 1f);

        // Behind camera / invalid
        if (p[3] <= 0f) {
            return null;
        }

        float invW = 1f / p[3];

        float ndcX = p[0] * invW;
        float ndcY = p[1] * invW;

        int sx = (int) ((ndcX * 0.5f + 0.5f) * screenW);
        int sy = (int) ((1f - (ndcY * 0.5f + 0.5f)) * screenH);

        return new int[]{sx, sy};
    }
}
