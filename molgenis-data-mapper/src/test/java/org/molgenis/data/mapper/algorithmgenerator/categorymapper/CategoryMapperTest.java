package org.molgenis.data.mapper.algorithmgenerator.categorymapper;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.molgenis.data.mapper.algorithmgenerator.bean.AmountWrapper;
import org.molgenis.data.mapper.algorithmgenerator.categorymapper.FrequencyCategoryMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CategoryMapperTest
{
	FrequencyCategoryMapper categoryMapper = new FrequencyCategoryMapper();

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
		Assert.assertEquals(
				FrequencyCategoryMapperUtil.extractNumbers("1-3 per month").stream()
						.mapToInt(doubleValue -> doubleValue.intValue()).sum(), 4);

		Assert.assertEquals(
				FrequencyCategoryMapperUtil.extractNumbers("2.6-3.4 per month").stream()
						.mapToInt(doubleValue -> doubleValue.intValue()).sum(), 5);
	}

	@Test
	public void testIsAmountRanged()
	{
		Amount<? extends Quantity> rangeOf = Amount.rangeOf(2, 2.4, NonSI.DAY.inverse());
		Assert.assertTrue(FrequencyCategoryMapperUtil.isAmountRanged(rangeOf));

		Amount<? extends Quantity> vauleOf = Amount.valueOf(2, NonSI.DAY.inverse());
		Assert.assertFalse(FrequencyCategoryMapperUtil.isAmountRanged(vauleOf));
	}

	@Test
	public void testGetMostGeneralUnit()
	{
		List<Unit<?>> units = new ArrayList<Unit<?>>();

		units.add(NonSI.DAY.inverse());
		units.add(NonSI.YEAR.inverse());
		units.add(NonSI.MONTH.inverse());

		Unit<?> unit = FrequencyCategoryMapperUtil.getMostGeneralUnit(units);
		Assert.assertEquals(unit.toString(), NonSI.YEAR.inverse().toString());
	}

	@Test
	public void testMatchUnit()
	{
		Unit<?> unit = FrequencyCategoryMapperUtil.findDurationUnit("1-3 per month");
		Assert.assertEquals(unit.toString(), NonSI.MONTH.inverse().toString());
	}

	@Test
	public void testConvertDescriptionToAmount()
	{
		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("1-3 per month"),
				AmountWrapper.create(Amount.rangeOf((double) 1, (double) 3, NonSI.MONTH.inverse())));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("once or twice per month"),
				AmountWrapper.create(Amount.rangeOf((double) 1, (double) 2, NonSI.MONTH.inverse())));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("daily"),
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.DAY.inverse())));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("About once a week"),
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

		Assert.assertEquals(
				categoryMapper.convertDescriptionToAmount("several times a month"),
				AmountWrapper.create(Amount.rangeOf((double) 1,
						NonSI.DAY.inverse().getConverterTo(NonSI.MONTH.inverse()).convert((double) 1),
						NonSI.MONTH.inverse()), false));
	}

	@Test
	public void testConvertWordToNumber()
	{
		Assert.assertEquals(FrequencyCategoryMapperUtil.convertWordToNumber("one-3 per month"), "1 3 per month");
		Assert.assertEquals(FrequencyCategoryMapperUtil.convertWordToNumber("once a week"), "1 a week");
	}

	@Test
	public void testIntegration()
	{
		String sourceCategory1 = "never/less than 1 per month";
		AmountWrapper amountSourceCategory1 = categoryMapper.convertDescriptionToAmount(sourceCategory1);
		Assert.assertTrue(FrequencyCategoryMapperUtil.isAmountRanged(amountSourceCategory1.getAmount()));
		Assert.assertEquals(amountSourceCategory1,
				AmountWrapper.create(Amount.rangeOf((double) 0, (double) 1, NonSI.MONTH.inverse())));

		String sourceCategory2 = "1-3 per month";
		AmountWrapper amountSourceCategory2 = categoryMapper.convertDescriptionToAmount(sourceCategory2);
		Assert.assertTrue(FrequencyCategoryMapperUtil.isAmountRanged(amountSourceCategory2.getAmount()));
		Assert.assertEquals(amountSourceCategory2,
				AmountWrapper.create(Amount.rangeOf((double) 1, (double) 3, NonSI.MONTH.inverse())));

		String sourceCategory3 = "once a week";
		AmountWrapper amountSourceCategory3 = categoryMapper.convertDescriptionToAmount(sourceCategory3);
		Assert.assertFalse(FrequencyCategoryMapperUtil.isAmountRanged(amountSourceCategory3.getAmount()));
		Assert.assertEquals(amountSourceCategory3,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

		String sourceCategory4 = "2-4 per week";
		AmountWrapper amountSourceCategory4 = categoryMapper.convertDescriptionToAmount(sourceCategory4);
		Assert.assertTrue(FrequencyCategoryMapperUtil.isAmountRanged(amountSourceCategory4.getAmount()));
		Assert.assertEquals(amountSourceCategory4,
				AmountWrapper.create(Amount.rangeOf((double) 2, (double) 4, NonSI.WEEK.inverse())));

		String sourceCategory5 = "5-6 per week";
		AmountWrapper amountSourceCategory5 = categoryMapper.convertDescriptionToAmount(sourceCategory5);
		Assert.assertTrue(FrequencyCategoryMapperUtil.isAmountRanged(amountSourceCategory5.getAmount()));
		Assert.assertEquals(amountSourceCategory5,
				AmountWrapper.create(Amount.rangeOf((double) 5, (double) 6, NonSI.WEEK.inverse())));

		String sourceCategory6 = "once a day";
		AmountWrapper amountSourceCategory6 = categoryMapper.convertDescriptionToAmount(sourceCategory6);
		Assert.assertFalse(FrequencyCategoryMapperUtil.isAmountRanged(amountSourceCategory6.getAmount()));
		Assert.assertEquals(amountSourceCategory6,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.DAY.inverse())));

		String targetCategory1 = "Almost daily + daily";
		AmountWrapper amountTargetCategory1 = categoryMapper.convertDescriptionToAmount(targetCategory1);
		Assert.assertFalse(FrequencyCategoryMapperUtil.isAmountRanged(amountTargetCategory1.getAmount()));
		Assert.assertEquals(amountTargetCategory1,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.DAY.inverse())));

		String targetCategory2 = "Several times a week";
		AmountWrapper amountTargetCategory2 = categoryMapper.convertDescriptionToAmount(targetCategory2);
		Assert.assertTrue(FrequencyCategoryMapperUtil.isAmountRanged(amountTargetCategory2.getAmount()));
		Assert.assertEquals(amountTargetCategory2,
				AmountWrapper.create(Amount.rangeOf((double) 1, (double) 7, NonSI.WEEK.inverse()), false));

		String targetCategory3 = "About once a week";
		AmountWrapper amountTargetCategory3 = categoryMapper.convertDescriptionToAmount(targetCategory3);
		Assert.assertFalse(FrequencyCategoryMapperUtil.isAmountRanged(amountTargetCategory3.getAmount()));
		Assert.assertEquals(amountTargetCategory3,
				AmountWrapper.create(Amount.valueOf((double) 1, NonSI.WEEK.inverse())));

		String targetCategory4 = "Never + fewer than once a week";
		AmountWrapper amountTargetCategory4 = categoryMapper.convertDescriptionToAmount(targetCategory4);
		Assert.assertTrue(FrequencyCategoryMapperUtil.isAmountRanged(amountTargetCategory4.getAmount()));
		Assert.assertEquals(amountTargetCategory4,
				AmountWrapper.create(Amount.rangeOf((double) 0, (double) 1, NonSI.WEEK.inverse())));
	}

	@Test
	public void testConvertFactor()
	{
		double convertFactor = categoryMapper.convertFactor(
				Amount.rangeOf((double) 1, (double) 6, NonSI.DAY.inverse()),
				Amount.valueOf((double) 3, NonSI.DAY.inverse()));

		double convertFactor2 = categoryMapper.convertFactor(
				Amount.rangeOf((double) 1, (double) 6, NonSI.DAY.inverse()),
				Amount.valueOf((double) 4, NonSI.DAY.inverse()));

		double convertFactor3 = categoryMapper.convertFactor(
				Amount.rangeOf((double) 1, (double) 6, NonSI.DAY.inverse()),
				Amount.rangeOf((double) 4, (double) 5, NonSI.DAY.inverse()));

		System.out.println(convertFactor);
		System.out.println(convertFactor2);
		System.out.println(convertFactor3);
	}
}
