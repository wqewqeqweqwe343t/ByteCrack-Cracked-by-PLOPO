package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_310;
import net.minecraft.class_634;

public class BlinkModule extends Module {
   public final NumberSetting autoRelease = this.add(new NumberSetting("Auto Release (s)", 0.0, 0.0, 20.0, 0.5));
   private static BlinkModule instance;
   private final List<class_2596<?>> held = new ArrayList<>();
   private boolean releasing;
   private long since;

   public BlinkModule() {
      super("Blink", "Freeze your position for others, then teleport when you turn it off.", Category.ANTICHEAT);
      instance = this;
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   @Override
   protected void onEnable() {
      synchronized (this.held) {
         this.held.clear();
      }

      this.since = System.currentTimeMillis();
   }

   @Override
   protected void onDisable() {
      this.release();
   }

   @Override
   public void onTick() {
      if (this.autoRelease.get() > 0.0 && System.currentTimeMillis() - this.since >= this.autoRelease.get() * 1000.0) {
         this.release();
         this.since = System.currentTimeMillis();
      }
   }

   @Override
   public void onJoin() {
      synchronized (this.held) {
         this.held.clear();
      }
   }

   private void release() {
      class_634 conn = class_310.method_1551().method_1562();
      List<class_2596<?>> copy;
      synchronized (this.held) {
         copy = new ArrayList<>(this.held);
         this.held.clear();
      }

      if (conn != null) {
         this.releasing = true;

         try {
            for (class_2596<?> p : copy) {
               conn.method_52787(p);
            }
         } finally {
            this.releasing = false;
         }
      }
   }

   public static boolean onSend(class_2596<?> packet) {
      BlinkModule b = instance;
      if (b == null || !b.isEnabled() || b.releasing || !(packet instanceof class_2828)) {
         return false;
      }

      if (class_310.method_1551().field_1724 == null) {
         return false;
      }

      synchronized (b.held) {
         b.held.add(packet);
         return true;
      }
   }
}
