package org.molgenis.semanticmapper.algorithmgenerator.categorymapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    assertEquals(
        categoryMapper.convert(twicePerDayAmount, twicePerWeekAmount), Double.valueOf(12.));

    AmountWrapper twiceAtLeastPerWeek =
        AmountWrapper.create(Amount.rangeOf(2, 7, NonSI.WEEK.inverse()));
    AmountWrapper threeTimesPerWeek = AmountWrapper.create(Amount.valueOf(3, NonSI.WEEK.inverse()));

    assertEquals(
        categoryMapper.convert(twiceAtLeastPerWeek, threeTimesPerWeek), Double.valueOf(2.5));
  }

  @Test
  void testExtractNumbers() {
    assertEquals(
        CategoryMapperUtil.extractNumbers("1-3 per month").stream()
            .mapToInt(Double::intValue)
            .sum(),
        4);

    assertEquals(
        CategoryMapperUtil.extractNumbers("2.6-3.4 per month").stream()
            .mapToInt(Double::intValue)
            .sum(),
        5);

    assertEquals(
        CategoryMapperUtil.extractNumbers("not this month").stream()
            .mapToInt(Double::intValue)
            .sum(),
        0);
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
    assertEquals(unit.toString(), NonSI.YEAR.inverse().toString());
  }

  @Test
  void testMatchUnit() {
    Unit<?> unit = CategoryMapperUtil.findDurationUnit("1-3 per month");
    assertEquals(unit.toString(), NonSI.MONTH.inverse().toString());
  }

  @Test
  void testConvertDescriptionToAmount() {
    assertEquals(
        CategoryMapperUtil.convertDescriptionToAmount("1-3 per month"),
        AmountWrapper.create(
            Amount.rangeOf((double) 1, (double) 3, NonSI.MONTH.inverse())
                .to(NonSI.WEEK.inverse())));

    assertEquals(
        CategoryMapperUtil.convertDescriptionToAmount("once or twice per month"),
        AmountWrapper.create(
            Amount.rangeOf((double) 1, (double) 2, NonSI.MONTH.inverse())
                .to(NonSI.WEEK.inverse())));

    assertEquals(
        CategoryMapperUtil.convertDescriptionToAmount("daily"),
        AmountWrapper.create(
            Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(NonSI.WEEK.inverse())));

    assertEquals(
        CategoryMapperUtil.convertDescriptionToAmount("About once a week"),
        AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

    assertEquals(
        CategoryMapperUtil.convertDescriptionToAmount("several times a month"),
        AmountWrapper.create(
            Amount.rangeOf(
                    (double) 3,
                    NonSI.DAY.inverse().getConverterTo(NonSI.MONTH.inverse()).convert((double) 1)
                        - 1,
                    NonSI.MONTH.inverse())
                .to(NonSI.WEEK.inverse()),
            false));
  }

  @Test
  void testConvertWordToNumber() {
    assertEquals(CategoryMapperUtil.convertWordToNumber("one-3 per month"), "1 3 per month");
    assertEquals(CategoryMapperUtil.convertWordToNumber("once a week"), "1 a week");
  }

  @Test
  void testIntegration() {
    String sourceCategory0 = "NoT this month";
    AmountWrapper amountSourceCategory0 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory0);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory0.getAmount()));
    assertEquals(
        amountSourceCategory0.toString(),
        AmountWrapper.create(Amount.valueOf(0, NonSI.MONTH.inverse()).to(NonSI.WEEK.inverse()))
            .toString());

    String sourceCategory1 = "never/less than 1 per month";
    AmountWrapper amountSourceCategory1 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory1);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory1.getAmount()));
    assertEquals(
        amountSourceCategory1.toString(),
        AmountWrapper.create(
                Amount.rangeOf((double) 0, (double) 1, NonSI.MONTH.inverse())
                    .to(NonSI.WEEK.inverse()))
            .toString());

    String sourceCategory2 = "1-3 per month";
    AmountWrapper amountSourceCategory2 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory2);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory2.getAmount()));
    assertEquals(
        amountSourceCategory2,
        AmountWrapper.create(
            Amount.rangeOf((double) 1, (double) 3, NonSI.MONTH.inverse())
                .to(NonSI.WEEK.inverse())));

    String sourceCategory3 = "once a week";
    AmountWrapper amountSourceCategory3 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory3);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory3.getAmount()));
    assertEquals(
        amountSourceCategory3,
        AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

    String sourceCategory4 = "2-4 per week";
    AmountWrapper amountSourceCategory4 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory4);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory4.getAmount()));
    assertEquals(
        amountSourceCategory4,
        AmountWrapper.create(Amount.rangeOf((double) 2, (double) 4, NonSI.WEEK.inverse())));

    String sourceCategory5 = "5-6 per week";
    AmountWrapper amountSourceCategory5 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory5);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory5.getAmount()));
    assertEquals(
        amountSourceCategory5,
        AmountWrapper.create(Amount.rangeOf((double) 5, (double) 6, NonSI.WEEK.inverse())));

    String sourceCategory6 = "once a day";
    AmountWrapper amountSourceCategory6 =
        CategoryMapperUtil.convertDescriptionToAmount(sourceCategory6);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory6.getAmount()));
    assertEquals(
        amountSourceCategory6,
        AmountWrapper.create(
            Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(NonSI.WEEK.inverse())));

    String targetCategory1 = "Almost daily + daily";
    AmountWrapper amountTargetCategory1 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory1);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountTargetCategory1.getAmount()));
    assertEquals(
        amountTargetCategory1,
        AmountWrapper.create(
            Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(NonSI.WEEK.inverse())));

    String targetCategory2 = "Several times a week";
    AmountWrapper amountTargetCategory2 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory2);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountTargetCategory2.getAmount()));
    assertEquals(
        amountTargetCategory2,
        AmountWrapper.create(Amount.rangeOf((double) 3, (double) 6, NonSI.WEEK.inverse()), false));

    String targetCategory3 = "About once a week";
    AmountWrapper amountTargetCategory3 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory3);
    assertFalse(CategoryMapperUtil.isAmountRanged(amountTargetCategory3.getAmount()));
    assertEquals(
        amountTargetCategory3,
        AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

    String targetCategory4 = "Never + fewer than once a week";
    AmountWrapper amountTargetCategory4 =
        CategoryMapperUtil.convertDescriptionToAmount(targetCategory4);
    assertTrue(CategoryMapperUtil.isAmountRanged(amountTargetCategory4.getAmount()));
    assertEquals(
        amountTargetCategory4,
        AmountWrapper.create(Amount.rangeOf((double) 0, (double) 1, NonSI.WEEK.inverse())));
  }
}
