package org.molgenis.data.importer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MEntityImportServiceImpl implements MEntityImportService
{
	MysqlRepositoryCollection store;

	public MEntityImportServiceImpl()
	{
		System.out.println("MEntityImportServiceImpl created");
	}

	@Autowired
	public void setRepositoryCollection(MysqlRepositoryCollection coll)
	{
		this.store = coll;
		System.out.println("MEntityImportServiceImpl created with coll=" + coll);
	}

	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction) throws IOException
	{
		if (store == null) throw new RuntimeException("store was not set");

		EntityImportReport report = new EntityImportReport();

		// TODO: need to change order

		Map<String, DefaultEntityMetaData> metadata = getEntityMetaData(source);

		for (String name : metadata.keySet())
		{
			if (!"entities".equals(name) && !"attributes".equals(name))
			{
				Repository from = source.getRepositoryByEntityName(name);

				// TODO check if compatible with metadata

				// create repo if needed
				MysqlRepository to = (MysqlRepository) store.getRepositoryByEntityName(name);

				if (to == null)
				{
                    System.out.println("tyring to create: " + name);

                    EntityMetaData em = metadata.get(name);
					if (em == null) throw new IllegalArgumentException("Unknown entity: " + name);
					store.add(em);

                    to = (MysqlRepository) store.getRepositoryByEntityName(name);
				}


				// import
				to.add(from);
			}
		}

		return report;
	}

	@Override
	public Map<String, DefaultEntityMetaData> getEntityMetaData(RepositoryCollection source)
	{
		// TODO: this task is actually a 'merge' instead of 'import'
		// so we need to consider both new metadata as existing ...

		Map<String, DefaultEntityMetaData> entities = new LinkedHashMap<String, DefaultEntityMetaData>();

		// load attributes first (because entities are optional).
		for (Entity a : source.getRepositoryByEntityName("attributes"))
		{
            int i = 1;
			String entityName = a.getString("entity");

			// required
			if (entityName == null) throw new IllegalArgumentException("attributes.entity is missing");
			if (a.get("name") == null) throw new IllegalArgumentException("attributes.name is missing");

			// create entity if not yet defined
			if (entities.get(entityName) == null) entities.put(entityName, new DefaultEntityMetaData(entityName));
			DefaultEntityMetaData md = entities.get(entityName);

			DefaultAttributeMetaData am = new DefaultAttributeMetaData(a.getString("name"));

			if (a.get("dataType") != null)
			{
				FieldType t = MolgenisFieldTypes.getType(a.getString("dataType"));
				if (t == null) throw new IllegalArgumentException("attributes.type error on line " + i + ": "
						+ a.getString("dataType") + " unknown");
				am.setDataType(t);
			}
			if (a.get("nillable") != null) am.setNillable(a.getBoolean("nillable"));
			if (a.get("auto") != null) am.setAuto(a.getBoolean("auto"));
			if (a.get("idAttribute") != null) am.setIdAttribute(a.getBoolean("idAttribute"));

			md.addAttributeMetaData(am);
		}

		// load all entities (optional)
		if (source.getRepositoryByEntityName("entities") != null)
		{
			int i = 1;
			for (Entity e : source.getRepositoryByEntityName("entities"))
			{
				i++;
				String entityName = e.getString("name");

				// required
				if (entityName == null) throw new IllegalArgumentException("entity.name is missing on line " + i);

				if (entities.get(entityName) == null) entities.put(entityName, new DefaultEntityMetaData(entityName));
				DefaultEntityMetaData md = entities.get(entityName);

				if (e.get("description") != null) md.setDescription(e.getString("description"));
			}
		}

		// re-iterate to map the mrefs/xref refEntity (or give error if not found)
		// TODO: consider also those in existing db
		int i = 1;
		for (Entity a : source.getRepositoryByEntityName("attributes"))
		{
			i++;
			if (a.get("refEntity") != null)
			{
				DefaultEntityMetaData em = (DefaultEntityMetaData) entities.get(a.getString("entity"));
				DefaultAttributeMetaData am = (DefaultAttributeMetaData) em.getAttribute(a.getString("name"));

				if (entities.get(a.getString("refEntity")) == null)
				{
					throw new IllegalArgumentException("attributes.refEntity error on line " + i + ": "
							+ a.getString("refEntity") + " unknown");
				}

				am.setRefEntity(entities.get(a.getString("refEntity")));
			}
		}

		return entities;
	}
}
