package dev.byteplus.util;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import net.minecraft.class_1923;

public final class ChunkSetBuilder {
   private ChunkSetBuilder() {
   }

   public static LongOpenHashSet build(long[] chunks, int simDistance) {
      LongOpenHashSet out = new LongOpenHashSet();
      int count = chunks.length;
      int linkRange = Math.max(2, simDistance * 2);
      int cellSize = linkRange + 1;
      boolean[] visited = new boolean[count];
      Long2ObjectOpenHashMap<IntArrayList> grid = new Long2ObjectOpenHashMap();

      for (int i = 0; i < count; i++) {
         long cell = class_1923.method_8331(
            Math.floorDiv(class_1923.method_8325(chunks[i]), cellSize), Math.floorDiv(class_1923.method_8332(chunks[i]), cellSize)
         );
         ((IntArrayList)grid.computeIfAbsent(cell, kx -> new IntArrayList())).add(i);
      }

      IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
      IntArrayList group = new IntArrayList();
      IntArrayList neighbours = new IntArrayList();

      for (int i = 0; i < count; i++) {
         if (!visited[i]) {
            group.clear();
            queue.enqueue(i);
            visited[i] = true;

            while (!queue.isEmpty()) {
               int cur = queue.dequeueInt();
               group.add(cur);
               int x = class_1923.method_8325(chunks[cur]);
               int z = class_1923.method_8332(chunks[cur]);
               int gx = Math.floorDiv(x, cellSize);
               int gz = Math.floorDiv(z, cellSize);
               neighbours.clear();

               for (int dx = -1; dx <= 1; dx++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     IntArrayList bucket = (IntArrayList)grid.get(class_1923.method_8331(gx + dx, gz + dz));
                     if (bucket != null) {
                        for (int j = 0; j < bucket.size(); j++) {
                           int other = bucket.getInt(j);
                           if (!visited[other]
                              && Math.max(Math.abs(x - class_1923.method_8325(chunks[other])), Math.abs(z - class_1923.method_8332(chunks[other])))
                                 <= linkRange) {
                              neighbours.add(other);
                           }
                        }
                     }
                  }
               }

               neighbours.sort(null);

               for (int k = 0; k < neighbours.size(); k++) {
                  int n = neighbours.getInt(k);
                  visited[n] = true;
                  queue.enqueue(n);
               }
            }

            long[] members = new long[group.size()];

            for (int k = 0; k < members.length; k++) {
               members[k] = chunks[group.getInt(k)];
            }

            connected(out, members);
         }
      }

      return out;
   }

   private static void connected(LongOpenHashSet out, long[] pts) {
      LongOpenHashSet tree = new LongOpenHashSet(pts);
      int n = pts.length;
      if (n < 2) {
         out.addAll(tree);
      } else {
         int[] best = new int[n];
         int[] parent = new int[n];
         boolean[] inTree = new boolean[n];
         Arrays.fill(best, Integer.MAX_VALUE);
         Arrays.fill(parent, -1);
         inTree[0] = true;
         int last = 0;

         for (int i = 1; i < n; i++) {
            int pick = -1;

            for (int j = 0; j < n; j++) {
               if (!inTree[j]) {
                  int dx = class_1923.method_8325(pts[last]) - class_1923.method_8325(pts[j]);
                  int dz = class_1923.method_8332(pts[last]) - class_1923.method_8332(pts[j]);
                  int d = dx * dx + dz * dz;
                  if (d < best[j] || d == best[j] && (parent[j] < 0 || last < parent[j])) {
                     best[j] = d;
                     parent[j] = last;
                  }

                  if (pick < 0 || best[j] < best[pick] || best[j] == best[pick] && (parent[j] < parent[pick] || parent[j] == parent[pick] && j < pick)) {
                     pick = j;
                  }
               }
            }

            if (pick < 0) {
               break;
            }

            long a = pts[parent[pick]];
            long b = pts[pick];
            rectangle(
               tree,
               Math.min(class_1923.method_8325(a), class_1923.method_8325(b)),
               Math.min(class_1923.method_8332(a), class_1923.method_8332(b)),
               Math.max(class_1923.method_8325(a), class_1923.method_8325(b)),
               Math.max(class_1923.method_8332(a), class_1923.method_8332(b))
            );
            inTree[pick] = true;
            last = pick;
         }

         out.addAll(tree);
         LongIterator it = tree.iterator();

         while (it.hasNext()) {
            long l = it.nextLong();
            int x = class_1923.method_8325(l);
            int z = class_1923.method_8332(l);
            rectangle(out, x - 1, z - 1, x + 1, z + 1);
         }
      }
   }

   private static void rectangle(LongOpenHashSet set, int x0, int z0, int x1, int z1) {
      for (int x = x0; x <= x1; x++) {
         for (int z = z0; z <= z1; z++) {
            set.add(class_1923.method_8331(x, z));
         }
      }
   }
}
