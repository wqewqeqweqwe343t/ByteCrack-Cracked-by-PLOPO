package dev.byteplus.module.modules.visuals;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.knife.KnifeModels;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.KeybindSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_1799;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_3675;

public class KnivesModule extends Module {
   public final ModeSetting knife = this.add(new ModeSetting("Knife", "Butterfly", KnifeModels.TYPES));
   public final ModeSetting finish = this.add(new ModeSetting("Finish", "Fade", KnifeModels.FINISHES));
   public final KeybindSetting inspectKey = this.add(new KeybindSetting("Inspect Key", 86));
   public final NumberSetting animationSpeed = this.add(new NumberSetting("Animation Speed", 1.0, 0.5, 2.0, 0.05));
   public final NumberSetting size = this.add(new NumberSetting("Size", 1.0, 0.6, 1.6, 0.05));
   public final NumberSetting posX = this.add(new NumberSetting("Position X", -0.06, -0.4, 0.4, 0.01));
   public final NumberSetting posY = this.add(new NumberSetting("Position Y", 0.08, -0.4, 0.4, 0.01));
   public final NumberSetting posZ = this.add(new NumberSetting("Position Z", 0.0, -0.4, 0.4, 0.01));
   public final NumberSetting tilt = this.add(new NumberSetting("Tilt", 28.0, -30.0, 60.0, 1.0));
   public final BooleanSetting alwaysBright = this.add(new BooleanSetting("Always Bright", true));
   public final BooleanSetting drawAnimation = this.add(new BooleanSetting("Draw Animation", true));
   public final BooleanSetting idleAnimation = this.add(new BooleanSetting("Idle Animation", true));
   public float drawTime = -1.0F;
   public float inspectTime = -1.0F;
   public float idleTime;
   public float swayYaw;
   public float swayPitch;
   public float sprint;
   public float bobPhase;
   private float lastYaw = Float.NaN;
   private float lastPitch;
   private int lastSlot = -1;
   private boolean lastWasSword;
   private boolean inspectHeld;
   public static final float DRAW_LENGTH = 1.15F;
   public static final float INSPECT_LENGTH = 4.2F;

   public KnivesModule() {
      super(
         "CSGO Knives",
         "Animated knives in place of your sword: butterfly, karambit, bayonet, flip, bowie. Press the Inspect key to show it off.",
         Category.VISUALS
      );
   }

   public static boolean isSword(class_1799 s) {
      return s != null && !s.method_7960() && s.method_31573(class_3489.field_42611);
   }

   @Override
   protected void onEnable() {
      this.drawTime = this.drawAnimation.isOn() ? 0.0F : -1.0F;
      this.inspectTime = -1.0F;
   }

   @Override
   public void onFrame(float dt) {
      if (mc.field_1724 != null) {
         float speed = this.animationSpeed.getFloat();
         this.idleTime += dt;
         class_1799 held = mc.field_1724.method_6047();
         boolean sword = isSword(held);
         int slot = mc.field_1724.method_31548().method_67532();
         if (sword && (!this.lastWasSword || slot != this.lastSlot)) {
            this.drawTime = this.drawAnimation.isOn() ? 0.0F : -1.0F;
            this.inspectTime = -1.0F;
         }

         this.lastWasSword = sword;
         this.lastSlot = slot;
         int key = this.inspectKey.getKey();
         boolean down = key != -1 && mc.field_1755 == null && class_3675.method_15987(mc.method_22683(), key);
         if (down && !this.inspectHeld && sword && this.drawTime < 0.0F) {
            this.inspectTime = 0.0F;
         }

         this.inspectHeld = down;
         if (mc.field_1724.field_6252) {
            this.inspectTime = -1.0F;
         }

         if (this.drawTime >= 0.0F) {
            this.drawTime += dt * speed;
            if (this.drawTime > 1.15F) {
               this.drawTime = -1.0F;
            }
         }

         if (this.inspectTime >= 0.0F) {
            this.inspectTime += dt * speed;
            if (this.inspectTime > 4.2F) {
               this.inspectTime = -1.0F;
            }
         }

         float yaw = mc.field_1724.method_36454();
         float pitch = mc.field_1724.method_36455();
         if (!Float.isNaN(this.lastYaw) && dt > 0.0F) {
            float dy = class_3532.method_15393(yaw - this.lastYaw);
            float dp = pitch - this.lastPitch;
            this.swayYaw = Math.max(-12.0F, Math.min(12.0F, this.swayYaw + dy * 0.35F));
            this.swayPitch = Math.max(-10.0F, Math.min(10.0F, this.swayPitch + dp * 0.3F));
         }

         this.lastYaw = yaw;
         this.lastPitch = pitch;
         float spring = (float)Math.exp(-dt * 9.0F);
         this.swayYaw *= spring;
         this.swayPitch *= spring;
         this.sprint = this.sprint + ((mc.field_1724.method_5624() ? 1 : 0) - this.sprint) * (1.0F - (float)Math.exp(-dt * 8.0F));
         double moving = mc.field_1724.method_18798().method_37267();
         this.bobPhase = this.bobPhase + dt * (float)Math.min(1.2, moving * 9.0) * 9.0F;
      }
   }
}
