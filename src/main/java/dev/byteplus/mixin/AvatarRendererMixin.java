package dev.byteplus.mixin;

import dev.byteplus.module.modules.render.NameTagsModule;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1007.class)
public abstract class AvatarRendererMixin {
   @Inject(
      method = "method_4213(Lnet/minecraft/class_10055;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void byteplus$hideNameTag(class_10055 state, class_4587 pose, class_11659 collector, class_12075 camera, CallbackInfo ci) {
      if (NameTagsModule.hideVanilla()) {
         ci.cancel();
      }
   }
}
