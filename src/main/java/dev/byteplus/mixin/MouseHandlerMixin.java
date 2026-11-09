package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_312.class)
public abstract class MouseHandlerMixin {
   @Inject(method = "method_1598", at = @At("HEAD"), cancellable = true)
   private void byteplus$scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
      if (BytePlus.modules() != null && vertical != 0.0 && BytePlus.modules().onScroll(vertical)) {
         ci.cancel();
      }
   }
}
