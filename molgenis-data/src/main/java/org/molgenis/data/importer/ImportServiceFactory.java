package org.molgenis.data.importer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.util.FileExtensionUtil;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class ImportServiceFactory
{
	private final Map<String, ImportService> importServicesMapedToExtentions = Maps.newHashMap();
	private final List<ImportService> importServices = Lists.newArrayList();

	public void addImportService(ImportService importService)
	{
		importServices.add(importService);
		for (String extension : importService.getSupportedFileExtensions())
		{
			importServicesMapedToExtentions.put(extension.toLowerCase(), importService);
		}
		Collections.sort(importServices, OrderComparator.INSTANCE);
	}
	

	/**
	 * Finds a suitable ImportService for a FileRepositoryCollection.
	 * 
	 * Import of mixed backend types in one FileRepositoryCollection isn't supported.
	 * 
	 * @throws MolgenisDataException
	 *             if no suitable ImportService is found for the FileRepositoryCollection
	 * @param source
	 * @return
	 */
	public ImportService getImportService(File file, RepositoryCollection source)
	{
		String extension = FileExtensionUtil.findTheClosestFileExtansionFromSet(file.getName(),
				importServicesMapedToExtentions.keySet());

		final ImportService importService = importServicesMapedToExtentions.get(extension);

		System.out.println("extension: " + extension);

		if (importService == null)
			throw new MolgenisDataException("Can not import file. No suitable importer found");

		System.out.println("SupportedFileExtensions" + importService.getSupportedFileExtensions());

		if (importService.canImport(file, source))
		{
			return importService;
		}
		else
		{
			throw new MolgenisDataException("Can not import file. No suitable importer found");
		}
	}
}
