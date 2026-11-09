package dev.byteplus.module;

import dev.byteplus.BytePlus;
import dev.byteplus.hud.Notifications;
import dev.byteplus.setting.KeybindSetting;
import dev.byteplus.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3419;
import net.minecraft.class_6606;

public abstract class Module {
   protected static final class_310 mc = class_310.method_1551();
   private final String name;
   private final String description;
   private final Category category;
   private final List<Setting<?>> settings = new ArrayList<>();
   public final KeybindSetting keybind;
   private boolean enabled;

   protected Module(String name, String description, Category category) {
      this(name, description, category, -1);
   }

   protected Module(String name, String description, Category category, int defaultKey) {
      this.name = name;
      this.description = description;
      this.category = category;
      this.keybind = new KeybindSetting("Keybind", defaultKey);
   }

   protected <S extends Setting<?>> S add(S setting) {
      this.settings.add(setting);
      return setting;
   }

   public void toggle() {
      this.setEnabled(!this.enabled);
   }

   public void setEnabled(boolean value) {
      if (this.enabled != value) {
         this.enabled = value;

         try {
            if (this.enabled) {
               this.onEnable();
            } else {
               this.onDisable();
            }
         } catch (Throwable t) {
            BytePlus.LOGGER.error("Error toggling {}", this.name, t);
         }

         if (this.isToggleable() && mc.field_1724 != null) {
            Notifications.toggled(this);
         }
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public Category getCategory() {
      return this.category;
   }

   public List<Setting<?>> getSettings() {
      return this.settings;
   }

   public boolean isToggleable() {
      return true;
   }

   public boolean persistEnabled() {
      return true;
   }

   public boolean showInArrayList() {
      return true;
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   public void onTick() {
   }

   public void onTickAlways() {
   }

   public void onRenderWorld() {
   }

   public void onRenderScreen(int fbW, int fbH, float guiScale) {
   }

   public void onRenderOverlay(float sw, float sh, float dt) {
   }

   public void onBlockUpdate(class_2338 pos, class_2680 state) {
   }

   public void onBlockChange(class_2338 pos, class_2680 oldState, class_2680 newState) {
   }

   public void onChunkLoad(int cx, int cz) {
   }

   public boolean onScroll(double amount) {
      return false;
   }

   public boolean onTurn(double dx, double dy) {
      return false;
   }

   public void onJoin() {
   }

   public void onBlockEntityData(String source, class_2338 pos, class_2591<?> type, class_2487 tag) {
   }

   public void onLightData(int cx, int cz, class_6606 data) {
   }

   public void onLightUpdatePacket(int cx, int cz, class_6606 data) {
   }

   public void onSoundPacket(class_3419 source, double x, double y, double z) {
   }

   public void onLevelEvent(int type, class_2338 pos) {
   }

   public void onBlockEvent(class_2338 pos, class_2248 block, int b0, int b1) {
   }

   public void onFrame(float dt) {
   }

   public void renderGlobal(float sw, float sh, float dt) {
   }

   protected void info(String text) {
      Notifications.push(this.name, text);
   }
}
