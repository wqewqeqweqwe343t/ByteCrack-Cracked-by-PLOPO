package dev.byteplus.util;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.awt.Color;

public final class ChunkCandidates {
   private ChunkCandidates() {
   }

   public static LongOpenHashSet candidates(Long2IntMap long2IntMap, Long2ObjectMap<Stacks> long2ObjectMap) {
      LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
      LongOpenHashSet longOpenHashSet2 = new LongOpenHashSet();
      LongArrayFIFOQueue longArrayFIFOQueue = new LongArrayFIFOQueue();
      LongIterator longIterator = long2IntMap.keySet().iterator();

      while (longIterator.hasNext()) {
         long l = longIterator.next();
         if (long2IntMap.get(l) > 0 && longOpenHashSet.add(l)) {
            longArrayFIFOQueue.enqueue(l);
            boolean bl = false;
            long l2 = 0L;

            while (!longArrayFIFOQueue.isEmpty()) {
               long l3 = longArrayFIFOQueue.dequeueLong();
               Stacks stacks = (Stacks)long2ObjectMap.get(l3);
               if (stacks != null && stacks.stacks() > 0 && (!bl || better(l3, stacks, l2, (Stacks)long2ObjectMap.get(l2), long2IntMap))) {
                  l2 = l3;
                  bl = true;
               }

               int n = (int)l3;
               int n2 = (int)(l3 >>> 32);
               visit(long2IntMap, longOpenHashSet, longArrayFIFOQueue, pack(n - 1, n2));
               visit(long2IntMap, longOpenHashSet, longArrayFIFOQueue, pack(n + 1, n2));
               visit(long2IntMap, longOpenHashSet, longArrayFIFOQueue, pack(n, n2 - 1));
               visit(long2IntMap, longOpenHashSet, longArrayFIFOQueue, pack(n, n2 + 1));
            }

            if (bl) {
               longOpenHashSet2.add(l2);
            }
         }
      }

      return longOpenHashSet2;
   }

   private static boolean better(long l, Stacks stacks, long l2, Stacks stacks2, Long2IntMap long2IntMap) {
      if (stacks.stacks() != stacks2.stacks()) {
         return stacks.stacks() > stacks2.stacks();
      }

      if (stacks.height() != stacks2.height()) {
         return stacks.height() > stacks2.height();
      }

      if (long2IntMap.get(l) != long2IntMap.get(l2)) {
         return long2IntMap.get(l) > long2IntMap.get(l2);
      }

      int n = (int)(l >>> 32);
      int n2 = (int)(l2 >>> 32);
      return n != n2 ? n < n2 : (int)l < (int)l2;
   }

   private static void visit(Long2IntMap long2IntMap, LongSet longSet, LongArrayFIFOQueue longArrayFIFOQueue, long l) {
      if (long2IntMap.get(l) > 0 && longSet.add(l)) {
         longArrayFIFOQueue.enqueue(l);
      }
   }

   public static long pack(int n, int n2) {
      return n & 4294967295L | (n2 & 4294967295L) << 32;
   }

   public static int rainbowRgb(long l) {
      float f = (float)Math.floorMod(l / 50000000L, 240L) / 240.0F;
      return Color.HSBtoRGB(f, 0.72F, 1.0F) & 16777215;
   }
}
