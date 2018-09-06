package org.molgenis.semanticmapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Category.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class Category {
  public abstract String getCode();

  public abstract String getLabel();

  @Nullable
  public abstract AmountWrapper getAmountWrapper();

  public static Category create(String code, String label) {
    return new AutoValue_Category(code, label, null);
  }

  public static Category create(String code, String label, AmountWrapper amountWrapper) {
    return new AutoValue_Category(code, label, amountWrapper);
  }
}
