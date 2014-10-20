package org.molgenis.data.meta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.UUID;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.semantic.Ontology;
import org.molgenis.data.semantic.OntologyTerm;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.IdGenerator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = TagRepositoryTest.Config.class)
public class TagRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private CrudRepository repository;

	private TagRepository tagRepository;

	@Autowired
	private IdGenerator idGenerator;

	@Autowired
	private AttributeMetaData attributeMetaData;

	@Autowired
	private ManageableCrudRepositoryCollection manageableCrudRepositoryCollection;

	private UUID uuid = UUID.randomUUID();

	@BeforeMethod
	public void beforeMethod()
	{
		when(manageableCrudRepositoryCollection.add(new TagMetaData())).thenReturn(repository);
		tagRepository = new TagRepository(manageableCrudRepositoryCollection, idGenerator);
		when(idGenerator.generateId()).thenReturn(uuid);
	}

	@Test
	public void testGetTag()
	{

		MapEntity expected = new MapEntity(TagMetaData.ENTITY_NAME);
		expected.set(TagMetaData.IDENTIFIER, uuid);
		expected.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_3031");
		expected.set(TagMetaData.LABEL, "Core data");
		expected.set(TagMetaData.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf");
		expected.set(TagMetaData.RELATION_LABEL, "instanceOf");
		expected.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		assertEquals(tagRepository.getTagEntity("http://edamontology.org/data_3031", "Core data", Relation.instanceOf,
				"http://edamontology.org"), expected);

		when(
				repository.findOne(new QueryImpl().eq(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_3031")
						.and().eq(TagMetaData.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf").and()
						.eq(TagMetaData.CODE_SYSTEM, "http://edamontology.org"))).thenReturn(expected);

		assertEquals(tagRepository.getTagEntity("http://edamontology.org/data_3031", "Core data", Relation.instanceOf,
				"http://edamontology.org"), expected);

		verify(repository, times(1)).add(expected);
	}

	@Test
	public void testGetTagTyped()
	{
		MapEntity expected = new MapEntity(TagMetaData.ENTITY_NAME);
		expected.set(TagMetaData.IDENTIFIER, uuid);
		expected.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_3031");
		expected.set(TagMetaData.LABEL, "Core data");
		expected.set(TagMetaData.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf");
		expected.set(TagMetaData.RELATION_LABEL, "instanceOf");
		expected.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		OntologyTerm coreData = mock(OntologyTerm.class);

		when(coreData.getIRI()).thenReturn("http://edamontology.org/data_3031");
		when(coreData.getLabel()).thenReturn("Core data");

		Ontology edamOntology = mock(Ontology.class);

		when(edamOntology.getIri()).thenReturn("http://edamontology.org");

		Tag<?, OntologyTerm, Ontology> tag = new TagImpl<Object, OntologyTerm, Ontology>(null, Relation.instanceOf,
				coreData, edamOntology);

		assertEquals(tagRepository.getTagEntity(tag), expected);

		when(
				repository.findOne(new QueryImpl().eq(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_3031")
						.and().eq(TagMetaData.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf").and()
						.eq(TagMetaData.CODE_SYSTEM, "http://edamontology.org"))).thenReturn(expected);

		assertEquals(tagRepository.getTagEntity(tag), expected);

		verify(repository, times(1)).add(expected);
	}

	@Configuration
	public static class Config
	{
		@Bean
		ManageableCrudRepositoryCollection manageableCrudRepositoryCollection()
		{
			return mock(ManageableCrudRepositoryCollection.class);
		}

		@Bean
		CrudRepository repository()
		{
			return mock(CrudRepository.class);
		}

		@Bean
		IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		AttributeMetaData attributeMetaData()
		{
			return mock(AttributeMetaData.class);
		}

	}
}
