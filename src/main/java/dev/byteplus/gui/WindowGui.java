package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.byteplus.BytePlusModule;
import dev.byteplus.render.Icons;
import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_310;

final class WindowGui {
   static final float W = 408.0F;
   static final float H = 268.0F;
   private static final float RAIL = 46.0F;
   private static final float R = 14.0F;
   private static final float PAD = 11.0F;
   private static final float HEADER = 44.0F;
   private static final float FOOTER = 20.0F;
   private static final float ROW_GAP = 3.0F;
   private Category selected = Category.COMBAT;
   private final Map<Module, ModuleButton> buttons = new HashMap<>();
   private final Map<Category, float[]> railAnim = new EnumMap<>(Category.class);
   private final Map<Category, Float> scrollTarget = new EnumMap<>(Category.class);
   private float scroll;
   private float searchScrollTarget;
   private float pageAge = 10.0F;
   private float appear = 1.0F;
   private float age;
   private float pillY = -1.0F;
   private float pillV;
   private final StringBuilder query = new StringBuilder();
   private boolean searchFocused;
   private float caret;
   private float searchAnim;
   private float scale = 1.0F;
   private float wx;
   private float wy;
   private float dragX;
   private float dragY;
   private float offX;
   private float offY;
   private boolean dragging;
   private Category tooltipCat;
   private float tooltipAnim;
   private float tooltipY;
   private float ringAnim;
   private float spotX = -1.0F;
   private float spotY = -1.0F;

   private ModuleButton button(Module m) {
      return this.buttons.computeIfAbsent(m, k -> new ModuleButton(k, true));
   }

   private boolean searching() {
      return !this.query.toString().trim().isEmpty();
   }

   private List<Module> shownModules() {
      String q = this.query.toString().trim().toLowerCase();
      if (q.isEmpty()) {
         return BytePlus.modules().byCategory(this.selected);
      }

      List<Module> out = new ArrayList<>();

      for (Module m : BytePlus.modules().all()) {
         if (m.getName().toLowerCase().contains(q) || m.getDescription().toLowerCase().contains(q)) {
            out.add(m);
         }
      }

      return out;
   }

   void onOpen() {
      this.appear = 0.0F;
      this.pageAge = 0.0F;
   }

   private float listX() {
      return 57.0F;
   }

   private float listY() {
      return 46.0F;
   }

   private float listW() {
      return 342.0F;
   }

   private float listH() {
      return 196.0F;
   }

   private float railY(int i) {
      return 46 + i * 27;
   }

   private int indexOf(Category c) {
      Category[] cats = Category.values();

      for (int i = 0; i < cats.length; i++) {
         if (cats[i] == c) {
            return i;
         }
      }

      return 0;
   }

   void draw(float sw, float sh, float mx, float my, float dt) {
      this.age += dt;
      this.pageAge += dt;
      this.appear = Math.min(1.0F, this.appear + dt * 2.6F);
      float user = BytePlus.modules().clickGui.scale.getFloat();
      this.scale = Math.min(user / 1.6F * 1.2F, Math.min(sw * 0.8F / 408.0F, sh * 0.74F / 268.0F));
      if (this.dragging) {
         this.offX = mx - this.dragX;
         this.offY = my - this.dragY;
      }

      this.wx = (sw - 408.0F * this.scale) / 2.0F + this.offX;
      this.wy = (sh - 268.0F * this.scale) / 2.0F + this.offY;
      float lx = (mx - this.wx) / this.scale;
      float ly = (my - this.wy) / this.scale;
      if (this.spotX < 0.0F) {
         this.spotX = lx;
         this.spotY = ly;
      }

      this.spotX = this.spotX + (lx - this.spotX) * (1.0F - (float)Math.exp(-dt * 12.0F));
      this.spotY = this.spotY + (ly - this.spotY) * (1.0F - (float)Math.exp(-dt * 12.0F));
      this.caret += dt;
      this.searchAnim = Anim.approach(this.searchAnim, this.searchFocused ? 1.0F : 0.0F, 14.0F, dt);
      float pop = Anim.easeOutCubic(this.appear);
      float s = 0.9F + 0.1F * outBack(this.appear);
      Render2D.save();
      Render2D.translate(this.wx + 408.0F * this.scale / 2.0F, this.wy + 268.0F * this.scale / 2.0F);
      Render2D.scale(this.scale * s);
      Render2D.translate(-204.0F, -134.0F + (1.0F - pop) * 10.0F);
      Render2D.globalAlpha(pop);
      this.drawShell(lx, ly);
      this.drawRail(lx, ly, dt);
      this.drawHeader(dt);
      this.drawList(lx, ly, dt);
      this.drawFooter();
      this.drawRailTooltip(dt);
      Render2D.restore();
   }

   private static float outBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(t - 1.0F, 3.0) + c1 * (float)Math.pow(t - 1.0F, 2.0);
   }

   private void drawShell(float lx, float ly) {
      int base = Theme.PANEL_BG | 0xFF000000;
      float breathe = 0.5F + 0.5F * (float)Math.sin(this.age * 1.3);
      Render2D.shadow(0.0F, 6.0F, 408.0F, 268.0F, 14.0F, 40.0F, -1610612736);
      if (BytePlus.modules().clickGui.panelGlow.isOn()) {
         Render2D.shadow(
            0.0F, 0.0F, 408.0F, 268.0F, 14.0F, 34.0F + 8.0F * breathe, Render2D.withAlpha(Theme.ACCENT & 16777215 | 0xFF000000, 0.16F + 0.08F * breathe)
         );
      }

      Render2D.roundRect(0.0F, 0.0F, 408.0F, 268.0F, 14.0F, Render2D.withAlpha(base, 0.84F));
      Render2D.verticalGradient(0.0F, 0.0F, 408.0F, 70.0F, 14.0F, Render2D.withAlpha(-1, 0.05F), 0);
      if (this.spotX > -50.0F && this.spotX < 458.0F && this.spotY > -50.0F && this.spotY < 318.0F) {
         Render2D.radialRoundRect(0.0F, 0.0F, 408.0F, 268.0F, 14.0F, this.spotX, this.spotY, 0.0F, 150.0F, Render2D.withAlpha(Theme.ACCENT, 0.06F), 0);
         Render2D.radialRoundRect(0.0F, 0.0F, 408.0F, 268.0F, 14.0F, this.spotX, this.spotY, 0.0F, 90.0F, Render2D.withAlpha(-1, 0.035F), 0);
      }

      Render2D.roundRect(0.0F, 0.0F, 46.0F, 268.0F, 14.0F, 0.0F, 0.0F, 14.0F, Render2D.withAlpha(-16777216, 0.3F));
      Render2D.rect(46.0F, 10.0F, 0.5F, 248.0F, Render2D.withAlpha(-1, 0.07F));
      float a = this.age * 0.45F;
      float cx = 204.0F;
      float cy = 134.0F;
      float ex = (float)Math.cos(a) * 408.0F * 0.6F;
      float ey = (float)Math.sin(a) * 268.0F * 0.6F;
      Render2D.gradientOutline(
         0.4F, 0.4F, 407.2F, 267.2F, 14.0F, 0.9F, cx - ex, cy - ey, cx + ex, cy + ey, Render2D.withAlpha(Theme.ACCENT, 0.75F), Render2D.withAlpha(-1, 0.06F)
      );
      Render2D.line(14.0F, 0.9F, 394.0F, 0.9F, 0.6F, Render2D.withAlpha(-1, 0.12F));
   }

   private void drawRail(float lx, float ly, float dt) {
      float pulse = 0.5F + 0.5F * (float)Math.sin(this.age * 2.0F);
      Render2D.glow(23.0F, 23.0F, 16.0F + 3.0F * pulse, Render2D.withAlpha(Theme.ACCENT, 0.18F));
      Render2D.image(Render2D.modImage("/assets/byteplus/textures/logo.png"), 12.0F, 12.0F, 22.0F, 22.0F, 1.0F);
      this.tooltipCat = null;
      Category[] cats = Category.values();
      float target = this.railY(this.indexOf(this.selected));
      if (this.pillY < 0.0F) {
         this.pillY = target;
      }

      this.pillV = this.pillV + (target - this.pillY) * 260.0F * dt;
      this.pillV = this.pillV * (float)Math.exp(-dt * 20.0F);
      this.pillY = this.pillY + this.pillV * dt;
      float stretch = Math.min(14.0F, Math.abs(this.pillV) * 0.035F);
      if (!this.searching()) {
         float py = this.pillY - stretch / 2.0F;
         float ph = 24.0F + stretch;
         Render2D.shadow(8.0F, py, 30.0F, ph, 8.0F, 10.0F, Render2D.withAlpha(Theme.ACCENT & 16777215 | 0xFF000000, 0.35F));
         Render2D.verticalGradient(8.0F, py, 30.0F, ph, 8.0F, Render2D.withAlpha(Theme.ACCENT, 0.32F), Render2D.withAlpha(Theme.HUD_ACCENT_2, 0.18F));
         Render2D.roundRectOutline(8.3F, py + 0.3F, 29.4F, ph - 0.6F, 8.0F, 0.6F, Render2D.withAlpha(Theme.ACCENT, 0.6F));
         Render2D.roundRect(2.0F, py + ph / 2.0F - 6.0F, 2.2F, 12.0F, 1.1F, Theme.ACCENT);
      }

      for (int i = 0; i < cats.length; i++) {
         Category c = cats[i];
         float y = this.railY(i);
         float[] a = this.railAnim.computeIfAbsent(c, k -> new float[2]);
         boolean hov = lx >= 4.0F && lx <= 42.0F && ly >= y && ly <= y + 24.0F;
         if (hov) {
            this.tooltipCat = c;
            this.tooltipY = y;
         }

         boolean sel = c == this.selected && !this.searching();
         a[0] = Anim.approach(a[0], sel ? 1.0F : 0.0F, 14.0F, dt);
         a[1] = Anim.approach(a[1], hov ? 1.0F : 0.0F, 18.0F, dt);
         if (a[1] > 0.01F && !sel) {
            Render2D.roundRect(8.0F, y, 30.0F, 24.0F, 8.0F, Render2D.withAlpha(-1, 0.06F * a[1]));
         }

         float s = 10.5F + a[1] * 1.0F;
         Icons.category(
            c,
            (46.0F - s) / 2.0F,
            y + (24.0F - s) / 2.0F,
            s,
            Anim.lerpColor(Anim.lerpColor(Render2D.withAlpha(Theme.TEXT_DIM, 0.85F), Theme.TEXT, a[1]), Theme.ACCENT, a[0])
         );
      }
   }

   private void drawRailTooltip(float dt) {
      this.tooltipAnim = Anim.approach(this.tooltipAnim, this.tooltipCat != null ? 1.0F : 0.0F, 16.0F, dt);
      if (this.tooltipCat != null && !(this.tooltipAnim < 0.02F)) {
         String t = this.tooltipCat.displayName;
         float tw = Render2D.textWidth(t, 6.4F, 1, 0.3F) + 14.0F;
         float x = 50.0F + (1.0F - this.tooltipAnim) * -5.0F;
         float y = this.tooltipY + 5.0F;
         Render2D.save();
         Render2D.globalAlpha(this.tooltipAnim);
         Render2D.shadow(x, y, tw, 14.0F, 5.0F, 8.0F, 1879048192);
         Render2D.roundRect(x, y, tw, 14.0F, 5.0F, Theme.mix(Theme.PANEL_BG | 0xFF000000, -1, 0.1F));
         Render2D.roundRectOutline(x + 0.3F, y + 0.3F, tw - 0.6F, 13.4F, 5.0F, 0.5F, Render2D.withAlpha(Theme.ACCENT, 0.4F));
         Render2D.triangle(x - 3.0F, y + 7.0F, x + 0.5F, y + 4.5F, x + 0.5F, y + 9.5F, Theme.mix(Theme.PANEL_BG | 0xFF000000, -1, 0.1F));
         Render2D.text(t, x + 7.0F, y + 4.0F, 6.4F, Theme.TEXT, 1, 0.3F);
         Render2D.restore();
      }
   }

   private void drawHeader(float dt) {
      List<Module> mods = this.shownModules();
      int on = 0;

      for (Module m : mods) {
         if (m.isEnabled()) {
            on++;
         }
      }

      String title = this.searching() ? "Search" : this.selected.displayName;
      float a = Anim.easeOutCubic(Math.min(1.0F, this.pageAge / 0.35F));
      float x = this.listX() + 2.0F;
      Render2D.save();
      Render2D.globalAlpha(a);
      Render2D.translate(0.0F, (1.0F - a) * 4.0F);
      Render2D.textGradient(title, x, 12.0F, 12.5F, -1, Theme.mix(Theme.ACCENT, -1, 0.35F), 1, 0.2F);
      Render2D.text(mods.size() + (mods.size() == 1 ? " module" : " modules") + "  ·  " + on + " enabled", x, 27.5F, 5.8F, Theme.TEXT_DIM, 0);
      Render2D.restore();
      float frac = mods.isEmpty() ? 0.0F : (float)on / mods.size();
      this.ringAnim = Anim.approach(this.ringAnim, frac, 6.0F, dt);
      float rcx = 269.0F;
      float rcy = 21.0F;
      Render2D.circleOutline(rcx, rcy, 8.0F, 2.0F, Render2D.withAlpha(-1, 0.1F));
      if (this.ringAnim > 0.005F) {
         Render2D.arc(rcx, rcy, 8.0F, (float) (-Math.PI / 2), (float)((-Math.PI / 2) + (Math.PI * 2) * this.ringAnim), 2.0F, Theme.ACCENT);
      }

      Render2D.textCentered(String.valueOf(on), rcx, rcy + 0.3F, 6.0F, Theme.TEXT, 1);
      float bw = 104.0F;
      float bh = 18.0F;
      float bx = 397.0F - bw;
      float by = 12.0F;
      Render2D.roundRect(bx, by, bw, bh, bh / 2.0F, Render2D.withAlpha(-16777216, 0.32F));
      if (this.searchAnim > 0.01F) {
         Render2D.shadow(bx, by, bw, bh, bh / 2.0F, 8.0F, Render2D.withAlpha(Theme.ACCENT & 16777215 | 0xFF000000, 0.3F * this.searchAnim));
      }

      Render2D.roundRectOutline(bx, by, bw, bh, bh / 2.0F, 0.6F, Anim.lerpColor(Render2D.withAlpha(-1, 0.1F), Theme.ACCENT, this.searchAnim));
      Icons.search(bx + 7.0F, by + 5.0F, 8.0F, Anim.lerpColor(Theme.TEXT_DIM, Theme.ACCENT, this.searchAnim));
      String q = this.query.toString();
      Render2D.save();
      Render2D.scissor(bx + 17.0F, by, bw - 24.0F, bh);
      float end = q.isEmpty()
         ? Render2D.text("Search modules", bx + 19.0F, by + 5.6F, 6.2F, Render2D.withAlpha(Theme.TEXT_DIM, 0.8F), 0)
         : Render2D.text(q, bx + 19.0F, by + 5.4F, 6.4F, Theme.TEXT, 1, 0.1F);
      if (this.searchFocused && this.caret % 1.0F < 0.55F) {
         Render2D.rect(q.isEmpty() ? bx + 18.5F : end + 0.6F, by + 4.5F, 0.8F, 9.0F, Theme.ACCENT);
      }

      Render2D.restore();
      if (q.isEmpty() && !this.searchFocused) {
         float kx = bx + bw - 15.0F;
         Render2D.roundRect(kx, by + 4.5F, 10.0F, 9.0F, 2.5F, Render2D.withAlpha(-1, 0.08F));
         Render2D.textCentered("/", kx + 5.0F, by + 9.2F, 5.6F, Theme.TEXT_DIM, 1);
      }
   }

   private float target() {
      return this.searching() ? this.searchScrollTarget : this.scrollTarget.getOrDefault(this.selected, 0.0F);
   }

   private void drawList(float lx, float ly, float dt) {
      float x = this.listX();
      float y = this.listY();
      float w = this.listW();
      float h = this.listH();
      List<Module> mods = this.shownModules();
      float total = 0.0F;

      for (Module m : mods) {
         total += this.button(m).height() + 3.0F;
      }

      float maxScroll = Math.max(0.0F, total - h);
      float t = Math.max(-maxScroll, Math.min(0.0F, this.target()));
      if (this.searching()) {
         this.searchScrollTarget = t;
      } else {
         this.scrollTarget.put(this.selected, t);
      }

      this.scroll = this.scroll + (t - this.scroll) * (1.0F - (float)Math.exp(-dt * 14.0F));
      boolean inList = lx >= x && lx <= x + w && ly >= y && ly <= y + h;
      Render2D.save();
      Render2D.scissor(x - 3.0F, y, w + 6.0F, h);
      float cy = y + this.scroll;

      for (int i = 0; i < mods.size(); i++) {
         Module m = mods.get(i);
         ModuleButton b = this.button(m);
         float bh = b.height();
         if (!(cy + bh < y - 2.0F) && !(cy > y + h + 2.0F)) {
            float ra = Anim.easeOutCubic(Math.max(0.0F, Math.min(1.0F, (this.pageAge - i * 0.03F) / 0.3F)));
            Render2D.save();
            Render2D.globalAlpha(ra);
            Render2D.translate(0.0F, (1.0F - ra) * 8.0F);
            b.draw(x, cy, w, dt, inList ? lx : -9999.0F, inList ? ly : -9999.0F);
            Render2D.restore();
            cy += bh + 3.0F;
         } else {
            b.x = -9999.0F;
            b.w = 0.0F;
            cy += bh + 3.0F;
         }
      }

      Render2D.restore();
      if (mods.isEmpty()) {
         Icons.search(x + w / 2.0F - 9.0F, y + h / 2.0F - 22.0F, 18.0F, Render2D.withAlpha(Theme.TEXT_DIM, 0.6F));
         Render2D.textCentered("No modules match \"" + this.query.toString().trim() + "\"", x + w / 2.0F, y + h / 2.0F + 6.0F, 6.6F, Theme.TEXT_DIM, 0);
      }

      int base = Render2D.withAlpha(Theme.PANEL_BG | 0xFF000000, 0.84F);
      if (this.scroll < -1.0F) {
         Render2D.verticalGradient(x - 3.0F, y, w + 6.0F, 12.0F, 0.0F, base, base & 16777215);
      }

      if (this.scroll > -maxScroll + 1.0F) {
         Render2D.verticalGradient(x - 3.0F, y + h - 14.0F, w + 6.0F, 14.0F, 0.0F, base & 16777215, base);
      }

      if (maxScroll > 0.0F) {
         float barH = Math.max(18.0F, h * h / total);
         float barY = y + -this.scroll / maxScroll * (h - barH);
         Render2D.roundRect(402.5F, barY, 2.0F, barH, 1.0F, Render2D.withAlpha(Theme.ACCENT, inList ? 0.7F : 0.35F));
      }
   }

   private void drawFooter() {
      float y = 248.0F;
      Render2D.rect(54.0F, y - 0.5F, 346.0F, 0.5F, Render2D.withAlpha(-1, 0.07F));
      class_310 mc = class_310.method_1551();
      String name = mc.method_1548() != null ? mc.method_1548().method_1676() : "Player";
      Render2D.circle(61.0F, y + 10.0F, 2.2F, -11800454);
      Render2D.text(name, 67.0F, y + 7.0F, 6.0F, Render2D.withAlpha(Theme.TEXT, 0.85F), 1, 0.2F);
      int enabled = 0;

      for (Module m : BytePlus.modules().all()) {
         if (m.isEnabled() && m.isToggleable()) {
            enabled++;
         }
      }

      String mid = enabled + " active  ·  " + BytePlus.modules().clickGui.theme.get();
      Render2D.textCentered(mid, 227.0F, y + 10.2F, 5.6F, Theme.TEXT_DIM, 0);
      String right = mc.method_47599() + " FPS  ·  Cracked By PLOPO";
      Render2D.text(right, 397.0F - Render2D.textWidth(right, 5.6F, 0), y + 7.4F, 5.6F, Theme.TEXT_DIM, 0);
   }

   private float lx(double mx) {
      return ((float)mx - this.wx) / this.scale;
   }

   private float ly(double my) {
      return ((float)my - this.wy) / this.scale;
   }

   boolean contains(double mx, double my) {
      float x = this.lx(mx);
      float y = this.ly(my);
      return x >= 0.0F && x <= 408.0F && y >= 0.0F && y <= 268.0F;
   }

   private void select(Category c) {
      if (this.selected != c || this.searching()) {
         BytePlus.modules().clickGui.playClick(1.0F);
         this.selected = c;
         this.query.setLength(0);
         this.pageAge = 0.0F;
      }
   }

   boolean mouseClicked(double mx, double my, int button) {
      if (!this.contains(mx, my)) {
         this.searchFocused = false;
         return false;
      }

      float x = this.lx(mx);
      float y = this.ly(my);
      BytePlusModule gui = BytePlus.modules().clickGui;
      float bw = 104.0F;
      float bh = 18.0F;
      float bx = 397.0F - bw;
      float by = 12.0F;
      if (x >= bx && x <= bx + bw && y >= by && y <= by + bh) {
         this.searchFocused = true;
         if (button == 1) {
            this.query.setLength(0);
         }

         return true;
      } else {
         this.searchFocused = false;
         if (x < 46.0F) {
            Category[] cats = Category.values();

            for (int i = 0; i < cats.length; i++) {
               float ry = this.railY(i);
               if (y >= ry && y <= ry + 24.0F) {
                  this.select(cats[i]);
                  return true;
               }
            }

            return true;
         } else if (y < 44.0F) {
            if (button == 0) {
               this.dragging = true;
               this.dragX = (float)mx - this.offX;
               this.dragY = (float)my - this.offY;
            }

            return true;
         } else if (!(y < this.listY()) && !(y > this.listY() + this.listH())) {
            for (Module m : this.shownModules()) {
               ModuleButton b = this.button(m);
               if (!(b.w <= 0.0F)) {
                  if (b.isRowHovered(x, y)) {
                     gui.playClick(button == 1 ? 0.85F : (m.isEnabled() ? 0.85F : 1.0F));
                  }

                  if (b.mouseClicked(x, y, button)) {
                     for (ModuleButton other : this.buttons.values()) {
                        if (other != b) {
                           other.stopEditing();
                        }
                     }

                     return true;
                  }
               }
            }

            return true;
         } else {
            return true;
         }
      }
   }

   void mouseReleased() {
      this.dragging = false;

      for (ModuleButton b : this.buttons.values()) {
         b.mouseReleased();
      }
   }

   boolean mouseScrolled(double mx, double my, double amount) {
      if (!this.contains(mx, my)) {
         return false;
      }

      float t = this.target() + (float)amount * 22.0F;
      if (this.searching()) {
         this.searchScrollTarget = t;
      } else {
         this.scrollTarget.put(this.selected, t);
      }

      return true;
   }

   boolean keyPressed(int key) {
      for (ModuleButton b : this.buttons.values()) {
         if (b.keyPressed(key)) {
            return true;
         }
      }

      if (!this.searchFocused) {
         if (key == 47) {
            this.searchFocused = true;
            return true;
         } else {
            Category[] cats = Category.values();
            if (key == 264) {
               this.select(cats[(this.indexOf(this.selected) + 1) % cats.length]);
               return true;
            } else if (key == 265) {
               this.select(cats[(this.indexOf(this.selected) - 1 + cats.length) % cats.length]);
               return true;
            } else {
               return false;
            }
         }
      } else {
         if (key == 259 && !this.query.isEmpty()) {
            this.query.deleteCharAt(this.query.length() - 1);
            this.pageAge = 0.2F;
            return true;
         }

         if (key != 256 && key != 257) {
            return true;
         }

         this.searchFocused = false;
         return true;
      }
   }

   boolean charTyped(String chars) {
      for (ModuleButton b : this.buttons.values()) {
         if (b.charTyped(chars)) {
            return true;
         }
      }

      if (!this.searchFocused) {
         if (chars.isBlank() || chars.equals("/")) {
            return false;
         }

         this.searchFocused = true;
      }

      for (char c : chars.toCharArray()) {
         if (c >= ' ' && c != 127 && this.query.length() < 28) {
            this.query.append(c);
         }
      }

      return true;
   }

   boolean isCapturingKeys() {
      if (this.searchFocused) {
         return true;
      }

      for (ModuleButton b : this.buttons.values()) {
         if (b.isListening() || b.isEditing()) {
            return true;
         }
      }

      return false;
   }

   void devSelect(Category c) {
      this.selected = c;
      this.query.setLength(0);
      this.pageAge = 0.0F;
   }

   void devExpand(String name) {
      for (Module m : BytePlus.modules().all()) {
         if (m.getName().equals(name)) {
            this.button(m).expanded = true;
         }
      }
   }

   float[] devRowCenter(String name) {
      for (Module m : BytePlus.modules().all()) {
         if (m.getName().equals(name)) {
            ModuleButton b = this.button(m);
            if (b.w <= 0.0F) {
               return null;
            }

            return new float[]{this.wx + (b.x + b.w * 0.5F) * this.scale, this.wy + (b.y + b.rowH() * 0.5F) * this.scale};
         }
      }

      return null;
   }

   void onClose() {
      this.dragging = false;
      this.searchFocused = false;

      for (ModuleButton b : this.buttons.values()) {
         b.stopEditing();
         b.mouseReleased();
      }
   }
}
