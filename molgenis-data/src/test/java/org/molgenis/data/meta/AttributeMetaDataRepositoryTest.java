package org.molgenis.data.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.testng.annotations.Test;

public class AttributeMetaDataRepositoryTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void AttributeMetaDataRepository()
	{
		new AttributeMetaDataRepository(null);
	}

	@Test
	public void addAttributeMetaData()
	{
		ManageableRepositoryCollection repoCollection = mock(ManageableRepositoryCollection.class);
		Repository repo = mock(Repository.class);
		when(repoCollection.addEntityMeta(AttributeMetaDataRepository.META_DATA)).thenReturn(repo);
		AttributeMetaDataRepository attributeMetaDataRepository = new AttributeMetaDataRepository(repoCollection);
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		Entity attrEntity0 = attributeMetaDataRepository.add(attr0);
		assertEquals(attrEntity0.getString(AttributeMetaDataMetaData.NAME), "attr0");
		verify(repo, times(1)).add(Arrays.asList(attrEntity0));
	}

	@Test
	public void addIterableAttributeMetaData()
	{
		ManageableRepositoryCollection repoCollection = mock(ManageableRepositoryCollection.class);
		Repository repo = mock(Repository.class);
		when(repoCollection.addEntityMeta(AttributeMetaDataRepository.META_DATA)).thenReturn(repo);
		AttributeMetaDataRepository attributeMetaDataRepository = new AttributeMetaDataRepository(repoCollection);

		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);

		AttributeMetaData attr1 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr1").getMock();
		when(attr1.getDataType()).thenReturn(STRING);

		Iterable<Entity> attrEntities = attributeMetaDataRepository.add(Arrays.asList(attr0, attr1));
		Iterator<Entity> it = attrEntities.iterator();
		Entity attrEntity0 = it.next();
		assertEquals(attrEntity0.getString(AttributeMetaDataMetaData.NAME), "attr0");
		Entity attrEntity1 = it.next();
		assertEquals(attrEntity1.getString(AttributeMetaDataMetaData.NAME), "attr1");
		verify(repo, times(1)).add(Arrays.asList(attrEntity0, attrEntity1));
	}
}
