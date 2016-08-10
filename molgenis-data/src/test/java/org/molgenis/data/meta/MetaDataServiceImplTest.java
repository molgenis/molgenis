package org.molgenis.data.meta;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.FULL_NAME;
import static org.testng.Assert.assertEquals;

public class MetaDataServiceImplTest
{
	private MetaDataServiceImpl metaDataServiceImpl;
	private DataService dataService;
	private RepositoryCollectionRegistry repoCollectionRegistry;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		repoCollectionRegistry = mock(RepositoryCollectionRegistry.class);
		SystemEntityMetaDataRegistry systemEntityMetaRegistry = mock(SystemEntityMetaDataRegistry.class);
		metaDataServiceImpl = new MetaDataServiceImpl(dataService, repoCollectionRegistry, systemEntityMetaRegistry);
	}

	@Test
	public void getLanguageCodes()
	{
		RepositoryCollection defaultRepoCollection = mock(RepositoryCollection.class);
		when(defaultRepoCollection.getLanguageCodes()).thenAnswer(new Answer<Stream<String>>()
		{
			@Override
			public Stream<String> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of("en", "nl");
			}
		});
		when(repoCollectionRegistry.getDefaultRepoCollection()).thenReturn(defaultRepoCollection);
		assertEquals(metaDataServiceImpl.getLanguageCodes().collect(toList()), asList("en", "nl"));
	}

	@Test
	public void updateEntityMeta()
	{
		String entityName = "entity";

		String attrShared0Name = "attrSame";
		String attrShared1Name = "attrUpdated";
		String attrAddedName = "attrAdded";
		String attrDeletedName = "attrDeleted";
		AttributeMetaData attrShared0 = when(mock(AttributeMetaData.class).getName()).thenReturn(attrShared0Name)
				.getMock();
		when(attrShared0.getAttributeParts()).thenReturn(emptyList());
		when(attrShared0.getTags()).thenReturn(emptyList());
		AttributeMetaData attrShared1 = when(mock(AttributeMetaData.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1.getLabel()).thenReturn("label");
		when(attrShared1.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1.getTags()).thenReturn(emptyList());
		AttributeMetaData attrShared1Updated = when(mock(AttributeMetaData.class).getName()).thenReturn(attrShared1Name)
				.getMock();
		when(attrShared1Updated.getLabel()).thenReturn("new label");
		when(attrShared1Updated.getAttributeParts()).thenReturn(emptyList());
		when(attrShared1Updated.getTags()).thenReturn(emptyList());
		AttributeMetaData attrAdded = when(mock(AttributeMetaData.class).getName()).thenReturn(attrAddedName).getMock();
		when(attrAdded.getAttributeParts()).thenReturn(emptyList());
		when(attrAdded.getTags()).thenReturn(emptyList());
		AttributeMetaData attrDeleted = when(mock(AttributeMetaData.class).getName()).thenReturn(attrDeletedName)
				.getMock();
		when(attrDeleted.getAttributeParts()).thenReturn(emptyList());
		when(attrDeleted.getTags()).thenReturn(emptyList());
		String attrDeletedIdentifier = "identifier";
		when(attrDeleted.getIdentifier()).thenReturn(attrDeletedIdentifier);

		EntityMetaData existingEntityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(existingEntityMeta.getLabel()).thenReturn("label");
		when(existingEntityMeta.getSimpleName()).thenReturn(entityName);
		when(existingEntityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1, attrDeleted));
		when(existingEntityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(existingEntityMeta.getTags()).thenReturn(emptyList());

		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		when(entityMeta.getLabel()).thenReturn("new label");
		when(entityMeta.getSimpleName()).thenReturn(entityName);
		when(entityMeta.getOwnAllAttributes()).thenReturn(newArrayList(attrShared0, attrShared1Updated, attrAdded));
		when(entityMeta.getOwnAttributes()).thenReturn(emptyList());
		when(entityMeta.getOwnLookupAttributes()).thenReturn(emptyList());
		when(entityMeta.getTags()).thenReturn(emptyList());

		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(existingEntityMeta);

		metaDataServiceImpl.updateEntityMeta(entityMeta);

		ArgumentCaptor<Stream<Entity>> attrAddCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).add(eq(ATTRIBUTE_META_DATA), attrAddCaptor.capture());
		assertEquals(attrAddCaptor.getValue().collect(toList()), singletonList(attrAdded));

		ArgumentCaptor<Stream<Entity>> attrUpdateCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).update(eq(ATTRIBUTE_META_DATA), attrUpdateCaptor.capture());
		assertEquals(attrUpdateCaptor.getValue().collect(toList()), singletonList(attrShared1Updated));

		verify(dataService).update(ENTITY_META_DATA, entityMeta);

		ArgumentCaptor<Stream<Object>> attrDeleteCaptor = ArgumentCaptor.forClass((Class) Stream.class);
		verify(dataService).deleteAll(eq(ATTRIBUTE_META_DATA), attrDeleteCaptor.capture());
		assertEquals(attrDeleteCaptor.getValue().collect(toList()), singletonList(attrDeletedIdentifier));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void updateEntityMetaEntityDoesNotExist()
	{
		String entityName = "entity";
		EntityMetaData entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn(entityName).getMock();
		Query<EntityMetaData> entityQ = mock(Query.class);
		when(dataService.query(ENTITY_META_DATA, EntityMetaData.class)).thenReturn(entityQ);
		when(entityQ.eq(FULL_NAME, entityName)).thenReturn(entityQ);
		when(entityQ.findOne()).thenReturn(null);
		metaDataServiceImpl.updateEntityMeta(entityMeta);
	}
}
