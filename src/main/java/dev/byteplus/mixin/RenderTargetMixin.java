package dev.byteplus.mixin;

import dev.byteplus.render.RenderHook;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_276.class)
public abstract class RenderTargetMixin {
   @Inject(method = "method_1237", at = @At("RETURN"))
   private void byteplus$afterPresent(CallbackInfo ci) {
      if ((Object)this == class_310.method_1551().method_1522()) {
         RenderHook.onFrame();
      }
   }
}
