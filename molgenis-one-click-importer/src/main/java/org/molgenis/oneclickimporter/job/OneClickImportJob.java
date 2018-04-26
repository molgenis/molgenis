package org.molgenis.oneclickimporter.job;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.Progress;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.file.util.FileExtensionUtils.findExtensionFromPossibilities;
import static org.molgenis.util.file.ZipFileUtil.unzip;

@Component
public class OneClickImportJob
{
	private final ExcelService excelService;
	private final CsvService csvService;
	private final OneClickImporterService oneClickImporterService;
	private final OneClickImporterNamingService oneClickImporterNamingService;
	private final EntityService entityService;
	private final FileStore fileStore;

	public OneClickImportJob(ExcelService excelService, CsvService csvService,
			OneClickImporterService oneClickImporterService,
			OneClickImporterNamingService oneClickImporterNamingService, EntityService entityService,
			FileStore fileStore)
	{
		this.excelService = requireNonNull(excelService);
		this.csvService = requireNonNull(csvService);
		this.oneClickImporterService = requireNonNull(oneClickImporterService);
		this.oneClickImporterNamingService = requireNonNull(oneClickImporterNamingService);
		this.entityService = requireNonNull(entityService);
		this.fileStore = requireNonNull(fileStore);
	}

	@Transactional
	public List<EntityType> getEntityType(Progress progress, String filename)
			throws UnknownFileTypeException, IOException, InvalidFormatException, EmptySheetException
	{
		File file = fileStore.getFile(filename);
		String fileExtension = findExtensionFromPossibilities(filename, newHashSet("csv", "xlsx", "zip", "xls"));

		progress.status("Preparing import");
		List<DataCollection> dataCollections = newArrayList();
		if (fileExtension == null)
		{
			throw new UnknownFileTypeException(
					String.format("File [%s] does not have a valid extension, supported: [csv, xlsx, zip, xls]",
							filename));
		}
		else if (fileExtension.equals("xls") || fileExtension.equals("xlsx"))
		{
			List<Sheet> sheets = excelService.buildExcelSheetsFromFile(file);
			dataCollections.addAll(oneClickImporterService.buildDataCollectionsFromExcel(sheets));
		}
		else if (fileExtension.equals("csv"))
		{
			List<String[]> lines = csvService.buildLinesFromFile(file);
			dataCollections.add(oneClickImporterService.buildDataCollectionFromCsv(
					oneClickImporterNamingService.createValidIdFromFileName(filename), lines));
		}
		else if (fileExtension.equals("zip"))
		{
			List<File> filesInZip = unzip(file);
			for (File fileInZip : filesInZip)
			{
				String fileInZipExtension = findExtensionFromPossibilities(fileInZip.getName(), newHashSet("csv"));
				if (fileInZipExtension != null)
				{
					List<String[]> lines = csvService.buildLinesFromFile(fileInZip);
					dataCollections.add(oneClickImporterService.buildDataCollectionFromCsv(
							oneClickImporterNamingService.createValidIdFromFileName(fileInZip.getName()), lines));
				}
				else
				{
					throw new UnknownFileTypeException("Zip file contains files which are not of type CSV");
				}
			}
		}

		List<EntityType> entityTypes = newArrayList();
		String packageName = oneClickImporterNamingService.createValidIdFromFileName(filename);
		dataCollections.forEach(dataCollection ->
		{
			progress.status("Importing [" + dataCollection.getName() + "] into package [" + packageName + "]");
			entityTypes.add(entityService.createEntityType(dataCollection, packageName));
		});

		return entityTypes;
	}
}
