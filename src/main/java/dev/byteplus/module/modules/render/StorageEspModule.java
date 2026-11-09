package dev.byteplus.module.modules.render;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.setting.BlockListSetting;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.InfoSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.WorldUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2611;
import net.minecraft.class_2680;
import net.minecraft.class_2745;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class StorageEspModule extends Module {
   public final NumberSetting alpha = this.add(new NumberSetting("Alpha", 125.0, 0.0, 255.0, 1.0));
   public final BooleanSetting tracers = this.add(new BooleanSetting("Tracers", true));
   public final BooleanSetting fill = this.add(new BooleanSetting("Fill", true));
   public final BooleanSetting outline = this.add(new BooleanSetting("Outline", true));
   public final BlockListSetting storages;
   public final InfoSetting count;
   private static final int CHEST = 16756800;
   private static final int TRAPPED = 16734780;
   private static final int ENDER = 11820287;
   private static final int SHULKER = 16735432;
   private static final int BARREL = 13670997;
   private static final int HOPPER = 10134709;
   private static final int DISPENSER = 13160152;
   private static final int FURNACE = 8225424;
   private static final int SPAWNER = 5046138;
   private static final int COPPER = 14254671;
   private static final int VAULT = 7328767;
   private static final int BREWING = 14729802;
   private static final int POT = 12613712;
   private static final int BOOKSHELF = 11569232;
   private volatile List<StorageEspModule.Box> boxes = Collections.emptyList();
   private int scanTimer;

   private static Map<class_2248, Integer> storageBlocks() {
      Map<class_2248, Integer> out = new LinkedHashMap<>();
      Map<String, Integer> fixed = new LinkedHashMap<>();
      fixed.put("chest", 16756800);
      fixed.put("trapped_chest", 16734780);
      fixed.put("ender_chest", 11820287);
      fixed.put("barrel", 13670997);
      fixed.put("shulker_box", 16735432);
      fixed.put("hopper", 10134709);
      fixed.put("dispenser", 13160152);
      fixed.put("dropper", 13160152);
      fixed.put("crafter", 13160152);
      fixed.put("furnace", 8225424);
      fixed.put("blast_furnace", 8225424);
      fixed.put("smoker", 8225424);
      fixed.put("brewing_stand", 14729802);
      fixed.put("spawner", 5046138);
      fixed.put("trial_spawner", 5046138);
      fixed.put("vault", 7328767);
      fixed.put("decorated_pot", 12613712);
      fixed.put("chiseled_bookshelf", 11569232);

      for (Entry<String, Integer> e : fixed.entrySet()) {
         class_2248 b = (class_2248)class_7923.field_41175.method_63535(class_2960.method_60656(e.getKey()));
         if (b != null && b != class_2246.field_10124) {
            out.put(b, e.getValue());
         }
      }

      for (class_2248 b : class_7923.field_41175) {
         String id = class_7923.field_41175.method_10221(b).method_12832();
         if (id.endsWith("_shulker_box")) {
            out.putIfAbsent(b, 16735432);
         } else if (id.contains("copper_chest")) {
            out.putIfAbsent(b, 14254671);
         }
      }

      return out;
   }

   private static Map<class_2248, Integer> enabledByDefault(Map<class_2248, Integer> all) {
      Map<class_2248, Integer> on = new LinkedHashMap<>();

      for (Entry<class_2248, Integer> e : all.entrySet()) {
         String id = class_7923.field_41175.method_10221(e.getKey()).method_12832();
         if (id.equals("chest")
            || id.equals("trapped_chest")
            || id.equals("ender_chest")
            || id.equals("barrel")
            || id.contains("shulker_box")
            || id.contains("copper_chest")
            || id.equals("spawner")) {
            on.put(e.getKey(), e.getValue());
         }
      }

      return on;
   }

   public StorageEspModule() {
      super("Storage ESP", "Highlights storage blocks through walls, across your whole render distance.", Category.RENDER);
      Map<class_2248, Integer> all = storageBlocks();
      this.storages = this.add(new BlockListSetting("Storages", enabledByDefault(all)).limitTo(List.copyOf(all.keySet()), all));
      this.count = this.add(new InfoSetting("Found", () -> this.boxes.size() + " blocks"));
   }

   public int storageCount() {
      return this.boxes.size();
   }

   @Override
   protected void onEnable() {
      this.scanTimer = 0;
   }

   @Override
   protected void onDisable() {
      this.boxes = Collections.emptyList();
   }

   @Override
   public void onTick() {
      if (--this.scanTimer <= 0) {
         this.scanTimer = 5;
         this.boxes = this.scan(Double.POSITIVE_INFINITY);
      }
   }

   public List<StorageEspModule.Box> scan(double maxRange) {
      List<StorageEspModule.Box> found = new ArrayList<>();
      double maxSq = maxRange * maxRange;

      for (class_2818 chunk : WorldUtil.loadedChunks()) {
         for (class_2586 be : chunk.method_12214().values()) {
            int rgb = this.colorFor(be);
            if (rgb != -1) {
               class_2338 pos = be.method_11016();
               if (!(WorldUtil.distSqToPlayer(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5) > maxSq)) {
                  StorageEspModule.Box box = boxFor(be, pos, rgb);
                  if (box != null) {
                     found.add(box);
                  }
               }
            }
         }
      }

      return found;
   }

   public int countStorages(double maxRange) {
      int n = 0;
      double maxSq = maxRange * maxRange;

      for (class_2818 chunk : WorldUtil.loadedChunks()) {
         for (class_2586 be : chunk.method_12214().values()) {
            if (this.colorFor(be) != -1) {
               class_2338 pos = be.method_11016();
               if (!(WorldUtil.distSqToPlayer(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5) > maxSq)) {
                  n++;
               }
            }
         }
      }

      return n;
   }

   private int colorFor(class_2586 be) {
      class_2248 b = be.method_11010().method_26204();
      return this.storages.contains(b) ? this.storages.colorOf(b) & 16777215 : -1;
   }

   private static StorageEspModule.Box boxFor(class_2586 be, class_2338 pos, int rgb) {
      double x = pos.method_10263();
      double y = pos.method_10264();
      double z = pos.method_10260();
      if (!(be instanceof class_2595) && !(be instanceof class_2611)) {
         return new StorageEspModule.Box(x, y, z, x + 1.0, y + 1.0, z + 1.0, rgb);
      }

      double i = 0.0625;
      double minX = x + i;
      double minZ = z + i;
      double maxX = x + 1.0 - i;
      double maxZ = z + 1.0 - i;
      double maxY = y + 0.875;
      class_2680 state = be.method_11010();
      if (be instanceof class_2595 && state.method_28498(class_2281.field_10770) && state.method_11654(class_2281.field_10770) != class_2745.field_12569) {
         class_2350 dir = class_2281.method_9758(state);
         class_2338 other = pos.method_10093(dir);
         if (Long.compare(pos.method_10063(), other.method_10063()) > 0) {
            return null;
         }

         minX = Math.min(minX, other.method_10263() + i);
         minZ = Math.min(minZ, other.method_10260() + i);
         maxX = Math.max(maxX, other.method_10263() + 1 - i);
         maxZ = Math.max(maxZ, other.method_10260() + 1 - i);
      }

      return new StorageEspModule.Box(minX, y, minZ, maxX, maxY, maxZ, rgb);
   }

   @Override
   public void onRenderWorld() {
      int a = this.alpha.getInt();

      for (StorageEspModule.Box b : this.boxes) {
         Esp.box(
            b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ, b.rgb, this.fill.isOn() ? (int)(a * 0.7F) : 0, this.outline.isOn() ? Math.min(255, a + 110) : 0
         );
         if (this.tracers.isOn()) {
            Esp.tracer((b.minX + b.maxX) / 2.0, (b.minY + b.maxY) / 2.0, (b.minZ + b.maxZ) / 2.0, b.rgb, 230);
         }
      }
   }

   public record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int rgb) {
   }
}
