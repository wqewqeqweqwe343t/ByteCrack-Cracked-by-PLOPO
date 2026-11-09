package dev.byteplus.mixin;

import dev.byteplus.module.modules.visuals.ZoomModule;
import net.minecraft.class_4184;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_757.class)
public abstract class GameRendererMixin {
   @Inject(method = "method_3196", at = @At("RETURN"), cancellable = true)
   private void byteplus$zoom(class_4184 camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
      float m = ZoomModule.fovMultiplier();
      if (m != 1.0F) {
         cir.setReturnValue(cir.getReturnValueF() * m);
      }
   }
}
