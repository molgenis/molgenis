package org.molgenis.omx.catalogmanager;

import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.catalogmanager.CatalogManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
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
		Protocol protocol = getProtocol(id);
		if (protocol == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		return new OmxCatalog(protocol);
	}

	@Override
	public Catalog getCatalogOfStudyDefinition(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		Protocol protocol = studyDataRequest.getProtocol();
		if (protocol == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		return new OmxCatalog(protocol);
	}

	@Override
	public void loadCatalog(String id) throws UnknownCatalogException
	{
		Protocol protocol = getProtocol(id);
		if (protocol == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		setProtocolActive(protocol, true);
	}

	@Override
	public void unloadCatalog(String id) throws UnknownCatalogException
	{
		Protocol protocol = getProtocol(id);
		if (protocol == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		setProtocolActive(protocol, false);
	}

	@Override
	public boolean isCatalogLoaded(String id) throws UnknownCatalogException
	{
		Protocol protocol = getProtocol(id);
		if (protocol == null) throw new UnknownCatalogException("Catalog [" + id + "] does not exist");
		return protocol.getActive();
	}

	@Override
	public void loadCatalogOfStudyDefinition(String id) throws UnknownCatalogException, UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		Protocol protocol = getProtocol(id);
		if (protocol == null) throw new UnknownCatalogException("No catalog defined for study definition [" + id + "]");
		setProtocolActive(protocol, true);
	}

	@Override
	public void unloadCatalogOfStudyDefinition(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		Protocol protocol = getProtocol(id);
		if (protocol == null) throw new UnknownCatalogException("No catalog defined for study definition [" + id + "]");
		setProtocolActive(protocol, false);
	}

	@Override
	public boolean isCatalogOfStudyDefinitionLoaded(String id) throws UnknownCatalogException,
			UnknownStudyDefinitionException
	{
		StudyDataRequest studyDataRequest = getStudyDataRequest(id);
		if (studyDataRequest == null) throw new UnknownStudyDefinitionException("Study definition [" + id
				+ "] does not exist");
		Protocol protocol = getProtocol(id);
		if (protocol == null) throw new UnknownCatalogException("No catalog defined for study definition [" + id + "]");
		return protocol.getActive();
	}

	private Protocol getProtocol(String protocolIdentifier)
	{
		return dataService.findOne(Protocol.ENTITY_NAME, new QueryImpl().eq(Protocol.IDENTIFIER, protocolIdentifier));
	}

	private void setProtocolActive(Protocol protocol, boolean active)
	{
		protocol.setActive(active);
		dataService.update(Protocol.ENTITY_NAME, protocol);
	}

	private StudyDataRequest getStudyDataRequest(String studyDataRequestIdentifier)
	{
		return dataService.findOne(StudyDataRequest.ENTITY_NAME,
				new QueryImpl().eq(StudyDataRequest.IDENTIFIER, studyDataRequestIdentifier));
	}
}
