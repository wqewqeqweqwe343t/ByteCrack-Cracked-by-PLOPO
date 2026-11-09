package dev.byteplus.module.modules.visuals;

import dev.byteplus.mixin.MultiPlayerGameModeAccessor;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.render.Projection;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_265;
import net.minecraft.class_3726;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public class BlockOutlineModule extends Module {
   public final ModeSetting style = this.add(new ModeSetting("Style", "Neon", "Neon", "Gradient", "Corners", "Box"));
   public final BooleanSetting useAccent = this.add(new BooleanSetting("Use Accent", true));
   public final ColorSetting color = this.add(new ColorSetting("Color", -14040).visibleWhen(() -> !this.useAccentOn()));
   public final BooleanSetting fill = this.add(new BooleanSetting("Fill", true));
   public final NumberSetting fillOpacity = this.add(new NumberSetting("Fill Opacity", 38.0, 5.0, 160.0, 1.0));
   public final BooleanSetting miningProgress = this.add(new BooleanSetting("Mining Progress", true));
   public final BooleanSetting smooth = this.add(new BooleanSetting("Smooth", true));
   public static float devProgress = -1.0F;
   private double[] cur;
   private long lastNanos = System.nanoTime();
   private long lastScreenNanos = System.nanoTime();
   private float progress;
   private float shownProgress;
   private float ringAlpha;
   private double time;
   private static final int[][] EDGES = new int[][]{
      {0, 1, 0, 2},
      {2, 3, 1, 2},
      {4, 5, 0, 3},
      {6, 7, 1, 3},
      {0, 2, 2, 4},
      {1, 3, 2, 5},
      {4, 6, 3, 4},
      {5, 7, 3, 5},
      {0, 4, 0, 4},
      {1, 5, 0, 5},
      {2, 6, 1, 4},
      {3, 7, 1, 5}
   };

   public BlockOutlineModule() {
      super("Block Outline", "A glowing outline on the block you're looking at (neon, gradient, corners) with a mining fill.", Category.VISUALS);
   }

   private boolean useAccentOn() {
      return this.useAccent.isOn();
   }

   @Override
   public boolean showInArrayList() {
      return false;
   }

   private int baseColor() {
      return this.useAccent.isOn() ? Theme.ACCENT : this.color.argb();
   }

   @Override
   public void onRenderWorld() {
      long now = System.nanoTime();
      float dt = Math.min(0.1F, (float)(now - this.lastNanos) / 1.0E9F);
      this.lastNanos = now;
      this.time += dt;
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (mc.field_1765 instanceof class_3965 hit && hit.method_17783() == class_240.field_1332) {
            class_2338 pos = hit.method_17777();
            class_265 shape = mc.field_1687.method_8320(pos).method_26172(mc.field_1687, pos, class_3726.method_16195(mc.field_1724));
            if (shape.method_1110()) {
               this.cur = null;
            } else {
               class_238 b = shape.method_1107().method_996(pos).method_1014(0.002);
               if (Projection.isValid() && b.method_1014(0.05).method_1008(Projection.camX(), Projection.camY(), Projection.camZ())) {
                  this.cur = null;
               } else {
                  double[] target = new double[]{b.field_1323, b.field_1322, b.field_1321, b.field_1320, b.field_1325, b.field_1324};
                  if (this.cur != null && this.smooth.isOn()) {
                     double k = 1.0 - Math.exp(-dt * 22.0F);

                     for (int i = 0; i < 6; i++) {
                        this.cur[i] = this.cur[i] + (target[i] - this.cur[i]) * k;
                     }
                  } else {
                     this.cur = (double[])target.clone();
                  }

                  this.progress = devProgress >= 0.0F ? devProgress : 0.0F;
                  if (this.miningProgress.isOn() && mc.field_1761 != null && mc.field_1761.method_2923()) {
                     MultiPlayerGameModeAccessor acc = (MultiPlayerGameModeAccessor)mc.field_1761;
                     if (pos.equals(acc.byteplus$destroyBlockPos())) {
                        this.progress = Math.max(0.0F, Math.min(1.0F, acc.byteplus$destroyProgress()));
                     }
                  }

                  this.shownProgress = this.progress <= 0.001F ? 0.0F : Anim.approach(this.shownProgress, this.progress, 18.0F, dt);
                  int rgb = this.baseColor() & 16777215;
                  boolean lines = this.style.is("Box");
                  int fillA = this.fill.isOn() ? this.fillOpacity.getInt() : 0;
                  if (fillA > 0 || lines) {
                     Esp.box(this.cur[0], this.cur[1], this.cur[2], this.cur[3], this.cur[4], this.cur[5], rgb, fillA, lines ? 235 : 0);
                  }

                  if (this.shownProgress > 0.01F) {
                     double cx = (this.cur[0] + this.cur[3]) / 2.0;
                     double cy = (this.cur[1] + this.cur[4]) / 2.0;
                     double cz = (this.cur[2] + this.cur[5]) / 2.0;
                     double s = 0.15 + 0.85 * this.shownProgress;
                     double hx = (this.cur[3] - this.cur[0]) / 2.0 * s;
                     double hy = (this.cur[4] - this.cur[1]) / 2.0 * s;
                     double hz = (this.cur[5] - this.cur[2]) / 2.0 * s;
                     int mix = Theme.mix(this.baseColor(), -1, this.shownProgress * 0.45F) & 16777215;
                     Esp.box(cx - hx, cy - hy, cz - hz, cx + hx, cy + hy, cz + hz, mix, (int)(50.0F + 110.0F * this.shownProgress), 0);
                  }
               }
            }
         } else {
            this.cur = null;
            this.progress = 0.0F;
         }
      }
   }

   @Override
   public void onRenderScreen(int fbW, int fbH, float guiScale) {
      long now = System.nanoTime();
      float dt = Math.min(0.1F, (float)(now - this.lastScreenNanos) / 1.0E9F);
      this.lastScreenNanos = now;
      this.ringAlpha = Anim.approach(this.ringAlpha, this.shownProgress > 0.01F ? 1.0F : 0.0F, 10.0F, dt);
      if (this.cur != null && Projection.isValid()) {
         String st = this.style.get();
         if (!st.equals("Box")) {
            double cx = Projection.camX();
            double cy = Projection.camY();
            double cz = Projection.camZ();
            boolean[] vis = new boolean[]{cy < this.cur[1], cy > this.cur[4], cz < this.cur[2], cz > this.cur[5], cx < this.cur[0], cx > this.cur[3]};
            if (!vis[0] && !vis[1] && !vis[2] && !vis[3] && !vis[4] && !vis[5]) {
               this.drawRing(fbW, fbH, guiScale);
               return;
            }

            float[][] p = new float[8][];

            for (int i = 0; i < 8; i++) {
               p[i] = Esp.project((i & 1) == 0 ? this.cur[0] : this.cur[3], (i & 2) == 0 ? this.cur[1] : this.cur[4], (i & 4) == 0 ? this.cur[2] : this.cur[5]);
            }

            int base = this.baseColor();
            int alt = this.useAccent.isOn() ? Theme.HUD_ACCENT_2 : Theme.mix(base, -1, 0.5F);
            float px = Math.max(1.0F, guiScale / 2.0F);
            float pulse = 0.85F + 0.15F * (float)Math.sin(this.time * 3.2);
            float mining = this.shownProgress;

            for (int pass = 0; pass < 3; pass++) {
               float w = pass == 0 ? 7.0F * px : (pass == 1 ? 3.4F * px : 1.35F * px);
               float a = pass == 0 ? 0.1F * pulse : (pass == 1 ? 0.26F * pulse : 1.0F);
               a = Math.min(1.0F, a * (1.0F + mining * 0.8F));

               for (int[] e : EDGES) {
                  if (vis[e[2]] || vis[e[3]]) {
                     float[] a0 = p[e[0]];
                     float[] a1 = p[e[1]];
                     if (a0 != null && a1 != null) {
                        if (st.equals("Corners")) {
                           this.bracket(a0, a1, w, a, base, pass);
                           this.bracket(a1, a0, w, a, base, pass);
                        } else if (st.equals("Gradient")) {
                           int segs = 6;

                           for (int s = 0; s < segs; s++) {
                              float t0 = (float)s / segs;
                              float t1 = (float)(s + 1) / segs;
                              float k = (float)(0.5 + 0.5 * Math.sin((e[0] + e[1]) * 0.9 + (t0 + t1) * 1.6 - this.time * 2.6));
                              int c = Anim.lerpColor(base, alt, k);
                              if (pass == 2) {
                                 c = Theme.mix(c, -1, 0.25F);
                              }

                              Render2D.line(
                                 lerp(a0[0], a1[0], t0), lerp(a0[1], a1[1], t0), lerp(a0[0], a1[0], t1), lerp(a0[1], a1[1], t1), w, Render2D.withAlpha(c, a)
                              );
                           }
                        } else {
                           int c = pass == 2 ? Theme.mix(base, -1, 0.35F) : base;
                           Render2D.line(a0[0], a0[1], a1[0], a1[1], w, Render2D.withAlpha(c, a));
                        }
                     }
                  }
               }
            }
         }

         this.drawRing(fbW, fbH, guiScale);
      } else {
         this.drawRing(fbW, fbH, guiScale);
      }
   }

   private void bracket(float[] from, float[] to, float w, float a, int base, int pass) {
      float x = from[0] + (to[0] - from[0]) * 0.26F;
      float y = from[1] + (to[1] - from[1]) * 0.26F;
      int c = pass == 2 ? Theme.mix(base, -1, 0.35F) : base;
      Render2D.line(from[0], from[1], x, y, w * 1.15F, Render2D.withAlpha(c, a));
   }

   private static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   private void drawRing(int fbW, int fbH, float guiScale) {
      if (!(this.ringAlpha < 0.02F)) {
         float cx = fbW / 2.0F;
         float cy = fbH / 2.0F;
         float r = 10.5F * guiScale;
         int base = this.baseColor();
         float w = Math.max(1.2F, 1.6F * guiScale / 2.0F);
         Render2D.circleOutline(cx, cy, r, w, Render2D.withAlpha(-16777216, 0.35F * this.ringAlpha));
         if (this.shownProgress > 0.005F) {
            float a0 = (float) (-Math.PI / 2);
            float a1 = a0 + (float)((Math.PI * 2) * this.shownProgress);
            Render2D.glow(cx + (float)Math.cos(a1) * r, cy + (float)Math.sin(a1) * r, 5.0F * guiScale, Render2D.withAlpha(base, 0.5F * this.ringAlpha));
            Render2D.arc(cx, cy, r, a0, a1, w * 1.3F, Render2D.withAlpha(Theme.mix(base, -1, this.shownProgress * 0.4F), this.ringAlpha));
         }
      }
   }
}
