package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.visuals.BlockOutlineModule;
import net.minecraft.class_11658;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
public abstract class BlockOutlineMixin {
   @Inject(method = "method_62210", at = @At("HEAD"), cancellable = true)
   private void byteplus$outline(class_4598 buffers, class_4587 pose, boolean translucent, class_11658 state, CallbackInfo ci) {
      if (BytePlus.modules() != null) {
         for (Module m : BytePlus.modules().all()) {
            if (m instanceof BlockOutlineModule b && b.isEnabled()) {
               ci.cancel();
               return;
            }
         }
      }
   }
}
