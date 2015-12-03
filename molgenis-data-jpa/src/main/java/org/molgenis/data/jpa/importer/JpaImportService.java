package org.molgenis.data.jpa.importer;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntitiesValidator;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.util.FileExtensionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Importer for jpa entities
 */
@Component
public class JpaImportService implements ImportService
{
	private final EntitiesValidator entitiesValidator;
	private final EntitiesImporter entitiesImporter;
	private final RepositoryCollection targetCollection;

	@Autowired
	public JpaImportService(EntitiesValidator entitiesValidator, EntitiesImporter entitiesImporter,
			@Qualifier("JpaRepositoryCollection") RepositoryCollection repositoryCollection)
	{
		this.entitiesValidator = requireNonNull(entitiesValidator);
		this.entitiesImporter = requireNonNull(entitiesImporter);
		this.targetCollection = requireNonNull(repositoryCollection);
	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		try
		{
			return entitiesValidator.validate(file);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction, String defaultPackage)
	{
		try
		{
			return entitiesImporter.importEntities(source, databaseAction);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		String fileNameExtension = FileExtensionUtils.findExtensionFromPossibilities(file.getName(),
				GenericImporterExtensions.getJPA());

		if (!source.getEntityNames().iterator().hasNext()) return false;

		if (null != fileNameExtension)
		{
			for (String entityName : source.getEntityNames())
			{
				if (!targetCollection.hasRepository(entityName)) return false;
			}
		}

		return true;
	}

	@Override
	public List<DatabaseAction> getSupportedDatabaseActions()
	{
		return Lists.newArrayList(DatabaseAction.values());
	}

	@Override
	public boolean getMustChangeEntityName()
	{
		return false;
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public Set<String> getSupportedFileExtensions()
	{
		return GenericImporterExtensions.getJPA();
	}

	@Override
	public LinkedHashMap<String, Boolean> integrationTestMetaData(MetaDataService metaDataService,
			RepositoryCollection repositoryCollection, String defaultPackage)
	{
		return metaDataService.integrationTestMetaData(repositoryCollection);
	}
}
