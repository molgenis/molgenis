package org.molgenis.data.importer;

import static org.molgenis.data.mysql.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.AUTO;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.ENTITY;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.mysql.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.EXTENDS;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.mysql.AttributeMetaDataMetaData;
import org.molgenis.data.mysql.EntityMetaDataMetaData;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.TransformedEntity;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.HugeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
public class EmxImportService implements ImportService
{
	private static final Logger logger = Logger.getLogger(EmxImportService.class);
	private static final List<String> SUPPORTED_FILE_EXTENSIONS = Arrays.asList("xls", "xlsx", "csv", "zip");

	// Sheet names
	private static final String ENTITIES = EntityMetaDataMetaData.ENTITY_NAME;
	private static final String ATTRIBUTES = AttributeMetaDataMetaData.ENTITY_NAME;

	private MysqlRepositoryCollection store;
	private TransactionTemplate transactionTemplate;
	private final DataService dataService;
	private PermissionSystemService permissionSystemService;

	@Autowired
	public EmxImportService(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		logger.debug("MEntityImportServiceImpl created");
		this.dataService = dataService;
	}

	@Autowired
	public void setRepositoryCollection(MysqlRepositoryCollection coll)
	{
		this.store = coll;
		logger.debug("MEntityImportServiceImpl created with coll=" + coll);
	}

	@Autowired
	public void setPlatformTransactionManager(PlatformTransactionManager transactionManager)
	{
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Autowired
	public void setPermissionSystemService(PermissionSystemService permissionSystemService)
	{
		this.permissionSystemService = permissionSystemService;
	}

	@Override
	public boolean canImport(File file, RepositoryCollection source)
	{
		String fileNameExtension = StringUtils.getFilenameExtension(file.getName());
		if (SUPPORTED_FILE_EXTENSIONS.contains(fileNameExtension.toLowerCase()))
		{
			for (String entityName : source.getEntityNames())
			{
				if (entityName.equalsIgnoreCase(AttributeMetaDataMetaData.ENTITY_NAME)) return true;
				if (store.getRepositoryByEntityName(entityName) != null) return true;
			}
		}

		return false;
	}

	@Override
	public EntityImportReport doImport(final RepositoryCollection source, DatabaseAction databaseAction)
	{
		if (store == null) throw new RuntimeException("store was not set");

		List<EntityMetaData> metadataList = Lists.newArrayList();

		if (source.getRepositoryByEntityName(ATTRIBUTES) != null)
		{
			Map<String, DefaultEntityMetaData> metadata = getEntityMetaData(source);
			for (String name : metadata.keySet())
			{
				metadataList.add(metadata.get(name));
			}
		}
		else
		{
			for (String name : source.getEntityNames())
			{
				metadataList.add(dataService.getRepositoryByEntityName(name).getEntityMetaData());
			}
		}

		List<String> addedEntities = Lists.newArrayList();
		Map<String, List<String>> addedAttributes = Maps.newLinkedHashMap();
		// TODO altered entities (merge, see getEntityMetaData)

		try
		{
			return transactionTemplate.execute(new EmxImportTransactionCallback(databaseAction, source, metadataList,
					addedEntities, addedAttributes, permissionSystemService));
		}
		catch (Exception e)
		{
			logger.info("Error during import, rollback.", e);

			// Rollback metadata, create table statements cannot be rolled back, we have to do it ourselfs
			Collections.reverse(addedEntities);

			for (String entityName : addedEntities)
			{
				store.dropEntityMetaData(entityName);
			}

			List<String> entities = Lists.newArrayList(addedAttributes.keySet());
			Collections.reverse(entities);

			for (String entityName : entities)
			{
				List<String> attributes = addedAttributes.get(entityName);
				for (String attributeName : attributes)
				{
					store.dropAttributeMetaData(entityName, attributeName);
				}
			}

			throw e;
		}

	}

	@Override
	public EntitiesValidationReport validateImport(File file, RepositoryCollection source)
	{
		EntitiesValidationReportImpl report = new EntitiesValidationReportImpl();

		// compare the data sheets against metadata in store or imported file
		Map<String, DefaultEntityMetaData> metaDataMap = new HashMap<String, DefaultEntityMetaData>();

		if (source.getRepositoryByEntityName(ATTRIBUTES) != null)
		{
			metaDataMap = getEntityMetaData(source);
		}
		else
		{
			for (String name : source.getEntityNames())
			{
				metaDataMap.put(name, (DefaultEntityMetaData) dataService.getRepositoryByEntityName(name)
						.getEntityMetaData());
			}
		}

		for (String sheet : source.getEntityNames())
		{
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
		}

		return report;
	}

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
			String attributeDataType = attribute.getString(DATA_TYPE);
			String refEntityName = attribute.getString(REF_ENTITY);

			// required
			if (entityName == null) throw new IllegalArgumentException("attributes.entity is missing");
			if (attributeName == null) throw new IllegalArgumentException("attributes.name is missing");

			// create entity if not yet defined
			if (!entities.containsKey(entityName)) entities.put(entityName, new DefaultEntityMetaData(entityName));
			DefaultEntityMetaData defaultEntityMetaData = entities.get(entityName);

			// create attribute meta data
			DefaultAttributeMetaData defaultAttributeMetaData = new DefaultAttributeMetaData(attributeName);

			if (attributeDataType != null)
			{
				FieldType t = MolgenisFieldTypes.getType(attributeDataType);
				if (t == null) throw new IllegalArgumentException("attributes.dataType error on line " + i + ": "
						+ attributeDataType + " unknown data type");
				defaultAttributeMetaData.setDataType(t);
			}
			else
			{
				defaultAttributeMetaData.setDataType(MolgenisFieldTypes.STRING);
			}

			Boolean attributeNillable = attribute.getBoolean(NILLABLE);
			Boolean attributeAuto = attribute.getBoolean(AUTO);
			Boolean attributeIdAttribute = attribute.getBoolean(ID_ATTRIBUTE);
			Boolean attributeVisible = attribute.getBoolean(VISIBLE);
			Boolean attributeAggregateable = attribute.getBoolean(AGGREGATEABLE);
			Boolean lookupAttribute = attribute.getBoolean(LOOKUP_ATTRIBUTE);
			Boolean labelAttribute = attribute.getBoolean(LABEL_ATTRIBUTE);
			Boolean readOnly = attribute.getBoolean(READ_ONLY);

			if (attributeNillable != null) defaultAttributeMetaData.setNillable(attributeNillable);
			if (attributeAuto != null) defaultAttributeMetaData.setAuto(attributeAuto);
			if (attributeIdAttribute != null) defaultAttributeMetaData.setIdAttribute(attributeIdAttribute);
			if (attributeVisible != null) defaultAttributeMetaData.setVisible(attributeVisible);
			if (attributeAggregateable != null) defaultAttributeMetaData.setAggregateable(attributeAggregateable);
			if (refEntityName != null) defaultAttributeMetaData.setRefEntity(entities.get(refEntityName));
			if (lookupAttribute != null) defaultAttributeMetaData.setLookupAttribute(lookupAttribute);
			if (labelAttribute != null) defaultAttributeMetaData.setLabelAttribute(labelAttribute);
			if (readOnly != null) defaultAttributeMetaData.setReadOnly(readOnly);

			defaultAttributeMetaData.setLabel(attribute.getString(LABEL));
			defaultAttributeMetaData.setDescription(attribute.getString(DESCRIPTION));

			if (defaultAttributeMetaData.getDataType() instanceof EnumField)
			{
				List<String> enumOptions = attribute.getList(ENUM_OPTIONS);
				if ((enumOptions == null) || enumOptions.isEmpty())
				{
					throw new MolgenisDataException("Missing enum options for attribute ["
							+ defaultAttributeMetaData.getName() + "] of entity [" + entityName + "]");
				}
				defaultAttributeMetaData.setEnumOptions(enumOptions);
			}

			Long rangeMin;
			Long rangeMax;
			try
			{
				rangeMin = attribute.getLong(RANGE_MIN);
			}
			catch (ConversionFailedException e)
			{
				throw new MolgenisDataException("Invalid range rangeMin [" + attribute.getString(RANGE_MIN)
						+ "] value for attribute [" + attributeName + "] of entity [" + entityName
						+ "], should be a long");
			}

			try
			{
				rangeMax = attribute.getLong(RANGE_MAX);
			}
			catch (ConversionFailedException e)
			{
				throw new MolgenisDataException("Invalid rangeMax value [" + attribute.getString(RANGE_MAX)
						+ "] for attribute [" + attributeName + "] of entity [" + entityName + "], should be a long");
			}

			if ((rangeMin != null) || (rangeMax != null))
			{
				if (!(defaultAttributeMetaData.getDataType() instanceof IntField)
						&& !(defaultAttributeMetaData.getDataType() instanceof LongField))
				{
					throw new MolgenisDataException("Range not supported for ["
							+ defaultAttributeMetaData.getDataType().getEnumType()
							+ "] fields only int and long are supported. (attribute ["
							+ defaultAttributeMetaData.getName() + "] of entity [" + entityName + "])");
				}

				defaultAttributeMetaData.setRange(new Range(rangeMin, rangeMax));
			}

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

				if (!entities.containsKey(entityName)) entities.put(entityName, new DefaultEntityMetaData(entityName));

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
			final String refEntityName = (String) attribute.get(REF_ENTITY);
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

	private final class EmxImportTransactionCallback implements TransactionCallback<EntityImportReport>
	{
		private final RepositoryCollection source;
		private final List<EntityMetaData> sourceMetadata;
		private final List<String> addedEntities;
		private final Map<String, List<String>> addedAttributes;// Attributes per entity
		private final DatabaseAction dbAction;
		private final PermissionSystemService permissionSystemService;

		private EmxImportTransactionCallback(DatabaseAction dbAction, RepositoryCollection source,
				List<EntityMetaData> metadata, List<String> addedEntities, Map<String, List<String>> addedAttributes,
				PermissionSystemService permissionSystemService)
		{
			this.permissionSystemService = permissionSystemService;
			this.dbAction = dbAction;
			this.source = source;
			this.sourceMetadata = metadata;
			this.addedEntities = addedEntities;
			this.addedAttributes = addedAttributes;
		}

		@Override
		public EntityImportReport doInTransaction(TransactionStatus status)
		{
			EntityImportReport report = new EntityImportReport();

			try
			{
				Set<EntityMetaData> allMetaData = Sets.newLinkedHashSet(sourceMetadata);
				Set<EntityMetaData> existingMetaData = store.getAllEntityMetaDataIncludingAbstract();
				allMetaData.addAll(existingMetaData);

				// Use all metadata for dependency resolving
				List<EntityMetaData> resolved = DependencyResolver.resolve(allMetaData);

				// Only import source
				resolved.retainAll(sourceMetadata);

				for (EntityMetaData entityMetaData : resolved)
				{
					String name = entityMetaData.getName();
					if (!ENTITIES.equals(name) && !ATTRIBUTES.equals(name))
					{
						Entity toMeta = store.getEntityMetaDataEntity(name);
						if (toMeta == null)
						{
							logger.debug("tyring to create: " + name);
							addedEntities.add(name);
							report.addNewEntity(name);
							store.add(entityMetaData);
						}
						else if (!entityMetaData.isAbstract())
						{
							List<String> addedEntityAttributes = store.update(entityMetaData);
							if ((addedEntityAttributes != null) && !addedEntityAttributes.isEmpty())
							{
								addedAttributes.put(name, addedEntityAttributes);
							}
						}
					}
				}

				// Give user permission to see and edit his imported entities (not if user is admin, admins can do that
				// anyway)
				if (!SecurityUtils.currentUserIsSu())
				{
					permissionSystemService.giveUserEntityAndMenuPermissions(SecurityContextHolder.getContext(),
							addedEntities);
				}

				// import data
				for (final EntityMetaData entityMetaData : resolved)
				{
					String name = entityMetaData.getName();
					CrudRepository crudRepository = (CrudRepository) store.getRepositoryByEntityName(name);
					if (crudRepository != null)
					{
						Repository fileEntityRepository = source.getRepositoryByEntityName(name);
						// check to prevent nullpointer when importing metadata only
						if (fileEntityRepository != null)
						{
							// transforms entities so that they match the entity meta data of the output repository
							Iterable<Entity> entities = Iterables.transform(fileEntityRepository,
									new Function<Entity, Entity>()
									{
										@Override
										public Entity apply(Entity entity)
										{
											return new TransformedEntity(entity, entityMetaData, dataService);
										}
									});
							entities = DependencyResolver.resolveSelfReferences(entities, entityMetaData);
							int count = update(crudRepository, entities, dbAction);
							report.getNrImportedEntitiesMap().put(name, count);
						}
					}
				}

				return report;
			}
			catch (Exception e)
			{
				logger.info("Error in import transaction, setRollbackOnly", e);
				status.setRollbackOnly();
				throw e;
			}
		}

		public int update(CrudRepository repo, Iterable<? extends Entity> entities, DatabaseAction dbAction)
		{
			if (entities == null) return 0;

			String idAttributeName = repo.getEntityMetaData().getIdAttribute().getName();
			FieldType idDataType = repo.getEntityMetaData().getIdAttribute().getDataType();

			HugeSet<Object> existingIds = new HugeSet<Object>();
			HugeSet<Object> ids = new HugeSet<Object>();
			try
			{
				for (Entity entity : entities)
				{
					Object id = entity.get(idAttributeName);
					if (id != null)
					{
						ids.add(id);
					}
				}

				if (!ids.isEmpty())
				{
					// Check if the ids already exist
					if (repo.count() > 0)
					{
						int batchSize = 100;
						Query q = new QueryImpl();
						Iterator<Object> it = ids.iterator();
						int batchCount = 0;
						while (it.hasNext())
						{
							q.eq(idAttributeName, it.next());
							batchCount++;
							if (batchCount == batchSize || !it.hasNext())
							{
								for (Entity existing : repo.findAll(q))
								{
									existingIds.add(existing.getIdValue());
								}
								q = new QueryImpl();
								batchCount = 0;
							}
							else
							{
								q.or();
							}
						}
					}
				}

				int count = 0;
				switch (dbAction)
				{
					case ADD:
						if (!existingIds.isEmpty())
						{
							StringBuilder msg = new StringBuilder();
							msg.append("Trying to add existing ").append(repo.getName())
									.append(" entities as new insert: ");

							int i = 0;
							Iterator<?> it = existingIds.iterator();
							while (it.hasNext() && i < 5)
							{
								if (i > 0)
								{
									msg.append(",");
								}
								msg.append(it.next());
								i++;
							}

							if (it.hasNext())
							{
								msg.append(" and more.");
							}
							throw new MolgenisDataException(msg.toString());
						}
						count = repo.add(entities);
						break;

					case ADD_UPDATE_EXISTING:
						int batchSize = 1000;
						List<Entity> existingEntities = Lists.newArrayList();
						List<Entity> newEntities = Lists.newArrayList();

						Iterator<? extends Entity> it = entities.iterator();
						while (it.hasNext())
						{
							Entity entity = it.next();
							count++;
							Object id = idDataType.convert(entity.get(idAttributeName));
							if (existingIds.contains(id))
							{
								existingEntities.add(entity);
								if (existingEntities.size() == batchSize)
								{
									repo.update(existingEntities);
									existingEntities.clear();
								}
							}
							else
							{
								newEntities.add(entity);
								if (newEntities.size() == batchSize)
								{
									repo.add(newEntities);
									newEntities.clear();
								}
							}
						}

						if (!existingEntities.isEmpty())
						{
							repo.update(existingEntities);
						}

						if (!newEntities.isEmpty())
						{
							repo.add(newEntities);
						}
						break;

					case UPDATE:
						int errorCount = 0;
						StringBuilder msg = new StringBuilder();
						msg.append("Trying to update not exsisting ").append(repo.getName()).append(" entities:");

						for (Entity entity : entities)
						{
							count++;
							Object id = idDataType.convert(entity.get(idAttributeName));
							if (!existingIds.contains(id))
							{
								if (++errorCount == 6)
								{
									break;
								}

								if (errorCount > 0)
								{
									msg.append(", ");
								}
								msg.append(id);
							}
						}

						if (errorCount > 0)
						{
							if (errorCount == 6)
							{
								msg.append(" and more.");
							}
							throw new MolgenisDataException(msg.toString());
						}
						repo.update(entities);
						break;

					default:
						break;

				}

				return count;
			}
			finally
			{
				IOUtils.closeQuietly(existingIds);
				IOUtils.closeQuietly(ids);
			}
		}
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
