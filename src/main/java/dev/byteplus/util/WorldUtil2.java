package dev.byteplus.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1937;
import net.minecraft.class_638;
import net.minecraft.class_7134;

@Environment(EnvType.CLIENT)
public final class WorldUtil2 {
   private WorldUtil2() {
   }

   public static boolean isOverworld(class_638 class_6382) {
      return class_6382.method_27983() == class_1937.field_25179 ? true : class_6382.method_40134().method_40225(class_7134.field_37666);
   }

   public static boolean isLoaded(class_638 class_6382, int n, int n2) {
      return class_6382.method_2935().method_12123(n, n2);
   }
}
