package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.visuals.CustomCrosshairModule;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_329.class)
public abstract class GuiMixin {
   @Inject(method = "method_1736", at = @At("HEAD"), cancellable = true)
   private void byteplus$crosshair(class_332 graphics, class_9779 delta, CallbackInfo ci) {
      if (BytePlus.modules() != null) {
         for (Module m : BytePlus.modules().all()) {
            if (m instanceof CustomCrosshairModule c && c.isEnabled()) {
               ci.cancel();
               return;
            }
         }
      }
   }
}
