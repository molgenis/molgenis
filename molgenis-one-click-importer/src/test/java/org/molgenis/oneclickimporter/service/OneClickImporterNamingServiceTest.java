package org.molgenis.oneclickimporter.service;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.oneclickimporter.service.impl.OneClickImporterNamingServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.LABEL;
import static org.testng.Assert.assertEquals;

public class OneClickImporterNamingServiceTest
{
	@Mock
	private DataService dataService;
	private OneClickImporterNamingService oneClickImporterNamingService;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
	}

	@Test
	public void testGetLabelWithPostFixWhenNoDuplicate()
	{
		String label = "label";
		when(dataService.findAll(ENTITY_TYPE_META_DATA, new QueryImpl<EntityType>().like(LABEL, label),
				EntityType.class)).thenReturn(Stream.empty());

		oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
		String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
		String expected = "label";

		assertEquals(actual, expected);
	}

	@Test
	public void testGetLabelWithPostFixWhenOneDuplicate()
	{
		EntityType e1 = Mockito.mock(EntityType.class);
		when(e1.getLabel()).thenReturn("label");

		String label = "label";
		when(dataService.findAll(ENTITY_TYPE_META_DATA, new QueryImpl<EntityType>().like(LABEL, label),
				EntityType.class)).thenReturn(Stream.of(e1));

		oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
		String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
		String expected = "label (1)";

		assertEquals(actual, expected);
	}

	@Test
	public void testGetLabelWithPostFixWhenFiveDuplicate()
	{
		EntityType e1 = Mockito.mock(EntityType.class);
		when(e1.getLabel()).thenReturn("label");

		EntityType e2 = Mockito.mock(EntityType.class);
		when(e2.getLabel()).thenReturn("label (1)");

		EntityType e3 = Mockito.mock(EntityType.class);
		when(e3.getLabel()).thenReturn("label (2)");

		EntityType e4 = Mockito.mock(EntityType.class);
		when(e4.getLabel()).thenReturn("label (3)");

		EntityType e5 = Mockito.mock(EntityType.class);
		when(e5.getLabel()).thenReturn("label (4)");

		String label = "label";
		when(dataService.findAll(ENTITY_TYPE_META_DATA, new QueryImpl<EntityType>().like(LABEL, label),
				EntityType.class)).thenReturn(Stream.of(e1, e2, e3, e4, e5));

		oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
		String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
		String expected = "label (5)";

		assertEquals(actual, expected);
	}

	@Test
	public void testGetLabelWithPostFixDuplicateInNoOrder()
	{
		EntityType e1 = Mockito.mock(EntityType.class);
		when(e1.getLabel()).thenReturn("label");

		EntityType e2 = Mockito.mock(EntityType.class);
		when(e2.getLabel()).thenReturn("label (1)");

		EntityType e3 = Mockito.mock(EntityType.class);
		when(e3.getLabel()).thenReturn("label (3)");

		String label = "label";
		when(dataService.findAll(ENTITY_TYPE_META_DATA, new QueryImpl<EntityType>().like(LABEL, label),
				EntityType.class)).thenReturn(Stream.of(e1, e2, e3));

		oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
		String actual = oneClickImporterNamingService.getLabelWithPostFix(label);
		String expected = "label (2)";

		assertEquals(actual, expected);
	}

	@Test
	public void testCreateValidIdFromFileName()
	{
		oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
		assertEquals(oneClickImporterNamingService.createValidIdFromFileName("test-file1.xlsx"), "test_file1");
		assertEquals(oneClickImporterNamingService.createValidIdFromFileName("test-f@#.xlsx"), "test_f_#");
		assertEquals(oneClickImporterNamingService.createValidIdFromFileName("test!##%.xlsx"), "test_##_");
	}

	@Test
	public void testAsValidAttributeName()
	{
		oneClickImporterNamingService = new OneClickImporterNamingServiceImpl(dataService);
		assertEquals(oneClickImporterNamingService.asValidColumnName("name#!3"), "name#_3");
	}
}
