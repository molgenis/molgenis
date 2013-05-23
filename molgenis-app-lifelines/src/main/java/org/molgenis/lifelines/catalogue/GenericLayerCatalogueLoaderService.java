package org.molgenis.lifelines.catalogue;

import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.umcg.hl7.CatalogService;
import nl.umcg.hl7.GenericLayerCatalogService;
import nl.umcg.hl7.GetCatalogResponse.GetCatalogResult;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.hl7.REPCMT000100UV01Component3;
import org.molgenis.hl7.REPCMT000100UV01Organizer;
import org.molgenis.omx.observ.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

public class GenericLayerCatalogueLoaderService implements CatalogLoaderService
{
	private static final Logger logger = Logger.getLogger(GenericLayerCatalogueLoaderService.class);

	private final Database database;

	private final GenericLayerCatalogService genericLayerCatalogService;

	@Autowired
	public GenericLayerCatalogueLoaderService(Database database, GenericLayerCatalogService genericLayerCatalogService)
	{
		if (database == null) throw new IllegalArgumentException("database is null");
		if (genericLayerCatalogService == null) throw new IllegalArgumentException("genericLayerCatalogService is null");
		this.database = database;
		this.genericLayerCatalogService = new CatalogService().getBasicHttpBindingGenericLayerCatalogService();
	}

	@Override
	public List<CatalogInfo> findCatalogs()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCatalog(String id)
	{
		GetCatalogResult catalogResult = genericLayerCatalogService.getCatalog(id, null);

		REPCMT000100UV01Organizer catalog;
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(REPCMT000100UV01Organizer.class);
			Unmarshaller um = jaxbContext.createUnmarshaller();
			catalog = um.unmarshal((Node) catalogResult.getAny(), REPCMT000100UV01Organizer.class).getValue();
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e); // TODO add exception to loadCatalog signature
		}

		Protocol rootProtocol = new Protocol();
		rootProtocol.setIdentifier(UUID.randomUUID().toString());
		rootProtocol.setName("LifeLines");

		for (REPCMT000100UV01Component3 rootComponent : catalog.getComponent()) // e.g. ["Generic", "LRA", "LRA2"]
		{
			parseComponent(rootComponent, rootProtocol);
		}
	}

	public void parseComponent(REPCMT000100UV01Component3 component, Protocol parentProtocol)
	{
		REPCMT000100UV01Organizer organizer = component.getOrganizer().getValue();
		logger.debug("parsing component with organizer:code=" + organizer.getCode().getCode());

		// FIXME implement
	}
}
