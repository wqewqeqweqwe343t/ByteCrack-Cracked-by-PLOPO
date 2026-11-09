package dev.byteplus.hud;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.render.HudModule;
import dev.byteplus.render.Icons;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_640;
import net.minecraft.class_746;

public final class Hud {
   private static final float TEXT = 7.4F;
   private static final float SPACING = 0.55F;
   private static final float BOX_H = 14.0F;
   private static final float ICON = 7.5F;
   private static final Map<Module, float[]> listAnim = new HashMap<>();
   private static final float[] infoRect = new float[4];
   private static final float[] radarRect = new float[4];
   private static final float[] armorRect = new float[4];
   private static final float[] potionRect = new float[4];
   private static final float[] keysRect = new float[4];
   private static float lastScale = 1.0F;
   private static float lastScreenW;
   private static float lastScreenH;
   private static int dragging = 0;
   private static float dragOffX;
   private static float dragOffY;

   private static float[] rect(int id) {
      return switch (id) {
         case 1 -> infoRect;
         case 2 -> radarRect;
         case 3 -> armorRect;
         case 4 -> potionRect;
         default -> keysRect;
      };
   }

   private static NumberSetting[] position(HudModule hud, int id) {
      return switch (id) {
         case 1 -> new NumberSetting[]{hud.infoX, hud.infoY};
         case 2 -> new NumberSetting[]{hud.radarX, hud.radarY};
         case 3 -> new NumberSetting[]{hud.armorX, hud.armorY};
         case 4 -> new NumberSetting[]{hud.potionX, hud.potionY};
         default -> new NumberSetting[]{hud.keysX, hud.keysY};
      };
   }

   private static void widget(int id, HudModule hud, float w, float h, float s, boolean editing, Hud.Widget draw) {
      NumberSetting[] pos = position(hud, id);
      float x = clamp(pos[0].getFloat(), 0.0F, w - 20.0F);
      float y = clamp(pos[1].getFloat(), 0.0F, h - 20.0F);
      float[] size = draw.draw(x, y);
      if (size == null) {
         setRect(rect(id), -1000.0F, -1000.0F, 0.0F, 0.0F, s);
      } else {
         setRect(rect(id), x, y, size[0], size[1], s);
         if (editing) {
            editOutline(x, y, size[0], size[1], dragging == id);
         }
      }
   }

   private Hud() {
   }

   public static void render(float sw, float sh, float dt, boolean editing) {
      class_310 mc = class_310.method_1551();
      class_746 p = mc.field_1724;
      if (p != null) {
         HudModule hud = BytePlus.modules().hud;
         float s = hud.scale.getFloat();
         lastScale = s;
         lastScreenW = sw;
         lastScreenH = sh;
         Render2D.save();
         Render2D.scale(s);
         float w = sw / s;
         float h = sh / s;
         float ix = clamp(hud.infoX.getFloat(), 0.0F, w - 20.0F);
         float iy = clamp(hud.infoY.getFloat(), 0.0F, h - 20.0F);
         float x = ix;
         float y = iy;
         float maxW = 0.0F;
         if (hud.watermark.isOn()) {
            float bw = logoBox(x, y);
            x += bw + 3.0F;
            maxW = Math.max(maxW, x - ix);
         }

         List<String[]> pills = new ArrayList<>();
         if (hud.fps.isOn()) {
            pills.add(new String[]{"fps", mc.method_47599() + " FPS"});
         }

         if (hud.ping.isOn()) {
            pills.add(new String[]{"ping", Math.max(0, ping(mc, p)) + " MS"});
         }

         for (String[] pill : pills) {
            float bw = box(x, y, pill[1], pill[0], ping(mc, p));
            x += bw + 3.0F;
         }

         maxW = Math.max(maxW, x - ix - 3.0F);
         y += 17.0F;
         if (hud.coordinates.isOn()) {
            String coords = (int)Math.floor(p.method_23317()) + "  " + (int)Math.floor(p.method_23318()) + "  " + (int)Math.floor(p.method_23321());
            float bw = box(ix, y, coords, "coords", 0);
            maxW = Math.max(maxW, bw);
            y += 17.0F;
         }

         if (hud.playerName.isOn()) {
            float bw = box(ix, y, p.method_5477().getString(), "name", 0);
            maxW = Math.max(maxW, bw);
            y += 17.0F;
         }

         setRect(infoRect, ix, iy, Math.max(20.0F, maxW), Math.max(14.0F, y - iy - 3.0F), s);
         if (editing) {
            editOutline(ix, iy, Math.max(20.0F, maxW), Math.max(14.0F, y - iy - 3.0F), dragging == 1);
         }

         if (hud.radar.isOn()) {
            float r = hud.radarSize.getFloat();
            float rx = clamp(hud.radarX.getFloat(), 0.0F, w - r * 2.0F);
            float ry = clamp(hud.radarY.getFloat(), 0.0F, h - r * 2.0F);
            drawRadar(mc, p, rx + r, ry + r, r, hud);
            setRect(radarRect, rx, ry, r * 2.0F, r * 2.0F, s);
            if (editing) {
               editOutline(rx, ry, r * 2.0F, r * 2.0F, dragging == 2);
            }
         } else {
            setRect(radarRect, -1000.0F, -1000.0F, 0.0F, 0.0F, s);
         }

         if (hud.armor.isOn()) {
            widget(3, hud, w, h, s, editing, (x0, y0) -> HudWidgets.armor(p, x0, y0, editing));
         } else {
            setRect(armorRect, -1000.0F, -1000.0F, 0.0F, 0.0F, s);
         }

         if (hud.potions.isOn()) {
            widget(4, hud, w, h, s, editing, (x0, y0) -> HudWidgets.potions(p, x0, y0, editing));
         } else {
            setRect(potionRect, -1000.0F, -1000.0F, 0.0F, 0.0F, s);
         }

         if (hud.keystrokes.isOn()) {
            widget(5, hud, w, h, s, editing, (x0, y0) -> HudWidgets.keystrokes(x0, y0, dt));
         } else {
            setRect(keysRect, -1000.0F, -1000.0F, 0.0F, 0.0F, s);
         }

         if (hud.arrayList.isOn()) {
            drawArrayList(w, dt);
         }

         if (hud.notifications.isOn()) {
            Notifications.render(w, h, dt);
         }

         Render2D.restore();
      } else {
         // The editor can be opened from the title screen.  Do not leave the
         // last in-world widget bounds active there.
         for (int id = 1; id <= 5; id++) {
            setRect(rect(id), -1000.0F, -1000.0F, 0.0F, 0.0F, 1.0F);
         }
         dragging = 0;
      }
   }

   private static float clamp(float v, float min, float max) {
      return Math.max(min, Math.min(max, v));
   }

   private static void setRect(float[] r, float x, float y, float w, float h, float s) {
      r[0] = x * s;
      r[1] = y * s;
      r[2] = w * s;
      r[3] = h * s;
   }

   private static void editOutline(float x, float y, float w, float h, boolean active) {
      Render2D.roundRectOutline(x - 2.0F, y - 2.0F, w + 4.0F, h + 4.0F, 4.0F, 0.8F, active ? Theme.HUD_ACCENT : 1728039208);
   }

   public static boolean mouseClicked(double mx, double my) {
      for (int id = 5; id >= 1; id--) {
         float[] r = rect(id);
         if (inside(r, mx, my)) {
            dragging = id;
            dragOffX = (float)mx - r[0];
            dragOffY = (float)my - r[1];
            return true;
         }
      }

      return false;
   }

   public static void mouseMoved(double mx, double my) {
      if (dragging != 0) {
         NumberSetting[] pos = position(BytePlus.modules().hud, dragging);
         float[] r = rect(dragging);
         float scale = Math.max(0.001F, lastScale);
         float x = clamp((float)mx - dragOffX, 0.0F, Math.max(0.0F, lastScreenW - r[2]));
         float y = clamp((float)my - dragOffY, 0.0F, Math.max(0.0F, lastScreenH - r[3]));
         pos[0].set((double)(x / scale));
         pos[1].set((double)(y / scale));
      }
   }

   public static void mouseReleased() {
      dragging = 0;
   }

   private static boolean inside(float[] r, double mx, double my) {
      return mx >= r[0] - 2.0F && mx <= r[0] + r[2] + 2.0F && my >= r[1] - 2.0F && my <= r[1] + r[3] + 2.0F;
   }

   private static float logoBox(float x, float y) {
      String text = "Cracked By PLOPO".toUpperCase();
      float tw = Render2D.textWidth(text, 7.4F, 1, 0.55F);
      float logo = 13.0F;
      float w = 4.0F + logo + 3.0F + tw + 6.0F;
      Render2D.shadow(x, y, w, 14.0F, 4.0F, 6.0F, 1342177280);
      Render2D.roundRect(x, y, w, 14.0F, 4.0F, Theme.HUD_BG);
      Render2D.roundRectOutline(x + 0.25F, y + 0.25F, w - 0.5F, 13.5F, 4.0F, 0.5F, Theme.HUD_OUTLINE);
      Render2D.image(Render2D.modImage("/assets/byteplus/textures/logo.png"), x + 3.0F, y + 0.5F, logo, logo, 1.0F);
      Render2D.text(text, x + 4.0F + logo + 3.0F, y + 3.3F + 0.2F, 7.4F, Theme.HUD_ACCENT, 1, 0.55F);
      return w;
   }

   private static float box(float x, float y, String text, String icon, int ping) {
      float tw = Render2D.textWidth(text, 7.4F, 1, 0.55F);
      float w = 16.5F + tw + 5.0F;
      Render2D.shadow(x, y, w, 14.0F, 4.0F, 6.0F, 1342177280);
      Render2D.roundRect(x, y, w, 14.0F, 4.0F, Theme.HUD_BG);
      Render2D.roundRectOutline(x + 0.25F, y + 0.25F, w - 0.5F, 13.5F, 4.0F, 0.5F, Theme.HUD_OUTLINE);
      float icx = x + 5.0F;
      float icy = y + 3.25F;
      switch (icon) {
         case "fps":
            Icons.speed(icx, icy, 7.5F, Theme.HUD_ACCENT);
            break;
         case "ping":
            Icons.signal(icx, icy, 7.5F, Theme.HUD_ACCENT, ping < 0 ? 0 : (ping < 80 ? 4 : (ping < 150 ? 3 : (ping < 250 ? 2 : 1))));
            break;
         case "coords":
            Icons.compass(icx, icy, 7.5F, Theme.HUD_ACCENT);
            break;
         default:
            Icons.person(icx, icy, 7.5F, Theme.HUD_ACCENT);
      }

      Render2D.text(text, x + 5.0F + 7.5F + 4.0F, y + 3.3F + 0.2F, 7.4F, Theme.HUD_TEXT, 1, 0.55F);
      return w;
   }

   private static int ping(class_310 mc, class_746 p) {
      if (mc.method_1562() == null) {
         return -1;
      }

      class_640 info = mc.method_1562().method_2871(p.method_5667());
      return info == null ? -1 : info.method_2959();
   }

   private static void drawRadar(class_310 mc, class_746 p, float cx, float cy, float r, HudModule hud) {
      float yaw = p.method_36454();
      Render2D.shadow(cx - r, cy - r, r * 2.0F, r * 2.0F, r, 10.0F, 1610612736);
      Render2D.circle(cx, cy, r, -501606121);
      Render2D.circleOutline(cx, cy, r - 0.6F, 1.1F, 1442826536);
      Render2D.circleOutline(cx, cy, r * 0.6F, 0.5F, 352321535);
      Render2D.line(cx - r * 0.85F, cy, cx + r * 0.85F, cy, 0.5F, 285212671);
      Render2D.line(cx, cy - r * 0.85F, cx, cy + r * 0.85F, 0.5F, 285212671);

      for (int i = 0; i < 12; i++) {
         double a = Math.toRadians(i * 30 - yaw);
         float sn = (float)Math.sin(a);
         float cs = (float)(-Math.cos(a));
         float inner = i % 3 == 0 ? r * 0.86F : r * 0.9F;
         Render2D.line(cx + sn * inner, cy + cs * inner, cx + sn * (r - 2.5F), cy + cs * (r - 2.5F), 0.7F, 822083583);
      }

      String[] names = new String[]{"S", "W", "N", "E"};

      for (int i = 0; i < 4; i++) {
         double a = Math.toRadians(i * 90 - yaw);
         float lx = cx + (float)Math.sin(a) * r * 0.7F;
         float ly = cy - (float)Math.cos(a) * r * 0.7F;
         Render2D.textCentered(names[i], lx, ly, Math.max(6.0F, r * 0.2F), names[i].equals("N") ? Theme.HUD_ACCENT : -6646130, 1);
      }

      if (hud.radarPlayers.isOn() && mc.field_1687 != null) {
         float range = hud.radarRange.getFloat();

         for (class_1657 other : mc.field_1687.method_18456()) {
            if (other != p) {
               double dx = other.method_23317() - p.method_23317();
               double dz = other.method_23321() - p.method_23321();
               double dist = Math.sqrt(dx * dx + dz * dz);
               if (!(dist > range)) {
                  double a = Math.toRadians(Math.toDegrees(Math.atan2(-dx, dz)) - yaw);
                  float d = (float)(dist / range) * r * 0.85F;
                  float px = cx + (float)Math.sin(a) * d;
                  float py = cy - (float)Math.cos(a) * d;
                  Render2D.circle(px, py, 2.6F, 1442798157);
                  Render2D.circle(px, py, 1.6F, -42419);
               }
            }
         }
      }

      Render2D.triangle(cx, cy - r + 1.5F, cx - 3.0F, cy - r + 6.0F, cx + 3.0F, cy - r + 6.0F, Theme.HUD_ACCENT);
      Render2D.circle(cx, cy, 3.4F, 1157613864);
      Render2D.circle(cx, cy, 2.0F, Theme.HUD_ACCENT);
   }

   private static void drawArrayList(float sw, float dt) {
      List<Module> list = new ArrayList<>();

      for (Module m : BytePlus.modules().all()) {
         if (m.showInArrayList()) {
            float[] a = listAnim.computeIfAbsent(m, k -> new float[]{0.0F});
            a[0] = Anim.approach(a[0], m.isEnabled() ? 1.0F : 0.0F, 12.0F, dt);
            if (a[0] > 0.005F) {
               list.add(m);
            }
         }
      }

      list.sort((a, b) -> Float.compare(width(b), width(a)));
      String style = BytePlus.modules().hud.listStyle.get();
      float rowH = 11.5F;
      float y = 4.0F;
      int i = 0;
      double time = System.nanoTime() / 1.0E9;

      for (Module m : list) {
         float v = Anim.easeOutCubic(listAnim.get(m)[0]);
         float w = width(m) + 11.0F;
         float x = sw - 4.0F - w + (1.0F - v) * (w + 8.0F);
         Render2D.save();
         Render2D.globalAlpha(v);
         float t = list.size() <= 1 ? 0.0F : (float)i / (list.size() - 1);

         int accent = switch (style) {
            case "Rainbow" -> 0xFF000000 | Color.HSBtoRGB((float)((time * 0.15 + i * 0.06) % 1.0), 0.6F, 1.0F);
            default -> Anim.lerpColor(Theme.HUD_ACCENT, Theme.HUD_ACCENT_2, t);
         };

         int text = switch (style) {
            case "Classic" -> Theme.HUD_TEXT;
            default -> Theme.mix(accent, -1, 0.25F);
         };
         if (style.equals("Glow")) {
            Render2D.shadow(x, y, w, rowH * v, 3.0F, 8.0F, accent & 16777215 | 1342177280);
         }

         Render2D.roundRect(x, y, w, rowH * v, 2.5F, 0.0F, 0.0F, 2.5F, Theme.HUD_BG);
         Render2D.roundRect(x + w, y, 1.8F, rowH * v, 0.0F, 1.0F, 1.0F, 0.0F, accent);
         Render2D.text(m.getName().toUpperCase(), x + 5.5F, y + (rowH - 7.4F) / 2.0F * v, 7.4F, text, 1, 0.55F);
         Render2D.restore();
         y += rowH * v;
         i++;
      }
   }

   private static float width(Module m) {
      return Render2D.textWidth(m.getName().toUpperCase(), 7.4F, 1, 0.55F);
   }

   private interface Widget {
      float[] draw(float var1, float var2);
   }
}
