package dev.byteplus.module.modules.basefinding;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.LightTileRenderer;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.util.ChunkScanner;
import dev.byteplus.util.WorldUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2804;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3562;
import net.minecraft.class_4076;
import net.minecraft.class_638;
import net.minecraft.class_6606;
import net.minecraft.class_2338.class_2339;

public class LightDebugModule extends Module {
   private static final int RADIUS = 32;
   private static final int LOAD_DELAY = 10;
   private static final int UPDATE_DELAY = 4;
   private final ColorSetting bright = this.add(new ColorSetting("Bright Color", -11745));
   private final ColorSetting dim = this.add(new ColorSetting("Dim Color", -12829626));
   private final Map<Long, int[]> tiles = new HashMap<>();
   private final Map<Long, Integer> scheduled = new HashMap<>();
   private final ChunkScanner scanner = new ChunkScanner(this::scan, 3L);
   private boolean dirty;
   private long lastUpload;
   private int ticks;
   private float[] buffer = new float[0];

   public LightDebugModule() {
      super("Light Debug", "Highlights light underground: yellow for a lot of light, grey for a little.", Category.BASEFINDING);
   }

   @Override
   protected void onEnable() {
      this.reset();
      if (mc.field_1687 != null) {
         for (class_2818 c : WorldUtil.loadedChunks()) {
            this.schedule(c.method_12004().field_9181, c.method_12004().field_9180, 0);
         }
      }
   }

   @Override
   protected void onDisable() {
      this.reset();
   }

   @Override
   public void onJoin() {
      this.reset();
   }

   private void reset() {
      this.tiles.clear();
      this.scheduled.clear();
      this.scanner.clear();
      LightTileRenderer.clear();
      this.dirty = false;
   }

   private void schedule(int cx, int cz, int delay) {
      long k = WorldUtil.key(cx, cz);
      int due = this.ticks + delay;
      this.scheduled.merge(k, due, Math::max);
   }

   @Override
   public void onChunkLoad(int cx, int cz) {
      this.schedule(cx, cz, 10);
   }

   @Override
   public void onLightUpdatePacket(int cx, int cz, class_6606 data) {
      this.schedule(cx, cz, 4);
   }

   @Override
   public void onBlockUpdate(class_2338 pos, class_2680 state) {
      int cx = pos.method_10263() >> 4;
      int cz = pos.method_10260() >> 4;
      int lx = pos.method_10263() & 15;
      int lz = pos.method_10260() & 15;
      this.schedule(cx, cz, 4);
      if (lx < 15) {
         this.schedule(cx - 1, cz, 4);
      }

      if (lx > 0) {
         this.schedule(cx + 1, cz, 4);
      }

      if (lz < 15) {
         this.schedule(cx, cz - 1, 4);
      }

      if (lz > 0) {
         this.schedule(cx, cz + 1, 4);
      }
   }

   @Override
   public void onTick() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.ticks++;
         class_1923 me = mc.field_1724.method_31476();
         if (this.ticks % 40 == 0) {
            Iterator<Entry<Long, int[]>> it = this.tiles.entrySet().iterator();

            while (it.hasNext()) {
               long k = it.next().getKey();
               int cx = WorldUtil.keyX(k);
               int cz = WorldUtil.keyZ(k);
               if (!inRange(cx, cz, me) || WorldUtil.chunk(cx, cz) == null) {
                  it.remove();
                  this.dirty = true;
               }
            }

            for (class_2818 c : WorldUtil.loadedChunks()) {
               long k = WorldUtil.key(c.method_12004().field_9181, c.method_12004().field_9180);
               if (!this.tiles.containsKey(k) && !this.scheduled.containsKey(k) && inRange(c.method_12004().field_9181, c.method_12004().field_9180, me)) {
                  this.schedule(c.method_12004().field_9181, c.method_12004().field_9180, 0);
               }
            }
         }

         Iterator<Entry<Long, Integer>> it = this.scheduled.entrySet().iterator();

         while (it.hasNext()) {
            Entry<Long, Integer> e = it.next();
            if (e.getValue() <= this.ticks) {
               this.scanner.enqueue(WorldUtil.keyX(e.getKey()), WorldUtil.keyZ(e.getKey()));
               it.remove();
            }
         }

         this.scanner.tick();
      }
   }

   private static boolean inRange(int cx, int cz, class_1923 me) {
      return Math.max(Math.abs(cx - me.field_9181), Math.abs(cz - me.field_9180)) <= 32;
   }

   private void scan(class_2818 chunk) {
      class_638 level = mc.field_1687;
      if (level != null && mc.field_1724 != null) {
         class_1923 cp = chunk.method_12004();
         long key = WorldUtil.key(cp.field_9181, cp.field_9180);
         if (!inRange(cp.field_9181, cp.field_9180, mc.field_1724.method_31476())) {
            if (this.tiles.remove(key) != null) {
               this.dirty = true;
            }
         } else {
            class_3562 blockLight = level.method_22336().method_15562(class_1944.field_9282);
            class_3562 skyLight = level.method_22336().method_15562(class_1944.field_9284);
            boolean hasSky = level.method_8597().comp_642();
            class_2826[] sections = chunk.method_12006();
            int minSection = chunk.method_32891();
            int minY = chunk.method_31607();
            class_2339 m = new class_2339();
            int[] out = new int[256];
            int n = 0;

            for (int i = 0; i < sections.length; i++) {
               int sy = minSection + i;
               class_2804 light = blockLight.method_15544(class_4076.method_18676(cp.field_9181, sy, cp.field_9180));
               if (light != null && !light.method_51379(0)) {
                  class_2826 s = sections[i];
                  class_2826 under = i > 0 ? sections[i - 1] : null;

                  for (int y = 0; y < 16; y++) {
                     for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                           int lvl = light.method_12139(x, y, z);
                           if (lvl != 0) {
                              class_2680 st = s == null ? null : s.method_12254(x, y, z);
                              if (st == null || !st.method_51366() && st.method_26227().method_15769()) {
                                 class_2680 floor;
                                 if (y > 0) {
                                    floor = s == null ? null : s.method_12254(x, y - 1, z);
                                 } else {
                                    floor = under == null ? null : under.method_12254(x, 15, z);
                                 }

                                 if (floor != null && floor.method_51366()) {
                                    int wy = (sy << 4) + y;
                                    if (!hasSky || skyLight.method_15543(m.method_10103(cp.method_8326() + x, wy, cp.method_8328() + z)) <= 0) {
                                       if (n == out.length) {
                                          out = Arrays.copyOf(out, n * 2);
                                       }

                                       out[n++] = x | z << 4 | lvl << 8 | wy - minY << 12;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (n == 0) {
               if (this.tiles.remove(key) != null) {
                  this.dirty = true;
               }
            } else {
               this.tiles.put(key, Arrays.copyOf(out, n));
               this.dirty = true;
            }
         }
      }
   }

   @Override
   public void onRenderWorld() {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         if (this.dirty && now - this.lastUpload > 200L) {
            this.dirty = false;
            this.lastUpload = now;
            this.upload();
         }

         LightTileRenderer.draw(this.bright.rgb(), this.dim.rgb());
      }
   }

   private void upload() {
      int total = 0;

      for (int[] t : this.tiles.values()) {
         total += t.length;
      }

      if (this.buffer.length < total * 4) {
         this.buffer = new float[Math.max(total * 4, this.buffer.length * 3 / 2)];
      }

      class_1923 me = mc.field_1724.method_31476();
      int ax = me.method_8326();
      int az = me.method_8328();
      int ay = (int)Math.floor(mc.field_1724.method_23318());
      int minY = mc.field_1687.method_31607();
      LongOpenHashSet lit = new LongOpenHashSet(total * 2);

      for (Entry<Long, int[]> e : this.tiles.entrySet()) {
         int bx = WorldUtil.keyX(e.getKey()) << 4;
         int bz = WorldUtil.keyZ(e.getKey()) << 4;

         for (int p : e.getValue()) {
            lit.add(class_2338.method_10064(bx + (p & 15), minY + (p >>> 12), bz + (p >> 4 & 15)));
         }
      }

      int i = 0;

      for (Entry<Long, int[]> e : this.tiles.entrySet()) {
         int bx = WorldUtil.keyX(e.getKey()) << 4;
         int bz = WorldUtil.keyZ(e.getKey()) << 4;

         for (int p : e.getValue()) {
            int x = bx + (p & 15);
            int y = minY + (p >>> 12);
            int z = bz + (p >> 4 & 15);
            int mask = 0;
            if (lit.contains(class_2338.method_10064(x - 1, y, z))) {
               mask |= 1;
            }

            if (lit.contains(class_2338.method_10064(x + 1, y, z))) {
               mask |= 2;
            }

            if (lit.contains(class_2338.method_10064(x, y, z - 1))) {
               mask |= 16;
            }

            if (lit.contains(class_2338.method_10064(x, y, z + 1))) {
               mask |= 32;
            }

            this.buffer[i++] = x - ax;
            this.buffer[i++] = y - ay;
            this.buffer[i++] = z - az;
            this.buffer[i++] = (p >> 8 & 15) + mask * 16;
         }
      }

      LightTileRenderer.upload(this.buffer, total, ax, ay, az);
   }
}
