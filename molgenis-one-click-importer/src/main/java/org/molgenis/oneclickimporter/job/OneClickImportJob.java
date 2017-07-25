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
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.requireNonNull;
import static org.molgenis.util.FileExtensionUtils.findExtensionFromPossibilities;
import static org.molgenis.util.FileExtensionUtils.getFileNameWithoutExtension;
import static org.molgenis.util.file.ZipFileUtil.unzip;

@Component
public class OneClickImportJob
{
	private ExcelService excelService;
	private CsvService csvService;
	private OneClickImporterService oneClickImporterService;
	private EntityService entityService;
	private FileStore fileStore;
	private PermissionSystemService permissionSystemService;

	public OneClickImportJob(ExcelService excelService, CsvService csvService,
			OneClickImporterService oneClickImporterService, EntityService entityService, FileStore fileStore,
			PermissionSystemService permissionSystemService)
	{
		this.excelService = requireNonNull(excelService);
		this.csvService = requireNonNull(csvService);
		this.oneClickImporterService = requireNonNull(oneClickImporterService);
		this.entityService = requireNonNull(entityService);
		this.fileStore = requireNonNull(fileStore);
		this.permissionSystemService = requireNonNull(permissionSystemService);
	}

	@Transactional
	public List<EntityType> getEntityType(Progress progress, String filename)
			throws UnknownFileTypeException, IOException, InvalidFormatException
	{
		File file = fileStore.getFile(filename);
		String fileExtension = findExtensionFromPossibilities(filename, newHashSet("csv", "xlsx", "zip", "xls"));

		List<DataCollection> dataCollections = newArrayList();
		if (fileExtension == null)
		{
			throw new UnknownFileTypeException(String.format(
					"File [%s] does not have a valid extension, supported: [csv, xlsx, zip, xls]",
					filename));
		}
		else if (fileExtension.equals("xls") || fileExtension.equals("xlsx"))
		{
			List<Sheet> sheets = excelService.buildExcelSheetsFromFile(file);
			dataCollections.addAll(oneClickImporterService.buildDataCollectionsFromExcel(sheets));
		}
		else if (fileExtension.equals("csv"))
		{
			List<String> lines = csvService.buildLinesFromFile(file);
			dataCollections.add(
					oneClickImporterService.buildDataCollectionFromCsv(createValidNameFromFileName(filename), lines));
		}
		else if (fileExtension.equals("zip"))
		{
			List<File> filesInZip = unzip(file);
			for (File fileInZip : filesInZip)
			{
				String fileInZipExtension = findExtensionFromPossibilities(fileInZip.getName(), newHashSet("csv"));
				if (fileInZipExtension != null)
				{
					List<String> lines = csvService.buildLinesFromFile(fileInZip);
					dataCollections.add(oneClickImporterService.buildDataCollectionFromCsv(
							createValidNameFromFileName(fileInZip.getName()), lines));
				}
				else
				{
					throw new UnknownFileTypeException("Zip file contains files which are not of type CSV");
				}
			}
		}

		List<EntityType> entityTypes = newArrayList();
		String packageName = createValidNameFromFileName(filename);
		dataCollections.forEach(
				dataCollection -> entityTypes.add(entityService.createEntityType(dataCollection, packageName)));

		permissionSystemService.giveUserWriteMetaPermissions(entityTypes);
		return entityTypes;
	}

	private String createValidNameFromFileName(String filename)
	{
		String packageName = getFileNameWithoutExtension(filename);
		return packageName.replaceAll("[^a-zA-Z0-9_#]+", "_");
	}
}
