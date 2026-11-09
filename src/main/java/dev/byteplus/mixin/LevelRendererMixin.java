package dev.byteplus.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.byteplus.render.Projection;
import net.minecraft.class_4184;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_761.class)
public abstract class LevelRendererMixin {
   @Inject(method = "method_22710", at = @At("HEAD"))
   private void byteplus$captureMatrices(
      class_9922 allocator,
      class_9779 deltaTracker,
      boolean renderBlockOutline,
      class_4184 camera,
      Matrix4f modelView,
      Matrix4f projection,
      Matrix4f cullingProjection,
      GpuBufferSlice fog,
      Vector4f fogColor,
      boolean renderSky,
      CallbackInfo ci
   ) {
      Projection.capture(modelView, projection, camera.method_71156(), deltaTracker.method_60637(false));
   }
}
