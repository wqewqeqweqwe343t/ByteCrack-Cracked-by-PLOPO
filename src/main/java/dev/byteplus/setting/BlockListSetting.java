package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public class BlockListSetting extends Setting<Map<class_2248, Integer>> {
   private int version;
   private List<class_2248> choices;
   private Map<class_2248, Integer> defaultColors;

   public BlockListSetting(String name, Map<class_2248, Integer> defaults) {
      super(name, new LinkedHashMap<>(defaults));
   }

   public BlockListSetting limitTo(List<class_2248> choices, Map<class_2248, Integer> colors) {
      this.choices = List.copyOf(choices);
      this.defaultColors = new LinkedHashMap<>(colors);
      return this;
   }

   public List<class_2248> choices() {
      return this.choices;
   }

   public int startColor(class_2248 b) {
      return this.defaultColors != null && this.defaultColors.containsKey(b) ? this.defaultColors.get(b) : defaultColor(b);
   }

   public int version() {
      return this.version;
   }

   public boolean contains(class_2248 b) {
      return this.value.containsKey(b);
   }

   public int colorOf(class_2248 b) {
      return this.value.getOrDefault(b, 16777215);
   }

   public void set(Map<class_2248, Integer> v) {
      this.value = new LinkedHashMap<>(v);
      this.version++;
   }

   public static int defaultColor(class_2248 b) {
      int c = b.method_26403().field_16011;
      return c == 0 ? 16763176 : c;
   }

   @Override
   public JsonElement save() {
      JsonObject o = new JsonObject();

      for (Entry<class_2248, Integer> e : this.value.entrySet()) {
         o.addProperty(class_7923.field_41175.method_10221(e.getKey()).toString(), String.format("#%06X", e.getValue() & 16777215));
      }

      return o;
   }

   @Override
   public void load(JsonElement e) {
      if (e.isJsonObject()) {
         Map<class_2248, Integer> map = new LinkedHashMap<>();

         for (Entry<String, JsonElement> entry : e.getAsJsonObject().entrySet()) {
            class_2960 id = class_2960.method_12829(entry.getKey());
            if (id != null) {
               class_2248 b = (class_2248)class_7923.field_41175.method_63535(id);
               if (b != null) {
                  int color;
                  try {
                     color = Integer.parseInt(entry.getValue().getAsString().replace("#", ""), 16);
                  } catch (Exception ex) {
                     color = defaultColor(b);
                  }

                  map.put(b, color);
               }
            }
         }

         this.set(map);
      }
   }
}
