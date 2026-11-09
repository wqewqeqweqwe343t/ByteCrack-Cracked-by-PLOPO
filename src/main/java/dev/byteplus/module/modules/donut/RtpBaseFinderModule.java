package dev.byteplus.module.modules.donut;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.setting.StringSetting;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2561;

public class RtpBaseFinderModule extends Module {
   private final ModeSetting region = this.add(new ModeSetting("Region", "East", "Asia", "East", "EU Central", "EU West", "NA West", "Oceania"));
   private final StringSetting command = this.add(new StringSetting("Command", "rtp {region}", 40));
   private final NumberSetting targetY = this.add(new NumberSetting("RTP At Y", -40.0, -64.0, 40.0, 1.0));
   private final NumberSetting waitAfterTp = this.add(new NumberSetting("Wait After TP", 3.0, 1.0, 15.0, 1.0));
   private final NumberSetting cooldown = this.add(new NumberSetting("RTP Cooldown", 12.0, 3.0, 60.0, 1.0));
   private final BooleanSetting stopOnStorage = this.add(new BooleanSetting("Stop On Storage", true));
   private final BooleanSetting lavaCheck = this.add(new BooleanSetting("Lava Check", true));
   private final NumberSetting kickAt = this.add(new NumberSetting("Kick At Storages", 100.0, 1.0, 1000.0, 1.0));
   private int baseCheckTimer;
   private RtpBaseFinderModule.State state = RtpBaseFinderModule.State.RTP;
   private int timer;
   private class_243 lastPos;
   private double lastY;
   private int stuckTicks;
   private long lastRtpMs;

   public RtpBaseFinderModule() {
      super("RTP Base Finder", "Randomly teleports, mines down and looks for bases.", Category.DONUT);
   }

   @Override
   protected void onEnable() {
      this.state = RtpBaseFinderModule.State.RTP;
      this.timer = 0;
      this.baseCheckTimer = 0;
   }

   @Override
   protected void onDisable() {
      mc.field_1690.field_1886.method_23481(false);
   }

   private String regionArg() {
      return switch ((String)this.region.get()) {
         case "Asia" -> "asia";
         case "EU Central" -> "eu-central";
         case "EU West" -> "eu-west";
         case "NA West" -> "west";
         case "Oceania" -> "oceania";
         default -> "east";
      };
   }

   @Override
   public void onTick() {
      class_243 pos = mc.field_1724.method_73189();
      if (--this.baseCheckTimer <= 0) {
         this.baseCheckTimer = 20;
         int found = BytePlus.modules().storageEsp.countStorages(Double.POSITIVE_INFINITY);
         if (found >= this.kickAt.getInt() && mc.method_1562() != null) {
            mc.field_1690.field_1886.method_23481(false);
            this.setEnabled(false);
            mc.method_1562().method_48296().method_10747(class_2561.method_43470("BYTECLIENT: You Found A Base!"));
            return;
         }
      }

      switch (this.state) {
         case RTP:
            String cmd = this.command.get().replace("{region}", this.regionArg()).trim();
            if (cmd.startsWith("/")) {
               cmd = cmd.substring(1);
            }

            mc.method_1562().method_45730(cmd);
            this.lastRtpMs = System.currentTimeMillis();
            this.lastPos = pos;
            this.timer = 400;
            this.state = RtpBaseFinderModule.State.WAIT_TP;
            break;
         case WAIT_TP:
            if (pos.method_1022(this.lastPos) > 64.0) {
               this.timer = this.waitAfterTp.getInt() * 20;
               this.state = RtpBaseFinderModule.State.SETTLE;
            } else if (--this.timer <= 0) {
               this.info("Teleport timed out, retrying");
               this.timer = this.cooldown.getInt() * 20;
               this.state = RtpBaseFinderModule.State.COOLDOWN;
            }
            break;
         case SETTLE:
            if (--this.timer <= 0) {
               this.lastY = pos.field_1351;
               this.stuckTicks = 0;
               this.state = RtpBaseFinderModule.State.MINING;
            }
            break;
         case MINING:
            if (mc.field_1755 != null) {
               mc.field_1690.field_1886.method_23481(false);
               return;
            }

            mc.field_1724.method_36457(90.0F);
            if (this.lavaCheck.isOn() && this.lavaBelow()) {
               mc.field_1690.field_1886.method_23481(false);
               this.info("Lava below - skipping this spot");
               this.timer = this.cooldown.getInt() * 20;
               this.state = RtpBaseFinderModule.State.COOLDOWN;
               return;
            }

            mc.field_1690.field_1886.method_23481(true);
            if (pos.field_1351 <= this.targetY.get()) {
               mc.field_1690.field_1886.method_23481(false);
               if (this.stopOnStorage.isOn() && !BytePlus.modules().storageEsp.scan(128.0).isEmpty()) {
                  this.info("Storage found nearby!");
                  this.setEnabled(false);
                  return;
               }

               long left = this.cooldown.getInt() * 1000L - (System.currentTimeMillis() - this.lastRtpMs);
               this.timer = (int)Math.max(0L, (left + 49L) / 50L);
               this.state = this.timer == 0 ? RtpBaseFinderModule.State.RTP : RtpBaseFinderModule.State.COOLDOWN;
               return;
            }

            if (Math.abs(pos.field_1351 - this.lastY) < 0.01) {
               if (++this.stuckTicks > 200) {
                  this.info("Stuck, moving on");
                  mc.field_1690.field_1886.method_23481(false);
                  this.timer = 20;
                  this.state = RtpBaseFinderModule.State.COOLDOWN;
               }
            } else {
               this.stuckTicks = 0;
            }

            this.lastY = pos.field_1351;
            break;
         case COOLDOWN:
            if (--this.timer <= 0) {
               this.state = RtpBaseFinderModule.State.RTP;
            }
      }
   }

   private boolean lavaBelow() {
      class_2338 base = mc.field_1724.method_24515();

      for (int dy = 1; dy <= 4; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (mc.field_1687.method_8320(base.method_10069(dx, -dy, dz)).method_27852(class_2246.field_10164)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   private enum State {
      RTP,
      WAIT_TP,
      SETTLE,
      MINING,
      COOLDOWN;
   }
}
