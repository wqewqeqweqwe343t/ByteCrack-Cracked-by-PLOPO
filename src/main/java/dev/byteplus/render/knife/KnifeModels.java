package dev.byteplus.render.knife;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KnifeModels {
   public static final String[] TYPES = new String[]{"Butterfly", "Karambit", "Bayonet", "Flip", "Bowie"};
   public static final String[] FINISHES = new String[]{
      "Stock", "Fade", "Ruby", "Sapphire", "Emerald", "Black Pearl", "Gold", "Tiger", "Marble", "Night", "Crimson", "Ultraviolet"
   };
   private static final Map<String, KnifeModels.Model> CACHE = new HashMap<>();

   private KnifeModels() {
   }

   public static KnifeModels.Model get(String type, String finish) {
      return CACHE.computeIfAbsent(type + "|" + finish, k -> build(type, finish));
   }

   private static KnifeModels.Finish finish(String name) {
      return switch (name) {
         case "Fade" -> new KnifeModels.Finish(
            (u, a, t) -> polish(gradient(u, 16766528, 16732044, 8015359), u, a, 0.16F), g10(1971748, 2891828), steel(2761776)
         );
         case "Ruby" -> gem(10224658, 1705223, 2756627);
         case "Sapphire" -> gem(734176, 461596, 1054250);
         case "Emerald" -> gem(176204, 333324, 926230);
         case "Black Pearl" -> new KnifeModels.Finish((u, a, t) -> {
            float n = noise(u * 5.0F, a * 2.5F);
            float m = noise(u * 11.0F + 3.0F, a * 4.0F);
            return polish(mixc(mixc(3412058, 289402, n), 657942, m * 0.6F), u, a, 0.18F);
         }, g10(920340, 1709092), steel(2761779));
         case "Gold" -> new KnifeModels.Finish((u, a, t) -> brushed(15908421, u, a), g10(1709584, 2761752), steel(15251530));
         case "Tiger" -> new KnifeModels.Finish((u, a, t) -> {
            float s = (float)Math.sin(u * 34.0F + a * 4.0F + noise(u * 6.0F, a * 3.0F) * 4.0F);
            return polish(s > 0.45F ? 1181700 : 15895060, u, a, 0.12F);
         }, g10(1708040, 2759180), steel(3811866));
         case "Marble" -> new KnifeModels.Finish((u, a, t) -> {
            float n = noise(u * 3.5F, a * 2.2F);
            int c = mixc(14162476, 2046944, smooth(0.35F, 0.65F, n));
            if (Math.abs(noise(u * 9.0F + 7.0F, a * 5.0F) - 0.5F) < 0.035F) {
               c = 657938;
            }

            return polish(c, u, a, 0.18F);
         }, g10(1183252, 2234920), steel(2894387));
         case "Night" -> new KnifeModels.Finish((u, a, t) -> brushed(1843760, u, a), g10(723984, 1382174), steel(2237999));
         case "Crimson" -> new KnifeModels.Finish((u, a, t) -> {
            boolean web = Math.abs(Math.sin(u * 24.0F + Math.sin(a * 6.0F) * 1.6F)) < 0.13F || Math.abs(Math.sin(a * 9.0F + u * 3.0F)) < 0.08F;
            return polish(web ? 786946 : 11012108, u, a, 0.14F);
         }, g10(1311749, 2492939), steel(2757911));
         case "Ultraviolet" -> new KnifeModels.Finish(
            (u, a, t) -> polish(noise(u * 7.0F, a * 3.0F) > 0.66F ? 1969704 : 5906310, u, a, 0.14F), g10(1182232, 1970728), steel(2760755)
         );
         default -> new KnifeModels.Finish((u, a, t) -> brushed(10923190, u, a), g10(1842464, 2763824), steel(9212057));
      };
   }

   private static KnifeModels.Finish gem(int base, int handle, int fittings) {
      return new KnifeModels.Finish((u, a, t) -> {
         float n = noise(u * 7.0F, a * 3.0F);
         return polish(mixc(mixc(base, 0, 0.55F), mixc(base, 16777215, 0.25F), n), u, a, 0.2F);
      }, g10(handle, mixc(handle, base, 0.3F)), steel(fittings));
   }

   private static KnifeMesh.Shader g10(int dark, int light) {
      return (u, a, t) -> {
         float w = (float)(Math.sin(u * 70.0F) * Math.sin(a * 22.0F + u * 8.0F));
         return shade(mixc(dark, light, 0.5F + 0.5F * w), 0.92F + 0.12F * a);
      };
   }

   private static KnifeMesh.Shader steel(int rgb) {
      return (u, a, t) -> {
         float band = (float)Math.exp(-Math.pow((a - 0.35F) / 0.18F, 2.0));
         return mixc(shade(rgb, 0.85F + 0.25F * a), 16777215, 0.18F * band);
      };
   }

   private static int polish(int rgb, float u, float a, float shine) {
      int c = shade(rgb, 0.8F + 0.32F * a);
      float band = (float)Math.exp(-Math.pow((a - 0.6F) / 0.11F, 2.0));
      c = mixc(c, 16777215, shine * band);
      if (u > 0.06F && u < 0.62F && a > 0.16F && a < 0.3F) {
         c = shade(c, 0.55F);
      }

      if (a < 0.04F) {
         c = mixc(c, 16777215, 0.18F);
      }

      if (a > 0.93F) {
         c = mixc(c, 16777215, 0.35F);
      }

      return c;
   }

   private static int brushed(int rgb, float u, float a) {
      float streak = 0.94F + 0.06F * (float)Math.sin(u * 160.0F + a * 3.0F);
      return polish(shade(rgb, streak), u, a, 0.24F);
   }

   private static int gradient(float u, int c0, int c1, int c2) {
      return u < 0.5F ? mixc(c0, c1, u * 2.0F) : mixc(c1, c2, (u - 0.5F) * 2.0F);
   }

   static int shade(int rgb, float k) {
      int r = Math.min(255, Math.round((rgb >> 16 & 0xFF) * k));
      int g = Math.min(255, Math.round((rgb >> 8 & 0xFF) * k));
      int b = Math.min(255, Math.round((rgb & 0xFF) * k));
      return r << 16 | g << 8 | b;
   }

   static int mixc(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int r = Math.round((a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
      int g = Math.round((a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t);
      int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
      return r << 16 | g << 8 | bl;
   }

   private static float smooth(float e0, float e1, float x) {
      float t = Math.max(0.0F, Math.min(1.0F, (x - e0) / (e1 - e0)));
      return t * t * (3.0F - 2.0F * t);
   }

   private static float hash(int x, int y) {
      int h = x * 374761393 + y * 668265263;
      h = (h ^ h >>> 13) * 1274126177;
      return ((h ^ h >>> 16) & 16777215) / 1.6777215E7F;
   }

   private static float noise(float x, float y) {
      int ix = (int)Math.floor(x);
      int iy = (int)Math.floor(y);
      float fx = x - ix;
      float fy = y - iy;
      fx = fx * fx * (3.0F - 2.0F * fx);
      fy = fy * fy * (3.0F - 2.0F * fy);
      float a = hash(ix, iy);
      float b = hash(ix + 1, iy);
      float c = hash(ix, iy + 1);
      float d = hash(ix + 1, iy + 1);
      return a + (b - a) * fx + (c + (d - c) * fx - (a + (b - a) * fx)) * fy;
   }

   private static float[][] bladeSection(float w, float tb) {
      float te = Math.min(0.025F, tb);
      return new float[][]{
         {0.0F, tb * 0.8F},
         {0.06F * w, tb},
         {0.3F * w, tb},
         {0.52F * w, tb * 0.96F},
         {0.7F * w, tb * 0.66F},
         {0.86F * w, tb * 0.3F},
         {w, te},
         {w, -te},
         {0.86F * w, -tb * 0.3F},
         {0.7F * w, -tb * 0.66F},
         {0.52F * w, -tb * 0.96F},
         {0.3F * w, -tb},
         {0.06F * w, -tb},
         {0.0F, -tb * 0.8F}
      };
   }

   private static KnifeMesh blade(KnifeMesh mesh, float y0, float y1, KnifeModels.F spine, KnifeModels.F edge, KnifeModels.F th, KnifeMesh.Shader shader) {
      List<KnifeMesh.Station> st = new ArrayList<>();
      int n = 64;

      for (int i = 0; i <= n; i++) {
         float s = (float)i / n;
         float sx = spine.at(s);
         float ex = edge.at(s);
         st.add(new KnifeMesh.Station(sx, y0 + (y1 - y0) * s, 1.0F, 0.0F, s, bladeSection(Math.max(0.001F, ex - sx), th.at(s))));
      }

      return mesh.sweep(st, shader, true, false);
   }

   private static KnifeMesh grip(KnifeMesh mesh, float x, float y0, float y1, KnifeModels.F width, float depth, float chamfer, KnifeMesh.Shader shader) {
      List<KnifeMesh.Station> st = new ArrayList<>();
      int n = 40;

      for (int i = 0; i <= n; i++) {
         float s = (float)i / n;
         st.add(new KnifeMesh.Station(x, y0 + (y1 - y0) * s, 1.0F, 0.0F, s, KnifeMesh.roundedRect(width.at(s), depth, chamfer)));
      }

      return mesh.sweep(st, shader, true, true);
   }

   private static void pin(KnifeMesh mesh, float x, float y, float r, float halfDepth, KnifeMesh.Shader shader) {
      mesh.prism(KnifeMesh.circle(x, y, r, 14), -halfDepth, halfDepth, 0.5F, shader);
      mesh.prism(KnifeMesh.circle(x, y, r * 0.55F, 10), -halfDepth - 0.06F, halfDepth + 0.06F, 0.5F, shader);
   }

   private static void ring(KnifeMesh mesh, float cx, float cy, float r, float tube, KnifeMesh.Shader shader) {
      List<KnifeMesh.Station> st = new ArrayList<>();

      for (int i = 0; i <= 40; i++) {
         double a = (Math.PI * 2) * i / 40.0;
         float ca = (float)Math.cos(a);
         float sa = (float)Math.sin(a);
         st.add(new KnifeMesh.Station(cx + ca * r, cy + sa * r, ca, sa, i / 40.0F, KnifeMesh.circle(tube, 12)));
      }

      mesh.sweep(st, shader, false, false);
   }

   private static float ease(float t) {
      return t * t;
   }

   private static KnifeModels.Model build(String type, String finishName) {
      KnifeModels.Finish f = finish(finishName);
      KnifeModels.Model m = new KnifeModels.Model();
      switch (type) {
         case "Butterfly":
            butterfly(m, f);
            break;
         case "Karambit":
            karambit(m, f);
            break;
         case "Flip":
            flip(m, f);
            break;
         case "Bowie":
            bowie(m, f);
            break;
         default:
            bayonet(m, f);
      }

      return m;
   }

   private static void butterfly(KnifeModels.Model m, KnifeModels.Finish f) {
      KnifeMesh blade = new KnifeMesh();
      blade.box(0.05F, -0.8F, 0.05F, 1.75F, 0.34F, 0.14F, f.blade());
      blade(
         blade,
         0.0F,
         8.6F,
         s -> s < 0.7F ? -0.74F : -0.74F + 0.88F * ease((s - 0.7F) / 0.3F),
         s -> s < 0.48F ? 0.9F : 0.9F - 0.76F * ease((s - 0.48F) / 0.52F),
         s -> 0.17F * (1.0F - 0.45F * s),
         f.blade()
      );
      m.parts.put("blade", blade);
      m.parts.put("handleA", balisongHandle(f, false));
      m.parts.put("handleB", balisongHandle(f, true));
   }

   private static KnifeMesh balisongHandle(KnifeModels.Finish f, boolean latch) {
      KnifeMesh h = new KnifeMesh();
      float top = 0.5F;
      float bottom = -7.8F;
      float d = 0.52F;
      h.box(-0.5F, bottom, top, 0.3F, d, 0.1F, f.handle());
      h.box(0.5F, bottom, top, 0.3F, d, 0.1F, f.handle());
      h.box(0.0F, top - 0.75F, top, 1.3F, d, 0.18F, f.handle());
      h.box(0.0F, bottom, bottom + 0.7F, 1.3F, d, 0.18F, f.handle());

      for (float y : new float[]{-2.2F, -4.1F, -5.9F}) {
         h.box(0.0F, y - 0.16F, y + 0.16F, 1.1F, d * 0.9F, 0.08F, f.handle());
      }

      pin(h, 0.0F, 0.0F, 0.34F, d / 2.0F + 0.05F, f.accent());
      pin(h, 0.0F, bottom + 0.35F, 0.24F, d / 2.0F + 0.04F, f.accent());
      if (latch) {
         h.box(0.0F, bottom - 0.55F, bottom + 0.05F, 1.45F, 0.26F, 0.08F, f.accent());
         pin(h, 0.0F, bottom - 0.25F, 0.16F, 0.18F, f.accent());
      }

      return h;
   }

   private static void karambit(KnifeModels.Model m, KnifeModels.Finish f) {
      KnifeMesh k = new KnifeMesh();
      float cx = 4.6F;
      float cy = 0.6F;
      float r = 4.6F;
      List<KnifeMesh.Station> st = new ArrayList<>();
      int n = 64;

      for (int i = 0; i <= n; i++) {
         float s = (float)i / n;
         double th = Math.toRadians(180.0F - 88.0F * s);
         float px = cx + r * (float)Math.cos(th);
         float py = cy + r * (float)Math.sin(th);
         float ax = (cx - px) / r;
         float ay = (cy - py) / r;
         float w = Math.max(0.001F, 1.95F * (float)Math.pow(1.0F - s, 0.72));
         st.add(new KnifeMesh.Station(px, py, ax, ay, s, bladeSection(w, 0.22F * (1.0F - 0.5F * s))));
      }

      k.sweep(st, f.blade(), true, false);

      for (int i = 0; i < 6; i++) {
         float s = 0.04F + i * 0.035F;
         double th = Math.toRadians(180.0F - 88.0F * s);
         float px = cx + (r + 0.05F) * (float)Math.cos(th);
         float py = cy + (r + 0.05F) * (float)Math.sin(th);
         float ox = (float)Math.cos(th) * 0.22F;
         float oy = (float)Math.sin(th) * 0.22F;
         k.prism(new float[][]{{px - oy * 0.4F, py + ox * 0.4F}, {px + ox, py + oy}, {px + oy * 0.4F, py - ox * 0.4F}}, -0.16F, 0.16F, s, f.blade());
      }

      List<KnifeMesh.Station> hs = new ArrayList<>();

      for (int i = 0; i <= 30; i++) {
         float s = i / 30.0F;
         float y = -5.0F + 5.7F * s;
         float x = 0.95F - 0.7F * (1.0F - s) * (1.0F - s);
         float w = 2.05F + 0.15F * (float)Math.sin(s * Math.PI);
         hs.add(new KnifeMesh.Station(x, y, 1.0F, 0.0F, s, KnifeMesh.roundedRect(w, 1.1F, 0.4F)));
      }

      k.sweep(hs, f.handle(), true, true);
      pin(k, 0.9F, -1.0F, 0.22F, 0.6F, f.accent());
      pin(k, 0.55F, -3.6F, 0.22F, 0.6F, f.accent());
      ring(k, 0.25F, -6.42F, 1.42F, 0.44F, f.accent());
      ring(k, 0.25F, -6.42F, 1.0F, 0.12F, f.handle());
      m.parts.put("body", k);
   }

   private static void bayonet(KnifeModels.Model m, KnifeModels.Finish f) {
      KnifeMesh k = new KnifeMesh();
      KnifeModels.F spine = s -> s < 0.68F ? -0.85F : -0.85F + 1.05F * ease((s - 0.68F) / 0.32F);
      blade(
         k, 0.2F, 11.0F, spine, s -> s < 0.55F ? 1.15F : 1.15F - 0.95F * (float)Math.pow((s - 0.55F) / 0.45F, 1.6F), s -> 0.21F * (1.0F - 0.5F * s), f.blade()
      );

      for (int i = 0; i < 9; i++) {
         float y = 1.6F + i * 0.48F;
         float x = -0.85F;
         k.prism(new float[][]{{x + 0.05F, y}, {x - 0.38F, y + 0.12F}, {x + 0.05F, y + 0.4F}}, -0.13F, 0.13F, (y - 0.2F) / 10.8F, f.blade());
      }

      k.box(0.3F, -0.7F, 0.2F, 3.4F, 0.8F, 0.2F, f.accent());
      ring(k, -1.75F, -0.25F, 0.62F, 0.2F, f.accent());
      grip(k, 0.15F, -7.2F, -0.7F, s -> 1.82F + 0.16F * (float)Math.pow(Math.abs(Math.sin(s * Math.PI * 7.0)), 3.0), 1.18F, 0.4F, f.handle());
      k.box(0.15F, -8.1F, -7.2F, 2.1F, 1.32F, 0.42F, f.accent());
      k.prism(KnifeMesh.circle(0.15F, -7.65F, 0.28F, 12), 0.6F, 0.82F, 0.5F, f.accent());
      m.parts.put("body", k);
   }

   private static void flip(KnifeModels.Model m, KnifeModels.Finish f) {
      KnifeMesh handle = new KnifeMesh();
      grip(handle, 0.1F, -7.0F, 0.15F, s -> 1.9F - 0.16F * (float)Math.sin(s * Math.PI), 0.82F, 0.3F, f.handle());

      for (float z : new float[]{0.47F, -0.47F}) {
         handle.prism(new float[][]{{-0.87F, -7.0F}, {1.07F, -7.0F}, {1.07F, 0.2F}, {-0.87F, 0.2F}}, z - 0.05F, z + 0.05F, 0.5F, f.accent());
      }

      handle.box(0.1F, -0.1F, 0.95F, 2.0F, 1.08F, 0.48F, f.accent());
      pin(handle, 0.1F, 0.35F, 0.4F, 0.6F, f.accent());
      pin(handle, 0.1F, -6.4F, 0.24F, 0.55F, f.accent());
      handle.box(0.1F, -7.6F, -7.0F, 1.7F, 1.0F, 0.3F, f.accent());
      KnifeMesh blade = new KnifeMesh();
      blade.box(0.1F, -0.3F, 0.5F, 1.65F, 0.3F, 0.1F, f.blade());
      blade.prism(new float[][]{{0.85F, -0.2F}, {1.7F, -0.55F}, {1.6F, 0.2F}, {0.9F, 0.35F}}, -0.13F, 0.13F, 0.0F, f.blade());
      blade(
         blade,
         0.5F,
         8.1F,
         s -> s < 0.6F ? -0.72F : -0.72F + 0.9F * ease((s - 0.6F) / 0.4F),
         s -> s < 0.45F ? 0.92F : 0.92F - 0.74F * (float)Math.pow((s - 0.45F) / 0.55F, 1.5),
         s -> 0.18F * (1.0F - 0.45F * s),
         f.blade()
      );
      blade.prism(KnifeMesh.circle(-0.35F, 1.2F, 0.22F, 12), -0.4F, 0.4F, 0.1F, f.accent());
      m.parts.put("handle", handle);
      m.parts.put("blade", blade);
   }

   private static void bowie(KnifeModels.Model m, KnifeModels.Finish f) {
      KnifeMesh k = new KnifeMesh();
      blade(
         k,
         0.0F,
         11.4F,
         sx -> sx < 0.62F ? -1.0F : -1.0F + 1.4F * (float)Math.pow((sx - 0.62F) / 0.38F, 0.9F),
         sx -> sx < 0.45F ? 1.42F : 1.42F - 1.02F * (float)Math.pow((sx - 0.45F) / 0.55F, 2.2F),
         sx -> 0.25F * (1.0F - 0.5F * sx),
         f.blade()
      );
      k.box(0.2F, -0.75F, 0.05F, 2.6F, 0.95F, 0.3F, f.accent());

      for (int side = -1; side <= 1; side += 2) {
         List<KnifeMesh.Station> q = new ArrayList<>();

         for (int i = 0; i <= 10; i++) {
            float s = i / 10.0F;
            float x = 0.2F + side * (1.3F + s * 1.5F);
            float y = -0.35F + s * s * 0.9F;
            q.add(new KnifeMesh.Station(x, y, 0.0F, 1.0F, s, KnifeMesh.roundedRect(0.6F - s * 0.2F, 0.85F - s * 0.25F, 0.2F)));
         }

         k.sweep(q, f.accent(), true, true);
      }

      KnifeMesh.Shader leather = (u, a, t) -> {
         boolean dark = (int)(u * 16.0F) % 2 == 0;
         int base = dark ? 4861466 : 7029798;
         if ((int)(u * 16.0F) % 8 == 7) {
            base = 1709586;
         }

         return shade(base, 0.85F + 0.2F * a);
      };
      grip(k, 0.2F, -7.0F, -0.75F, sx -> 2.05F - 0.3F * Math.max(0.0F, (float)Math.sin(sx * Math.PI * 4.0)), 1.25F, 0.42F, leather);
      k.box(0.2F, -7.9F, -7.0F, 2.45F, 1.5F, 0.5F, f.accent());
      pin(k, 0.2F, -7.45F, 0.26F, 0.8F, f.accent());
      m.parts.put("body", k);
   }

   private interface F {
      float at(float var1);
   }

   private record Finish(KnifeMesh.Shader blade, KnifeMesh.Shader handle, KnifeMesh.Shader accent) {
   }

   public static final class Model {
      public final Map<String, KnifeMesh> parts = new HashMap<>();
   }
}
