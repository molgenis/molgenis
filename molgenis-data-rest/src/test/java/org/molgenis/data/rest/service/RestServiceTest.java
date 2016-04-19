package org.molgenis.data.rest.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IdGenerator;
import org.molgenis.file.FileStore;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RestServiceTest
{
	private RestService restService;
	private DataService dataService;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		dataService = mock(DataService.class);
		IdGenerator idGenerator = mock(IdGenerator.class);
		FileStore fileStore = mock(FileStore.class);
		this.restService = new RestService(dataService, idGenerator, fileStore);
	}

	@Test
	public void toEntityValue()
	{
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(MREF);
		assertEquals(restService.toEntityValue(attr, null), emptyList());
	}

	// https://github.com/molgenis/molgenis/issues/4725
	@Test
	public void toEntityValueMrefToIntAttr()
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		String refEntityName = "refEntity";
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(INT);
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(dataService.findAll(eq(refEntityName), argThat(new ArgumentMatcher<Stream<Object>>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object argument)
			{
				return ((Stream<Object>) argument).collect(toList()).equals(Arrays.asList(0, 1)); // integers
			}
		}))).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(entity0, entity1);
			}
		});
		Object entityValue = restService.toEntityValue(attr, "0,1"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}

	@Test
	public void toEntityValueMrefToStringAttr()
	{
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		String refEntityName = "refEntity";
		AttributeMetaData refIdAttr = mock(AttributeMetaData.class);
		when(refIdAttr.getDataType()).thenReturn(STRING);
		EntityMetaData refEntityMeta = mock(EntityMetaData.class);
		when(refEntityMeta.getName()).thenReturn(refEntityName);
		when(refEntityMeta.getIdAttribute()).thenReturn(refIdAttr);
		AttributeMetaData attr = mock(AttributeMetaData.class);
		when(attr.getDataType()).thenReturn(MREF);
		when(attr.getRefEntity()).thenReturn(refEntityMeta);
		when(dataService.findAll(eq(refEntityName), argThat(new ArgumentMatcher<Stream<Object>>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object argument)
			{
				return ((Stream<Object>) argument).collect(toList()).equals(Arrays.asList("0", "1")); // strings
			}
		}))).thenAnswer(new Answer<Stream<Entity>>()
		{
			@Override
			public Stream<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return Stream.of(entity0, entity1);
			}
		});
		Object entityValue = restService.toEntityValue(attr, "0,1"); // string
		assertEquals(entityValue, Arrays.asList(entity0, entity1));
	}
}
