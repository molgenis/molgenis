package org.molgenis.data.meta;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.LanguageService;
import org.testng.annotations.Test;

public class AttributeMetaDataRepositoryTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void AttributeMetaDataRepository()
	{
		new AttributeMetaDataRepository(null, null);
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Test
	public void addAttributeMetaData()
	{
		ManageableRepositoryCollection repoCollection = mock(ManageableRepositoryCollection.class);
		Repository repo = mock(Repository.class);
		LanguageService languageService = mock(LanguageService.class);
		when(repoCollection.addEntityMeta(AttributeMetaDataRepository.META_DATA)).thenReturn(repo);
		AttributeMetaDataRepository attributeMetaDataRepository = new AttributeMetaDataRepository(repoCollection,
				languageService);
		AttributeMetaData attr0 = when(mock(AttributeMetaData.class).getName()).thenReturn("attr0").getMock();
		when(attr0.getDataType()).thenReturn(STRING);
		Entity attrEntity0 = attributeMetaDataRepository.add(attr0);
		assertEquals(attrEntity0.getString(AttributeMetaDataMetaData.NAME), "attr0");
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(repo, times(1)).add(captor.capture());
		assertEquals(captor.getValue().collect(toList()), Arrays.asList(attrEntity0));
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Test
	public void addIterableAttributeMetaData()
	{
		ManageableRepositoryCollection repoCollection = mock(ManageableRepositoryCollection.class);
		Repository repo = mock(Repository.class);
		LanguageService languageService = mock(LanguageService.class);
		when(repoCollection.addEntityMeta(AttributeMetaDataRepository.META_DATA)).thenReturn(repo);
		AttributeMetaDataRepository attributeMetaDataRepository = new AttributeMetaDataRepository(repoCollection,
				languageService);

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
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(repo, times(1)).add(captor.capture());
		assertEquals(captor.getValue().collect(toList()), Arrays.asList(attrEntity0, attrEntity1));
	}
}
