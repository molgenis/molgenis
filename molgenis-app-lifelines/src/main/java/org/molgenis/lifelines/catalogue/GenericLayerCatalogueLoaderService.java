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
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.hl7.REPCMT000100UV01Component3;
import org.molgenis.hl7.REPCMT000100UV01Observation;
import org.molgenis.hl7.REPCMT000100UV01Organizer;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
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
	public void loadCatalog(String id) throws UnknownCatalogException
	{
		// retrieve catalog data from LifeLines Generic Layer catalog service
		GetCatalogResult catalogResult = genericLayerCatalogService.getCatalog(id, null);

		// convert to HL7 organizer
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

		// convert to MOLGENIS OMX model and add to database
		DataSet dataSet = new DataSet();
		dataSet.setIdentifier(DataSet.class.getSimpleName() + "_LifeLines" + id);
		dataSet.setName("LifeLines" + id);

		Protocol rootProtocol = new Protocol();
		rootProtocol.setIdentifier(UUID.randomUUID().toString());
		rootProtocol.setName("LifeLines" + id);

		dataSet.setProtocolUsed(rootProtocol);

		try
		{
			// parse protocols between root protocols
			for (REPCMT000100UV01Component3 rootComponent : catalog.getComponent())
			{
				parseComponent(rootComponent, rootProtocol, database);
			}
			database.add(dataSet);
			database.commitTx();
		}
		catch (DatabaseException e)
		{
			try
			{
				database.rollbackTx();
			}
			catch (DatabaseException e1)
			{
				throw new RuntimeException(e1);
			}
			throw new RuntimeException(e);
		}
	}

	public void parseComponent(REPCMT000100UV01Component3 component, Protocol parentProtocol, Database database)
			throws DatabaseException
	{

		// parse feature
		if (component.getObservation() != null)
		{
			REPCMT000100UV01Observation observation = component.getObservation().getValue();
			logger.debug("parsing observation " + observation.getCode().getDisplayName());

			String observationName = observation.getCode().getDisplayName();
			if (observationName == null) observationName = observation.getCode().getCode();

			ObservableFeature observableFeature = new ObservableFeature();
			observableFeature.setIdentifier(UUID.randomUUID().toString());
			observableFeature.setName(observationName);

			parentProtocol.getFeatures().add(observableFeature);

			database.add(observableFeature);
		}

		// parse sub-protocol
		if (component.getOrganizer() != null)
		{
			REPCMT000100UV01Organizer organizer = component.getOrganizer().getValue();
			logger.debug("parsing organizer " + organizer.getCode().getCode() + " "
					+ organizer.getCode().getDisplayName());

			String organizerName = organizer.getCode().getDisplayName();
			if (organizerName == null) organizerName = organizer.getCode().getCode();

			Protocol protocol = new Protocol();
			protocol.setIdentifier(UUID.randomUUID().toString());
			protocol.setName(organizerName);

			// recurse over nested protocols
			for (REPCMT000100UV01Component3 subComponent : organizer.getComponent())
				parseComponent(subComponent, protocol, database);

			parentProtocol.getSubprotocols().add(protocol);
		}

		database.add(parentProtocol);
	}
}
