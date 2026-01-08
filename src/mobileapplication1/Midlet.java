/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication1;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

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

        World world = new World();
        Camera camera = new Camera(0, 0, 0);

        float[] v0 = {-0.5f, -0.5f, -2f};
        float[] v1 = {0.5f, -0.5f, -2f};
        float[] v2 = {-0.5f, 0.5f, -2f};
        Triangle triangle = new Triangle(v0, v1, v2);

        float[] v3 = {0.5f, 0.5f, -2f};
        float[] v4 = {-0.5f, 0.5f, -2f};
        float[] v5 = {0.5f, -0.5f, -2f};

        Triangle triangle1 = new Triangle(v3, v4, v5);

        float[] pl0 = {-0.5f, 0.5f, -2.5f};
        float[] pl1 = {-0.5f, 0.5f, -1.5f};
        float[] pl2 = {0.5f, 0.5f, -2.5f};
        Triangle plane0 = new Triangle(pl0, pl1, pl2);

        float[] pl3 = {0.5f, 0.5f, -2.5f};
        float[] pl4 = {-0.5f, 0.5f, -1.5f};
        float[] pl5 = {0.5f, 0.5f, -1.5f};
        Triangle plane1 = new Triangle(pl3, pl4, pl5);

        final float aspect = (float) getWidth() / (float) getHeight();
        final float focal = (float) (1 / Math.tan(Configuration.FOV_DEGREES / 2));

        Projector projector = new Projector() {
            public int[] project(float x, float y, float z) {
                float xN = (focal * x / aspect) / -z;
                float yN = (y * focal) / -z;

                int sx = (int) ((xN * 0.5f + 0.5f) * getWidth());
                int sy = (int) ((1f - (yN * 0.5f + 0.5f)) * getHeight());

                return new int[]{sx, sy};
            }
        };

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

        public int[] project(float x, float y, float z) {
            float xN = (focal * x / aspect) / -z;
            float yN = (y * focal) / -z;

            int sx = (int) ((xN * 0.5f + 0.5f) * getWidth());
            int sy = (int) ((1f - (yN * 0.5f + 0.5f)) * getHeight());

            return new int[]{sx, sy};
        }

        private void drawClippedLine(Graphics g, int[] a, int[] b) {
            int[] p0 = {a[0], a[1]};
            int[] p1 = {b[0], b[1]};

            if (clipLine(p0, p1, getWidth(), getHeight())) {
                g.drawLine(p0[0], p0[1], p1[0], p1[1]);
            }
        }

        private void drawTri(Graphics g, Triangle tri, float aspect, float focal, Projector projector) {
            int[][] screenPoints = tri.project(camera, world, aspect, focal, projector);
            if ((screenPoints != null) && (Configuration.WIREFRAME_RENDER)) {
                drawClippedLine(g, screenPoints[0], screenPoints[1]);
                drawClippedLine(g, screenPoints[1], screenPoints[2]);
                drawClippedLine(g, screenPoints[2], screenPoints[0]);
            } else if (screenPoints != null) {
                g.fillTriangle(
                        screenPoints[0][0],
                        screenPoints[0][1],
                        screenPoints[1][0],
                        screenPoints[1][1],
                        screenPoints[2][0],
                        screenPoints[2][1]
                );
            }
        }

        protected void paint(Graphics g) {
            g.setColor(0, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw the triangles
            g.setColor(0, 0, 255);
            drawTri(g, plane0, aspect, focal, projector);
            g.setColor(0, 127, 127);
            drawTri(g, plane1, aspect, focal, projector);
            g.setColor(0, 255, 0);
            drawTri(g, triangle, aspect, focal, projector);
            g.setColor(255, 0, 0);
            drawTri(g, triangle1, aspect, focal, projector);

            // Debug rendering
            if (Configuration.DEBUG_RENDERING) {
                g.setColor(255, 255, 255);
                g.drawString("Pos X/Y/Z: " + Float.toString(camera.x) + "f/" + Float.toString(camera.y) + "f/" + Float.toString(camera.z) + "f", 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawString("Rot X/Y/Z: " + Float.toString(camera.rotX) + "f/" + Float.toString(camera.rotY) + "f/" + Float.toString(camera.rotZ) + "f", 0, 16, Graphics.TOP | Graphics.LEFT);
                g.drawString("Last keycode: " + Integer.toString(lastInput), 0, 32, Graphics.TOP | Graphics.LEFT);
                g.drawString("FPS/MSPF: " + Integer.toString(lastFPS) + "/" + Long.toString(lastFrameTime) + "ms", 0, 48, Graphics.TOP | Graphics.LEFT);
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
                case -3:
                    camera.rotY -= 0.1f;
                    break;
                case -4:
                    camera.rotY += 0.1f;
                    break;
                case -1:
                    camera.rotX -= 0.1f;
                    break;
                case -2:
                    camera.rotX += 0.1f;
                    break;
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
                float sinY = (float) Math.sin(camera.rotY);
                float cosY = (float) Math.cos(camera.rotY);
                float sinX = (float) Math.sin(camera.rotX);
                float cosX = (float) Math.cos(camera.rotX);

                // Right vector (remember +X = left)
                float rightX = cosY;
                float rightZ = sinY;

                // Forward vector (−Z forward, pitch = −X)
                float forwardX = sinY * cosX;
                float forwardY = -sinX;
                float forwardZ = -cosY * cosX;

                // Apply input signs
                float moveX = (-dx) * rightX + (-dz) * forwardX;
                float moveY = (-dz) * forwardY;
                float moveZ = (-dx) * rightZ + (-dz) * forwardZ;

                camera.setPosition(
                        camera.x + moveX,
                        camera.y + moveY,
                        camera.z + moveZ
                );
            }
        }

    }
}
