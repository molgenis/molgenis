package org.molgenis.data.importer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.file.util.FileExtensionUtils;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class ImportServiceFactory
{
	private final List<ImportService> importServices = Lists.newArrayList();

	void addImportService(ImportService importService)
	{
		importServices.add(importService);
		importServices.sort(OrderComparator.INSTANCE);
	}

	/**
	 * Finds a suitable ImportService for a FileRepositoryCollection.
	 * <p>
	 * Import of mixed backend types in one FileRepositoryCollection isn't supported.
	 *
	 * @return ImportService
	 * @throws MolgenisDataException if no suitable ImportService is found for the FileRepositoryCollection
	 */
	public ImportService getImportService(File file, RepositoryCollection source)
	{
		final Map<String, ImportService> importServicesMappedToExtensions = Maps.newHashMap();
		importServices.stream().filter(importService -> importService.canImport(file, source)).forEach(importService ->
		{
			for (String extension : importService.getSupportedFileExtensions())
			{
				importServicesMappedToExtensions.put(extension.toLowerCase(), importService);
			}
		});

		String extension = FileExtensionUtils.findExtensionFromPossibilities(file.getName(),
				importServicesMappedToExtensions.keySet());

		final ImportService importService = importServicesMappedToExtensions.get(extension);

		if (importService == null) throw new MolgenisDataException("Can not import file. No suitable importer found");

		return importService;
	}

	public ImportService getImportService(String fileName)
	{
		final Map<String, ImportService> importServicesMappedToExtensions = Maps.newHashMap();
		for (ImportService importService : importServices)
		{
			for (String extension : importService.getSupportedFileExtensions())
			{
				importServicesMappedToExtensions.put(extension.toLowerCase(), importService);
			}
		}

		String extension = FileExtensionUtils.findExtensionFromPossibilities(fileName,
				importServicesMappedToExtensions.keySet());

		final ImportService importService = importServicesMappedToExtensions.get(extension);

		if (importService == null) throw new MolgenisDataException("Can not import file. No suitable importer found");

		return importService;
	}

	public Set<String> getSupportedFileExtensions()
	{
		return importServices.stream()
							 .flatMap(importService -> importService.getSupportedFileExtensions().stream())
							 .collect(toSet());
	}
}
