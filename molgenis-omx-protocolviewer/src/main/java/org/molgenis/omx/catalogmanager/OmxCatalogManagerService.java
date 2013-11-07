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
		DataSet dataSet = getDataSet(id);
		if (dataSet == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		return new OmxCatalog(dataSet);
	}

	@Override
	public Catalog getCatalogOfStudyDefinition(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		DataSet dataSet = studyDataRequest.getDataSet();
		if (dataSet == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		return new OmxCatalog(dataSet);
	}

	@Override
	public void loadCatalog(String id) throws UnknownCatalogException
	{
		DataSet dataSet = getDataSet(id);
		if (dataSet == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		setDataSetActive(dataSet, true);
	}

	@Override
	public void unloadCatalog(String id) throws UnknownCatalogException
	{
		DataSet dataSet = getDataSet(id);
		if (dataSet == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		setDataSetActive(dataSet, false);
	}

	@Override
	public boolean isCatalogLoaded(String id) throws UnknownCatalogException
	{
		DataSet dataSet = getDataSet(id);
		if (dataSet == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		return dataSet.getActive();
	}

	@Override
	public void loadCatalogOfStudyDefinition(String id) throws UnknownCatalogException, UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		DataSet dataSet = studyDataRequest.getDataSet();
		if (dataSet == null) throw new UnknownCatalogException("No catalog defined for study definition [" + id + "]");
		setDataSetActive(dataSet, true);
	}

	@Override
	public void unloadCatalogOfStudyDefinition(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		DataSet dataSet = studyDataRequest.getDataSet();
		if (dataSet == null) throw new UnknownCatalogException("No catalog defined for study definition [" + id + "]");
		setDataSetActive(dataSet, false);
	}

	@Override
	public boolean isCatalogOfStudyDefinitionLoaded(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		DataSet dataSet = studyDataRequest.getDataSet();
		if (dataSet == null) throw new UnknownCatalogException("No catalog defined for study definition [" + id + "]");
		return dataSet.getActive();
	}

	private DataSet getDataSet(String dataSetIdentifier)
	{
		try
		{
			return DataSet.findByIdentifier(database, dataSetIdentifier);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void setDataSetActive(DataSet dataSet, boolean active)
	{
		dataSet.setActive(active);
		try
		{
			database.update(dataSet);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	private StudyDataRequest getStudyDataRequest(String studyDataRequestIdentifier)
	{
		try
		{
			return StudyDataRequest.findByIdentifier(database, studyDataRequestIdentifier);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}
}
