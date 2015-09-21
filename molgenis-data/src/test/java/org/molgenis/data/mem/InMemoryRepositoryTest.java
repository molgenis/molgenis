package org.molgenis.data.mem;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.Test;

public class InMemoryRepositoryTest
{
	@Test
	public void findAll() throws IOException
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute("id").setIdAttribute(true);
		InMemoryRepository inMemoryRepository = new InMemoryRepository(entityMetaData);
		try
		{
			Entity entity = new MapEntity(entityMetaData);
			entity.set("id", "0");
			inMemoryRepository.add(entity);
			assertEquals(inMemoryRepository.findAll(new QueryImpl()), Arrays.asList(entity));
		}
		finally
		{
			inMemoryRepository.close();
		}
	}
}
