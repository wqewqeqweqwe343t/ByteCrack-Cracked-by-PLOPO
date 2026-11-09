package dev.byteplus.render;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.joml.Vector4f;

public final class Esp {
   public static final float NEAR = 0.05F;
   private static int fbW;
   private static int fbH;
   private static float px = 1.0F;
   private static final Map<Integer, Esp.Group> groups = new LinkedHashMap<>();
   private static final float GUARD = 3.0F;
   private static final Vector4f[] corners = new Vector4f[8];
   private static final float[] clipIn = new float[64];
   private static final float[] clipOut = new float[64];
   private static final Vector4f tmpA = new Vector4f();
   private static final Vector4f tmpB = new Vector4f();
   private static final int[][] FACES;
   private static float guiScale;
   private static final Vector4f[] qc;

   private Esp() {
   }

   public static void begin(int framebufferW, int framebufferH, float scale) {
      fbW = framebufferW;
      fbH = framebufferH;
      guiScale = scale;
      px = Math.max(1.0F, scale / 2.0F);
      groups.clear();
   }

   public static int width() {
      return fbW;
   }

   public static int height() {
      return fbH;
   }

   public static float px() {
      return px;
   }

   private static Esp.Group group(int rgb) {
      return groups.computeIfAbsent(rgb & 16777215, k -> new Esp.Group());
   }

   public static void box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int rgb, int fillAlpha, int lineAlpha) {
      group(rgb).boxes.add(new double[]{minX, minY, minZ, maxX, maxY, maxZ, fillAlpha, lineAlpha});
   }

   public static void tracer(double x, double y, double z, int rgb, int alpha) {
      group(rgb).tracers.add(new double[]{x, y, z, alpha});
   }

   public static void line(double x1, double y1, double z1, double x2, double y2, double z2, int rgb, int alpha) {
      group(rgb).lines.add(new double[]{x1, y1, z1, x2, y2, z2, alpha});
   }

   public static void quad(double[] q, int rgb, int fillAlpha, int lineAlpha) {
      double[] e = new double[19];
      System.arraycopy(q, 0, e, 0, 15);
      e[15] = fillAlpha;
      e[16] = lineAlpha;
      group(rgb).quads.add(e);
   }

   public static void plate(double minX, double minZ, double maxX, double maxZ, double y, int rgb, int fillAlpha, int lineAlpha) {
      box(minX, y, minZ, maxX, y + 0.02, maxZ, rgb, fillAlpha, lineAlpha);
   }

   public static void flush(float tracerWidth) {
      if (!Projection.isValid()) {
         groups.clear();
      } else {
         double cx = Projection.camX();
         double cy = Projection.camY();
         double cz = Projection.camZ();
         float lineW = 1.15F * px;
         float tw = Math.max(0.75F, tracerWidth * px * 2.0F);

         for (Entry<Integer, Esp.Group> e : groups.entrySet()) {
            int rgb = e.getKey();
            Esp.Group g = e.getValue();
            Map<Integer, List<double[]>> byFill = new LinkedHashMap<>();
            Map<Integer, List<double[]>> byLine = new LinkedHashMap<>();

            for (double[] b : g.boxes) {
               if (b[6] > 0.0) {
                  byFill.computeIfAbsent((int)b[6], k -> new ArrayList<>()).add(b);
               }

               if (b[7] > 0.0) {
                  byLine.computeIfAbsent((int)b[7], k -> new ArrayList<>()).add(b);
               }
            }

            for (Entry<Integer, List<double[]>> f : byFill.entrySet()) {
               Render2D.beginPath();

               for (double[] b : f.getValue()) {
                  emitBox(b, cx, cy, cz);
               }

               Render2D.fillPath(Math.min(255, f.getKey()) << 24 | rgb);
            }

            for (Entry<Integer, List<double[]>> f : byLine.entrySet()) {
               int a = Math.min(255, f.getKey());
               Render2D.beginPath();

               for (double[] b : f.getValue()) {
                  emitBox(b, cx, cy, cz);
               }

               Render2D.strokePath(lineW, a << 24 | rgb);
            }

            if (!g.quads.isEmpty()) {
               Map<Integer, List<double[]>> qFill = new LinkedHashMap<>();
               Map<Integer, List<double[]>> qLine = new LinkedHashMap<>();
               double[] cam = new double[]{cx, cy, cz};

               for (double[] q : g.quads) {
                  int axis = (int)q[12];
                  if (!((cam[axis] - q[14]) * q[13] < 0.0)) {
                     if (q[15] > 0.0) {
                        qFill.computeIfAbsent((int)q[15], k -> new ArrayList<>()).add(q);
                     }

                     if (q[16] > 0.0) {
                        qLine.computeIfAbsent((int)q[16], k -> new ArrayList<>()).add(q);
                     }
                  }
               }

               for (Entry<Integer, List<double[]>> f : qFill.entrySet()) {
                  Render2D.beginPath();

                  for (double[] q : f.getValue()) {
                     emitQuad(q);
                  }

                  Render2D.fillPath(Math.min(255, f.getKey()) << 24 | rgb);
               }

               for (Entry<Integer, List<double[]>> f : qLine.entrySet()) {
                  Render2D.beginPath();

                  for (double[] q : f.getValue()) {
                     emitQuad(q);
                  }

                  Render2D.strokePath(lineW, Math.min(255, f.getKey()) << 24 | rgb);
               }
            }

            if (!g.lines.isEmpty()) {
               int a = 0;
               Render2D.beginPath();

               for (double[] l : g.lines) {
                  a = Math.max(a, (int)l[6]);
                  emitSegment(l[0], l[1], l[2], l[3], l[4], l[5]);
               }

               Render2D.strokePath(lineW, Math.min(255, a) << 24 | rgb);
            }

            if (!g.tracers.isEmpty()) {
               int a = 0;
               Render2D.beginPath();

               for (double[] t : g.tracers) {
                  a = Math.max(a, (int)t[3]);
                  emitTracer(t[0], t[1], t[2]);
               }

               Render2D.strokePath(tw, Math.min(255, a) << 24 | rgb);
            }
         }

         groups.clear();
      }
   }

   public static float[] project(double x, double y, double z) {
      Vector4f v = Projection.toClip(x, y, z, tmpA);
      return v.w <= 0.05F ? null : new float[]{(v.x / v.w * 0.5F + 0.5F) * fbW, (0.5F - v.y / v.w * 0.5F) * fbH};
   }

   private static void emitBox(double[] b, double cx, double cy, double cz) {
      int outL = 0;
      int outR = 0;
      int outB = 0;
      int outT = 0;
      int behind = 0;

      for (int i = 0; i < 8; i++) {
         double x = (i & 1) == 0 ? b[0] : b[3];
         double y = (i & 2) == 0 ? b[1] : b[4];
         double z = (i & 4) == 0 ? b[2] : b[5];
         Vector4f v = Projection.toClip(x, y, z, corners[i]);
         if (v.x < -v.w) {
            outL++;
         }

         if (v.x > v.w) {
            outR++;
         }

         if (v.y < -v.w) {
            outB++;
         }

         if (v.y > v.w) {
            outT++;
         }

         if (v.w < 0.05F) {
            behind++;
         }
      }

      if (outL != 8 && outR != 8 && outB != 8 && outT != 8 && behind != 8) {
         boolean inside = cx > b[0] && cx < b[3] && cy > b[1] && cy < b[4] && cz > b[2] && cz < b[5];
         boolean[] visible = inside
            ? new boolean[]{true, true, true, true, true, true}
            : new boolean[]{cy < b[1], cy > b[4], cz < b[2], cz > b[5], cx < b[0], cx > b[3]};

         for (int f = 0; f < 6; f++) {
            if (visible[f]) {
               int n = 0;

               for (int k = 0; k < 4; k++) {
                  Vector4f v = corners[FACES[f][k]];
                  clipIn[n * 4] = v.x;
                  clipIn[n * 4 + 1] = v.y;
                  clipIn[n * 4 + 2] = v.z;
                  clipIn[n * 4 + 3] = v.w;
                  n++;
               }

               n = clipNear(clipIn, n, clipOut);
               if (n >= 3) {
                  for (int k = 0; k < n; k++) {
                     float w = clipOut[k * 4 + 3];
                     float sx = (clipOut[k * 4] / w * 0.5F + 0.5F) * fbW;
                     float sy = (0.5F - clipOut[k * 4 + 1] / w * 0.5F) * fbH;
                     if (k == 0) {
                        Render2D.moveTo(sx, sy);
                     } else {
                        Render2D.lineTo(sx, sy);
                     }
                  }

                  Render2D.closePath();
               }
            }
         }
      }
   }

   private static void emitQuad(double[] q) {
      int outL = 0;
      int outR = 0;
      int outB = 0;
      int outT = 0;
      int behind = 0;

      for (int i = 0; i < 4; i++) {
         Vector4f v = Projection.toClip(q[i * 3], q[i * 3 + 1], q[i * 3 + 2], qc[i]);
         if (v.x < -v.w) {
            outL++;
         }

         if (v.x > v.w) {
            outR++;
         }

         if (v.y < -v.w) {
            outB++;
         }

         if (v.y > v.w) {
            outT++;
         }

         if (v.w < 0.05F) {
            behind++;
         }
      }

      if (outL != 4 && outR != 4 && outB != 4 && outT != 4 && behind != 4) {
         for (int i = 0; i < 4; i++) {
            Vector4f v = qc[i];
            clipIn[i * 4] = v.x;
            clipIn[i * 4 + 1] = v.y;
            clipIn[i * 4 + 2] = v.z;
            clipIn[i * 4 + 3] = v.w;
         }

         int n = clipNear(clipIn, 4, clipOut);
         if (n >= 3) {
            for (int k = 0; k < n; k++) {
               float w = clipOut[k * 4 + 3];
               float sx = (clipOut[k * 4] / w * 0.5F + 0.5F) * fbW;
               float sy = (0.5F - clipOut[k * 4 + 1] / w * 0.5F) * fbH;
               if (k == 0) {
                  Render2D.moveTo(sx, sy);
               } else {
                  Render2D.lineTo(sx, sy);
               }
            }

            Render2D.closePath();
         }
      }
   }

   private static float planeDist(float x, float y, float w, int p) {
      return switch (p) {
         case 0 -> w - 0.05F;
         case 1 -> 3.0F * w - x;
         case 2 -> 3.0F * w + x;
         case 3 -> 3.0F * w - y;
         default -> 3.0F * w + y;
      };
   }

   private static int clipNear(float[] in, int n, float[] out) {
      float[] src = in;
      float[] dst = out;

      for (int p = 0; p < 5 && n > 0; p++) {
         int m = 0;

         for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float di = planeDist(src[i * 4], src[i * 4 + 1], src[i * 4 + 3], p);
            float dj = planeDist(src[j * 4], src[j * 4 + 1], src[j * 4 + 3], p);
            if (di >= 0.0F) {
               System.arraycopy(src, i * 4, dst, m * 4, 4);
               m++;
            }

            if (di >= 0.0F != dj >= 0.0F) {
               float t = di / (di - dj);

               for (int c = 0; c < 4; c++) {
                  dst[m * 4 + c] = src[i * 4 + c] + (src[j * 4 + c] - src[i * 4 + c]) * t;
               }

               m++;
            }
         }

         n = m;
         float[] tmp = src;
         src = dst;
         dst = tmp;
      }

      if (src != out) {
         System.arraycopy(src, 0, out, 0, n * 4);
      }

      return n;
   }

   private static boolean clipSegment(Vector4f a, Vector4f b) {
      float t0 = 0.0F;
      float t1 = 1.0F;

      for (int p = 0; p < 5; p++) {
         float da = planeDist(a.x, a.y, a.w, p);
         float db = planeDist(b.x, b.y, b.w, p);
         if (da < 0.0F && db < 0.0F) {
            return false;
         }

         if (da < 0.0F) {
            t0 = Math.max(t0, da / (da - db));
         } else if (db < 0.0F) {
            t1 = Math.min(t1, da / (da - db));
         }
      }

      if (t0 > t1) {
         return false;
      }

      float ax = a.x;
      float ay = a.y;
      float aw = a.w;
      float bx = b.x;
      float by = b.y;
      float bw = b.w;
      a.set(ax + (bx - ax) * t0, ay + (by - ay) * t0, 0.0F, aw + (bw - aw) * t0);
      b.set(ax + (bx - ax) * t1, ay + (by - ay) * t1, 0.0F, aw + (bw - aw) * t1);
      return true;
   }

   private static void emitClipped(Vector4f a, Vector4f b) {
      if (clipSegment(a, b)) {
         Render2D.moveTo((a.x / a.w * 0.5F + 0.5F) * fbW, (0.5F - a.y / a.w * 0.5F) * fbH);
         Render2D.lineTo((b.x / b.w * 0.5F + 0.5F) * fbW, (0.5F - b.y / b.w * 0.5F) * fbH);
      }
   }

   private static void emitSegment(double x1, double y1, double z1, double x2, double y2, double z2) {
      emitClipped(Projection.toClip(x1, y1, z1, tmpA), Projection.toClip(x2, y2, z2, tmpB));
   }

   private static void emitTracer(double x, double y, double z) {
      Vector4f t = Projection.toClip(x, y, z, tmpB);
      tmpA.set(0.0F, 0.0F, 0.0F, 1.0F);
      emitClipped(tmpA, t);
   }

   static {
      for (int i = 0; i < 8; i++) {
         corners[i] = new Vector4f();
      }

      FACES = new int[][]{{0, 1, 5, 4}, {2, 3, 7, 6}, {0, 1, 3, 2}, {4, 5, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}};
      guiScale = 2.0F;
      qc = new Vector4f[]{new Vector4f(), new Vector4f(), new Vector4f(), new Vector4f()};
   }

   private static final class Group {
      final List<double[]> boxes = new ArrayList<>();
      final List<double[]> tracers = new ArrayList<>();
      final List<double[]> lines = new ArrayList<>();
      final List<double[]> quads = new ArrayList<>();
   }
}
