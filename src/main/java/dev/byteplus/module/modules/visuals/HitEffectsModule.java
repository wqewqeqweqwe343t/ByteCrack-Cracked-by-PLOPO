package dev.byteplus.module.modules.visuals;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.render.Icons;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.util.Theme;
import dev.byteplus.util.UiSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1538;
import net.minecraft.class_2246;
import net.minecraft.class_2388;
import net.minecraft.class_2390;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3730;
import net.minecraft.class_746;

public class HitEffectsModule extends Module {
   public final ModeSetting hitEffect = this.add(new ModeSetting("Hit Effect", "Sparks", "Sparks", "Crit", "Hearts", "Blood", "Magic", "None"));
   public final ModeSetting killEffect = this.add(
      new ModeSetting("Kill Effect", "Lightning", "Lightning", "Firework", "Totem", "Blood Burst", "Soul Flames", "None")
   );
   public final BooleanSetting damageNumbers = this.add(new BooleanSetting("Damage Numbers", true));
   public final BooleanSetting killBanner = this.add(new BooleanSetting("Kill Banner", true));
   public final BooleanSetting screenFx = this.add(new BooleanSetting("Glow Effects", true));
   public final BooleanSetting hitSound = this.add(new BooleanSetting("Hit Sound", true));
   public final BooleanSetting killSound = this.add(new BooleanSetting("Kill Sound", true));
   private static HitEffectsModule instance;
   private static int boltId = -1000000;
   private class_1309 lastTarget;
   private long lastHitMs;
   private float lastHealthFrac = 1.0F;
   private final Random random = new Random();
   private final List<HitEffectsModule.Fx> fx = new ArrayList<>();
   private final Map<Integer, HitEffectsModule.Tracked> tracked = new HashMap<>();
   private final List<HitEffectsModule.DamageNumber> numbers = new ArrayList<>();
   private String bannerName;
   private long bannerStart;
   private long killMarkerStart;
   private int streak;
   private long lastKillMs;

   public HitEffectsModule() {
      super("Hit Effects", "Impact flashes, floating damage numbers, kill banners with streaks, lightning and shockwaves.", Category.VISUALS);
      instance = this;
   }

   @Override
   public boolean showInArrayList() {
      return false;
   }

   @Override
   protected void onDisable() {
      this.fx.clear();
      this.numbers.clear();
      this.tracked.clear();
      this.lastTarget = null;
      this.bannerName = null;
   }

   public static void onAttack(class_1297 target) {
      CustomCrosshairModule.onHit();
      if (instance != null && instance.isEnabled() && target instanceof class_1309 living) {
         instance.hit(living);
      }
   }

   private boolean isCrit() {
      class_746 p = mc.field_1724;
      return p != null && p.field_6017 > 0.0 && !p.method_24828() && !p.method_6101() && !p.method_5799() && !p.method_5765();
   }

   private void hit(class_1309 e) {
      this.lastTarget = e;
      this.lastHitMs = System.currentTimeMillis();
      this.lastHealthFrac = e.method_6063() > 0.0F ? e.method_6032() / e.method_6063() : 1.0F;
      boolean crit = this.isCrit();
      HitEffectsModule.Tracked tr = this.tracked.get(e.method_5628());
      if (tr != null && tr.entity == e) {
         tr.crit = crit;
         tr.until = System.currentTimeMillis() + 1500L;
      } else {
         this.tracked.put(e.method_5628(), new HitEffectsModule.Tracked(e, crit));
      }

      double x = e.method_23317();
      double y = e.method_23318() + e.method_17682() * 0.6;
      double z = e.method_23321();
      switch ((String)this.hitEffect.get()) {
         case "Sparks":
            this.burst(class_2398.field_29644, x, y, z, 22, 0.25);
            this.burst(class_2398.field_11205, x, y, z, 8, 0.3);
            break;
         case "Crit":
            this.burst(class_2398.field_11205, x, y, z, 26, 0.35);
            break;
         case "Hearts":
            this.burst(class_2398.field_11201, x, y + 0.3, z, 6, 0.12);
            break;
         case "Blood":
            this.burst(new class_2388(class_2398.field_11217, class_2246.field_10002.method_9564()), x, y, z, 26, 0.22);
            this.burst(new class_2390(10485760, 1.4F), x, y, z, 12, 0.15);
            break;
         case "Magic":
            this.burst(class_2398.field_11208, x, y, z, 28, 0.35);
      }

      if (this.screenFx.isOn()) {
         this.fx.add(new HitEffectsModule.Fx(0, x, y, z, System.nanoTime(), this.random.nextFloat()));
      }

      if (this.hitSound.isOn()) {
         UiSounds.play(UiSounds.Kind.CLICK_ON, 0.6F);
      }
   }

   @Override
   public void onTick() {
      long nowMs = System.currentTimeMillis();
      Iterator<HitEffectsModule.Tracked> ti = this.tracked.values().iterator();

      while (ti.hasNext()) {
         HitEffectsModule.Tracked t = ti.next();
         float h = t.entity.method_6032();
         if (h < t.health - 0.01F && this.damageNumbers.isOn()) {
            float dmg = t.health - h;
            String s = Math.abs(dmg - Math.round(dmg)) < 0.05F ? String.valueOf(Math.round(dmg)) : String.format(Locale.ROOT, "%.1f", dmg);
            this.numbers
               .add(
                  new HitEffectsModule.DamageNumber(
                     s,
                     t.entity.method_23317(),
                     t.entity.method_23318() + t.entity.method_17682() * 0.85,
                     t.entity.method_23321(),
                     System.nanoTime(),
                     t.crit,
                     (this.random.nextFloat() - 0.5F) * 0.6F
                  )
               );
            t.crit = false;
         }

         t.health = h;
         if (nowMs > t.until || t.entity.method_31481()) {
            ti.remove();
         }
      }

      if (this.lastTarget != null) {
         long since = nowMs - this.lastHitMs;
         if (since > 2500L) {
            this.lastTarget = null;
         } else {
            boolean dead = this.lastTarget.method_29504()
               || this.lastTarget.field_6213 > 0
               || this.lastTarget.method_31481() && since < 1500L && this.lastHealthFrac <= 0.35F;
            if (dead) {
               this.kill(this.lastTarget);
               this.lastTarget = null;
            }
         }
      }
   }

   private void kill(class_1309 e) {
      double x = e.method_23317();
      double y = e.method_23318();
      double z = e.method_23321();
      switch ((String)this.killEffect.get()) {
         case "Lightning":
            class_1538 bolt = (class_1538)class_1299.field_6112.method_5883(mc.field_1687, class_3730.field_16461);
            if (bolt != null) {
               bolt.method_5838(boltId--);
               bolt.method_29498(true);
               bolt.method_5814(x, y, z);
               mc.field_1687.method_53875(bolt);
            }
            break;
         case "Firework":
            for (int i = 0; i < 60; i++) {
               double a = this.random.nextDouble() * Math.PI * 2.0;
               double b = this.random.nextDouble() * Math.PI;
               double vx = Math.sin(b) * Math.cos(a) * 0.3;
               double vy = Math.cos(b) * 0.3 + 0.08;
               double vz = Math.sin(b) * Math.sin(a) * 0.3;
               this.particle(class_2398.field_11248, x, y + 1.3, z, vx, vy, vz);
            }

            if (this.killSound.isOn()) {
               mc.field_1687.method_8486(x, y, z, class_3417.field_15188, class_3419.field_15248, 1.0F, 1.0F, false);
            }
            break;
         case "Totem":
            mc.field_1713.method_3051(e, class_2398.field_11220, 30);
            this.burst(class_2398.field_11220, x, y + 1.0, z, 50, 0.45);
            if (this.killSound.isOn()) {
               mc.field_1687.method_8486(x, y, z, class_3417.field_14931, class_3419.field_15248, 0.6F, 1.2F, false);
            }
            break;
         case "Blood Burst":
            this.burst(new class_2388(class_2398.field_11217, class_2246.field_10002.method_9564()), x, y + 1.0, z, 70, 0.45);
            this.burst(new class_2390(11534336, 1.8F), x, y + 1.0, z, 40, 0.35);
            break;
         case "Soul Flames":
            for (int i = 0; i < 48; i++) {
               double a = (Math.PI * 2) * i / 48.0;
               this.particle(class_2398.field_22246, x + Math.cos(a) * 0.7, y + 0.1, z + Math.sin(a) * 0.7, 0.0, 0.06 + this.random.nextDouble() * 0.08, 0.0);
            }
      }

      if (this.screenFx.isOn()) {
         this.fx.add(new HitEffectsModule.Fx(1, x, y, z, System.nanoTime(), this.random.nextFloat()));
      }

      long nowMs = System.currentTimeMillis();
      this.streak = nowMs - this.lastKillMs < 6000L ? this.streak + 1 : 1;
      this.lastKillMs = nowMs;
      this.bannerName = e.method_5477().getString();
      this.bannerStart = System.nanoTime();
      this.killMarkerStart = System.nanoTime();
      if (this.killSound.isOn() && !this.killEffect.is("Firework") && !this.killEffect.is("Totem") && !this.killEffect.is("None")) {
         mc.field_1687.method_8486(x, y, z, class_3417.field_14627, class_3419.field_15248, 0.8F, 0.6F, false);
      }
   }

   private void particle(class_2394 p, double x, double y, double z, double vx, double vy, double vz) {
      mc.field_1687.method_8466(p, true, true, x, y, z, vx, vy, vz);
   }

   private void burst(class_2394 p, double x, double y, double z, int n, double speed) {
      for (int i = 0; i < n; i++) {
         this.particle(
            p, x, y, z, (this.random.nextDouble() - 0.5) * speed * 2.0, this.random.nextDouble() * speed, (this.random.nextDouble() - 0.5) * speed * 2.0
         );
      }
   }

   private static float clamp01(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   private static float outCubic(float t) {
      float u = 1.0F - clamp01(t);
      return 1.0F - u * u * u;
   }

   private static float outBack(float t) {
      t = clamp01(t);
      float c1 = 2.2F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(t - 1.0F, 3.0) + c1 * (float)Math.pow(t - 1.0F, 2.0);
   }

   @Override
   public void onRenderWorld() {
      long now = System.nanoTime();
      int rgb = Theme.ACCENT & 16777215;

      for (HitEffectsModule.Fx f : this.fx) {
         if (f.kind() == 1) {
            float t = (float)(now - f.start()) / 1.0E9F;
            if (!(t > 1.2F)) {
               float p = t / 1.2F;
               float ease = 1.0F - (1.0F - p) * (1.0F - p) * (1.0F - p);
               float r = 0.4F + ease * 3.2F;
               int alpha = (int)(230.0F * (1.0F - p));
               int seg = 48;

               for (int i = 0; i < seg; i++) {
                  double a0 = (Math.PI * 2) * i / seg;
                  double a1 = (Math.PI * 2) * (i + 1) / seg;
                  Esp.line(
                     f.x() + Math.cos(a0) * r,
                     f.y() + 0.05,
                     f.z() + Math.sin(a0) * r,
                     f.x() + Math.cos(a1) * r,
                     f.y() + 0.05,
                     f.z() + Math.sin(a1) * r,
                     rgb,
                     alpha
                  );
                  float r2 = r * 0.7F;
                  Esp.line(
                     f.x() + Math.cos(a0) * r2,
                     f.y() + 0.05,
                     f.z() + Math.sin(a0) * r2,
                     f.x() + Math.cos(a1) * r2,
                     f.y() + 0.05,
                     f.z() + Math.sin(a1) * r2,
                     rgb,
                     alpha / 2
                  );
               }

               float beam = 0.25F * (1.0F - p);
               Esp.box(f.x() - beam, f.y(), f.z() - beam, f.x() + beam, f.y() + 6.0F * ease + 1.0, f.z() + beam, rgb, (int)(90.0F * (1.0F - p)), 0);
            }
         }
      }
   }

   @Override
   public void onRenderScreen(int fbW, int fbH, float guiScale) {
      if (mc.field_1724 != null) {
         long now = System.nanoTime();
         int accent = Theme.ACCENT;
         Iterator<HitEffectsModule.Fx> it = this.fx.iterator();

         while (it.hasNext()) {
            HitEffectsModule.Fx f = it.next();
            float t = (float)(now - f.start()) / 1.0E9F;
            if (t > (f.kind() == 0 ? 0.5F : 1.2F)) {
               it.remove();
            } else if (f.kind() == 0) {
               float[] p = Esp.project(f.x(), f.y(), f.z());
               if (p != null) {
                  double dist = Math.max(1.5, Math.sqrt(mc.field_1724.method_5649(f.x(), f.y(), f.z())));
                  float size = (float)(34.0 / dist) * guiScale;
                  float k = t / 0.5F;
                  float ease = outCubic(k);
                  float a = 1.0F - k;
                  Render2D.glow(p[0], p[1], size * (0.9F + 0.6F * ease), Render2D.withAlpha(accent, 0.55F * a * a));
                  Render2D.glow(p[0], p[1], size * 0.45F * (1.0F - k * 0.5F), Render2D.withAlpha(-1, 0.7F * a * a * a));
                  Render2D.circleOutline(
                     p[0], p[1], size * (0.25F + 1.05F * ease), Math.max(0.6F, 1.6F * guiScale / 2.0F * a), Render2D.withAlpha(accent, 0.85F * a)
                  );

                  for (int i = 0; i < 7; i++) {
                     double ang = (Math.PI * 2) * i / 7.0 + f.seed() * 6.0F + Math.sin(i * 12.9 + f.seed() * 30.0F) * 0.25;
                     float len = 0.45F + 0.35F * (float)Math.abs(Math.sin(i * 7.3 + f.seed() * 50.0F));
                     float in = size * (0.3F + 1.2F * ease);
                     float out = in + size * len * (1.0F - k * 0.7F);
                     float cx = (float)Math.cos(ang);
                     float cy = (float)Math.sin(ang);
                     Render2D.line(
                        p[0] + cx * in,
                        p[1] + cy * in,
                        p[0] + cx * out,
                        p[1] + cy * out,
                        Math.max(0.6F, 1.5F * guiScale / 2.0F * a),
                        Render2D.withAlpha(i % 2 == 0 ? -1 : accent, 0.95F * a)
                     );
                  }
               }
            }
         }

         Iterator<HitEffectsModule.DamageNumber> ni = this.numbers.iterator();

         while (ni.hasNext()) {
            HitEffectsModule.DamageNumber n = ni.next();
            float t = (float)(now - n.start()) / 1.0E9F;
            if (t > 1.15F) {
               ni.remove();
            } else {
               float rise = outCubic(t / 0.9F);
               float[] p = Esp.project(n.x() + n.drift() * rise * 0.6, n.y() + 0.75 * rise, n.z());
               if (p != null) {
                  double dist = Math.max(2.0, Math.sqrt(mc.field_1724.method_5649(n.x(), n.y(), n.z())));
                  float base = (float)Math.max(10.0, Math.min(22.0, 54.0 / dist)) * guiScale * (n.crit() ? 1.3F : 1.0F);
                  float pop = t < 0.14F ? 0.4F + 0.95F * outBack(t / 0.14F) : 1.35F - 0.35F * outCubic((t - 0.14F) / 0.2F);
                  float alpha = 1.0F - clamp01((t - 0.8F) / 0.35F);
                  float size = base * pop;
                  int main = n.crit() ? -15320 : -1;
                  int glowCol = n.crit() ? -30180 : accent;
                  Render2D.glow(p[0], p[1], size * 1.1F, Render2D.withAlpha(glowCol, 0.35F * alpha));
                  float o = Math.max(1.0F, size * 0.07F);
                  int outline = Render2D.withAlpha(-16777216, 0.75F * alpha);
                  Render2D.textCentered(n.text(), p[0] + o, p[1] + o, size, outline, 1);
                  Render2D.textCentered(n.text(), p[0] - o * 0.5F, p[1] + o, size, outline, 1);
                  Render2D.textCentered(n.text(), p[0], p[1], size, Render2D.withAlpha(main, alpha), 1);
                  if (n.crit()) {
                     float w = Render2D.textWidth(n.text(), size, 1);
                     float spin = t * 4.0F;
                     Icons.sparkle(
                        p[0] + w / 2.0F + size * 0.35F,
                        p[1] - size * 0.35F,
                        size * 0.22F * (1.0F + 0.2F * (float)Math.sin(spin)),
                        Render2D.withAlpha(-7558, alpha)
                     );
                  }
               }
            }
         }
      }
   }

   @Override
   public void onRenderOverlay(float sw, float sh, float dt) {
      long now = System.nanoTime();
      if (this.killMarkerStart != 0L) {
         float t = (float)(now - this.killMarkerStart) / 1.0E9F;
         if (t < 0.55F) {
            float k = t / 0.55F;
            float a = 1.0F - k * k;
            float cx = sw / 2.0F;
            float cy = sh / 2.0F;
            float in = 4.0F + 5.0F * outCubic(k);
            float out = in + 6.0F * (1.0F - k * 0.5F);

            for (int i = 0; i < 4; i++) {
               double ang = (Math.PI / 4) + (Math.PI / 2) * i;
               float dx = (float)Math.cos(ang);
               float dy = (float)Math.sin(ang);
               Render2D.line(cx + dx * in, cy + dy * in, cx + dx * out, cy + dy * out, 2.2F, Render2D.withAlpha(-16777216, 0.5F * a));
               Render2D.line(cx + dx * in, cy + dy * in, cx + dx * out, cy + dy * out, 1.4F, Render2D.withAlpha(-50373, a));
            }
         } else {
            this.killMarkerStart = 0L;
         }
      }

      if (this.killBanner.isOn() && this.bannerName != null) {
         float t = (float)(now - this.bannerStart) / 1.0E9F;
         float dur = 2.6F;
         if (t > dur) {
            this.bannerName = null;
         } else {
            float in = outBack(t / 0.32F);
            float out = clamp01((t - (dur - 0.35F)) / 0.35F);
            float alpha = clamp01(t / 0.12F) * (1.0F - out);

            String label = switch (this.streak) {
               case 1 -> "ELIMINATED";
               case 2 -> "DOUBLE KILL";
               case 3 -> "TRIPLE KILL";
               case 4 -> "QUAD KILL";
               default -> "RAMPAGE";
            };
            float nameSize = 11.5F;
            float labelSize = 6.2F;
            float nw = Render2D.textWidth(this.bannerName, nameSize, 1, 0.3F);
            float lw = Render2D.textWidth(label, labelSize, 1, 1.4F);
            float w = Math.max(nw, lw) + 66.0F;
            float h = 36.0F;
            float x = (sw - w) / 2.0F;
            float y = sh * 0.2F - (1.0F - in) * 10.0F - out * 6.0F;
            float s = 0.85F + 0.15F * in;
            Render2D.save();
            Render2D.globalAlpha(alpha);
            Render2D.translate(sw / 2.0F, y + h / 2.0F);
            Render2D.scale(s);
            Render2D.translate(-sw / 2.0F, -(y + h / 2.0F));
            int accent = Theme.ACCENT;
            int accent2 = Theme.HUD_ACCENT_2;
            Render2D.shadow(x, y, w, h, 9.0F, 16.0F, -1879048192);
            Render2D.shadow(x, y, w, h, 9.0F, 18.0F, Render2D.withAlpha(accent & 16777215 | 0xFF000000, 0.25F));
            Render2D.roundRect(x, y, w, h, 9.0F, -536081654);
            Render2D.radialRoundRect(x, y, w, h, 9.0F, x + 16.0F, y + h / 2.0F, 0.0F, w * 0.7F, Render2D.withAlpha(accent, 0.18F), 0);
            Render2D.gradientOutline(
               x + 0.3F, y + 0.3F, w - 0.6F, h - 0.6F, 9.0F, 0.8F, x, y, x + w, y + h, Render2D.withAlpha(accent, 0.85F), Render2D.withAlpha(accent2, 0.25F)
            );
            float bx = x + 19.0F;
            float by = y + h / 2.0F;
            float pulse = 1.0F + 0.08F * (float)Math.sin(t * 9.0F) * (1.0F - out);
            Render2D.glow(bx, by, 15.0F * pulse, Render2D.withAlpha(-50373, 0.45F));
            Render2D.circle(bx, by, 10.0F * pulse, -1560518);
            Render2D.line(bx - 3.8F, by - 3.8F, bx + 3.8F, by + 3.8F, 2.0F, -1);
            Render2D.line(bx - 3.8F, by + 3.8F, bx + 3.8F, by - 3.8F, 2.0F, -1);
            float tx = x + 37.0F;
            Render2D.text(label, tx, y + 6.5F, labelSize, Render2D.withAlpha(accent, 0.95F), 1, 1.4F);
            Render2D.textGradient(this.bannerName, tx, y + 16.0F, nameSize, -1, Theme.mix(accent, -1, 0.4F), 1, 0.3F);
            if (this.streak > 1) {
               String sx = "x" + this.streak;
               float sxw = Render2D.textWidth(sx, 7.5F, 1) + 9.0F;
               Render2D.roundRect(x + w - sxw - 8.0F, y + 10.0F, sxw, 16.0F, 5.0F, Render2D.withAlpha(accent, 0.9F));
               Render2D.textCentered(sx, x + w - 8.0F - sxw / 2.0F, y + 18.2F, 7.5F, -15396342, 1);
            }

            float life = 1.0F - clamp01(t / dur);
            Render2D.horizontalGradient(x + 9.0F, y + h - 2.6F, (w - 18.0F) * life, 1.4F, 0.7F, accent, accent2);
            Render2D.restore();
         }
      }
   }

   private record DamageNumber(String text, double x, double y, double z, long start, boolean crit, float drift) {
   }

   private record Fx(int kind, double x, double y, double z, long start, float seed) {
   }

   private static final class Tracked {
      final class_1309 entity;
      float health;
      long until;
      boolean crit;

      Tracked(class_1309 e, boolean crit) {
         this.entity = e;
         this.health = e.method_6032();
         this.crit = crit;
         this.until = System.currentTimeMillis() + 1500L;
      }
   }
}
