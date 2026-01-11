/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package J3DE;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Vector;
import java.util.Random;

/**
 * @author c
 */
public class Midlet extends MIDlet {

    Display display;
    GameCanvas canvas;

    public void startApp() {
        display = Display.getDisplay(this);
        canvas = new GameCanvas();
        display.setCurrent(canvas);
        canvas.startLoop();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        canvas.stopLoop();
    }

    class GameCanvas extends Canvas {

        Random random = new Random();

        private int[] randomColour() {
            int r, g, b;
            r = random.nextInt(255);
            g = random.nextInt(255);
            b = random.nextInt(255);

            int[] col = {r, g, b};

            return col;
        }

        static final int S_INSIDE = 0;
        static final int S_LEFT = 1;
        static final int S_RIGHT = 2;
        static final int S_BOTTOM = 4;
        static final int S_TOP = 8;

        private static final long FRAME_TIME = (1000 / Configuration.TARGET_FPS);

        private Thread loopThread;
        private boolean running = false;

        private int lastInput = 0;
        private int lastFrameTime = 0;
        private int lastFPS = 0;

        final float aspect = (float) getWidth() / (float) getHeight();

        World world = new World();
        Scripter script = new Scripter();
        Camera camera = new Camera(0, 0, 0);
        Matrix4 view = camera.getViewMatrix();
        Matrix4 proj = Matrix4.perspective(
                Configuration.FOV_DEGREES,
                aspect,
                0.1f,
                100f
        );

        private final Polygon cube;

        {
            cube = new Polygon(0f, 0f, -2f);

            // Front face (z = 0.5)
            float[] f0 = {-0.5f, -0.5f, 0.5f};
            float[] f1 = {0.5f, -0.5f, 0.5f};
            float[] f2 = {-0.5f, 0.5f, 0.5f};
            Triangle ft0 = new Triangle(f0, f1, f2, randomColour());

            float[] f3 = {0.5f, 0.5f, 0.5f};
            float[] f4 = {-0.5f, 0.5f, 0.5f};
            float[] f5 = {0.5f, -0.5f, 0.5f};
            Triangle ft1 = new Triangle(f3, f4, f5, randomColour());

            cube.addTriangle(ft0);
            cube.addTriangle(ft1);

            // Back face (z = -0.5)
            float[] b0 = {-0.5f, -0.5f, -0.5f};
            float[] b1 = {0.5f, -0.5f, -0.5f};
            float[] b2 = {-0.5f, 0.5f, -0.5f};
            Triangle bt0 = new Triangle(b2, b1, b0, randomColour());

            float[] b3 = {0.5f, 0.5f, -0.5f};
            float[] b4 = {-0.5f, 0.5f, -0.5f};
            float[] b5 = {0.5f, -0.5f, -0.5f};
            Triangle bt1 = new Triangle(b3, b4, b5, randomColour());

            cube.addTriangle(bt0);
            cube.addTriangle(bt1);

            // Left face (x = -0.5)
            float[] l0 = {-0.5f, -0.5f, -0.5f};
            float[] l1 = {-0.5f, -0.5f, 0.5f};
            float[] l2 = {-0.5f, 0.5f, -0.5f};
            Triangle lt0 = new Triangle(l0, l1, l2, randomColour());

            float[] l3 = {-0.5f, 0.5f, 0.5f};
            float[] l4 = {-0.5f, 0.5f, -0.5f};
            float[] l5 = {-0.5f, -0.5f, 0.5f};
            Triangle lt1 = new Triangle(l3, l4, l5, randomColour());

            cube.addTriangle(lt0);
            cube.addTriangle(lt1);

            // Right face (x = 0.5)
            float[] r0 = {0.5f, -0.5f, -0.5f};
            float[] r1 = {0.5f, -0.5f, 0.5f};
            float[] r2 = {0.5f, 0.5f, -0.5f};
            Triangle rt0 = new Triangle(r1, r0, r2, randomColour());

            float[] r3 = {0.5f, 0.5f, 0.5f};
            float[] r4 = {0.5f, -0.5f, 0.5f};
            float[] r5 = {0.5f, 0.5f, -0.5f};
            Triangle rt1 = new Triangle(r3, r4, r5, randomColour());

            cube.addTriangle(rt0);
            cube.addTriangle(rt1);

            // Top face (y = 0.5)
            float[] t0 = {-0.5f, 0.5f, -0.5f};
            float[] t1 = {0.5f, 0.5f, -0.5f};
            float[] t2 = {-0.5f, 0.5f, 0.5f};
            Triangle tt0 = new Triangle(t0, t1, t2, randomColour());

            float[] t3 = {0.5f, 0.5f, 0.5f};
            float[] t4 = {-0.5f, 0.5f, 0.5f};
            float[] t5 = {0.5f, 0.5f, -0.5f};
            Triangle tt1 = new Triangle(t3, t4, t5, randomColour());

            cube.addTriangle(tt0);
            cube.addTriangle(tt1);

            // Bottom face (y = -0.5)
            float[] bo0 = {-0.5f, -0.5f, -0.5f};
            float[] bo1 = {0.5f, -0.5f, -0.5f};
            float[] bo2 = {-0.5f, -0.5f, 0.5f};
            Triangle btr0 = new Triangle(bo2, bo1, bo0, randomColour());

            float[] bo3 = {0.5f, -0.5f, 0.5f};
            float[] bo4 = {-0.5f, -0.5f, 0.5f};
            float[] bo5 = {0.5f, -0.5f, -0.5f};
            Triangle btr1 = new Triangle(bo3, bo4, bo5, randomColour());

            cube.addTriangle(btr0);
            cube.addTriangle(btr1);
        }

        private final Polygon plane;

        {
            float[] v0 = {-0.5f, -0.5f, 0f};
            float[] v1 = {0.5f, -0.5f, 0f};
            float[] v2 = {-0.5f, 0.5f, 0f};
            Triangle triangle0 = new Triangle(v0, v1, v2, randomColour());

            float[] v3 = {0.5f, 0.5f, 0f};
            float[] v4 = {-0.5f, 0.5f, 0f};
            float[] v5 = {0.5f, -0.5f, 0f};

            Triangle triangle1 = new Triangle(v3, v4, v5, randomColour());
            plane = new Polygon(0f, 0f, -2f);
            plane.addTriangle(triangle0);
            plane.addTriangle(triangle1);
        }

        private final Polygon floor;

        {
            floor = new Polygon(0f, 0f, -0f);
            float[] v0 = {-0.5f, 0.5f, -0.5f};
            float[] v1 = {-0.5f, 0.5f, 0.5f};
            float[] v2 = {0.5f, 0.5f, -0.5f};
            Triangle triangle0 = new Triangle(v0, v1, v2, randomColour());

            float[] v3 = {0.5f, 0.5f, -0.5f};
            float[] v4 = {-0.5f, 0.5f, 0.5f};
            float[] v5 = {0.5f, 0.5f, 0.5f};
            Triangle triangle1 = new Triangle(v3, v4, v5, randomColour());
            plane.addTriangle(triangle0);
            plane.addTriangle(triangle1);
        }

        private int computeOutCode(int x, int y, int w, int h) {
            int code = S_INSIDE;

            if (x < 0) {
                code |= S_LEFT;
            } else if (x > w) {
                code |= S_RIGHT;
            }

            if (y < 0) {
                code |= S_TOP;
            } else if (y > h) {
                code |= S_BOTTOM;
            }

            return code;
        }

        private boolean clipLine(int[] p0, int[] p1, int w, int h) {
            int x0 = p0[0], y0 = p0[1];
            int x1 = p1[0], y1 = p1[1];

            int out0 = computeOutCode(x0, y0, w, h);
            int out1 = computeOutCode(x1, y1, w, h);

            while (true) {
                if ((out0 | out1) == 0) {
                    p0[0] = x0;
                    p0[1] = y0;
                    p1[0] = x1;
                    p1[1] = y1;
                    return true;
                }
                if ((out0 & out1) != 0) {
                    return false;
                }

                int out = (out0 != 0) ? out0 : out1;
                int x = 0, y = 0;

                if ((out & S_TOP) != 0) {
                    x = x0 + (x1 - x0) * (0 - y0) / (y1 - y0);
                    y = 0;
                } else if ((out & S_BOTTOM) != 0) {
                    x = x0 + (x1 - x0) * (h - y0) / (y1 - y0);
                    y = h;
                } else if ((out & S_RIGHT) != 0) {
                    y = y0 + (y1 - y0) * (w - x0) / (x1 - x0);
                    x = w;
                } else if ((out & S_LEFT) != 0) {
                    y = y0 + (y1 - y0) * (0 - x0) / (x1 - x0);
                    x = 0;
                }

                if (out == out0) {
                    x0 = x;
                    y0 = y;
                    out0 = computeOutCode(x0, y0, w, h);
                } else {
                    x1 = x;
                    y1 = y;
                    out1 = computeOutCode(x1, y1, w, h);
                }
            }
        }

        private void drawClippedLine(Graphics g, int[] a, int[] b) {
            int[] p0 = {a[0], a[1]};
            int[] p1 = {b[0], b[1]};

            if (clipLine(p0, p1, getWidth(), getHeight())) {
                g.drawLine(p0[0], p0[1], p1[0], p1[1]);
            }
        }

        private void drawTri(Graphics g, Triangle tri) {
            int[] p0 = Projection.projectVertex(tri.v0, view, proj, getWidth(), getHeight());
            int[] p1 = Projection.projectVertex(tri.v1, view, proj, getWidth(), getHeight());
            int[] p2 = Projection.projectVertex(tri.v2, view, proj, getWidth(), getHeight());

            if (p0 == null || p1 == null || p2 == null) {
                return;
            }
            g.setColor(tri.colour[0], tri.colour[1], tri.colour[2]);
            if (Configuration.WIREFRAME_RENDER == true) {
                drawClippedLine(g, p0, p1);
                drawClippedLine(g, p1, p2);
                drawClippedLine(g, p2, p0);
            } else {
                g.fillTriangle(
                        p0[0],
                        p0[1],
                        p1[0],
                        p1[1],
                        p2[0],
                        p2[1]
                );
            }
            g.setColor(255, 255, 255);
        }

        private String f3(float v) {
            int a = (int) (v * 1000f);
            return Float.toString((float) a / 1000.0f);
        }

        private void drawPolygon(Graphics g, Polygon polygon) {
            Vector triangles = polygon.getTrianglesInWorldSpace(world);
            Vector cameraTriangles = new Vector();

            // Transform all triangles into camera space
            for (int i = 0; i < triangles.size(); i++) {
                Triangle tri = (Triangle) triangles.elementAt(i);

                float[] c0 = camera.worldToCamera(tri.v0[0], tri.v0[1], tri.v0[2]);
                float[] c1 = camera.worldToCamera(tri.v1[0], tri.v1[1], tri.v1[2]);
                float[] c2 = camera.worldToCamera(tri.v2[0], tri.v2[1], tri.v2[2]);

                if (c0 == null || c1 == null || c2 == null) {
                    continue;
                }

                // Skip triangles completely behind the camera
                if (c0[2] >= -0.01f && c1[2] >= -0.01f && c2[2] >= -0.01f) {
                    continue;
                }

                // Create a temporary triangle in camera space (preserve colour)
                Triangle camTri = new Triangle(c0, c1, c2, tri.colour);
                cameraTriangles.addElement(camTri);
            }

            // Sort triangles by average z in camera space (Painter's Algorithm)
            for (int i = 0; i < cameraTriangles.size() - 1; i++) {
                for (int j = 0; j < cameraTriangles.size() - i - 1; j++) {
                    Triangle t1 = (Triangle) cameraTriangles.elementAt(j);
                    Triangle t2 = (Triangle) cameraTriangles.elementAt(j + 1);

                    float z1 = (t1.v0[2] + t1.v1[2] + t1.v2[2]) / 3;
                    float z2 = (t2.v0[2] + t2.v1[2] + t2.v2[2]) / 3;

                    if (z1 > z2) {
                        cameraTriangles.setElementAt(t2, j);
                        cameraTriangles.setElementAt(t1, j + 1);
                    }
                }
            }

            // Draw triangles in sorted order
            for (int i = 0; i < cameraTriangles.size(); i++) {
                Triangle triangle = (Triangle) cameraTriangles.elementAt(i);
                drawTri(g, triangle);
            }
        }

        protected void paint(Graphics g) {
            g.setColor(0, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());
            
            try {
                script.Execute(camera);
            } finally {
                
            }

            drawPolygon(g, cube);

            // Debug rendering
            if (Configuration.DEBUG_RENDERING) {
                g.setColor(255, 255, 255);

                g.drawString(
                        "Pos X/Y/Z: " + f3(camera.x) + "f/" + f3(camera.y) + "f/" + f3(camera.z) + "f",
                        0, 0, Graphics.TOP | Graphics.LEFT
                );

                g.drawString(
                        "Rot (quat): " + f3(camera.orientation.x) + " / " + f3(camera.orientation.y) + " / "
                        + f3(camera.orientation.z) + " / " + f3(camera.orientation.w),
                        0, 16, Graphics.TOP | Graphics.LEFT
                );

                g.drawString("Last keycode: " + lastInput, 0, 32, Graphics.TOP | Graphics.LEFT);
                g.drawString("FPS/MSPF: " + lastFPS + "/" + lastFrameTime + "ms", 0, 48, Graphics.TOP | Graphics.LEFT);
                if (script.scriptErrors > 0) {
                    g.setColor(255, 0, 0);
                    g.drawString("Script Errors: " + Integer.toString(script.scriptErrors), 0, 64, Graphics.TOP | Graphics.LEFT);
                }
            }
        }

        private void startLoop() {
            running = true;
            loopThread = new Thread() {
                long frameStart;
                long elapsed;
                long sleepTime;

                public void run() {
                    while (running) {
                        frameStart = System.currentTimeMillis();

                        repaint();
                        serviceRepaints();

                        elapsed = System.currentTimeMillis() - frameStart;
                        lastFrameTime = (int) (elapsed);
                        sleepTime = FRAME_TIME - elapsed;

                        if (sleepTime > 0) {
                            try {
                                Thread.sleep(FRAME_TIME);
                            } catch (InterruptedException e) {
                            }
                        }
                        elapsed = System.currentTimeMillis() - frameStart;
                        if (elapsed > 0) {
                            lastFPS = (int) (1000 / elapsed);
                        } else {
                            lastFPS = -1;
                        }
                    }
                }
            };
            loopThread.start();
        }

        private void stopLoop() {
            running = false;
            if (loopThread != null) {
                loopThread.interrupt();
                loopThread = null;
            }
        }

        protected void keyPressed(int keyCode) {
            float dx = 0, dy = 0, dz = 0;
            lastInput = keyCode;

            switch (keyCode) {
                case -3: {
                    Quaternion q = Quaternion.fromAxisAngle(0, 1, 0, 0.1f);
                    camera.orientation = q.multiply(camera.orientation);
                    break;
                }
                case -4: {
                    Quaternion q = Quaternion.fromAxisAngle(0, 1, 0, -0.1f);
                    camera.orientation = q.multiply(camera.orientation);
                    break;
                }
                case -1: {
                    float[] right = camera.rotateVector(camera.orientation, 1, 0, 0);
                    Quaternion q = Quaternion.fromAxisAngle(
                            right[0], right[1], right[2],
                            0.1f
                    );
                    camera.orientation = q.multiply(camera.orientation);
                    break;
                }
                case -2: {
                    float[] right = camera.rotateVector(camera.orientation, 1, 0, 0);
                    Quaternion q = Quaternion.fromAxisAngle(
                            right[0], right[1], right[2],
                            -0.1f
                    );
                    camera.orientation = q.multiply(camera.orientation);
                    break;
                }
                case KEY_NUM2:
                    dz = -0.1f;
                    break;
                case KEY_NUM8:
                    dz = 0.1f;
                    break;
                case KEY_NUM4:
                    dx = -0.1f;
                    break;
                case KEY_NUM6:
                    dx = 0.1f;
                    break;
            }

            if (dx != 0 || dz != 0) {
                float[] move = camera.rotateVector(camera.orientation, dx, dy, dz);

                camera.setPosition(
                        camera.x + move[0],
                        camera.y + move[1],
                        camera.z + move[2]
                );
            }
        }

    }
}
