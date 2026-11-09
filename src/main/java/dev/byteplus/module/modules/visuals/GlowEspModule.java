package dev.byteplus.module.modules.visuals;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.util.Friends;
import dev.byteplus.util.Theme;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_310;

public class GlowEspModule extends Module {
   public final BooleanSetting players = this.add(new BooleanSetting("Players", true));
   public final BooleanSetting hostiles = this.add(new BooleanSetting("Hostile Mobs", false));
   public final BooleanSetting items = this.add(new BooleanSetting("Items", false));
   public final ModeSetting colorMode = this.add(new ModeSetting("Color", "Accent", "Accent", "Custom", "Health"));
   public final ColorSetting custom = this.add(new ColorSetting("Custom Color", -45747));
   private static GlowEspModule instance;

   public GlowEspModule() {
      super("Glow ESP", "Glowing outlines on players, mobs and items through walls.", Category.VISUALS);
      instance = this;
   }

   public static boolean shouldGlow(class_1297 e) {
      if (instance == null || !instance.isEnabled() || e == class_310.method_1551().field_1724) {
         return false;
      } else if (e instanceof class_1657) {
         return instance.players.isOn();
      } else if (e instanceof class_1569) {
         return instance.hostiles.isOn();
      } else {
         return e instanceof class_1542 ? instance.items.isOn() : false;
      }
   }

   public static int color(class_1297 e) {
      if (e instanceof class_1657 p && Friends.isFriend(p)) {
         return Friends.color() & 16777215;
      } else {
         return switch ((String)instance.colorMode.get()) {
            case "Custom" -> instance.custom.argb() & 16777215;
            case "Health" -> {
               if (e instanceof class_1309 l) {
                  float f = Math.max(0.0F, Math.min(1.0F, l.method_6032() / l.method_6063()));
                  yield Theme.mix(-50373, -11800454, f) & 16777215;
               } else {
                  yield Theme.ACCENT & 16777215;
               }
            }
            default -> Theme.ACCENT & 16777215;
         };
      }
   }
}
