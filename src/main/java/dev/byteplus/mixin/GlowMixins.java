package dev.byteplus.mixin;

import dev.byteplus.module.modules.visuals.GlowEspModule;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_310.class)
public abstract class GlowMixins {
   @Inject(method = "method_27022", at = @At("RETURN"), cancellable = true)
   private void byteplus$glow(class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
      if (!cir.getReturnValueZ() && GlowEspModule.shouldGlow(entity)) {
         cir.setReturnValue(true);
      }
   }
}
