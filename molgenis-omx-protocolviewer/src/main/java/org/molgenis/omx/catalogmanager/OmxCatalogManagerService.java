package org.molgenis.omx.catalogmanager;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.study.UnknownStudyDefinitionException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class OmxCatalogManagerService implements CatalogManagerService
{
	private final DataService dataService;

	public OmxCatalogManagerService(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.dataService = dataService;
	}

	@Override
	public Iterable<CatalogMeta> findCatalogs()
	{
		Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, new QueryImpl());

		return Iterables.transform(dataSets, new Function<DataSet, CatalogMeta>()
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
		return dataService.findOne(DataSet.ENTITY_NAME, new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier));
	}

	private void setDataSetActive(DataSet dataSet, boolean active)
	{
		dataSet.setActive(active);
		dataService.update(DataSet.ENTITY_NAME, dataSet);
	}

	private StudyDataRequest getStudyDataRequest(String studyDataRequestIdentifier)
	{
		return dataService.findOne(StudyDataRequest.ENTITY_NAME,
				new QueryImpl().eq(StudyDataRequest.IDENTIFIER, studyDataRequestIdentifier));
	}
}
