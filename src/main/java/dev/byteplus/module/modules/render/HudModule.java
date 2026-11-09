package dev.byteplus.module.modules.render;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;

public class HudModule extends Module {
   public final NumberSetting scale = this.add(new NumberSetting("Size", 0.75, 0.35, 1.5, 0.05));
   public final BooleanSetting watermark = this.add(new BooleanSetting("Watermark", true));
   public final BooleanSetting coordinates = this.add(new BooleanSetting("Coordinates", true));
   public final BooleanSetting playerName = this.add(new BooleanSetting("Player Name", false));
   public final BooleanSetting fps = this.add(new BooleanSetting("FPS", true));
   public final BooleanSetting ping = this.add(new BooleanSetting("Ping", true));
   public final BooleanSetting arrayList = this.add(new BooleanSetting("Modules", true));
   public final ModeSetting listStyle = this.add(new ModeSetting("List Style", "Gradient", "Gradient", "Rainbow", "Glow", "Classic"));
   public final BooleanSetting armor = this.add(new BooleanSetting("Armor", true));
   public final BooleanSetting potions = this.add(new BooleanSetting("Potions", true));
   public final BooleanSetting keystrokes = this.add(new BooleanSetting("Keystrokes", true));
   public final BooleanSetting radar = this.add(new BooleanSetting("Radar", true));
   public final BooleanSetting radarPlayers = this.add(new BooleanSetting("Radar Players", true));
   public final NumberSetting radarSize = this.add(new NumberSetting("Radar Size", 38.0, 22.0, 80.0, 1.0));
   public final NumberSetting radarRange = this.add(new NumberSetting("Radar Range", 64.0, 16.0, 256.0, 8.0));
   public final BooleanSetting notifications = this.add(new BooleanSetting("Notifications", true));
   public final BooleanSetting toggleNotifications = this.add(new BooleanSetting("Toggle Alerts", true));
   public final NumberSetting infoX = this.add(new NumberSetting("infoX", 4.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting infoY = this.add(new NumberSetting("infoY", 4.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting radarX = this.add(new NumberSetting("radarX", 4.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting radarY = this.add(new NumberSetting("radarY", 52.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting armorX = this.add(new NumberSetting("armorX", 4.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting armorY = this.add(new NumberSetting("armorY", 140.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting potionX = this.add(new NumberSetting("potionX", 4.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting potionY = this.add(new NumberSetting("potionY", 210.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting keysX = this.add(new NumberSetting("keysX", 4.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));
   public final NumberSetting keysY = this.add(new NumberSetting("keysY", 290.0, -10000.0, 10000.0, 0.0).visibleWhen(() -> false));

   public HudModule() {
      super("HUD", "Shows the Byte+ overlay. Drag its parts while the menu is open.", Category.RENDER);
   }
}
