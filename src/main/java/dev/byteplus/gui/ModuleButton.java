package dev.byteplus.gui;

import dev.byteplus.module.Module;
import dev.byteplus.render.Icons;
import dev.byteplus.render.ModuleIcons;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BlockListSetting;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.InfoSetting;
import dev.byteplus.setting.KeybindSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.setting.Setting;
import dev.byteplus.setting.StringSetting;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ModuleButton {
   static final float ROW_H = 16.5F;
   static final float TEXT = 7.2F;
   static final float SPACING = 0.45F;
   private static final float PAD = 3.0F;
   private static final float H_BOOL = 13.0F;
   private static final float H_NUMBER = 21.0F;
   private static final float H_INFO = 13.0F;
   private static final float H_KEY = 16.0F;
   private static final float H_MODE = 14.0F;
   private static final float H_STRING = 25.0F;
   private static final float H_COLOR = 14.0F;
   private static final float H_BLOCKS = 16.0F;
   final Module module;
   boolean expanded;
   private float hover;
   private float toggleAnim;
   private float expandAnim;
   private boolean listening;
   private StringSetting editing;
   private float caret;
   private NumberSetting draggingSlider;
   private final List<float[]> hitboxes = new ArrayList<>();
   private final List<Setting<?>> hitSettings = new ArrayList<>();
   float x;
   float y;
   float w;
   boolean modern;
   static final float MODERN_ROW_H = 28.0F;
   private float sliderLeft;
   private float sliderWidth = 1.0F;
   private float localMx;
   private float localMy;
   private boolean wasEnabled;
   private float knobSquash;
   private final List<float[]> sparks = new ArrayList<>();
   private final Map<Setting<?>, Float> segAnim = new HashMap<>();
   private KeybindSetting listenTarget;

   ModuleButton(Module module) {
      this.module = module;
      this.toggleAnim = module.isEnabled() ? 1.0F : 0.0F;
      this.wasEnabled = module.isEnabled();
   }

   ModuleButton(Module module, boolean modern) {
      this(module);
      this.modern = modern;
   }

   float rowH() {
      return this.modern ? 28.0F : 16.5F;
   }

   boolean isListening() {
      return this.listening;
   }

   boolean isEditing() {
      return this.editing != null;
   }

   private List<Setting<?>> visibleSettings() {
      List<Setting<?>> list = new ArrayList<>();

      for (Setting<?> s : this.module.getSettings()) {
         if (s.isVisible()) {
            list.add(s);
         }
      }

      list.add(this.module.keybind);
      return list;
   }

   private static boolean segmented(ModeSetting m) {
      if (m.getModes().size() > 5) {
         return false;
      }

      int chars = 0;

      for (String s : m.getModes()) {
         chars += s.length();
      }

      return chars <= 34;
   }

   private float heightOf(Setting<?> s) {
      if (this.modern) {
         if (s instanceof NumberSetting) {
            return 24.0F;
         } else if (s instanceof ModeSetting m) {
            return segmented(m) ? 27.0F : 17.0F;
         } else if (s instanceof StringSetting) {
            return 27.0F;
         } else if (s instanceof KeybindSetting || s instanceof BlockListSetting) {
            return 17.0F;
         } else {
            return s instanceof InfoSetting ? 14.0F : 16.0F;
         }
      } else if (s instanceof NumberSetting) {
         return 21.0F;
      } else if (s instanceof KeybindSetting) {
         return 16.0F;
      } else if (s instanceof InfoSetting) {
         return 13.0F;
      } else if (s instanceof ModeSetting) {
         return 14.0F;
      } else if (s instanceof StringSetting) {
         return 25.0F;
      } else if (s instanceof ColorSetting) {
         return 14.0F;
      } else {
         return s instanceof BlockListSetting ? 16.0F : 13.0F;
      }
   }

   private float settingsHeight() {
      float h = 6.0F + (this.modern ? 4 : 0);

      for (Setting<?> s : this.visibleSettings()) {
         h += this.heightOf(s);
      }

      return h;
   }

   float height() {
      return this.rowH() + (this.settingsHeight() + (this.modern ? 4 : 0)) * Anim.easeOutCubic(this.expandAnim);
   }

   boolean isRowHovered(float mx, float my) {
      return mx >= this.x && mx <= this.x + this.w && my >= this.y && my <= this.y + this.rowH();
   }

   private void drawModernRow(float x, float y, float w, float dt) {
      float h = 28.0F;
      boolean on = this.module.isEnabled();
      if (on && !this.wasEnabled) {
         this.burst(x + w - 18.0F, y + h / 2.0F);
      }

      if (on != this.wasEnabled) {
         this.knobSquash = 1.0F;
      }

      this.wasEnabled = on;
      this.knobSquash = Math.max(0.0F, this.knobSquash - dt * 4.0F);
      if (this.toggleAnim > 0.01F) {
         Render2D.horizontalGradient(x, y, w, h, 8.0F, Render2D.withAlpha(Theme.ACCENT, 0.1F * this.toggleAnim), Render2D.withAlpha(Theme.ACCENT, 0.0F));
         Render2D.roundRect(x + 1.5F, y + 7.0F, 2.0F, (h - 14.0F) * this.toggleAnim, 1.0F, Theme.ACCENT);
      }

      float bgA = Math.max(this.hover, this.expandAnim * 0.7F);
      if (bgA > 0.01F) {
         Render2D.roundRect(x, y, w, h, 8.0F, Render2D.withAlpha(-1, 0.045F * bgA));
         if (this.hover > 0.01F) {
            Render2D.radialRoundRect(x, y, w, h, 8.0F, this.localMx, this.localMy, 0.0F, 90.0F, Render2D.withAlpha(-1, 0.07F * this.hover), 0);
         }

         Render2D.roundRectOutline(x + 0.3F, y + 0.3F, w - 0.6F, h - 0.6F, 8.0F, 0.5F, Render2D.withAlpha(-1, 0.07F * bgA));
      }

      float tile = 19.0F;
      float tx = x + 8.0F;
      float ty = y + (h - tile) / 2.0F - this.hover * 0.6F;
      if (this.toggleAnim > 0.01F) {
         Render2D.shadow(tx, ty, tile, tile, 6.0F, 9.0F, Render2D.withAlpha(Theme.ACCENT & 16777215 | 0xFF000000, 0.45F * this.toggleAnim));
      }

      Render2D.roundRect(tx, ty, tile, tile, 6.0F, Render2D.withAlpha(-1, 0.06F + 0.03F * this.hover));
      if (this.toggleAnim > 0.01F) {
         Render2D.horizontalGradient(
            tx, ty, tile, tile, 6.0F, Render2D.withAlpha(Theme.ACCENT, this.toggleAnim), Render2D.withAlpha(Theme.HUD_ACCENT_2, this.toggleAnim)
         );
      }

      Render2D.verticalGradient(tx, ty, tile, tile * 0.5F, 6.0F, Render2D.withAlpha(-1, 0.12F), 0);
      float ic = 10.0F;
      ModuleIcons.draw(
         this.module,
         tx + (tile - ic) / 2.0F,
         ty + (tile - ic) / 2.0F,
         ic,
         Anim.lerpColor(Anim.lerpColor(Theme.TEXT_DIM, Theme.TEXT, this.hover), Theme.ACCENT_DARK, this.toggleAnim)
      );
      float textX = tx + tile + 8.0F;
      float right = this.module.isToggleable() ? 46.0F : 22.0F;
      float maxW = w - (textX - x) - right;
      Render2D.text(
         fit(this.module.getName(), 7.8F, 1, 0.15F, maxW),
         textX + this.hover * 1.2F,
         y + 6.0F,
         7.8F,
         Anim.lerpColor(Theme.TEXT, -1, this.hover * 0.6F),
         1,
         0.15F
      );
      Render2D.text(
         fit(this.module.getDescription(), 5.6F, 0, 0.0F, maxW), textX + this.hover * 1.2F, y + 16.0F, 5.6F, Render2D.withAlpha(Theme.TEXT_DIM, 0.95F), 0
      );
      float cx = x + w - (this.module.isToggleable() ? 38 : 13);
      float cy = y + h / 2.0F;
      Render2D.save();
      Render2D.translate(cx, cy);
      Render2D.rotate((float)((Math.PI / 2) * Anim.easeOutCubic(this.expandAnim)));
      int chev = Anim.lerpColor(Render2D.withAlpha(Theme.TEXT_DIM, 0.7F), Theme.ACCENT, this.expandAnim);
      Render2D.line(-1.3F, -2.6F, 1.2F, 0.0F, 1.1F, chev);
      Render2D.line(1.2F, 0.0F, -1.3F, 2.6F, 1.1F, chev);
      Render2D.restore();
      if (this.module.isToggleable()) {
         float sw = 21.0F;
         float sh = 11.5F;
         float sx = x + w - sw - 9.0F;
         float sy = y + (h - sh) / 2.0F;
         float e = Anim.easeOutCubic(this.toggleAnim);
         if (this.toggleAnim > 0.01F) {
            Render2D.shadow(sx, sy, sw, sh, sh / 2.0F, 8.0F, Render2D.withAlpha(Theme.ACCENT & 16777215 | 0xFF000000, 0.55F * this.toggleAnim));
         }

         Render2D.roundRect(sx, sy, sw, sh, sh / 2.0F, Render2D.withAlpha(-1, 0.1F));
         if (this.toggleAnim > 0.01F) {
            Render2D.horizontalGradient(
               sx, sy, sw, sh, sh / 2.0F, Render2D.withAlpha(Theme.HUD_ACCENT_2, this.toggleAnim), Render2D.withAlpha(Theme.ACCENT, this.toggleAnim)
            );
         }

         float kr = sh / 2.0F - 1.7F;
         float squash = (float)Math.sin(Math.PI * Math.min(1.0F, this.knobSquash)) * 3.5F;
         float kx = sx + sh / 2.0F + (sw - sh) * e;
         Render2D.shadow(kx - kr - squash / 2.0F, sy + sh / 2.0F - kr, kr * 2.0F + squash, kr * 2.0F, kr, 3.0F, 1342177280);
         Render2D.roundRect(kx - kr - squash / 2.0F, sy + sh / 2.0F - kr, kr * 2.0F + squash, kr * 2.0F, kr, Anim.lerpColor(-2501172, -1, this.toggleAnim));
      }

      this.drawSparks(dt);
   }

   private void burst(float cx, float cy) {
      for (int i = 0; i < 12; i++) {
         double a = (Math.PI * 2) * i / 12.0 + Math.random() * 0.4;
         float sp = 20.0F + (float)Math.random() * 35.0F;
         this.sparks.add(new float[]{cx, cy, (float)Math.cos(a) * sp, (float)Math.sin(a) * sp, 1.0F});
      }
   }

   private void drawSparks(float dt) {
      for (int i = this.sparks.size() - 1; i >= 0; i--) {
         float[] s = this.sparks.get(i);
         s[4] -= dt * 2.4F;
         if (s[4] <= 0.0F) {
            this.sparks.remove(i);
         } else {
            s[0] += s[2] * dt;
            s[1] += s[3] * dt;
            s[2] *= 0.9F;
            s[3] *= 0.9F;
            Render2D.circle(s[0], s[1], 0.8F + s[4], Render2D.withAlpha(Theme.ACCENT, s[4]));
         }
      }
   }

   static String fit(String s, float size, int font, float spacing, float maxW) {
      if (s == null) {
         return "";
      }

      if (Render2D.textWidth(s, size, font, spacing) <= maxW) {
         return s;
      }

      int lo = 0;
      int hi = s.length();

      while (lo < hi) {
         int mid = (lo + hi + 1) / 2;
         if (Render2D.textWidth(s.substring(0, mid) + "…", size, font, spacing) <= maxW) {
            lo = mid;
         } else {
            hi = mid - 1;
         }
      }

      return lo <= 0 ? "" : s.substring(0, lo).stripTrailing() + "…";
   }

   void draw(float x, float y, float w, float dt, float mx, float my) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.localMx = mx;
      this.localMy = my;
      boolean hovered = this.isRowHovered(mx, my);
      this.hover = Anim.approach(this.hover, hovered ? 1.0F : 0.0F, 18.0F, dt);
      this.toggleAnim = Anim.approach(this.toggleAnim, this.module.isEnabled() ? 1.0F : 0.0F, 14.0F, dt);
      this.expandAnim = Anim.approach(this.expandAnim, this.expanded ? 1.0F : 0.0F, 11.0F, dt);
      this.caret += dt;
      if (this.modern) {
         this.drawModernRow(x, y, w, dt);
         this.drawSettings(x + 6.0F, y, w - 12.0F, mx, my, dt);
      } else {
         if (this.toggleAnim > 0.01F) {
            Render2D.rect(x, y, w, 16.5F, Render2D.withAlpha(Theme.ROW_ENABLED, this.toggleAnim));
         }

         if (this.hover > 0.01F) {
            Render2D.rect(x, y, w, 16.5F, Render2D.withAlpha(Theme.ROW_HOVER, this.hover));
         }

         if (this.toggleAnim > 0.01F) {
            float bh = 13.5F * this.toggleAnim;
            Render2D.roundRect(x + 1.5F, y + (16.5F - bh) / 2.0F, 1.8F, bh, 0.9F, Theme.ACCENT);
         }

         int textColor = Anim.lerpColor(Theme.TEXT, Theme.ACCENT, this.toggleAnim);
         float icon = 7.6F;
         ModuleIcons.draw(
            this.module,
            x + 7.0F + this.hover * 1.2F,
            y + (16.5F - icon) / 2.0F,
            icon,
            Anim.lerpColor(Theme.TEXT_DIM, Theme.ACCENT, Math.max(this.toggleAnim, this.hover * 0.6F))
         );
         String name = this.module.getName().toUpperCase();
         float nameX = x + 18.0F;
         float room = w - 18.0F - 24.0F;
         float size = 7.2F;
         float spacing = 0.45F;
         float nw = Render2D.textWidth(name, size, 1, spacing);
         if (nw > room) {
            spacing = 0.1F;
            size = 7.2F * Math.min(1.0F, room / Render2D.textWidth(name, 7.2F, 1, spacing));
         }

         Render2D.text(name, nameX + this.hover * 1.2F, y + (16.5F - size) / 2.0F, size, textColor, 1, spacing);
         if (this.module.isToggleable()) {
            float tw = 13.0F;
            float th = 7.0F;
            float tx = x + w - tw - 7.0F;
            float ty = y + (16.5F - th) / 2.0F;
            Render2D.roundRect(tx, ty, tw, th, th / 2.0F, Anim.lerpColor(Theme.TOGGLE_OFF, Theme.ACCENT, this.toggleAnim));
            Render2D.circle(
               tx + th / 2.0F + (tw - th) * this.toggleAnim, ty + th / 2.0F, th / 2.0F - 1.3F, Anim.lerpColor(-4869720, Theme.ACCENT_DARK, this.toggleAnim)
            );
         } else {
            float cx = x + w - 11.0F;
            float cy = y + 8.25F;
            Render2D.line(cx - 1.5F, cy - 2.5F, cx + 1.5F, cy, 1.1F, Theme.TEXT_DIM);
            Render2D.line(cx + 1.5F, cy, cx - 1.5F, cy + 2.5F, 1.1F, Theme.TEXT_DIM);
         }

         this.drawSettings(x, y, w, mx, my, dt);
      }
   }

   private void drawSettings(float x, float y, float w, float mx, float my, float dt) {
      float open = Anim.easeOutCubic(this.expandAnim);
      this.hitboxes.clear();
      this.hitSettings.clear();
      if (!(open <= 0.001F)) {
         float full = this.settingsHeight();
         float shown = full * open;
         float sy = y + this.rowH() + (this.modern ? 2 : 0);
         Render2D.save();
         Render2D.scissor(x, sy, w, shown);
         if (this.modern) {
            Render2D.roundRect(x, sy, w, shown, 8.0F, Render2D.withAlpha(-16777216, 0.28F));
            Render2D.roundRectOutline(x + 0.3F, sy + 0.3F, w - 0.6F, shown - 0.6F, 8.0F, 0.5F, Render2D.withAlpha(-1, 0.06F));
            Render2D.verticalGradient(
               x + 5.0F,
               sy + 6.0F,
               1.6F,
               Math.max(0.0F, shown - 12.0F),
               0.8F,
               Render2D.withAlpha(Theme.ACCENT, 0.8F),
               Render2D.withAlpha(Theme.HUD_ACCENT_2, 0.15F)
            );
         } else {
            Render2D.rect(x, sy, w, shown, Theme.SETTINGS_BG);
            Render2D.rect(x + 4.0F, sy + 2.0F, 1.0F, Math.max(0.0F, shown - 4.0F), Render2D.withAlpha(Theme.ACCENT, 0.35F));
         }

         Render2D.globalAlpha(Math.min(1.0F, open * 1.4F));
         float cy = sy + 3.0F + (this.modern ? 2 : 0);
         float left = x + (this.modern ? 13 : 9);
         float right = x + w - (this.modern ? 9 : 7);
         this.sliderLeft = left;
         this.sliderWidth = right - left;

         for (Setting<?> s : this.visibleSettings()) {
            float h = this.heightOf(s);
            this.hitboxes.add(new float[]{cy, h});
            this.hitSettings.add(s);
            if (this.modern) {
               this.drawSettingModern(s, left, cy, right - left, h, mx, my, dt);
            } else {
               this.drawSetting(s, left, cy, right - left, h, mx, my);
            }

            cy += h;
         }

         Render2D.restore();
      }
   }

   private static String title(Setting<?> s) {
      String n = s.getName();
      return n.isEmpty() ? n : Character.toUpperCase(n.charAt(0)) + n.substring(1);
   }

   private void drawSettingModern(Setting<?> s, float x, float y, float w, float h, float mx, float my, float dt) {
      float ts = 6.4F;
      int label = Render2D.withAlpha(Theme.TEXT, 0.9F);
      boolean rowHov = mx >= x - 4.0F && mx <= x + w + 4.0F && my >= y && my <= y + h;
      if (rowHov && !(s instanceof InfoSetting)) {
         Render2D.roundRect(x - 5.0F, y + 0.5F, w + 10.0F, h - 1.0F, 5.0F, Render2D.withAlpha(-1, 0.035F));
      }

      if (s instanceof BooleanSetting b) {
         Render2D.text(title(s), x, y + (h - ts) / 2.0F, ts, label, 0);
         float sw = 16.0F;
         float sh = 8.5F;
         float sx = x + w - sw;
         float sy = y + (h - sh) / 2.0F;
         float on = this.segAnim.merge(s, 0.0F, (a, z) -> (Float)a);
         on = Anim.approach(on, b.isOn() ? 1.0F : 0.0F, 16.0F, dt);
         this.segAnim.put(s, on);
         Render2D.roundRect(sx, sy, sw, sh, sh / 2.0F, Render2D.withAlpha(-1, 0.1F));
         if (on > 0.01F) {
            Render2D.horizontalGradient(sx, sy, sw, sh, sh / 2.0F, Render2D.withAlpha(Theme.HUD_ACCENT_2, on), Render2D.withAlpha(Theme.ACCENT, on));
         }

         Render2D.circle(sx + sh / 2.0F + (sw - sh) * Anim.easeOutCubic(on), sy + sh / 2.0F, sh / 2.0F - 1.5F, -1);
      } else if (s instanceof NumberSetting n) {
         Render2D.text(title(s), x, y + 2.5F, ts, label, 0);
         String v = n.format();
         float vw = Render2D.textWidth(v, 6.0F, 1, 0.2F) + 9.0F;
         Render2D.roundRect(x + w - vw, y + 1.0F, vw, 9.5F, 4.75F, Render2D.withAlpha(Theme.ACCENT, 0.16F));
         Render2D.text(v, x + w - vw + 4.5F, y + 2.6F, 6.0F, Theme.ACCENT, 1, 0.2F);
         if (this.draggingSlider == n) {
            n.setFraction((mx - x) / w);
         }

         float ty = y + h - 7.0F;
         float f = (float)n.getFraction();
         boolean drag = this.draggingSlider == n;
         Render2D.roundRect(x, ty, w, 3.0F, 1.5F, Render2D.withAlpha(-1, 0.09F));
         Render2D.horizontalGradient(x, ty, Math.max(3.0F, w * f), 3.0F, 1.5F, Theme.HUD_ACCENT_2, Theme.ACCENT);
         float kx = x + w * f;
         float kr = drag ? 4.4F : (rowHov ? 3.9F : 3.4F);
         Render2D.glow(kx, ty + 1.5F, kr * 3.0F, Render2D.withAlpha(Theme.ACCENT, drag ? 0.45F : 0.22F));
         Render2D.circle(kx, ty + 1.5F, kr, -1);
         Render2D.circle(kx, ty + 1.5F, kr * 0.45F, Theme.ACCENT);
      } else if (s instanceof ModeSetting m) {
         List<String> modes = m.getModes();
         if (segmented(m)) {
            Render2D.text(title(s), x, y + 1.5F, ts, label, 0);
            float sy = y + 11.0F;
            float sh = 13.0F;
            float segW = w / modes.size();
            Render2D.roundRect(x, sy, w, sh, 6.5F, Render2D.withAlpha(-1, 0.06F));
            float target = m.index();
            float pos = this.segAnim.getOrDefault(s, target);
            pos += (target - pos) * (1.0F - (float)Math.exp(-dt * 16.0F));
            this.segAnim.put(s, pos);
            Render2D.shadow(
               x + pos * segW + 1.0F, sy + 1.0F, segW - 2.0F, sh - 2.0F, 5.5F, 6.0F, Render2D.withAlpha(Theme.ACCENT & 16777215 | 0xFF000000, 0.4F)
            );
            Render2D.horizontalGradient(x + pos * segW + 1.0F, sy + 1.0F, segW - 2.0F, sh - 2.0F, 5.5F, Theme.ACCENT, Theme.HUD_ACCENT_2);

            for (int i = 0; i < modes.size(); i++) {
               String o = fit(modes.get(i), 5.6F, 1, 0.1F, segW - 4.0F);
               float sel = Math.max(0.0F, 1.0F - Math.abs(pos - i));
               float tw = Render2D.textWidth(o, 5.6F, 1, 0.1F);
               Render2D.text(
                  o,
                  x + i * segW + (segW - tw) / 2.0F,
                  sy + (sh - 5.6F) / 2.0F,
                  5.6F,
                  Anim.lerpColor(Render2D.withAlpha(Theme.TEXT, 0.75F), Theme.ACCENT_DARK, sel),
                  1,
                  0.1F
               );
            }
         } else {
            Render2D.text(title(s), x, y + (h - ts) / 2.0F, ts, label, 0);
            String v = m.get();
            float vw = Render2D.textWidth(v, 6.0F, 1, 0.15F) + 22.0F;
            float px = x + w - vw;
            float py = y + (h - 11.0F) / 2.0F;
            Render2D.roundRect(px, py, vw, 11.0F, 5.5F, Render2D.withAlpha(Theme.ACCENT, 0.16F));
            Render2D.text(v, px + 11.0F, py + 2.4F, 6.0F, Theme.ACCENT, 1, 0.15F);
            Render2D.line(px + 6.0F, py + 3.6F, px + 4.0F, py + 5.5F, 0.9F, Theme.ACCENT);
            Render2D.line(px + 4.0F, py + 5.5F, px + 6.0F, py + 7.4F, 0.9F, Theme.ACCENT);
            Render2D.line(px + vw - 6.0F, py + 3.6F, px + vw - 4.0F, py + 5.5F, 0.9F, Theme.ACCENT);
            Render2D.line(px + vw - 4.0F, py + 5.5F, px + vw - 6.0F, py + 7.4F, 0.9F, Theme.ACCENT);
         }
      } else if (s instanceof InfoSetting info) {
         Render2D.text(title(s), x, y + (h - ts) / 2.0F, ts, Render2D.withAlpha(Theme.TEXT_DIM, 0.9F), 0);
         String v = info.text();
         Render2D.text(v, x + w - Render2D.textWidth(v, 6.0F, 1, 0.2F), y + (h - 6.0F) / 2.0F, 6.0F, Theme.ACCENT, 1, 0.2F);
      } else if (s instanceof StringSetting str) {
         Render2D.text(title(s), x, y + 1.5F, ts, label, 0);
         float fy = y + 11.0F;
         float fh = 13.0F;
         boolean active = this.editing == str;
         Render2D.roundRect(x, fy, w, fh, 5.0F, Render2D.withAlpha(-16777216, 0.35F));
         Render2D.roundRectOutline(x, fy, w, fh, 5.0F, 0.7F, active ? Theme.ACCENT : Render2D.withAlpha(-1, 0.1F));
         Render2D.save();
         Render2D.scissor(x + 2.0F, fy, w - 4.0F, fh);
         String v = str.get();
         float vw = Render2D.textWidth(v, ts, 0);
         float shift = Math.max(0.0F, vw - (w - 10.0F));
         float end = v.isEmpty() && !active
            ? Render2D.text("Click to type", x + 5.0F, fy + (fh - ts) / 2.0F, ts, Theme.TEXT_DIM, 0)
            : Render2D.text(v, x + 5.0F - shift, fy + (fh - ts) / 2.0F, ts, Theme.TEXT, 0);
         if (active && this.caret % 1.0F < 0.55F) {
            Render2D.rect(end + 0.6F, fy + 3.0F, 0.8F, fh - 6.0F, Theme.ACCENT);
         }

         Render2D.restore();
      } else if (s instanceof ColorSetting c) {
         Render2D.text(title(s), x, y + (h - ts) / 2.0F, ts, label, 0);
         String hex = String.format("#%06X", c.rgb());
         float hw = Render2D.textWidth(hex, 5.6F, 0);
         float sw = 18.0F;
         float sh = 9.0F;
         float sx = x + w - sw;
         float sy = y + (h - sh) / 2.0F;
         Render2D.text(hex, sx - hw - 5.0F, y + (h - 5.6F) / 2.0F, 5.6F, Theme.TEXT_DIM, 0);
         Render2D.shadow(sx, sy, sw, sh, 4.5F, 6.0F, Render2D.withAlpha(0xFF000000 | c.rgb(), 0.5F));
         Render2D.roundRect(sx, sy, sw, sh, 4.5F, 0xFF000000 | c.rgb());
         Render2D.roundRectOutline(sx, sy, sw, sh, 4.5F, 0.6F, Render2D.withAlpha(-1, 0.3F));
      } else if (s instanceof BlockListSetting bl) {
         Render2D.text(title(s), x, y + (h - ts) / 2.0F, ts, label, 0);
         String t = bl.get().size() + " blocks";
         float pw = Render2D.textWidth(t, 6.0F, 1, 0.15F) + 18.0F;
         float ph = 11.0F;
         float px = x + w - pw;
         float py = y + (h - ph) / 2.0F;
         boolean hov = mx >= px && mx <= px + pw && my >= py && my <= py + ph;
         Render2D.roundRect(px, py, pw, ph, ph / 2.0F, hov ? Theme.ACCENT : Render2D.withAlpha(Theme.ACCENT, 0.16F));
         Render2D.text(t, px + 6.0F, py + 2.5F, 6.0F, hov ? Theme.ACCENT_DARK : Theme.ACCENT, 1, 0.15F);
         int cc = hov ? Theme.ACCENT_DARK : Theme.ACCENT;
         Render2D.line(px + pw - 8.0F, py + 3.5F, px + pw - 6.0F, py + 5.5F, 0.9F, cc);
         Render2D.line(px + pw - 6.0F, py + 5.5F, px + pw - 8.0F, py + 7.5F, 0.9F, cc);
      } else if (s instanceof KeybindSetting k) {
         Render2D.text(k == this.module.keybind ? "Keybind" : title(s), x, y + (h - ts) / 2.0F, ts, label, 0);
         boolean listening = this.listening && this.keyTarget() == k;
         String key = listening ? "Press a key…" : k.keyName();
         float pw = Math.max(20.0F, Render2D.textWidth(key, 6.0F, 1, 0.2F) + 12.0F);
         float ph = 11.0F;
         float px = x + w - pw;
         float py = y + (h - ph) / 2.0F - 0.5F;
         Render2D.roundRect(px, py + 1.2F, pw, ph, 3.5F, Render2D.withAlpha(-16777216, 0.5F));
         Render2D.roundRect(px, py, pw, ph, 3.5F, listening ? Render2D.withAlpha(Theme.ACCENT, 0.25F) : Render2D.withAlpha(-1, 0.1F));
         Render2D.roundRectOutline(px + 0.3F, py + 0.3F, pw - 0.6F, ph - 0.6F, 3.5F, 0.5F, listening ? Theme.ACCENT : Render2D.withAlpha(-1, 0.15F));
         float kw = Render2D.textWidth(key, 6.0F, 1, 0.2F);
         Render2D.text(key, px + (pw - kw) / 2.0F, py + 2.5F, 6.0F, listening ? Theme.ACCENT : Theme.TEXT, 1, 0.2F);
      }
   }

   private void drawSetting(Setting<?> s, float x, float y, float w, float h, float mx, float my) {
      float ts = 6.6F;
      String label = s.getName().toUpperCase();
      if (s instanceof BooleanSetting b) {
         float box = 7.0F;
         float by = y + (h - box) / 2.0F;
         boolean on = b.isOn();
         Render2D.roundRect(x, by, box, box, 2.0F, on ? Theme.ACCENT : Theme.TOGGLE_OFF);
         if (on) {
            Icons.check(x, by, box, Theme.ACCENT_DARK);
         }

         Render2D.text(label, x + box + 4.0F, y + (h - ts) / 2.0F, ts, Theme.TEXT, 1, 0.45F);
      } else if (s instanceof NumberSetting n) {
         Render2D.text(label, x, y + 2.5F, ts, Theme.TEXT, 1, 0.45F);
         String v = n.format();
         float vw = Render2D.textWidth(v, ts, 1, 0.45F);
         Render2D.text(v, x + w - vw, y + 2.5F, ts, Theme.ACCENT, 1, 0.45F);
         if (this.draggingSlider == n) {
            n.setFraction((mx - x) / w);
         }

         float ty = y + h - 5.5F;
         float f = (float)n.getFraction();
         Render2D.roundRect(x, ty, w, 2.4F, 1.2F, Theme.ACCENT_SOFT);
         Render2D.roundRect(x, ty, Math.max(2.4F, w * f), 2.4F, 1.2F, Theme.ACCENT);
         Render2D.circle(x + w * f, ty + 1.2F, 3.1F, Theme.ACCENT);
         Render2D.circle(x + w * f, ty + 1.2F, 1.4F, Theme.ACCENT_DARK);
      } else if (s instanceof InfoSetting info) {
         float lw = Render2D.text(label + ": ", x, y + (h - ts) / 2.0F, ts, Theme.TEXT, 1, 0.45F) - x;
         Render2D.text(info.text().toUpperCase(), x + lw, y + (h - ts) / 2.0F, ts, Theme.ACCENT, 1, 0.45F);
      } else if (s instanceof ModeSetting m) {
         float lw = Render2D.text(label + ": ", x, y + (h - ts) / 2.0F, ts, Theme.TEXT, 1, 0.45F) - x;
         String v = m.get().toUpperCase();
         Render2D.text(v, x + lw, y + (h - ts) / 2.0F, ts, Theme.ACCENT, 1, 0.45F);
         float ax = x + w - 3.0F;
         float ay = y + h / 2.0F;
         Render2D.line(ax - 2.0F, ay - 2.0F, ax, ay, 1.0F, Theme.TEXT_DIM);
         Render2D.line(ax, ay, ax - 2.0F, ay + 2.0F, 1.0F, Theme.TEXT_DIM);
      } else if (s instanceof StringSetting str) {
         Render2D.text(label, x, y + 1.5F, ts, Theme.TEXT, 1, 0.45F);
         float fy = y + 10.0F;
         float fh = 12.0F;
         boolean active = this.editing == str;
         Render2D.roundRect(x, fy, w, fh, 3.0F, -14869736);
         Render2D.roundRectOutline(x, fy, w, fh, 3.0F, 0.7F, active ? Theme.ACCENT : 587202559);
         Render2D.save();
         Render2D.scissor(x + 2.0F, fy, w - 4.0F, fh);
         String v = str.get();
         float vw = Render2D.textWidth(v, ts, 1, 0.2F);
         float shift = Math.max(0.0F, vw - (w - 10.0F));
         float end = v.isEmpty() && !active
            ? Render2D.text("CLICK TO TYPE", x + 4.0F, fy + (fh - ts) / 2.0F, ts, Theme.TEXT_DIM, 1, 0.3F)
            : Render2D.text(v, x + 4.0F - shift, fy + (fh - ts) / 2.0F, ts, Theme.TEXT, 1, 0.2F);
         if (active && this.caret % 1.0F < 0.55F) {
            Render2D.rect(end + 0.6F, fy + 2.5F, 0.9F, fh - 5.0F, Theme.ACCENT);
         }

         Render2D.restore();
      } else if (s instanceof ColorSetting c) {
         Render2D.text(label, x, y + (h - ts) / 2.0F, ts, Theme.TEXT, 1, 0.45F);
         float sw = 16.0F;
         float sh = 8.0F;
         float sx = x + w - sw;
         float sy = y + (h - sh) / 2.0F;
         Render2D.roundRect(sx, sy, sw, sh, 2.5F, 0xFF000000 | c.rgb());
         Render2D.roundRectOutline(sx, sy, sw, sh, 2.5F, 0.6F, 1157627903);
      } else if (s instanceof BlockListSetting bl) {
         Render2D.text(label, x, y + (h - ts) / 2.0F, ts, Theme.TEXT, 1, 0.45F);
         String t = bl.get().size() + " BLOCKS";
         float pw = Render2D.textWidth(t, ts, 1, 0.45F) + 12.0F;
         float ph = 10.0F;
         float px = x + w - pw;
         float py = y + (h - ph) / 2.0F;
         boolean hov = mx >= px && mx <= px + pw && my >= py && my <= py + ph;
         Render2D.roundRect(px, py, pw, ph, ph / 2.0F, hov ? Theme.ACCENT : Theme.ACCENT_SOFT);
         Render2D.text(t, px + 6.0F, py + (ph - ts) / 2.0F, ts, hov ? Theme.ACCENT_DARK : Theme.ACCENT, 1, 0.45F);
      } else if (s instanceof KeybindSetting k) {
         Render2D.text(k == this.module.keybind ? "KEYBIND" : label, x, y + (h - ts) / 2.0F, ts, Theme.TEXT, 1, 0.45F);
         boolean listening = this.listening && this.keyTarget() == k;
         String key = listening ? "..." : k.keyName();
         float pw = Math.max(26.0F, Render2D.textWidth(key, ts, 1, 0.45F) + 12.0F);
         float ph = 10.0F;
         float px = x + w - pw;
         float py = y + (h - ph) / 2.0F;
         boolean hov = mx >= px && mx <= px + pw && my >= py && my <= py + ph;
         Render2D.roundRect(px, py, pw, ph, ph / 2.0F, listening ? Theme.ACCENT_SOFT : (hov ? -12764620 : Theme.TOGGLE_OFF));
         float kw = Render2D.textWidth(key, ts, 1, 0.45F);
         Render2D.text(key, px + (pw - kw) / 2.0F, py + (ph - ts) / 2.0F, ts, listening ? Theme.ACCENT : Theme.TEXT, 1, 0.45F);
      }
   }

   boolean mouseClicked(float mx, float my, int button) {
      if (this.isRowHovered(mx, my)) {
         if (button == 0) {
            this.module.toggle();
         } else if (button == 1) {
            this.expanded = !this.expanded;
            if (!this.expanded) {
               this.listening = false;
               this.editing = null;
            }
         }

         return true;
      } else if (this.expanded && !(mx < this.x) && !(mx > this.x + this.w)) {
         for (int i = 0; i < this.hitboxes.size(); i++) {
            float[] hb = this.hitboxes.get(i);
            if (!(my < hb[0]) && !(my > hb[0] + hb[1])) {
               Setting<?> s = this.hitSettings.get(i);
               if (s != this.editing) {
                  this.editing = null;
               }

               if (s instanceof ModeSetting m) {
                  if (this.modern && segmented(m) && my >= hb[0] + 11.0F && button == 0) {
                     int n = m.getModes().size();
                     int idx = (int)((mx - this.sliderLeft) / (this.sliderWidth / n));
                     if (idx >= 0 && idx < n) {
                        m.set(m.getModes().get(idx));
                     }
                  } else {
                     m.cycle(button == 1 ? -1 : 1);
                  }

                  return true;
               }

               if (button != 0) {
                  return true;
               }

               if (s instanceof BooleanSetting b) {
                  b.toggle();
               } else if (s instanceof NumberSetting n) {
                  this.draggingSlider = n;
                  n.setFraction((mx - this.sliderLeft) / this.sliderWidth);
               } else if (s instanceof KeybindSetting kb) {
                  this.listening = !this.listening || this.keyTarget() != kb;
                  this.listenTarget = kb;
               } else if (s instanceof StringSetting str) {
                  this.editing = str;
                  this.caret = 0.0F;
               } else if (s instanceof ColorSetting c) {
                  ClickGui.INSTANCE.openPopup(new ColorPickerPopup(this.module.getName() + ": " + c.getName(), c.argb(), c::set));
               } else if (s instanceof BlockListSetting bl) {
                  ClickGui.INSTANCE.openPopup(new BlockPickerPopup(this.module.getName() + ": " + bl.getName(), bl));
               }

               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   void mouseReleased() {
      this.draggingSlider = null;
   }

   boolean keyPressed(int key) {
      if (this.editing != null) {
         String v = this.editing.get();
         if (key == 259 && !v.isEmpty()) {
            this.editing.set(v.substring(0, v.length() - 1));
         } else if (key == 257 || key == 256 || key == 335) {
            this.editing = null;
         }

         this.caret = 0.0F;
         return true;
      } else {
         if (!this.listening) {
            return false;
         }

         if (key == 256) {
            this.listening = false;
         } else if (key != 259 && key != 261) {
            this.keyTarget().set(key);
            this.listening = false;
         } else {
            this.keyTarget().set(-1);
            this.listening = false;
         }

         return true;
      }
   }

   private KeybindSetting keyTarget() {
      return this.listenTarget != null ? this.listenTarget : this.module.keybind;
   }

   boolean charTyped(String chars) {
      if (this.editing == null) {
         return false;
      }

      String v = this.editing.get();

      for (char c : chars.toCharArray()) {
         if (c >= ' ' && c != 127 && v.length() < this.editing.getMaxLength()) {
            v = v + c;
         }
      }

      this.editing.set(v);
      this.caret = 0.0F;
      return true;
   }

   void stopEditing() {
      this.editing = null;
      this.listening = false;
   }
}
