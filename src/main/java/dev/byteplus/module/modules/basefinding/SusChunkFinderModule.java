package dev.byteplus.module.modules.basefinding;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.CachedThickness;
import dev.byteplus.render.Esp;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.InfoSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.ChunkSetBuilder;
import dev.byteplus.util.WorldUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_4481;
import net.minecraft.class_2338.class_2339;

public class SusChunkFinderModule extends Module {
   private static final double PLATE_Y = 63.0;
   private static final double PLATE_THICKNESS = 0.05;
   private static final int MAX_CLUSTER_MARKERS = 400;
   private static final long SCAN_BUDGET_NANOS = 4000000L;
   private static final int SCAN_CHUNKS_PER_TICK = 24;
   private static final int MAX_TICKS_BETWEEN_COMMITS = 60;
   private final NumberSetting sensitivity = this.add(new NumberSetting("Sensitivity", 12.0, 1.0, 20.0, 1.0));
   private final NumberSetting simulationDistance = this.add(new NumberSetting("Simulation Distance", 4.0, 2.0, 16.0, 1.0));
   private final NumberSetting alpha = this.add(new NumberSetting("Alpha", 140.0, 0.0, 255.0, 1.0));
   private final BooleanSetting markGeodeChunks = this.add(new BooleanSetting("Mark Geode Chunks", false));
   private final BooleanSetting renderClusters = this.add(new BooleanSetting("Render Clusters", false));
   private final InfoSetting info = this.add(new InfoSetting("Flagged", () -> this.geodes.size() + " chunks"));
   private final Map<Long, SusChunkFinderModule.Flagged> geodes = new HashMap<>();
   private final ArrayDeque<Long> scanQueue = new ArrayDeque<>();
   private final Set<Long> queued = new HashSet<>();
   private final Set<Long> scanned = new HashSet<>();
   private LongOpenHashSet candidates = new LongOpenHashSet();
   private final CachedThickness candidatePlates = new CachedThickness();
   private boolean candidatesDirty = true;
   private int cachedSimRadius = -1;
   private int cachedSensitivity = -1;
   private class_1937 trackedLevel;
   private int ticks;
   private int ticksSinceCommit;

   public SusChunkFinderModule() {
      super("Sus Chunk Finder", "Finds suspicious chunks from amethyst and full-beehive generation signals.", Category.BASEFINDING);
   }

   private void reset() {
      this.geodes.clear();
      this.scanQueue.clear();
      this.queued.clear();
      this.scanned.clear();
      this.candidates = new LongOpenHashSet();
      this.candidatesDirty = true;
      this.cachedSimRadius = -1;
      this.cachedSensitivity = -1;
      this.trackedLevel = null;
      this.ticks = 0;
      this.ticksSinceCommit = 0;
   }

   @Override
   protected void onEnable() {
      this.reset();
      this.trackedLevel = mc.field_1687;
      this.enqueueLoaded();
   }

   @Override
   protected void onDisable() {
      this.reset();
   }

   @Override
   public void onChunkLoad(int cx, int cz) {
      this.enqueue(cx, cz);
   }

   private static boolean amethyst(class_2248 b) {
      return b == class_2246.field_27159
         || b == class_2246.field_27160
         || b == class_2246.field_27161
         || b == class_2246.field_27162
         || b == class_2246.field_27163
         || b == class_2246.field_27164;
   }

   private static boolean interesting(class_2680 s) {
      return amethyst(s.method_26204()) || s.method_27852(class_2246.field_20421) || s.method_27852(class_2246.field_20422);
   }

   private void enqueue(int cx, int cz) {
      long k = WorldUtil.key(cx, cz);
      if (!this.scanned.contains(k) && this.queued.add(k)) {
         this.scanQueue.offer(k);
      }
   }

   private void enqueueLoaded() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (class_2818 c : WorldUtil.loadedChunks()) {
            this.enqueue(c.method_12004().field_9181, c.method_12004().field_9180);
         }
      }
   }

   @Override
   public void onTick() {
      if (this.trackedLevel != mc.field_1687) {
         this.reset();
         this.trackedLevel = mc.field_1687;
      }

      this.ticks++;
      this.ticksSinceCommit++;
      int sens = this.sensitivity.getInt();
      if (sens != this.cachedSensitivity) {
         this.cachedSensitivity = sens;
         this.geodes.clear();
         this.scanQueue.clear();
         this.queued.clear();
         this.scanned.clear();
         this.candidatesDirty = true;
         this.enqueueLoaded();
      }

      if (this.ticks % 100 == 1) {
         this.enqueueLoaded();
      }

      if (this.ticks % 240 == 0) {
         this.scanned.clear();
         this.enqueueLoaded();
      }

      long deadline = System.nanoTime() + 4000000L;

      for (int i = 0; i < 24 && !this.scanQueue.isEmpty(); i++) {
         this.scanNext();
         if (i > 0 && System.nanoTime() >= deadline) {
            break;
         }
      }

      if (this.ticks % 20 == 0 && this.geodes.keySet().removeIf(k -> WorldUtil.chunk(WorldUtil.keyX(k), WorldUtil.keyZ(k)) == null)) {
         this.candidatesDirty = true;
      }

      this.commitCandidates();
   }

   private void scanNext() {
      Long key = this.scanQueue.poll();
      if (key != null) {
         this.queued.remove(key);
         this.scanned.add(key);
         int cx = WorldUtil.keyX(key);
         int cz = WorldUtil.keyZ(key);
         class_2818 chunk = WorldUtil.chunk(cx, cz);
         if (chunk == null) {
            this.unflag(key);
         } else {
            class_2826[] sections = chunk.method_12006();
            int minSection = chunk.method_32891();
            int bx = chunk.method_12004().method_8326();
            int bz = chunk.method_12004().method_8328();
            Set<Long> stoneTouching = new HashSet<>();
            List<class_2338> cluster = new ArrayList<>();
            class_2339 m = new class_2339();
            int fullHives = 0;

            for (int i = 0; i < sections.length; i++) {
               class_2826 s = sections[i];
               if (s != null && !s.method_38292() && s.method_12265().method_19526(SusChunkFinderModule::interesting)) {
                  int by = minSection + i << 4;

                  for (int y = 0; y < 16; y++) {
                     for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                           class_2680 st = s.method_12254(x, y, z);
                           if (amethyst(st.method_26204())) {
                              class_2338 p = new class_2338(bx + x, by + y, bz + z);
                              if (cluster.size() < 400) {
                                 cluster.add(p);
                              }

                              for (class_2350 d : class_2350.values()) {
                                 m.method_10103(p.method_10263() + d.method_10148(), p.method_10264() + d.method_10164(), p.method_10260() + d.method_10165());
                                 if (mc.field_1687.method_8320(m).method_27852(class_2246.field_10340)) {
                                    stoneTouching.add(m.method_10063());
                                 }
                              }
                           } else if ((st.method_27852(class_2246.field_20421) || st.method_27852(class_2246.field_20422))
                              && (Integer)st.method_11654(class_4481.field_20420) >= 5) {
                              fullHives++;
                           }
                        }
                     }
                  }
               }
            }

            int sens = Math.max(1, this.sensitivity.getInt());
            int score = Math.max(stoneTouching.size(), fullHives >= 2 ? sens : 0);
            if (score >= sens) {
               SusChunkFinderModule.Flagged f = new SusChunkFinderModule.Flagged(cx, cz, score, List.copyOf(cluster));
               SusChunkFinderModule.Flagged old = this.geodes.put(key, f);
               if (!f.equals(old)) {
                  this.candidatesDirty = true;
               }
            } else {
               this.unflag(key);
            }
         }
      }
   }

   private void unflag(long key) {
      if (this.geodes.remove(key) != null) {
         this.candidatesDirty = true;
      }
   }

   private void commitCandidates() {
      int r = this.simulationDistance.getInt();
      boolean ready = this.scanQueue.isEmpty() || this.ticksSinceCommit >= 60;
      if (r != this.cachedSimRadius || this.candidatesDirty && ready) {
         List<SusChunkFinderModule.Flagged> sorted = new ArrayList<>(this.geodes.values());
         sorted.sort(Comparator.comparingInt(SusChunkFinderModule.Flagged::cx).thenComparingInt(SusChunkFinderModule.Flagged::cz));
         long[] keys = new long[sorted.size()];

         for (int i = 0; i < keys.length; i++) {
            keys[i] = WorldUtil.key(sorted.get(i).cx, sorted.get(i).cz);
         }

         this.candidates = ChunkSetBuilder.build(keys, r);
         this.cachedSimRadius = r;
         this.candidatesDirty = false;
         this.ticksSinceCommit = 0;
      }
   }

   @Override
   public void onRenderWorld() {
      if (!this.geodes.isEmpty()) {
         int a = this.alpha.getInt();
         if (a != 0) {
            this.candidatePlates.draw(this.candidates, a << 24 | 0xFF0000, null, 63.0, 0.05);
            if (this.markGeodeChunks.isOn() || this.renderClusters.isOn()) {
               for (SusChunkFinderModule.Flagged f : this.geodes.values()) {
                  double x = f.cx * 16.0;
                  double z = f.cz * 16.0;
                  if (this.markGeodeChunks.isOn()) {
                     Esp.box(x, 63.0, z, x + 16.0, 63.05, z + 16.0, 11822335, a / 3, 0);
                     Esp.box(x, 63.0, z, x + 16.0, 63.05, z + 16.0, 12811775, 0, a);
                  }

                  if (this.renderClusters.isOn()) {
                     for (class_2338 p : f.cluster) {
                        Esp.box(
                           p.method_10263(),
                           p.method_10264(),
                           p.method_10260(),
                           p.method_10263() + 1,
                           p.method_10264() + 1,
                           p.method_10260() + 1,
                           11822335,
                           a / 3,
                           0
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private record Flagged(int cx, int cz, int score, List<class_2338> cluster) {
   }
}
