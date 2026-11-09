package dev.byteplus.mixin;

import dev.byteplus.render.knife.KnifeRenderer;
import net.minecraft.class_11659;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_759;
import net.minecraft.class_811;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_759.class)
public abstract class ItemInHandRendererMixin {
   @Inject(method = "method_3233", at = @At("HEAD"), cancellable = true)
   private void byteplus$knife(class_1309 entity, class_1799 stack, class_811 ctx, class_4587 pose, class_11659 collector, int light, CallbackInfo ci) {
      if (KnifeRenderer.tryRender(entity, stack, ctx, pose, collector, light)) {
         ci.cancel();
      }
   }
}
