package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.modules.misc.FreecamModule;
import dev.byteplus.module.modules.render.FreeLookModule;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4184.class)
public abstract class CameraMixin {
   @Shadow
   private boolean field_18719;

   @Shadow
   protected abstract void method_19325(float var1, float var2);

   @Shadow
   protected abstract void method_19327(double var1, double var3, double var5);

   @Inject(method = "method_19321", at = @At("TAIL"))
   private void byteplus$override(class_1937 level, class_1297 entity, boolean detachedArg, boolean mirror, float partialTick, CallbackInfo ci) {
      if (BytePlus.modules() != null) {
         FreecamModule freecam = BytePlus.modules().freecam;
         if (freecam.isEnabled()) {
            freecam.advance();
            this.field_18719 = true;
            this.method_19325(freecam.yaw, freecam.pitch);
            this.method_19327(freecam.x, freecam.y, freecam.z);
         } else {
            FreeLookModule look = BytePlus.modules().freeLook;
            if (look.isEnabled() && entity != null) {
               this.field_18719 = true;
               this.method_19325(look.yaw, look.pitch);
               class_243 pos = look.cameraPosition(entity, partialTick);
               this.method_19327(pos.field_1352, pos.field_1351, pos.field_1350);
            }
         }
      }
   }
}
