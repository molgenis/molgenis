package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.ontology.beans.AbstractSemanticEntity;
import org.molgenis.ontology.beans.OntologyEntity;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

public class IndexEntityTest
{
	AbstractSemanticEntity indexEntity;
	DefaultEntityMetaData entityMetaData;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		entityMetaData = new DefaultEntityMetaData("indexEntity");
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData("id");
		attributeMetaData.setIdAttribute(true);
		entityMetaData.addAttributeMetaData(attributeMetaData);
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("name"));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData("description"));
		DefaultAttributeMetaData attributeMetaData_2 = new DefaultAttributeMetaData("attributes");
		attributeMetaData_2.setDataType(MolgenisFieldTypes.MREF);
		entityMetaData.addAttributeMetaData(attributeMetaData_2);
		entityMetaData.setIdAttribute("id");

		Entity entity = mock(Entity.class);
		when(entity.getIdValue()).thenReturn("forged-ID");
		indexEntity = new OntologyEntity(entity, entityMetaData, null, null, null);
	}

	@Test
	public void getAttributeNames()
	{
		assertEquals(Iterables.size(indexEntity.getAttributeNames()), 4);
		List<String> attributeNames = Arrays.asList("id", "name", "description", "attributes");
		for (String name : indexEntity.getAttributeNames())
		{
			assertTrue(attributeNames.contains(name));
		}
	}

	@Test
	public void getEntityMetaData()
	{
		assertTrue(indexEntity.getEntityMetaData().equals(entityMetaData));
	}

	@Test
	public void getIdValue()
	{
		assertEquals(indexEntity.getIdValue(), "forged-ID");
	}

	@Test
	public void getLabelAttributeNames()
	{
		assertEquals(Iterables.size(indexEntity.getLabelAttributeNames()), 1);

		for (String name : indexEntity.getLabelAttributeNames())
		{
			assertEquals(name, "id");
		}
	}
}
