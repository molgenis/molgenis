package org.molgenis.omx.catalogmanager;

import java.util.Collections;
import java.util.List;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.study.UnknownStudyDefinitionException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class OmxCatalogManagerService implements CatalogManagerService
{
	private final Database database;

	public OmxCatalogManagerService(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
	}

	@Override
	public List<CatalogMeta> findCatalogs()
	{
		List<DataSet> dataSets;
		try
		{
			dataSets = database.find(DataSet.class);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		if (dataSets == null) return Collections.emptyList();

		return Lists.transform(dataSets, new Function<DataSet, CatalogMeta>()
		{
			@Override
			public CatalogMeta apply(DataSet dataSet)
			{
				CatalogMeta catalogMeta = new CatalogMeta(dataSet.getIdentifier(), dataSet.getName());
				catalogMeta.setDescription(dataSet.getDescription());
				return catalogMeta;
			}
		});
	}

	@Override
	public Catalog getCatalog(String id) throws UnknownCatalogException
	{
		DataSet dataSet;
		try
		{
			dataSet = DataSet.findByIdentifier(database, id);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		if (dataSet == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		return new OmxCatalog(dataSet);
	}

	@Override
	public Catalog getCatalogOfStudyDefinition(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest;
		try
		{
			studyDataRequest = StudyDataRequest.findByIdentifier(database, id);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");

		// TODO get data set for this study data request

		// get MOLGENIS catalog
		List<DataSet> dataSets;
		try
		{
			dataSets = database.find(DataSet.class);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

		if (dataSets == null || dataSets.isEmpty()) throw new RuntimeException("Database contains no catalogs");
		if (dataSets.size() > 1) throw new RuntimeException(
				"Can't determine catalog for study definition, because database contains multiple catalogs");

		return new OmxCatalog(dataSets.get(0));
	}

	@Override
	public void loadCatalog(String id) throws UnknownCatalogException
	{
		// TODO decide how to implement
	}

	@Override
	public void unloadCatalog(String id) throws UnknownCatalogException
	{
		// TODO decide how to implement
	}

	@Override
	public boolean isCatalogLoaded(String id) throws UnknownCatalogException
	{
		try
		{
			return DataSet.findByIdentifier(database, id) != null;
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void loadCatalogOfStudyDefinition(String id) throws UnknownCatalogException
	{
		// TODO decide how to implement
	}

	@Override
	public void unloadCatalogOfStudyDefinition(String id) throws UnknownCatalogException
	{
		// TODO decide how to implement
	}

	@Override
	public boolean isCatalogOfStudyDefinitionLoaded(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		try
		{
			// get study definition
			StudyDataRequest studyDataRequest = StudyDataRequest.findByIdentifier(database, id);
			if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
					+ "] does not exist");

			// get catalog of study definition
			List<DataSet> dataSets = database.find(DataSet.class);
			return dataSets != null && dataSets.size() == 1;
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}
}
