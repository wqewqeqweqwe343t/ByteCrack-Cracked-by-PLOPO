package dev.byteplus.module.modules.donut;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.InfoSetting;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2591;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class BlockEntityDebugModule extends Module {
   private static final int MAX_Y = -10;
   private final BooleanSetting showNbt = this.add(new BooleanSetting("Show NBT", true));
   private final BooleanSetting deduplicate = this.add(new BooleanSetting("Deduplicate", true));
   private final BooleanSetting tracers = this.add(new BooleanSetting("Tracers", true));
   private final InfoSetting count = this.add(new InfoSetting("Tracked", () -> String.valueOf(this.positions.size())));
   private final Map<class_2338, String> positions = new LinkedHashMap<>();
   private final ArrayBlockingQueue<BlockEntityDebugModule.Received> pending = new ArrayBlockingQueue<>(2048);
   private class_1937 level;
   private int ticks;

   public BlockEntityDebugModule() {
      super("Block Entity Debug", "Displays received block entity data below Y -10.", Category.DONUT);
   }

   @Override
   protected void onEnable() {
      this.positions.clear();
      this.pending.clear();
      this.level = mc.field_1687;
   }

   @Override
   protected void onDisable() {
      this.positions.clear();
      this.pending.clear();
      this.level = null;
   }

   @Override
   public void onJoin() {
      this.pending.clear();
   }

   @Override
   public void onBlockEntityData(String source, class_2338 pos, class_2591<?> type, class_2487 tag) {
      if (pos.method_10264() < -10) {
         class_2960 id = type == null ? null : class_7923.field_41181.method_10221(type);
         this.pending.offer(new BlockEntityDebugModule.Received(source, pos.method_10062(), id == null ? "unknown" : id.toString(), tag, mc.field_1687));
      }
   }

   @Override
   public void onTick() {
      if (this.level != mc.field_1687) {
         this.positions.clear();
         this.level = mc.field_1687;
      }

      BlockEntityDebugModule.Received r;
      for (int i = 0; i < 12 && (r = this.pending.poll()) != null; i++) {
         if (r.level == this.level) {
            boolean seen = this.positions.containsKey(r.pos);
            this.positions.put(r.pos, r.type);
            if (!this.deduplicate.isOn() || !seen) {
               if (this.positions.size() > 8192) {
                  this.positions.remove(this.positions.keySet().iterator().next());
               }

               String nbt = "";
               if (this.showNbt.isOn() && r.nbt != null) {
                  nbt = " nbt=" + r.nbt;
                  if (nbt.length() > 2048) {
                     nbt = nbt.substring(0, 2048) + "…";
                  }
               }

               mc.field_1724
                  .method_7353(
                     class_2561.method_43470(
                        "[" + r.source + "] " + r.type + " at (" + r.pos.method_10263() + ", " + r.pos.method_10264() + ", " + r.pos.method_10260() + ")" + nbt
                     ),
                     false
                  );
            }
         }
      }

      if (++this.ticks % 20 == 0) {
         int range = mc.field_1690.method_38521() + 2;
         int pcx = mc.field_1724.method_31477() >> 4;
         int pcz = mc.field_1724.method_31479() >> 4;
         this.positions.keySet().removeIf(p -> Math.abs((p.method_10263() >> 4) - pcx) > range || Math.abs((p.method_10260() >> 4) - pcz) > range);
      }
   }

   private static int color(String type) {
      return switch (type) {
         case "minecraft:trapped_chest" -> 13130752;
         case "minecraft:chest" -> 10246656;
         case "minecraft:ender_chest" -> 7667711;
         case "minecraft:mob_spawner", "minecraft:spawner" -> 9076390;
         case "minecraft:shulker_box" -> 8781982;
         case "minecraft:furnace" -> 8224125;
         case "minecraft:barrel" -> 16747660;
         case "minecraft:enchanting_table" -> 5263615;
         case "minecraft:piston" -> 2351616;
         default -> 16777215;
      };
   }

   @Override
   public void onRenderWorld() {
      int range = mc.field_1690.method_38521();
      int pcx = mc.field_1724.method_31477() >> 4;
      int pcz = mc.field_1724.method_31479() >> 4;

      for (Entry<class_2338, String> e : this.positions.entrySet()) {
         class_2338 p = e.getKey();
         if (Math.abs((p.method_10263() >> 4) - pcx) <= range && Math.abs((p.method_10260() >> 4) - pcz) <= range) {
            int rgb = color(e.getValue());
            Esp.box(p.method_10263(), p.method_10264(), p.method_10260(), p.method_10263() + 1, p.method_10264() + 1, p.method_10260() + 1, rgb, 125, 255);
            if (this.tracers.isOn()) {
               Esp.tracer(p.method_10263() + 0.5, p.method_10264() + 0.5, p.method_10260() + 0.5, rgb, 255);
            }
         }
      }
   }

   private record Received(String source, class_2338 pos, String type, class_2487 nbt, class_1937 level) {
   }
}
