package org.molgenis.fair.controller;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.semanticsearch.service.TagService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.rdf4j.model.vocabulary.RDF.TYPE;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class EntityModelWriterTest extends AbstractMockitoTest
{
	@Mock
	private TagService<LabeledResource, LabeledResource> tagService;
	@Mock
	private Entity objectEntity;
	@Mock
	private Entity refEntity;
	@Mock
	private EntityType entityType;
	@Mock
	private EntityType refEntityType;
	@Mock
	private Attribute attribute;
	@Mock
	private Attribute attr1;
	@Mock
	private Attribute attr2;
	@Mock
	private Attribute attr3;
	private EntityModelWriter writer;
	private SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void beforeMethod() throws DatatypeConfigurationException
	{
		writer = new EntityModelWriter(tagService, valueFactory);
	}

	@Test
	public void testCreateRfdModelStringAttribute()
	{
		List<Attribute> attributeList = singletonList(attr1);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName1")).thenReturn("value1");
		when(objectEntity.getString("attributeName1")).thenReturn("value1");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);

		when(attr1.getName()).thenReturn("attributeName1");
		when(attr1.getDataType()).thenReturn(AttributeType.STRING);

		LabeledResource tag1 = new LabeledResource("http://IRI1.nl", "tag1Label");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag1);
		when(tagService.getTagsForAttribute(entityType, attr1)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI1.nl, \"value1\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]");
	}

	@Test
	public void testCreateRfdModelIntAttribute()
	{
		List<Attribute> attributeList = singletonList(attr2);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName2")).thenReturn(2);
		when(objectEntity.getInt("attributeName2")).thenReturn(2);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr2.getName()).thenReturn("attributeName2");

		when(attr2.getDataType()).thenReturn(AttributeType.INT);

		LabeledResource tag2 = new LabeledResource("http://IRI2.nl", "tag2Label");
		Multimap<Relation, LabeledResource> tags2 = ImmutableMultimap.of(Relation.isAssociatedWith, tag2);
		when(tagService.getTagsForAttribute(entityType, attr2)).thenReturn(tags2);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI2.nl, \"2\"^^<http://www.w3.org/2001/XMLSchema#int>) [null]");
	}

	@Test
	public void testCreateRfdModelXREF()
	{
		List<Attribute> attributeList = singletonList(attr3);
		List<String> refAttributeList = singletonList("refAttr");

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName3")).thenReturn(refEntity);
		when(objectEntity.getEntity("attributeName3")).thenReturn(refEntity);

		when(refEntity.getEntityType()).thenReturn(refEntityType);
		when(refEntityType.getAttributeNames()).thenReturn(refAttributeList);
		when(refEntity.getIdValue()).thenReturn("refID");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr3.getName()).thenReturn("attributeName3");

		when(attr3.getDataType()).thenReturn(AttributeType.XREF);

		LabeledResource tag3 = new LabeledResource("http://IRI3.nl", "labelTag3");
		Multimap<Relation, LabeledResource> tags3 = ImmutableMultimap.of(Relation.isAssociatedWith, tag3);
		when(tagService.getTagsForAttribute(entityType, attr3)).thenReturn(tags3);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI3.nl, http://molgenis01.gcc.rug.nl/fdp/catolog/test/this/refID) [null]");
	}

	@Test
	public void testCreateRfdModelMREF()
	{
		List<Attribute> attributeList = singletonList(attribute);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName")).thenReturn(refEntity);
		when(objectEntity.getEntities("attributeName")).thenReturn(singletonList(refEntity));

		when(refEntity.getIdValue()).thenReturn("refID");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute.getName()).thenReturn("attributeName");

		when(attribute.getDataType()).thenReturn(AttributeType.MREF);

		LabeledResource tag = new LabeledResource("http://IRI.nl", "labelTag3");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag);
		when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI.nl, http://molgenis01.gcc.rug.nl/fdp/catolog/test/this/refID) [null]");
	}

	@Test
	public void testCreateRfdModelBOOL()
	{
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attribute = mock(Attribute.class);
		List<Attribute> attributeList = singletonList(attribute);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName")).thenReturn(true);
		when(objectEntity.getBoolean("attributeName")).thenReturn(true);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute.getName()).thenReturn("attributeName");

		when(attribute.getDataType()).thenReturn(AttributeType.BOOL);

		LabeledResource tag = new LabeledResource("http://IRI.nl", "tag label");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag);
		when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI.nl, \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>) [null]");
	}

	@Test
	public void testCreateRfdModelDATE()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attribute = mock(Attribute.class);
		List<Attribute> attributeList = singletonList(attribute);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		LocalDate value = LocalDate.of(2013, Month.AUGUST, 12);
		when(objectEntity.get("attributeName")).thenReturn(value);
		when(objectEntity.getLocalDate("attributeName")).thenReturn(value);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute.getName()).thenReturn("attributeName");

		when(attribute.getDataType()).thenReturn(AttributeType.DATE);

		LabeledResource tag = new LabeledResource("http://IRI.nl", "tag label");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag);
		when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI.nl, \"2013-08-12\"^^<http://www.w3.org/2001/XMLSchema#date>) [null]");
	}

	@Test
	public void testCreateRfdModelDATETIME()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attribute = mock(Attribute.class);
		List<Attribute> attributeList = singletonList(attribute);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		Instant value = Instant.parse("2011-12-03T10:15:30Z");
		when(objectEntity.get("attributeName")).thenReturn(value);
		when(objectEntity.getInstant("attributeName")).thenReturn(value);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute.getName()).thenReturn("attributeName");

		when(attribute.getDataType()).thenReturn(AttributeType.DATE_TIME);

		LabeledResource tag = new LabeledResource("http://IRI.nl", "tag label");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag);
		when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI.nl, \"2011-12-03T10:15:30Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [null]");
	}

	@Test
	public void testCreateRfdModelDECIMAL()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attribute = mock(Attribute.class);
		List<Attribute> attributeList = singletonList(attribute);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		double value = 10.0;
		when(objectEntity.get("attributeName")).thenReturn(value);
		when(objectEntity.getDouble("attributeName")).thenReturn(value);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute.getName()).thenReturn("attributeName");

		when(attribute.getDataType()).thenReturn(AttributeType.DECIMAL);

		LabeledResource tag = new LabeledResource("http://IRI.nl", "tag label");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag);
		when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI.nl, \"10.0\"^^<http://www.w3.org/2001/XMLSchema#double>) [null]");
	}

	@Test
	public void testCreateRfdModelLONG()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attribute = mock(Attribute.class);
		List<Attribute> attributeList = singletonList(attribute);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		long value = 987654321L;
		when(objectEntity.get("attributeName")).thenReturn(value);
		when(objectEntity.getLong("attributeName")).thenReturn(value);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute.getName()).thenReturn("attributeName");

		when(attribute.getDataType()).thenReturn(AttributeType.LONG);

		LabeledResource tag = new LabeledResource("http://IRI.nl", "tag label");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag);
		when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI.nl, \"987654321\"^^<http://www.w3.org/2001/XMLSchema#long>) [null]");
	}

	@Test
	public void testCreateRfdModelSTRINGKeywords()
	{
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attribute = mock(Attribute.class);
		List<Attribute> attributeList = singletonList(attribute);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		String value = "molgenis,genetics,fair";
		when(objectEntity.get("attributeName")).thenReturn(value);
		when(objectEntity.getString("attributeName")).thenReturn(value);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute.getName()).thenReturn("attributeName");

		when(attribute.getDataType()).thenReturn(AttributeType.STRING);

		LabeledResource tag = new LabeledResource("http://www.w3.org/ns/dcat#keyword", "keywords");
		Multimap<Relation, LabeledResource> tags = ImmutableMultimap.of(Relation.isAssociatedWith, tag);
		when(tagService.getTagsForAttribute(entityType, attribute)).thenReturn(tags);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 3);
		List<String> statements = result.stream().map(Statement::toString).collect(toList());
		assertEquals(statements, Arrays.asList(
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://www.w3.org/ns/dcat#keyword, \"molgenis\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]",
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://www.w3.org/ns/dcat#keyword, \"genetics\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]",
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://www.w3.org/ns/dcat#keyword, \"fair\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]"));
	}

	@Test
	public void testCreateRfdModelNullValuePlusHyperlink()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attribute1 = mock(Attribute.class);
		Attribute attribute2 = mock(Attribute.class);
		List<Attribute> attributeList = Arrays.asList(attribute1, attribute2);

		when(objectEntity.getEntityType()).thenReturn(entityType);

		when(objectEntity.get("attribute1Name")).thenReturn(null);

		String value = "http://molgenis.org/index.html";
		doReturn(value).when(objectEntity).get("attribute2Name");
		when(objectEntity.getString("attribute2Name")).thenReturn(value);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attribute1.getName()).thenReturn("attribute1Name");
		when(attribute2.getName()).thenReturn("attribute2Name");

		when(attribute2.getDataType()).thenReturn(AttributeType.HYPERLINK);

		LabeledResource tag2 = new LabeledResource("http://IRI1.nl", "tag1 label");
		Multimap<Relation, LabeledResource> tags2 = ImmutableMultimap.of(Relation.isAssociatedWith, tag2);
		doReturn(tags2).when(tagService).getTagsForAttribute(entityType, attribute2);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI1.nl, http://molgenis.org/index.html) [null]");
	}

	@Test
	public void testAddStatementsForEntityType()
	{
		Model model = new LinkedHashModel();
		Resource subject = valueFactory.createIRI("http://example.org/subject");
		LabeledResource object = new LabeledResource( "http://example.org/object", "object");
		LabeledResource codeSystem = new LabeledResource( "ex:object");

		SemanticTag<EntityType, LabeledResource, LabeledResource> tag =
				new SemanticTag<>("tagId", entityType, Relation.isAssociatedWith, object, codeSystem);

		when(tagService.getTagsForEntity(entityType)).thenReturn(singletonList(tag));


		writer.addStatementsForEntityTags(model, subject, entityType);

		Statement statement = valueFactory.createStatement(subject, TYPE, valueFactory.createIRI("http://example.org/object"));
		assertEquals(newArrayList(model), singletonList(statement));
	}
}
