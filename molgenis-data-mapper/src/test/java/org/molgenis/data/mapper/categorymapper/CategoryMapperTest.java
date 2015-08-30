package org.molgenis.data.mapper.categorymapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class CategoryMapperTest
{
	FrequencyCategoryMapper categoryMapper = new FrequencyCategoryMapper();

	@Test
	public void testConvertCategory()
	{
		Amount<?> twicePerDayAmount = Amount.valueOf(2, NonSI.DAY.inverse());
		Amount<?> twicePerWeekAmount = Amount.valueOf(2, NonSI.WEEK.inverse());
		Assert.assertEquals(categoryMapper.convert(twicePerDayAmount, twicePerWeekAmount), (double) 12);

		Amount<?> twiceAtLeastPerWeek = Amount.rangeOf(2, 7, NonSI.WEEK.inverse());
		Amount<?> threeTimesPerWeek = Amount.valueOf(3, NonSI.WEEK.inverse());

		Assert.assertEquals(categoryMapper.convert(twiceAtLeastPerWeek, threeTimesPerWeek), 2.5);
	}

	@Test
	public void testExtractNumbers()
	{
		Assert.assertEquals(
				DurationUnitConversionUtil.extractNumbers("1-3 per month").stream()
						.mapToInt(doubleValue -> doubleValue.intValue()).sum(), 4);

		Assert.assertEquals(
				DurationUnitConversionUtil.extractNumbers("2.6-3.4 per month").stream()
						.mapToInt(doubleValue -> doubleValue.intValue()).sum(), 5);
	}

	@Test
	public void testIsAmountRanged()
	{
		Amount<? extends Quantity> rangeOf = Amount.rangeOf(2, 2.4, NonSI.DAY.inverse());
		Assert.assertTrue(DurationUnitConversionUtil.isAmountRanged(rangeOf));

		Amount<? extends Quantity> vauleOf = Amount.valueOf(2, NonSI.DAY.inverse());
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(vauleOf));
	}

	@Test
	public void testGetMostGeneralUnit()
	{
		List<Unit<?>> units = new ArrayList<Unit<?>>();

		units.add(NonSI.DAY.inverse());
		units.add(NonSI.YEAR.inverse());
		units.add(NonSI.MONTH.inverse());

		Unit<?> unit = DurationUnitConversionUtil.getMostGeneralUnit(units);
		Assert.assertEquals(unit.toString(), NonSI.YEAR.inverse().toString());
	}

	@Test
	public void testMatchUnit()
	{
		Unit<?> unit = DurationUnitConversionUtil.findDurationUnit("1-3 per month");
		Assert.assertEquals(unit.toString(), NonSI.MONTH.inverse().toString());
	}

	@Test
	public void testConvertDescriptionToAmount()
	{
		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("1-3 per month"),
				Amount.valueOf((double) 2, NonSI.MONTH.inverse()));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("once or twice per month"),
				Amount.valueOf((double) 1.5, NonSI.MONTH.inverse()));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("daily"),
				Amount.valueOf((double) 1, NonSI.DAY.inverse()));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("About once a week"),
				Amount.valueOf((double) 1, NonSI.WEEK.inverse()));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("several times a month"),
				Amount.rangeOf((double) 1, Double.MAX_VALUE, NonSI.MONTH.inverse()));
	}

	@Test
	public void testConvertWordToNumber()
	{
		Assert.assertEquals(DurationUnitConversionUtil.convertWordToNumber("one-3 per month"), "1 3 per month");
		Assert.assertEquals(DurationUnitConversionUtil.convertWordToNumber("once a week"), "1 a week");
	}

	@Test
	public void testIntegration()
	{
		String sourceCategory1 = "never/less than 1 per month";
		Amount<?> amountSourceCategory1 = categoryMapper.convertDescriptionToAmount(sourceCategory1);
		Assert.assertTrue(DurationUnitConversionUtil.isAmountRanged(amountSourceCategory1));
		Assert.assertEquals(amountSourceCategory1, Amount.rangeOf((double) 0, (double) 1, NonSI.MONTH.inverse()));

		String sourceCategory2 = "1-3 per month";
		Amount<?> amountSourceCategory2 = categoryMapper.convertDescriptionToAmount(sourceCategory2);
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(amountSourceCategory2));
		Assert.assertEquals(amountSourceCategory2, Amount.valueOf((double) 2, NonSI.MONTH.inverse()));

		String sourceCategory3 = "once a week";
		Amount<?> amountSourceCategory3 = categoryMapper.convertDescriptionToAmount(sourceCategory3);
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(amountSourceCategory3));
		Assert.assertEquals(amountSourceCategory3, Amount.valueOf((double) 1, NonSI.WEEK.inverse()));

		String sourceCategory4 = "2-4 per week";
		Amount<?> amountSourceCategory4 = categoryMapper.convertDescriptionToAmount(sourceCategory4);
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(amountSourceCategory4));
		Assert.assertEquals(amountSourceCategory4, Amount.valueOf((double) 3, NonSI.WEEK.inverse()));

		String sourceCategory5 = "5-6 per week";
		Amount<?> amountSourceCategory5 = categoryMapper.convertDescriptionToAmount(sourceCategory5);
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(amountSourceCategory5));
		Assert.assertEquals(amountSourceCategory5, Amount.valueOf((double) 5.5, NonSI.WEEK.inverse()));

		String sourceCategory6 = "once a day";
		Amount<?> amountSourceCategory6 = categoryMapper.convertDescriptionToAmount(sourceCategory6);
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(amountSourceCategory6));
		Assert.assertEquals(amountSourceCategory6, Amount.valueOf((double) 1, NonSI.DAY.inverse()));

		String sourceCategory7 = "2-3 per day";
		String sourceCategory8 = "4-5 per day";
		String sourceCategory9 = "6+ per day";

		String targetCategory1 = "Almost daily + daily";
		Amount<?> amountTargetCategory1 = categoryMapper.convertDescriptionToAmount(targetCategory1);
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(amountTargetCategory1));
		Assert.assertEquals(amountTargetCategory1, Amount.valueOf((double) 1, NonSI.DAY.inverse()));

		String targetCategory2 = "Several times a week";
		Amount<?> amountTargetCategory2 = categoryMapper.convertDescriptionToAmount(targetCategory2);
		Assert.assertTrue(DurationUnitConversionUtil.isAmountRanged(amountTargetCategory2));
		Assert.assertEquals(amountTargetCategory2, Amount.rangeOf((double) 1, Double.MAX_VALUE, NonSI.WEEK.inverse()));

		String targetCategory3 = "About once a week";
		Amount<?> amountTargetCategory3 = categoryMapper.convertDescriptionToAmount(targetCategory3);
		Assert.assertFalse(DurationUnitConversionUtil.isAmountRanged(amountTargetCategory3));
		Assert.assertEquals(amountTargetCategory3, Amount.valueOf((double) 1, NonSI.WEEK.inverse()));

		String targetCategory4 = "Never + fewer than once a week";
		Amount<?> amountTargetCategory4 = categoryMapper.convertDescriptionToAmount(targetCategory4);
		Assert.assertTrue(DurationUnitConversionUtil.isAmountRanged(amountTargetCategory4));
		Assert.assertEquals(amountTargetCategory4, Amount.rangeOf((double) 0, (double) 1, NonSI.WEEK.inverse()));

		HashSet<Amount<?>> newHashSet = Sets.<Amount<?>> newHashSet(amountTargetCategory1, amountTargetCategory2,
				amountTargetCategory3, amountTargetCategory4);

		Amount<?> findBestAmount1 = categoryMapper.findBestAmount(amountSourceCategory1, newHashSet);
		System.out.println("Source amount : " + amountSourceCategory1.getEstimatedValue() + " ("
				+ amountSourceCategory1.getUnit() + ")");
		System.out.println("Best standard amount : " + findBestAmount1.getEstimatedValue() + " ("
				+ findBestAmount1.getUnit() + ")");

		Amount<?> findBestAmount2 = categoryMapper.findBestAmount(amountSourceCategory2, newHashSet);
		System.out.println("Source amount : " + amountSourceCategory2.getEstimatedValue() + " ("
				+ amountSourceCategory2.getUnit() + ")");
		System.out.println("Best standard amount : " + findBestAmount2.getEstimatedValue() + " ("
				+ findBestAmount2.getUnit() + ")");

		Amount<?> findBestAmount3 = categoryMapper.findBestAmount(amountSourceCategory3, newHashSet);
		System.out.println("Source amount : " + amountSourceCategory3.getEstimatedValue() + " ("
				+ amountSourceCategory3.getUnit() + ")");
		System.out.println("Best standard amount : " + findBestAmount3.getEstimatedValue() + " ("
				+ findBestAmount3.getUnit() + ")");

		// Several times a day --> 30 times a week
		// Several times a week --> 1 between 7 days
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
