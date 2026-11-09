package dev.byteplus.render;

import net.minecraft.class_243;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class Projection {
   private static final Matrix4f viewProj = new Matrix4f();
   private static double camX;
   private static double camY;
   private static double camZ;
   private static boolean valid;
   private static float partialTick;

   private Projection() {
   }

   public static float partialTick() {
      return partialTick;
   }

   public static void capture(Matrix4f modelView, Matrix4f projection, class_243 cameraPos, float partial) {
      partialTick = partial;
      viewProj.set(projection).mul(modelView);
      camX = cameraPos.field_1352;
      camY = cameraPos.field_1351;
      camZ = cameraPos.field_1350;
      valid = true;
   }

   public static boolean isValid() {
      return valid;
   }

   public static Matrix4f viewProj() {
      return viewProj;
   }

   public static void invalidate() {
      valid = false;
   }

   public static double camX() {
      return camX;
   }

   public static double camY() {
      return camY;
   }

   public static double camZ() {
      return camZ;
   }

   public static Vector4f toClip(double x, double y, double z, Vector4f out) {
      out.set((float)(x - camX), (float)(y - camY), (float)(z - camZ), 1.0F);
      return viewProj.transform(out);
   }
}
