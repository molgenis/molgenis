package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.search.Hit;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

public class IndexEntityTest
{
	IndexEntity indexEntity;
	DefaultEntityMetaData entityMetaData;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		entityMetaData = new DefaultEntityMetaData("indexEntity");
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(Characteristic.ID));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(Characteristic.NAME));
		entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(Characteristic.DESCRIPTION));
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData("attributes");
		attributeMetaData.setDataType(MolgenisFieldTypes.MREF);
		entityMetaData.addAttributeMetaData(attributeMetaData);
		entityMetaData.setIdAttribute(Characteristic.ID);

		Hit hit = mock(Hit.class);
		when(hit.getId()).thenReturn("forged-ID");
		indexEntity = new OntologyIndexEntity(hit, entityMetaData, null, null);
	}

	@Test
	public void getAttributeNames()
	{
		assertEquals(Iterables.size(indexEntity.getAttributeNames()), 4);
		List<String> attributeNames = Arrays.asList(Characteristic.ID, Characteristic.NAME, Characteristic.DESCRIPTION,
				"attributes");
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
			assertEquals(name, Characteristic.ID);
		}
	}
}
