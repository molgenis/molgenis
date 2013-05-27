<#include "GeneratorHelper.ftl">
package ${package};

import org.mockito.Mockito;
import org.molgenis.framework.db.Database;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class ${JavaName(entity)}JpaMapperTest
{
	@Test
	public void createList()
	{
		Database database = Mockito.mock(Database.class);
		${JavaName(entity)}JpaMapper ${JavaName(entity)?uncap_first}JpaMapper = new ${JavaName(entity)}JpaMapper(database);
		assertTrue(${JavaName(entity)?uncap_first}JpaMapper.createList(1).isEmpty());
		assertTrue(${JavaName(entity)?uncap_first}JpaMapper.createList(2).isEmpty());
		assertTrue(${JavaName(entity)?uncap_first}JpaMapper.createList(3).isEmpty());
	}
}