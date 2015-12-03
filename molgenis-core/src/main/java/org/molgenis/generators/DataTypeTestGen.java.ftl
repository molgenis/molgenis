<#include "GeneratorHelper.ftl">
package ${package};

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/**
 * <#if JavaName(entity) == "RuntimeProperty">@deprecated replaced by setting classes that derive from {@link org.molgenis.data.settings.DefaultSettingsEntity}</#if>
 */
<#if JavaName(entity) == "RuntimeProperty">@Deprecated</#if>
public class ${JavaName(entity)}Test
{
	@Test
	public void equals_Self()
	{
		${JavaName(entity)} ${JavaName(entity)?lower_case} = new ${JavaName(entity)}();
		assertTrue(${JavaName(entity)?lower_case}.equals(${JavaName(entity)?lower_case}));
	}
	
	@Test
	public void equals_Other()
	{
		assertTrue(new ${JavaName(entity)}().equals(new ${JavaName(entity)}()));
	}
	
	@Test
	public void equals_Null()
	{
		assertFalse(new ${JavaName(entity)}().equals(null));
	}
	
	@Test
	public void hashCode_Other()
	{
		assertEquals(new ${JavaName(entity)}().hashCode(), new ${JavaName(entity)}().hashCode());
	}
}