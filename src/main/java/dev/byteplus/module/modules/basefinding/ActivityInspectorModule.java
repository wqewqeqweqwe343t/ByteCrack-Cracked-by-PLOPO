package dev.byteplus.module.modules.basefinding;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.util.WorldUtil;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1923;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2453;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2591;
import net.minecraft.class_2665;
import net.minecraft.class_2667;
import net.minecraft.class_2671;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2745;
import net.minecraft.class_2818;
import net.minecraft.class_3419;
import net.minecraft.class_5703;
import net.minecraft.class_642;
import net.minecraft.class_6606;
import net.minecraft.class_7126;
import net.minecraft.class_7268;
import net.minecraft.class_7923;
import net.minecraft.class_2902.class_2903;

public class ActivityInspectorModule extends Module {
   private static final int DEEP_Y = 0;
   private static final int PLATE_CAP = 55;
   private static final double SELF_RANGE_SQ = 36.0;
   private static final int DIG_MIN_LENGTH = 8;
   private static final long DIG_WINDOW_MS = 120000L;
   private final ColorSetting color = this.add(new ColorSetting("Color", 1510014790));
   private final BooleanSetting redstone = this.add(new BooleanSetting("Moving Redstone", true));
   private final BooleanSetting light = this.add(new BooleanSetting("Light Updates", true));
   private final BooleanSetting digging = this.add(new BooleanSetting("Digging Down", true));
   private final BooleanSetting blockEntities = this.add(new BooleanSetting("Block Entities", true));
   private final BooleanSetting sounds = this.add(new BooleanSetting("Sounds", true));
   private final BooleanSetting activeTicks = this.add(new BooleanSetting("Active Ticks", true));
   private final Map<String, Map<Long, ActivityInspectorModule.Flag>> flagsByWorld = new HashMap<>();
   private final Map<Long, Map<Integer, Long>> digColumns = new HashMap<>();
   private int tickCounter;
   private static final Set<String> NATURAL_BLOCK_ENTITIES = Set.of(
      "spawner",
      "trial_spawner",
      "vault",
      "sculk_sensor",
      "calibrated_sculk_sensor",
      "sculk_shrieker",
      "sculk_catalyst",
      "suspicious_sand",
      "suspicious_gravel",
      "decorated_pot",
      "dispenser"
   );
   private static final Set<String> NATURAL_EVENT_BLOCKS = Set.of(
      "spawner", "trial_spawner", "vault", "sculk_sensor", "calibrated_sculk_sensor", "sculk_shrieker", "sculk_catalyst"
   );

   public ActivityInspectorModule() {
      super("Activity Inspector", "Flags chunks with redstone, light, digging, storage, sound or ticking activity below Y 0.", Category.BASEFINDING);
   }

   @Override
   protected void onEnable() {
      if (mc.field_1687 != null && this.blockEntities.isOn()) {
         for (class_2818 c : WorldUtil.loadedChunks()) {
            this.scanBlockEntities(c);
         }
      }
   }

   @Override
   protected void onDisable() {
      this.flagsByWorld.clear();
      this.digColumns.clear();
   }

   private String worldKey() {
      class_642 server = mc.method_1558();
      return (server != null ? server.field_3761 : "local") + "|" + mc.field_1687.method_27983();
   }

   private Map<Long, ActivityInspectorModule.Flag> flags() {
      return this.flagsByWorld.computeIfAbsent(this.worldKey(), k -> new LinkedHashMap<>());
   }

   private void flag(int cx, int cz) {
      if (mc.field_1687 != null) {
         ActivityInspectorModule.Flag f = this.flags().computeIfAbsent(class_1923.method_8331(cx, cz), k -> new ActivityInspectorModule.Flag(cx, cz));
         if (!f.heightKnown) {
            this.updateHeight(f);
         }
      }
   }

   private void flagAt(double x, double z) {
      this.flag((int)Math.floor(x) >> 4, (int)Math.floor(z) >> 4);
   }

   private void updateHeight(ActivityInspectorModule.Flag f) {
      class_2818 c = WorldUtil.chunk(f.cx, f.cz);
      if (c != null) {
         int top = Integer.MIN_VALUE;

         for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
               top = Math.max(top, c.method_12005(class_2903.field_13202, x, z));
            }
         }

         f.plateY = Math.min(top + 1, 55);
         f.heightKnown = true;
      }
   }

   private boolean nearMe(double x, double y, double z) {
      return mc.field_1724 != null && mc.field_1724.method_5649(x, y, z) < 36.0;
   }

   @Override
   public void onBlockChange(class_2338 pos, class_2680 oldState, class_2680 newState) {
      if (mc.field_1687 != null && oldState != newState) {
         double cx = pos.method_10263() + 0.5;
         double cy = pos.method_10264() + 0.5;
         double cz = pos.method_10260() + 0.5;
         if (!this.nearMe(cx, cy, cz)) {
            if (this.redstone.isOn() && pos.method_10264() < 0 && (isRedstone(oldState) || isRedstone(newState))) {
               this.flagAt(cx, cz);
            }

            if (this.digging.isOn() && pos.method_10264() < 55 && !oldState.method_26215() && newState.method_26215()) {
               this.trackDig(pos);
            }
         }
      }
   }

   private static boolean isRedstone(class_2680 s) {
      class_2248 b = s.method_26204();
      return !(b instanceof class_5703) && !(b instanceof class_7268) && !(b instanceof class_7126)
         ? s.method_26219()
            || s.method_28498(class_2741.field_12484)
            || s.method_28498(class_2741.field_12511)
            || s.method_28498(class_2741.field_12515)
            || b instanceof class_2665
            || b instanceof class_2671
            || b instanceof class_2667
            || b instanceof class_2453
         : false;
   }

   private void trackDig(class_2338 pos) {
      long now = System.currentTimeMillis();
      long col = class_2338.method_10064(pos.method_10263(), 0, pos.method_10260());
      Map<Integer, Long> ys = this.digColumns.computeIfAbsent(col, k -> new HashMap<>());
      ys.put(pos.method_10264(), now);
      ys.values().removeIf(t -> now - t > 120000L);
      int up = 0;
      int down = 0;

      while (ys.containsKey(pos.method_10264() + up + 1)) {
         up++;
      }

      while (ys.containsKey(pos.method_10264() - down - 1)) {
         down++;
      }

      if (up + down + 1 >= 8) {
         this.flagAt(pos.method_10263(), pos.method_10260());
         this.digColumns.remove(col);
      }
   }

   @Override
   public void onLightUpdatePacket(int cx, int cz, class_6606 data) {
      if (this.light.isOn() && mc.field_1687 != null && mc.field_1724 != null) {
         class_1923 me = mc.field_1724.method_31476();
         if (Math.max(Math.abs(cx - me.field_9181), Math.abs(cz - me.field_9180)) > 1) {
            int firstLightSection = mc.field_1687.method_32891() - 1;
            BitSet changed = new BitSet();
            changed.or(data.method_38601());
            changed.or(data.method_38608());
            changed.or(data.method_38604());
            changed.or(data.method_38609());

            for (int i = changed.nextSetBit(0); i >= 0; i = changed.nextSetBit(i + 1)) {
               int sectionY = firstLightSection + i;
               if (sectionY * 16 + 15 < 0) {
                  this.flag(cx, cz);
                  return;
               }
            }
         }
      }
   }

   @Override
   public void onChunkLoad(int cx, int cz) {
      if (mc.field_1687 != null) {
         class_2818 c = WorldUtil.chunk(cx, cz);
         if (c != null) {
            if (this.blockEntities.isOn()) {
               this.scanBlockEntities(c);
            }

            ActivityInspectorModule.Flag f = this.flags().get(class_1923.method_8331(cx, cz));
            if (f != null) {
               this.updateHeight(f);
            }
         }
      }
   }

   private void scanBlockEntities(class_2818 c) {
      for (class_2586 be : c.method_12214().values()) {
         class_2338 p = be.method_11016();
         if (p.method_10264() < 0 && playerMade(be.method_11010())) {
            this.flag(c.method_12004().field_9181, c.method_12004().field_9180);
            return;
         }
      }
   }

   @Override
   public void onBlockEntityData(String source, class_2338 pos, class_2591<?> type, class_2487 tag) {
      if (this.blockEntities.isOn() && mc.field_1687 != null && pos.method_10264() < 0) {
         if (!this.nearMe(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5)) {
            if (playerMade(mc.field_1687.method_8320(pos))) {
               this.flagAt(pos.method_10263(), pos.method_10260());
            }
         }
      }
   }

   private static boolean playerMade(class_2680 state) {
      String id = class_7923.field_41175.method_10221(state.method_26204()).method_12832();
      if (NATURAL_BLOCK_ENTITIES.contains(id)) {
         return false;
      } else {
         return state.method_28498(class_2281.field_10770) ? state.method_11654(class_2281.field_10770) != class_2745.field_12569 : true;
      }
   }

   @Override
   public void onSoundPacket(class_3419 source, double x, double y, double z) {
      if (this.sounds.isOn() && !(y >= 0.0)) {
         if (source == class_3419.field_15245 || source == class_3419.field_15248) {
            if (!this.nearMe(x, y, z)) {
               this.flagAt(x, z);
            }
         }
      }
   }

   @Override
   public void onLevelEvent(int type, class_2338 pos) {
      if (this.sounds.isOn() && pos != null && pos.method_10264() < 0) {
         if (type == 2001 || type >= 1000 && type <= 1099) {
            if (!this.nearMe(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5)) {
               this.flagAt(pos.method_10263(), pos.method_10260());
            }
         }
      }
   }

   @Override
   public void onBlockEvent(class_2338 pos, class_2248 block, int b0, int b1) {
      if (this.activeTicks.isOn() && pos != null && pos.method_10264() < 0) {
         if (!NATURAL_EVENT_BLOCKS.contains(class_7923.field_41175.method_10221(block).method_12832())) {
            if (!this.nearMe(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5)) {
               this.flagAt(pos.method_10263(), pos.method_10260());
            }
         }
      }
   }

   @Override
   public void onTick() {
      if (++this.tickCounter % 20 == 0 && mc.field_1724 != null) {
         int rd = mc.field_1690.method_38521() + 1;
         class_1923 me = mc.field_1724.method_31476();
         Iterator<ActivityInspectorModule.Flag> it = this.flags().values().iterator();
         boolean refresh = this.tickCounter % 100 == 0;

         while (it.hasNext()) {
            ActivityInspectorModule.Flag f = it.next();
            if (Math.max(Math.abs(f.cx - me.field_9181), Math.abs(f.cz - me.field_9180)) > rd) {
               it.remove();
            } else if (refresh || !f.heightKnown) {
               this.updateHeight(f);
            }
         }

         long now = System.currentTimeMillis();
         this.digColumns.values().removeIf(ys -> {
            ys.values().removeIf(t -> now - t > 120000L);
            return ys.isEmpty();
         });
      }
   }

   @Override
   public void onRenderWorld() {
      if (mc.field_1687 != null) {
         Map<Long, ActivityInspectorModule.Flag> map = this.flags();
         if (!map.isEmpty()) {
            int argb = this.color.argb();
            int rgb = argb & 16777215;
            int fill = argb >>> 24;
            if (fill == 0) {
               fill = 90;
            }

            for (ActivityInspectorModule.Flag f : map.values()) {
               double x = f.cx * 16.0;
               double z = f.cz * 16.0;
               double y = f.plateY;
               Esp.box(x, y, z, x + 16.0, y + 0.05, z + 16.0, rgb, fill, 220);
            }
         }
      }
   }

   private static final class Flag {
      final int cx;
      final int cz;
      int plateY = 55;
      boolean heightKnown;

      Flag(int cx, int cz) {
         this.cx = cx;
         this.cz = cz;
      }
   }
}
