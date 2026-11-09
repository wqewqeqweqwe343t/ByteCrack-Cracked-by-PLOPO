package dev.byteplus.util;

public class Stacks {
   public final int stacks;
   public final int height;

   public Stacks(int stacks, int height) {
      this.stacks = stacks;
      this.height = height;
   }

   public int stacks() {
      return this.stacks;
   }

   public int height() {
      return this.height;
   }
}
