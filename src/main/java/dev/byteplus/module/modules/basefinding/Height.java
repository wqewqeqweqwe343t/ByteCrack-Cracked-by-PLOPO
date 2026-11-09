package dev.byteplus.module.modules.basefinding;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
class Height {
   public final long pos;
   public final int height;
   public final long[] stacks;

   public Height(long pos, int height, long[] stacks) {
      this.pos = pos;
      this.height = height;
      this.stacks = stacks;
   }

   public long pos() {
      return this.pos;
   }

   public int height() {
      return this.height;
   }

   public long[] stacks() {
      return this.stacks;
   }

   boolean sameAs(Height height) {
      if (this.pos != height.pos) {
         return false;
      } else {
         return this.height != height.height ? false : Arrays.equals(this.stacks, height.stacks);
      }
   }
}
