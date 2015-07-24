package org.molgenis.data.mapper.categorymapper;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CategoryMapperTest
{
	FrequencyCategoryMapper categoryMapper = new FrequencyCategoryMapper();

	@Test
	public void testConvertCategory()
	{

		Amount<Duration> twicePerDayAmount = Amount.valueOf(2, NonSI.DAY);

		Amount<?> convertedAmount = categoryMapper.convertCategory(twicePerDayAmount, NonSI.WEEK, 2);

		System.out.println(convertedAmount.getEstimatedValue());
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
	public void testMatchUnit()
	{
		Unit<?> unit = DurationUnitConversionUtil.findDurationUnit("1-3 per month");
		Assert.assertEquals(unit.toString(), NonSI.MONTH.toString());
	}

	@Test
	public void testConvertDescriptionToAmount()
	{
		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("1-3 per month"),
				Amount.valueOf((double) 2, NonSI.MONTH));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("once or twice per month"),
				Amount.valueOf((double) 1.5, NonSI.MONTH));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("daily"), Amount.valueOf((double) 1, NonSI.DAY));

		Assert.assertEquals(categoryMapper.convertDescriptionToAmount("About once a week"),
				Amount.valueOf((double) 1, NonSI.WEEK));
	}

	@Test
	public void testConvertWordToNumber()
	{
		Assert.assertEquals(DurationUnitConversionUtil.convertWordToNumber("one-3 per month"), "1 3 per month");
		Assert.assertEquals(DurationUnitConversionUtil.convertWordToNumber("once a week"), "1 a week");
	}

	@Test
	public void testOrder()
	{
		Unit<?> monthUnit = NonSI.MONTH;
		Unit<?> dayUnit = NonSI.DAY;

		Assert.assertEquals(DurationUnitConversionUtil.getMoreSpecificUnit(NonSI.MONTH), NonSI.WEEK);
	}

	@Test
	public void f()
	{
		// Unit<?> unit = NonSI.MONTH;
		// UnitConverter converterTo = unit.getConverterTo(NonSI.DAY);
		// System.out.println(converterTo.convert(1));

		String sourceCategory1 = "never/less than 1 per month";

		Assert.assertTrue(DurationUnitConversionUtil.containsNegativeAdjectives(sourceCategory1));

		String sourceCategory2 = "1-3 per month";
		String sourceCategory3 = "once a week";
		String sourceCategory4 = "2-4 per week";
		String sourceCategory5 = "5-6 per week";
		String sourceCategory6 = "once a day";
		String sourceCategory7 = "2-3 per day";
		String sourceCategory8 = "4-5 per day";
		String sourceCategory9 = "6+ per day";

		String targetCategory1 = "Almost daily + daily";
		String targetCategory2 = "Several times a week";
		String targetCategory3 = "About once a week";
		String targetCategory4 = "Never + fewer than once a week";

		Unit<Duration> hour_unit = NonSI.HOUR;
		Unit<Duration> unit1 = NonSI.DAY;
		Unit<Duration> unit2 = NonSI.WEEK;

		Amount<?> hour = Amount.valueOf(1, unit2);

		System.out.println(hour.to(NonSI.DAY).getEstimatedValue());
	}
}
