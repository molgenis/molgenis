package org.molgenis.data.omx2emx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.Writable;
import org.molgenis.data.WritableFactory;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.EntityUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Omx2EmxConverter
{
	private static enum OMX_TABS
	{
		OBSERVABLEFEATURE, PROTOCOL, DATASET, CATEGORY, ONTOLOGY, ONTOLOGYTERM, ACCESSION, OBSERVATIONTARGET, INDIVIDUAL, PANEL, SPECIES
	}

	private static enum DATASET_COLUMNS
	{
		IDENTIFIER, NAME, PROTOCOLUSED_IDENTIFIER, DESCRIPTION
	}

	public static enum OBSERVABLE_FEATURE_COLUMNS
	{
		IDENTIFIER, NAME, DESCRIPTION, DATATYPE, UNIT_IDENTIFIER, DEFINITIONS_IDENTIFIER
	}

	public static enum PROTOCOL_COLUMNS
	{
		IDENTIFIER, NAME, DESCRIPTION, FEATURES_IDENTIFIER, SUBPROTOCOLS_IDENTIFIER, ROOT, ACTIVE
	}

	public static enum CATEGORY_COLUMNS
	{
		IDENTIFIER, NAME, VALUECODE, OBSERVABLEFEATURE_IDENTIFIER
	}

	private final RepositoryCollection omxRepositoryCollection;
	private final String namespace;
	private Map<String, Entity> protocols;

	public Omx2EmxConverter(RepositoryCollection omxRepositoryCollection, String namespace)
	{
		this.omxRepositoryCollection = omxRepositoryCollection;
		this.namespace = namespace;
	}

	public void convert(WritableFactory writableFactory)
	{
		System.out.println("Write entities...");
		writeEntities(writableFactory);

		System.out.println("Write attributes...");
		writeAttributes(writableFactory);

		System.out.println("Write datasets...");
		writeDatasets(writableFactory);

		System.out.println("Conversion done");
	}

	private void writeDatasets(WritableFactory writableFactory)
	{
		Map<String, Map<String, String>> categories = Maps.newHashMap();

		// Categories
		if (omxContainsEntity(OMX_TABS.CATEGORY.toString()))
		{
			Set<String> observableFeatureIdentifiers = Sets.newHashSet();
			Repository categoryRepo = omxRepositoryCollection.getRepository(OMX_TABS.CATEGORY.toString());
			for (Entity category : categoryRepo)
			{
				String observableFeatureIdentifier = category
						.getString(CATEGORY_COLUMNS.OBSERVABLEFEATURE_IDENTIFIER.toString());
				if (!observableFeatureIdentifiers.contains(observableFeatureIdentifier))
				{
					observableFeatureIdentifiers.add(observableFeatureIdentifier);
					Writable lut = writableFactory.createWritable(getFullEntityName(observableFeatureIdentifier),
							Arrays.asList("Name"));

					for (Entity cat : categoryRepo)
					{
						String catObservableFeatureIdentifier = cat
								.getString(CATEGORY_COLUMNS.OBSERVABLEFEATURE_IDENTIFIER.toString());

						if (catObservableFeatureIdentifier.equalsIgnoreCase(observableFeatureIdentifier))
						{
							String valueCode = cat.getString(CATEGORY_COLUMNS.VALUECODE.toString());
							String name = cat.getString(CATEGORY_COLUMNS.NAME.toString());

							Entity entity = new MapEntity();
							entity.set("Name", name);
							lut.add(entity);

							Map<String, String> categoryMap = categories.get(catObservableFeatureIdentifier);
							if (categoryMap == null)
							{
								categoryMap = Maps.<String, String> newHashMap();
								categories.put(catObservableFeatureIdentifier, categoryMap);
							}
							categoryMap.put(valueCode, name);
						}
					}
				}
			}
		}

		// Individuals
		if (omxContainsEntity(OMX_TABS.INDIVIDUAL.toString()))
		{

			Writable writable = writableFactory.createWritable(getFullEntityName("Individual"),
					Arrays.asList("Identifier", "Name", "Description"));
			Repository individualRepo = omxRepositoryCollection.getRepository(OMX_TABS.INDIVIDUAL.toString());
			writable.add(individualRepo.stream());
		}

		// Panels
		if (omxContainsEntity(OMX_TABS.PANEL.toString()))
		{
			Writable writable = writableFactory.createWritable(getFullEntityName("Panel"),
					Arrays.asList("Identifier", "Name", "NumberOfIndividuals"));
			Repository panelRepo = omxRepositoryCollection.getRepository(OMX_TABS.PANEL.toString());
			writable.add(panelRepo.stream());
		}

		for (String entityName : omxRepositoryCollection.getEntityNames())
		{
			if (entityName.toLowerCase().startsWith("dataset_"))
			{
				Repository repo = omxRepositoryCollection.getRepository(entityName);
				List<String> attributeNames = Lists.newArrayList("Identifier");
				for (AttributeMetaData attr : repo.getEntityMetaData().getAtomicAttributes())
				{
					attributeNames.add(attr.getName());
				}

				String dataset = entityName.substring("dataset_".length());
				Writable writable = writableFactory.createWritable(getFullEntityName(dataset), attributeNames);
				for (Entity excelEntity : repo)
				{
					Entity entity = new MapEntity();
					entity.set(excelEntity);
					entity.set("Identifier", UUID.randomUUID().toString());

					for (String attributeName : attributeNames)
					{
						if (categories.containsKey(attributeName))
						{
							String valueCode = entity.getString(attributeName);
							entity.set(attributeName, categories.get(attributeName).get(valueCode));
						}
					}

					writable.add(entity);
				}
			}
		}

	}

	private void writeAttributes(WritableFactory writableFactory)
	{
		Writable attributes = writableFactory.createWritable("attributes", Arrays.asList("name", "entity", "label",
				"dataType", "description", "refEntity", "nillable", "idAttribute", "visible"));
		try
		{
			// Rename Indivual and Panel to Individuals and Panels because it would collide with omx Panel and
			// Individual tables

			// Individual
			if (omxContainsEntity(OMX_TABS.INDIVIDUAL.toString()))
			{
				Entity identifier = new MapEntity();
				identifier.set("name", "Identifier");
				identifier.set("entity", getFullEntityName("Individual"));
				identifier.set("dataType", "string");
				identifier.set("nillable", false);
				identifier.set("idAttribute", true);
				attributes.add(identifier);

				Entity name = new MapEntity();
				name.set("name", "Name");
				name.set("entity", getFullEntityName("Individual"));
				name.set("dataType", "string");
				name.set("nillable", true);
				name.set("idAttribute", false);
				attributes.add(name);

				Entity description = new MapEntity();
				description.set("name", "Description");
				description.set("entity", getFullEntityName("Individual"));
				description.set("dataType", "string");
				description.set("nillable", false);
				description.set("idAttribute", false);
				attributes.add(description);
			}

			// Panel
			if (omxContainsEntity(OMX_TABS.PANEL.toString()))
			{
				Entity identifier = new MapEntity();
				identifier.set("name", "Identifier");
				identifier.set("entity", getFullEntityName("Panel"));
				identifier.set("dataType", "string");
				identifier.set("nillable", false);
				identifier.set("idAttribute", true);
				attributes.add(identifier);

				Entity name = new MapEntity();
				name.set("name", "Name");
				name.set("entity", getFullEntityName("Panel"));
				name.set("dataType", "string");
				name.set("nillable", true);
				name.set("idAttribute", false);
				attributes.add(name);

				Entity nrOfIndividuals = new MapEntity();
				nrOfIndividuals.set("name", "NumberOfIndividuals");
				nrOfIndividuals.set("entity", getFullEntityName("Panel"));
				nrOfIndividuals.set("dataType", "int");
				nrOfIndividuals.set("nillable", true);
				nrOfIndividuals.set("idAttribute", false);
				attributes.add(nrOfIndividuals);
			}

			// Categorical
			if (omxContainsEntity(OMX_TABS.CATEGORY.toString()))
			{
				Set<String> observableFeatureIdentifiers = Sets.newHashSet();
				Repository categoryRepo = omxRepositoryCollection.getRepository(OMX_TABS.CATEGORY.toString());

				for (Entity category : categoryRepo)
				{
					observableFeatureIdentifiers
							.add(category.getString(CATEGORY_COLUMNS.OBSERVABLEFEATURE_IDENTIFIER.toString()));
				}

				for (String observableFeatureIdentifier : observableFeatureIdentifiers)
				{
					Entity categoryName = new MapEntity();
					categoryName.set("name", "Name");
					categoryName.set("entity", getFullEntityName(observableFeatureIdentifier));
					categoryName.set("dataType", "string");// ?
					categoryName.set("nillable", false);
					categoryName.set("idAttribute", true);
					attributes.add(categoryName);
				}
			}

			// DataSet identifiers
			for (String entity : omxRepositoryCollection.getEntityNames())
			{
				if (entity.toLowerCase().startsWith("dataset_"))
				{
					String dataset = entity.substring("dataset_".length());
					Entity idAttribute = new MapEntity();
					idAttribute.set("name", "Identifier");
					idAttribute.set("entity", getFullEntityName(dataset));
					idAttribute.set("label", "Identifier");
					idAttribute.set("dataType", "string");
					idAttribute.set("nillable", false);
					idAttribute.set("idAttribute", true);
					idAttribute.set("visible", false);
					attributes.add(idAttribute);
				}
			}

			for (Entity feature : getObservableFeatures())
			{
				String identifier = feature.getString(OBSERVABLE_FEATURE_COLUMNS.IDENTIFIER.toString());
				List<Entity> protocols = getObservableFeatureProtocol(identifier);
				if (protocols.isEmpty())
				{
					System.out.println("WARN: dangling ObservableFeature with identifier [" + identifier + "]");
				}
				else
				{
					for (Entity protocol : protocols)
					{
						String dataType = feature.getString(OBSERVABLE_FEATURE_COLUMNS.DATATYPE.toString());
						String entity = protocol.getString(PROTOCOL_COLUMNS.IDENTIFIER.toString());

						Entity attribute = new MapEntity();
						attribute.set("name", identifier);
						attribute.set("entity", getFullEntityName(entity));
						attribute.set("label", feature.getString(OBSERVABLE_FEATURE_COLUMNS.NAME.toString()));
						attribute.set("dataType", dataType);
						attribute.set("description",
								feature.getString(OBSERVABLE_FEATURE_COLUMNS.DESCRIPTION.toString()));
						attribute.set("nillable", true);
						attribute.set("idAttribute", false);

						// Categorical
						if ((dataType != null) && dataType.equalsIgnoreCase("categorical"))
						{
							attribute.set("refEntity", getFullEntityName(identifier));
						}
						// xref/mref
						else if ((dataType != null)
								&& (dataType.equalsIgnoreCase("xref") || dataType.equalsIgnoreCase("mref")))
						{
							// We assume that all ObservedValues of an ObservableFeature in an xref column point to the
							// same
							// entity, find it

							for (String entityName : omxRepositoryCollection.getEntityNames())
							{
								// See where the first not null row points to
								Repository repo = omxRepositoryCollection.getRepository(entityName);
								if (entityName.toLowerCase().startsWith("dataset_"))

								{
									List<String> refs = null;
									Iterator<Entity> it = repo.iterator();
									while ((refs == null) && it.hasNext())
									{
										Entity row = it.next();
										refs = row.getList(identifier);
									}

									if (refs != null)
									{
										for (String ref : refs)
										{
											String refEntity = getRefEntity(ref);
											if (refEntity != null)
											{
												attribute.set("refEntity", refEntity);
												break;
											}
										}
									}
								}

							}
						}

						attributes.add(attribute);
					}
				}
			}

			// Compound attributes
			for (Entity protocol : getProtocols().values())
			{
				List<String> subprotocolIdentifiers = protocol
						.getList(PROTOCOL_COLUMNS.SUBPROTOCOLS_IDENTIFIER.toString());
				if (subprotocolIdentifiers != null)
				{
					for (String subprotocolIdentifier : subprotocolIdentifiers)
					{
						String entity = protocol.getString(PROTOCOL_COLUMNS.IDENTIFIER.toString());
						String refEntity = subprotocolIdentifier;

						Entity subprotocol = getProtocol(subprotocolIdentifier);

						Entity attribute = new MapEntity();
						attribute.set("name", subprotocolIdentifier);
						attribute.set("entity", getFullEntityName(entity));
						attribute.set("dataType", "compound");
						attribute.set("label", subprotocol.get(PROTOCOL_COLUMNS.NAME.toString()));
						attribute.set("refEntity", getFullEntityName(refEntity));
						attributes.add(attribute);
					}
				}
			}

		}
		finally
		{
			IOUtils.closeQuietly(attributes);
		}
	}

	private String getRefEntity(String identifier)
	{
		if (containsIdentifier(OMX_TABS.INDIVIDUAL.toString(), identifier))
		{
			return getFullEntityName("Individual");
		}

		if (containsIdentifier(OMX_TABS.PANEL.toString(), identifier))
		{
			return getFullEntityName("Panel");
		}

		return null;
	}

	private List<Entity> getObservableFeatureProtocol(String observableFeatureIdentifier)
	{
		List<Entity> protocols = new ArrayList<Entity>();
		for (Entity protocol : getProtocols().values())
		{
			List<String> protocolFeatures = protocol.getList(PROTOCOL_COLUMNS.FEATURES_IDENTIFIER.toString());
			if (protocolFeatures != null)
			{
				for (String protocolFeature : protocolFeatures)
				{
					if (observableFeatureIdentifier.equalsIgnoreCase(protocolFeature))
					{
						protocols.add(protocol);
					}
				}
			}
		}

		return protocols;
	}

	private String getFullEntityName(String name)
	{
		name = name.replace('-', '_'); // sanitize name
		return namespace == null ? name : namespace + "_" + name;
	}

	private void writeEntities(WritableFactory writableFactory)
	{
		Writable entities = writableFactory.createWritable("entities",
				Arrays.asList("name", "description", "abstract", "label", "extends"));
		try
		{
			// Protocols
			for (Entity protocol : getProtocols().values())
			{
				Entity protocolMeta = new MapEntity();
				protocolMeta.set("name", getFullEntityName(protocol.getString(PROTOCOL_COLUMNS.IDENTIFIER.toString())));
				protocolMeta.set("label", protocol.getString(PROTOCOL_COLUMNS.NAME.toString()));
				protocolMeta.set("description", protocol.getString(PROTOCOL_COLUMNS.DESCRIPTION.toString()));
				protocolMeta.set("abstract", true);
				entities.add(protocolMeta);
			}

			// Datasets
			for (Entity dataset : getDatasets())
			{
				String protocolUsedIdentifier = dataset.getString(DATASET_COLUMNS.PROTOCOLUSED_IDENTIFIER.toString());
				if (protocolUsedIdentifier == null)
				{
					throw new MolgenisDataException("Missing protocolUsed");
				}

				Entity protocolUsed = getProtocol(protocolUsedIdentifier);
				if (protocolUsed == null)
				{
					throw new MolgenisDataException("Missing protocol [" + protocolUsedIdentifier + "]");
				}

				Entity datasetMeta = new MapEntity();
				datasetMeta.set("name", getFullEntityName(dataset.getString(DATASET_COLUMNS.IDENTIFIER.toString())));
				datasetMeta.set("label", dataset.getString(DATASET_COLUMNS.NAME.toString()));
				datasetMeta.set("description", dataset.getString(DATASET_COLUMNS.DESCRIPTION.toString()));
				datasetMeta.set("extends",
						getFullEntityName(protocolUsed.getString(PROTOCOL_COLUMNS.IDENTIFIER.toString())));
				entities.add(datasetMeta);
			}

			// Categories
			if (omxContainsEntity(OMX_TABS.CATEGORY.toString()))
			{
				Set<String> observableFeatureIdentifiers = Sets.newHashSet();
				Repository categoryRepo = omxRepositoryCollection.getRepository(OMX_TABS.CATEGORY.toString());

				for (Entity category : categoryRepo)
				{
					observableFeatureIdentifiers
							.add(category.getString(CATEGORY_COLUMNS.OBSERVABLEFEATURE_IDENTIFIER.toString()));
				}

				for (String observableFeatureIdentifier : observableFeatureIdentifiers)
				{
					Entity catMeta = new MapEntity();
					catMeta.set("name", getFullEntityName(observableFeatureIdentifier));
					catMeta.set("label", observableFeatureIdentifier);
					entities.add(catMeta);
				}
			}

			// Rename Indivual and Panel to Individuals and Panels because it would collide with omx Panel and
			// Individual
			// tables

			// Individuals
			if (omxContainsEntity(OMX_TABS.INDIVIDUAL.toString()))
			{
				Entity individualMeta = new MapEntity();
				individualMeta.set("name", getFullEntityName("Individual"));
				individualMeta.set("label", "Individual");
				entities.add(individualMeta);
			}

			// Panels
			if (omxContainsEntity(OMX_TABS.PANEL.toString()))
			{
				Entity panelMeta = new MapEntity();
				panelMeta.set("name", getFullEntityName("Panel"));
				panelMeta.set("label", "Panel");
				entities.add(panelMeta);
			}

		}
		finally
		{
			IOUtils.closeQuietly(entities);
		}
	}

	private Iterable<Entity> getDatasets()
	{
		return omxRepositoryCollection.getRepository(OMX_TABS.DATASET.toString());
	}

	private Iterable<Entity> getObservableFeatures()
	{
		return omxRepositoryCollection.getRepository(OMX_TABS.OBSERVABLEFEATURE.toString());
	}

	private Map<String, Entity> getProtocols()
	{
		if (protocols == null)
		{
			protocols = Maps.newLinkedHashMap();
			for (Entity protocol : omxRepositoryCollection.getRepository(OMX_TABS.PROTOCOL.toString()))
			{
				if (!EntityUtils.isEmpty(protocol))
				{
					protocols.put(protocol.getString(PROTOCOL_COLUMNS.IDENTIFIER.toString()), protocol);
				}
			}
		}

		return protocols;
	}

	private Entity getProtocol(String identifier)
	{
		Entity protocol = getProtocols().get(identifier);
		if (protocol == null)
		{
			throw new IllegalArgumentException("Unknown protocol [" + identifier + "]");
		}

		return protocol;
	}

	private boolean omxContainsEntity(String name)
	{
		for (String entity : omxRepositoryCollection.getEntityNames())
		{
			if (entity.equalsIgnoreCase(name))
			{
				return true;
			}
		}

		return false;
	}

	private boolean containsIdentifier(String entityName, String identifier)
	{
		if (!omxContainsEntity(entityName))
		{
			return false;
		}

		for (Entity entity : omxRepositoryCollection.getRepository(entityName))
		{
			String entityIdentifier = entity.getString("identifier");
			if ((entityIdentifier != null) && entityIdentifier.equalsIgnoreCase(identifier))
			{
				return true;
			}
		}

		return false;
	}
}
