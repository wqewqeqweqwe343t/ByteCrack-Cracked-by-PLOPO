package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.function.Supplier;

public class InfoSetting extends Setting<Supplier<String>> {
   public InfoSetting(String name, Supplier<String> value) {
      super(name, value);
   }

   public String text() {
      return this.value.get();
   }

   @Override
   public JsonElement save() {
      return JsonNull.INSTANCE;
   }

   @Override
   public void load(JsonElement e) {
   }
}
