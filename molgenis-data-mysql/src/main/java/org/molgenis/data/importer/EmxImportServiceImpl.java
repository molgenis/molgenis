package org.molgenis.data.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmxImportServiceImpl implements EmxImporterService
{
	private static final Logger logger = Logger.getLogger(EmxImportServiceImpl.class);

	// Sheet names
	private static final String ENTITIES = "entities";
	private static final String ATTRIBUTES = "attributes";

	// Column names
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String REFENTITY = "refEntity";
	private static final String ENTITY = "entity";
	private static final String DATATYPE = "dataType";
	private static final String AUTO = "auto";
	private static final String IDATTRIBUTE = "idAttribute";
	private static final String NILLABLE = "nillable";
	private static final String ABSTRACT = "abstract";
	private static final String VISIBLE = "visible";
	private static final String LABEL = "label";
	private static final String EXTENDS = "extends";

	private MysqlRepositoryCollection store;

	public EmxImportServiceImpl()
	{
		logger.debug("MEntityImportServiceImpl created");
	}

	@Autowired
	public void setRepositoryCollection(MysqlRepositoryCollection coll)
	{
		this.store = coll;
		logger.debug("MEntityImportServiceImpl created with coll=" + coll);
	}

	@Override
	@Transactional(rollbackFor = IOException.class)
	public EntityImportReport doImport(RepositoryCollection source, DatabaseAction databaseAction) throws IOException
	{
		if (store == null) throw new RuntimeException("store was not set");

		EntityImportReport report = new EntityImportReport();

		// TODO: need to change order

		Map<String, DefaultEntityMetaData> metadata = getEntityMetaData(source);

		for (Entry<String, DefaultEntityMetaData> entry : metadata.entrySet())
		{
			String name = entry.getKey();
			DefaultEntityMetaData defaultEntityMetaData = entry.getValue();
			if (defaultEntityMetaData == null) throw new IllegalArgumentException("Unknown entity: " + name);

			if (!ENTITIES.equals(name) && !ATTRIBUTES.equals(name))
			{
				Repository from = source.getRepositoryByEntityName(name);

				// TODO check if compatible with metadata
				MysqlRepository to = (MysqlRepository) store.getRepositoryByEntityName(name);
				if (to == null)
				{
					logger.debug("tyring to create: " + name);
					to = store.add(defaultEntityMetaData);
				}

				// import
				if (to != null)
				{
					Integer count = to.add(from);
					report.getNrImportedEntitiesMap().put(name, count);
				}
			}
		}

		return report;
	}

	@Override
	public EntitiesValidationReport validateImport(RepositoryCollection source)
	{
		EntitiesValidationReportImpl report = new EntitiesValidationReportImpl();

		// compare the data sheets against metadata in store or imported file
		Map<String, DefaultEntityMetaData> metaDataMap = getEntityMetaData(source);

		for (String sheet : source.getEntityNames())
			if (!ENTITIES.equals(sheet) && !ATTRIBUTES.equals(sheet))
			{
				// check if sheet is known?
				if (metaDataMap.containsKey(sheet)) report.getSheetsImportable().put(sheet, true);
				else report.getSheetsImportable().put(sheet, false);

				// check the fields
				Repository s = source.getRepositoryByEntityName(sheet);
				EntityMetaData target = metaDataMap.get(sheet);

				if (target != null)
				{
					List<String> fieldsAvailable = new ArrayList<String>();
					List<String> fieldsImportable = new ArrayList<String>();
					List<String> fieldsRequired = new ArrayList<String>();
					List<String> fieldsUnknown = new ArrayList<String>();

					for (AttributeMetaData att : s.getEntityMetaData().getAttributes())
					{
						if (target.getAttribute(att.getName()) == null) fieldsUnknown.add(att.getName());
						else fieldsImportable.add(att.getName());
					}
					for (AttributeMetaData att : target.getAttributes())
					{
						if (!att.isAuto() && !fieldsImportable.contains(att.getName()))
						{
							if (!att.isNillable()) fieldsRequired.add(att.getName());
							else fieldsAvailable.add(att.getName());
						}
					}

					report.getFieldsAvailable().put(sheet, fieldsAvailable);
					report.getFieldsRequired().put(sheet, fieldsRequired);
					report.getFieldsUnknown().put(sheet, fieldsUnknown);
					report.getFieldsImportable().put(sheet, fieldsImportable);
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
		loadAllAttributesToMap(source, entities);
		loadAllEntitiesToMap(source, entities);
		reiterateToMapRefEntity(source, entities);

		return entities;
	}

	/**
	 * Load all attributes
	 * 
	 * @param source
	 * @param entities
	 *            the map to add entities meta data
	 */
	private void loadAllAttributesToMap(RepositoryCollection source, Map<String, DefaultEntityMetaData> entities)
	{
		for (Entity attribute : source.getRepositoryByEntityName(ATTRIBUTES))
		{
			int i = 1;
			String entityName = attribute.getString(ENTITY);
			String attributeName = attribute.getString(NAME);
			String attributeDataType = attribute.getString(DATATYPE);
			String refEntityName = attribute.getString(REFENTITY);

			// required
			if (entityName == null) throw new IllegalArgumentException("attributes.entity is missing");
			if (attributeName == null) throw new IllegalArgumentException("attributes.name is missing");

			// create entity if not yet defined
			if (entities.get(entityName) == null) entities.put(entityName, new DefaultEntityMetaData(entityName));
			DefaultEntityMetaData defaultEntityMetaData = entities.get(entityName);

			// create attribute meta data
			DefaultAttributeMetaData defaultAttributeMetaData = new DefaultAttributeMetaData(attributeName);

			if (attributeDataType != null)
			{
				FieldType t = MolgenisFieldTypes.getType(attributeDataType);
				if (t == null) throw new IllegalArgumentException("attributes.type error on line " + i + ": "
						+ attributeDataType + " unknown");
				defaultAttributeMetaData.setDataType(t);
			}
			else
			{
				defaultAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);
			}

			Boolean attributeNillable = attribute.getBoolean(NILLABLE);
			Boolean attributeAuto = attribute.getBoolean(AUTO);
			Boolean attributeIdAttribute = attribute.getBoolean(IDATTRIBUTE);
			Boolean attributeVisible = attribute.getBoolean(VISIBLE);

			if (attributeNillable != null) defaultAttributeMetaData.setNillable(attributeNillable);
			if (attributeAuto != null) defaultAttributeMetaData.setAuto(attributeAuto);
			if (attributeIdAttribute != null) defaultAttributeMetaData.setIdAttribute(attributeIdAttribute);
			if (attributeVisible != null) defaultAttributeMetaData.setVisible(attributeVisible);

			if (refEntityName != null)
			{
				defaultAttributeMetaData.setRefEntity(entities.get(refEntityName));
			}

			defaultAttributeMetaData.setLabel(attribute.getString(LABEL));
			defaultAttributeMetaData.setDescription(attribute.getString(DESCRIPTION));

			defaultEntityMetaData.addAttributeMetaData(defaultAttributeMetaData);
		}
	}

	/**
	 * Load all entities (optional)
	 * 
	 * @param source
	 *            the map to add entities meta data
	 */
	private void loadAllEntitiesToMap(RepositoryCollection source, Map<String, DefaultEntityMetaData> entities)
	{
		if (source.getRepositoryByEntityName(ENTITIES) != null)
		{
			int i = 1;
			for (Entity entity : source.getRepositoryByEntityName(ENTITIES))
			{
				i++;
				String entityName = entity.getString(NAME);

				// required
				if (entityName == null) throw new IllegalArgumentException("entity.name is missing on line " + i);

				if (entities.get(entityName) == null) entities.put(entityName, new DefaultEntityMetaData(entityName));

				DefaultEntityMetaData md = entities.get(entityName);
				md.setLabel(entity.getString(LABEL));
				md.setDescription(entity.getString(DESCRIPTION));
				if (entity.getBoolean(ABSTRACT) != null) md.setAbstract(entity.getBoolean(ABSTRACT));

				String extendsEntityName = entity.getString(EXTENDS);
				if (extendsEntityName != null)
				{
					DefaultEntityMetaData extendsEntityMeta = entities.get(extendsEntityName);
					if (extendsEntityMeta == null)
					{
						throw new MolgenisDataException("Missing super entity " + extendsEntityName + " for entity "
								+ entityName + " on line " + i);
					}
					md.setExtends(extendsEntityMeta);
				}
			}
		}
	}

	/**
	 * re-iterate to map the mrefs/xref refEntity (or give error if not found) TODO consider also those in existing db
	 * 
	 * @param source
	 *            the map to add entities meta data
	 */
	private void reiterateToMapRefEntity(RepositoryCollection source, Map<String, DefaultEntityMetaData> entities)
	{
		int i = 1;
		for (Entity attribute : source.getRepositoryByEntityName(ATTRIBUTES))
		{
			final String refEntityName = (String) attribute.get(REFENTITY);
			final String entityName = attribute.getString(ENTITY);
			final String attributeName = attribute.getString(NAME);
			i++;
			if (refEntityName != null)
			{
				DefaultEntityMetaData defaultEntityMetaData = entities.get(entityName);
				DefaultAttributeMetaData defaultAttributeMetaData = (DefaultAttributeMetaData) defaultEntityMetaData
						.getAttribute(attributeName);

				if (entities.get(refEntityName) == null)
				{
					throw new IllegalArgumentException("attributes.refEntity error on line " + i + ": " + refEntityName
							+ " unknown");
				}

				defaultAttributeMetaData.setRefEntity(entities.get(refEntityName));
			}
		}
	}
}
