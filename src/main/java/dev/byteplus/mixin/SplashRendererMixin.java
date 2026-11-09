package dev.byteplus.mixin;

import dev.byteplus.gui.TitleScreenOverlay;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_8519;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_8519.class)
public abstract class SplashRendererMixin {
   @Inject(method = "method_51453", at = @At("HEAD"), cancellable = true)
   private void byteplus$splash(class_332 graphics, int width, class_327 font, float alpha, CallbackInfo ci) {
      if (TitleScreenOverlay.active()) {
         ci.cancel();
      }
   }
}
