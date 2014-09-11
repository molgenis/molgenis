package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LazyReferenceEntityTest
{
	private LazyReferenceEntity lazyReferenceEntity;
	private Entity entity;
	private String idAttributeName = "idAttribute";
	private String otherAttributeName = "otherAttribute";
	private Object idValue = "1";

	@BeforeMethod
	public void setUp()
	{
		String entityName = "entity";
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(entityName);
		entityMetaData.addAttribute(idAttributeName).setIdAttribute(true);
		entityMetaData.addAttribute(otherAttributeName);
		DataService dataService = mock(DataService.class);
		entity = mock(Entity.class);
		when(dataService.findOne(entityName, idValue)).thenReturn(entity);
		lazyReferenceEntity = new LazyReferenceEntity(idValue, entityMetaData, dataService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void LazyReferenceEntity()
	{
		new LazyReferenceEntity(null, null, null);
	}

	@Test
	public void getIdAttribute()
	{
		assertEquals(lazyReferenceEntity.get(idAttributeName), idValue);
		verifyZeroInteractions(entity);
	}

	@Test
	public void getIdValue()
	{
		assertEquals(lazyReferenceEntity.getIdValue(), idValue);
		verifyZeroInteractions(entity);
	}
}
