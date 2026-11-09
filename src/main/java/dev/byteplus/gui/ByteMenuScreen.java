package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.mixin.PlayerSkinWidgetAccessor;
import dev.byteplus.render.CustomSkyRenderer;
import dev.byteplus.render.Icons;
import dev.byteplus.render.MenuBackground;
import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import dev.byteplus.util.UiSounds;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.class_1044;
import net.minecraft.class_10868;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_155;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_412;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_8685;
import net.minecraft.class_8765;
import net.minecraft.class_642.class_8678;

public class ByteMenuScreen extends class_437 {
   private static final float VW = 640.0F;
   private static final float VH = 360.0F;
   private static final float HOLE_X = 432.0F;
   private static final float HOLE_Y = 166.0F;
   private static final float HOLE_R = 58.0F;
   private static final float SKIN_Y = 92.0F;
   private static final float SKIN_W = 80.0F;
   private static final float SKIN_H = 148.0F;
   private static boolean shownOnce;
   private final List<ByteMenuScreen.Btn> buttons = new ArrayList<>();
   private final float[] hover = new float[10];
   private final float[] ripple = new float[10];
   private final float[][] rippleAt = new float[10][2];
   private class_8765 skin;
   private Supplier<class_8685> skinLookup;
   private float age;
   private float introLen;
   private float parX;
   private float parY;
   private long lastDragMs;
   private int focus = -1;
   private int lastHovered = -1;
   private float selX;
   private float selY;
   private float selW;
   private float selH;
   private float selA;
   private float curX = -1.0F;
   private float curY = -1.0F;
   private Runnable pending;
   private float warp = -1.0F;
   private float k = 1.0F;
   private float ox;
   private float oy;
   private static final int PARTICLES = 60;
   private final float[] px = new float[60];
   private final float[] py = new float[60];
   private final float[] pz = new float[60];
   private final float[] pvx = new float[60];
   private final float[] pvy = new float[60];
   private boolean particlesReady;
   private static final String[] TIPS = new String[]{
      "Press Right Shift in game to open Cracked By PLOPO.",
      "Middle-click a player to add them as a friend.",
      "Type .help in chat for Cracked By PLOPO commands.",
      "Hold C to zoom, scroll to zoom further.",
      "Pick a theme in the Cracked By PLOPO module to recolour everything.",
      "Press V while holding a knife to inspect it."
   };

   public ByteMenuScreen() {
      super(class_2561.method_43470("Cracked By PLOPO"));
   }

   private void layout(float sw, float sh) {
      this.k = Math.min(sw / 640.0F, sh / 360.0F);
      this.ox = (sw - 640.0F * this.k) / 2.0F;
      this.oy = (sh - 360.0F * this.k) / 2.0F;
   }

   protected void method_25426() {
      class_310 mc = class_310.method_1551();
      this.layout(this.field_22789, this.field_22790);
      this.introLen = shownOnce ? 0.9F : 2.6F;
      shownOnce = true;
      this.buttons.clear();
      float x = 44.0F;
      float y = 150.0F;
      float w = 196.0F;
      float h = 24.0F;
      float gap = 5.5F;
      this.buttons.add(new ByteMenuScreen.Btn("Singleplayer", "person", false, x, y, w, h, () -> mc.method_1507(new class_526(this))));
      y += h + gap;
      this.buttons.add(new ByteMenuScreen.Btn("Multiplayer", "globe", false, x, y, w, h, () -> mc.method_1507(new class_500(this))));
      y += h + gap;
      String last = BytePlus.modules().clickGui.lastServer.get();
      if (!last.isBlank()) {
         this.buttons.add(new ByteMenuScreen.Btn("Join " + shortIp(last), "bolt", false, x, y, w, h, () -> {
            class_642 data = new class_642(last, last, class_8678.field_45611);
            class_412.method_36877(this, mc, class_639.method_2950(last), data, false, null);
         }));
         y += h + gap;
      }

      this.buttons.add(new ByteMenuScreen.Btn("Cracked By PLOPO Settings", "wand", false, x, y, w, h, () -> mc.method_1507(ClickGuiScreen.get())));
      y += h + gap;
      float half = (w - gap) / 2.0F;
      this.buttons.add(new ByteMenuScreen.Btn("Options", "gear", false, x, y, half, h, () -> mc.method_1507(new class_429(this, mc.field_1690))));
      this.buttons.add(new ByteMenuScreen.Btn("Quit", "power", true, x + half + gap, y, half, h, mc::method_1592));
      this.skinLookup = mc.method_1582().method_73544(mc.method_53462(), false);
      int sw = Math.max(20, Math.round(80.0F * this.k));
      int shh = Math.max(40, Math.round(148.0F * this.k));
      this.skin = new class_8765(sw, shh, mc.method_31974(), this.skinLookup);
      this.skin.method_46421(Math.round(this.ox + 432.0F * this.k - sw / 2.0F));
      this.skin.method_46419(Math.round(this.oy + 92.0F * this.k));
      this.method_37063(this.skin);
      this.particlesReady = false;
   }

   private static String shortIp(String ip) {
      return ip.length() > 16 ? ip.substring(0, 15) + "…" : ip;
   }

   public boolean method_25422() {
      return false;
   }

   public boolean method_25421() {
      return false;
   }

   private float t(float start, float dur) {
      float s = this.introLen / 2.6F;
      return Math.max(0.0F, Math.min(1.0F, (this.age - start * s) / (dur * s)));
   }

   private float warpT() {
      return this.warp < 0.0F ? 0.0F : Math.min(1.0F, this.warp / 0.45F);
   }

   public void method_25420(class_332 g, int mouseX, int mouseY, float delta) {
      float sw = this.field_22789;
      float sh = this.field_22790;
      this.layout(sw, sh);
      float arrival = this.t(0.0F, 1.35F);
      boolean ok = MenuBackground.draw(
         arrival, this.warpT(), this.parX, this.parY, (this.ox + 432.0F * this.k) / sw, (this.oy + 166.0F * this.k) / sh, 58.0F * this.k / sh
      );
      if (!ok && !CustomSkyRenderer.drawMenu(9, this.parX * 6.0F, this.parY * 4.0F)) {
         super.method_25420(g, mouseX, mouseY, delta);
      }
   }

   public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
      float a = Anim.easeOutCubic(this.t(1.1F, 0.6F)) * (1.0F - this.warpT());
      this.skin.method_46419(Math.round(this.oy + 92.0F * this.k + (1.0F - a) * 26.0F * this.k));
      this.skin.field_22764 = a > 0.03F;
      if (System.currentTimeMillis() - this.lastDragMs > 2500L) {
         PlayerSkinWidgetAccessor acc = (PlayerSkinWidgetAccessor)this.skin;
         float target = -22.0F + (float)Math.sin(this.age * 0.55) * 20.0F;
         acc.byteplus$setRotationY(acc.byteplus$getRotationY() + (target - acc.byteplus$getRotationY()) * 0.04F);
      }

      super.method_25394(g, mouseX, mouseY, delta);
   }

   public void method_25393() {
      if (this.pending != null && this.warpT() >= 1.0F) {
         Runnable r = this.pending;
         this.pending = null;
         this.warp = -1.0F;
         r.run();
      }
   }

   public void draw(float sw, float sh, float dt) {
      this.age += dt;
      if (this.warp >= 0.0F) {
         this.warp += dt;
      }

      this.layout(sw, sh);
      float[] mouse = GuiDev.mouse();
      float mx = mouse[0];
      float my = mouse[1];
      this.parX = this.parX + (mx / sw - 0.5F - this.parX) * (1.0F - (float)Math.exp(-dt * 3.0F));
      this.parY = this.parY + (my / sh - 0.5F - this.parY) * (1.0F - (float)Math.exp(-dt * 3.0F));
      if (this.curX < 0.0F) {
         this.curX = mx;
         this.curY = my;
      }

      this.curX = this.curX + (mx - this.curX) * (1.0F - (float)Math.exp(-dt * 14.0F));
      this.curY = this.curY + (my - this.curY) * (1.0F - (float)Math.exp(-dt * 14.0F));
      float w = this.warpT();
      float uiA = 1.0F - Anim.easeOutCubic(w);
      this.particles(sw, sh, dt, mx, my, uiA);
      Render2D.glow(this.curX, this.curY, 70.0F, Render2D.withAlpha(Theme.ACCENT, 0.07F * uiA));
      float hx = this.ox + 432.0F * this.k;
      float hy = this.oy + 166.0F * this.k;
      float sw1 = this.t(0.95F, 1.1F);
      if (sw1 > 0.0F && sw1 < 1.0F) {
         float e = Anim.easeOutCubic(sw1);
         Render2D.circleOutline(hx, hy, (30.0F + e * 520.0F) * this.k, (6.0F * (1.0F - e) + 0.5F) * this.k, Render2D.withAlpha(-1, 0.55F * (1.0F - e)));
         Render2D.circleOutline(hx, hy, (20.0F + e * 380.0F) * this.k, 3.0F * (1.0F - e) * this.k, Render2D.withAlpha(Theme.ACCENT, 0.6F * (1.0F - e)));
      }

      float vmx = (mx - this.ox) / this.k;
      float vmy = (my - this.oy) / this.k;
      Render2D.save();
      Render2D.translate(this.ox, this.oy);
      Render2D.scale(this.k);
      if (w > 0.0F) {
         float s = 1.0F + 0.12F * w;
         Render2D.translate(320.0F, 180.0F);
         Render2D.scale(s);
         Render2D.translate(-320.0F, -180.0F);
      }

      Render2D.globalAlpha(uiA);
      this.drawBrand();
      this.drawButtons(vmx, vmy, dt);
      this.drawStage();
      this.drawAccount();
      this.drawNews();
      this.drawFooter();
      Render2D.restore();
      float fade = 1.0F - this.t(0.0F, 0.5F);
      if (fade > 0.0F) {
         Render2D.rect(0.0F, 0.0F, sw, sh, Render2D.withAlpha(-16777216, fade));
      }
   }

   private void particles(float sw, float sh, float dt, float mx, float my, float alpha) {
      if (!this.particlesReady) {
         Random r = new Random(9L);

         for (int i = 0; i < 60; i++) {
            this.px[i] = r.nextFloat() * sw;
            this.py[i] = r.nextFloat() * sh;
            this.pz[i] = 0.3F + r.nextFloat() * 0.7F;
            this.pvx[i] = (r.nextFloat() - 0.5F) * 6.0F;
            this.pvy[i] = -4.0F - r.nextFloat() * 10.0F;
         }

         this.particlesReady = true;
      }

      float reach = Math.min(sw, sh) * 0.22F;

      for (int i = 0; i < 60; i++) {
         float dx = this.px[i] - mx;
         float dy = this.py[i] - my;
         float d2 = dx * dx + dy * dy;
         if (d2 < reach * reach && d2 > 1.0F) {
            float f = (1.0F - (float)Math.sqrt(d2) / reach) * 60.0F * this.pz[i];
            float d = (float)Math.sqrt(d2);
            this.px[i] = this.px[i] + dx / d * f * dt;
            this.py[i] = this.py[i] + dy / d * f * dt;
         }

         this.px[i] = this.px[i] + this.pvx[i] * this.pz[i] * dt;
         this.py[i] = this.py[i] + this.pvy[i] * this.pz[i] * dt;
         if (this.py[i] < -6.0F) {
            this.py[i] = sh + 6.0F;
            this.px[i] = (float)(Math.random() * sw);
         }

         if (this.px[i] < -6.0F) {
            this.px[i] = sw + 6.0F;
         } else if (this.px[i] > sw + 6.0F) {
            this.px[i] = -6.0F;
         }
      }

      for (int i = 0; i < 60; i++) {
         float dxm = this.px[i] - mx;
         float dym = this.py[i] - my;
         float dm = (float)Math.sqrt(dxm * dxm + dym * dym);
         if (!(dm > reach * 1.15F)) {
            float near = 1.0F - dm / (reach * 1.15F);
            Render2D.line(this.px[i], this.py[i], mx, my, 0.5F, Render2D.withAlpha(Theme.ACCENT, 0.22F * near * alpha));

            for (int j = i + 1; j < 60; j++) {
               float dx = this.px[i] - this.px[j];
               float dy = this.py[i] - this.py[j];
               float d = (float)Math.sqrt(dx * dx + dy * dy);
               if (d < reach * 0.45F) {
                  Render2D.line(
                     this.px[i], this.py[i], this.px[j], this.py[j], 0.45F, Render2D.withAlpha(-1, 0.16F * near * (1.0F - d / (reach * 0.45F)) * alpha)
                  );
               }
            }
         }
      }

      for (int i = 0; i < 60; i++) {
         float tw = 0.35F + 0.65F * (float)Math.abs(Math.sin(this.age * 1.2 + i * 0.9));
         int c = i % 3 == 0 ? Theme.ACCENT : -1;
         float r = (0.3F + this.pz[i] * 0.55F) * this.k;
         Render2D.circle(this.px[i], this.py[i], r * 3.2F, Render2D.withAlpha(c, 0.05F * tw * alpha));
         Render2D.circle(this.px[i], this.py[i], r, Render2D.withAlpha(c, 0.6F * tw * alpha));
      }
   }

   private void drawBrand() {
      float a = this.t(0.55F, 0.75F);
      if (!(a <= 0.0F)) {
         float x = 44.0F;
         float y = 62.0F;
         float e = Anim.easeOutCubic(a);
         Render2D.save();
         Render2D.globalAlpha(e);
         Render2D.translate((1.0F - e) * -16.0F, 0.0F);
         float pulse = 0.5F + 0.5F * (float)Math.sin(this.age * 2.0F);
         float logo = 42.0F;
         Render2D.glow(x + logo / 2.0F, y + logo / 2.0F, 44.0F + 6.0F * pulse, Render2D.withAlpha(Theme.ACCENT, 0.18F + 0.08F * pulse));
         Render2D.image(Render2D.modImage("/assets/byteplus/textures/logo.png"), x, y, logo, logo, 1.0F);
         float tx = x + logo + 10.0F;
         float ty = y + 5.0F;
         float ts = 31.0F;
         float glitch = 1.0F - Anim.easeOutCubic(this.t(0.6F, 0.55F));
         if (glitch > 0.02F) {
            float g = glitch * 6.0F;
            float jitter = (float)Math.sin(this.age * 90.0F) * glitch * 2.5F;
            Render2D.text("CRACKED BY PLOPO", tx + g + jitter, ty, ts, Render2D.withAlpha(-50325, 0.7F * glitch), 1, 2.6F);
            Render2D.text("CRACKED BY PLOPO", tx - g, ty - jitter * 0.4F, ts, Render2D.withAlpha(-13244673, 0.7F * glitch), 1, 2.6F);
         }

         float end = Render2D.textGradient("CRACKED BY PLOPO", tx, ty, ts, Theme.ACCENT, Theme.HUD_ACCENT_2, 1, 2.6F);
         float cycle = this.age % 4.0F / 4.0F;
         float band = tx - 40.0F + (end - tx + 80.0F) * cycle;
         Render2D.save();
         Render2D.scissor(band, ty - 4.0F, 16.0F, ts + 10.0F);
         Render2D.text("CRACKED BY PLOPO", tx, ty, ts, -2086, 1, 2.6F);
         Render2D.restore();
         Render2D.text(
            "UTILITY CLIENT  ·  " + class_155.method_16673().comp_4025(), x + 1.0F, y + logo + 11.0F, 6.4F, Render2D.withAlpha(Theme.TEXT, 0.55F), 1, 1.6F
         );
         String name = class_310.method_1551().method_1548().method_1676();
         String hello = greeting() + ", ";
         Render2D.text(hello, x + 1.0F, y + logo + 24.0F, 9.2F, Theme.TEXT_DIM, 0);
         float gw = Render2D.textWidth(hello, 9.2F, 0);
         Render2D.textGradient(name, x + 1.0F + gw, y + logo + 24.0F, 9.2F, Theme.TEXT, Theme.mix(Theme.ACCENT, -1, 0.4F), 1, 0.2F);
         Render2D.restore();
      }
   }

   private static String greeting() {
      int h = LocalDateTime.now().getHour();
      return h < 5 ? "Late night" : (h < 12 ? "Good morning" : (h < 18 ? "Good afternoon" : "Good evening"));
   }

   private int hovered(float vmx, float vmy) {
      if (this.warp >= 0.0F) {
         return -1;
      }

      for (int i = 0; i < this.buttons.size(); i++) {
         ByteMenuScreen.Btn b = this.buttons.get(i);
         if (vmx >= b.x() && vmx <= b.x() + b.w() && vmy >= b.y() && vmy <= b.y() + b.h()) {
            return i;
         }
      }

      return -1;
   }

   private void drawButtons(float vmx, float vmy, float dt) {
      int hov = this.hovered(vmx, vmy);
      if (hov != this.lastHovered && hov >= 0) {
         UiSounds.play(UiSounds.Kind.HOVER, 0.22F * BytePlus.modules().clickGui.volume.getFloat() * 2.0F);
      }

      this.lastHovered = hov;
      int active = hov >= 0 ? hov : this.focus;
      if (active >= 0) {
         ByteMenuScreen.Btn b = this.buttons.get(active);
         float kk = 1.0F - (float)Math.exp(-dt * 18.0F);
         if (this.selA < 0.01F) {
            this.selX = b.x();
            this.selY = b.y();
            this.selW = b.w();
            this.selH = b.h();
         }

         this.selX = this.selX + (b.x() - this.selX) * kk;
         this.selY = this.selY + (b.y() - this.selY) * kk;
         this.selW = this.selW + (b.w() - this.selW) * kk;
         this.selH = this.selH + (b.h() - this.selH) * kk;
      }

      this.selA = Anim.approach(this.selA, active >= 0 ? 1.0F : 0.0F, 10.0F, dt);
      if (this.selA > 0.01F) {
         Render2D.shadow(this.selX, this.selY, this.selW, this.selH, 9.0F, 18.0F, Render2D.withAlpha(Theme.ACCENT & 16777215 | 0xFF000000, 0.32F * this.selA));
      }

      for (int i = 0; i < this.buttons.size(); i++) {
         ByteMenuScreen.Btn b = this.buttons.get(i);
         float a = Anim.easeOutCubic(this.t(1.0F + i * 0.07F, 0.5F));
         if (!(a <= 0.0F)) {
            boolean on = i == active;
            this.hover[i] = Anim.approach(this.hover[i], on ? 1.0F : 0.0F, 16.0F, dt);
            this.ripple[i] = Math.max(0.0F, this.ripple[i] - dt * 2.2F);
            float h = this.hover[i];
            int accent = b.danger() ? -42406 : Theme.ACCENT;
            float mxo = 0.0F;
            float myo = 0.0F;
            if (i == hov) {
               mxo = Math.max(-3.0F, Math.min(3.0F, (vmx - (b.x() + b.w() / 2.0F)) * 0.03F));
               myo = Math.max(-1.5F, Math.min(1.5F, (vmy - (b.y() + b.h() / 2.0F)) * 0.06F));
            }

            float x = b.x() + (1.0F - a) * -36.0F + h * 3.0F + mxo;
            float y = b.y() + myo;
            float bw = b.w();
            float bh = b.h();
            Render2D.save();
            Render2D.globalAlpha(a);
            Render2D.roundRect(x, y, bw, bh, 8.0F, Render2D.withAlpha(-16119544, 0.58F + 0.2F * h));
            Render2D.horizontalGradient(x, y, bw, bh, 8.0F, Render2D.withAlpha(accent, 0.2F * h), Render2D.withAlpha(accent, 0.02F * h));
            if (i == hov) {
               Render2D.radialRoundRect(x, y, bw, bh, 8.0F, vmx, vmy, 0.0F, 60.0F, Render2D.withAlpha(-1, 0.09F), 0);
            }

            Render2D.verticalGradient(x, y, bw, bh * 0.5F, 8.0F, Render2D.withAlpha(-1, 0.045F), 0);
            Render2D.gradientOutline(
               x + 0.3F,
               y + 0.3F,
               bw - 0.6F,
               bh - 0.6F,
               8.0F,
               0.7F,
               x,
               y,
               x + bw,
               y + bh,
               Anim.lerpColor(Render2D.withAlpha(-1, 0.14F), Render2D.withAlpha(accent, 0.95F), h),
               Anim.lerpColor(Render2D.withAlpha(-1, 0.04F), Render2D.withAlpha(accent, 0.25F), h)
            );
            if (this.ripple[i] > 0.0F) {
               float re = 1.0F - this.ripple[i];
               Render2D.save();
               Render2D.scissor(x, y, bw, bh);
               Render2D.circle(this.rippleAt[i][0], this.rippleAt[i][1], 6.0F + re * bw, Render2D.withAlpha(-1, 0.22F * this.ripple[i]));
               Render2D.restore();
            }

            float tile = 16.0F;
            float tx = x + 8.0F;
            float ty = y + (bh - tile) / 2.0F;
            Render2D.roundRect(tx, ty, tile, tile, 5.0F, Anim.lerpColor(Render2D.withAlpha(-1, 0.07F), Render2D.withAlpha(accent, 0.32F), h));
            this.icon(b.icon(), tx + 3.5F, ty + 3.5F, 9.0F, Anim.lerpColor(-2961723, accent, h));
            Render2D.text(b.label(), tx + tile + 7.0F, y + (bh - 8.0F) / 2.0F, 8.0F, Anim.lerpColor(Theme.TEXT, -1, h), 1, 0.3F);
            if (bw > 120.0F) {
               float cx = x + bw - 13.0F - (1.0F - h) * 4.0F;
               float cy = y + bh / 2.0F;
               int cc = Render2D.withAlpha(accent, h);
               Render2D.line(cx - 2.0F, cy - 3.0F, cx + 1.2F, cy, 1.3F, cc);
               Render2D.line(cx + 1.2F, cy, cx - 2.0F, cy + 3.0F, 1.3F, cc);
            }

            Render2D.restore();
         }
      }
   }

   private void icon(String name, float x, float y, float s, int c) {
      switch (name) {
         case "person":
            Icons.person(x, y, s, c);
            break;
         case "gear":
            Icons.gear(x, y, s, c);
            break;
         case "wand":
            Icons.wand(x, y, s, c);
            break;
         case "globe":
            Icons.radar(x, y, s, c);
            break;
         case "bolt":
            Render2D.beginPath();
            Render2D.moveTo(x + s * 0.6F, y);
            Render2D.lineTo(x + s * 0.15F, y + s * 0.58F);
            Render2D.lineTo(x + s * 0.48F, y + s * 0.58F);
            Render2D.lineTo(x + s * 0.35F, y + s);
            Render2D.lineTo(x + s * 0.85F, y + s * 0.38F);
            Render2D.lineTo(x + s * 0.52F, y + s * 0.38F);
            Render2D.closePath();
            Render2D.fillPath(c);
            break;
         case "power":
            Render2D.arc(x + s / 2.0F, y + s / 2.0F + 0.5F, s * 0.42F, (float)Math.toRadians(-55.0), (float)Math.toRadians(235.0), s * 0.13F, c);
            Render2D.line(x + s / 2.0F, y, x + s / 2.0F, y + s * 0.45F, s * 0.13F, c);
            break;
         default:
            Icons.sparkle(x + s / 2.0F, y + s / 2.0F, s / 2.0F, c);
      }
   }

   private void drawStage() {
      float a = Anim.easeOutCubic(this.t(1.1F, 0.6F));
      if (!(a <= 0.0F)) {
         float cx = 432.0F;
         float feet = 237.0F;
         Render2D.save();
         Render2D.globalAlpha(a);
         float beam = 1.0F - Anim.easeOutCubic(this.t(1.1F, 1.0F));
         if (beam > 0.01F) {
            Render2D.verticalGradient(cx - 30.0F * beam - 6.0F, 0.0F, 60.0F * beam + 12.0F, feet, 0.0F, 0, Render2D.withAlpha(-2864, 0.35F * beam));
         }

         Render2D.save();
         Render2D.translate(cx, feet);
         Render2D.scaleXY(1.0F, 0.26F);
         Render2D.glow(0.0F, 0.0F, 70.0F, Render2D.withAlpha(Theme.ACCENT, 0.22F));
         Render2D.circleOutline(0.0F, 0.0F, 54.0F, 1.6F, Render2D.withAlpha(Theme.ACCENT, 0.75F));
         Render2D.circleOutline(0.0F, 0.0F, 42.0F, 0.8F, Render2D.withAlpha(Theme.ACCENT, 0.35F));
         Render2D.circleOutline(0.0F, 0.0F, 66.0F, 0.6F, Render2D.withAlpha(-1, 0.2F));

         for (int i = 0; i < 16; i++) {
            float a0 = (float)(this.age * 0.7 + i * Math.PI / 8.0);
            Render2D.arc(0.0F, 0.0F, 60.0F, a0, a0 + 0.16F, 2.0F, Render2D.withAlpha(Theme.ACCENT, 0.55F));
         }

         Render2D.restore();

         for (int i = 0; i < 14; i++) {
            float ph = (float)((this.age * 0.35 + i * 0.137) % 1.0);
            float ang = i * 2.39996F;
            float mxp = cx + (float)Math.cos(ang) * 40.0F * (0.4F + i % 5 * 0.12F);
            float myp = feet - ph * 120.0F;
            Render2D.circle(mxp, myp, 0.9F, Render2D.withAlpha(Theme.ACCENT, 0.8F * (1.0F - ph) * Math.min(1.0F, ph * 5.0F)));
         }

         float scan = (float)(this.age % 4.5 / 1.4);
         if (scan < 1.0F) {
            float sy = feet - scan * 180.0F;
            Render2D.verticalGradient(cx - 46.0F, sy - 6.0F, 92.0F, 12.0F, 0.0F, 0, Render2D.withAlpha(Theme.ACCENT, 0.22F * (1.0F - scan)));
            Render2D.line(cx - 40.0F, sy, cx + 40.0F, sy, 0.6F, Render2D.withAlpha(-1, 0.35F * (1.0F - scan)));
         }

         Render2D.textCenteredTop("drag to rotate", cx, feet + 22.0F, 5.2F, Render2D.withAlpha(Theme.TEXT_DIM, 0.6F), 0, 0.8F);
         Render2D.restore();
      }
   }

   private int faceImage() {
      try {
         class_8685 s = this.skinLookup == null ? null : this.skinLookup.get();
         if (s == null) {
            return 0;
         }

         class_1044 tex = class_310.method_1551().method_1531().method_4619(s.comp_1626().comp_3627());
         if (tex != null && tex.method_68004() instanceof class_10868 gt) {
            return Render2D.glImage(gt.method_68427(), 64, 64);
         }
      } catch (Throwable var5) {
      }

      return 0;
   }

   private void drawAccount() {
      float a = Anim.easeOutCubic(this.t(1.4F, 0.5F));
      if (!(a <= 0.0F)) {
         String name = class_310.method_1551().method_1548().method_1676();
         float tw = Render2D.textWidth(name, 7.4F, 1, 0.2F);
         float w = 30.0F + tw + 30.0F;
         float h = 24.0F;
         float x = 622.0F - w;
         float y = 16.0F;
         Render2D.save();
         Render2D.globalAlpha(a);
         Render2D.translate(0.0F, (1.0F - a) * -8.0F);
         this.glass(x, y, w, h, 12.0F);
         int face = this.faceImage();
         if (face != 0) {
            Render2D.save();
            Render2D.imageRegion(face, 8.0F, 8.0F, 8.0F, 8.0F, x + 5.0F, y + 4.0F, 16.0F, 16.0F, 1.0F);
            Render2D.imageRegion(face, 40.0F, 8.0F, 8.0F, 8.0F, x + 4.5F, y + 3.5F, 17.0F, 17.0F, 1.0F);
            Render2D.restore();
         } else {
            Icons.person(x + 6.0F, y + 5.0F, 14.0F, Theme.ACCENT);
         }

         Render2D.text(name, x + 26.0F, y + 4.5F, 7.4F, Theme.TEXT, 1, 0.2F);
         Render2D.circle(x + 27.5F, y + 17.0F, 1.8F, -11800454);
         Render2D.text("Online", x + 31.0F, y + 14.0F, 5.6F, Theme.TEXT_DIM, 0);
         Render2D.restore();
      }
   }

   private void glass(float x, float y, float w, float h, float r) {
      Render2D.shadow(x, y, w, h, r, 14.0F, 1610612736);
      Render2D.roundRect(x, y, w, h, r, Render2D.withAlpha(-16053751, 0.64F));
      Render2D.verticalGradient(x, y, w, h * 0.5F, r, Render2D.withAlpha(-1, 0.05F), 0);
      Render2D.roundRectOutline(x + 0.3F, y + 0.3F, w - 0.6F, h - 0.6F, r, 0.6F, Render2D.withAlpha(-1, 0.1F));
   }

   private void drawNews() {
      float a = Anim.easeOutCubic(this.t(1.55F, 0.55F));
      if (!(a <= 0.0F)) {
         float w = 136.0F;
         float h = 92.0F;
         float x = 622.0F - w;
         float y = 338.0F - h;
         Render2D.save();
         Render2D.globalAlpha(a);
         Render2D.translate((1.0F - a) * 18.0F, 0.0F);
         this.glass(x, y, w, h, 10.0F);
         Render2D.roundRect(x + 10.0F, y + 10.0F, 22.0F, 9.0F, 4.5F, Theme.ACCENT);
         Render2D.text("NEW", x + 13.2F, y + 11.4F, 5.6F, Theme.ACCENT_DARK, 1, 0.8F);
         Render2D.text("What's new in Byte+", x + 37.0F, y + 10.6F, 6.6F, Theme.TEXT, 1, 0.2F);
         String[][] items = new String[][]{
            {"wand", "CSGO knives + inspects"}, {"sky", "12 animated skies"}, {"cape", "HD capes, wings, halo"}, {"gear", "Premium menus + themes"}
         };

         for (int i = 0; i < items.length; i++) {
            float iy = y + 28.0F + i * 14.5F;
            Render2D.roundRect(x + 10.0F, iy - 1.0F, 11.0F, 11.0F, 3.5F, Render2D.withAlpha(Theme.ACCENT, 0.18F));
            Icons.sparkle(x + 15.5F, iy + 4.5F, 3.2F, Theme.ACCENT);
            Render2D.text(items[i][1], x + 27.0F, iy + 0.5F, 6.4F, Render2D.withAlpha(Theme.TEXT, 0.92F), 0);
         }

         Render2D.restore();
      }
   }

   private void drawFooter() {
      float a = Anim.easeOutCubic(this.t(1.6F, 0.6F));
      if (!(a <= 0.0F)) {
         Render2D.save();
         Render2D.globalAlpha(a);
         LocalDateTime now = LocalDateTime.now();
         Render2D.text(now.format(DateTimeFormatter.ofPattern("HH:mm")), 44.0F, 316.0F, 17.0F, Theme.TEXT, 1, 0.6F);
         Render2D.text(now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), 45.0F, 335.0F, 6.2F, Theme.TEXT_DIM, 0);
         int idx = (int)(this.age / 6.0F) % TIPS.length;
         float local = this.age % 6.0F;
         String tip = TIPS[idx];
         int shown = Math.min(tip.length(), (int)(local * 40.0F));
         float fadeOut = local > 5.4F ? 1.0F - (local - 5.4F) / 0.6F : 1.0F;
         String text = tip.substring(0, shown);
         float tw = Render2D.textWidth(tip, 6.2F, 0);
         float tx = 290.0F - tw / 2.0F + 60.0F;
         float ty = 340.0F;
         Render2D.save();
         Render2D.globalAlpha(a * fadeOut);
         Icons.sparkle(tx - 8.0F, ty + 3.2F, 3.0F, Theme.ACCENT);
         float end = Render2D.text(text, tx, ty, 6.2F, Render2D.withAlpha(Theme.TEXT, 0.75F), 0);
         if (shown < tip.length() || this.age % 1.0F < 0.5F) {
            Render2D.rect(end + 1.0F, ty + 0.5F, 0.7F, 6.2F, Theme.ACCENT);
         }

         Render2D.restore();
         String ver = "Cracked By PLOPO  ·  not affiliated with Mojang";
         Render2D.text(ver, 622.0F - Render2D.textWidth(ver, 5.2F, 0), 348.0F, 5.2F, Render2D.withAlpha(Theme.TEXT_DIM, 0.55F), 0);
         Render2D.restore();
      }
   }

   public float[] devButtonCenter(int i) {
      ByteMenuScreen.Btn b = this.buttons.get(Math.min(i, this.buttons.size() - 1));
      return new float[]{this.ox + (b.x() + b.w() / 2.0F) * this.k, this.oy + (b.y() + b.h() / 2.0F) * this.k};
   }

   public int devButtonIndex(String label) {
      for (int i = 0; i < this.buttons.size(); i++) {
         if (this.buttons.get(i).label().startsWith(label)) {
            return i;
         }
      }

      return -1;
   }

   public void devActivate(int i) {
      if (i >= 0 && i < this.buttons.size()) {
         ByteMenuScreen.Btn b = this.buttons.get(i);
         this.activate(i, b.x() + b.w() / 2.0F, b.y() + b.h() / 2.0F);
      }
   }

   public static void devResetIntro() {
      shownOnce = false;
   }

   private void activate(int i, float vmx, float vmy) {
      if (this.pending == null && i >= 0 && i < this.buttons.size()) {
         ByteMenuScreen.Btn b = this.buttons.get(i);
         this.ripple[i] = 1.0F;
         this.rippleAt[i][0] = vmx;
         this.rippleAt[i][1] = vmy;
         BytePlus.modules().clickGui.playClick(1.0F);
         if (b.danger()) {
            b.action().run();
         } else {
            UiSounds.play(UiSounds.Kind.WHOOSH, 0.45F * BytePlus.modules().clickGui.volume.getFloat() * 2.0F);
            this.pending = b.action();
            this.warp = 0.0F;
         }
      }
   }

   public boolean method_25402(class_11909 event, boolean doubleClick) {
      double mx = event.comp_4798();
      double my = event.comp_4799();
      float vmx = (float)((mx - this.ox) / this.k);
      float vmy = (float)((my - this.oy) / this.k);
      if (event.method_74245() == 0 && this.age > 0.4F) {
         int i = this.hovered(vmx, vmy);
         if (i >= 0) {
            this.activate(i, vmx, vmy);
            return true;
         }
      }

      if (this.skin.method_25405(mx, my)) {
         this.lastDragMs = System.currentTimeMillis();
      }

      return super.method_25402(event, doubleClick);
   }

   public boolean method_25403(class_11909 event, double dx, double dy) {
      this.lastDragMs = System.currentTimeMillis();
      return super.method_25403(event, dx, dy);
   }

   public boolean method_25404(class_11908 event) {
      int key = event.comp_4795();
      if (key == 264 || key == 258) {
         this.focus = (this.focus + 1) % this.buttons.size();
         UiSounds.play(UiSounds.Kind.HOVER, 0.3F);
         return true;
      } else if (key == 265) {
         this.focus = (this.focus - 1 + this.buttons.size()) % this.buttons.size();
         UiSounds.play(UiSounds.Kind.HOVER, 0.3F);
         return true;
      } else if ((key == 257 || key == 335) && this.focus >= 0) {
         ByteMenuScreen.Btn b = this.buttons.get(this.focus);
         this.activate(this.focus, b.x() + b.w() / 2.0F, b.y() + b.h() / 2.0F);
         return true;
      } else {
         return super.method_25404(event);
      }
   }

   private record Btn(String label, String icon, boolean danger, float x, float y, float w, float h, Runnable action) {
   }
}
