package org.molgenis.fair.controller;

import com.google.common.collect.Multimap;
import org.eclipse.rdf4j.model.IRI;
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

	@BeforeMethod
	public void beforeTest() throws DatatypeConfigurationException
	{
		valueFactory = mock(SimpleValueFactory.class);
		tagService = mock(TagService.class);
		writer = new EntityModelWriter(tagService, valueFactory);
	}

	@Test
	public void testCreateRfdModel()
	{
		//public Model createRdfModel(String subjectIRI, Entity objectEntity)
		Entity objectEntity = mock(Entity.class);
		Entity refEntity = mock(Entity.class);
		Entity iriRefEntity = mock(Entity.class);
		EntityType entityType = mock(EntityType.class);
		EntityType refEntityType = mock(EntityType.class);
		EntityType iriRefEntityType = mock(EntityType.class);

		Attribute attr1 = mock(Attribute.class);
		Attribute attr2 = mock(Attribute.class);
		Attribute attr3 = mock(Attribute.class);
		Attribute attr4 = mock(Attribute.class);
		Attribute attr5 = mock(Attribute.class);
		Attribute attr6 = mock(Attribute.class);
		Attribute attr7 = mock(Attribute.class);
		Attribute attr8 = mock(Attribute.class);
		Attribute attr9 = mock(Attribute.class);
		Attribute attr10 = mock(Attribute.class);
		Attribute attr11 = mock(Attribute.class);
		Attribute attr12 = mock(Attribute.class);

		IRI iri1 = mock(IRI.class);
		IRI iri2 = mock(IRI.class);
		IRI iri3 = mock(IRI.class);
		IRI iri4 = mock(IRI.class);
		IRI iri5 = mock(IRI.class);
		IRI iri6 = mock(IRI.class);
		IRI iri7 = mock(IRI.class);
		IRI iri8 = mock(IRI.class);
		IRI iri9 = mock(IRI.class);
		IRI iri10 = mock(IRI.class);
		IRI iri11 = mock(IRI.class);
		IRI iri12 = mock(IRI.class);

		Multimap<Relation, LabeledResource> multiMap1 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap2 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap3 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap4 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap5 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap6 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap7 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap8 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap9 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap10 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap11 = mock(Multimap.class);
		Multimap<Relation, LabeledResource> multiMap12 = mock(Multimap.class);

		LabeledResource tag1 = mock(LabeledResource.class);
		LabeledResource tag2 = mock(LabeledResource.class);
		LabeledResource tag3 = mock(LabeledResource.class);
		LabeledResource tag4 = mock(LabeledResource.class);
		LabeledResource tag5 = mock(LabeledResource.class);
		LabeledResource tag6 = mock(LabeledResource.class);
		LabeledResource tag7 = mock(LabeledResource.class);
		LabeledResource tag8 = mock(LabeledResource.class);
		LabeledResource tag9 = mock(LabeledResource.class);
		LabeledResource tag10 = mock(LabeledResource.class);
		LabeledResource tag11 = mock(LabeledResource.class);
		LabeledResource tag12 = mock(LabeledResource.class);

		List<Attribute> attributeList = Arrays
				.asList(attr1, attr2, attr3, attr4, attr5, attr6, attr7, attr8, attr9, attr10, attr11, attr12);
		List<String> refAttributeList = Arrays.asList("refAttr");
		List<String> iriRefAttributeList = Arrays.asList("IRI");

		Date date = mock(Date.class);

		when(objectEntity.getEntityType()).thenReturn(entityType);
		when(objectEntity.get("attributeName1")).thenReturn("value1");
		when(objectEntity.getString("attributeName1")).thenReturn("value1");
		when(objectEntity.get("attributeName2")).thenReturn("value2");
		when(objectEntity.getInt("attributeName2")).thenReturn(2);
		when(objectEntity.get("attributeName3")).thenReturn("value3");
		when(objectEntity.getEntity("attributeName3")).thenReturn(refEntity);
		when(objectEntity.get("attributeName4")).thenReturn("value4");
		when(objectEntity.getBoolean("attributeName4")).thenReturn(true);
		when(objectEntity.get("attributeName5")).thenReturn("value5");
		when(objectEntity.getUtilDate("attributeName5")).thenReturn(date);
		when(objectEntity.get("attributeName6")).thenReturn("value6");
		when(objectEntity.getDouble("attributeName6")).thenReturn(10.0);
		when(objectEntity.get("attributeName7")).thenReturn("value7");
		when(objectEntity.getLong("attributeName7")).thenReturn(987654321l);
		when(objectEntity.get("attributeName8")).thenReturn("value8");
		when(objectEntity.getEntities("attributeName8")).thenReturn(Collections.singletonList(refEntity));
		when(objectEntity.get("KEYWORDS")).thenReturn("molgenis,genetics,fair");
		when(objectEntity.getString("KEYWORDS")).thenReturn("molgenis,genetics,fair");
		when(objectEntity.get("IRI")).thenReturn("http://refIRI.iri");
		when(objectEntity.getEntity("IRI")).thenReturn(iriRefEntity);
		when(objectEntity.get("attributeName11")).thenReturn(null);
		when(objectEntity.get("attributeName12")).thenReturn("http://www/molgenis.org");
		when(objectEntity.getString("attributeName12")).thenReturn("http://www/molgenis.org");

		when(refEntity.getEntityType()).thenReturn(refEntityType);
		when(refEntityType.getAttributeNames()).thenReturn(refAttributeList);
		when(refEntity.getIdValue()).thenReturn("refID");
		when(iriRefEntity.getEntityType()).thenReturn(iriRefEntityType);
		when(iriRefEntityType.getAttributeNames()).thenReturn(iriRefAttributeList);
		when(iriRefEntity.getIdValue()).thenReturn("iriRefID");
		when(iriRefEntity.get("IRI")).thenReturn("http://refIRI.iri");
		when(iriRefEntity.getString("IRI")).thenReturn("http://refIRI.iri");

		when(entityType.getAtomicAttributes()).thenReturn(attributeList);
		when(attr1.getName()).thenReturn("attributeName1");
		when(attr2.getName()).thenReturn("attributeName2");
		when(attr3.getName()).thenReturn("attributeName3");
		when(attr4.getName()).thenReturn("attributeName4");
		when(attr5.getName()).thenReturn("attributeName5");
		when(attr6.getName()).thenReturn("attributeName6");
		when(attr7.getName()).thenReturn("attributeName7");
		when(attr8.getName()).thenReturn("attributeName8");
		when(attr9.getName()).thenReturn("KEYWORDS");
		when(attr10.getName()).thenReturn("IRI");
		when(attr11.getName()).thenReturn("attributeName11");
		when(attr12.getName()).thenReturn("attributeName12");

		when(attr1.getDataType()).thenReturn(AttributeType.STRING);
		when(attr2.getDataType()).thenReturn(AttributeType.INT);
		when(attr3.getDataType()).thenReturn(AttributeType.XREF);
		when(attr4.getDataType()).thenReturn(AttributeType.BOOL);
		when(attr5.getDataType()).thenReturn(AttributeType.DATE);
		when(attr6.getDataType()).thenReturn(AttributeType.DECIMAL);
		when(attr7.getDataType()).thenReturn(AttributeType.LONG);
		when(attr8.getDataType()).thenReturn(AttributeType.MREF);
		when(attr9.getDataType()).thenReturn(AttributeType.STRING);
		when(attr10.getDataType()).thenReturn(AttributeType.XREF);
		when(attr11.getDataType()).thenReturn(AttributeType.STRING);
		when(attr12.getDataType()).thenReturn(AttributeType.HYPERLINK);

		when(tag1.getIri()).thenReturn("http://IRI1.nl");
		when(tag2.getIri()).thenReturn("http://IRI2.nl");
		when(tag3.getIri()).thenReturn("http://IRI3.nl");
		when(tag4.getIri()).thenReturn("http://IRI4.nl");
		when(tag5.getIri()).thenReturn("http://IRI5.nl");
		when(tag6.getIri()).thenReturn("http://IRI6.nl");
		when(tag7.getIri()).thenReturn("http://IRI7.nl");
		when(tag8.getIri()).thenReturn("http://IRI8.nl");
		when(tag9.getIri()).thenReturn("http://www.w3.org/ns/dcat#keyword");
		when(tag10.getIri()).thenReturn("http://IRI10.nl");
		when(tag11.getIri()).thenReturn("http://IRI11.nl");
		when(tag12.getIri()).thenReturn("http://IRI12.nl");

		when(tagService.getTagsForAttribute(entityType, attr1)).thenReturn(multiMap1);
		when(tagService.getTagsForAttribute(entityType, attr2)).thenReturn(multiMap2);
		when(tagService.getTagsForAttribute(entityType, attr3)).thenReturn(multiMap3);
		when(tagService.getTagsForAttribute(entityType, attr4)).thenReturn(multiMap4);
		when(tagService.getTagsForAttribute(entityType, attr5)).thenReturn(multiMap5);
		when(tagService.getTagsForAttribute(entityType, attr6)).thenReturn(multiMap6);
		when(tagService.getTagsForAttribute(entityType, attr7)).thenReturn(multiMap7);
		when(tagService.getTagsForAttribute(entityType, attr8)).thenReturn(multiMap8);
		when(tagService.getTagsForAttribute(entityType, attr9)).thenReturn(multiMap9);
		when(tagService.getTagsForAttribute(entityType, attr10)).thenReturn(multiMap10);
		when(tagService.getTagsForAttribute(entityType, attr11)).thenReturn(multiMap11);
		when(tagService.getTagsForAttribute(entityType, attr12)).thenReturn(multiMap12);

		when(multiMap1.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag1));
		when(multiMap2.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag2));
		when(multiMap3.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag3));
		when(multiMap4.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag4));
		when(multiMap5.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag5));
		when(multiMap6.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag6));
		when(multiMap7.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag7));
		when(multiMap8.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag8));
		when(multiMap9.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag9));
		when(multiMap10.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag10));
		when(multiMap11.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag11));
		when(multiMap12.get(Relation.isAssociatedWith)).thenReturn(Arrays.asList(tag12));

		when(valueFactory.createIRI("IRI1")).thenReturn(iri1);
		when(valueFactory.createIRI("IRI2")).thenReturn(iri2);
		when(valueFactory.createIRI("IRI3")).thenReturn(iri3);
		when(valueFactory.createIRI("IRI4")).thenReturn(iri4);
		when(valueFactory.createIRI("IRI5")).thenReturn(iri5);
		when(valueFactory.createIRI("IRI6")).thenReturn(iri6);
		when(valueFactory.createIRI("IRI7")).thenReturn(iri7);
		when(valueFactory.createIRI("IRI8")).thenReturn(iri8);
		when(valueFactory.createIRI("IRI9")).thenReturn(iri9);
		when(valueFactory.createIRI("IRI10")).thenReturn(iri10);
		when(valueFactory.createIRI("IRI11")).thenReturn(iri11);
		when(valueFactory.createIRI("IRI12")).thenReturn(iri12);

		Model result = writer.createRdfModel("http://molgenis01.gcc.rug.nl/fdp/catolog/test/this", objectEntity);

		assertEquals(result.size(), 13);
		Iterator results = result.iterator();
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI1.nl, \"value1\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI2.nl, \"2\"^^<http://www.w3.org/2001/XMLSchema#int>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI3.nl, http://molgenis01.gcc.rug.nl/fdp/catolog/test/this/refID) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI4.nl, \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI5.nl, \"1970-01-01T01:00:00.000+01:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI6.nl, \"10.0\"^^<http://www.w3.org/2001/XMLSchema#double>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI7.nl, \"987654321\"^^<http://www.w3.org/2001/XMLSchema#long>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI8.nl, http://molgenis01.gcc.rug.nl/fdp/catolog/test/this/refID) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://www.w3.org/ns/dcat#keyword, \"molgenis\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://www.w3.org/ns/dcat#keyword, \"genetics\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://www.w3.org/ns/dcat#keyword, \"fair\"^^<http://www.w3.org/2001/XMLSchema#string>) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI10.nl, http://refIRI.iri) [null]");
		assertEquals(results.next().toString(),
				"(http://molgenis01.gcc.rug.nl/fdp/catolog/test/this, http://IRI12.nl, http://www/molgenis.org) [null]");

	}
}
