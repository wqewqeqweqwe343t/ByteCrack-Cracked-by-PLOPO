package dev.byteplus.mixin;

import net.minecraft.class_8765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_8765.class)
public interface PlayerSkinWidgetAccessor {
   @Accessor("field_46006")
   float byteplus$getRotationY();

   @Accessor("field_46006")
   void byteplus$setRotationY(float var1);

   @Accessor("field_46005")
   void byteplus$setRotationX(float var1);
}
