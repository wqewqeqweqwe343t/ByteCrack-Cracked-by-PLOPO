package dev.byteplus.util;

import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_746;

public final class InvUtil {
   private InvUtil() {
   }

   private static class_310 mc() {
      return class_310.method_1551();
   }

   public static int findHotbar(Predicate<class_1799> p) {
      class_1661 inv = mc().field_1724.method_31548();

      for (int i = 0; i < 9; i++) {
         if (p.test(inv.method_5438(i))) {
            return i;
         }
      }

      return -1;
   }

   public static int findInventory(Predicate<class_1799> p) {
      class_1661 inv = mc().field_1724.method_31548();

      for (int i = 9; i < 36; i++) {
         if (p.test(inv.method_5438(i))) {
            return i;
         }
      }

      for (int i = 0; i < 9; i++) {
         if (p.test(inv.method_5438(i))) {
            return i;
         }
      }

      return -1;
   }

   public static int selected() {
      return mc().field_1724.method_31548().method_67532();
   }

   public static void select(int hotbarSlot) {
      if (hotbarSlot >= 0 && hotbarSlot < 9) {
         mc().field_1724.method_31548().method_61496(hotbarSlot);
      }
   }

   public static int menuSlot(int invIndex) {
      return invIndex < 9 ? invIndex + 36 : invIndex;
   }

   public static void swapToOffhand(int invIndex) {
      class_746 p = mc().field_1724;
      mc().field_1761.method_2906(p.field_7498.field_7763, menuSlot(invIndex), 40, class_1713.field_7791, p);
   }

   public static void swapToHotbar(int invIndex, int hotbarSlot) {
      class_746 p = mc().field_1724;
      mc().field_1761.method_2906(p.field_7498.field_7763, menuSlot(invIndex), hotbarSlot, class_1713.field_7791, p);
   }

   public static void interact(class_3965 hit) {
      mc().field_1761.method_2896(mc().field_1724, class_1268.field_5808, hit);
      mc().field_1724.method_6104(class_1268.field_5808);
   }
}
