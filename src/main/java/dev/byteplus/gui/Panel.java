package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.List;

abstract class Panel {
   static final float WIDTH = 104.0F;
   static final float HEADER_H = 18.0F;
   static final float RADIUS = 4.0F;
   final String id;
   float x;
   float y;
   boolean collapsed;
   private float collapseAnim = 1.0F;
   private boolean dragging;
   private float dragOffX;
   private float dragOffY;

   Panel(String id, float x, float y) {
      this.id = id;
      this.x = x;
      this.y = y;
   }

   abstract String title();

   abstract void drawIcon(float var1, float var2, float var3, int var4);

   abstract List<ModuleButton> buttons();

   float drawExtra(float x, float y, float w, float dt, float mx, float my) {
      return 0.0F;
   }

   boolean clickExtra(float mx, float my, int button) {
      return false;
   }

   float bodyHeight() {
      float h = 0.0F;

      for (ModuleButton b : this.buttons()) {
         h += b.height();
      }

      return h;
   }

   void draw(float dt, float mx, float my) {
      if (this.dragging) {
         this.x = mx - this.dragOffX;
         this.y = my - this.dragOffY;
      }

      this.collapseAnim = Anim.approach(this.collapseAnim, this.collapsed ? 0.0F : 1.0F, 12.0F, dt);
      float open = Anim.easeOutCubic(this.collapseAnim);
      float extraH = this.measureExtra();
      float body = (extraH + this.bodyHeight() + (this.buttons().isEmpty() && extraH == 0.0F ? 0 : 3)) * open;
      float total = 18.0F + body;
      Render2D.shadow(this.x, this.y, 104.0F, total, 4.0F, 10.0F, 1711276032);
      if (BytePlus.modules().clickGui.panelGlow.isOn()) {
         Render2D.shadow(this.x, this.y, 104.0F, total, 4.0F, 16.0F, Theme.ACCENT & 16777215 | 939524096);
      }

      Render2D.roundRect(this.x, this.y, 104.0F, total, 4.0F, Theme.PANEL_BG);
      float headerBottomRadius = body < 1.0F ? 4.0F : 0.0F;
      Render2D.roundRect(this.x, this.y, 104.0F, 18.0F, 4.0F, 4.0F, headerBottomRadius, headerBottomRadius, Theme.PANEL_HEADER);
      Render2D.roundRectOutline(this.x + 0.25F, this.y + 0.25F, 103.5F, total - 0.5F, 4.0F, 0.5F, Theme.OUTLINE);
      this.drawIcon(this.x + 7.0F, this.y + 5.0F, 8.0F, Theme.HEADER_TEXT);
      Render2D.text(this.title().toUpperCase(), this.x + 19.0F, this.y + 5.2F, 7.6F, Theme.HEADER_TEXT, 1, 0.55F);
      float cx = this.x + 104.0F - 10.0F;
      float cy = this.y + 9.0F;
      boolean hovMinus = mx >= this.x + 104.0F - 18.0F && mx <= this.x + 104.0F && my >= this.y && my <= this.y + 18.0F;
      int minusColor = hovMinus ? -1 : Theme.TEXT_DIM;
      Render2D.line(cx - 2.6F, cy, cx + 2.6F, cy, 1.2F, minusColor);
      if (this.collapseAnim < 0.999F) {
         float v = 2.6F * (1.0F - open);
         Render2D.line(cx, cy - v, cx, cy + v, 1.2F, minusColor);
      }

      if (body < 0.5F) {
         for (ModuleButton b : this.buttons()) {
            b.x = -9999.0F;
            b.w = 0.0F;
         }
      } else {
         Render2D.save();
         Render2D.scissor(this.x, this.y + 18.0F, 104.0F, body);
         float cy2 = this.y + 18.0F;
         cy2 += this.drawExtra(this.x, cy2, 104.0F, dt, mx, my);

         for (ModuleButton b : this.buttons()) {
            b.draw(this.x, cy2, 104.0F, dt, mx, my);
            cy2 += b.height();
         }

         Render2D.restore();
      }
   }

   float measureExtra() {
      return 0.0F;
   }

   boolean isOverHeader(float mx, float my) {
      return mx >= this.x && mx <= this.x + 104.0F && my >= this.y && my <= this.y + 18.0F;
   }

   boolean mouseClicked(float mx, float my, int button) {
      if (!this.isOverHeader(mx, my)) {
         if (this.collapsed) {
            return false;
         }

         if (this.clickExtra(mx, my, button)) {
            return true;
         }

         for (ModuleButton b : this.buttons()) {
            if (b.mouseClicked(mx, my, button)) {
               return true;
            }
         }

         return false;
      } else {
         if (mx >= this.x + 104.0F - 18.0F || button == 1) {
            this.collapsed = !this.collapsed;
         } else if (button == 0) {
            this.dragging = true;
            this.dragOffX = mx - this.x;
            this.dragOffY = my - this.y;
         }

         return true;
      }
   }

   void mouseReleased() {
      this.dragging = false;

      for (ModuleButton b : this.buttons()) {
         b.mouseReleased();
      }
   }

   boolean keyPressed(int key) {
      for (ModuleButton b : this.buttons()) {
         if (b.keyPressed(key)) {
            return true;
         }
      }

      return false;
   }

   boolean isListening() {
      for (ModuleButton b : this.buttons()) {
         if (b.isListening() || b.isEditing()) {
            return true;
         }
      }

      return false;
   }

   boolean charTyped(String chars) {
      for (ModuleButton b : this.buttons()) {
         if (b.charTyped(chars)) {
            return true;
         }
      }

      return false;
   }

   void stopEditing() {
      for (ModuleButton b : this.buttons()) {
         b.stopEditing();
      }
   }

   boolean contains(float mx, float my) {
      float h = 18.0F + (this.collapsed ? 0.0F : this.measureExtra() + this.bodyHeight() + 3.0F);
      return mx >= this.x && mx <= this.x + 104.0F && my >= this.y && my <= this.y + h;
   }

   ModuleButton hoveredButton(float mx, float my) {
      if (this.collapsed) {
         return null;
      }

      for (ModuleButton b : this.buttons()) {
         if (b.isRowHovered(mx, my)) {
            return b;
         }
      }

      return null;
   }
}
