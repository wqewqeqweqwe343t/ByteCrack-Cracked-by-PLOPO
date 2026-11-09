package dev.byteplus.gui;

import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;

abstract class Popup {
   protected float x;
   protected float y;
   protected float w;
   protected float h;
   private float anim;
   boolean closing;

   abstract String title();

   abstract void layout(float var1, float var2);

   abstract void drawContent(float var1, float var2, float var3);

   boolean mouseClicked(float mx, float my, int button) {
      return true;
   }

   void mouseReleased() {
   }

   boolean keyPressed(int key) {
      return false;
   }

   boolean charTyped(String chars) {
      return false;
   }

   void scroll(double amount) {
   }

   void close() {
      this.closing = true;
   }

   boolean finished() {
      return this.closing && this.anim <= 0.02F;
   }

   boolean contains(float mx, float my) {
      return mx >= this.x && mx <= this.x + this.w && my >= this.y && my <= this.y + this.h;
   }

   void draw(float sw, float sh, float mx, float my, float dt) {
      this.anim = Anim.approach(this.anim, this.closing ? 0.0F : 1.0F, 14.0F, dt);
      float v = Anim.easeOutCubic(this.anim);
      this.layout(sw, sh);
      Render2D.save();
      Render2D.rect(0.0F, 0.0F, sw, sh, Render2D.withAlpha(1711276032, v));
      Render2D.globalAlpha(v);
      float s = 0.94F + 0.06F * v;
      Render2D.translate(this.x + this.w / 2.0F, this.y + this.h / 2.0F);
      Render2D.scale(s);
      Render2D.translate(-(this.x + this.w / 2.0F), -(this.y + this.h / 2.0F));
      Render2D.shadow(this.x, this.y, this.w, this.h, 7.0F, 16.0F, Integer.MIN_VALUE);
      Render2D.roundRect(this.x, this.y, this.w, this.h, 7.0F, Theme.PANEL_BG);
      Render2D.roundRectOutline(this.x, this.y, this.w, this.h, 7.0F, 0.6F, Theme.OUTLINE);
      String t = this.title().toUpperCase();
      float tw = Render2D.textWidth(t, 7.8F, 1, 0.6F);
      Render2D.text(t, this.x + (this.w - tw) / 2.0F, this.y + 6.0F, 7.8F, Theme.TEXT, 1, 0.6F);
      Render2D.rect(this.x + 6.0F, this.y + 19.0F, this.w - 12.0F, 0.7F, Render2D.withAlpha(Theme.ACCENT, 0.7F));
      this.drawContent(mx, my, dt);
      Render2D.restore();
   }

   static boolean button(String label, float bx, float by, float bw, float bh, int bg, int fg, float mx, float my) {
      boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
      Render2D.roundRect(bx, by, bw, bh, 4.0F, hov ? Anim.lerpColor(bg, -1, 0.12F) : bg);
      float tw = Render2D.textWidth(label, 7.0F, 1, 0.6F);
      Render2D.text(label, bx + (bw - tw) / 2.0F, by + (bh - 7.0F) / 2.0F, 7.0F, fg, 1, 0.6F);
      return hov;
   }

   static boolean in(float mx, float my, float bx, float by, float bw, float bh) {
      return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
   }
}
