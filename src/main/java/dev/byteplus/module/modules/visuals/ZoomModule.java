package dev.byteplus.module.modules.visuals;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.KeybindSetting;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_3675;

public class ZoomModule extends Module {
   public final KeybindSetting zoomKey = this.add(new KeybindSetting("Zoom Key", 67));
   public final NumberSetting zoom = this.add(new NumberSetting("Zoom", 4.0, 1.5, 15.0, 0.5));
   public final BooleanSetting smooth = this.add(new BooleanSetting("Smooth", true));
   public final BooleanSetting scrollAdjust = this.add(new BooleanSetting("Scroll Adjust", true));
   public final BooleanSetting cinematic = this.add(new BooleanSetting("Cinematic Camera", false));
   private static ZoomModule instance;
   private float factor = 1.0F;
   private float scrollZoom = -1.0F;
   private boolean zooming;
   private boolean wasCinematic;
   private boolean cinematicSet;

   public ZoomModule() {
      super("Zoom", "Hold the zoom key (C) to zoom in. Scroll while zooming to adjust.", Category.VISUALS);
      instance = this;
   }

   @Override
   public boolean showInArrayList() {
      return false;
   }

   public static float fovMultiplier() {
      return instance != null && instance.isEnabled() ? 1.0F / instance.factor : 1.0F;
   }

   @Override
   public void onFrame(float dt) {
      int key = this.zoomKey.getKey();
      this.zooming = key != -1 && mc.field_1755 == null && class_3675.method_15987(mc.method_22683(), key);
      if (!this.zooming) {
         this.scrollZoom = -1.0F;
      }

      float target = this.zooming ? (this.scrollZoom > 0.0F ? this.scrollZoom : this.zoom.getFloat()) : 1.0F;
      if (this.smooth.isOn()) {
         this.factor = this.factor + (target - this.factor) * (1.0F - (float)Math.exp(-dt * 14.0F));
      } else {
         this.factor = target;
      }

      if (Math.abs(this.factor - target) < 0.001F) {
         this.factor = target;
      }

      if (this.cinematic.isOn()) {
         if (this.zooming && !this.cinematicSet) {
            this.wasCinematic = mc.field_1690.field_1914;
            mc.field_1690.field_1914 = true;
            this.cinematicSet = true;
         } else if (!this.zooming && this.cinematicSet) {
            mc.field_1690.field_1914 = this.wasCinematic;
            this.cinematicSet = false;
         }
      }
   }

   @Override
   public boolean onScroll(double amount) {
      if (this.zooming && this.scrollAdjust.isOn()) {
         float z = this.scrollZoom > 0.0F ? this.scrollZoom : this.zoom.getFloat();
         this.scrollZoom = (float)Math.max(1.2, Math.min(40.0, z * (amount > 0.0 ? 1.15 : 0.8695652173913044)));
         return true;
      } else {
         return false;
      }
   }

   @Override
   protected void onDisable() {
      this.factor = 1.0F;
      if (this.cinematicSet) {
         mc.field_1690.field_1914 = this.wasCinematic;
         this.cinematicSet = false;
      }
   }
}
