package org.molgenis.data.semantic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.Ontology;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.OntologyTerm;
import org.molgenis.ontology.beans.OntologyImpl;
import org.molgenis.ontology.beans.OntologyTermImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = OntologyTagServiceTest.Config.class)
public class OntologyTagServiceTest extends AbstractTestNGSpringContextTests
{
	private OntologyTagService ontologyTagService;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private DataService dataService;

	private MapEntity chromosomeNameTagEntity;

	private MapEntity geneAnnotationTagEntity;

	private final Relation instanceOf = Relation.valueOf("instanceOf");

	public static final Ontology EDAM_ONTOLOGY = new OntologyImpl("EDAM", "http://edamontology.org",
			"The EDAM ontology.");

	public static final OntologyTerm CHROMOSOME_NAME_ONTOLOGY_TERM = new OntologyTermImpl(
			"http://edamontology.org/data_0987", "Chromosome name", "Name of a chromosome.", "data_0987", EDAM_ONTOLOGY);

	public static final OntologyTerm GENE_ANNOTATION_ONTOLOGY_TERM = new OntologyTermImpl(
			"http://edamontology.org/data_0919", "Gene annotation (chromosome)",
			"This includes basic information. e.g. chromosome number...", "data_0919", EDAM_ONTOLOGY);

	@BeforeTest
	public void beforeTest()
	{
		chromosomeNameTagEntity = new MapEntity();
		chromosomeNameTagEntity.set(TagMetaData.IDENTIFIER, "1234");
		chromosomeNameTagEntity.set(TagMetaData.LABEL, "Chromosome name");
		chromosomeNameTagEntity.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_0987");
		chromosomeNameTagEntity.set(TagMetaData.RELATION_IRI, instanceOf.getIRI());
		chromosomeNameTagEntity.set(TagMetaData.RELATION_LABEL, instanceOf.getLabel());
		chromosomeNameTagEntity.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		geneAnnotationTagEntity = new MapEntity();
		geneAnnotationTagEntity.set(TagMetaData.IDENTIFIER, "4321");
		geneAnnotationTagEntity.set(TagMetaData.LABEL, "Gene annotation (chromosome)");
		geneAnnotationTagEntity.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_0919");
		geneAnnotationTagEntity.set(TagMetaData.RELATION_IRI, instanceOf.getIRI());
		geneAnnotationTagEntity.set(TagMetaData.RELATION_LABEL, instanceOf.getLabel());
		geneAnnotationTagEntity.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");
	}

	@BeforeMethod
	public void beforeMethod()
	{
		ontologyTagService = new OntologyTagService(dataService, ontologyService, tagRepository);
	}

	@Test
	public void testGetTags()
	{
		EntityMetaData emd = new DefaultEntityMetaData("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = new DefaultAttributeMetaData("Chr");

		Relation instanceOf = Relation.valueOf("instanceOf");

		MapEntity attributeEntity = new MapEntity();
		attributeEntity.set(AttributeMetaDataMetaData.TAGS,
				Arrays.asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
		attributeEntity.set(AttributeMetaDataMetaData.NAME, "Chr");
		Entity entityMetaDataEntity = new MapEntity(new EntityMetaDataMetaData());
		entityMetaDataEntity.set(EntityMetaDataMetaData.ATTRIBUTES, Collections.singleton(attributeEntity));
		when(dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME, "org.molgenis.SNP")).thenReturn(
				entityMetaDataEntity);

		Ontology edamOntology = new OntologyImpl("EDAM", "http://edamontology.org", "The EDAM ontology.");
		OntologyTerm chromosomeName = new OntologyTermImpl("http://edamontology.org/data_0987", "Chromosome name",
				"Name of a chromosome.", "data_0987", edamOntology);
		OntologyTerm geneAnnotation = new OntologyTermImpl("http://edamontology.org/data_0919",
				"Gene annotation (chromosome)", "This includes basic information. e.g. chromosome number...",
				"data_0919", edamOntology);

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(edamOntology);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987", "http://edamontology.org"))
				.thenReturn(chromosomeName);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0919", "http://edamontology.org"))
				.thenReturn(geneAnnotation);

		Tag<AttributeMetaData, OntologyTerm, Ontology> chromosomeTag = new TagImpl<AttributeMetaData, OntologyTerm, Ontology>(
				"1234", attributeMetaData, instanceOf, chromosomeName, edamOntology);
		Tag<AttributeMetaData, OntologyTerm, Ontology> geneAnnotationTag = new TagImpl<AttributeMetaData, OntologyTerm, Ontology>(
				"4321", attributeMetaData, instanceOf, geneAnnotation, edamOntology);

		assertEquals(ontologyTagService.getTagsForAttribute(emd, attributeMetaData),
				Arrays.asList(chromosomeTag, geneAnnotationTag));
	}

	@Test
	public void testGetTagTyped()
	{
		MapEntity expected = new MapEntity(TagMetaData.ENTITY_NAME);
		expected.set(TagMetaData.IDENTIFIER, "1233");
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

		Tag<Object, OntologyTerm, Ontology> tag = new TagImpl<Object, OntologyTerm, Ontology>("1233", null,
				Relation.instanceOf, coreData, edamOntology);
		when(
				tagRepository.getTagEntity("http://edamontology.org/data_3031", "Core data", Relation.instanceOf,
						"http://edamontology.org")).thenReturn(expected);

		assertEquals(ontologyTagService.getTagEntity(tag), expected);

	}

	@Test
	public void testAddTag()
	{
		EntityMetaData emd = new DefaultEntityMetaData("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = new DefaultAttributeMetaData("Chr");

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(EDAM_ONTOLOGY);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987", "http://edamontology.org"))
				.thenReturn(CHROMOSOME_NAME_ONTOLOGY_TERM);

		MapEntity attributeEntity = new MapEntity();
		attributeEntity.set(AttributeMetaDataMetaData.TAGS, Arrays.asList(geneAnnotationTagEntity));
		attributeEntity.set(AttributeMetaDataMetaData.NAME, "Chr");
		Tag<AttributeMetaData, OntologyTerm, Ontology> chromosomeTag = new TagImpl<AttributeMetaData, OntologyTerm, Ontology>(
				"1233", attributeMetaData, instanceOf, CHROMOSOME_NAME_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		Entity entityMetaDataEntity = new MapEntity(new EntityMetaDataMetaData());
		entityMetaDataEntity.set(EntityMetaDataMetaData.ATTRIBUTES, Collections.singleton(attributeEntity));
		when(dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME, "org.molgenis.SNP")).thenReturn(
				entityMetaDataEntity);
		when(
				tagRepository.getTagEntity("http://edamontology.org/data_0987", "Chromosome name", instanceOf,
						"http://edamontology.org")).thenReturn(chromosomeNameTagEntity);

		ontologyTagService.addAttributeTag(emd, chromosomeTag);

		MapEntity updatedEntity = new MapEntity();
		updatedEntity.set(AttributeMetaDataMetaData.TAGS,
				Arrays.asList(geneAnnotationTagEntity, chromosomeNameTagEntity));
		updatedEntity.set(AttributeMetaDataMetaData.NAME, "Chr");

		verify(dataService, times(1)).update(AttributeMetaDataMetaData.ENTITY_NAME, updatedEntity);
	}

	@Test
	public void testRemoveTag()
	{
		EntityMetaData emd = new DefaultEntityMetaData("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = new DefaultAttributeMetaData("Chr");

		MapEntity attributeEntity = new MapEntity();
		attributeEntity.set(AttributeMetaDataMetaData.TAGS,
				Arrays.asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
		attributeEntity.set(AttributeMetaDataMetaData.NAME, "Chr");
		Entity entityMetaDataEntity = new MapEntity(new EntityMetaDataMetaData());
		entityMetaDataEntity.set(EntityMetaDataMetaData.ATTRIBUTES, Collections.singleton(attributeEntity));
		when(dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME, "org.molgenis.SNP")).thenReturn(
				entityMetaDataEntity);

		Tag<AttributeMetaData, OntologyTerm, Ontology> geneAnnotationTag = new TagImpl<AttributeMetaData, OntologyTerm, Ontology>(
				"4321", attributeMetaData, instanceOf, GENE_ANNOTATION_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		ontologyTagService.removeAttributeTag(emd, geneAnnotationTag);

		MapEntity updatedEntity = new MapEntity(attributeEntity);
		updatedEntity.set(AttributeMetaDataMetaData.TAGS, Arrays.asList(chromosomeNameTagEntity));

		verify(dataService, times(1)).update(AttributeMetaDataMetaData.ENTITY_NAME, updatedEntity);
	}

	@Configuration
	public static class Config
	{
		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		OntologyService ontologyService()
		{
			return mock(OntologyService.class);
		}

		@Bean
		TagRepository tagRepository()
		{
			return mock(TagRepository.class);
		}

	}
}
