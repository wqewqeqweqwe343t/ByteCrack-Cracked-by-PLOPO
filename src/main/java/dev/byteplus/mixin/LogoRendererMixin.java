package dev.byteplus.mixin;

import dev.byteplus.gui.TitleScreenOverlay;
import net.minecraft.class_332;
import net.minecraft.class_8020;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_8020.class)
public abstract class LogoRendererMixin {
   @Inject(method = "method_48209(Lnet/minecraft/class_332;IF)V", at = @At("HEAD"), cancellable = true)
   private void byteplus$logo(class_332 graphics, int width, float alpha, CallbackInfo ci) {
      if (TitleScreenOverlay.active()) {
         ci.cancel();
      }
   }
}
