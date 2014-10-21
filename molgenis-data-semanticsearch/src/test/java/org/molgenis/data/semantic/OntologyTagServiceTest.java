package org.molgenis.data.semantic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.beans.OntologyImpl;
import org.molgenis.ontology.beans.OntologyTermImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = OntologyTagServiceTest.Config.class)
public class OntologyTagServiceTest extends AbstractTestNGSpringContextTests
{
	private OntologyTagService ontologyTagService;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private CrudRepository attributeRepository;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private DataService dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		when(dataService.getCrudRepository(AttributeMetaDataMetaData.ENTITY_NAME)).thenReturn(attributeRepository);
		ontologyTagService = new OntologyTagService(dataService, ontologyService, tagRepository);
	}

	@Test
	public void testGetTags()
	{
		EntityMetaData emd = new DefaultEntityMetaData("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = new DefaultAttributeMetaData("Chr");

		Relation instanceOf = Relation.valueOf("instanceOf");

		MapEntity chromosomeNameTagEntity = new MapEntity();
		chromosomeNameTagEntity.set(TagMetaData.IDENTIFIER, "1234");
		chromosomeNameTagEntity.set(TagMetaData.LABEL, "Chromosome name");
		chromosomeNameTagEntity.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_0987");
		chromosomeNameTagEntity.set(TagMetaData.RELATION_IRI, instanceOf.getIRI());
		chromosomeNameTagEntity.set(TagMetaData.RELATION_LABEL, instanceOf.getLabel());
		chromosomeNameTagEntity.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		MapEntity geneAnnotationTagEntity = new MapEntity();
		geneAnnotationTagEntity.set(TagMetaData.IDENTIFIER, "4321");
		geneAnnotationTagEntity.set(TagMetaData.LABEL, "Gene annotation (chromosome)");
		geneAnnotationTagEntity.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_0919");
		geneAnnotationTagEntity.set(TagMetaData.RELATION_IRI, instanceOf.getIRI());
		geneAnnotationTagEntity.set(TagMetaData.RELATION_LABEL, instanceOf.getLabel());
		geneAnnotationTagEntity.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		MapEntity attributeEntity = new MapEntity();
		attributeEntity.set(AttributeMetaDataMetaData.TAGS,
				Arrays.asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
		when(
				attributeRepository.findOne(new QueryImpl().eq(AttributeMetaDataMetaData.ENTITY, "org.molgenis.SNP")
						.and().eq(AttributeMetaDataMetaData.NAME, "Chr"))).thenReturn(attributeEntity);

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
				attributeMetaData, instanceOf, chromosomeName, edamOntology);
		Tag<AttributeMetaData, OntologyTerm, Ontology> geneAnnotationTag = new TagImpl<AttributeMetaData, OntologyTerm, Ontology>(
				attributeMetaData, instanceOf, geneAnnotation, edamOntology);

		assertEquals(ontologyTagService.getTagsForAttribute(emd, attributeMetaData),
				Arrays.asList(chromosomeTag, geneAnnotationTag));
	}

	@Configuration
	public static class Config
	{
		@Bean
		CrudRepository attributeRepository()
		{
			return mock(CrudRepository.class);
		}

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
