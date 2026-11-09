package dev.byteplus.util;

public final class TickRate {
   private static final float[] samples = new float[10];
   private static int index;
   private static int count;
   private static long lastPacket = -1L;

   private TickRate() {
   }

   public static void onTimePacket() {
      long now = System.nanoTime();
      if (lastPacket != -1L) {
         float seconds = (float)(now - lastPacket) / 1.0E9F;
         if (seconds > 0.0F) {
            samples[index] = Math.max(0.0F, Math.min(20.0F, 20.0F / seconds));
            index = (index + 1) % samples.length;
            count = Math.min(samples.length, count + 1);
         }
      }

      lastPacket = now;
   }

   public static void reset() {
      lastPacket = -1L;
      count = 0;
      index = 0;
   }

   public static float get() {
      if (count == 0) {
         return 20.0F;
      }

      float sum = 0.0F;

      for (int i = 0; i < count; i++) {
         sum += samples[i];
      }

      return sum / count;
   }
}
