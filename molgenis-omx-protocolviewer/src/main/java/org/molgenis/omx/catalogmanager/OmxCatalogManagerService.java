package org.molgenis.omx.catalogmanager;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.DataSetRepository;
import org.molgenis.omx.study.StudyDataRequest;
import org.molgenis.omx.study.StudyDataRequestRepository;
import org.molgenis.study.UnknownStudyDefinitionException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class OmxCatalogManagerService implements CatalogManagerService
{
	private final DataSetRepository dataSetRepository;
	private final StudyDataRequestRepository studyDataRequestRepository;

	public OmxCatalogManagerService(DataSetRepository dataSetRepository,
			StudyDataRequestRepository studyDataRequestRepository)
	{
		if (dataSetRepository == null) throw new IllegalArgumentException("dataSetRepository is null");
		if (studyDataRequestRepository == null) throw new IllegalArgumentException("studyDataRequestRepository is null");
		this.dataSetRepository = dataSetRepository;
		this.studyDataRequestRepository = studyDataRequestRepository;
	}

	@Override
	public Iterable<CatalogMeta> findCatalogs()
	{
		Iterable<DataSet> dataSets = dataSetRepository.findAll(new QueryImpl());

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
	public Catalog getCatalogOfStudyDefinition(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		DataSet dataSet = studyDataRequest.getDataSet();
		if (dataSet == null) throw new UnknownCatalogException("No catalog defined for study definition [" + id + "]");
		return new OmxCatalog(dataSet);
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
		Query q = new QueryImpl().eq(DataSet.IDENTIFIER, dataSetIdentifier);
		return dataSetRepository.findOne(q);
	}

	private void setDataSetActive(DataSet dataSet, boolean active)
	{
		dataSet.setActive(active);
		dataSetRepository.update(dataSet);
	}

	private StudyDataRequest getStudyDataRequest(String studyDataRequestIdentifier)
	{
		Query q = new QueryImpl().eq(StudyDataRequest.IDENTIFIER, studyDataRequestIdentifier);
		return studyDataRequestRepository.findOne(q);
	}
}
