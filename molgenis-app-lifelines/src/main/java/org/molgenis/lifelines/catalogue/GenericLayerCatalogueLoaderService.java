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
import org.molgenis.hl7.ANY;
import org.molgenis.hl7.CD;
import org.molgenis.hl7.II;
import org.molgenis.hl7.PQ;
import org.molgenis.hl7.REPCMT000100UV01Component3;
import org.molgenis.hl7.REPCMT000100UV01Observation;
import org.molgenis.hl7.REPCMT000100UV01Organizer;
import org.molgenis.hl7.ValueSets;
import org.molgenis.hl7.ValueSets.ValueSet;
import org.molgenis.hl7.ValueSets.ValueSet.Code;
import org.molgenis.lifelines.hl7.HL7DataTypeMapper;
import org.molgenis.lifelines.resourcemanager.ResourceManagerService;
import org.molgenis.omx.observ.Category;
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

			// retrieve catalog data from LifeLines Generic Layer catalog service
			GetCatalogResult catalogResult = genericLayerCatalogService.getCatalog(id, null);

			// convert to MOLGENIS OMX model and add to database
			DataSet dataSet = new DataSet();
			dataSet.setIdentifier(CatalogIdConverter.catalogIdToOmxIdentifier(id));
			dataSet.setName("Catalogue #" + id);

			Protocol rootProtocol = new Protocol();
			rootProtocol.setIdentifier(UUID.randomUUID().toString());
			rootProtocol.setName("LifeLines");

			dataSet.setProtocolUsed(rootProtocol);

			Map<String, String> valueSetMap = parseCatalog(dataSet, rootProtocol, catalogResult);

			database.add(rootProtocol);
			database.add(dataSet);

			// retrieve catalog data from LifeLines Generic Layer catalog service
			GetValuesetsResult valueSetsResult = genericLayerCatalogService.getValuesets(id, null);
			loadValueSets(valueSetsResult, valueSetMap);

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

	@Override
	public void loadCatalogOfStudyDefinition(String id) throws UnknownCatalogException
	{
		try
		{
			database.beginTx();

			// retrieve catalog data from LifeLines Generic Layer catalog service
			GetCatalogResult catalogResult = genericLayerCatalogService.getCatalog(null, id);

			// convert to MOLGENIS OMX model and add to database
			DataSet dataSet = new DataSet();
			dataSet.setIdentifier(CatalogIdConverter.catalogOfStudyDefinitionIdToOmxIdentifier(id));
			dataSet.setName("Study definition #" + id);

			Protocol rootProtocol = new Protocol();
			rootProtocol.setIdentifier(UUID.randomUUID().toString());
			rootProtocol.setName("LifeLines");

			dataSet.setProtocolUsed(rootProtocol);

			// FIXME add patient protocol/features

			Map<String, String> valueSetMap = parseCatalog(dataSet, rootProtocol, catalogResult);

			database.add(rootProtocol);
			database.add(dataSet);

			// retrieve catalog data from LifeLines Generic Layer catalog service
			GetValuesetsResult valueSetsResult = genericLayerCatalogService.getValuesets(null, id);
			loadValueSets(valueSetsResult, valueSetMap);

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

	private void loadValueSets(GetValuesetsResult valueSetsResult, Map<String, String> featureMap)
	{
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
			throw new RuntimeException(e);
		}

		OntologyIndex ontologyIndex = new OntologyIndex();

		try
		{
			for (ValueSet valueSet : valueSets.getValueSet())
			{
				String identifier = featureMap.get(valueSet.getName());
				ObservableFeature observableFeature = ObservableFeature.findByIdentifier(database, identifier);
				if (observableFeature == null)
				{
					throw new RuntimeException("missing ObservableFeature with name '" + identifier + "'");
				}

				for (Code code : valueSet.getCode())
				{
					String codeSystem = code.getCodeSystem();
					Ontology ontology = ontologyIndex.get(codeSystem);
					if (ontology == null)
					{
						String codeSystemName = code.getCodeSystemName();
						if (codeSystem == null || codeSystemName == null)
						{
							logger.warn("missing code system or code system name for ontology term '"
									+ code.getDisplayName() + "'");
						}
						else
						{
							// create ontology for each code system
							ontology = new Ontology();
							ontology.setIdentifier(UUID.randomUUID().toString());
							ontology.setName(codeSystemName);
							ontology.setOntologyAccession(codeSystem);

							database.add(ontology);
							ontologyIndex.put(codeSystem, ontology);
						}
					}

					logger.error((ontology != null ? ontology.getId() : null) + "-" + code.getCode());

					OntologyTerm ontologyTerm = ontologyIndex.get(codeSystem, code.getCode());
					if (ontologyTerm == null)
					{
						ontologyTerm = new OntologyTerm();
						ontologyTerm.setIdentifier(UUID.randomUUID().toString());
						ontologyTerm.setName(code.getDisplayName());
						ontologyTerm.setTermAccession(code.getCode());
						if (ontology != null) ontologyTerm.setOntology(ontology);

						database.add(ontologyTerm);
						ontologyIndex.put(codeSystem, code.getCode(), ontologyTerm);
					}

					Category category = new Category();
					category.setIdentifier(UUID.randomUUID().toString());
					category.setName(code.getDisplayName());
					category.setObservableFeature(observableFeature);
					category.setDefinition(ontologyTerm);
					category.setValueCode(code.getCodeSystem() + '.' + code.getCode());
					database.add(category);
				}
			}
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}

	}

	private Map<String, String> parseCatalog(DataSet dataSet, Protocol rootProtocol, GetCatalogResult catalogResult)
			throws DatabaseException
	{

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
			throw new RuntimeException(e);
		}

		Map<String, String> featureMap = new HashMap<String, String>();

		// parse protocols between root protocols
		for (REPCMT000100UV01Component3 rootComponent : catalog.getComponent())
		{
			parseComponent(rootComponent, rootProtocol, database, featureMap);
		}
		return featureMap;
	}

	private void parseComponent(REPCMT000100UV01Component3 component, Protocol parentProtocol, Database database,
			Map<String, String> featureMap) throws DatabaseException
	{

		// parse feature
		if (component.getObservation() != null)
		{
			REPCMT000100UV01Observation observation = component.getObservation().getValue();
			CD observationCode = observation.getCode();
			logger.debug("parsing observation " + observationCode.getDisplayName());

			String observationName = observationCode.getDisplayName();
			if (observationName == null)
			{
				logger.warn("observation does not have a display name '" + observationCode.getCode() + "'");
				observationName = observationCode.getCode();
			}
			OntologyTerm ontologyTerm = getOntologyTerm(observationCode.getCodeSystem(), observationCode.getCode());

			// determine data type
			ANY anyValue = observation.getValue();
			String dataType = HL7DataTypeMapper.get(anyValue);
			if (dataType == null) logger.warn("HL7 data type not supported: " + anyValue.getClass().getSimpleName());

			// TODO how to get description from originalText?
			List<II> observationId = observation.getId();
			String featureId = (observationId != null && !observationId.isEmpty()) ? observationId.get(0).getRoot() : UUID
					.randomUUID().toString();
			ObservableFeature observableFeature = new ObservableFeature();
			observableFeature.setIdentifier(featureId);
			observableFeature.setName(observationName);
			if (dataType != null) observableFeature.setDataType(dataType);
			observableFeature.setDefinition(ontologyTerm);

			// determine unit
			if (anyValue instanceof PQ)
			{
				PQ value = (PQ) anyValue;
				// TODO how to determine ontologyterms for units and do observableFeature.setUnit()
			}
			else if (anyValue instanceof CD) // for CD and CO values
			{
				CD value = (CD) anyValue;
				featureMap.put(value.getCodeSystemName(), featureId);
			}

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
			if (organizerName == null)
			{
				logger.warn("organizer does not have a display name '" + organizerCode.getCode() + "'");
				organizerName = organizerCode.getCode();
			}

			OntologyTerm ontologyTerm = getOntologyTerm(organizerCode.getCodeSystem(), organizerCode.getCode());

			// FIXME how to get description from originalText?
			Protocol protocol = new Protocol();

			List<II> organizerId = organizer.getId();
			String protocolId = (organizerId != null && !organizerId.isEmpty()) ? organizerId.get(0).getRoot() : UUID
					.randomUUID().toString();
			protocol.setIdentifier(protocolId);
			protocol.setName(organizerName);
			protocol.setProtocolType(ontologyTerm);

			// recurse over nested protocols
			for (REPCMT000100UV01Component3 subComponent : organizer.getComponent())
				parseComponent(subComponent, protocol, database, featureMap);

			parentProtocol.getSubprotocols().add(protocol);
		}

		database.add(parentProtocol);
	}

	private OntologyTerm getOntologyTerm(String codeSystem, String code) throws DatabaseException
	{
		List<OntologyTerm> ontologyTerms = database.query(OntologyTerm.class).eq(OntologyTerm.TERMACCESSION, code)
				.eq(OntologyTerm.ONTOLOGY_IDENTIFIER, codeSystem).find();
		return !ontologyTerms.isEmpty() ? ontologyTerms.get(0) : null;
	}
}
