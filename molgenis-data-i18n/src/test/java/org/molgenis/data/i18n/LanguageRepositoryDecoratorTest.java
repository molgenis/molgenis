package org.molgenis.data.i18n;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.*;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.i18n.model.LanguageMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { LanguageRepositoryDecoratorTest.Config.class })
public class LanguageRepositoryDecoratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private AttributeMetaDataMetaData attrMetaMeta;

	@Autowired
	private EntityMetaDataMetaData entityMetaMeta;

	@Autowired
	private I18nStringMetaData i18nStringMetaData;

	@Autowired
	private LanguageFactory languageFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

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
		when(dataService.getEntityMetaData(ENTITY_META_DATA)).thenReturn(entityMetaMeta);
		when(dataService.getEntityMetaData(ATTRIBUTE_META_DATA)).thenReturn(attrMetaMeta);
		when(dataService.getEntityMetaData(I18N_STRING)).thenReturn(i18nStringMetaData);
		languageRepositoryDecorator = new LanguageRepositoryDecorator(decoratedRepo, dataService, attrMetaFactory,
				entityMetaMeta, i18nStringMetaData);
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
		String nl = "nl";
		String de = "de";

		// Add NL to language
		Language languageNL = languageFactory.create(nl, "Nederlands");

		// Add DE to language
		Language languageDE = languageFactory.create(de, "Deutsch");

		// Add NL attribute to i18n
		i18nStringMetaData.addAttribute(attrMetaFactory.create().setName(nl));

		// Add DE attributes to i18n
		i18nStringMetaData.addAttribute(attrMetaFactory.create().setName(de));

		// Add language NL attributes for entity meta data
		Attribute entityLabelNL = attrMetaFactory.create().setName(EntityMetaDataMetaData.LABEL + '-' + nl);
		Attribute entityDescriptionNL = attrMetaFactory.create()
				.setName(EntityMetaDataMetaData.DESCRIPTION + '-' + nl);
		entityMetaMeta.addAttribute(entityLabelNL);
		entityMetaMeta.addAttribute(entityDescriptionNL);

		// Add language DE attributes for entity meta data
		Attribute entityLabelDE = attrMetaFactory.create().setName(EntityMetaDataMetaData.LABEL + '-' + de);
		Attribute entityDescriptionDE = attrMetaFactory.create()
				.setName(EntityMetaDataMetaData.DESCRIPTION + '-' + de);
		entityMetaMeta.addAttribute(entityLabelDE);
		entityMetaMeta.addAttribute(entityDescriptionDE);

		// Add language NL attributes for attribute meta data
		Attribute attributeLabelNL = attrMetaFactory.create()
				.setName(AttributeMetaDataMetaData.LABEL + '-' + nl);
		Attribute attributeDescriptionNL = attrMetaFactory.create()
				.setName(AttributeMetaDataMetaData.DESCRIPTION + '-' + nl);
		attrMetaMeta.addAttribute(attributeLabelNL);
		attrMetaMeta.addAttribute(attributeDescriptionNL);

		// Add language DE attributes for attribute meta data
		Attribute attributeLabelDE = attrMetaFactory.create()
				.setName(AttributeMetaDataMetaData.LABEL + '-' + de);
		Attribute attributeDescriptionDE = attrMetaFactory.create()
				.setName(AttributeMetaDataMetaData.DESCRIPTION + '-' + de);
		attrMetaMeta.addAttribute(attributeLabelDE);
		attrMetaMeta.addAttribute(attributeDescriptionDE);

		languageRepositoryDecorator.delete(Stream.of(languageNL, languageDE));

		verify(decoratedRepo, times(1)).delete(languageNL);
		verify(decoratedRepo, times(1)).delete(languageDE);
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

	@Configuration
	@ComponentScan({ "org.molgenis.data.i18n.model" })
	public static class Config
	{

	}
}
