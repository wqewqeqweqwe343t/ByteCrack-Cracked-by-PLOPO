package dev.byteplus.mixin;

import dev.byteplus.gui.TitleScreenOverlay;
import dev.byteplus.render.CustomSkyRenderer;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_437.class)
public abstract class ScreenPanoramaMixin {
   @Inject(method = "method_57728", at = @At("HEAD"), cancellable = true)
   private void byteplus$panorama(class_332 graphics, float delta, CallbackInfo ci) {
      if ((Object)this instanceof class_442 && TitleScreenOverlay.active() && CustomSkyRenderer.drawMenu()) {
         ci.cancel();
      }
   }
}
