package dev.byteplus.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_310.class)
public interface MinecraftAccessor {
   @Accessor("field_1752")
   int byteplus$getRightClickDelay();

   @Accessor("field_1752")
   void byteplus$setRightClickDelay(int var1);
}
