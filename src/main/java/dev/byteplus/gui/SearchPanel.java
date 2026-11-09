package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.render.Icons;
import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.glfw.GLFW;

final class SearchPanel extends Panel {
   private static final float BOX_H = 20.0F;
   private final StringBuilder query = new StringBuilder();
   private final Map<Module, ModuleButton> cache = new HashMap<>();
   private final List<ModuleButton> results = new ArrayList<>();
   private boolean focused = true;
   private float caretTime;
   private float focusAnim = 1.0F;

   SearchPanel(float x, float y) {
      super("SEARCH", x, y);
   }

   @Override
   String title() {
      return "Search";
   }

   @Override
   void drawIcon(float x, float y, float size, int color) {
      Icons.search(x, y, size, color);
   }

   @Override
   List<ModuleButton> buttons() {
      return this.results;
   }

   private void refresh() {
      this.results.clear();
      String q = this.query.toString().trim().toLowerCase();
      if (!q.isEmpty()) {
         for (Module m : BytePlus.modules().all()) {
            if (m.getName().toLowerCase().contains(q) || m.getCategory().displayName.toLowerCase().contains(q)) {
               this.results.add(this.cache.computeIfAbsent(m, ModuleButton::new));
            }
         }
      }
   }

   @Override
   float measureExtra() {
      return 20.0F;
   }

   @Override
   float drawExtra(float x, float y, float w, float dt, float mx, float my) {
      this.caretTime += dt;
      this.focusAnim = Anim.approach(this.focusAnim, this.focused ? 1.0F : 0.0F, 14.0F, dt);
      float bx = x + 5.0F;
      float by = y + 4.0F;
      float bw = w - 10.0F;
      float bh = 14.0F;
      Render2D.roundRect(bx, by, bw, bh, 3.0F, Theme.SETTINGS_BG);
      Render2D.roundRectOutline(bx, by, bw, bh, 3.0F, 0.8F, Anim.lerpColor(587202559, Render2D.withAlpha(Theme.ACCENT, 0.8F), this.focusAnim));
      float ts = 6.8F;
      float tx = bx + 5.0F;
      float ty = by + (bh - ts) / 2.0F;
      String q = this.query.toString();
      Render2D.save();
      Render2D.scissor(bx + 2.0F, by, bw - 4.0F, bh);
      float end;
      if (q.isEmpty()) {
         Render2D.text("TYPE TO SEARCH...", tx, ty, ts, Theme.TEXT_DIM, 1, 0.4F);
         end = tx;
      } else {
         float qw = Render2D.textWidth(q, ts, 1, 0.2F);
         float shift = Math.max(0.0F, qw - (bw - 14.0F));
         end = Render2D.text(q, tx - shift, ty, ts, Theme.TEXT, 1, 0.2F);
      }

      if (this.focused && this.caretTime % 1.0F < 0.55F) {
         Render2D.rect(end + 0.8F, by + 3.5F, 0.9F, bh - 7.0F, Theme.ACCENT);
      }

      Render2D.restore();
      return 20.0F;
   }

   @Override
   boolean clickExtra(float mx, float my, int button) {
      float by = this.y + 18.0F;
      boolean inBox = mx >= this.x && mx <= this.x + 104.0F && my >= by && my <= by + 20.0F;
      if (inBox) {
         this.focused = true;
         if (button == 1) {
            this.query.setLength(0);
            this.refresh();
         }

         return true;
      } else {
         return false;
      }
   }

   void unfocus() {
      this.focused = false;
   }

   @Override
   boolean keyPressed(int key) {
      if (super.keyPressed(key)) {
         return true;
      }

      if (!this.focused) {
         return false;
      }

      if (key == 259 && !this.query.isEmpty()) {
         boolean ctrl = GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), 341) == 1;
         if (ctrl) {
            this.query.setLength(0);
         } else {
            this.query.deleteCharAt(this.query.length() - 1);
         }

         this.caretTime = 0.0F;
         this.refresh();
         return true;
      } else {
         return false;
      }
   }

   @Override
   boolean charTyped(String s) {
      if (this.focused && this.query.length() <= 32) {
         for (char c : s.toCharArray()) {
            if (c >= ' ' && c != 127) {
               this.query.append(c);
            }
         }

         this.caretTime = 0.0F;
         this.refresh();
         return true;
      } else {
         return false;
      }
   }
}
