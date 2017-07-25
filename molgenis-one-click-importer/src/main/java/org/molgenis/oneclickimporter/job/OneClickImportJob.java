package org.molgenis.oneclickimporter.job;

import com.google.common.collect.Sets;
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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.util.FileExtensionUtils.findExtensionFromPossibilities;
import static org.molgenis.util.FileExtensionUtils.getFileNameWithoutExtension;

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

	public List<EntityType> getEntityType(Progress progress, String filename)
			throws UnknownFileTypeException, IOException, InvalidFormatException
	{
		File file = fileStore.getFile(filename);

		String fileExtension = findExtensionFromPossibilities(filename, Sets.newHashSet("csv", "xlsx", "zip", "xls"));

		List<DataCollection> dataCollections;
		if (fileExtension.equals("xls") || fileExtension.equals("xlsx"))
		{
			List<Sheet> sheets = excelService.buildExcelSheetsFromFile(file);
			dataCollections = oneClickImporterService.buildDataCollection(sheets);
		}
		//		else if (fileExtension.equals("csv"))
		//		{
		//			List<String> lines = csvService.buildLinesFromFile(file);
		//			progress.progress(2, "Creating dataCollection");
		//			dataCollection = oneClickImporterService.buildDataCollection(dataCollectionName, lines);
		//		}
		else
		{
			throw new UnknownFileTypeException(
					String.format("File with extension: %s is not a valid one-click importer file", fileExtension));
		}

		List<EntityType> entityTypes = newArrayList();
		String packageName = createValidPackageNameFromFileName(filename);
		dataCollections.forEach(
				dataCollection -> entityTypes.add(entityService.createEntityType(dataCollection, packageName)));

		// TODO permissionSystemService.giveUserWriteMetaPermissions(entityTypes);
		return entityTypes;
	}

	private String createValidPackageNameFromFileName(String filename)
	{
		String packageName = getFileNameWithoutExtension(filename);
		// TODO regex it up "[^a-zA-Z0-9_#]+"
		return packageName.replace(" ", "_");
	}
}
