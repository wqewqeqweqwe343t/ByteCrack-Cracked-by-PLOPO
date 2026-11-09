package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.module.ModuleManager;
import dev.byteplus.render.Render2D;
import dev.byteplus.util.Theme;
import net.minecraft.class_155;
import net.minecraft.class_310;
import net.minecraft.class_442;

public final class TitleScreenOverlay {
   private static float appear;

   private TitleScreenOverlay() {
   }

   public static boolean active() {
      ModuleManager mods = BytePlus.modules();
      return mods != null && mods.clickGui.mainMenu.is("Classic") && class_310.method_1551().field_1755 instanceof class_442;
   }

   public static void draw(float sw, float sh, float dt) {
      appear = Math.min(1.0F, appear + dt * 1.6F);
      float a = 1.0F - (float)Math.pow(1.0F - appear, 3.0);
      double t = System.nanoTime() / 1.0E9;
      float cx = sw / 2.0F;
      float top = 26.0F - (1.0F - a) * 10.0F;
      Render2D.save();
      Render2D.globalAlpha(a);
      float bob = (float)Math.sin(t * 1.4) * 1.5F;
      Render2D.shadow(cx - 70.0F, top + 4.0F + bob, 140.0F, 40.0F, 20.0F, 40.0F, Theme.ACCENT & 16777215 | 1073741824);
      float logo = 34.0F;
      String title = "CRACKED BY PLOPO";
      float ts = 28.0F;
      float tw = Render2D.textWidth(title, ts, 1, 2.2F);
      float total = logo + 8.0F + tw;
      float lx = cx - total / 2.0F;
      Render2D.image(Render2D.modImage("/assets/byteplus/textures/logo.png"), lx, top + bob, logo, logo, 1.0F);
      Render2D.text(title, lx + logo + 8.0F, top + (logo - ts) / 2.0F + 2.0F + bob, ts, Theme.ACCENT, 1, 2.2F);
      String sub = "UTILITY CLIENT  ·  " + class_155.method_16673().comp_4025();
      float sw2 = Render2D.textWidth(sub, 7.0F, 1, 1.4F);
      Render2D.text(sub, cx - sw2 / 2.0F, top + logo + 7.0F + bob, 7.0F, Render2D.withAlpha(Theme.TEXT, 0.75F), 1, 1.4F);
      float lw = 60.0F + 20.0F * (float)Math.sin(t * 2.0);
      Render2D.roundRect(cx - lw / 2.0F, top + logo + 19.0F + bob, lw, 1.4F, 0.7F, Render2D.withAlpha(Theme.ACCENT, 0.8F));
      Render2D.text("Cracked By PLOPO  —  press Right Shift in-game", 4.0F, sh - 22.0F, 6.2F, Render2D.withAlpha(Theme.TEXT_DIM, 0.9F), 0);
      Render2D.restore();
   }

   public static void reset() {
      appear = 0.0F;
   }
}
