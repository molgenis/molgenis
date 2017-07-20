package org.molgenis.data.importer.emx;

import org.mockito.Mock;
import org.molgenis.data.meta.DefaultPackage;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class IntermediateParseResultsTest extends AbstractMockitoTest
{
	private IntermediateParseResults intermediateParseResults;
	@Mock
	private EntityTypeFactory entityTypeFactory;
	@Mock
	private DefaultPackage defaultPackage;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		intermediateParseResults = new IntermediateParseResults(entityTypeFactory, defaultPackage);
	}

	@Test
	public void testAddEntityType() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.setLabel(any())).thenReturn(entityType);
		when(entityType.setPackage(any())).thenReturn(entityType);

		when(entityTypeFactory.create("base_entityType")).thenReturn(entityType);

		assertEquals(intermediateParseResults.addEntityType("entityType"), entityType);
		verify(entityType).setLabel("entityType");
	}
}