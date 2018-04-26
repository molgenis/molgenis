package org.molgenis.oneclickimporter.job;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.mockito.Mock;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.Progress;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadFile;

public class OneClickImportJobTest
{
	@Mock
	private ExcelService excelService;

	@Mock
	private CsvService csvService;

	@Mock
	private OneClickImporterService oneClickImporterService;

	@Mock
	private OneClickImporterNamingService oneClickImporterNamingService;

	@Mock
	private EntityService entityService;

	@Mock
	private FileStore fileStore;

	private OneClickImportJob oneClickImporterJob;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
	}

	@Test
	public void testGetEntityTypeWithExcel()
			throws InvalidFormatException, IOException, URISyntaxException, UnknownFileTypeException,
			EmptySheetException
	{
		Progress progress = mock(Progress.class);
		String filename = "simple-valid.xlsx";

		when(oneClickImporterNamingService.createValidIdFromFileName(filename)).thenReturn("simple_valid");

		File file = loadFile(OneClickImportJobTest.class, "/" + filename);
		when(fileStore.getFile(filename)).thenReturn(file);

		List<Sheet> sheets = new ArrayList<>();
		when(excelService.buildExcelSheetsFromFile(file)).thenReturn(sheets);

		DataCollection dataCollection = mock(DataCollection.class);
		when(dataCollection.getName()).thenReturn("Sheet1");
		when(oneClickImporterService.buildDataCollectionsFromExcel(sheets)).thenReturn(newArrayList(dataCollection));

		EntityType entityType = mock(EntityType.class);
		when(entityService.createEntityType(dataCollection, "simple_valid")).thenReturn(entityType);

		oneClickImporterJob = new OneClickImportJob(excelService, csvService, oneClickImporterService,
				oneClickImporterNamingService, entityService, fileStore);

		oneClickImporterJob.getEntityType(progress, filename);

		verify(progress).status("Preparing import");
		verify(excelService).buildExcelSheetsFromFile(file);
		verify(oneClickImporterService).buildDataCollectionsFromExcel(sheets);
		verify(progress).status("Importing [Sheet1] into package [simple_valid]");
		verify(entityService).createEntityType(dataCollection, "simple_valid");
	}

	@Test
	public void testGetEntityTypeWithCsv()
			throws UnknownFileTypeException, InvalidFormatException, IOException, URISyntaxException,
			EmptySheetException
	{
		Progress progress = mock(Progress.class);
		String filename = "simple-valid.csv";

		when(oneClickImporterNamingService.createValidIdFromFileName(filename)).thenReturn("simple_valid");

		File file = loadFile(OneClickImportJobTest.class, "/" + filename);
		when(fileStore.getFile(filename)).thenReturn(file);

		List<String[]> lines = new ArrayList<>();
		lines.add(new String[] { "name,age", "piet,25" });
		when(csvService.buildLinesFromFile(file)).thenReturn(lines);

		DataCollection dataCollection = mock(DataCollection.class);
		when(dataCollection.getName()).thenReturn("file_1");
		when(oneClickImporterService.buildDataCollectionFromCsv("simple_valid", lines)).thenReturn(dataCollection);

		EntityType entityType = mock(EntityType.class);
		when(entityService.createEntityType(dataCollection, "simple_valid")).thenReturn(entityType);

		oneClickImporterJob = new OneClickImportJob(excelService, csvService, oneClickImporterService,
				oneClickImporterNamingService, entityService, fileStore);

		oneClickImporterJob.getEntityType(progress, filename);

		verify(progress).status("Preparing import");
		verify(csvService).buildLinesFromFile(file);
		verify(oneClickImporterService).buildDataCollectionFromCsv("simple_valid", lines);
		verify(progress).status("Importing [file_1] into package [simple_valid]");
		verify(entityService).createEntityType(dataCollection, "simple_valid");
	}

	@Test
	public void testGetEntityTypeWithZip()
			throws InvalidFormatException, IOException, URISyntaxException, UnknownFileTypeException,
			EmptySheetException
	{
		Progress progress = mock(Progress.class);
		String filename = "simple-valid.zip";

		when(oneClickImporterNamingService.createValidIdFromFileName(filename)).thenReturn("simple_valid");

		File file = loadFile(OneClickImportJobTest.class, "/" + filename);
		when(fileStore.getFile(filename)).thenReturn(file);

		File zipFile1 = loadFile(OneClickImportJobTest.class, "/zip_file_1.csv");
		when(oneClickImporterNamingService.createValidIdFromFileName("zip_file_1.csv")).thenReturn("zip_file_1");

		File zipFile2 = loadFile(OneClickImportJobTest.class, "/zip_file_2.csv");
		when(oneClickImporterNamingService.createValidIdFromFileName("zip_file_2.csv")).thenReturn("zip_file_2");

		File zipFile3 = loadFile(OneClickImportJobTest.class, "/zip_file_3.csv");
		when(oneClickImporterNamingService.createValidIdFromFileName("zip_file_3.csv")).thenReturn("zip_file_3");

		File zipFile4 = loadFile(OneClickImportJobTest.class, "/zip_file_4.csv");
		when(oneClickImporterNamingService.createValidIdFromFileName("zip_file_4.csv")).thenReturn("zip_file_4");

		List<String[]> lines1 = new ArrayList<>();
		lines1.add(new String[] { "name,age", "piet,25" });
		when(csvService.buildLinesFromFile(zipFile1)).thenReturn(lines1);

		List<String[]> lines2 = new ArrayList<>();
		lines2.add(new String[] { "name,age", "klaas,30" });
		when(csvService.buildLinesFromFile(zipFile2)).thenReturn(lines2);

		List<String[]> lines3 = new ArrayList<>();
		lines3.add(new String[] { "name,age", "Jan,35" });
		when(csvService.buildLinesFromFile(zipFile3)).thenReturn(lines3);

		List<String[]> lines4 = new ArrayList<>();
		lines4.add(new String[] { "name,age", "Henk,40" });
		when(csvService.buildLinesFromFile(zipFile4)).thenReturn(lines4);

		DataCollection dataCollection1 = mock(DataCollection.class);
		when(dataCollection1.getName()).thenReturn("zip_file_1");
		when(oneClickImporterService.buildDataCollectionFromCsv("zip_file_1", lines1)).thenReturn(dataCollection1);

		DataCollection dataCollection2 = mock(DataCollection.class);
		when(dataCollection2.getName()).thenReturn("zip_file_2");
		when(oneClickImporterService.buildDataCollectionFromCsv("zip_file_2", lines2)).thenReturn(dataCollection2);

		DataCollection dataCollection3 = mock(DataCollection.class);
		when(dataCollection3.getName()).thenReturn("zip_file_3");
		when(oneClickImporterService.buildDataCollectionFromCsv("zip_file_3", lines3)).thenReturn(dataCollection3);

		DataCollection dataCollection4 = mock(DataCollection.class);
		when(dataCollection4.getName()).thenReturn("zip_file_4");
		when(oneClickImporterService.buildDataCollectionFromCsv("zip_file_4", lines4)).thenReturn(dataCollection4);

		EntityType entityType1 = mock(EntityType.class);
		when(entityService.createEntityType(dataCollection1, "simple_valid")).thenReturn(entityType1);

		EntityType entityType2 = mock(EntityType.class);
		when(entityService.createEntityType(dataCollection2, "simple_valid")).thenReturn(entityType2);

		EntityType entityType3 = mock(EntityType.class);
		when(entityService.createEntityType(dataCollection3, "simple_valid")).thenReturn(entityType3);

		EntityType entityType4 = mock(EntityType.class);
		when(entityService.createEntityType(dataCollection4, "simple_valid")).thenReturn(entityType4);

		oneClickImporterJob = new OneClickImportJob(excelService, csvService, oneClickImporterService,
				oneClickImporterNamingService, entityService, fileStore);

		oneClickImporterJob.getEntityType(progress, filename);

		verify(progress).status("Preparing import");
		verify(csvService).buildLinesFromFile(zipFile1);
		verify(oneClickImporterService).buildDataCollectionFromCsv("zip_file_1", lines1);

		verify(csvService).buildLinesFromFile(zipFile2);
		verify(oneClickImporterService).buildDataCollectionFromCsv("zip_file_2", lines2);

		verify(csvService).buildLinesFromFile(zipFile3);
		verify(oneClickImporterService).buildDataCollectionFromCsv("zip_file_3", lines3);

		verify(csvService).buildLinesFromFile(zipFile4);
		verify(oneClickImporterService).buildDataCollectionFromCsv("zip_file_4", lines4);

		verify(progress).status("Importing [zip_file_1] into package [simple_valid]");
		verify(entityService).createEntityType(dataCollection1, "simple_valid");

		verify(progress).status("Importing [zip_file_2] into package [simple_valid]");
		verify(entityService).createEntityType(dataCollection2, "simple_valid");

		verify(progress).status("Importing [zip_file_3] into package [simple_valid]");
		verify(entityService).createEntityType(dataCollection3, "simple_valid");

		verify(progress).status("Importing [zip_file_4] into package [simple_valid]");
		verify(entityService).createEntityType(dataCollection4, "simple_valid");
	}

	@Test(expectedExceptions = UnknownFileTypeException.class, expectedExceptionsMessageRegExp = "Zip file contains files which are not of type CSV")
	public void testInvalidZipContent()
			throws InvalidFormatException, IOException, URISyntaxException, UnknownFileTypeException,
			EmptySheetException
	{
		Progress progress = mock(Progress.class);
		String filename = "unsupported-file-zip.zip";

		File file = loadFile(OneClickImportJobTest.class, "/" + filename);
		when(fileStore.getFile(filename)).thenReturn(file);

		oneClickImporterJob = new OneClickImportJob(excelService, csvService, oneClickImporterService,
				oneClickImporterNamingService, entityService, fileStore);

		oneClickImporterJob.getEntityType(progress, filename);
	}

	@Test(expectedExceptions = UnknownFileTypeException.class, expectedExceptionsMessageRegExp = "Zip file contains files which are not of type CSV")
	public void testInvalidZipContentWithImage()
			throws InvalidFormatException, IOException, URISyntaxException, UnknownFileTypeException,
			EmptySheetException
	{
		Progress progress = mock(Progress.class);
		String filename = "unsupported-file-zip2.zip";

		File file = loadFile(OneClickImportJobTest.class, "/" + filename);
		when(fileStore.getFile(filename)).thenReturn(file);

		oneClickImporterJob = new OneClickImportJob(excelService, csvService, oneClickImporterService,
				oneClickImporterNamingService, entityService, fileStore);

		oneClickImporterJob.getEntityType(progress, filename);
	}

	@Test(expectedExceptions = UnknownFileTypeException.class, expectedExceptionsMessageRegExp = "File \\[unsupported-file-type.nft\\] does not have a valid extension, supported: \\[csv, xlsx, zip, xls\\]")
	public void testInvalidFileType()
			throws InvalidFormatException, IOException, URISyntaxException, UnknownFileTypeException,
			EmptySheetException
	{
		Progress progress = mock(Progress.class);
		String filename = "unsupported-file-type.nft";

		File file = loadFile(OneClickImportJobTest.class, "/" + filename);
		when(fileStore.getFile(filename)).thenReturn(file);

		oneClickImporterJob = new OneClickImportJob(excelService, csvService, oneClickImporterService,
				oneClickImporterNamingService, entityService, fileStore);

		oneClickImporterJob.getEntityType(progress, filename);
	}
}
