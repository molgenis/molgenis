package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.algorithmgenerator.bean.AmountWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryMapperTest
{
	FrequencyCategoryMapper categoryMapper = new FrequencyCategoryMapper(Collections.emptyList());

	@Test
	public void testConvertCategory()
	{
		AmountWrapper twicePerDayAmount = AmountWrapper.create(Amount.valueOf(2, NonSI.DAY.inverse()));
		AmountWrapper twicePerWeekAmount = AmountWrapper.create(Amount.valueOf(2, NonSI.WEEK.inverse()));

		Assert.assertEquals(categoryMapper.convert(twicePerDayAmount, twicePerWeekAmount), (double) 12);

		AmountWrapper twiceAtLeastPerWeek = AmountWrapper.create(Amount.rangeOf(2, 7, NonSI.WEEK.inverse()));
		AmountWrapper threeTimesPerWeek = AmountWrapper.create(Amount.valueOf(3, NonSI.WEEK.inverse()));

		Assert.assertEquals(categoryMapper.convert(twiceAtLeastPerWeek, threeTimesPerWeek), 2.5);
	}

	@Test
	public void testExtractNumbers()
	{
		Assert.assertEquals(CategoryMapperUtil.extractNumbers("1-3 per month")
											  .stream()
											  .mapToInt(Double::intValue)
											  .sum(), 4);

		Assert.assertEquals(CategoryMapperUtil.extractNumbers("2.6-3.4 per month")
											  .stream()
											  .mapToInt(Double::intValue)
											  .sum(), 5);

		Assert.assertEquals(CategoryMapperUtil.extractNumbers("not this month")
											  .stream()
											  .mapToInt(Double::intValue)
											  .sum(), 0);
	}

	@Test
	public void testIsAmountRanged()
	{
		Amount<? extends Quantity> rangeOf = Amount.rangeOf(2, 2.4, NonSI.DAY.inverse());
		Assert.assertTrue(CategoryMapperUtil.isAmountRanged(rangeOf));

		Amount<? extends Quantity> vauleOf = Amount.valueOf(2, NonSI.DAY.inverse());
		Assert.assertFalse(CategoryMapperUtil.isAmountRanged(vauleOf));
	}

	@Test
	public void testGetMostGeneralUnit()
	{
		List<Unit<?>> units = new ArrayList<>();

		units.add(NonSI.DAY.inverse());
		units.add(NonSI.YEAR.inverse());
		units.add(NonSI.MONTH.inverse());

		Unit<?> unit = CategoryMapperUtil.getMostGeneralUnit(units);
		Assert.assertEquals(unit.toString(), NonSI.YEAR.inverse().toString());
	}

	@Test
	public void testMatchUnit()
	{
		Unit<?> unit = CategoryMapperUtil.findDurationUnit("1-3 per month");
		Assert.assertEquals(unit.toString(), NonSI.MONTH.inverse().toString());
	}

	@Test
	public void testConvertDescriptionToAmount()
	{
		Assert.assertEquals(CategoryMapperUtil.convertDescriptionToAmount("1-3 per month"), AmountWrapper.create(
				Amount.rangeOf((double) 1, (double) 3, NonSI.MONTH.inverse()).to(NonSI.WEEK.inverse())));

		Assert.assertEquals(CategoryMapperUtil.convertDescriptionToAmount("once or twice per month"),
				AmountWrapper.create(
						Amount.rangeOf((double) 1, (double) 2, NonSI.MONTH.inverse()).to(NonSI.WEEK.inverse())));

		Assert.assertEquals(CategoryMapperUtil.convertDescriptionToAmount("daily"),
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(NonSI.WEEK.inverse())));

		Assert.assertEquals(CategoryMapperUtil.convertDescriptionToAmount("About once a week"),
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

		Assert.assertEquals(CategoryMapperUtil.convertDescriptionToAmount("several times a month"),
				AmountWrapper.create(Amount.rangeOf((double) 3,
						NonSI.DAY.inverse().getConverterTo(NonSI.MONTH.inverse()).convert((double) 1) - 1,
						NonSI.MONTH.inverse()).to(NonSI.WEEK.inverse()), false));
	}

	@Test
	public void testConvertWordToNumber()
	{
		Assert.assertEquals(CategoryMapperUtil.convertWordToNumber("one-3 per month"), "1 3 per month");
		Assert.assertEquals(CategoryMapperUtil.convertWordToNumber("once a week"), "1 a week");
	}

	@Test
	public void testIntegration()
	{
		String sourceCategory0 = "NoT this month";
		AmountWrapper amountSourceCategory0 = CategoryMapperUtil.convertDescriptionToAmount(sourceCategory0);
		Assert.assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory0.getAmount()));
		Assert.assertEquals(amountSourceCategory0.toString(),
				AmountWrapper.create(Amount.valueOf(0, NonSI.MONTH.inverse()).to(NonSI.WEEK.inverse())).toString());

		String sourceCategory1 = "never/less than 1 per month";
		AmountWrapper amountSourceCategory1 = CategoryMapperUtil.convertDescriptionToAmount(sourceCategory1);
		Assert.assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory1.getAmount()));
		Assert.assertEquals(amountSourceCategory1.toString(), AmountWrapper.create(
				Amount.rangeOf((double) 0, (double) 1, NonSI.MONTH.inverse()).to(NonSI.WEEK.inverse())).toString());

		String sourceCategory2 = "1-3 per month";
		AmountWrapper amountSourceCategory2 = CategoryMapperUtil.convertDescriptionToAmount(sourceCategory2);
		Assert.assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory2.getAmount()));
		Assert.assertEquals(amountSourceCategory2, AmountWrapper.create(
				Amount.rangeOf((double) 1, (double) 3, NonSI.MONTH.inverse()).to(NonSI.WEEK.inverse())));

		String sourceCategory3 = "once a week";
		AmountWrapper amountSourceCategory3 = CategoryMapperUtil.convertDescriptionToAmount(sourceCategory3);
		Assert.assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory3.getAmount()));
		Assert.assertEquals(amountSourceCategory3,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

		String sourceCategory4 = "2-4 per week";
		AmountWrapper amountSourceCategory4 = CategoryMapperUtil.convertDescriptionToAmount(sourceCategory4);
		Assert.assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory4.getAmount()));
		Assert.assertEquals(amountSourceCategory4,
				AmountWrapper.create(Amount.rangeOf((double) 2, (double) 4, NonSI.WEEK.inverse())));

		String sourceCategory5 = "5-6 per week";
		AmountWrapper amountSourceCategory5 = CategoryMapperUtil.convertDescriptionToAmount(sourceCategory5);
		Assert.assertTrue(CategoryMapperUtil.isAmountRanged(amountSourceCategory5.getAmount()));
		Assert.assertEquals(amountSourceCategory5,
				AmountWrapper.create(Amount.rangeOf((double) 5, (double) 6, NonSI.WEEK.inverse())));

		String sourceCategory6 = "once a day";
		AmountWrapper amountSourceCategory6 = CategoryMapperUtil.convertDescriptionToAmount(sourceCategory6);
		Assert.assertFalse(CategoryMapperUtil.isAmountRanged(amountSourceCategory6.getAmount()));
		Assert.assertEquals(amountSourceCategory6,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(NonSI.WEEK.inverse())));

		String targetCategory1 = "Almost daily + daily";
		AmountWrapper amountTargetCategory1 = CategoryMapperUtil.convertDescriptionToAmount(targetCategory1);
		Assert.assertFalse(CategoryMapperUtil.isAmountRanged(amountTargetCategory1.getAmount()));
		Assert.assertEquals(amountTargetCategory1,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.DAY.inverse()).to(NonSI.WEEK.inverse())));

		String targetCategory2 = "Several times a week";
		AmountWrapper amountTargetCategory2 = CategoryMapperUtil.convertDescriptionToAmount(targetCategory2);
		Assert.assertTrue(CategoryMapperUtil.isAmountRanged(amountTargetCategory2.getAmount()));
		Assert.assertEquals(amountTargetCategory2,
				AmountWrapper.create(Amount.rangeOf((double) 3, (double) 6, NonSI.WEEK.inverse()), false));

		String targetCategory3 = "About once a week";
		AmountWrapper amountTargetCategory3 = CategoryMapperUtil.convertDescriptionToAmount(targetCategory3);
		Assert.assertFalse(CategoryMapperUtil.isAmountRanged(amountTargetCategory3.getAmount()));
		Assert.assertEquals(amountTargetCategory3,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

		String targetCategory4 = "Never + fewer than once a week";
		AmountWrapper amountTargetCategory4 = CategoryMapperUtil.convertDescriptionToAmount(targetCategory4);
		Assert.assertTrue(CategoryMapperUtil.isAmountRanged(amountTargetCategory4.getAmount()));
		Assert.assertEquals(amountTargetCategory4,
				AmountWrapper.create(Amount.rangeOf((double) 0, (double) 1, NonSI.WEEK.inverse())));
	}
}
