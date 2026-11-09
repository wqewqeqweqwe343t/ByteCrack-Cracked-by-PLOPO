package dev.byteplus.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_636.class)
public interface MultiPlayerGameModeAccessor {
   @Accessor("field_3715")
   float byteplus$destroyProgress();

   @Accessor("field_3714")
   class_2338 byteplus$destroyBlockPos();

   @Accessor("field_3715")
   void byteplus$setDestroyProgress(float var1);

   @Accessor("field_3716")
   void byteplus$setDestroyDelay(int var1);
}
