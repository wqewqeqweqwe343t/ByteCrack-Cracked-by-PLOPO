package dev.byteplus.module.modules.render;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.misc.NameProtectModule;
import dev.byteplus.render.Esp;
import dev.byteplus.render.Projection;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Theme;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_640;

public class NameTagsModule extends Module {
   private final NumberSetting scale = this.add(new NumberSetting("Scale", 1.0, 0.5, 2.5, 0.05));
   private final BooleanSetting health = this.add(new BooleanSetting("Health", true));
   private final BooleanSetting ping = this.add(new BooleanSetting("Ping", true));
   private final BooleanSetting distance = this.add(new BooleanSetting("Distance", true));
   private final BooleanSetting heldItem = this.add(new BooleanSetting("Held Item", false));
   private final BooleanSetting background = this.add(new BooleanSetting("Background", true));

   public NameTagsModule() {
      super("NameTags", "Clean, customizable nametags above players.", Category.RENDER);
   }

   public static boolean hideVanilla() {
      return BytePlus.modules() != null && BytePlus.modules().nameTags.isEnabled();
   }

   @Override
   public void onRenderScreen(int fbW, int fbH, float guiScale) {
      float pt = Projection.partialTick();
      List<class_1657> players = new ArrayList<>(mc.field_1687.method_18456());
      players.remove(mc.field_1724);
      players.sort(Comparator.comparingDouble(px -> -px.method_5858(mc.field_1724)));

      for (class_1657 p : players) {
         if (p.method_5805()) {
            class_243 pos = p.method_30950(pt);
            float[] s = Esp.project(pos.field_1352, pos.field_1351 + p.method_17682() + 0.45, pos.field_1350);
            if (s != null) {
               double dist = mc.field_1724.method_5739(p);
               float k = guiScale * this.scale.getFloat() * (float)Math.max(0.55, Math.min(1.0, 12.0 / Math.max(1.0, dist)));
               this.draw(p, s[0], s[1], k, dist);
            }
         }
      }
   }

   private void draw(class_1657 p, float cx, float by, float k, double dist) {
      String name = NameProtectModule.replace(p.method_5477().getString());
      List<String[]> parts = new ArrayList<>();
      parts.add(new String[]{name, Integer.toHexString(Theme.HUD_TEXT)});
      if (this.health.isOn()) {
         float hp = p.method_6032() + p.method_6067();
         float frac = p.method_6032() / Math.max(1.0F, p.method_6063());
         int c = frac > 0.6F ? -9705867 : (frac > 0.3F ? -14040 : -42419);
         if (p.method_6067() > 0.0F) {
            c = -10163;
         }

         parts.add(new String[]{String.format("%.1f", hp), Integer.toHexString(c)});
      }

      if (this.ping.isOn() && mc.method_1562() != null) {
         class_640 info = mc.method_1562().method_2871(p.method_5667());
         if (info != null) {
            parts.add(new String[]{info.method_2959() + "ms", Integer.toHexString(Theme.TEXT_DIM)});
         }
      }

      if (this.distance.isOn()) {
         parts.add(new String[]{String.format("%.0fm", dist), Integer.toHexString(Theme.TEXT_DIM)});
      }

      float ts = 7.5F;
      float gap = 4.0F;
      float padX = 5.0F;
      float h = 12.0F;
      float w = 0.0F;

      for (String[] part : parts) {
         w += Render2D.textWidth(part[0], ts, 1, 0.3F) + gap;
      }

      w = w - gap + padX * 2.0F;
      Render2D.save();
      Render2D.translate(cx, by);
      Render2D.scale(k);
      float x = -w / 2.0F;
      float y = -h;
      if (this.background.isOn()) {
         Render2D.shadow(x, y, w, h, 3.0F, 6.0F, 1342177280);
         Render2D.roundRect(x, y, w, h, 3.0F, -938142701);
         Render2D.roundRect(x + 3.0F, y + h - 1.2F, w - 6.0F, 1.2F, 0.6F, -1996502744);
      }

      float tx = x + padX;

      for (String[] part : parts) {
         int color = (int)Long.parseLong(part[1], 16);
         tx = Render2D.text(part[0], tx, y + (h - ts) / 2.0F - 0.3F, ts, color, 1, 0.3F) + gap;
      }

      if (this.heldItem.isOn() && !p.method_6047().method_7960()) {
         String item = p.method_6047().method_7964().getString();
         float iw = Render2D.textWidth(item, 6.5F, 1, 0.2F);
         Render2D.text(item, -iw / 2.0F, y - 9.0F, 6.5F, Theme.HUD_ACCENT, 1, 0.2F);
      }

      Render2D.restore();
   }
}
