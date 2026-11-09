package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting<Integer> {
   public ColorSetting(String name, int argb) {
      super(name, argb);
   }

   public int rgb() {
      return this.value & 16777215;
   }

   public int argb() {
      return this.value;
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(String.format("#%08X", this.value));
   }

   @Override
   public void load(JsonElement e) {
      if (e.isJsonPrimitive()) {
         try {
            this.value = (int)Long.parseLong(e.getAsString().replace("#", ""), 16);
         } catch (NumberFormatException var3) {
         }
      }
   }
}
