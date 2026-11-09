package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {
   public BooleanSetting(String name, boolean defaultValue) {
      super(name, defaultValue);
   }

   public boolean isOn() {
      return this.value;
   }

   public void toggle() {
      this.value = !this.value;
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement e) {
      if (e.isJsonPrimitive()) {
         this.value = e.getAsBoolean();
      }
   }
}
