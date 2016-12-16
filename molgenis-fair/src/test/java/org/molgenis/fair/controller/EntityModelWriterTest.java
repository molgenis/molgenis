package org.molgenis.fair.controller;

import com.google.common.collect.Multimap;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.TagService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class EntityModelWriterTest
{
	private SimpleValueFactory valueFactory;
	private TagService<LabeledResource, LabeledResource> tagService;
	private EntityModelWriter writer;
	private IRI url_iri;
	private IRI ref_iri;
	private IRI ref_iri2;
	private IRI molgenis_iri;

	@BeforeMethod
	public void beforeTest() throws DatatypeConfigurationException
	{
		valueFactory = mock(SimpleValueFactory.class);
		tagService = mock(TagService.class);
		writer = new EntityModelWriter(tagService, valueFactory);

		url_iri = mock(IRI.class);
		ref_iri = mock(IRI.class);
		ref_iri2 = mock(IRI.class);
		molgenis_iri = mock(IRI.class);
		Literal url_literal = mock(Literal.class);
		Literal ref_literal1 = mock(Literal.class);
		Literal ref_literal2 = mock(Literal.class);
		Literal molgenis_literal = mock(Literal.class);

		when(url_iri.toString()).thenReturn("url_iri");
		when(ref_iri.toString()).thenReturn("ref_iri");
		when(ref_iri2.toString()).thenReturn("ref_iri2");
		when(molgenis_iri.toString()).thenReturn("molgenis_iri");

		when(url_literal.toString()).thenReturn("url_literal");
		when(ref_literal1.toString()).thenReturn("ref_literal1");
		when(ref_literal2.toString()).thenReturn("ref_literal2");
		when(molgenis_literal.toString()).thenReturn("molgenis_literal");

		when(url_iri.stringValue()).thenReturn("http://IRI13.nl");
		when(ref_iri.stringValue()).thenReturn("http://IRI14.nl");
		when(ref_iri2.stringValue()).thenReturn("http://IRI15.nl");
		when(molgenis_iri.stringValue()).thenReturn("http://IRI16.nl");

		when(valueFactory.createIRI("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this")).thenReturn(url_iri);
		when(valueFactory.createIRI("http://IRI13.nl/refID")).thenReturn(ref_iri);
		when(valueFactory.createIRI("http://refIRI.iri")).thenReturn(ref_iri2);
		when(valueFactory.createIRI("http://www/molgenis.org")).thenReturn(molgenis_iri);

	}

	@Test
	public void testCreateRfdModel1()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr1 = mock(Attribute.class);

		IRI iri1 = mock(IRI.class);

		Literal literal1 = mock(Literal.class);

		when(iri1.toString()).thenReturn("iri1");
		when(literal1.toString()).thenReturn("literal1");

		Multimap<Relation, LabeledResource> multiMap1 = mock(Multimap.class);

		LabeledResource tag1 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr1);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName1")).thenReturn("value1");
		when(objectEntity.getString("attributeName1")).thenReturn("value1");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);

		when(attr1.getName()).thenReturn("attributeName1");
		when(attr1.getDataType()).thenReturn(AttributeType.STRING);

		when(tag1.getIri()).thenReturn("http://IRI1.nl");
		when(tagService.getTagsForAttribute(entityType, attr1)).thenReturn(multiMap1);
		when(multiMap1.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag1));


		when(valueFactory.createIRI("http://IRI1.nl")).thenReturn(iri1);

		when(iri1.stringValue()).thenReturn("http://IRI1.nl");

		when(valueFactory.createLiteral("value1")).thenReturn(literal1);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri1, literal1) [null]");

	}

	@Test
	public void testCreateRfdModel2()
	{
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr2 = mock(Attribute.class);

		IRI iri2 = mock(IRI.class);
		Literal literal2 = mock(Literal.class);
		when(iri2.toString()).thenReturn("iri2");
		when(literal2.toString()).thenReturn("literal2");

		Multimap<Relation, LabeledResource> multiMap2 = mock(Multimap.class);

		LabeledResource tag2 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr2);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName2")).thenReturn(2);
		when(objectEntity.getInt("attributeName2")).thenReturn(2);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr2.getName()).thenReturn("attributeName2");

		when(attr2.getDataType()).thenReturn(AttributeType.INT);

		when(tag2.getIri()).thenReturn("http://IRI2.nl");

		when(tagService.getTagsForAttribute(entityType, attr2)).thenReturn(multiMap2);

		when(multiMap2.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag2));

		when(valueFactory.createIRI("http://IRI2.nl")).thenReturn(iri2);

		when(iri2.stringValue()).thenReturn("http://IRI2.nl");

		when(valueFactory.createLiteral(2)).thenReturn(literal2);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri2, literal2) [null]");

	}

	@Test
	public void testCreateRfdModel3()
	{
		Entity objectEntity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);
		EntityType refEntityType = mock(EntityType.class);

		Attribute attr3 = mock(Attribute.class);

		IRI iri3 = mock(IRI.class);

		Literal literal3 = mock(Literal.class);
		when(iri3.toString()).thenReturn("iri3");
		when(literal3.toString()).thenReturn("literal3");

		Multimap<Relation, LabeledResource> multiMap3 = mock(Multimap.class);

		LabeledResource tag3 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr3);
		List<String> refAttributeList = Collections.singletonList("refAttr");

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName3")).thenReturn(refEntity);
		when(objectEntity.getEntity("attributeName3")).thenReturn(refEntity);

		when(refEntity.getEntityType()).thenReturn(refEntityType);
		when(refEntityType.getAttributeNames()).thenReturn(refAttributeList);
		when(refEntity.getIdValue()).thenReturn("refID");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr3.getName()).thenReturn("attributeName3");

		when(attr3.getDataType()).thenReturn(AttributeType.XREF);

		when(tag3.getIri()).thenReturn("http://IRI3.nl");

		when(tagService.getTagsForAttribute(entityType, attr3)).thenReturn(multiMap3);

		when(multiMap3.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag3));
		when(valueFactory.createIRI("http://IRI3.nl")).thenReturn(iri3);

		when(iri3.stringValue()).thenReturn("http://IRI3.nl");

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri3, ref_iri) [null]");

	}

	@Test
	public void testCreateRfdModel4()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr4 = mock(Attribute.class);

		IRI iri4 = mock(IRI.class);

		Literal literal4 = mock(Literal.class);

		Literal url_literal = mock(Literal.class);
		Literal ref_literal1 = mock(Literal.class);
		Literal ref_literal2 = mock(Literal.class);
		Literal molgenis_literal = mock(Literal.class);

		when(iri4.toString()).thenReturn("iri4");
		when(literal4.toString()).thenReturn("literal4");

		Multimap<Relation, LabeledResource> multiMap4 = mock(Multimap.class);

		LabeledResource tag4 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr4);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName4")).thenReturn(true);
		when(objectEntity.getBoolean("attributeName4")).thenReturn(true);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr4.getName()).thenReturn("attributeName4");

		when(attr4.getDataType()).thenReturn(AttributeType.BOOL);

		when(tag4.getIri()).thenReturn("http://IRI4.nl");


		when(tagService.getTagsForAttribute(entityType, attr4)).thenReturn(multiMap4);

		when(multiMap4.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag4));

		when(valueFactory.createIRI("http://IRI4.nl")).thenReturn(iri4);

		when(iri4.stringValue()).thenReturn("http://IRI4.nl");

		when(valueFactory.createLiteral(true)).thenReturn(literal4);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri4, literal4) [null]");
	}

	@Test
	public void testCreateRfdModel5()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr5 = mock(Attribute.class);

		IRI iri5 = mock(IRI.class);

		Literal literal5 = mock(Literal.class);

		when(iri5.toString()).thenReturn("iri5");
		when(literal5.toString()).thenReturn("literal5");

		Multimap<Relation, LabeledResource> multiMap5 = mock(Multimap.class);

		LabeledResource tag5 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr5);

		Date date = mock(Date.class);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName5")).thenReturn(date);
		when(objectEntity.getUtilDate("attributeName5")).thenReturn(date);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr5.getName()).thenReturn("attributeName5");

		when(attr5.getDataType()).thenReturn(AttributeType.DATE);

		when(tag5.getIri()).thenReturn("http://IRI5.nl");

		when(tagService.getTagsForAttribute(entityType, attr5)).thenReturn(multiMap5);

		when(multiMap5.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag5));

		when(valueFactory.createIRI("http://IRI5.nl")).thenReturn(iri5);

		when(iri5.stringValue()).thenReturn("http://IRI5.nl");

		when(valueFactory.createLiteral(date)).thenReturn(literal5);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri5, literal5) [null]");

	}

	@Test
	public void testCreateRfdModel6()
	{
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr6 = mock(Attribute.class);

		IRI iri6 = mock(IRI.class);
		Literal literal6 = mock(Literal.class);

		when(iri6.toString()).thenReturn("iri6");
		when(literal6.toString()).thenReturn("literal6");

		Multimap<Relation, LabeledResource> multiMap6 = mock(Multimap.class);

		LabeledResource tag6 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr6);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName6")).thenReturn(10.0);
		when(objectEntity.getDouble("attributeName6")).thenReturn(10.0);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr6.getName()).thenReturn("attributeName6");

		when(attr6.getDataType()).thenReturn(AttributeType.DECIMAL);

		when(tag6.getIri()).thenReturn("http://IRI6.nl");

		when(tagService.getTagsForAttribute(entityType, attr6)).thenReturn(multiMap6);

		when(multiMap6.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag6));

		when(valueFactory.createIRI("http://IRI6.nl")).thenReturn(iri6);

		when(iri6.stringValue()).thenReturn("http://IRI6.nl");

		when(valueFactory.createLiteral(10.0)).thenReturn(literal6);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri6, literal6) [null]");

	}

	@Test
	public void testCreateRfdModel7()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr7 = mock(Attribute.class);

		IRI iri7 = mock(IRI.class);
		Literal literal7 = mock(Literal.class);
		when(iri7.toString()).thenReturn("iri7");
		when(literal7.toString()).thenReturn("literal7");
		Multimap<Relation, LabeledResource> multiMap7 = mock(Multimap.class);
		LabeledResource tag7 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr7);

		Date date = mock(Date.class);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName7")).thenReturn(987654321L);
		when(objectEntity.getLong("attributeName7")).thenReturn(987654321L);

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr7.getName()).thenReturn("attributeName7");

		when(attr7.getDataType()).thenReturn(AttributeType.LONG);

		when(tag7.getIri()).thenReturn("http://IRI7.nl");

		when(tagService.getTagsForAttribute(entityType, attr7)).thenReturn(multiMap7);

		when(multiMap7.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag7));

		when(valueFactory.createIRI("http://IRI7.nl")).thenReturn(iri7);

		when(iri7.stringValue()).thenReturn("http://IRI7.nl");

		when(valueFactory.createLiteral(987654321L)).thenReturn(literal7);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri7, literal7) [null]");
	}

	@Test
	public void testCreateRfdModel8()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		Entity iriRefEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);
		EntityType refEntityType = mock(EntityType.class);
		EntityType iriRefEntityType = mock(EntityType.class);

		Attribute attr8 = mock(Attribute.class);

		IRI iri8 = mock(IRI.class);

		when(iri8.toString()).thenReturn("iri8");

		Multimap<Relation, LabeledResource> multiMap8 = mock(Multimap.class);

		LabeledResource tag8 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr8);
		List<String> refAttributeList = Collections.singletonList("refAttr");
		List<String> iriRefAttributeList = Collections.singletonList("IRI");

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName8")).thenReturn("value8");
		when(objectEntity.getEntities("attributeName8")).thenReturn(Collections.singletonList(refEntity));

		when(refEntity.getEntityType()).thenReturn(refEntityType);
		when(refEntityType.getAttributeNames()).thenReturn(refAttributeList);
		when(refEntity.getIdValue()).thenReturn("refID");
		when(iriRefEntity.getEntityType()).thenReturn(iriRefEntityType);
		when(iriRefEntityType.getAttributeNames()).thenReturn(iriRefAttributeList);
		when(iriRefEntity.getIdValue()).thenReturn("iriRefID");
		when(iriRefEntity.get("IRI")).thenReturn("http://refIRI.iri");
		when(iriRefEntity.getString("IRI")).thenReturn("http://refIRI.iri");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr8.getName()).thenReturn("attributeName8");

		when(attr8.getDataType()).thenReturn(AttributeType.MREF);

		when(tag8.getIri()).thenReturn("http://IRI8.nl");

		when(tagService.getTagsForAttribute(entityType, attr8)).thenReturn(multiMap8);

		when(multiMap8.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag8));

		when(valueFactory.createIRI("http://IRI8.nl")).thenReturn(iri8);
		when(iri8.stringValue()).thenReturn("http://IRI8.nl");

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri8, ref_iri) [null]");

	}

	@Test
	public void testCreateRfdModel9()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr9 = mock(Attribute.class);
		IRI iri9 = mock(IRI.class);
		Literal literal9a = mock(Literal.class);
		Literal literal9b = mock(Literal.class);
		Literal literal9c = mock(Literal.class);
		when(iri9.toString()).thenReturn("iri9");
		when(literal9a.toString()).thenReturn("literal9a");
		when(literal9b.toString()).thenReturn("literal9b");
		when(literal9c.toString()).thenReturn("literal9c");
		Multimap<Relation, LabeledResource> multiMap9 = mock(Multimap.class);
		LabeledResource tag9 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr9);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("KEYWORDS")).thenReturn("molgenis,genetics,fair");
		when(objectEntity.getString("KEYWORDS")).thenReturn("molgenis,genetics,fair");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr9.getName()).thenReturn("KEYWORDS");

		when(attr9.getDataType()).thenReturn(AttributeType.STRING);
		when(tag9.getIri()).thenReturn("http://www.w3.org/ns/dcat#keyword");
		when(tagService.getTagsForAttribute(entityType, attr9)).thenReturn(multiMap9);
		when(multiMap9.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag9));
		when(valueFactory.createIRI("http://www.w3.org/ns/dcat#keyword")).thenReturn(iri9);
		when(iri9.stringValue()).thenReturn("http://www.w3.org/ns/dcat#keyword");
		when(valueFactory.createLiteral("molgenis")).thenReturn(literal9a);
		when(valueFactory.createLiteral("genetics")).thenReturn(literal9b);
		when(valueFactory.createLiteral("fair")).thenReturn(literal9c);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 3);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri9, literal9a) [null]");
		assertEquals(results.next().toString(),
				"(url_iri, iri9, literal9b) [null]");
		assertEquals(results.next().toString(),
				"(url_iri, iri9, literal9c) [null]");

	}

	@Test
	public void testCreateRfdModel10()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		Entity iriRefEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);
		EntityType refEntityType = mock(EntityType.class);
		EntityType iriRefEntityType = mock(EntityType.class);

		Attribute attr10 = mock(Attribute.class);

		IRI iri10 = mock(IRI.class);

		Literal literal10 = mock(Literal.class);
		when(iri10.toString()).thenReturn("iri10");
		when(literal10.toString()).thenReturn("literal10");
		Multimap<Relation, LabeledResource> multiMap10 = mock(Multimap.class);
		LabeledResource tag10 = mock(LabeledResource.class);

		List<Attribute> attributeList = Collections.singletonList(attr10);
		List<String> refAttributeList = Collections.singletonList("refAttr");
		List<String> iriRefAttributeList = Collections.singletonList("IRI");

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("IRI")).thenReturn("http://refIRI.iri");
		when(objectEntity.getString("IRI")).thenReturn("http://refIRI.iri");
		when(objectEntity.getEntity("IRI")).thenReturn(iriRefEntity);

		when(refEntity.getEntityType()).thenReturn(refEntityType);
		when(refEntityType.getAttributeNames()).thenReturn(refAttributeList);
		when(refEntity.getIdValue()).thenReturn("refID");
		when(iriRefEntity.getEntityType()).thenReturn(iriRefEntityType);
		when(iriRefEntityType.getAttributeNames()).thenReturn(iriRefAttributeList);
		when(iriRefEntity.getIdValue()).thenReturn("iriRefID");
		when(iriRefEntity.get("IRI")).thenReturn("http://refIRI.iri");
		when(iriRefEntity.getString("IRI")).thenReturn("http://refIRI.iri");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr10.getName()).thenReturn("IRI");

		when(attr10.getDataType()).thenReturn(AttributeType.XREF);

		when(tag10.getIri()).thenReturn("http://IRI10.nl");

		when(tagService.getTagsForAttribute(entityType, attr10)).thenReturn(multiMap10);

		when(multiMap10.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag10));
		when(valueFactory.createIRI("http://IRI10.nl")).thenReturn(iri10);
		when(iri10.stringValue()).thenReturn("http://IRI10.nl");
		when(valueFactory.createLiteral("http://refIRI.iri")).thenReturn(literal10);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri10, ref_iri2) [null]");

	}

	@Test
	public void testCreateRfdModel12()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);

		Attribute attr11 = mock(Attribute.class);
		Attribute attr12 = mock(Attribute.class);

		IRI iri11 = mock(IRI.class);
		IRI iri12 = mock(IRI.class);

		Literal literal12 = mock(Literal.class);

		when(iri11.toString()).thenReturn("iri11");
		when(iri12.toString()).thenReturn("iri12");
		when(literal12.toString()).thenReturn("literal12");

		Multimap<Relation, LabeledResource> multiMap11 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap12 = mock(Multimap.class);

		LabeledResource tag11 = mock(LabeledResource.class);
		LabeledResource tag12 = mock(LabeledResource.class);

		List<Attribute> attributeList = Arrays
				.asList(attr11, attr12);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName11")).thenReturn(null);
		when(objectEntity.get("attributeName12")).thenReturn("http://www/molgenis.org");
		when(objectEntity.getString("attributeName12")).thenReturn("http://www/molgenis.org");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr11.getName()).thenReturn("attributeName11");
		when(attr12.getName()).thenReturn("attributeName12");

		when(attr11.getDataType()).thenReturn(AttributeType.STRING);
		when(attr12.getDataType()).thenReturn(AttributeType.HYPERLINK);

		when(tag11.getIri()).thenReturn("http://IRI11.nl");
		when(tag12.getIri()).thenReturn("http://IRI12.nl");

		when(tagService.getTagsForAttribute(entityType, attr11)).thenReturn(multiMap11);
		when(tagService.getTagsForAttribute(entityType, attr12)).thenReturn(multiMap12);

		when(multiMap11.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag11));
		when(multiMap12.get(Relation.isAssociatedWith)).thenReturn(Collections.singletonList(tag12));

		when(valueFactory.createIRI("http://IRI11.nl")).thenReturn(iri11);
		when(valueFactory.createIRI("http://IRI12.nl")).thenReturn(iri12);

		when(iri11.stringValue()).thenReturn("http://IRI11.nl");
		when(iri12.stringValue()).thenReturn("http://IRI12.nl");

		when(valueFactory.createLiteral("http://www/molgenis.org")).thenReturn(literal12);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 1);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(url_iri, iri12, molgenis_iri) [null]");

	}
}
