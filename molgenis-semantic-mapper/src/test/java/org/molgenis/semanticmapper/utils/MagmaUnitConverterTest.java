package org.molgenis.semanticmapper.utils;

import com.google.common.collect.Sets;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.measure.unit.Unit;
import java.util.Set;

public class MagmaUnitConverterTest
{
	MagmaUnitConverter unitConverter = new MagmaUnitConverter();

	@Test
	public void convertUnit()
	{
		Assert.assertEquals(unitConverter.convertUnit(Unit.valueOf("kg"), Unit.valueOf("g")), ".div(1000.0)");
		Assert.assertEquals(unitConverter.convertUnit(Unit.valueOf("m"), Unit.valueOf("cm")), ".div(100.0)");
		Assert.assertEquals(unitConverter.convertUnit(Unit.valueOf("kg/m²"), Unit.valueOf("g")), ".div(1000.0)");
		Assert.assertEquals(unitConverter.convertUnit(Unit.valueOf("kg/m²"), Unit.valueOf("cm")), ".div(100.0)");
	}

	@Test
	public void findCompositeUnitNames()
	{
		Set<String> findCompositeUnitNames = unitConverter.findCompositeUnitNames("kg/m²");
		Assert.assertTrue(Sets.newHashSet("kg", "m", "kg/m²").containsAll(findCompositeUnitNames));
	}
}
