package org.molgenis.gaf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testng.annotations.Test;

public class GafListFileRepositoryTest
{
	@Autowired
	private GafListFileImporterService gafListFileImporterService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private DataService dataService;

	@Test(expectedExceptions = java.lang.NullPointerException.class)
	public void GafListFileRepository() throws IOException
	{
		GafListFileRepository gafListFileRepository = null;
		try
		{
			gafListFileRepository = new GafListFileRepository(null, null, null, null);
		}
		finally
		{
			if (gafListFileRepository != null) gafListFileRepository.close();
		}
	}

	@Test
	public void iteratorWithoutReport() throws IOException
	{
		File file = ResourceUtils.getFile(getClass(), "/flowexport_test_gaflistfilerepository.csv");
		@SuppressWarnings("resource")
		GafListFileRepository gafListFileRepository = new GafListFileRepository(file, null, null, null);
		Iterator<Entity> it = gafListFileRepository.iterator();
		assertTrue(it.hasNext());
		Entity firstEntity = it.next();
		String barcode1 = firstEntity.getString("Barcode_1");
		String barcode = firstEntity.getString("barcode");
		String barcodeType = firstEntity.getString("barcodeType");
		assertEquals(barcode1, "AGI 1 AAGGTTCC");
		assertEquals(barcode, "AAGGTTCC");
		assertEquals(barcodeType, "AGI");
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}

	@Test
	public void iteratorWithReport() throws IOException
	{
		File file = ResourceUtils.getFile(getClass(), "/flowexport_test_gaflistfilerepository.csv");

		GafListValidationReport gafListValidationReport = mock(GafListValidationReport.class);
		when(gafListValidationReport.hasErrors("1500")).thenReturn(false);
		when(gafListValidationReport.hasErrors("1501")).thenReturn(true);

		@SuppressWarnings("resource")
		GafListFileRepository gafListFileRepository = new GafListFileRepository(file, null, null,
				gafListValidationReport);
		Iterator<Entity> it = gafListFileRepository.iterator();
		assertTrue(it.hasNext());
		Entity firstEntity = it.next();
		String barcode1 = firstEntity.getString("Barcode_1");
		String barcode = firstEntity.getString("barcode");
		String barcodeType = firstEntity.getString("barcodeType");
		assertEquals(barcode1, "AGI 1 AAGGTTCC");
		assertEquals(barcode, "AAGGTTCC");
		assertEquals(barcodeType, "AGI");
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public GafListFileImporterService gafListFileImporterService()
		{
			return mock(GafListFileImporterService.class);
		}
	}
}
