package dev.byteplus.module.modules.render;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.setting.BlockListSetting;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.InfoSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.ChunkScanner;
import dev.byteplus.util.WorldUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2338.class_2339;

public class BlockEspModule extends Module {
   public final BlockListSetting blocks = this.add(new BlockListSetting("Blocks", defaults()));
   private final NumberSetting alpha = this.add(new NumberSetting("Alpha", 90.0, 0.0, 255.0, 1.0));
   private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true));
   private final BooleanSetting tracers = this.add(new BooleanSetting("Tracers", false));
   private final InfoSetting found = this.add(new InfoSetting("Found", () -> this.total + " blocks"));
   private static final int MAX_TRACERS = 300;
   private final Map<Long, BlockEspModule.ChunkResult> results = new HashMap<>();
   private final Set<Long> scanned = new HashSet<>();
   private final ChunkScanner scanner = new ChunkScanner(this::scanChunk, 4L);
   private List<long[]> tracerList = new ArrayList<>();
   private int seenVersion = -1;
   private int total;
   private int ticks;

   public BlockEspModule() {
      super("Block ESP", "Highlights the blocks you pick through walls, in every loaded chunk.", Category.RENDER);
   }

   private static Map<class_2248, Integer> defaults() {
      Map<class_2248, Integer> m = new LinkedHashMap<>();
      m.put(class_2246.field_10442, 6087142);
      m.put(class_2246.field_29029, 6087142);
      m.put(class_2246.field_22109, 10506797);
      return m;
   }

   private void restart() {
      this.results.clear();
      this.scanned.clear();
      this.tracerList = new ArrayList<>();
      this.scanner.clear();
      this.scanner.enqueueAllLoaded();
      this.seenVersion = this.blocks.version();
   }

   @Override
   protected void onEnable() {
      this.restart();
   }

   @Override
   protected void onDisable() {
      this.results.clear();
      this.scanned.clear();
      this.tracerList = new ArrayList<>();
      this.scanner.clear();
      this.total = 0;
   }

   @Override
   public void onJoin() {
      this.results.clear();
      this.scanned.clear();
      this.scanner.clear();
   }

   @Override
   public void onChunkLoad(int cx, int cz) {
      this.scanner.enqueue(cx, cz);
      if (this.scanned.contains(WorldUtil.key(cx + 1, cz))) {
         this.scanner.enqueue(cx + 1, cz);
      }

      if (this.scanned.contains(WorldUtil.key(cx - 1, cz))) {
         this.scanner.enqueue(cx - 1, cz);
      }

      if (this.scanned.contains(WorldUtil.key(cx, cz + 1))) {
         this.scanner.enqueue(cx, cz + 1);
      }

      if (this.scanned.contains(WorldUtil.key(cx, cz - 1))) {
         this.scanner.enqueue(cx, cz - 1);
      }
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 state) {
      this.scanner.enqueue(pos.method_10263() >> 4, pos.method_10260() >> 4);
   }

   @Override
   public void onTick() {
      if (this.seenVersion != this.blocks.version()) {
         this.restart();
      }

      this.ticks++;
      if (this.ticks % 40 == 0) {
         this.scanned.removeIf(k -> WorldUtil.chunk(WorldUtil.keyX(k), WorldUtil.keyZ(k)) == null);
         this.results.keySet().removeIf(k -> !this.scanned.contains(k));

         for (class_2818 c : WorldUtil.loadedChunks()) {
            if (!this.scanned.contains(WorldUtil.key(c.method_12004().field_9181, c.method_12004().field_9180))) {
               this.scanner.enqueue(c.method_12004().field_9181, c.method_12004().field_9180);
            }
         }
      }

      this.scanner.tick();
      if (this.ticks % 10 == 0) {
         this.rebuildTracers();
      }
   }

   private void rebuildTracers() {
      int t = 0;
      List<long[]> all = new ArrayList<>();

      for (BlockEspModule.ChunkResult r : this.results.values()) {
         t += r.blocks.size();
         if (this.tracers.isOn()) {
            all.addAll(r.blocks);
         }
      }

      this.total = t;
      if (this.tracers.isOn() && !all.isEmpty()) {
         class_2338 p = mc.field_1724.method_24515();
         all.sort(Comparator.comparingDouble(e -> {
            double dx = class_2338.method_10061(e[0]) - p.method_10263();
            double dy = class_2338.method_10071(e[0]) - p.method_10264();
            double dz = class_2338.method_10083(e[0]) - p.method_10260();
            return dx * dx + dy * dy + dz * dz;
         }));
         this.tracerList = new ArrayList<>(all.subList(0, Math.min(300, all.size())));
      } else {
         this.tracerList = new ArrayList<>();
      }
   }

   private void scanChunk(class_2818 chunk) {
      long key = WorldUtil.key(chunk.method_12004().field_9181, chunk.method_12004().field_9180);
      this.scanned.add(key);
      Map<class_2248, Integer> sel = this.blocks.get();
      if (sel.isEmpty()) {
         this.results.remove(key);
      } else {
         Map<Long, Integer> colorAt = new HashMap<>();
         List<long[]> found = new ArrayList<>();
         class_2826[] sections = chunk.method_12006();
         int minSection = chunk.method_32891();
         int bx = chunk.method_12004().method_8326();
         int bz = chunk.method_12004().method_8328();

         for (int i = 0; i < sections.length; i++) {
            class_2826 s = sections[i];
            if (s != null && !s.method_38292() && s.method_19523(st -> sel.containsKey(st.method_26204()))) {
               int by = minSection + i << 4;

               for (int y = 0; y < 16; y++) {
                  for (int z = 0; z < 16; z++) {
                     for (int x = 0; x < 16; x++) {
                        Integer c = sel.get(s.method_12254(x, y, z).method_26204());
                        if (c != null) {
                           long packed = class_2338.method_10064(bx + x, by + y, bz + z);
                           colorAt.put(packed, c);
                           found.add(new long[]{packed, c.intValue()});
                        }
                     }
                  }
               }
            }
         }

         if (found.isEmpty()) {
            this.results.remove(key);
         } else {
            int[][] dirs = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            List<double[]> quads = new ArrayList<>();
            class_2339 m = new class_2339();

            for (long[] e : found) {
               int x = class_2338.method_10061(e[0]);
               int y = class_2338.method_10071(e[0]);
               int z = class_2338.method_10083(e[0]);
               int color = (int)e[1];

               for (int d = 0; d < 6; d++) {
                  int nx = x + dirs[d][0];
                  int ny = y + dirs[d][1];
                  int nz = z + dirs[d][2];
                  Integer nc;
                  if (nx >> 4 == chunk.method_12004().field_9181 && nz >> 4 == chunk.method_12004().field_9180) {
                     nc = colorAt.get(class_2338.method_10064(nx, ny, nz));
                  } else {
                     nc = sel.get(mc.field_1687.method_8320(m.method_10103(nx, ny, nz)).method_26204());
                  }

                  if (nc == null || nc != color) {
                     int axis = d / 2;
                     int plane = axis == 0 ? x : (axis == 1 ? y : z);
                     int u = axis == 0 ? z : x;
                     int v = axis == 1 ? z : y;
                     quads.add(face(d, plane, u, v, 1, 1, color));
                  }
               }
            }

            this.results.put(key, new BlockEspModule.ChunkResult(quads, found));
         }
      }
   }

   private static double[] face(int d, int plane, int u, int v, int w, int h, int color) {
      int axis = d / 2;
      int side = d % 2 == 0 ? 1 : -1;
      double p = side > 0 ? plane + 1 : plane;
      double u1 = u + w;
      double v1 = v + h;
      double[] q = new double[16];
      double[][] c = new double[4][];
      switch (axis) {
         case 0:
            c[0] = new double[]{p, v, u};
            c[1] = new double[]{p, v, u1};
            c[2] = new double[]{p, v1, u1};
            c[3] = new double[]{p, v1, u};
            break;
         case 1:
            c[0] = new double[]{u, p, v};
            c[1] = new double[]{u1, p, v};
            c[2] = new double[]{u1, p, v1};
            c[3] = new double[]{u, p, v1};
            break;
         default:
            c[0] = new double[]{u, v, p};
            c[1] = new double[]{u1, v, p};
            c[2] = new double[]{u1, v1, p};
            c[3] = new double[]{u, v1, p};
      }

      for (int i = 0; i < 4; i++) {
         System.arraycopy(c[i], 0, q, i * 3, 3);
      }

      q[12] = axis;
      q[13] = side;
      q[14] = p;
      q[15] = color;
      return q;
   }

   @Override
   public void onRenderWorld() {
      int a = this.alpha.getInt();
      int fillA = (int)(a * 0.7F);
      int lineA = this.outline.isOn() ? Math.min(255, a + 110) : 0;

      for (BlockEspModule.ChunkResult r : this.results.values()) {
         for (double[] q : r.quads) {
            Esp.quad(q, (int)q[15], fillA, lineA);
         }
      }

      for (long[] e : this.tracerList) {
         Esp.tracer(class_2338.method_10061(e[0]) + 0.5, class_2338.method_10071(e[0]) + 0.5, class_2338.method_10083(e[0]) + 0.5, (int)e[1], 220);
      }
   }

   private record ChunkResult(List<double[]> quads, List<long[]> blocks) {
   }
}
