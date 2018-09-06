package org.molgenis.semanticmapper.algorithmgenerator.categorymapper.convertor;

import javax.measure.unit.NonSI;
import org.jscience.physics.amount.Amount;
import org.molgenis.semanticmapper.algorithmgenerator.bean.AmountWrapper;

public class DailyAmountConvertor extends AmountConvertor {
  private static final String CRITERIA = "daily";
  private static final Amount<?> AMOUNT =
      Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(STANDARD_PER_WEEK_UNIT);

  public boolean matchCriteria(String description) {
    return description.toLowerCase().contains(CRITERIA);
  }

  AmountWrapper getInternalAmount(String description) {
    return AmountWrapper.create(AMOUNT);
  }
}
