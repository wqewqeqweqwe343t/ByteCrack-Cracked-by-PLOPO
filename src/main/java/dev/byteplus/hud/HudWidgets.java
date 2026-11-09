package dev.byteplus.hud;

import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_746;
import net.minecraft.class_7923;

final class HudWidgets {
   private static final float TEXT = 7.0F;
   private static final class_1304[] SLOTS = new class_1304[]{
      class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166, class_1304.field_6173
   };
   private static final String[] ROMAN = new String[]{"", "", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};
   private static final float[] keyAnim = new float[7];
   private static final ArrayDeque<Long> leftClicks = new ArrayDeque<>();
   private static final ArrayDeque<Long> rightClicks = new ArrayDeque<>();
   private static boolean lastLeft;
   private static boolean lastRight;

   private HudWidgets() {
   }

   private static void card(float x, float y, float w, float h) {
      Render2D.shadow(x, y, w, h, 5.0F, 8.0F, 1342177280);
      Render2D.roundRect(x, y, w, h, 5.0F, Theme.HUD_BG);
      Render2D.roundRectOutline(x + 0.25F, y + 0.25F, w - 0.5F, h - 0.5F, 5.0F, 0.5F, Theme.HUD_OUTLINE);
   }

   private static int itemIcon(class_1799 s) {
      String id = class_7923.field_41178.method_10221(s.method_7909()).method_12832();
      return Render2D.mcTexture("textures/item/" + id + ".png");
   }

   static float[] armor(class_746 p, float x, float y, boolean editing) {
      List<class_1799> items = new ArrayList<>();

      for (class_1304 slot : SLOTS) {
         class_1799 s = p.method_6118(slot);
         if (!s.method_7960()) {
            items.add(s);
         }
      }

      if (items.isEmpty() && !editing) {
         return null;
      }

      float rowH = 15.0F;
      float w = 74.0F;
      float h = Math.max(1, items.size()) * rowH + 6.0F;
      card(x, y, w, h);
      float cy = y + 3.0F;
      if (items.isEmpty()) {
         Render2D.text("NO ARMOR", x + 8.0F, cy + 4.0F, 7.0F, Theme.TEXT_DIM, 1, 0.4F);
      }

      for (class_1799 s : items) {
         int img = itemIcon(s);
         if (img > 0) {
            Render2D.image(img, x + 5.0F, cy + 1.5F, 12.0F, 12.0F, 1.0F);
         }

         if (s.method_7963()) {
            float frac = 1.0F - (float)s.method_7919() / Math.max(1, s.method_7936());
            int col = frac > 0.6F ? -11149190 : (frac > 0.3F ? -14040 : -45747);
            String pct = Math.round(frac * 100.0F) + "%";
            Render2D.text(pct, x + 21.0F, cy + 2.2F, 7.0F, col, 1, 0.3F);
            Render2D.roundRect(x + 21.0F, cy + 10.5F, w - 27.0F, 2.0F, 1.0F, 872415231);
            Render2D.roundRect(x + 21.0F, cy + 10.5F, (w - 27.0F) * frac, 2.0F, 1.0F, col);
         } else {
            String count = s.method_7947() > 1 ? "x" + s.method_7947() : s.method_7964().getString();
            Render2D.save();
            Render2D.scissor(x + 21.0F, cy, w - 25.0F, rowH);
            Render2D.text(count, x + 21.0F, cy + 4.0F, 7.0F, Theme.HUD_TEXT, 1, 0.3F);
            Render2D.restore();
         }

         cy += rowH;
      }

      return new float[]{w, h};
   }

   static float[] potions(class_746 p, float x, float y, boolean editing) {
      ArrayList<class_1293> effects = new ArrayList<>(p.method_6026());
      if (effects.isEmpty() && !editing) {
         return null;
      }

      float rowH = 15.0F;
      float w = 112.0F;
      float h = Math.max(1, effects.size()) * rowH + 6.0F;
      card(x, y, w, h);
      float cy = y + 3.0F;
      if (effects.isEmpty()) {
         Render2D.text("NO EFFECTS", x + 8.0F, cy + 4.0F, 7.0F, Theme.TEXT_DIM, 1, 0.4F);
      }

      for (class_1293 e : effects) {
         String id = class_7923.field_41174.method_10221((class_1291)e.method_5579().comp_349()).method_12832();
         int img = Render2D.mcTexture("textures/mob_effect/" + id + ".png");
         if (img > 0) {
            Render2D.image(img, x + 5.0F, cy + 1.5F, 12.0F, 12.0F, 1.0F);
         }

         int amp = e.method_5578() + 1;
         String name = ((class_1291)e.method_5579().comp_349()).method_5560().getString() + (amp < ROMAN.length ? ROMAN[amp] : " " + amp);
         String time;
         if (e.method_48559()) {
            time = "**:**";
         } else {
            int secs = e.method_5584() / 20;
            time = String.format("%d:%02d", secs / 60, secs % 60);
         }

         int col = 0xFF000000 | ((class_1291)e.method_5579().comp_349()).method_5556();
         float tw = Render2D.textWidth(time, 7.0F, 1, 0.3F);
         Render2D.save();
         Render2D.scissor(x + 21.0F, cy, w - 30.0F - tw, rowH);
         Render2D.text(name, x + 21.0F, cy + 4.0F, 7.0F, Theme.mix(col, -1, 0.45F), 1, 0.2F);
         Render2D.restore();
         boolean ending = !e.method_48559() && e.method_5584() < 200 && System.currentTimeMillis() / 400L % 2L == 0L;
         Render2D.text(time, x + w - 6.0F - tw, cy + 4.0F, 7.0F, ending ? -45747 : Theme.TEXT_DIM, 1, 0.3F);
         cy += rowH;
      }

      return new float[]{w, h};
   }

   static float[] keystrokes(float x, float y, float dt) {
      class_310 mc = class_310.method_1551();
      class_315 o = mc.field_1690;
      boolean left = o.field_1886.method_1434();
      boolean right = o.field_1904.method_1434();
      long now = System.currentTimeMillis();
      if (left && !lastLeft) {
         leftClicks.add(now);
      }

      if (right && !lastRight) {
         rightClicks.add(now);
      }

      lastLeft = left;
      lastRight = right;

      while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000L) {
         leftClicks.pollFirst();
      }

      while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000L) {
         rightClicks.pollFirst();
      }

      float k = 20.0F;
      float g = 2.5F;
      float w = k * 3.0F + g * 2.0F;
      boolean[] down = new boolean[]{
         o.field_1894.method_1434(),
         o.field_1913.method_1434(),
         o.field_1881.method_1434(),
         o.field_1849.method_1434(),
         left,
         right,
         o.field_1903.method_1434()
      };

      for (int i = 0; i < down.length; i++) {
         keyAnim[i] = Anim.approach(keyAnim[i], down[i] ? 1.0F : 0.0F, 20.0F, dt);
      }

      key(x + k + g, y, k, k, keyName(o.field_1894.method_16007().getString(), "W"), null, keyAnim[0]);
      float y2 = y + k + g;
      key(x, y2, k, k, keyName(o.field_1913.method_16007().getString(), "A"), null, keyAnim[1]);
      key(x + k + g, y2, k, k, keyName(o.field_1881.method_16007().getString(), "S"), null, keyAnim[2]);
      key(x + (k + g) * 2.0F, y2, k, k, keyName(o.field_1849.method_16007().getString(), "D"), null, keyAnim[3]);
      float y3 = y2 + k + g;
      float half = (w - g) / 2.0F;
      key(x, y3, half, k, "LMB", leftClicks.size() + " CPS", keyAnim[4]);
      key(x + half + g, y3, half, k, "RMB", rightClicks.size() + " CPS", keyAnim[5]);
      float y4 = y3 + k + g;
      key(x, y4, w, 10.0F, null, null, keyAnim[6]);
      Render2D.roundRect(x + w / 2.0F - 9.0F, y4 + 4.5F, 18.0F, 1.2F, 0.6F, Anim.lerpColor(Theme.HUD_TEXT, Theme.ACCENT_DARK, keyAnim[6]));
      return new float[]{w, y4 + 10.0F - y};
   }

   private static String keyName(String s, String fallback) {
      if (s != null && !s.isEmpty()) {
         return s.length() > 3 ? s.substring(0, 3).toUpperCase() : s.toUpperCase();
      } else {
         return fallback;
      }
   }

   private static void key(float x, float y, float w, float h, String label, String sub, float press) {
      Render2D.shadow(x, y, w, h, 4.0F, 6.0F, 1073741824);
      Render2D.roundRect(x, y, w, h, 4.0F, Anim.lerpColor(Theme.HUD_BG, Theme.HUD_ACCENT, press));
      Render2D.roundRectOutline(x + 0.25F, y + 0.25F, w - 0.5F, h - 0.5F, 4.0F, 0.5F, Theme.HUD_OUTLINE);
      int col = Anim.lerpColor(Theme.HUD_TEXT, Theme.ACCENT_DARK, press);
      if (label != null) {
         if (sub == null) {
            Render2D.textCentered(label, x + w / 2.0F, y + h / 2.0F, 7.0F, col, 1);
         } else {
            Render2D.textCentered(label, x + w / 2.0F, y + h / 2.0F - 3.2F, 6.4F, col, 1);
            Render2D.textCentered(sub, x + w / 2.0F, y + h / 2.0F + 4.2F, 5.0F, Render2D.withAlpha(col, 0.75F), 1);
         }
      }
   }
}
