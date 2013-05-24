package org.molgenis.lifelines.catalogue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import nl.umcg.hl7.CatalogService;
import nl.umcg.hl7.GenericLayerCatalogService;
import nl.umcg.hl7.GetCatalogResponse.GetCatalogResult;
import nl.umcg.hl7.GetValuesetsResponse.GetValuesetsResult;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.hl7.CD;
import org.molgenis.hl7.REPCMT000100UV01Component3;
import org.molgenis.hl7.REPCMT000100UV01Observation;
import org.molgenis.hl7.REPCMT000100UV01Organizer;
import org.molgenis.hl7.ValueSets;
import org.molgenis.hl7.ValueSets.ValueSet;
import org.molgenis.hl7.ValueSets.ValueSet.Code;
import org.molgenis.lifelines.resourcemanager.ResourceManagerService;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

@Service
public class GenericLayerCatalogueLoaderService implements CatalogLoaderService
{
	private static final Logger logger = Logger.getLogger(GenericLayerCatalogueLoaderService.class);
	private final Database database;
	private final GenericLayerCatalogService genericLayerCatalogService;
	private final ResourceManagerService resourceManagerService;

	@Autowired
	public GenericLayerCatalogueLoaderService(Database database, GenericLayerCatalogService genericLayerCatalogService,
			ResourceManagerService resourceManagerService)
	{
		if (database == null) throw new IllegalArgumentException("database is null");
		if (genericLayerCatalogService == null) throw new IllegalArgumentException("genericLayerCatalogService is null");
		if (resourceManagerService == null) throw new IllegalArgumentException("resourceManagerService is null");
		this.database = database;
		this.genericLayerCatalogService = new CatalogService().getBasicHttpBindingGenericLayerCatalogService();
		this.resourceManagerService = resourceManagerService;
	}

	@Override
	public List<CatalogInfo> findCatalogs()
	{
		return resourceManagerService.findCatalogs();
	}

	@Override
	public void loadCatalog(String id) throws UnknownCatalogException
	{
		try
		{
			database.beginTx();

			OntologyTermIndex ontologyTermIndex = parseValueSets(id);
			parseCatalog(id, ontologyTermIndex);

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

	private OntologyTermIndex parseValueSets(String id) throws DatabaseException
	{
		// retrieve catalog data from LifeLines Generic Layer catalog service
		GetValuesetsResult valueSetsResult = genericLayerCatalogService.getValuesets(id, null);

		// convert to HL7 organizer
		ValueSets valueSets;
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(ValueSets.class);
			Unmarshaller um = jaxbContext.createUnmarshaller();
			valueSets = um.unmarshal((Node) valueSetsResult.getAny(), ValueSets.class).getValue();
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(e); // TODO add exception to loadCatalog signature
		}

		OntologyTermIndex ontologyTermIndex = new OntologyTermIndex();

		for (ValueSet valueSet : valueSets.getValueSet())
		{
			for (Code code : valueSet.getCode())
			{
				OntologyTerm ontologyTerm = new OntologyTerm();
				ontologyTerm.setIdentifier(UUID.randomUUID().toString());
				ontologyTerm.setName(code.getDisplayName());
				ontologyTerm.setTermAccession(code.getCode());

				String codeSystem = code.getCodeSystem();
				if (!ontologyTermIndex.containsKey(codeSystem))
				{
					if (codeSystem == null)
					{
						logger.warn("missing code system for ontology term '" + code.getDisplayName() + "'");
						continue;
					}

					String codeSystemName = code.getCodeSystemName();
					if (codeSystemName == null)
					{
						logger.warn("missing code system name for ontology term '" + code.getDisplayName() + "'");
						continue;
					}

					// create ontology for each code system
					Ontology ontology = new Ontology();
					ontology.setIdentifier(UUID.randomUUID().toString());
					ontology.setName(codeSystemName);
					ontology.setOntologyAccession(codeSystem);

					database.add(ontology);
				}

				database.add(ontologyTerm);
				ontologyTermIndex.put(codeSystem, code.getCode(), ontologyTerm);
			}
		}

		return ontologyTermIndex;
	}

	private void parseCatalog(String id, OntologyTermIndex ontologyTermIndex) throws DatabaseException
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
		dataSet.setIdentifier(CatalogIdConverter.catalogIdToOmxIdentifier(id));
		dataSet.setName("LifeLines" + id);

		Protocol rootProtocol = new Protocol();
		rootProtocol.setIdentifier(UUID.randomUUID().toString());
		rootProtocol.setName("LifeLines" + id);

		dataSet.setProtocolUsed(rootProtocol);

		// parse protocols between root protocols
		for (REPCMT000100UV01Component3 rootComponent : catalog.getComponent())
		{
			parseComponent(rootComponent, rootProtocol, database, ontologyTermIndex);
		}
		database.add(dataSet);
	}

	private void parseComponent(REPCMT000100UV01Component3 component, Protocol parentProtocol, Database database,
			OntologyTermIndex ontologyTermIndex) throws DatabaseException
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
			CD organizerCode = organizer.getCode();
			logger.debug("parsing organizer " + organizerCode.getCode() + " " + organizerCode.getDisplayName());

			String organizerName = organizerCode.getDisplayName();
			// FIXME ask TCC why some components do not have a displayname
			if (organizerName == null) organizerName = organizerCode.getCode();

			OntologyTerm ontologyTerm = ontologyTermIndex.get(organizerCode.getCodeSystem(), organizerCode.getCode());

			Protocol protocol = new Protocol();
			protocol.setIdentifier(UUID.randomUUID().toString());
			protocol.setName(organizerName);
			protocol.setProtocolType(ontologyTerm);

			// recurse over nested protocols
			for (REPCMT000100UV01Component3 subComponent : organizer.getComponent())
				parseComponent(subComponent, protocol, database, ontologyTermIndex);

			parentProtocol.getSubprotocols().add(protocol);
		}

		database.add(parentProtocol);
	}

	private static class OntologyTermIndex
	{
		private final Map<String, Map<String, OntologyTerm>> codeSystemMap;

		public OntologyTermIndex()
		{
			codeSystemMap = new HashMap<String, Map<String, OntologyTerm>>();
		}

		public void put(String codeSystem, String code, OntologyTerm ontologyTerm)
		{
			Map<String, OntologyTerm> codeMap = codeSystemMap.get(codeSystem);
			if (codeMap == null)
			{
				codeMap = new HashMap<String, OntologyTerm>();
				codeSystemMap.put(codeSystem, codeMap);
			}
			codeMap.put(code, ontologyTerm);
		}

		public OntologyTerm get(String codeSystem, String code)
		{
			Map<String, OntologyTerm> codeMap = codeSystemMap.get(codeSystem);
			return codeMap != null ? codeMap.get(code) : null;
		}

		public boolean containsKey(String codeSystem)
		{
			return codeSystemMap.containsKey(codeSystem);
		}
	}
}
