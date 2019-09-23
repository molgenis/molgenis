package org.molgenis.semanticmapper.algorithmgenerator.categorymapper;

import static java.lang.Double.valueOf;
import static javax.measure.unit.NonSI.DAY;
import static javax.measure.unit.NonSI.MONTH;
import static javax.measure.unit.NonSI.WEEK;
import static javax.measure.unit.NonSI.YEAR;
import static org.jscience.physics.amount.Amount.rangeOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.semanticmapper.algorithmgenerator.bean.AmountWrapper.create;
import static org.molgenis.semanticmapper.algorithmgenerator.categorymapper.CategoryMapperUtil.convertDescriptionToAmount;
import static org.molgenis.semanticmapper.algorithmgenerator.categorymapper.CategoryMapperUtil.convertWordToNumber;
import static org.molgenis.semanticmapper.algorithmgenerator.categorymapper.CategoryMapperUtil.extractNumbers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import org.jscience.physics.amount.Amount;
import org.junit.jupiter.api.Test;
import org.molgenis.semanticmapper.algorithmgenerator.bean.AmountWrapper;

class CategoryMapperTest {
  FrequencyCategoryMapper categoryMapper = new FrequencyCategoryMapper(Collections.emptyList());

  @Test
  void testConvertCategory() {
    AmountWrapper twicePerDayAmount = AmountWrapper.create(Amount.valueOf(2, NonSI.DAY.inverse()));
    AmountWrapper twicePerWeekAmount =
        AmountWrapper.create(Amount.valueOf(2, NonSI.WEEK.inverse()));

    assertEquals(valueOf(12.), categoryMapper.convert(twicePerDayAmount, twicePerWeekAmount));

    AmountWrapper twiceAtLeastPerWeek =
        AmountWrapper.create(Amount.rangeOf(2, 7, NonSI.WEEK.inverse()));
    AmountWrapper threeTimesPerWeek = AmountWrapper.create(Amount.valueOf(3, NonSI.WEEK.inverse()));

    assertEquals(valueOf(2.5), categoryMapper.convert(twiceAtLeastPerWeek, threeTimesPerWeek));
  }

  @Test
  void testExtractNumbers() {
    assertEquals(4, extractNumbers("1-3 per month").stream().mapToInt(Double::intValue).sum());

    assertEquals(5, extractNumbers("2.6-3.4 per month").stream().mapToInt(Double::intValue).sum());

    assertEquals(0, extractNumbers("not this month").stream().mapToInt(Double::intValue).sum());
  }

  @Test
  void testIsAmountRanged() {
    Amount<? extends Quantity> rangeOf = Amount.rangeOf(2, 2.4, NonSI.DAY.inverse());
    assertTrue(CategoryMapperUtil.isAmountRanged(rangeOf));

    Amount<? extends Quantity> vauleOf = Amount.valueOf(2, NonSI.DAY.inverse());
    assertFalse(CategoryMapperUtil.isAmountRanged(vauleOf));
  }

  @Test
  void testGetMostGeneralUnit() {
    List<Unit<?>> units = new ArrayList<>();

    units.add(NonSI.DAY.inverse());
    units.add(NonSI.YEAR.inverse());
    units.add(NonSI.MONTH.inverse());

    Unit<?> unit = CategoryMapperUtil.getMostGeneralUnit(units);
    assertEquals(YEAR.inverse().toString(), unit.toString());
  }

  @Test
  void testMatchUnit() {
    Unit<?> unit = CategoryMapperUtil.findDurationUnit("1-3 per month");
    assertEquals(MONTH.inverse().toString(), unit.toString());
  }

  @Test
  void testConvertDescriptionToAmount() {
    assertEquals(
        create(rangeOf((double) 1, (double) 3, MONTH.inverse()).to(WEEK.inverse())),
        convertDescriptionToAmount("1-3 per month"));

    assertEquals(
        create(rangeOf((double) 1, (double) 2, MONTH.inverse()).to(WEEK.inverse())),
        convertDescriptionToAmount("once or twice per month"));

    assertEquals(
        create(Amount.valueOf((double) 1, DAY.inverse()).to(WEEK.inverse())),
        convertDescriptionToAmount("daily"));

    assertEquals(
        create(Amount.valueOf((double) 1, WEEK.inverse())),
        convertDescriptionToAmount("About once a week"));

    assertEquals(
        create(
            rangeOf(
                    (double) 3,
                    DAY.inverse().getConverterTo(MONTH.inverse()).convert((double) 1) - 1,
                    MONTH.inverse())
                .to(WEEK.inverse()),
            false),
        convertDescriptionToAmount("several times a month"));
  }

  @Test
  void testConvertWordToNumber() {
    assertEquals("1 3 per month", convertWordToNumber("one-3 per month"));
    assertEquals("1 a week", convertWordToNumber("once a week"));
  }

  @Test
  void testIntegration() {
    String sourceCategory0 = "NoT this month";
    AmountWrapper amountSourceCategory0 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory0);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory0.getAmount()));
    assertEquals(
        create(Amount.valueOf(0, MONTH.inverse()).to(WEEK.inverse())).toString(),
        amountSourceCategory0.toString());

    String sourceCategory1 = "never/less than 1 per month";
    AmountWrapper amountSourceCategory1 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory1);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory1.getAmount()));
    assertEquals(
        create(rangeOf((double) 0, (double) 1, MONTH.inverse()).to(WEEK.inverse())).toString(),
        amountSourceCategory1.toString());

    String sourceCategory2 = "1-3 per month";
    AmountWrapper amountSourceCategory2 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory2);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory2.getAmount()));
    assertEquals(
        create(rangeOf((double) 1, (double) 3, MONTH.inverse()).to(WEEK.inverse())),
        amountSourceCategory2);

    String sourceCategory3 = "once a week";
    AmountWrapper amountSourceCategory3 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory3);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory3.getAmount()));
    assertEquals(create(Amount.valueOf((double) 1, WEEK.inverse())), amountSourceCategory3);

    String sourceCategory4 = "2-4 per week";
    AmountWrapper amountSourceCategory4 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory4);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory4.getAmount()));
    assertEquals(create(rangeOf((double) 2, (double) 4, WEEK.inverse())), amountSourceCategory4);

    String sourceCategory5 = "5-6 per week";
    AmountWrapper amountSourceCategory5 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory5);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory5.getAmount()));
    assertEquals(create(rangeOf((double) 5, (double) 6, WEEK.inverse())), amountSourceCategory5);

    String sourceCategory6 = "once a day";
    AmountWrapper amountSourceCategory6 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory6);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory6.getAmount()));
    assertEquals(
        create(Amount.valueOf((double) 1, DAY.inverse()).to(WEEK.inverse())),
        amountSourceCategory6);

    String targetCategory1 = "Almost daily + daily";
    AmountWrapper amountTargetCategory1 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory1);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountTargetCategory1.getAmount()));
    assertEquals(
        create(Amount.valueOf((double) 1, DAY.inverse()).to(WEEK.inverse())),
        amountTargetCategory1);

    String targetCategory2 = "Several times a week";
    AmountWrapper amountTargetCategory2 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory2);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountTargetCategory2.getAmount()));
    assertEquals(
        create(rangeOf((double) 3, (double) 6, WEEK.inverse()), false), amountTargetCategory2);

    String targetCategory3 = "About once a week";
    AmountWrapper amountTargetCategory3 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory3);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountTargetCategory3.getAmount()));
    assertEquals(create(Amount.valueOf((double) 1, WEEK.inverse())), amountTargetCategory3);

    String targetCategory4 = "Never + fewer than once a week";
    AmountWrapper amountTargetCategory4 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory4);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountTargetCategory4.getAmount()));
    assertEquals(create(rangeOf((double) 0, (double) 1, WEEK.inverse())), amountTargetCategory4);
  }
}
