package org.molgenis.data.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class ImportServiceFactory
{
	private final List<ImportService> importServices = Lists.newArrayList();

	public void addImportService(ImportService importService)
	{
		importServices.add(importService);
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
		List<ImportService> possibleImportServices = new ArrayList<ImportService>();
		for (ImportService importService : importServices)
		{
			if (importService.canImport(file, source))
			{
				possibleImportServices.add(importService);
			}
		}

		String name = file.getName().toLowerCase();
		Map<String, ImportService> possibleExtensions = new HashMap<String, ImportService>();
		for (ImportService importSerivce : possibleImportServices)
		{
			for (String extension : importSerivce.getSupportedFileExtensions())
			{
				if (name.endsWith('.' + extension))
				{
					if (possibleExtensions.containsKey(extension)) throw new MolgenisDataException(
							"Cannot have the same file extension registered in multiple ImportServices : "
									+ importSerivce.getClass().getSimpleName() + ", "
									+ possibleExtensions.get(extension).getClass().getSimpleName());

					possibleExtensions.put(extension, importSerivce);
				}
			}
		}

		String longestExtention = "";
		for (String possibleExtension : possibleExtensions.keySet())
		{
			if (longestExtention.length() < possibleExtension.length()) longestExtention = possibleExtension;
		}

		if (!possibleExtensions.containsKey(longestExtention)) throw new MolgenisDataException(
				"Can not import file. No suitable importer found");

		return possibleExtensions.get(longestExtention);
	}
}
