package dev.byteplus.module.modules.basefinding;

import dev.byteplus.hud.Notifications;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.CachedThickness;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.ChunkCandidates;
import dev.byteplus.util.Stacks;
import dev.byteplus.util.WorldUtil2;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1109;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_634;
import net.minecraft.class_638;

@Environment(EnvType.CLIENT)
public final class ChunkFinderModule extends Module {
   private static final int PROBE_CHUNKS = 48;
   private static final byte OTHER = 0;
   private static final byte STONE = 1;
   private static final byte BLOB = 2;
   private static final byte AIR = 3;
   private static final byte MISSING = 4;
   private static int[] SIDE_X = new int[]{1, -1, 0, 0};
   private static int[] SIDE_Z = new int[]{0, 0, 1, -1};
   private static int[] CORNER_X = new int[]{1, 1, -1, -1};
   private static int[] CORNER_Z = new int[]{1, -1, 1, -1};
   private static final int MIN_Y = 0;
   private static final int MAX_Y = 32;
   private static final int CHUNK_COLOR = 1627389951;
   private static final int OLD_CHUNK_COLOR = 1614011733;
   private static final double PLATE_Y = 63.0;
   private static final double PLATE_THICKNESS = 0.05;
   private final ModeSetting detection = new ModeSetting("Detection", "Calm", "Calm", "Aggressive");
   private final NumberSetting spreadRadius = new NumberSetting("Spread Radius", 1.0, 0.0, 16.0, 1.0);
   private final NumberSetting alpha = new NumberSetting("Alpha", 96.0, 0.0, 255.0, 1.0);
   private final ColorSetting chunkColor = this.add(new ColorSetting("Chunk Color", 1627389951));
   private final BooleanSetting outline = new BooleanSetting("Outline", true);
   private final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);
   private final BooleanSetting baseMarker = new BooleanSetting("Base Marker", true);
   private final BooleanSetting sound = new BooleanSetting("Sound", true);
   private final BooleanSetting notifications;
   private final CachedThickness plates = new CachedThickness();
   private final CachedThickness basePlates = new CachedThickness();
   private LongOpenHashSet baseChunks = new LongOpenHashSet();
   private final LongOpenHashSet coloredChunks = new LongOpenHashSet();
   private LongOpenHashSet visualSource;
   private boolean visualMarkerEnabled;
   private static Height UNDECIDED = new Height(0L, 0, new long[0]);
   private final Long2ObjectOpenHashMap<Height> flagged = new Long2ObjectOpenHashMap();
   private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
   private final LongOpenHashSet queued = new LongOpenHashSet();
   private final List<Height> fresh = new ArrayList<>();
   private final Long2IntOpenHashMap heat = new Long2IntOpenHashMap();
   private int appliedRadius;
   private LongOpenHashSet shown = new LongOpenHashSet();
   private boolean shownDirty = true;
   private int shownAge;
   private class_638 lastLevel;
   private boolean scanning;
   private StackHeight appliedConfig;
   private WeakReference<class_634> probeServer = new WeakReference<>(null);
   private boolean probing;
   private final LongOpenHashSet probed = new LongOpenHashSet();
   private final class_2818[] around = new class_2818[9];
   private final int[] groupX = new int[4];
   private final int[] groupZ = new int[4];
   private int groupSize;
   private boolean guessed;
   private class_2248 wall;
   private class_2248 lastBlock;
   private final int[] stackTop = new int[256];
   private final int[] stackShape = new int[256];
   private final int[] stackAreas = new int[256];
   private final boolean[] stackGuessed = new boolean[256];
   private final class_2248[] stackWall = new class_2248[256];
   private boolean sawBlob;
   private int scanX;
   private int scanZ;
   private int keepHeight;
   private long[] keptStacks = new long[16];
   private int keptStackCount;
   private int tallest;
   private long bottom;
   private boolean unsure;

   public ChunkFinderModule() {
      super("Chunk Finder", "Detects suspicious underground shafts and potential base locations.", Category.BASEFINDING);
      BooleanSetting macroBoolSetting = this.notifications = new BooleanSetting("Notifications", true);
      this.sound.visibleWhen(macroBoolSetting::get);
   }

   private boolean notificationsOn() {
      return this.notifications.isOn();
   }

   private void N(class_638 class_6382) {
      this.baseChunks.clear();
      this.coloredChunks.clear();
      this.visualSource = null;
      this.flagged.clear();
      this.heat.clear();
      this.appliedRadius = this.spreadRadius.getInt();
      this.queue.clear();
      this.queued.clear();
      this.fresh.clear();
      this.shown = new LongOpenHashSet();
      this.shownDirty = true;
      this.shownAge = 0;
      this.lastLevel = class_6382;
      boolean bl = this.scanning = class_6382 != null
         && class_6382.method_27983() != class_1937.field_25180
         && class_6382.method_27983() != class_1937.field_25181;
      if (this.scanning) {
         class_634 class_6342 = class_310.method_1551().method_1562();
         if (class_6342 != this.probeServer.get()) {
            this.probeServer = new WeakReference<>(class_6342);
            this.probing = true;
            this.probed.clear();
         }

         this.appliedConfig = this.v(class_6382);
         this.j(class_6382);
      }
   }

   private LongOpenHashSet U(class_638 class_6382) {
      Long2IntOpenHashMap long2IntOpenHashMap = new Long2IntOpenHashMap();
      ObjectIterator stacksMap = Long2IntMaps.fastIterable(this.heat).iterator();

      while (stacksMap.hasNext()) {
         Entry entry2 = (Entry)stacksMap.next();
         long l = entry2.getLongKey();
         if (WorldUtil2.isLoaded(class_6382, class_1923.method_8325(l), class_1923.method_8332(l))) {
            long2IntOpenHashMap.put(l, entry2.getIntValue());
         }
      }

      Long2ObjectOpenHashMap<Stacks> stacksMapx = new Long2ObjectOpenHashMap();
      ObjectIterator var8 = this.flagged.long2ObjectEntrySet().iterator();

      while (var8.hasNext()) {
         it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry entry3 = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry)var8.next();
         Height height = (Height)entry3.getValue();
         if (long2IntOpenHashMap.containsKey(entry3.getLongKey()) && height != null && height != UNDECIDED) {
            stacksMapx.put(entry3.getLongKey(), new Stacks(height.stacks().length, height.height()));
         }
      }

      this.baseChunks = ChunkCandidates.candidates(long2IntOpenHashMap, stacksMapx);
      return this.appliedRadius == 0 ? new LongOpenHashSet(long2IntOpenHashMap.keySet()) : e(long2IntOpenHashMap);
   }

   @Override
   public void onRenderWorld() {
      if (class_310.method_1551().field_1687 != null) {
         int n = this.alpha.getInt();
         if (n > 0) {
            boolean bl = this.baseMarker.get();
            if (this.visualSource != this.shown || this.visualMarkerEnabled != bl) {
               this.coloredChunks.clear();
               this.coloredChunks.addAll(this.shown);
               if (bl) {
                  this.coloredChunks.removeAll(this.baseChunks);
               }

               this.visualSource = this.shown;
               this.visualMarkerEnabled = bl;
            }

            int n2 = this.rainbow.get() ? ChunkCandidates.rainbowRgb(System.nanoTime()) : this.chunkColor.argb() & 16777215;
            Color color = this.outline.get() ? new Color(Math.min(220, n + 45) << 24 | n2, true) : null;
            this.plates.draw(this.coloredChunks, new Color(n << 24 | n2, true).getRGB(), color == null ? null : color.getRGB(), 63.0, 0.05);
            if (bl) {
               Color color2 = this.outline.get() ? new Color(255, 92, 92, Math.min(220, n + 45)) : null;
               this.basePlates.draw(this.baseChunks, new Color(255, 48, 55, n).getRGB(), color2 == null ? null : color2.getRGB(), 63.0, 0.05);
            }
         }
      }
   }

   @Override
   public void onEnable() {
      this.N(class_310.method_1551().field_1687);
   }

   @Override
   public void onDisable() {
      this.N(null);
   }

   private StackHeight v(class_638 class_6382) {
      return new StackHeight(Math.max(0, class_6382.method_31607()), Math.min(32, class_6382.method_31600()), this.detection.is("Aggressive") ? 3 : 2);
   }

   private void j(class_638 class_6382) {
      class_310 class_3102 = class_310.method_1551();
      if (class_3102.field_1724 != null) {
         int n = (Integer)class_3102.field_1690.method_42503().method_41753() + 1;
         class_1923 class_19232 = class_3102.field_1724.method_31476();

         for (int i = class_19232.field_9181 - n; i <= class_19232.field_9181 + n; i++) {
            for (int j = class_19232.field_9180 - n; j <= class_19232.field_9180 + n; j++) {
               if (WorldUtil2.isLoaded(class_6382, i, j)) {
                  this.u(class_1923.method_8331(i, j));
               }
            }
         }
      }
   }

   private void u(long l) {
      if (this.scanning && this.queued.add(l)) {
         this.queue.enqueue(l);
      }
   }

   @Override
   public void onChunkLoad(int cx, int cz) {
      this.shownDirty = true;
      int n = cx;
      int n2 = cz;

      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            this.u(class_1923.method_8331(n + i, n2 + j));
         }
      }
   }

   @Override
   public void onBlockChange(class_2338 pos, class_2680 oldState, class_2680 newState) {
      int n;
      if (this.scanning
         && pos != null
         && (n = pos.method_10264()) >= this.appliedConfig.minY() - 1
         && n <= this.appliedConfig.maxY() + 1
         && (O(oldState) || O(newState))) {
         int n2 = pos.method_10263();
         int n3 = pos.method_10260();

         for (int i = n2 - 2 >> 4; i <= n2 + 1 >> 4; i++) {
            for (int j = n3 - 2 >> 4; j <= n3 + 2 >> 4; j++) {
               this.u(class_1923.method_8331(i, j));
            }
         }
      }
   }

   @Override
   public void onTickAlways() {
      class_638 class_6382 = class_310.method_1551().field_1687;
      if (class_6382 != this.lastLevel) {
         this.N(class_6382);
      }

      if (this.scanning) {
         StackHeight stackHeight = this.v(class_6382);
         if (!stackHeight.equals(this.appliedConfig)) {
            this.appliedConfig = stackHeight;
            this.j(class_6382);
         }

         int n;
         if ((n = this.spreadRadius.getInt()) != this.appliedRadius) {
            this.appliedRadius = n;
            this.shownDirty = true;
            this.heat.clear();
            ObjectIterator objectIterator = this.flagged.long2ObjectEntrySet().fastIterator();

            while (objectIterator.hasNext()) {
               it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry entry = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry)objectIterator.next();
               this.I(entry.getLongKey(), (Height)entry.getValue(), 1);
            }
         }

         long l = System.nanoTime() + 3000000L;

         while (!this.queue.isEmpty() && System.nanoTime() < l) {
            long l2 = this.queue.dequeueLong();
            this.queued.remove(l2);
            int n3 = class_1923.method_8325(l2);
            int n2;
            if (WorldUtil2.isLoaded(class_6382, n3, n2 = class_1923.method_8332(l2))) {
               Height height = this.t(class_6382, n3, n2, stackHeight);
               if (this.sawBlob) {
                  this.X(l2);
               }

               if (height != UNDECIDED) {
                  Height height2 = (Height)this.flagged.get(l2);
                  if (height == null ? height2 != null : height2 == null || !height2.sameAs(height)) {
                     this.d(l2, height2, height);
                     if (height2 == null) {
                        this.fresh.add(height);
                     }
                  }
               }
            }
         }

         this.K(this.fresh);
         this.fresh.clear();
         if (this.shownDirty || ++this.shownAge >= 20) {
            this.shown = this.U(class_6382);
            this.shownDirty = false;
            this.shownAge = 0;
         }
      }
   }

   private void d(long l, Height height, Height height2) {
      this.shownDirty = true;
      if (height != null) {
         this.I(l, height, -1);
      }

      if (height2 == null) {
         this.flagged.remove(l);
      } else {
         this.flagged.put(l, height2);
         this.I(l, height2, 1);
      }
   }

   private void I(long l, Height height, int n) {
      int n2 = n * height.stacks().length;
      int n3 = this.appliedRadius;
      int n4 = class_1923.method_8325(l);
      int n5 = class_1923.method_8332(l);

      for (int i = n4 - n3; i <= n4 + n3; i++) {
         for (int j = n5 - n3; j <= n5 + n3; j++) {
            long l2 = class_1923.method_8331(i, j);
            if (this.heat.addTo(l2, n2) + n2 == 0) {
               this.heat.remove(l2);
            }
         }
      }
   }

   private static LongOpenHashSet e(Long2IntOpenHashMap long2IntOpenHashMap) {
      LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
      LongOpenHashSet longOpenHashSet2 = new LongOpenHashSet();
      LongArrayList longArrayList = new LongArrayList();
      LongArrayFIFOQueue longArrayFIFOQueue = new LongArrayFIFOQueue();
      LongIterator longIterator = long2IntOpenHashMap.keySet().iterator();

      while (longIterator.hasNext()) {
         long l = longIterator.next();
         if (longOpenHashSet2.add(l)) {
            longArrayList.clear();
            longArrayFIFOQueue.enqueue(l);
            int n = 0;

            while (!longArrayFIFOQueue.isEmpty()) {
               long l2 = longArrayFIFOQueue.dequeueLong();
               longArrayList.add(l2);
               n = Math.max(n, long2IntOpenHashMap.get(l2));
               int n2 = class_1923.method_8325(l2);
               int n3 = class_1923.method_8332(l2);

               for (int i = 0; i < 4; i++) {
                  long l3 = class_1923.method_8331(n2 + SIDE_X[i], n3 + SIDE_Z[i]);
                  if (long2IntOpenHashMap.containsKey(l3) && longOpenHashSet2.add(l3)) {
                     longArrayFIFOQueue.enqueue(l3);
                  }
               }
            }

            for (int ix = 0; ix < longArrayList.size(); ix++) {
               if (long2IntOpenHashMap.get(longArrayList.getLong(ix)) == n) {
                  longOpenHashSet.add(longArrayList.getLong(ix));
               }
            }
         }
      }

      return longOpenHashSet;
   }

   private Height t(class_638 class_6382, int n, int n2, StackHeight stackHeight) {
      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            this.around[(i + 1) * 3 + j + 1] = class_6382.method_2935().method_21730(n + i, n2 + j);
         }
      }

      this.sawBlob = false;
      this.scanX = n << 4;
      this.scanZ = n2 << 4;
      this.keepHeight = stackHeight.stackHeight();
      this.keptStackCount = 0;
      this.tallest = 0;
      this.bottom = 0L;
      this.unsure = false;

      Height var29;
      try {
         class_2818 class_28182 = this.around[4];
         class_2826[] class_2826Array = class_28182.method_12006();
         int n4 = class_28182.method_32891();
         Arrays.fill(this.stackTop, Integer.MIN_VALUE);
         boolean bl = true;

         for (int n3 = 0; n3 < class_2826Array.length; n3++) {
            class_2826 class_28262 = class_2826Array[n3];
            int n5;
            if (class_28262 != null && !class_28262.method_38292() && (n5 = n4 + n3 << 4) + 15 >= stackHeight.minY() && n5 <= stackHeight.maxY()) {
               int n6 = Math.max(stackHeight.minY() - n5, 0);
               int n7 = Math.min(stackHeight.maxY() - n5, 15);
               if (bl) {
                  bl = !m(class_28262, n6, n7);
               }

               if (class_28262.method_19523(ChunkFinderModule::lmbd$scan$0) && class_28262.method_19523(ChunkFinderModule::lmbd$scan$1)) {
                  for (int i = n6; i <= n7; i++) {
                     for (int j = 0; j < 16; j++) {
                        boolean bl3 = false;

                        for (int k = 0; k < 16; k++) {
                           class_2248 class_22482 = class_28262.method_12254(k, i, j).method_26204();
                           boolean bl4 = class_22482 == class_2246.field_10340;
                           if (bl4) {
                              if (!bl3 && this.P(k, n5 + i, j)) {
                                 this.p(k, n5 + i, j);
                              }
                           } else if (this.probing && g(class_22482)) {
                              this.sawBlob = true;
                              if (this.q(k, n5 + i, j)) {
                                 this.probing = false;
                                 this.probed.clear();
                              }
                           }

                           bl3 = bl4;
                        }
                     }
                  }
               }
            }
         }

         for (int var25 = 0; var25 < 256; var25++) {
            this.Q(var25);
         }

         if (this.tallest < stackHeight.stackHeight()) {
            return !this.unsure && !bl ? null : UNDECIDED;
         }

         Height height = new Height(this.bottom, this.tallest, Arrays.copyOf(this.keptStacks, this.keptStackCount));
         var29 = height;
      } finally {
         Arrays.fill(this.around, null);
      }

      return var29;
   }

   private void p(int n, int n2, int n3) {
      int n4 = n3 << 4 | n;
      int n5 = this.B(n, n3);
      boolean bl = this.stackTop[n4] == n2 - 1 && this.stackShape[n4] == n5 && this.stackWall[n4] == this.wall;
      if (!bl) {
         this.Q(n4);
      }

      this.stackTop[n4] = n2;
      this.stackShape[n4] = n5;
      this.stackWall[n4] = this.wall;
      this.stackAreas[n4] = bl ? this.stackAreas[n4] + 1 : 1;
      this.stackGuessed[n4] = this.guessed || bl && this.stackGuessed[n4];
   }

   private void Q(int n) {
      int n3 = this.stackAreas[n];
      int n4 = this.stackTop[n];
      int n2;
      if (n4 != Integer.MIN_VALUE && n3 >= this.keepHeight && this.J(n & 15, n4 + 1, n >> 4, n2 = this.stackShape[n]) && this.J(n & 15, n4 - n3, n >> 4, n2)) {
         if (this.stackGuessed[n]) {
            this.unsure = true;
         } else {
            int n5 = this.scanX + (n & 15);
            int n6 = this.scanZ + (n >> 4);
            long l = class_2338.method_10064(n5, n4 - n3 + 1, n6);
            if (n3 > this.tallest || n3 == this.tallest && b(l, this.bottom)) {
               this.tallest = n3;
               this.bottom = l;
            }

            if (this.keptStackCount == this.keptStacks.length) {
               this.keptStacks = Arrays.copyOf(this.keptStacks, this.keptStacks.length * 2);
            }

            this.keptStacks[this.keptStackCount++] = l;
         }
      }
   }

   private boolean J(int n, int n2, int n3, int n4) {
      for (int i = 0; i < 6; i++) {
         if ((n4 >> i & 1) != 0) {
            this.i(n + i / 3, n2, n3 + i % 3 - 1);
            if (!f(this.lastBlock)) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean b(long l, long l2) {
      if (class_2338.method_10071(l) != class_2338.method_10071(l2)) {
         return class_2338.method_10071(l) < class_2338.method_10071(l2);
      } else {
         return class_2338.method_10083(l) != class_2338.method_10083(l2)
            ? class_2338.method_10083(l) < class_2338.method_10083(l2)
            : class_2338.method_10061(l) < class_2338.method_10061(l2);
      }
   }

   private int B(int n, int n2) {
      int n3 = 0;

      for (int i = 0; i < this.groupSize; i++) {
         n3 |= 1 << (this.groupX[i] - n) * 3 + this.groupZ[i] - n2 + 1;
      }

      return n3;
   }

   private boolean P(int n, int n2, int n3) {
      this.guessed = false;
      this.wall = null;
      if (this.F(n - 1, n2, n3) == 2 && this.w() && this.F(n, n2, n3 - 1) == 2 && this.w()) {
         this.groupX[0] = n;
         this.groupZ[0] = n3;
         int n8 = 1;
         int n9 = n;
         int n10 = n3;
         int n11 = n3;

         for (int n7 = 0; n7 < n8; n7++) {
            for (int n6 = 0; n6 < 4; n6++) {
               int n5 = this.groupX[n7] + SIDE_X[n6];
               int n4 = this.groupZ[n7] + SIDE_Z[n6];
               if (!this.E(n5, n4, n8)) {
                  byte by = this.F(n5, n2, n4);
                  if (by == 2) {
                     if (!this.w()) {
                        return false;
                     }
                  } else {
                     if (by != 1) {
                        return false;
                     }

                     if (n5 < n || n5 == n && n4 < n3) {
                        return false;
                     }

                     n9 = Math.max(n9, n5);
                     n10 = Math.min(n10, n4);
                     n11 = Math.max(n11, n4);
                     if (n9 - n > 1 || n11 - n10 > 1) {
                        return false;
                     }

                     this.groupX[n8] = n5;
                     this.groupZ[n8] = n4;
                     n8++;
                  }
               }
            }
         }

         for (int var16 = 0; var16 < n8; var16++) {
            for (int n6x = 0; n6x < 4; n6x++) {
               int n5 = this.groupX[var16] + CORNER_X[n6x];
               int n4 = this.groupZ[var16] + CORNER_Z[n6x];
               if (!this.E(n5, n4, n8) && (this.F(n5, n2, n4) != 2 || !this.w())) {
                  return false;
               }
            }
         }

         for (int var17 = 0; var17 < n8; var17++) {
            if (this.i(this.groupX[var17], n2 + 1, this.groupZ[var17]) == 3 || this.i(this.groupX[var17], n2 - 1, this.groupZ[var17]) == 3) {
               return false;
            }
         }

         this.groupSize = n8;
         return true;
      } else {
         return false;
      }
   }

   private boolean E(int n, int n2, int n3) {
      for (int i = 0; i < n3; i++) {
         if (this.groupX[i] == n && this.groupZ[i] == n2) {
            return true;
         }
      }

      return false;
   }

   private byte F(int n, int n2, int n3) {
      byte by = this.i(n, n2, n3);
      if (by != 4) {
         return by;
      }

      this.guessed = true;
      return 2;
   }

   private boolean w() {
      if (this.lastBlock == null) {
         return true;
      }

      if (this.wall == null) {
         this.wall = this.lastBlock;
      }

      return this.lastBlock == this.wall;
   }

   private byte i(int n, int n2, int n3) {
      class_2818 class_28182 = this.around[((n >> 4) + 1) * 3 + (n3 >> 4) + 1];
      this.lastBlock = null;
      if (class_28182 == null) {
         return 4;
      } else {
         int n4 = class_28182.method_31602(n2);
         if (n4 >= 0 && n4 < class_28182.method_32890()) {
            class_2680 class_26802 = class_28182.method_38259(n4).method_12254(n & 15, n2 & 15, n3 & 15);
            this.lastBlock = class_26802.method_26204();
            return a(class_26802);
         } else {
            return 0;
         }
      }
   }

   private boolean q(int n, int n2, int n3) {
      if (!this.s(n + 1, n2, n3)) {
         return false;
      } else if (!this.s(n - 1, n2, n3)) {
         return false;
      } else if (!this.s(n, n2, n3 + 1)) {
         return false;
      } else if (!this.s(n, n2, n3 - 1)) {
         return false;
      } else {
         return !this.s(n, n2 + 1, n3) ? false : this.s(n, n2 - 1, n3);
      }
   }

   private boolean s(int n, int n2, int n3) {
      class_2818 class_28182 = this.around[((n >> 4) + 1) * 3 + (n3 >> 4) + 1];
      if (class_28182 == null) {
         return false;
      } else {
         int n4 = class_28182.method_31602(n2);
         if (n4 < 0) {
            return false;
         } else {
            return n4 >= class_28182.method_32890() ? false : class_28182.method_38259(n4).method_12254(n & 15, n2 & 15, n3 & 15).method_26216();
         }
      }
   }

   private static boolean m(class_2826 class_28262, int n, int n2) {
      if (!class_28262.method_19523(ChunkFinderModule::lmbd$hasBlocks$0)) {
         return false;
      }

      for (int i = n; i <= n2; i++) {
         for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
               if (!class_28262.method_12254(k, i, j).method_26215()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static byte a(class_2680 class_26802) {
      class_2248 class_22482 = class_26802.method_26204();
      if (class_22482 == class_2246.field_10340) {
         return 1;
      } else {
         return g(class_22482) ? 2 : (byte)(!class_26802.method_26215() ? 0 : 3);
      }
   }

   private static boolean g(class_2248 class_22482) {
      if (class_22482 == class_2246.field_10508) {
         return true;
      } else if (class_22482 == class_2246.field_10115) {
         return true;
      } else {
         return class_22482 == class_2246.field_10474 ? true : class_22482 == class_2246.field_10255;
      }
   }

   private static boolean f(class_2248 class_22482) {
      return class_22482 == class_2246.field_10340 ? true : class_22482 == class_2246.field_28888;
   }

   private static boolean O(class_2680 class_26802) {
      if (class_26802 == null) {
         return false;
      } else {
         return a(class_26802) != 0 ? true : f(class_26802.method_26204());
      }
   }

   private void X(long l) {
      if (this.probing && this.probed.add(l) && this.probed.size() >= 48) {
         this.probing = false;
         this.probed.clear();
         class_638 class_6382 = class_310.method_1551().field_1687;
         if (class_6382 != null && class_310.method_1551().field_1724 != null) {
            class_310.method_1551()
               .field_1724
               .method_7353(class_2561.method_43470("Byte+: This server hides ores with anti-xray; patched shafts may not be visible."), false);
         }
      }
   }

   private void K(List<Height> list) {
      if (this.notificationsOn() && !list.isEmpty()) {
         Height height = list.get(0);
         Notifications.chunkFlag(class_2338.method_10061(height.pos()), class_2338.method_10083(height.pos()));
         if (this.sound.get()) {
            class_310.method_1551().method_1483().method_4873(class_1109.method_4758((class_3414)class_3417.field_14622.comp_349(), 1.0F));
         }
      }
   }

   private static boolean lmbd$hasBlocks$0(class_2680 class_26802) {
      return !class_26802.method_26215();
   }

   private static boolean lmbd$scan$1(class_2680 class_26802) {
      return g(class_26802.method_26204());
   }

   private static boolean lmbd$scan$0(class_2680 class_26802) {
      return class_26802.method_27852(class_2246.field_10340);
   }
}
