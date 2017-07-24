package org.molgenis.oneclickimporter.job;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.jobs.Progress;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.file.FileStore;
import org.molgenis.oneclickimporter.exceptions.UnknownFileTypeException;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.CsvService;
import org.molgenis.oneclickimporter.service.EntityService;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.molgenis.oneclickimporter.service.OneClickImporterService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Component
public class OneClickImportJob
{
	private ExcelService excelService;
	private CsvService csvService;
	private OneClickImporterService oneClickImporterService;
	private EntityService entityService;
	private FileStore fileStore;

	public OneClickImportJob(ExcelService excelService, CsvService csvService,
			OneClickImporterService oneClickImporterService, EntityService entityService, FileStore fileStore)
	{
		this.excelService = requireNonNull(excelService);
		this.csvService = requireNonNull(csvService);
		this.oneClickImporterService = requireNonNull(oneClickImporterService);
		this.entityService = requireNonNull(entityService);
		this.fileStore = requireNonNull(fileStore);
	}

	public EntityType getEntityType(Progress progress, String filename)
			throws UnknownFileTypeException, IOException, InvalidFormatException
	{
		progress.setProgressMax(4);

		progress.progress(0, "Starting import");
		File file = fileStore.getFile(filename);

		String fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
		String dataCollectionName = filename.substring(0, filename.lastIndexOf('.'));

		DataCollection dataCollection;
		if (fileExtension.equals("xls") || fileExtension.equals("xlsx"))
		{
			progress.progress(1, "Creating sheet from Excel");
			Sheet sheet = excelService.buildExcelSheetFromFile(file);
			progress.progress(2, "Creating dataCollection");
			dataCollection = oneClickImporterService.buildDataCollection(dataCollectionName, sheet);
		}
		else if (fileExtension.equals("csv"))
		{
			progress.progress(1, "Reading lines from CSV");
			List<String> lines = csvService.buildLinesFromFile(file);
			progress.progress(2, "Creating dataCollection");
			dataCollection = oneClickImporterService.buildDataCollection(dataCollectionName, lines);
		}
		else
		{
			throw new UnknownFileTypeException(
					String.format("File with extension: %s is not a valid one-click importer file", fileExtension));
		}

		progress.progress(3, "Inserting table into database");
		EntityType entityType = entityService.createEntityType(dataCollection);
		progress.progress(4, "Table created");

		return entityType;
	}
}
