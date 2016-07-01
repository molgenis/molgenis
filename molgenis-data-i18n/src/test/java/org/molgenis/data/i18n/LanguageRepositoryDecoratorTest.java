package org.molgenis.data.i18n;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.*;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.i18n.model.LanguageMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class LanguageRepositoryDecoratorTest
{
	@Mock
	private Repository<Language> decoratedRepo;
	@Mock
	private DataService dataService;
	@Mock
	private MetaDataService metaDataService;
	@Mock
	private RepositoryCollection defaultBackend;
	@Mock
	private LanguageMetaData languageMeta;
	private LanguageRepositoryDecorator languageRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		when(metaDataService.getDefaultBackend()).thenReturn(defaultBackend);
		when(dataService.getMeta()).thenReturn(metaDataService);
		EntityMetaData attrMetaMeta = mock(EntityMetaData.class);
		when(attrMetaMeta.getEntityMetaData()).thenReturn(mock(EntityMetaDataMetaData.class));
		when(dataService.getEntityMetaData(AttributeMetaDataMetaData.ATTRIBUTE_META_DATA)).thenReturn(attrMetaMeta);
		AttributeMetaDataFactory attrMetaFactory = mock(AttributeMetaDataFactory.class);
		when(attrMetaFactory.create()).thenAnswer(new Answer<AttributeMetaData>()
		{
			@Override
			public AttributeMetaData answer(InvocationOnMock invocation) throws Throwable
			{
				AttributeMetaData attrMeta = mock(AttributeMetaData.class);
				when(attrMeta.setName(anyString())).thenReturn(attrMeta);
				when(attrMeta.setNillable(anyBoolean())).thenReturn(attrMeta);
				return attrMeta;
			}
		});
		EntityMetaDataMetaData entityMetaMeta = mock(EntityMetaDataMetaData.class);
		I18nStringMetaData i18nStringMeta = mock(I18nStringMetaData.class);
		languageRepositoryDecorator = new LanguageRepositoryDecorator(decoratedRepo, dataService, attrMetaFactory,
				entityMetaMeta, i18nStringMeta);
	}

	@Test
	public void addStream()
	{
		Language language0 = mock(Language.class);
		when(language0.getEntityMetaData()).thenReturn(languageMeta);
		when(language0.getCode()).thenReturn("nl");

		Language language1 = mock(Language.class);
		when(language1.getEntityMetaData()).thenReturn(languageMeta);
		when(language1.getCode()).thenReturn("de");

		Stream<Language> entities = Arrays.asList(language0, language1).stream();
		assertEquals(languageRepositoryDecorator.add(entities), Integer.valueOf(2));
		verify(decoratedRepo, times(1)).add(language0);
		verify(decoratedRepo, times(1)).add(language1);
	}

	@Test
	public void deleteStream()
	{
		Language language0 = mock(Language.class);
		when(language0.getEntityMetaData()).thenReturn(languageMeta);
		when(language0.getCode()).thenReturn("nl");

		Language language1 = mock(Language.class);
		when(language1.getEntityMetaData()).thenReturn(languageMeta);
		when(language1.getCode()).thenReturn("de");

		languageRepositoryDecorator.delete(Stream.of(language0, language1));
		verify(decoratedRepo, times(1)).delete(language0);
		verify(decoratedRepo, times(1)).delete(language1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Language language0 = mock(Language.class);
		Stream<Language> entities = Stream.of(language0);
		ArgumentCaptor<Stream<Language>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepo).update(captor.capture());
		languageRepositoryDecorator.update(entities);
		assertEquals(captor.getValue().collect(Collectors.toList()), singletonList(language0));
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Language language0 = mock(Language.class);
		Language language1 = mock(Language.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds)).thenReturn(Stream.of(language0, language1));
		Stream<Language> expectedEntities = languageRepositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(language0, language1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Language language0 = mock(Language.class);
		Language language1 = mock(Language.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepo.findAll(entityIds, fetch)).thenReturn(Stream.of(language0, language1));
		Stream<Language> expectedEntities = languageRepositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(language0, language1));
	}

	@Test
	public void findAllAsStream()
	{
		Language language0 = mock(Language.class);
		Query<Language> query = mock(Query.class);
		when(decoratedRepo.findAll(query)).thenReturn(Stream.of(language0));
		Stream<Language> entities = languageRepositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), singletonList(language0));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<Language>> consumer = mock(Consumer.class);
		decoratedRepo.forEachBatched(fetch, consumer, 234);
		verify(decoratedRepo, times(1)).forEachBatched(fetch, consumer, 234);
	}
}
