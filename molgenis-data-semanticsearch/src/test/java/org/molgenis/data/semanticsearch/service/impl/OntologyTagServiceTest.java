package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ATTRIBUTES;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.meta.model.TagMetaData;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.repository.TagRepository;
import org.molgenis.data.semanticsearch.semantic.OntologyTag;
import org.molgenis.ontology.core.model.CombinedOntologyTerm;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTagObject;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

@WebAppConfiguration
@ContextConfiguration(classes = OntologyTagServiceTest.Config.class)
public class OntologyTagServiceTest extends AbstractMolgenisSpringTest
{
	private OntologyTagServiceImpl ontologyTagService;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private DataService dataService;

	@Autowired
	private TagMetaData tagMetaData;

	@Autowired
	private TagFactory tagFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrFactory;

	@Autowired
	private PackageFactory packageFactory;

	private Tag chromosomeNameTagEntity;

	private Tag geneAnnotationTagEntity;

	private final Relation instanceOf = Relation.valueOf("instanceOf");

	private static final Ontology EDAM_ONTOLOGY = Ontology.create("EDAM", "http://edamontology.org",
			"The EDAM ontology.");

	private static final OntologyTerm CHROMOSOME_NAME_ONTOLOGY_TERM = OntologyTerm.create("0987",
			"http://edamontology.org/data_0987", "Chromosome name");

	private static final OntologyTerm GENE_ANNOTATION_ONTOLOGY_TERM = OntologyTerm.create("data_0919",
			"http://edamontology.org/data_0919", "Gene annotation (chromosome)");

	@BeforeMethod
	public void beforeMethod()
	{
		reset(dataService);
		chromosomeNameTagEntity = tagFactory.create();
		chromosomeNameTagEntity.set(TagMetaData.IDENTIFIER, "1234");
		chromosomeNameTagEntity.set(TagMetaData.LABEL, "Chromosome name");
		chromosomeNameTagEntity.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_0987");
		chromosomeNameTagEntity.set(TagMetaData.RELATION_IRI, instanceOf.getIRI());
		chromosomeNameTagEntity.set(TagMetaData.RELATION_LABEL, instanceOf.getLabel());
		chromosomeNameTagEntity.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		geneAnnotationTagEntity = tagFactory.create();
		geneAnnotationTagEntity.set(TagMetaData.IDENTIFIER, "4321");
		geneAnnotationTagEntity.set(TagMetaData.LABEL, "Gene annotation (chromosome)");
		geneAnnotationTagEntity.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_0919");
		geneAnnotationTagEntity.set(TagMetaData.RELATION_IRI, instanceOf.getIRI());
		geneAnnotationTagEntity.set(TagMetaData.RELATION_LABEL, instanceOf.getLabel());
		geneAnnotationTagEntity.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		IdGenerator idGenerator = mock(IdGenerator.class);
		ontologyTagService = new OntologyTagServiceImpl(dataService, ontologyService, tagRepository, idGenerator,
				tagMetaData);
	}

	@Test
	public void testgetTagsForAttribute()
	{
		EntityMetaData emd = entityMetaFactory.create().setName("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = attrFactory.create().setName("Chr");

		Relation instanceOf = Relation.valueOf("instanceOf");

		AttributeMetaData attributeEntity = attrFactory.create();
		attributeEntity.setTags(asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
		attributeEntity.set(AttributeMetaDataMetaData.NAME, "Chr");

		EntityMetaData entityMetaDataEntity = entityMetaFactory.create();
		entityMetaDataEntity.setOwnAttributes(singleton(attributeEntity));
		when(dataService.findOneById(ENTITY_META_DATA, "org.molgenis.SNP")).thenReturn(entityMetaDataEntity);

		Ontology edamOntology = Ontology.create("EDAM", "http://edamontology.org", "The EDAM ontology.");
		OntologyTerm chromosomeName = OntologyTerm.create("0987", "http://edamontology.org/data_0987",
				"Chromosome name");
		OntologyTerm geneAnnotation = OntologyTerm.create("data_0919", "http://edamontology.org/data_0919",
				"Gene annotation (chromosome)");

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(edamOntology);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987")).thenReturn(chromosomeName);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0919")).thenReturn(geneAnnotation);

		OntologyTagObject expectedCombinedChromesomeName = CombinedOntologyTerm
				.create("http://edamontology.org/data_0987", "Chromosome name");
		OntologyTagObject expectedCombinedGeneAnnotation = CombinedOntologyTerm
				.create("http://edamontology.org/data_0919", "Gene annotation (chromosome)");
		Multimap<Relation, OntologyTagObject> expected = LinkedHashMultimap.create();
		expected.put(instanceOf, expectedCombinedChromesomeName);
		expected.put(instanceOf, expectedCombinedGeneAnnotation);

		assertEquals(ontologyTagService.getTagsForAttribute(emd, attributeMetaData), expected);
	}

	@Test
	public void testGetTagEntity()
	{
		Tag expected = tagFactory.create();
		expected.set(TagMetaData.IDENTIFIER, "1233");
		expected.set(TagMetaData.OBJECT_IRI, "http://edamontology.org/data_3031");
		expected.set(TagMetaData.LABEL, "Core data");
		expected.set(TagMetaData.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf");
		expected.set(TagMetaData.RELATION_LABEL, "instanceOf");
		expected.set(TagMetaData.CODE_SYSTEM, "http://edamontology.org");

		OntologyTagObject coreData = mock(OntologyTagObject.class);

		when(coreData.getIRI()).thenReturn("http://edamontology.org/data_3031");
		when(coreData.getLabel()).thenReturn("Core data");

		Ontology edamOntology = mock(Ontology.class);

		when(edamOntology.getIRI()).thenReturn("http://edamontology.org");

		SemanticTag<Object, OntologyTagObject, Ontology> tag = new SemanticTag<>("1233", null, Relation.instanceOf, coreData,
				edamOntology);
		when(tagRepository.getTagEntity("http://edamontology.org/data_3031", "Core data", Relation.instanceOf,
				"http://edamontology.org")).thenReturn(expected);

		assertEquals(ontologyTagService.getTagEntity(tag), expected);

	}

	@Test
	public void testAddAttributeTag()
	{
		EntityMetaData emd = entityMetaFactory.create().setName("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = attrFactory.create().setName("Chr");

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(EDAM_ONTOLOGY);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987"))
				.thenReturn(CHROMOSOME_NAME_ONTOLOGY_TERM);

		AttributeMetaData attributeEntity = attrFactory.create();
		attributeEntity.setTags(singletonList(geneAnnotationTagEntity));
		attributeEntity.setName("Chr");
		SemanticTag<AttributeMetaData, OntologyTagObject, Ontology> chromosomeTag = new SemanticTag<>("1233",
				attributeMetaData, instanceOf, CHROMOSOME_NAME_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		EntityMetaData entityMetaDataEntity = entityMetaFactory.create();
		entityMetaDataEntity.setOwnAttributes(singleton(attributeEntity));
		when(dataService.findOneById(ENTITY_META_DATA, "org.molgenis.SNP")).thenReturn(entityMetaDataEntity);
		when(tagRepository.getTagEntity("http://edamontology.org/data_0987", "Chromosome name", instanceOf,
				"http://edamontology.org")).thenReturn(chromosomeNameTagEntity);

		ontologyTagService.addAttributeTag(emd, chromosomeTag);

		ArgumentCaptor<AttributeMetaData> captor = forClass(AttributeMetaData.class);
		verify(dataService, times(1)).update(eq(ATTRIBUTE_META_DATA), captor.capture());
		assertEquals(captor.getValue().getName(), "Chr");
		assertEquals(captor.getValue().getTags(), asList(geneAnnotationTagEntity, chromosomeNameTagEntity));
	}

	@Test
	public void testRemoveAttributeTag()
	{
		EntityMetaData emd = entityMetaFactory.create().setName("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = attrFactory.create().setName("Chr");

		AttributeMetaData attributeEntity = attrFactory.create();
		attributeEntity.setTags(asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
		attributeEntity.setName("Chr");
		EntityMetaData entityMetaDataEntity = entityMetaFactory.create();
		entityMetaDataEntity.setOwnAttributes(singleton(attributeEntity));
		when(dataService.findOneById(ENTITY_META_DATA, "org.molgenis.SNP")).thenReturn(entityMetaDataEntity);

		SemanticTag<AttributeMetaData, OntologyTagObject, Ontology> geneAnnotationTag = new SemanticTag<>("4321",
				attributeMetaData, instanceOf, GENE_ANNOTATION_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		ontologyTagService.removeAttributeTag(emd, geneAnnotationTag);

		ArgumentCaptor<AttributeMetaData> captor = forClass(AttributeMetaData.class);
		verify(dataService, times(1)).update(eq(ATTRIBUTE_META_DATA), captor.capture());
		assertEquals(captor.getValue().getTags(), asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
	}

	@Test
	public void testgetTagsForPackage()
	{
		Package p = packageFactory.create("test", "desc");

		Package pack = packageFactory.create();
		pack.setName("test");
		pack.setSimpleName("test");
		pack.setTags(singletonList(chromosomeNameTagEntity));

		when(dataService.findOneById(PACKAGE, "test")).thenReturn(pack);

		assertEquals(ontologyTagService.getTagsForPackage(p),
				singletonList(
						new SemanticTag<>("1234", p, Relation.forIRI("http://molgenis.org/biobankconnect/instanceOf"),
								CombinedOntologyTerm.create("http://edamontology.org/data_0987", "Chromosome name"),
								Ontology.create("EDAM", "http://edamontology.org", "The EDAM ontology."))));
	}

	@Test
	public void testRemoveAllTagsFromEntity()
	{
		// FIXME This does not make sense...
		EntityMetaData emd = entityMetaFactory.create().setName("test");
		AttributeMetaData amd = attrFactory.create().setName("Chr");

		emd.addAttribute(amd);
		when(dataService.getEntityMetaData("test")).thenReturn(emd);

		Entity entityMetaDataEntity = mock(Entity.class);
		Entity att = mock(Entity.class);

		when(entityMetaDataEntity.getEntities(ATTRIBUTES)).thenReturn(singletonList(att));
		when(att.getString(AttributeMetaDataMetaData.NAME)).thenReturn("Chr");

		when(dataService.findOneById(ENTITY_META_DATA, "test")).thenReturn(entityMetaDataEntity);
		ontologyTagService.removeAllTagsFromEntity("test");

		verify(dataService).update(ENTITY_META_DATA, entityMetaDataEntity);
	}

	@Test
	public void testTagAttributesInEntity()
	{
		Map<String, OntologyTag> attributeTagMap = Maps.newHashMap();
		Map<AttributeMetaData, OntologyTagObject> tags = Maps.newHashMap();

		EntityMetaData emd = entityMetaFactory.create().setName("org.molgenis.SNP");
		AttributeMetaData attributeMetaData = attrFactory.create().setName("Chr");

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(EDAM_ONTOLOGY);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987"))
				.thenReturn(CHROMOSOME_NAME_ONTOLOGY_TERM);

		AttributeMetaData attributeEntity = attrFactory.create();
		attributeEntity.setTags(singletonList(geneAnnotationTagEntity));
		attributeEntity.setName("Chr");
		SemanticTag<AttributeMetaData, OntologyTagObject, Ontology> chromosomeTag = new SemanticTag<>("1233",
				attributeMetaData, instanceOf, CHROMOSOME_NAME_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		EntityMetaData entityMetaDataEntity = entityMetaFactory.create();
		entityMetaDataEntity.setOwnAttributes(singleton(attributeEntity));
		when(dataService.findOneById(ENTITY_META_DATA, "org.molgenis.SNP")).thenReturn(entityMetaDataEntity);
		when(tagRepository.getTagEntity("http://edamontology.org/data_0987", "Chromosome name", instanceOf,
				"http://edamontology.org")).thenReturn(chromosomeNameTagEntity);

		ontologyTagService.addAttributeTag(emd, chromosomeTag);

		AttributeMetaData updatedEntity = attrFactory.create();
		updatedEntity.setTags(asList(geneAnnotationTagEntity, chromosomeNameTagEntity));
		updatedEntity.setName("Chr");

		assertEquals(ontologyTagService.tagAttributesInEntity("test", tags), attributeTagMap);
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
