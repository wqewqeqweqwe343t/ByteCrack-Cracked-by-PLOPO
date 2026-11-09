package dev.byteplus.render.knife;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_4587.class_4665;

public final class KnifeMesh {
   private static final int STRIDE = 8;
   private static final float CREASE = 0.62F;
   private final List<float[]> quads = new ArrayList<>();

   public void emit(class_4665 pose, class_4588 vc, int light) {
      this.emit(pose, vc, light, 1.0F, -1.0F, 0.0F);
   }

   public void emit(class_4665 pose, class_4588 vc, int light, float alpha, float shinePos, float shineAmt) {
      int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
      boolean shine = shinePos >= 0.0F && shineAmt > 0.001F;

      for (float[] q : this.quads) {
         for (int i = 0; i < 4; i++) {
            int o = i * 8;
            int c = Float.floatToRawIntBits(q[o + 6]) & 16777215;
            if (shine) {
               float d = (q[o + 7] - shinePos) / 0.07F;
               float k = shineAmt * (float)Math.exp(-d * d);
               if (k > 0.004F) {
                  c = lighten(c, k);
               }
            }

            vc.method_56824(pose, q[o], q[o + 1], q[o + 2])
               .method_39415(a << 24 | c)
               .method_22913(0.5F, 0.5F)
               .method_22922(class_4608.field_21444)
               .method_60803(light)
               .method_60831(pose, q[o + 3], q[o + 4], q[o + 5]);
         }
      }
   }

   private static int lighten(int rgb, float k) {
      int r = rgb >> 16 & 0xFF;
      int g = rgb >> 8 & 0xFF;
      int b = rgb & 0xFF;
      r += (int)((255 - r) * k);
      g += (int)((255 - g) * k);
      b += (int)((255 - b) * k);
      return r << 16 | g << 8 | b;
   }

   private static float[] faceNormal(float[] a, float[] b, float[] c, float[] d) {
      float e1x = c[0] - a[0];
      float e1y = c[1] - a[1];
      float e1z = c[2] - a[2];
      float e2x = d[0] - b[0];
      float e2y = d[1] - b[1];
      float e2z = d[2] - b[2];
      float nx = e1y * e2z - e1z * e2y;
      float ny = e1z * e2x - e1x * e2z;
      float nz = e1x * e2y - e1y * e2x;
      float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
      return len < 1.0E-8F ? null : new float[]{nx / len, ny / len, nz / len};
   }

   private static void orient(float[] n, float[] a, float[] b, float[] c, float[] d, float ox, float oy, float oz) {
      float mx = (a[0] + b[0] + c[0] + d[0]) / 4.0F - ox;
      float my = (a[1] + b[1] + c[1] + d[1]) / 4.0F - oy;
      float mz = (a[2] + b[2] + c[2] + d[2]) / 4.0F - oz;
      if (n[0] * mx + n[1] * my + n[2] * mz < 0.0F) {
         n[0] = -n[0];
         n[1] = -n[1];
         n[2] = -n[2];
      }
   }

   private void addQuad(float[][] p, float[][] n, int[] col, float[] u) {
      float[] q = new float[32];

      for (int i = 0; i < 4; i++) {
         int o = i * 8;
         q[o] = p[i][0];
         q[o + 1] = p[i][1];
         q[o + 2] = p[i][2];
         q[o + 3] = n[i][0];
         q[o + 4] = n[i][1];
         q[o + 5] = n[i][2];
         q[o + 6] = Float.intBitsToFloat(0xFF000000 | col[i]);
         q[o + 7] = u[i];
      }

      this.quads.add(q);
   }

   public KnifeMesh sweep(List<KnifeMesh.Station> stations, KnifeMesh.Shader shader, boolean capStart, boolean capEnd) {
      int ns = stations.size();
      int n = stations.get(0).section().length;
      float[][][] pts = new float[ns][n][];
      int[][] cols = new int[ns][n];
      float[][] centers = new float[ns][];

      for (int i = 0; i < ns; i++) {
         KnifeMesh.Station s = stations.get(i);
         float minA = Float.MAX_VALUE;
         float maxA = -Float.MAX_VALUE;

         for (float[] p : s.section()) {
            minA = Math.min(minA, p[0]);
            maxA = Math.max(maxA, p[0]);
         }

         float cx = 0.0F;
         float cy = 0.0F;
         float cz = 0.0F;

         for (int k = 0; k < n; k++) {
            float a = s.section()[k][0];
            float t = s.section()[k][1];
            float[] p = new float[]{s.x() + s.ax() * a, s.y() + s.ay() * a, t};
            pts[i][k] = p;
            cx += p[0];
            cy += p[1];
            cz += p[2];
            float an = maxA - minA < 1.0E-5F ? 0.5F : (a - minA) / (maxA - minA);
            cols[i][k] = shader.color(s.u(), an, Math.signum(t));
         }

         centers[i] = new float[]{cx / n, cy / n, cz / n};
      }

      float[][][] fn = new float[ns - 1][n][];

      for (int i = 0; i + 1 < ns; i++) {
         float ox = (centers[i][0] + centers[i + 1][0]) / 2.0F;
         float oy = (centers[i][1] + centers[i + 1][1]) / 2.0F;
         float oz = (centers[i][2] + centers[i + 1][2]) / 2.0F;

         for (int k = 0; k < n; k++) {
            int k2 = (k + 1) % n;
            float[] f = faceNormal(pts[i][k], pts[i][k2], pts[i + 1][k2], pts[i + 1][k]);
            if (f != null) {
               orient(f, pts[i][k], pts[i][k2], pts[i + 1][k2], pts[i + 1][k], ox, oy, oz);
            }

            fn[i][k] = f;
         }
      }

      for (int i = 0; i + 1 < ns; i++) {
         for (int k = 0; k < n; k++) {
            float[] f = fn[i][k];
            if (f != null) {
               int k2 = (k + 1) % n;
               int[][] corners = new int[][]{{i, k}, {i, k2}, {i + 1, k2}, {i + 1, k}};
               float[][] p = new float[4][];
               float[][] nn = new float[4][];
               int[] c = new int[4];
               float[] u = new float[4];

               for (int j = 0; j < 4; j++) {
                  int vi = corners[j][0];
                  int vk = corners[j][1];
                  p[j] = pts[vi][vk];
                  c[j] = cols[vi][vk];
                  u[j] = stations.get(vi).u();
                  nn[j] = smoothNormal(fn, vi, vk, n, ns, f);
               }

               this.addQuad(p, nn, c, u);
            }
         }
      }

      if (capStart) {
         this.cap(pts[0], cols[0], centers[0], ns > 1 ? centers[1] : centers[0], stations.get(0).u());
      }

      if (capEnd) {
         this.cap(pts[ns - 1], cols[ns - 1], centers[ns - 1], ns > 1 ? centers[ns - 2] : centers[0], stations.get(ns - 1).u());
      }

      return this;
   }

   private static float[] smoothNormal(float[][][] fn, int vi, int vk, int n, int ns, float[] ref) {
      float sx = 0.0F;
      float sy = 0.0F;
      float sz = 0.0F;

      for (int di = -1; di <= 0; di++) {
         int qi = vi + di;
         if (qi >= 0 && qi < ns - 1) {
            for (int dk = -1; dk <= 0; dk++) {
               int qk = ((vk + dk) % n + n) % n;
               float[] f = fn[qi][qk];
               if (f != null && !(f[0] * ref[0] + f[1] * ref[1] + f[2] * ref[2] < 0.62F)) {
                  sx += f[0];
                  sy += f[1];
                  sz += f[2];
               }
            }
         }
      }

      float len = (float)Math.sqrt(sx * sx + sy * sy + sz * sz);
      return len < 1.0E-6F ? ref : new float[]{sx / len, sy / len, sz / len};
   }

   private void cap(float[][] ring, int[] cols, float[] c, float[] inner, float u) {
      int n = ring.length;
      float ox = c[0] + (inner[0] - c[0]) * 2.0F;
      float oy = c[1] + (inner[1] - c[1]) * 2.0F;
      float oz = c[2] + (inner[2] - c[2]) * 2.0F;

      for (int k = 0; k < n; k++) {
         int k2 = (k + 1) % n;
         float[] f = faceNormal(c, ring[k], ring[k2], ring[k2]);
         if (f == null) {
            float e1x = ring[k][0] - c[0];
            float e1y = ring[k][1] - c[1];
            float e1z = ring[k][2] - c[2];
            float e2x = ring[k2][0] - c[0];
            float e2y = ring[k2][1] - c[1];
            float e2z = ring[k2][2] - c[2];
            float nx = e1y * e2z - e1z * e2y;
            float ny = e1z * e2x - e1x * e2z;
            float nz = e1x * e2y - e1y * e2x;
            float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1.0E-8F) {
               continue;
            }

            f = new float[]{nx / len, ny / len, nz / len};
         }

         orient(f, c, ring[k], ring[k2], ring[k2], ox, oy, oz);
         this.addQuad(
            new float[][]{c, ring[k], ring[k2], ring[k2]}, new float[][]{f, f, f, f}, new int[]{cols[0], cols[k], cols[k2], cols[k2]}, new float[]{u, u, u, u}
         );
      }
   }

   public KnifeMesh box(float x, float y0, float y1, float w, float d, float chamfer, KnifeMesh.Shader shader) {
      List<KnifeMesh.Station> st = new ArrayList<>();
      float[][] sec = roundedRect(w, d, chamfer);
      st.add(new KnifeMesh.Station(x, y0, 1.0F, 0.0F, 0.0F, sec));
      st.add(new KnifeMesh.Station(x, y1, 1.0F, 0.0F, 1.0F, sec));
      return this.sweep(st, shader, true, true);
   }

   public KnifeMesh prism(float[][] poly, float z0, float z1, float u, KnifeMesh.Shader shader) {
      int n = poly.length;
      float cx = 0.0F;
      float cy = 0.0F;

      for (float[] p : poly) {
         cx += p[0];
         cy += p[1];
      }

      cx /= n;
      cy /= n;
      float[][] bot = new float[n][];
      float[][] top = new float[n][];

      for (int k = 0; k < n; k++) {
         bot[k] = new float[]{poly[k][0], poly[k][1], z0};
         top[k] = new float[]{poly[k][0], poly[k][1], z1};
      }

      float zc = (z0 + z1) / 2.0F;
      float[][] fn = new float[n][];

      for (int k = 0; k < n; k++) {
         int k2 = (k + 1) % n;
         float[] f = faceNormal(bot[k], bot[k2], top[k2], top[k]);
         if (f != null) {
            orient(f, bot[k], bot[k2], top[k2], top[k], cx, cy, zc);
         }

         fn[k] = f;
      }

      for (int k = 0; k < n; k++) {
         int k2 = (k + 1) % n;
         float[] f = fn[k];
         if (f != null) {
            float[] n0 = sideNormal(fn, k, f, n);
            float[] n1 = sideNormal(fn, k2, f, n);
            int c0 = shader.color(u, 0.5F + 0.5F * (poly[k][0] - cx), 0.0F);
            int c1 = shader.color(u, 0.5F + 0.5F * (poly[k2][0] - cx), 0.0F);
            this.addQuad(new float[][]{bot[k], bot[k2], top[k2], top[k]}, new float[][]{n0, n1, n1, n0}, new int[]{c0, c1, c1, c0}, new float[]{u, u, u, u});
         }
      }

      int capCol = shader.color(u, 0.5F, 1.0F);
      float[] cTop = new float[]{cx, cy, z1};
      float[] cBot = new float[]{cx, cy, z0};
      this.cap(top, fill(n, capCol), cTop, cBot, u);
      this.cap(bot, fill(n, shader.color(u, 0.5F, -1.0F)), cBot, cTop, u);
      return this;
   }

   private static float[] sideNormal(float[][] fn, int vertex, float[] ref, int n) {
      float[] a = fn[(vertex - 1 + n) % n];
      float[] b = fn[vertex % n];
      float sx = 0.0F;
      float sy = 0.0F;
      float sz = 0.0F;

      for (float[] f : new float[][]{a, b}) {
         if (f != null && !(f[0] * ref[0] + f[1] * ref[1] + f[2] * ref[2] < 0.62F)) {
            sx += f[0];
            sy += f[1];
            sz += f[2];
         }
      }

      float len = (float)Math.sqrt(sx * sx + sy * sy + sz * sz);
      return len < 1.0E-6F ? ref : new float[]{sx / len, sy / len, sz / len};
   }

   private static int[] fill(int n, int c) {
      int[] a = new int[n];
      Arrays.fill(a, c);
      return a;
   }

   public static float[][] roundedRect(float w, float d, float c) {
      float hw = w / 2.0F;
      float hd = d / 2.0F;
      c = Math.min(c, Math.min(hw, hd));
      return new float[][]{{-hw + c, hd}, {hw - c, hd}, {hw, hd - c}, {hw, -hd + c}, {hw - c, -hd}, {-hw + c, -hd}, {-hw, -hd + c}, {-hw, hd - c}};
   }

   public static float[][] circle(float cx, float cy, float r, int seg) {
      float[][] p = new float[seg][];

      for (int i = 0; i < seg; i++) {
         double a = (Math.PI * 2) * i / seg;
         p[i] = new float[]{cx + (float)Math.cos(a) * r, cy + (float)Math.sin(a) * r};
      }

      return p;
   }

   public static float[][] circle(float r, int seg) {
      return circle(0.0F, 0.0F, r, seg);
   }

   public interface Shader {
      int color(float var1, float var2, float var3);
   }

   public record Station(float x, float y, float ax, float ay, float u, float[][] section) {
   }
}
