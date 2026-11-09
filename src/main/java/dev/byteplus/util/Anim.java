package dev.byteplus.util;

public final class Anim {
   private Anim() {
   }

   public static float approach(float current, float target, float speed, float dt) {
      float t = 1.0F - (float)Math.exp(-speed * dt);
      float v = current + (target - current) * t;
      return Math.abs(target - v) < 5.0E-4F ? target : v;
   }

   public static float easeOutCubic(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float f = 1.0F - t;
      return 1.0F - f * f * f;
   }

   public static float easeInOutCubic(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
   }

   public static int lerpColor(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int aa = a >>> 24;
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int ba = b >>> 24;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)(aa + (ba - aa) * t) << 24 | (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
   }
}
