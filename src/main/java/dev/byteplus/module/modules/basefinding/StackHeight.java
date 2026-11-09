package dev.byteplus.module.modules.basefinding;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
class StackHeight {
   public final int minY;
   public final int maxY;
   public final int stackHeight;

   public StackHeight(int minY, int maxY, int stackHeight) {
      this.minY = minY;
      this.maxY = maxY;
      this.stackHeight = stackHeight;
   }

   public int minY() {
      return this.minY;
   }

   public int maxY() {
      return this.maxY;
   }

   public int stackHeight() {
      return this.stackHeight;
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof StackHeight other && this.minY == other.minY && this.maxY == other.maxY && this.stackHeight == other.stackHeight;
   }

   @Override
   public int hashCode() {
      return (this.minY * 31 + this.maxY) * 31 + this.stackHeight;
   }
}
