package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public class ModeSetting extends Setting<String> {
   private final List<String> modes;

   public ModeSetting(String name, String defaultValue, String... modes) {
      super(name, defaultValue);
      this.modes = List.of(modes);
   }

   public List<String> getModes() {
      return this.modes;
   }

   public boolean is(String mode) {
      return this.value.equalsIgnoreCase(mode);
   }

   public int index() {
      return Math.max(0, this.modes.indexOf(this.value));
   }

   public void cycle(int dir) {
      int i = (this.index() + dir + this.modes.size()) % this.modes.size();
      this.value = this.modes.get(i);
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement e) {
      if (e.isJsonPrimitive()) {
         String s = e.getAsString();

         for (String m : this.modes) {
            if (m.equalsIgnoreCase(s)) {
               this.value = m;
            }
         }
      }
   }
}
