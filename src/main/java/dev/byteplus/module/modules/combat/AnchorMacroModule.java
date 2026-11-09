package dev.byteplus.module.modules.combat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.InvUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_4969;
import net.minecraft.class_239.class_240;

public class AnchorMacroModule extends Module {
   private final NumberSetting delay = this.add(new NumberSetting("Delay", 1.0, 0.0, 10.0, 1.0));
   private final BooleanSetting autoPlaced = this.add(new BooleanSetting("Auto On Place", true));
   private final BooleanSetting lookAt = this.add(new BooleanSetting("Looked-At Anchors", true));
   private final BooleanSetting switchBack = this.add(new BooleanSetting("Switch Back", true));
   private class_2338 target;
   private int timer;
   private int targetTimeout;
   private int previousSlot = -1;
   private boolean wasHoldingAnchor;
   private int placedTicks;

   public AnchorMacroModule() {
      super("Anchor Macro", "Automatically charges and explodes anchors you place.", Category.COMBAT);
   }

   @Override
   protected void onDisable() {
      this.target = null;
      this.previousSlot = -1;
   }

   @Override
   public void onTick() {
      boolean holdingAnchor = mc.field_1724.method_6047().method_31574(class_1802.field_23141)
         || mc.field_1724.method_6079().method_31574(class_1802.field_23141);
      if (holdingAnchor && mc.field_1690.field_1904.method_1434()) {
         this.placedTicks = 10;
      } else if (this.placedTicks > 0) {
         this.placedTicks--;
      }

      this.wasHoldingAnchor = holdingAnchor;
      if (this.autoPlaced.isOn()
         && this.target == null
         && this.placedTicks > 0
         && mc.field_1765 instanceof class_3965 hit
         && hit.method_17783() == class_240.field_1332) {
         class_2338 a = hit.method_17777();
         class_2338 b = hit.method_17777().method_10093(hit.method_17780());

         for (class_2338 p : new class_2338[]{a, b}) {
            class_2680 s = mc.field_1687.method_8320(p);
            if (s.method_27852(class_2246.field_23152) && (Integer)s.method_11654(class_4969.field_23153) == 0) {
               this.target = p.method_10062();
               this.targetTimeout = 40;
               break;
            }
         }
      }

      if (this.target == null
         && this.lookAt.isOn()
         && mc.field_1690.field_1904.method_1434()
         && mc.field_1765 instanceof class_3965 hit
         && hit.method_17783() == class_240.field_1332
         && mc.field_1687.method_8320(hit.method_17777()).method_27852(class_2246.field_23152)) {
         this.target = hit.method_17777().method_10062();
         this.targetTimeout = 40;
      }

      if (this.target == null) {
         this.restore();
      } else if (this.timer > 0) {
         this.timer--;
      } else if (--this.targetTimeout > 0 && !(mc.field_1724.method_33571().method_1025(class_243.method_24953(this.target)) > 30.0)) {
         class_2680 state = mc.field_1687.method_8320(this.target);
         if (!state.method_27852(class_2246.field_23152)) {
            this.target = null;
            this.restore();
         } else {
            class_3965 hit = new class_3965(class_243.method_24953(this.target).method_1031(0.0, 0.5, 0.0), class_2350.field_11036, this.target, false);
            if ((Integer)state.method_11654(class_4969.field_23153) == 0) {
               int glow = InvUtil.findHotbar(sx -> sx.method_31574(class_1802.field_8801));
               if (glow == -1) {
                  this.target = null;
                  this.restore();
                  return;
               }

               if (this.previousSlot == -1) {
                  this.previousSlot = InvUtil.selected();
               }

               InvUtil.select(glow);
               this.interact(hit);
            } else {
               int slot = InvUtil.findHotbar(sx -> sx.method_31574(class_1802.field_8288));
               if (slot == -1) {
                  slot = InvUtil.findHotbar(sx -> !sx.method_31574(class_1802.field_8801) && !sx.method_31574(class_1802.field_23141));
               }

               if (slot == -1) {
                  this.target = null;
                  this.restore();
                  return;
               }

               if (this.previousSlot == -1) {
                  this.previousSlot = InvUtil.selected();
               }

               InvUtil.select(slot);
               this.interact(hit);
            }

            this.timer = this.delay.getInt();
         }
      } else {
         this.target = null;
         this.restore();
      }
   }

   private void interact(class_3965 hit) {
      mc.field_1761.method_2896(mc.field_1724, class_1268.field_5808, hit);
      mc.field_1724.method_6104(class_1268.field_5808);
   }

   private void restore() {
      if (this.previousSlot != -1 && this.switchBack.isOn()) {
         InvUtil.select(this.previousSlot);
      }

      this.previousSlot = -1;
   }
}
