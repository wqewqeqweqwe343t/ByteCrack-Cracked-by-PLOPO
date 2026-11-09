package dev.byteplus.gui;

import dev.byteplus.render.Render2D;
import dev.byteplus.util.Theme;
import java.awt.Color;
import java.util.function.Consumer;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

final class ColorPickerPopup extends Popup {
   private final String title;
   private final int original;
   private final Consumer<Integer> onOk;
   private float hue;
   private float sat;
   private float bri;
   private int dragging;
   private static final float SQ = 120.0F;
   private static final float HUE_W = 10.0F;

   ColorPickerPopup(String title, int argb, Consumer<Integer> onOk) {
      this.title = title;
      this.original = argb;
      this.onOk = onOk;
      float[] hsb = Color.RGBtoHSB(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, null);
      this.hue = hsb[0];
      this.sat = hsb[1];
      this.bri = hsb[2];
   }

   @Override
   String title() {
      return "Color: " + this.title;
   }

   @Override
   void layout(float sw, float sh) {
      this.w = 170.0F;
      this.h = 178.0F;
      this.x = (sw - this.w) / 2.0F;
      this.y = (sh - this.h) / 2.0F;
   }

   private int current() {
      return 0xFF000000 | Color.HSBtoRGB(this.hue, this.sat, this.bri) & 16777215;
   }

   private float sqX() {
      return this.x + 10.0F;
   }

   private float sqY() {
      return this.y + 26.0F;
   }

   @Override
   void drawContent(float mx, float my, float dt) {
      if (this.dragging == 1) {
         this.sat = clamp((mx - this.sqX()) / 120.0F);
         this.bri = 1.0F - clamp((my - this.sqY()) / 120.0F);
      }

      if (this.dragging == 2) {
         this.hue = clamp((my - this.sqY()) / 120.0F);
      }

      long vg = Render2D.vg();
      float sx = this.sqX();
      float sy = this.sqY();
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint p = NVGPaint.malloc(stack);
         int pure = 0xFF000000 | Color.HSBtoRGB(this.hue, 1.0F, 1.0F) & 16777215;
         Render2D.roundRect(sx, sy, 120.0F, 120.0F, 4.0F, pure);
         NanoVG.nvgLinearGradient(vg, sx, sy, sx + 120.0F, sy, Render2D.col(-1), colB(stack, 16777215), p);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRoundedRect(vg, sx, sy, 120.0F, 120.0F, 4.0F);
         NanoVG.nvgFillPaint(vg, p);
         NanoVG.nvgFill(vg);
         NanoVG.nvgLinearGradient(vg, sx, sy, sx, sy + 120.0F, Render2D.col(0), colB(stack, -16777216), p);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRoundedRect(vg, sx, sy, 120.0F, 120.0F, 4.0F);
         NanoVG.nvgFillPaint(vg, p);
         NanoVG.nvgFill(vg);
      } catch (Throwable var18) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var17) {
               var18.addSuppressed(var17);
            }
         }

         throw var18;
      }

      if (stack != null) {
         stack.close();
      }

      float cx = sx + this.sat * 120.0F;
      float cy = sy + (1.0F - this.bri) * 120.0F;
      Render2D.circleOutline(cx, cy, 3.5F, 1.4F, -1);
      Render2D.circleOutline(cx, cy, 4.6F, 0.8F, -2013265920);
      float hx = sx + 120.0F + 10.0F;
      int steps = 12;

      for (int i = 0; i < steps; i++) {
         int c1 = 0xFF000000 | Color.HSBtoRGB((float)i / steps, 1.0F, 1.0F) & 16777215;
         int c2 = 0xFF000000 | Color.HSBtoRGB((float)(i + 1) / steps, 1.0F, 1.0F) & 16777215;
         float yy = sy + 120.0F * i / steps;
         float r = i != 0 && i != steps - 1 ? 0.0F : 3.0F;
         Render2D.verticalGradient(hx, yy, 10.0F, 120.0F / steps + 0.5F, r, c1, c2);
      }

      float hy = sy + this.hue * 120.0F;
      Render2D.roundRect(hx - 1.5F, hy - 1.5F, 13.0F, 3.0F, 1.5F, -1);
      float py = sy + 120.0F + 8.0F;
      float pw = 67.0F;
      Render2D.roundRect(sx, py, pw, 10.0F, 3.0F, 0xFF000000 | this.original);
      Render2D.roundRect(sx + pw + 6.0F, py, pw, 10.0F, 3.0F, this.current());
      Render2D.text("ORIGINAL", sx + pw / 2.0F - Render2D.textWidth("ORIGINAL", 6.0F, 1, 0.4F) / 2.0F, py + 12.0F, 6.0F, Theme.TEXT_DIM, 1, 0.4F);
      Render2D.text("NEW", sx + pw + 6.0F + pw / 2.0F - Render2D.textWidth("NEW", 6.0F, 1, 0.4F) / 2.0F, py + 12.0F, 6.0F, Theme.TEXT_DIM, 1, 0.4F);
      button("CANCEL", this.x + this.w - 92.0F, this.y + this.h - 18.0F, 40.0F, 13.0F, Theme.TOGGLE_OFF, Theme.TEXT, mx, my);
      button("OK", this.x + this.w - 48.0F, this.y + this.h - 18.0F, 40.0F, 13.0F, Theme.ACCENT, Theme.ACCENT_DARK, mx, my);
   }

   private static NVGColor colB(MemoryStack stack, int argb) {
      NVGColor c = NVGColor.malloc(stack);
      c.r((argb >> 16 & 0xFF) / 255.0F);
      c.g((argb >> 8 & 0xFF) / 255.0F);
      c.b((argb & 0xFF) / 255.0F);
      c.a((argb >>> 24 & 0xFF) / 255.0F);
      return c;
   }

   private static float clamp(float v) {
      return Math.max(0.0F, Math.min(1.0F, v));
   }

   @Override
   boolean mouseClicked(float mx, float my, int button) {
      if (!this.contains(mx, my)) {
         this.close();
         return true;
      } else if (in(mx, my, this.sqX(), this.sqY(), 120.0F, 120.0F)) {
         this.dragging = 1;
         return true;
      } else if (in(mx, my, this.sqX() + 120.0F + 8.0F, this.sqY(), 14.0F, 120.0F)) {
         this.dragging = 2;
         return true;
      } else if (in(mx, my, this.x + this.w - 92.0F, this.y + this.h - 18.0F, 40.0F, 13.0F)) {
         this.close();
         return true;
      } else if (in(mx, my, this.x + this.w - 48.0F, this.y + this.h - 18.0F, 40.0F, 13.0F)) {
         this.onOk.accept(this.current());
         this.close();
         return true;
      } else {
         return true;
      }
   }

   @Override
   void mouseReleased() {
      this.dragging = 0;
   }

   @Override
   boolean keyPressed(int key) {
      if (key == 256) {
         this.close();
         return true;
      } else if (key == 257) {
         this.onOk.accept(this.current());
         this.close();
         return true;
      } else {
         return true;
      }
   }
}
