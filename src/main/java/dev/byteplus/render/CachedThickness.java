package dev.byteplus.render;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.class_1923;

public final class CachedThickness {
   private final ArrayList<double[]> boxes = new ArrayList<>();
   private final ArrayList<double[]> lines = new ArrayList<>();
   private final LongOpenHashSet cachedChunks = new LongOpenHashSet();
   private Integer cachedFill;
   private Integer cachedEdge;
   private double cachedY;
   private double cachedThickness;

   public void draw(LongSet chunks, int fillArgb, Integer edgeArgb, double y, double thickness) {
      if (!chunks.isEmpty() && fillArgb >>> 24 != 0) {
         this.prepare(chunks, fillArgb, edgeArgb, y, thickness);
         int rgb = fillArgb & 16777215;
         int a = fillArgb >>> 24;

         for (double[] b : this.boxes) {
            Esp.box(b[0], b[1], b[2], b[3], b[4], b[5], rgb, a, 0);
         }

         if (edgeArgb != null) {
            for (double[] l : this.lines) {
               Esp.line(l[0], l[1], l[2], l[3], l[4], l[5], edgeArgb & 16777215, edgeArgb >>> 24);
            }
         }
      }
   }

   private void prepare(LongSet chunks, int fill, Integer edge, double y, double thickness) {
      if (!Objects.equals(fill, this.cachedFill)
         || !Objects.equals(edge, this.cachedEdge)
         || y != this.cachedY
         || thickness != this.cachedThickness
         || !this.cachedChunks.equals(chunks)) {
         this.cachedChunks.clear();
         this.cachedChunks.addAll(chunks);
         this.cachedFill = fill;
         this.cachedEdge = edge;
         this.cachedY = y;
         this.cachedThickness = thickness;
         this.boxes.clear();
         this.lines.clear();
         LongOpenHashSet remaining = new LongOpenHashSet(chunks);

         while (!remaining.isEmpty()) {
            long l = remaining.iterator().nextLong();
            int x = class_1923.method_8325(l);
            int z = class_1923.method_8332(l);
            int minX = x;
            int maxX = x;
            int minZ = z;
            int maxZ = z;

            while (remaining.contains(class_1923.method_8331(minX - 1, z))) {
               minX--;
            }

            while (remaining.contains(class_1923.method_8331(maxX + 1, z))) {
               maxX++;
            }

            while (hasRow(remaining, minX, maxX, minZ - 1)) {
               minZ--;
            }

            while (hasRow(remaining, minX, maxX, maxZ + 1)) {
               maxZ++;
            }

            for (int i = minX; i <= maxX; i++) {
               for (int j = minZ; j <= maxZ; j++) {
                  remaining.remove(class_1923.method_8331(i, j));
               }
            }

            this.boxes.add(new double[]{minX * 16.0, y, minZ * 16.0, (maxX + 1) * 16.0, y + thickness, (maxZ + 1) * 16.0});
         }

         if (edge != null && edge >>> 24 != 0) {
            LongOpenHashSet horizontal = new LongOpenHashSet();
            LongOpenHashSet vertical = new LongOpenHashSet();
            LongIterator it = chunks.iterator();

            while (it.hasNext()) {
               long l = it.nextLong();
               int x = class_1923.method_8325(l);
               int z = class_1923.method_8332(l);
               horizontal.add(l);
               vertical.add(class_1923.method_8331(z, x));
               if (!chunks.contains(class_1923.method_8331(x, z + 1))) {
                  horizontal.add(class_1923.method_8331(x, z + 1));
               }

               if (!chunks.contains(class_1923.method_8331(x + 1, z))) {
                  vertical.add(class_1923.method_8331(z, x + 1));
               }
            }

            double ly = y + thickness + 0.003;
            this.mergeLines(horizontal, false, ly);
            this.mergeLines(vertical, true, ly);
         }
      }
   }

   private static boolean hasRow(LongSet set, int x0, int x1, int z) {
      for (int i = x0; i <= x1; i++) {
         if (!set.contains(class_1923.method_8331(i, z))) {
            return false;
         }
      }

      return true;
   }

   private void mergeLines(LongOpenHashSet set, boolean swapped, double y) {
      while (!set.isEmpty()) {
         long l = set.iterator().nextLong();
         int a = class_1923.method_8325(l);
         int b = class_1923.method_8332(l);
         int start = a;
         int end = a;

         while (set.contains(class_1923.method_8331(start - 1, b))) {
            start--;
         }

         while (set.contains(class_1923.method_8331(end + 1, b))) {
            end++;
         }

         for (int i = start; i <= end; i++) {
            set.remove(class_1923.method_8331(i, b));
         }

         if (swapped) {
            this.lines.add(new double[]{b * 16.0, y, start * 16.0, b * 16.0, y, (end + 1) * 16.0});
         } else {
            this.lines.add(new double[]{start * 16.0, y, b * 16.0, (end + 1) * 16.0, y, b * 16.0});
         }
      }
   }
}
