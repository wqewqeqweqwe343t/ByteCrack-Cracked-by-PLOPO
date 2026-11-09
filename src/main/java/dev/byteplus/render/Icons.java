package dev.byteplus.render;

import dev.byteplus.module.Category;
import org.lwjgl.nanovg.NanoVG;

public final class Icons {
   private Icons() {
   }

   public static void category(Category c, float x, float y, float s, int color) {
      switch (c) {
         case COMBAT:
            swords(x, y, s, color);
            break;
         case ANTICHEAT:
            shield(x, y, s, color);
            break;
         case MISC:
            gear(x, y, s, color);
            break;
         case DONUT:
            donut(x, y, s, color);
            break;
         case BASEFINDING:
            radar(x, y, s, color);
            break;
         case RENDER:
            eye(x, y, s, color);
            break;
         case VISUALS:
            sparkles(x, y, s, color);
            break;
         case BYTEPLUS:
            wand(x, y, s, color);
      }
   }

   public static void sparkles(float x, float y, float s, int c) {
      sparkle(x + s * 0.42F, y + s * 0.55F, s * 0.42F, c);
      sparkle(x + s * 0.82F, y + s * 0.18F, s * 0.17F, c);
   }

   public static void swords(float x, float y, float s, int c) {
      float w = s * 0.13F;
      Render2D.line(x + s * 0.15F, y + s * 0.15F, x + s * 0.85F, y + s * 0.85F, w, c);
      Render2D.line(x + s * 0.85F, y + s * 0.15F, x + s * 0.15F, y + s * 0.85F, w, c);
      Render2D.line(x + s * 0.55F, y + s * 0.85F, x + s * 0.85F, y + s * 0.55F, w, c);
      Render2D.line(x + s * 0.15F, y + s * 0.55F, x + s * 0.45F, y + s * 0.85F, w, c);
   }

   public static void shield(float x, float y, float s, int c) {
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.5F, y + s * 0.06F);
      Render2D.lineTo(x + s * 0.88F, y + s * 0.2F);
      NanoVG.nvgQuadTo(Render2D.vg(), x + s * 0.88F, y + s * 0.74F, x + s * 0.5F, y + s * 0.96F);
      NanoVG.nvgQuadTo(Render2D.vg(), x + s * 0.12F, y + s * 0.74F, x + s * 0.12F, y + s * 0.2F);
      Render2D.closePath();
      Render2D.strokePath(s * 0.11F, c);
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.56F, y + s * 0.24F);
      Render2D.lineTo(x + s * 0.36F, y + s * 0.56F);
      Render2D.lineTo(x + s * 0.5F, y + s * 0.56F);
      Render2D.lineTo(x + s * 0.44F, y + s * 0.8F);
      Render2D.lineTo(x + s * 0.66F, y + s * 0.46F);
      Render2D.lineTo(x + s * 0.52F, y + s * 0.46F);
      Render2D.closePath();
      Render2D.fillPath(c);
   }

   public static void gear(float x, float y, float s, int c) {
      float cx = x + s / 2.0F;
      float cy = y + s / 2.0F;

      for (int i = 0; i < 8; i++) {
         double a = (Math.PI * 2) * i / 8.0;
         Render2D.line(
            cx + (float)Math.cos(a) * s * 0.28F,
            cy + (float)Math.sin(a) * s * 0.28F,
            cx + (float)Math.cos(a) * s * 0.46F,
            cy + (float)Math.sin(a) * s * 0.46F,
            s * 0.14F,
            c
         );
      }

      Render2D.circleOutline(cx, cy, s * 0.28F, s * 0.13F, c);
   }

   public static void donut(float x, float y, float s, int c) {
      float cx = x + s / 2.0F;
      float cy = y + s / 2.0F;
      Render2D.circleOutline(cx, cy, s * 0.32F, s * 0.2F, c);
      Render2D.circle(cx - s * 0.18F, cy - s * 0.2F, s * 0.05F, -15461095);
      Render2D.circle(cx + s * 0.22F, cy + s * 0.05F, s * 0.05F, -15461095);
      Render2D.circle(cx - s * 0.05F, cy + s * 0.28F, s * 0.05F, -15461095);
   }

   public static void radar(float x, float y, float s, int c) {
      float cx = x + s / 2.0F;
      float cy = y + s / 2.0F;
      Render2D.circleOutline(cx, cy, s * 0.42F, s * 0.1F, c);
      Render2D.circleOutline(cx, cy, s * 0.2F, s * 0.1F, c);
      Render2D.line(cx, cy, cx + s * 0.32F, cy - s * 0.28F, s * 0.12F, c);
      Render2D.circle(cx, cy, s * 0.07F, c);
   }

   public static void eye(float x, float y, float s, int c) {
      float cx = x + s / 2.0F;
      float cy = y + s / 2.0F;
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.05F, cy);
      NanoVG.nvgQuadTo(Render2D.vg(), cx, y + s * 0.05F, x + s * 0.95F, cy);
      NanoVG.nvgQuadTo(Render2D.vg(), cx, y + s * 0.95F, x + s * 0.05F, cy);
      Render2D.closePath();
      Render2D.strokePath(s * 0.11F, c);
      Render2D.circle(cx, cy, s * 0.16F, c);
   }

   public static void wand(float x, float y, float s, int c) {
      Render2D.line(x + s * 0.12F, y + s * 0.88F, x + s * 0.62F, y + s * 0.38F, s * 0.14F, c);
      sparkle(x + s * 0.74F, y + s * 0.22F, s * 0.2F, c);
      sparkle(x + s * 0.3F, y + s * 0.22F, s * 0.11F, c);
      sparkle(x + s * 0.82F, y + s * 0.66F, s * 0.11F, c);
   }

   public static void sparkle(float cx, float cy, float r, int c) {
      Render2D.beginPath();
      Render2D.moveTo(cx, cy - r);
      Render2D.lineTo(cx + r * 0.28F, cy - r * 0.28F);
      Render2D.lineTo(cx + r, cy);
      Render2D.lineTo(cx + r * 0.28F, cy + r * 0.28F);
      Render2D.lineTo(cx, cy + r);
      Render2D.lineTo(cx - r * 0.28F, cy + r * 0.28F);
      Render2D.lineTo(cx - r, cy);
      Render2D.lineTo(cx - r * 0.28F, cy - r * 0.28F);
      Render2D.closePath();
      Render2D.fillPath(c);
   }

   public static void search(float x, float y, float s, int c) {
      Render2D.circleOutline(x + s * 0.42F, y + s * 0.42F, s * 0.28F, s * 0.12F, c);
      Render2D.line(x + s * 0.63F, y + s * 0.63F, x + s * 0.88F, y + s * 0.88F, s * 0.14F, c);
   }

   public static void compass(float x, float y, float s, int c) {
      float cx = x + s / 2.0F;
      float cy = y + s / 2.0F;
      Render2D.circleOutline(cx, cy, s * 0.42F, s * 0.1F, c);
      Render2D.triangle(cx + s * 0.2F, cy - s * 0.2F, cx - s * 0.06F, cy - s * 0.06F, cx + s * 0.06F, cy + s * 0.06F, c);
      Render2D.triangle(cx - s * 0.2F, cy + s * 0.2F, cx - s * 0.06F, cy - s * 0.06F, cx + s * 0.06F, cy + s * 0.06F, Render2D.withAlpha(c, 0.5F));
   }

   public static void signal(float x, float y, float s, int c, int bars) {
      for (int i = 0; i < 4; i++) {
         float h = s * (0.25F + 0.22F * i);
         float bx = x + s * 0.06F + i * s * 0.25F;
         Render2D.roundRect(bx, y + s * 0.92F - h, s * 0.16F, h, s * 0.05F, i < bars ? c : Render2D.withAlpha(c, 0.25F));
      }
   }

   public static void refresh(float x, float y, float s, int c) {
      float cx = x + s / 2.0F;
      float cy = y + s / 2.0F;
      Render2D.arc(cx, cy, s * 0.34F, (float)Math.toRadians(-60.0), (float)Math.toRadians(200.0), s * 0.12F, c);
      float ax = cx + (float)Math.cos(Math.toRadians(-60.0)) * s * 0.34F;
      float ay = cy + (float)Math.sin(Math.toRadians(-60.0)) * s * 0.34F;
      Render2D.triangle(ax + s * 0.2F, ay - s * 0.02F, ax - s * 0.1F, ay - s * 0.14F, ax - s * 0.02F, ay + s * 0.2F, c);
   }

   public static void speed(float x, float y, float s, int c) {
      float w = s * 0.13F;

      for (int i = 0; i < 2; i++) {
         float ox = x + s * (0.15F + i * 0.33F);
         Render2D.line(ox, y + s * 0.2F, ox + s * 0.28F, y + s * 0.5F, w, c);
         Render2D.line(ox + s * 0.28F, y + s * 0.5F, ox, y + s * 0.8F, w, c);
      }
   }

   public static void person(float x, float y, float s, int c) {
      Render2D.circle(x + s / 2.0F, y + s * 0.3F, s * 0.22F, c);
      Render2D.beginPath();
      NanoVG.nvgRoundedRectVarying(Render2D.vg(), x + s * 0.12F, y + s * 0.58F, s * 0.76F, s * 0.38F, s * 0.3F, s * 0.3F, s * 0.08F, s * 0.08F);
      Render2D.fillPath(c);
   }

   public static void cross(float x, float y, float s, int c) {
      Render2D.line(x + s * 0.25F, y + s * 0.25F, x + s * 0.75F, y + s * 0.75F, s * 0.14F, c);
      Render2D.line(x + s * 0.75F, y + s * 0.25F, x + s * 0.25F, y + s * 0.75F, s * 0.14F, c);
   }

   public static void check(float x, float y, float s, int c) {
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.2F, y + s * 0.52F);
      Render2D.lineTo(x + s * 0.42F, y + s * 0.74F);
      Render2D.lineTo(x + s * 0.8F, y + s * 0.3F);
      Render2D.strokePath(s * 0.16F, c);
   }
}
