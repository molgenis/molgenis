package org.molgenis.data.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.util.EntityUtils;

public class EntityMetaDataUtils
{
	Logger logger = Logger.getLogger(EntityMetaDataUtils.class);
	Map<String, EntityMetaData> omxEntities = new LinkedHashMap<String, EntityMetaData>();

	public EntityMetaDataUtils()
	{
		DefaultEntityMetaData characteristic = new DefaultEntityMetaData("Characteristic");
		characteristic.addAttribute("identifier");
		omxEntities.put(characteristic.getName(), characteristic);

		// TODO: categroy should be converted
		DefaultEntityMetaData category = new DefaultEntityMetaData("Category");
		category.addAttribute("identifier");
		omxEntities.put(category.getName(), category);
	}

	public Collection<EntityMetaData> load(RepositoryCollection omx)
	{
		// extract attribute metadata
		Map<String, AttributeMetaData> attributes = new LinkedHashMap<String, AttributeMetaData>();
		for (Entity e : omx.getRepositoryByEntityName("observablefeature"))
		{
			logger.debug("found observablefeature: " + e);

			DefaultAttributeMetaData att = new DefaultAttributeMetaData(e.getString("name"));
			if (e.get("dataType") != null) att.setDataType(MolgenisFieldTypes.getType(e.getString("dataType")));
			attributes.put(e.getString("identifier"), att);
			if (e.get("description") != null) att.setDescription(e.getString("description"));
			// TODO unit! if(e.get("unit") != null)

			if ("xref".equals(e.get("dataType")) || "mref".equals(e.get("dataType")))
			{
				// TODO: cannot solve!!!
				att.setRefEntity(omxEntities.get("Characteristic"));
			}
			if ("categorical".equals(e.get("dataType")))
			{
				att.setRefEntity(omxEntities.get("Category"));
			}
		}
		// TODO: fix categorical!

		// extract protocols as entities(abstract=true)
		Map<String, EntityMetaData> entities = new LinkedHashMap<String, EntityMetaData>();
		for (Entity e : omx.getRepositoryByEntityName("protocol"))
		{
			// skip all null entities
			if (hasValues(e))
			{
				logger.debug("found protocol: " + e);

				DefaultEntityMetaData ent = new DefaultEntityMetaData(e.getString("identifier")); // alas
				ent.setLabel(e.getString("name"));
				ent.setAbstract(true);

				// add attributes
				if (e.get("features_identifier") != null) for (String attIdentifier : e.getList("features_identifier"))
				{
					if (attributes.get(attIdentifier) == null) throw new RuntimeException("attribute '" + attIdentifier
							+ "' unknown");
					ent.addAttributeMetaData(attributes.get(attIdentifier));
				}

				entities.put(e.getString("identifier"), ent);
			}
		}

        for(Entity e: omx.getRepositoryByEntityName("protocol"))
        {
            // add subprotocols as compound
            if (e.get("subprotocols_identifier") != null)
            {
                for (String subProtocol : e.getList("subprotocols_identifier"))
                {
                    DefaultAttributeMetaData att = new DefaultAttributeMetaData(subProtocol);
                    att.setDataType(MolgenisFieldTypes.COMPOUND);
                    att.setRefEntity(entities.get(subProtocol));
                    ((DefaultEntityMetaData)entities.get(e.get("identifier"))).addAttributeMetaData(att);
                }

            }
        }

		// create dataset as entities
		for (Entity e : omx.getRepositoryByEntityName("dataset"))
		{
			logger.debug("found dataset: " + e);

			DefaultEntityMetaData ent = new DefaultEntityMetaData(e.getString("identifier"));
			ent.setLabel(e.getString("name"));
			// dataset 'extends' protocol
			ent.setExtends(entities.get(e.getString("protocolused_identifier")));
			entities.put(e.getString("identifier"), ent);
		}

		return entities.values();
	}

	public void store(Collection<EntityMetaData> model, RepositoryCollection store)
	{
		Map<String, Entity> entities = new LinkedHashMap<String, Entity>();
		Map<String, Entity> attributes = new LinkedHashMap<String, Entity>();

		for (EntityMetaData emd : model)
		{
			toEntity(emd, entities, attributes);
		}
	}

	private void toEntity(EntityMetaData emd, Map<String, Entity> entities, Map<String, Entity> attributes)
	{
		// TODO prevent duplicates
		Entity e = new MapEntity();
		e.set("name", emd.getName());
		entities.put(emd.getName(), e);

		logger.debug("entity: " + e);

		for (AttributeMetaData amd : emd.getAttributes())
		{
			Entity a = new MapEntity();
			a.set("name", amd.getName());
			a.set("entity", emd.getName());
			if (amd.getDataType() != null && amd.getDataType() != MolgenisFieldTypes.STRING) a.set("dataType",
					amd.getDataType());
			if (amd.getDefaultValue() != null) a.set("defaultValue", amd.getDefaultValue());
			if (amd.getRefEntity() != null) a.set("refEntity", amd.getRefEntity().getName());

			logger.debug("attribute: " + a);

			attributes.put(emd.getName() + "." + amd.getName(), a);

			// compound
			if (amd.getDataType() == MolgenisFieldTypes.COMPOUND && entities.get(amd.getRefEntity().getName()) == null)
			{
				this.toEntity(amd.getRefEntity(), entities, attributes);
			}
		}
	}

	public String toXml(Collection<EntityMetaData> entities)
	{
		String xml = "<molgenis>\n";

		// TODO prevent duplicates
		Map<String, EntityMetaData> allEntities = new LinkedHashMap<String, EntityMetaData>();
		getEntitiesRecursive(allEntities, entities);

		for (EntityMetaData emd : allEntities.values())
		{
			xml += "\t<entity name=\"" + emd.getName() + "\"";
			if (emd.isAbstract()) xml += " abstract=\"true\"";
			if (emd.getExtends() != null) xml += " extends=\"" + emd.getExtends().getName() + "\"";
			xml += ">\n";
			for (AttributeMetaData amd : emd.getAttributes())
			{
				xml += "\t\t<field name=\"" + amd.getName() + "\"";
				if (amd.getDataType() != MolgenisFieldTypes.STRING) xml += " dataType=\"" + amd.getDataType() + "\"";
				if (amd.getRefEntity() != null) xml += " refEntity=\"" + amd.getRefEntity().getName() + "\"";
				xml += "/>\n";
			}
			xml += "\t</entity\n";
		}

		xml += "</molgenis>\n";
		return xml;
	}

	private void getEntitiesRecursive(Map<String, EntityMetaData> allEntities, Collection<EntityMetaData> rootEntities)
	{
		for (EntityMetaData emd : rootEntities)
		{
			if (!allEntities.containsKey(emd.getName())) allEntities.put(emd.getName(), emd);
			for (AttributeMetaData amd : emd.getAttributes())
			{
				if (amd.getRefEntity() != null && !allEntities.containsKey(amd.getRefEntity().getName()))
				{
					getEntitiesRecursive(allEntities, Arrays.asList(new EntityMetaData[]
					{ amd.getRefEntity() }));
				}
			}
		}
	}

	private boolean hasValues(Entity e)
	{
		return !EntityUtils.isEmpty(e);
	}

}
