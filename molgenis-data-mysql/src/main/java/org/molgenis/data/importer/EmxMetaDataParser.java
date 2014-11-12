package org.molgenis.data.importer;

import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LABEL_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.LOOKUP_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NAME;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.NILLABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.meta.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.meta.EntityMetaDataMetaData.PACKAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.util.StringUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Parser for the EMX metadata.
 */
public class EmxMetaDataParser
{

	static final String PACKAGES = PackageMetaData.ENTITY_NAME;
	static final String TAGS = TagMetaData.ENTITY_NAME;
	static final String ATTRIBUTES = AttributeMetaDataMetaData.ENTITY_NAME;
	// Sheet names
	static final String ENTITIES = EntityMetaDataMetaData.ENTITY_NAME;
	static final List<String> SUPPORTED_ENTITY_ATTRIBUTES = Arrays.asList(
			org.molgenis.data.meta.EntityMetaDataMetaData.LABEL.toLowerCase(),
			org.molgenis.data.meta.EntityMetaDataMetaData.DESCRIPTION.toLowerCase(), "name", ABSTRACT.toLowerCase(),
			EXTENDS.toLowerCase(), "package");
	static final List<String> SUPPORTED_ATTRIBUTE_ATTRIBUTES = Arrays.asList(AGGREGATEABLE.toLowerCase(),
			DATA_TYPE.toLowerCase(), DESCRIPTION.toLowerCase(), ENTITY.toLowerCase(), ENUM_OPTIONS.toLowerCase(),
			ID_ATTRIBUTE.toLowerCase(), LABEL.toLowerCase(), LABEL_ATTRIBUTE.toLowerCase(),
			LOOKUP_ATTRIBUTE.toLowerCase(), NAME, NILLABLE.toLowerCase(), RANGE_MAX.toLowerCase(),
			RANGE_MIN.toLowerCase(), READ_ONLY.toLowerCase(), REF_ENTITY.toLowerCase(), VISIBLE.toLowerCase(),
			UNIQUE.toLowerCase(), org.molgenis.data.meta.AttributeMetaDataMetaData.TAGS.toLowerCase());

	/**
	 * Parses metadata from a collection of repositories and creates a list of EntityMetaData
	 * 
	 * @param source
	 *            the {@link RepositoryCollection} containing the metadata to parse
	 * @return map containing the parsed metadata
	 */
	private Map<String, EntityMetaData> getAllEntityMetaDataFromSource(RepositoryCollection source)
	{
		// TODO: this task is actually a 'merge' instead of 'import'
		// so we need to consider both new metadata and existing ...

		// load attributes first, entities and packages are optional
		Map<String, EditableEntityMetaData> entities = parseAttributesSheet(source);
		parseEntitiesSheet(source, entities);
		parsePackagesSheetToEntityMap(source, entities);
		reiterateToMapRefEntity(source, entities);

		return Collections.<String, EntityMetaData> unmodifiableMap(entities);
	}

	/**
	 * Load all attributes
	 * 
	 * @param source
	 */
	private Map<String, EditableEntityMetaData> parseAttributesSheet(RepositoryCollection source)
	{
		Map<String, EditableEntityMetaData> entities = new LinkedHashMap<String, EditableEntityMetaData>();

		Repository attributesRepo = source.getRepositoryByEntityName(EmxMetaDataParser.ATTRIBUTES);
		for (AttributeMetaData attr : attributesRepo.getEntityMetaData().getAtomicAttributes())
		{
			if (!EmxMetaDataParser.SUPPORTED_ATTRIBUTE_ATTRIBUTES.contains(attr.getName().toLowerCase()))
			{
				throw new IllegalArgumentException("Unsupported attribute metadata: attributes. " + attr.getName());
			}
		}

		int i = 1;// Header
		for (Entity attribute : attributesRepo)
		{
			i++;
			String entityName = attribute.getString(ENTITY);
			String attributeName = attribute.getString(NAME);
			String attributeDataType = attribute.getString(DATA_TYPE);
			String refEntityName = attribute.getString(REF_ENTITY);

			// required
			if (attributeName == null) throw new IllegalArgumentException("attributes.name is missing on line " + i);
			if (entityName == null) throw new IllegalArgumentException(
					"attributes.entity is missing for attrubute named: " + attributeName + " on line " + i);

			// create entity if not yet defined
			if (!entities.containsKey(entityName)) entities.put(entityName, new DefaultEntityMetaData(entityName));
			EditableEntityMetaData defaultEntityMetaData = entities.get(entityName);

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
			Boolean attributeIdAttribute = attribute.getBoolean(ID_ATTRIBUTE);
			Boolean attributeVisible = attribute.getBoolean(VISIBLE);
			Boolean attributeAggregateable = attribute.getBoolean(AGGREGATEABLE);
			Boolean lookupAttribute = attribute.getBoolean(LOOKUP_ATTRIBUTE);
			Boolean labelAttribute = attribute.getBoolean(LABEL_ATTRIBUTE);
			Boolean readOnly = attribute.getBoolean(READ_ONLY);
			Boolean unique = attribute.getBoolean(UNIQUE);

			if (attributeNillable != null) defaultAttributeMetaData.setNillable(attributeNillable);
			if (attributeIdAttribute != null) defaultAttributeMetaData.setIdAttribute(attributeIdAttribute);
			if (attributeVisible != null) defaultAttributeMetaData.setVisible(attributeVisible);
			if (attributeAggregateable != null) defaultAttributeMetaData.setAggregateable(attributeAggregateable);
			if (refEntityName != null) defaultAttributeMetaData.setRefEntity(entities.get(refEntityName));
			if (readOnly != null) defaultAttributeMetaData.setReadOnly(readOnly);
			if (unique != null) defaultAttributeMetaData.setUnique(unique);

			if (lookupAttribute != null)
			{
				if (lookupAttribute
						&& ((defaultAttributeMetaData.getDataType() instanceof XrefField) || (defaultAttributeMetaData
								.getDataType() instanceof MrefField)))
				{
					throw new IllegalArgumentException("attributes.lookupAttribute error on line " + i + " ("
							+ entityName + "." + attributeName + "): lookupAttribute cannot be of type "
							+ defaultAttributeMetaData.getDataType());
				}

				defaultAttributeMetaData.setLookupAttribute(lookupAttribute);
			}

			if (labelAttribute != null)
			{
				if (labelAttribute
						&& ((defaultAttributeMetaData.getDataType() instanceof XrefField) || (defaultAttributeMetaData
								.getDataType() instanceof MrefField)))
				{
					throw new IllegalArgumentException("attributes.labelAttribute error on line " + i + " ("
							+ entityName + "." + attributeName + "): labelAttribute cannot be of type "
							+ defaultAttributeMetaData.getDataType());
				}

				defaultAttributeMetaData.setLabelAttribute(labelAttribute);
			}

			defaultAttributeMetaData.setLabel(attribute.getString(LABEL));
			defaultAttributeMetaData.setDescription(attribute.getString(DESCRIPTION));

			if (defaultAttributeMetaData.getDataType() instanceof EnumField)
			{
				List<String> enumOptions = attribute.getList(ENUM_OPTIONS);
				if ((enumOptions == null) || enumOptions.isEmpty())
				{
					throw new IllegalArgumentException("Missing enum options for attribute ["
							+ defaultAttributeMetaData.getName() + "] of entity [" + entityName + "]");
				}
				defaultAttributeMetaData.setEnumOptions(enumOptions);
			}

			if (((defaultAttributeMetaData.getDataType() instanceof XrefField) || (defaultAttributeMetaData
					.getDataType() instanceof MrefField)) && StringUtils.isEmpty(refEntityName))
			{
				throw new IllegalArgumentException("Missing refEntity on line " + i + " (" + entityName + "."
						+ attributeName + ")");
			}

			if (((defaultAttributeMetaData.getDataType() instanceof XrefField) || (defaultAttributeMetaData
					.getDataType() instanceof MrefField))
					&& defaultAttributeMetaData.isNillable()
					&& defaultAttributeMetaData.isAggregateable())
			{
				throw new IllegalArgumentException("attributes.aggregatable error on line " + i + " (" + entityName
						+ "." + attributeName + "): aggregatable nillable attribute cannot be of type "
						+ defaultAttributeMetaData.getDataType());
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
		return entities;
	}

	/**
	 * Load all entities (optional)
	 * 
	 * @param source
	 *            the map to add entities meta data
	 * @param entities
	 *            TODO
	 */
	private void parseEntitiesSheet(RepositoryCollection source, Map<String, EditableEntityMetaData> entities)
	{
		Repository entitiesRepo = source.getRepositoryByEntityName(EmxMetaDataParser.ENTITIES);
		if (entitiesRepo != null)
		{
			for (AttributeMetaData attr : entitiesRepo.getEntityMetaData().getAtomicAttributes())
			{
				if (!EmxMetaDataParser.SUPPORTED_ENTITY_ATTRIBUTES.contains(attr.getName().toLowerCase()))
				{
					throw new IllegalArgumentException("Unsupported entity metadata: entities." + attr.getName());
				}
			}

			int i = 1;
			for (Entity entity : entitiesRepo)
			{
				i++;
				String entityName = entity.getString("name");

				// required
				if (entityName == null) throw new IllegalArgumentException("entity.name is missing on line " + i);

				if (!entities.containsKey(entityName)) entities.put(entityName, new DefaultEntityMetaData(entityName));

				EditableEntityMetaData md = entities.get(entityName);
				md.setLabel(entity.getString(org.molgenis.data.meta.EntityMetaDataMetaData.LABEL));
				md.setDescription(entity.getString(org.molgenis.data.meta.EntityMetaDataMetaData.DESCRIPTION));
				if (entity.getBoolean(ABSTRACT) != null) md.setAbstract(entity.getBoolean(ABSTRACT));

				String extendsEntityName = entity.getString(EXTENDS);
				if (extendsEntityName != null)
				{
					EntityMetaData extendsEntityMeta = entities.get(extendsEntityName);
					if (extendsEntityMeta == null)
					{
						throw new MolgenisDataException("Missing super entity " + extendsEntityName + " for entity "
								+ entityName + " on line " + i);
					}
					md.setExtends(extendsEntityMeta);
				}

				String packageName = entity.getString(PACKAGE);
				if (packageName != null)
				{
					md.setPackage(new PackageImpl(packageName, null));
				}
			}
		}
	}

	Map<String, PackageImpl> parsePackagesSheet(RepositoryCollection source)
	{
		Map<String, PackageImpl> packages = Maps.newHashMap();
		Repository repo = source.getRepositoryByEntityName(EmxMetaDataParser.PACKAGES);
		if (repo == null) return packages;

		// Collect packages
		int i = 1;
		for (Entity pack : repo)
		{
			i++;
			String simpleName = pack.getString(NAME);

			// required
			if (simpleName == null) throw new IllegalArgumentException("package.name is missing on line " + i);

			PackageImpl parentPackage = null;
			String description = pack.getString(org.molgenis.data.meta.PackageMetaData.DESCRIPTION);
			String parent = pack.getString(org.molgenis.data.meta.PackageMetaData.PARENT);
			if (parent != null)
			{
				parentPackage = new PackageImpl(parent, null);
			}
			Iterable<String> tagIdentifiers = pack.getList(org.molgenis.data.meta.PackageMetaData.TAGS);
			PackageImpl p = new PackageImpl(simpleName, description, parentPackage);

			if (tagIdentifiers != null && !Iterables.isEmpty(tagIdentifiers))
			{
				Repository tagsRepo = source.getRepositoryByEntityName(TAGS);
				if (tagsRepo == null) throw new IllegalArgumentException("Missing 'tags'");

				for (String tagIdentifier : tagIdentifiers)
				{
					Entity tagEntity = getTagFromSource(tagIdentifier, tagsRepo);
					if (tagEntity == null)
					{
						throw new IllegalArgumentException("Unknown tag '" + tagIdentifier + "'");
					}
					p.addTag(TagImpl.<Package> asTag(p, tagEntity));
				}

			}

			packages.put(simpleName, p);
		}

		// Resolve parent packages
		for (PackageImpl p : packages.values())
		{
			if (p.getParent() != null)
			{
				PackageImpl parent = packages.get(p.getParent().getSimpleName());
				if (parent == null) throw new IllegalArgumentException("Unknown parent package '"
						+ p.getParent().getSimpleName() + "' of package '" + p.getSimpleName() + "'");

				p.setParent(parent);
			}
		}

		return packages;
	}

	private Entity getTagFromSource(String identifier, Repository source)
	{
		for (Entity tag : source)
		{
			String id = tag.getString(TagMetaData.IDENTIFIER);
			if ((id != null) && id.equals(identifier))
			{
				return tag;
			}
		}

		return null;
	}

	/**
	 * Load entity packages (optional)
	 * 
	 * @param emxImportService
	 *            TODO
	 * @param source
	 *            the map to add package meta data
	 * @param entities
	 *            TODO
	 */
	private void parsePackagesSheetToEntityMap(RepositoryCollection source, Map<String, EditableEntityMetaData> entities)
	{
		Map<String, PackageImpl> packages = parsePackagesSheet(source);

		// Resolve entity packages
		for (EditableEntityMetaData emd : entities.values())
		{
			if (emd.getPackage() != null)
			{
				Package p = packages.get(emd.getPackage().getSimpleName());
				if (p == null) throw new IllegalArgumentException("Unknown package '"
						+ emd.getPackage().getSimpleName() + "' of entity '" + emd.getSimpleName() + "'");

				emd.setPackage(p);
			}
		}

	}

	/**
	 * re-iterate to map the mrefs/xref refEntity (or give error if not found) TODO consider also those in existing db
	 * 
	 * @param source
	 *            the map to add entities meta data
	 * @param entities
	 *            TODO
	 */
	private void reiterateToMapRefEntity(RepositoryCollection source, Map<String, EditableEntityMetaData> entities)
	{
		int i = 1;
		for (Entity attribute : source.getRepositoryByEntityName(EmxMetaDataParser.ATTRIBUTES))
		{
			final String refEntityName = (String) attribute.get(REF_ENTITY);
			final String entityName = attribute.getString(ENTITY);
			final String attributeName = attribute.getString(NAME);
			i++;
			if (refEntityName != null)
			{
				EntityMetaData defaultEntityMetaData = entities.get(entityName);
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

	List<EntityMetaData> combineMetaDataToList(DataService dataService, final RepositoryCollection source)
	{
		List<EntityMetaData> metadataList = Lists.newArrayList();

		if (source.getRepositoryByEntityName(EmxMetaDataParser.ATTRIBUTES) != null)
		{
			Map<String, EntityMetaData> metadata = getAllEntityMetaDataFromSource(source);
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
		return metadataList;
	}

	private Map<String, EntityMetaData> combineMetaDataToMap(DataService dataService, RepositoryCollection source)
	{
		// compare the data sheets against metadata in store or imported file
		if (source.getRepositoryByEntityName(EmxMetaDataParser.ATTRIBUTES) != null)
		{
			return getAllEntityMetaDataFromSource(source);
		}
		else
		{
			Map<String, EntityMetaData> metaDataMap = new HashMap<String, EntityMetaData>();
			for (String name : source.getEntityNames())
			{
				metaDataMap.put(name, dataService.getRepositoryByEntityName(name).getEntityMetaData());
			}
			return metaDataMap;
		}
	}

	public EntitiesValidationReport validateInput(DataService dataService, RepositoryCollection source)
	{
		EntitiesValidationReportImpl report = new EntitiesValidationReportImpl();

		Map<String, EntityMetaData> metaDataMap = combineMetaDataToMap(dataService, source);

		// "-" is not allowed because of bug: https://github.com/molgenis/molgenis/issues/2055
		// FIXME remove this line once this bug is fixed
		for (String entityName : metaDataMap.keySet())
		{
			if (entityName.contains("-"))
			{
				throw new IllegalArgumentException("'-' is not allowed in an entity name (" + entityName + ")");
			}
		}

		for (String sheet : source.getEntityNames())
		{
			if (!ENTITIES.equals(sheet) && !ATTRIBUTES.equals(sheet) && !PACKAGES.equals(sheet) && !TAGS.equals(sheet))
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

}
