package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.visuals.CustomSkyModule;
import dev.byteplus.render.CustomSkyRenderer;
import net.minecraft.class_12131;
import net.minecraft.class_4587;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_9975.class)
public abstract class SkyRendererMixin {
   private static CustomSkyModule byteplus$sky() {
      if (BytePlus.modules() == null) {
         return null;
      }

      for (Module m : BytePlus.modules().all()) {
         if (m instanceof CustomSkyModule s) {
            return s.isEnabled() ? s : null;
         }
      }

      return null;
   }

   @Inject(method = "method_62302", at = @At("HEAD"), cancellable = true)
   private void byteplus$skyDisc(int color, CallbackInfo ci) {
      if (byteplus$sky() != null && CustomSkyRenderer.draw()) {
         ci.cancel();
      }
   }

   @Inject(method = "method_62306", at = @At("HEAD"), cancellable = true)
   private void byteplus$sunrise(class_4587 pose, float angle, int color, CallbackInfo ci) {
      if (byteplus$sky() != null) {
         ci.cancel();
      }
   }

   @Inject(method = "method_62305", at = @At("HEAD"), cancellable = true)
   private void byteplus$darkDisc(CallbackInfo ci) {
      if (byteplus$sky() != null) {
         ci.cancel();
      }
   }

   @Inject(method = "method_62307", at = @At("HEAD"), cancellable = true)
   private void byteplus$sunMoon(
      class_4587 pose, float sunAngle, float moonAngle, float starAngle, class_12131 phase, float rain, float starBrightness, CallbackInfo ci
   ) {
      CustomSkyModule s = byteplus$sky();
      if (s != null && !s.sunAndMoon.isOn()) {
         ci.cancel();
      }
   }

   @Inject(method = "method_62310", at = @At("HEAD"), cancellable = true)
   private void byteplus$stars(float brightness, class_4587 pose, CallbackInfo ci) {
      if (byteplus$sky() != null) {
         ci.cancel();
      }
   }
}
