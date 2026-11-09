package dev.byteplus.mixin;

import dev.byteplus.module.modules.visuals.CustomSkyModule;
import net.minecraft.class_243;
import net.minecraft.class_4063;
import net.minecraft.class_9955;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_9955.class)
public abstract class CloudRendererMixin {
   @Inject(method = "method_62168", at = @At("HEAD"), cancellable = true)
   private void byteplus$hideClouds(int color, class_4063 status, float height, class_243 camera, long gameTime, float partialTick, CallbackInfo ci) {
      if (CustomSkyModule.hidesClouds()) {
         ci.cancel();
      }
   }
}
