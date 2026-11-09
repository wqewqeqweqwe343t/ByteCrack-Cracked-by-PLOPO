package dev.byteplus.util;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.class_2818;

public final class ChunkScanner {
   private final ArrayDeque<Long> queue = new ArrayDeque<>();
   private final Set<Long> queued = new HashSet<>();
   private final Consumer<class_2818> scan;
   private final long budgetNanos;

   public ChunkScanner(Consumer<class_2818> scan, long budgetMillis) {
      this.scan = scan;
      this.budgetNanos = budgetMillis * 1000000L;
   }

   public void enqueue(int cx, int cz) {
      long k = WorldUtil.key(cx, cz);
      if (this.queued.add(k)) {
         this.queue.add(k);
      }
   }

   public void enqueueAllLoaded() {
      for (class_2818 c : WorldUtil.loadedChunks()) {
         this.enqueue(c.method_12004().field_9181, c.method_12004().field_9180);
      }
   }

   public void clear() {
      this.queue.clear();
      this.queued.clear();
   }

   public boolean isIdle() {
      return this.queue.isEmpty();
   }

   public void tick() {
      long end = System.nanoTime() + this.budgetNanos;
      int done = 0;

      while (!this.queue.isEmpty()) {
         long k = this.queue.poll();
         this.queued.remove(k);
         class_2818 c = WorldUtil.chunk(WorldUtil.keyX(k), WorldUtil.keyZ(k));
         if (c != null) {
            this.scan.accept(c);
         }

         if (++done > 0 && System.nanoTime() > end) {
            break;
         }
      }
   }
}
