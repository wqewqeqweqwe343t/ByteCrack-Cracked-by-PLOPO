package dev.byteplus.module.modules.donut;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.InfoSetting;
import dev.byteplus.setting.NumberSetting;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;

public class PrimeChunkFinderModule extends Module {
   private final NumberSetting maxY = this.add(new NumberSetting("Below Y", 40.0, -64.0, 320.0, 1.0));
   private final NumberSetting threshold = this.add(new NumberSetting("Threshold", 6.0, 1.0, 50.0, 1.0));
   private final BooleanSetting allActivity = this.add(new BooleanSetting("All Activity", false));
   private final BooleanSetting tracers = this.add(new BooleanSetting("Tracers", true));
   private final NumberSetting memory = this.add(new NumberSetting("Memory (min)", 5.0, 1.0, 30.0, 1.0));
   private final InfoSetting count = this.add(new InfoSetting("Chunks", () -> String.valueOf(this.flagged())));
   private static final Set<class_2248> REDSTONE = Set.of(
      class_2246.field_10091,
      class_2246.field_10450,
      class_2246.field_10377,
      class_2246.field_10560,
      class_2246.field_10615,
      class_2246.field_10379,
      class_2246.field_10008,
      class_2246.field_10282,
      class_2246.field_10312,
      class_2246.field_10200,
      class_2246.field_10228,
      class_2246.field_10523,
      class_2246.field_10301,
      class_2246.field_10524,
      class_2246.field_10179,
      class_2246.field_10363,
      class_2246.field_10425,
      class_2246.field_10025,
      class_2246.field_10546,
      class_2246.field_10167,
      class_2246.field_10429,
      class_2246.field_22422,
      class_2246.field_46797,
      class_2246.field_10002,
      class_2246.field_10589,
      class_2246.field_10348,
      class_2246.field_28108,
      class_2246.field_43231,
      class_2246.field_10494,
      class_2246.field_10057,
      class_2246.field_10158,
      class_2246.field_10582,
      class_2246.field_10224,
      class_2246.field_16328,
      class_2246.field_10181,
      class_2246.field_16333,
      class_2246.field_16334
   );
   private final Map<Long, PrimeChunkFinderModule.Activity> activity = new HashMap<>();

   public PrimeChunkFinderModule() {
      super("Prime Chunk Finder", "Finds chunks with underground redstone/machine activity.", Category.DONUT);
   }

   private int flagged() {
      int n = 0;

      for (PrimeChunkFinderModule.Activity a : this.activity.values()) {
         if (a.hits >= this.threshold.getInt()) {
            n++;
         }
      }

      return n;
   }

   @Override
   protected void onDisable() {
      this.activity.clear();
   }

   @Override
   public void onJoin() {
      this.activity.clear();
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 state) {
      if (pos.method_10264() < this.maxY.getInt()) {
         boolean relevant = this.allActivity.isOn()
            || REDSTONE.contains(state.method_26204())
            || mc.field_1687 != null && REDSTONE.contains(mc.field_1687.method_8320(pos).method_26204());
         if (relevant) {
            int cx = pos.method_10263() >> 4;
            int cz = pos.method_10260() >> 4;
            PrimeChunkFinderModule.Activity a = this.activity.computeIfAbsent((long)cx << 32 ^ cz & 4294967295L, k -> new PrimeChunkFinderModule.Activity());
            a.cx = cx;
            a.cz = cz;
            a.hits++;
            a.ySum = a.ySum + pos.method_10264();
            a.last = System.currentTimeMillis();
         }
      }
   }

   @Override
   public void onTick() {
      long now = System.currentTimeMillis();
      this.activity.values().removeIf(a -> now - a.last > this.memory.getInt() * 60000L);
   }

   @Override
   public void onRenderWorld() {
      int rgb = 5046138;

      for (PrimeChunkFinderModule.Activity a : this.activity.values()) {
         if (a.hits >= this.threshold.getInt()) {
            double y = a.ySum / a.hits;
            double x = a.cx * 16;
            double z = a.cz * 16;
            Esp.box(x, y - 0.5, z, x + 16.0, y + 0.5, z + 16.0, rgb, 0, 240);
            if (this.tracers.isOn()) {
               Esp.tracer(x + 8.0, y, z + 8.0, rgb, 220);
            }
         }
      }
   }

   private static final class Activity {
      int cx;
      int cz;
      int hits;
      double ySum;
      long last;
   }
}
