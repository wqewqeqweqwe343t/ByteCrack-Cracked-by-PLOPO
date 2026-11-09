package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
   private final double min;
   private final double max;
   private final double step;

   public NumberSetting(String name, double defaultValue, double min, double max, double step) {
      super(name, defaultValue);
      this.min = min;
      this.max = max;
      this.step = step;
   }

   public double getMin() {
      return this.min;
   }

   public double getMax() {
      return this.max;
   }

   public double getStep() {
      return this.step;
   }

   public int getInt() {
      return (int)Math.round(this.value);
   }

   public float getFloat() {
      return this.value.floatValue();
   }

   public void set(Double v) {
      double clamped = Math.max(this.min, Math.min(this.max, v));
      if (this.step > 0.0) {
         clamped = Math.round(clamped / this.step) * this.step;
      }

      this.value = Math.max(this.min, Math.min(this.max, clamped));
   }

   public void setFraction(double f) {
      this.set(this.min + (this.max - this.min) * Math.max(0.0, Math.min(1.0, f)));
   }

   public double getFraction() {
      return (this.value - this.min) / (this.max - this.min);
   }

   public String format() {
      if (this.step >= 1.0) {
         return String.valueOf(this.getInt());
      } else {
         return this.step >= 0.1 ? String.format("%.1f", this.value) : String.format("%.2f", this.value);
      }
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement e) {
      if (e.isJsonPrimitive()) {
         this.set(e.getAsDouble());
      }
   }
}
