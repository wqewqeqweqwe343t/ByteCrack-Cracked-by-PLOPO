package dev.byteplus.render;

import dev.byteplus.module.modules.visuals.CosmeticsModule;
import dev.byteplus.render.knife.KnifeMesh;
import dev.byteplus.util.Theme;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_10034;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_7833;

public final class CosmeticsRenderer {
   private static final class_2960 WHITE = class_2960.method_60655("byteplus", "textures/misc/white.png");
   private static final class_1921 SOLID = class_12249.method_75994(WHITE);
   private static final class_1921 GLOW = class_12249.method_76000(WHITE);
   private static final int FULL_BRIGHT = 15728880;
   private static final Map<String, KnifeMesh> CACHE = new HashMap<>();
   private static float flapPhase;
   private static float smoothSpeed;
   private static float lastAge = Float.NaN;

   private CosmeticsRenderer() {
   }

   public static void render(CosmeticsModule m, class_10034 state, class_4587 pose, class_11659 collector) {
      float t = state.field_53328 / 20.0F;
      float height = state.field_53330 * 16.0F;
      pose.method_22903();
      pose.method_22907(class_7833.field_40716.rotationDegrees(180.0F - state.field_53446));
      pose.method_22905(0.0625F, 0.0625F, 0.0625F);
      if (m.halo.isOn()) {
         int col = haloColor(m);
         pose.method_22903();
         pose.method_46416(0.0F, height + 3.2F + (float)Math.sin(t * 2.1) * 0.35F, 0.0F);
         pose.method_22907(class_7833.field_40714.rotationDegrees(80.0F));
         pose.method_22907(class_7833.field_40718.rotationDegrees(t * 25.0F));
         KnifeMesh ring = CACHE.computeIfAbsent("halo|" + col, k -> torus(4.4F, 0.5F, col));
         KnifeMesh glow = CACHE.computeIfAbsent("haloglow|" + col, k -> torus(4.4F, 1.3F, col));
         collector.method_73483(pose, SOLID, (p, vc) -> ring.emit(p, vc, 15728880));
         collector.method_73483(pose, GLOW, (p, vc) -> glow.emit(p, vc, 15728880, 0.28F, -1.0F, 0.0F));
         pose.method_22909();
      }

      String wings = m.wings.get();
      boolean flatPose = state.field_53411 || state.field_53412 || state.field_53403 > 0.15F;
      if (!wings.equals("None") && !flatPose) {
         float shoulder = height * 0.74F - (state.field_53410 ? 3.5F : 0.0F);
         float age = state.field_53328;
         float dt = Float.isNaN(lastAge) ? 0.0F : Math.max(0.0F, Math.min(0.25F, (age - lastAge) / 20.0F));
         lastAge = age;
         float target = Math.min(1.0F, state.field_53451 * 1.4F);
         smoothSpeed = smoothSpeed + (target - smoothSpeed) * (1.0F - (float)Math.exp(-dt * 5.0F));
         float speed = smoothSpeed;
         flapPhase = (flapPhase + dt * (2.2F + speed * 4.5F)) % 6283.1855F;
         float flap = (float)Math.sin(flapPhase) * (10.0F + speed * 14.0F);

         for (int side = -1; side <= 1; side += 2) {
            pose.method_22903();
            pose.method_46416(side * 1.2F, shoulder, 2.6F);
            if (side < 0) {
               pose.method_22905(-1.0F, 1.0F, 1.0F);
            }

            pose.method_22907(class_7833.field_40716.rotationDegrees(-28.0F - flap * 0.6F));
            pose.method_22907(class_7833.field_40718.rotationDegrees(8.0F + flap * 0.35F));
            String key = "wing|" + wings + "|" + (Theme.ACCENT & 16777215);
            KnifeMesh wing = CACHE.computeIfAbsent(key, k -> wing(wings));
            int light = !wings.equals("Byte+") && !wings.equals("Crystal") ? state.field_61820 : 15728880;
            collector.method_73483(pose, SOLID, (p, vc) -> wing.emit(p, vc, light));
            pose.method_22909();
         }
      }

      pose.method_22909();
   }

   private static int haloColor(CosmeticsModule m) {
      return switch ((String)m.haloColor.get()) {
         case "Gold" -> 16765773;
         case "White" -> 16053503;
         case "Rainbow" -> Color.HSBtoRGB((float)(System.currentTimeMillis() % 6000L) / 6000.0F, 0.6F, 1.0F) & 16777215;
         default -> Theme.ACCENT & 16777215;
      };
   }

   private static KnifeMesh torus(float radius, float tube, int rgb) {
      List<KnifeMesh.Station> ring = new ArrayList<>();
      int seg = 40;

      for (int i = 0; i <= seg; i++) {
         double a = (Math.PI * 2) * i / seg;
         float ca = (float)Math.cos(a);
         float sa = (float)Math.sin(a);
         ring.add(new KnifeMesh.Station(ca * radius, sa * radius, ca, sa, (float)i / seg, KnifeMesh.circle(tube, 12)));
      }

      return new KnifeMesh().sweep(ring, (u, ax, tt) -> rgb, false, false);
   }

   private static KnifeMesh wing(String style) {
      int root;
      int tip;
      int arm;
      switch (style) {
         case "Demon":
            root = 1706530;
            tip = 11539760;
            arm = 2756405;
            break;
         case "Crystal":
            root = 6281471;
            tip = 15137791;
            arm = 10480127;
            break;
         case "Byte+":
            root = 1709842;
            tip = Theme.ACCENT & 16777215;
            arm = 2762012;
            break;
         default:
            root = 15920870;
            tip = 16770984;
            arm = 16777215;
      }

      int fRoot = root;
      int fTip = tip;
      int fArm = arm;
      KnifeMesh mesh = new KnifeMesh();
      List<KnifeMesh.Station> armSt = new ArrayList<>();

      for (int i = 0; i <= 16; i++) {
         float s = i / 16.0F;
         float x = s * 15.0F;
         float y = (float)Math.sin(s * Math.PI * 0.9) * 5.5F + s * 1.5F;
         float dx = 15.0F;
         float dy = (float)(Math.cos(s * Math.PI * 0.9) * Math.PI * 0.9 * 5.5) + 1.5F;
         float len = (float)Math.sqrt(dx * dx + dy * dy);
         float w = 1.6F * (1.0F - s * 0.55F);
         armSt.add(new KnifeMesh.Station(x, y, -dy / len, dx / len, s, KnifeMesh.roundedRect(w, 0.9F * (1.0F - s * 0.4F), 0.35F)));
      }

      mesh.sweep(armSt, (u, a, t) -> fArm, true, true);
      int count = 9;

      for (int f = 0; f < count; f++) {
         float s = (f + 0.5F) / count;
         float ax = s * 15.0F;
         float ay = (float)Math.sin(s * Math.PI * 0.9) * 5.5F + s * 1.5F;
         float len = 6.5F + s * 9.0F;
         double ang = Math.toRadians(-100.0F + s * 55.0F);
         float fx = (float)Math.cos(ang);
         float fy = (float)Math.sin(ang);
         float px = -fy;
         float py = fx;
         List<KnifeMesh.Station> st = new ArrayList<>();

         for (int i = 0; i <= 10; i++) {
            float k = i / 10.0F;
            float bend = (float)Math.sin(k * Math.PI) * 0.6F;
            float cx = ax + fx * len * k + px * bend;
            float cy = ay + fy * len * k + py * bend;
            float w = 2.6F * (float)Math.sin(Math.PI * Math.min(1.0F, 0.15F + k * 0.85F)) * (0.75F + s * 0.4F) + 0.05F;
            st.add(new KnifeMesh.Station(cx, cy, px, py, k, KnifeMesh.roundedRect(w, 0.22F, 0.1F)));
         }

         float layer = 0.18F * (f % 2 == 0 ? 1 : -1);
         float shade = 0.85F + 0.15F * (f % 3) / 2.0F;
         KnifeMesh.Shader sh = (u, a, t) -> {
            int c = mix(fRoot, fTip, (float)Math.pow(u, 1.4));
            if (Math.abs(a - 0.5F) < 0.06F) {
               c = mix(c, 16777215, 0.25F);
            }

            return mul(c, shade + (t > 0.0F ? 0.06F : -0.06F));
         };
         mesh.sweep(offsetZ(st, layer), sh, true, false);
      }

      return mesh;
   }

   private static List<KnifeMesh.Station> offsetZ(List<KnifeMesh.Station> st, float dz) {
      List<KnifeMesh.Station> out = new ArrayList<>();

      for (KnifeMesh.Station s : st) {
         float[][] sec = new float[s.section().length][];

         for (int i = 0; i < sec.length; i++) {
            sec[i] = new float[]{s.section()[i][0], s.section()[i][1] + dz};
         }

         out.add(new KnifeMesh.Station(s.x(), s.y(), s.ax(), s.ay(), s.u(), sec));
      }

      return out;
   }

   private static int mix(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int r = Math.round((a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
      int g = Math.round((a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t);
      int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
      return r << 16 | g << 8 | bl;
   }

   private static int mul(int c, float k) {
      int r = Math.min(255, Math.round((c >> 16 & 0xFF) * k));
      int g = Math.min(255, Math.round((c >> 8 & 0xFF) * k));
      int b = Math.min(255, Math.round((c & 0xFF) * k));
      return r << 16 | g << 8 | b;
   }
}
