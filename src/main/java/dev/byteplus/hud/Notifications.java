package dev.byteplus.hud;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.render.Icons;
import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Notifications {
   private static final List<Notifications.Toast> toasts = new ArrayList<>();

   private Notifications() {
   }

   public static void push(String title, String text) {
      add(new Notifications.Toast(title, text, null, false, 2.6F, false));
   }

   public static void banner(String title, String text, String iconTexture) {
      add(new Notifications.Toast(title, text, iconTexture, true, 4.5F, false));
   }

   public static void chunkFlag(int x, int z) {
      add(new Notifications.Toast("Chunk Flagged", "X: " + x + "   Z: " + z, null, false, 6.0F, true));
   }

   public static void toggled(Module m) {
      if (BytePlus.modules() != null && BytePlus.modules().hud.toggleNotifications.isOn()) {
         push(m.getName(), m.isEnabled() ? "Enabled" : "Disabled");
      }
   }

   private static void add(Notifications.Toast t) {
      synchronized (toasts) {
         if (!t.banner && !t.flag && toasts.stream().filter(xx -> !xx.banner && !xx.flag).count() >= 5L) {
            for (Notifications.Toast x : toasts) {
               if (!x.banner && !x.flag) {
                  toasts.remove(x);
                  break;
               }
            }
         }

         toasts.add(t);
      }
   }

   public static void render(float sw, float sh, float dt) {
      synchronized (toasts) {
         float stackY = sh - 6.0F;
         float flagY = sh * 0.22F;
         int bannerIndex = 0;
         Iterator<Notifications.Toast> it = toasts.iterator();

         while (it.hasNext()) {
            Notifications.Toast t = it.next();
            t.age += dt;
            boolean leaving = t.age > t.duration;
            t.anim = Anim.approach(t.anim, leaving ? 0.0F : 1.0F, 12.0F, dt);
            if (leaving && t.anim <= 0.01F) {
               it.remove();
            } else {
               float v = Anim.easeOutCubic(t.anim);
               if (t.banner) {
                  drawBanner(t, sw, 6 + bannerIndex * 30, v);
                  bannerIndex++;
               } else if (t.flag) {
                  drawFlag(t, sw, flagY, v);
                  flagY += 38.0F * v;
               } else {
                  float h = 26.0F;
                  stackY -= (h + 4.0F) * v;
                  drawToast(t, sw, stackY, h, v);
               }
            }
         }
      }
   }

   private static void drawToast(Notifications.Toast t, float sw, float y, float h, float v) {
      float tw = Math.max(Render2D.textWidth(t.title.toUpperCase(), 7.0F, 1, 0.5F), Render2D.textWidth(t.text, 7.0F, 0));
      float w = tw + 18.0F;
      float x = sw - 6.0F - w + (1.0F - v) * (w + 10.0F);
      Render2D.save();
      Render2D.globalAlpha(v);
      Render2D.shadow(x, y, w, h, 4.0F, 8.0F, 1426063360);
      Render2D.roundRect(x, y, w, h, 4.0F, Theme.HUD_BG);
      Render2D.roundRect(x, y + 4.0F, 2.0F, h - 8.0F, 1.0F, Theme.HUD_ACCENT);
      Render2D.text(t.title.toUpperCase(), x + 9.0F, y + 4.5F, 7.0F, Theme.HUD_ACCENT, 1, 0.5F);
      Render2D.text(t.text, x + 9.0F, y + 14.0F, 7.0F, t.text.equals("Disabled") ? Theme.TEXT_DIM : Theme.HUD_TEXT, 0);
      float p = Math.max(0.0F, 1.0F - t.age / t.duration);
      Render2D.roundRect(x + 4.0F, y + h - 2.0F, (w - 8.0F) * p, 1.2F, 0.6F, Render2D.withAlpha(Theme.HUD_ACCENT, 0.5F));
      Render2D.restore();
   }

   private static void drawFlag(Notifications.Toast t, float sw, float y, float v) {
      float w = 150.0F;
      float h = 34.0F;
      float x = sw - 6.0F - w + (1.0F - v) * (w + 12.0F);
      Render2D.save();
      Render2D.globalAlpha(v);
      Render2D.shadow(x, y, w, h, 6.0F, 12.0F, 1879048192);
      Render2D.roundRect(x, y, w, h, 6.0F, -267054062);
      Render2D.roundRectOutline(x + 0.5F, y + 0.5F, w - 1.0F, h - 1.0F, 6.0F, 1.0F, -1426077400);
      float bx = x + 6.0F;
      float by = y + 5.0F;
      float bs = 24.0F;
      Render2D.roundRect(bx, by, bs, bs, 5.0F, -15783401);
      Render2D.roundRectOutline(bx, by, bs, bs, 5.0F, 0.8F, -16711866);
      Icons.radar(bx + 4.0F, by + 4.0F, bs - 8.0F, -16711866);
      Render2D.text("CHUNK FINDER", x + 36.0F, y + 6.0F, 6.2F, Theme.HUD_ACCENT, 1, 0.7F);
      Render2D.text(t.title, x + 36.0F, y + 14.0F, 7.6F, Theme.HUD_TEXT, 1, 0.2F);
      Render2D.text(t.text, x + 36.0F, y + 23.5F, 6.6F, -8519777, 1, 0.3F);
      float p = Math.max(0.0F, 1.0F - t.age / t.duration);
      Render2D.roundRect(x + 6.0F, y + h - 2.2F, (w - 12.0F) * p, 1.2F, 0.6F, -1727987898);
      Render2D.restore();
   }

   private static void drawBanner(Notifications.Toast t, float sw, float y, float v) {
      float h = 26.0F;
      float icon = 16.0F;
      float tw = Math.max(Render2D.textWidth(t.title.toUpperCase(), 7.5F, 1, 0.6F), Render2D.textWidth(t.text, 7.0F, 0));
      float w = tw + icon + 26.0F;
      float x = (sw - w) / 2.0F;
      y -= (1.0F - v) * 20.0F;
      Render2D.save();
      Render2D.globalAlpha(v);
      Render2D.shadow(x, y, w, h, 6.0F, 10.0F, 1610612736);
      Render2D.roundRect(x, y, w, h, 6.0F, Theme.HUD_BG);
      Render2D.roundRectOutline(x, y, w, h, 6.0F, 0.6F, Theme.HUD_OUTLINE);
      Render2D.roundRect(x + 5.0F, y + 5.0F, icon, icon, 3.0F, 587188520);
      if (t.icon != null) {
         Render2D.image(Render2D.mcTexture(t.icon), x + 6.0F, y + 6.0F, icon - 2.0F, icon - 2.0F, 1.0F);
      }

      Render2D.text(t.title.toUpperCase(), x + icon + 12.0F, y + 4.5F, 7.5F, Theme.HUD_ACCENT, 1, 0.6F);
      Render2D.text(t.text, x + icon + 12.0F, y + 14.5F, 7.0F, Theme.HUD_TEXT, 0);
      Render2D.restore();
   }

   private static final class Toast {
      final String title;
      final String text;
      final String icon;
      final boolean banner;
      final boolean flag;
      final float duration;
      float age;
      float anim;

      Toast(String title, String text, String icon, boolean banner, float duration, boolean flag) {
         this.title = title;
         this.text = text;
         this.icon = icon;
         this.banner = banner;
         this.duration = duration;
         this.flag = flag;
      }
   }
}
