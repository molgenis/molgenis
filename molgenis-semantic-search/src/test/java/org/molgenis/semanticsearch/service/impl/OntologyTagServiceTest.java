package org.molgenis.semanticsearch.service.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.semanticsearch.repository.TagRepository;
import org.molgenis.semanticsearch.semantic.OntologyTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = OntologyTagServiceTest.Config.class)
public class OntologyTagServiceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private Config config;

	private OntologyTagServiceImpl ontologyTagService;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private DataService dataService;

	@Autowired
	private TagMetadata tagMetadata;

	@Autowired
	private TagFactory tagFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrFactory;

	@Autowired
	private PackageFactory packageFactory;

	private Tag chromosomeNameTagEntity;

	private Tag geneAnnotationTagEntity;

	private final Relation instanceOf = Relation.valueOf("instanceOf");

	private static final Ontology EDAM_ONTOLOGY = Ontology.create("EDAM", "http://edamontology.org",
			"The EDAM ontology.");

	private static final OntologyTerm CHROMOSOME_NAME_ONTOLOGY_TERM = OntologyTerm.create(
			"http://edamontology.org/data_0987", "Chromosome name", "Name of a chromosome.");

	private static final OntologyTerm GENE_ANNOTATION_ONTOLOGY_TERM = OntologyTerm.create(
			"http://edamontology.org/data_0919", "Gene annotation (chromosome)",
			"This includes basic information. e.g. chromosome number...");

	public OntologyTagServiceTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		config.resetMocks();

		chromosomeNameTagEntity = tagFactory.create();
		chromosomeNameTagEntity.set(TagMetadata.ID, "1234");
		chromosomeNameTagEntity.set(TagMetadata.LABEL, "Chromosome name");
		chromosomeNameTagEntity.set(TagMetadata.OBJECT_IRI, "http://edamontology.org/data_0987");
		chromosomeNameTagEntity.set(TagMetadata.RELATION_IRI, instanceOf.getIRI());
		chromosomeNameTagEntity.set(TagMetadata.RELATION_LABEL, instanceOf.getLabel());
		chromosomeNameTagEntity.set(TagMetadata.CODE_SYSTEM, "http://edamontology.org");

		geneAnnotationTagEntity = tagFactory.create();
		geneAnnotationTagEntity.set(TagMetadata.ID, "4321");
		geneAnnotationTagEntity.set(TagMetadata.LABEL, "Gene annotation (chromosome)");
		geneAnnotationTagEntity.set(TagMetadata.OBJECT_IRI, "http://edamontology.org/data_0919");
		geneAnnotationTagEntity.set(TagMetadata.RELATION_IRI, instanceOf.getIRI());
		geneAnnotationTagEntity.set(TagMetadata.RELATION_LABEL, instanceOf.getLabel());
		geneAnnotationTagEntity.set(TagMetadata.CODE_SYSTEM, "http://edamontology.org");

		IdGenerator idGenerator = mock(IdGenerator.class);
		ontologyTagService = new OntologyTagServiceImpl(dataService, ontologyService, tagRepository, idGenerator,
				tagMetadata);
	}

	@Test
	public void testGetTagsForAttribute()
	{
		EntityType emd = entityTypeFactory.create("org.molgenis.SNP");
		Attribute attribute = attrFactory.create().setName("Chr");
		attribute.setTags(asList(chromosomeNameTagEntity, geneAnnotationTagEntity));

		Relation instanceOf = Relation.valueOf("instanceOf");

		Ontology edamOntology = Ontology.create("EDAM", "http://edamontology.org", "The EDAM ontology.");
		OntologyTerm chromosomeName = OntologyTerm.create("http://edamontology.org/data_0987", "Chromosome name",
				"Name of a chromosome.");
		OntologyTerm geneAnnotation = OntologyTerm.create("http://edamontology.org/data_0919",
				"Gene annotation (chromosome)", "This includes basic information. e.g. chromosome number...");

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(edamOntology);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987")).thenReturn(chromosomeName);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0919")).thenReturn(geneAnnotation);

		Multimap<Relation, OntologyTerm> expected = LinkedHashMultimap.create();
		expected.put(instanceOf, chromosomeName);
		expected.put(instanceOf, geneAnnotation);

		assertEquals(ontologyTagService.getTagsForAttribute(emd, attribute), expected);
	}

	@Test
	public void testGetTagEntity()
	{
		Tag expected = tagFactory.create();
		expected.set(TagMetadata.ID, "1233");
		expected.set(TagMetadata.OBJECT_IRI, "http://edamontology.org/data_3031");
		expected.set(TagMetadata.LABEL, "Core data");
		expected.set(TagMetadata.RELATION_IRI, "http://molgenis.org/biobankconnect/instanceOf");
		expected.set(TagMetadata.RELATION_LABEL, "instanceOf");
		expected.set(TagMetadata.CODE_SYSTEM, "http://edamontology.org");

		OntologyTerm coreData = mock(OntologyTerm.class);

		when(coreData.getIRI()).thenReturn("http://edamontology.org/data_3031");
		when(coreData.getLabel()).thenReturn("Core data");

		Ontology edamOntology = mock(Ontology.class);

		when(edamOntology.getIRI()).thenReturn("http://edamontology.org");

		SemanticTag<Object, OntologyTerm, Ontology> tag = new SemanticTag<>("1233", null, Relation.instanceOf, coreData,
				edamOntology);
		when(tagRepository.getTagEntity("http://edamontology.org/data_3031", "Core data", Relation.instanceOf,
				"http://edamontology.org")).thenReturn(expected);

		assertEquals(ontologyTagService.getTagEntity(tag), expected);

	}

	@Test
	public void testAddAttributeTag()
	{
		EntityType emd = entityTypeFactory.create("org.molgenis.SNP");
		Attribute attribute = attrFactory.create().setName("Chr");

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(EDAM_ONTOLOGY);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987")).thenReturn(
				CHROMOSOME_NAME_ONTOLOGY_TERM);

		Attribute attributeEntity = attrFactory.create();
		attributeEntity.setTags(singletonList(geneAnnotationTagEntity));
		attributeEntity.setName("Chr");
		SemanticTag<Attribute, OntologyTerm, Ontology> chromosomeTag = new SemanticTag<>("1233", attribute, instanceOf,
				CHROMOSOME_NAME_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		EntityType entityTypeEntity = entityTypeFactory.create();
		entityTypeEntity.setOwnAllAttributes(singleton(attributeEntity));
		when(dataService.getEntityType("org.molgenis.SNP")).thenReturn(entityTypeEntity);
		when(tagRepository.getTagEntity("http://edamontology.org/data_0987", "Chromosome name", instanceOf,
				"http://edamontology.org")).thenReturn(chromosomeNameTagEntity);
		ontologyTagService.addAttributeTag(emd, chromosomeTag);

		ArgumentCaptor<Attribute> captor = forClass(Attribute.class);
		verify(dataService, times(1)).update(eq(ATTRIBUTE_META_DATA), captor.capture());
		assertEquals(captor.getValue().getName(), "Chr");
		assertEquals(captor.getValue().getTags(), asList(geneAnnotationTagEntity, chromosomeNameTagEntity));
	}

	@Test
	public void testRemoveAttributeTag()
	{
		EntityType emd = entityTypeFactory.create("org.molgenis.SNP");
		Attribute attribute = attrFactory.create().setName("Chr");

		Attribute attributeEntity = attrFactory.create();
		attributeEntity.setTags(asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
		attributeEntity.setName("Chr");
		EntityType entityTypeEntity = entityTypeFactory.create();
		entityTypeEntity.setOwnAllAttributes(singleton(attributeEntity));
		when(dataService.getEntityType("org.molgenis.SNP")).thenReturn(entityTypeEntity);

		SemanticTag<Attribute, OntologyTerm, Ontology> geneAnnotationTag = new SemanticTag<>("4321", attribute,
				instanceOf, GENE_ANNOTATION_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		ontologyTagService.removeAttributeTag(emd, geneAnnotationTag);

		ArgumentCaptor<Attribute> captor = forClass(Attribute.class);
		verify(dataService, times(1)).update(eq(ATTRIBUTE_META_DATA), captor.capture());
		assertEquals(captor.getValue().getTags(), asList(chromosomeNameTagEntity, geneAnnotationTagEntity));
	}

	@Test
	public void testGetTagsForPackage()
	{
		Package p = packageFactory.create("test", "desc");

		Package pack = packageFactory.create();
		pack.setTags(singletonList(chromosomeNameTagEntity));
				when(ontologyService.getOntology("http://edamontology.org")).thenReturn(EDAM_ONTOLOGY);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987")).thenReturn(
				CHROMOSOME_NAME_ONTOLOGY_TERM);

		when(dataService.findOneById(PACKAGE, "test")).thenReturn(pack);

		assertEquals(ontologyTagService.getTagsForPackage(p), singletonList(
				new SemanticTag<>("1234", p, Relation.forIRI("http://molgenis.org/biobankconnect/instanceOf"),
						OntologyTerm.create("http://edamontology.org/data_0987", "Chromosome name",
								"Name of a chromosome."),
						Ontology.create("EDAM", "http://edamontology.org", "The EDAM ontology."))));
	}

	@Test
	public void testRemoveAllTagsFromEntity()
	{
		Attribute att = mock(Attribute.class);
		when(att.getName()).thenReturn("Chr");
		EntityType entityTypeEntity = mock(EntityType.class);
		when(entityTypeEntity.getId()).thenReturn("test");
		when(entityTypeEntity.getAtomicAttributes()).thenReturn(singletonList(att));
		when(entityTypeEntity.getAttributes()).thenReturn(singletonList(att));
		when(entityTypeEntity.getOwnAllAttributes()).thenReturn(singletonList(att));
		when(entityTypeEntity.getAttribute("Chr")).thenReturn(att);

		when(dataService.getEntityType("test")).thenReturn(entityTypeEntity);
		ontologyTagService.removeAllTagsFromEntity("test");

		verify(dataService).update(ENTITY_TYPE_META_DATA, entityTypeEntity);
	}

	@Test
	public void testTagAttributesInEntity()
	{
		Map<String, OntologyTag> attributeTagMap = Maps.newHashMap();
		Map<Attribute, OntologyTerm> tags = Maps.newHashMap();

		EntityType emd = entityTypeFactory.create("org.molgenis.SNP");
		Attribute attribute = attrFactory.create().setName("Chr");

		when(ontologyService.getOntology("http://edamontology.org")).thenReturn(EDAM_ONTOLOGY);
		when(ontologyService.getOntologyTerm("http://edamontology.org/data_0987")).thenReturn(
				CHROMOSOME_NAME_ONTOLOGY_TERM);

		Attribute attributeEntity = attrFactory.create();
		attributeEntity.setTags(singletonList(geneAnnotationTagEntity));
		attributeEntity.setName("Chr");
		SemanticTag<Attribute, OntologyTerm, Ontology> chromosomeTag = new SemanticTag<>("1233", attribute, instanceOf,
				CHROMOSOME_NAME_ONTOLOGY_TERM, EDAM_ONTOLOGY);

		EntityType entityTypeEntity = entityTypeFactory.create();
		entityTypeEntity.setOwnAllAttributes(singleton(attributeEntity));
		when(dataService.findOneById(ENTITY_TYPE_META_DATA, "org.molgenis.SNP", EntityType.class)).thenReturn(
				entityTypeEntity);
		when(dataService.getEntityType("org.molgenis.SNP")).thenReturn(entityTypeEntity);
		when(tagRepository.getTagEntity("http://edamontology.org/data_0987", "Chromosome name", instanceOf,
				"http://edamontology.org")).thenReturn(chromosomeNameTagEntity);

		ontologyTagService.addAttributeTag(emd, chromosomeTag);

		Attribute updatedEntity = attrFactory.create();
		updatedEntity.setTags(asList(geneAnnotationTagEntity, chromosomeNameTagEntity));
		updatedEntity.setName("Chr");

		assertEquals(ontologyTagService.tagAttributesInEntity("test", tags), attributeTagMap);
	}

	@Configuration
	public static class Config
	{
		@Mock
		private DataService dataService;

		@Mock
		private OntologyService ontologyService;

		@Mock
		private TagRepository tagRepository;

		public Config()
		{
			initMocks(this);
		}

		@Bean
		DataService dataService()
		{
			return dataService;
		}

		@Bean
		OntologyService ontologyService()
		{
			return ontologyService;
		}

		@Bean
		TagRepository tagRepository()
		{
			return tagRepository;
		}

		void resetMocks()
		{
			reset(dataService, ontologyService, tagRepository);
		}
	}
}