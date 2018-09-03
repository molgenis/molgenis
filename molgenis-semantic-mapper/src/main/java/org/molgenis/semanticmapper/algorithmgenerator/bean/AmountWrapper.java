package org.molgenis.semanticmapper.algorithmgenerator.bean;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.jscience.physics.amount.Amount;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AmountWrapper.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AmountWrapper {
  @Nullable
  public abstract Amount<?> getAmount();

  public abstract boolean isDetermined();

  public static AmountWrapper create(Amount<?> amount) {
    return new AutoValue_AmountWrapper(amount, true);
  }

  public static AmountWrapper create(Amount<?> amount, boolean determined) {
    return new AutoValue_AmountWrapper(amount, determined);
  }
}
