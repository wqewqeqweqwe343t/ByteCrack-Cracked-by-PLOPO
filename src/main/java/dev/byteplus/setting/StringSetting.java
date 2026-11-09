package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting<String> {
   private final int maxLength;

   public StringSetting(String name, String defaultValue) {
      this(name, defaultValue, 64);
   }

   public StringSetting(String name, String defaultValue, int maxLength) {
      super(name, defaultValue);
      this.maxLength = maxLength;
   }

   public int getMaxLength() {
      return this.maxLength;
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement e) {
      if (e.isJsonPrimitive()) {
         this.value = e.getAsString();
      }
   }
}
