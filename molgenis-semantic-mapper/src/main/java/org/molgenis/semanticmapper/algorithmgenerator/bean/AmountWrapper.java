package org.molgenis.semanticmapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.jscience.physics.amount.Amount;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AmountWrapper.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AmountWrapper {
  @Nullable
  @CheckForNull
  public abstract Amount<?> getAmount();

  public abstract boolean isDetermined();

  public static AmountWrapper create(Amount<?> amount) {
    return new AutoValue_AmountWrapper(amount, true);
  }

  public static AmountWrapper create(Amount<?> amount, boolean determined) {
    return new AutoValue_AmountWrapper(amount, determined);
  }
}
