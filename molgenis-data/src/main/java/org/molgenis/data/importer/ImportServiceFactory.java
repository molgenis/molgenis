package org.molgenis.data.importer;

import java.io.File;
import java.util.Collections;
import java.util.List;

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
		for (ImportService importService : importServices)
		{
			if (importService.canImport(file, source)) return importService;
		}

		throw new MolgenisDataException("Can not import file. No suitable importer found");
	}
}
