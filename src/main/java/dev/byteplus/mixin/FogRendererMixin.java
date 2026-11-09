package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.visuals.CustomSkyModule;
import net.minecraft.class_1937;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_638;
import net.minecraft.class_758;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_758.class)
public abstract class FogRendererMixin {
   @Inject(method = "method_62185", at = @At("RETURN"))
   private void byteplus$fogColor(class_4184 camera, float partialTick, class_638 level, int renderDistance, float darken, CallbackInfoReturnable<Vector4f> cir) {
      if (BytePlus.modules() != null && level != null && level.method_27983() == class_1937.field_25179) {
         class_5636 fluid = camera.method_19334();
         if (fluid != class_5636.field_27886 && fluid != class_5636.field_27885 && fluid != class_5636.field_27887) {
            for (Module m : BytePlus.modules().all()) {
               if (m instanceof CustomSkyModule s && s.isEnabled() && s.fogBlend.isOn()) {
                  float[] h = s.horizon();
                  Vector4f v = (Vector4f)cir.getReturnValue();
                  float k = 0.85F;
                  v.set(v.x + (h[0] - v.x) * k, v.y + (h[1] - v.y) * k, v.z + (h[2] - v.z) * k, v.w);
                  return;
               }
            }
         }
      }
   }
}
