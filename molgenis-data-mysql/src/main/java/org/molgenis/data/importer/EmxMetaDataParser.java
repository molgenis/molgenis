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
import static org.molgenis.data.meta.AttributeMetaDataMetaData.PART_OF_ATTRIBUTE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MAX;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.RANGE_MIN;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.READ_ONLY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.REF_ENTITY;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.UNIQUE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.meta.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.meta.EntityMetaDataMetaData.PACKAGE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
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
import org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.CompoundField;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.util.DependencyResolver;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.util.StringUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Parser for the EMX metadata. This class is stateless.
 */
public class EmxMetaDataParser implements MetaDataParser
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
			LOOKUP_ATTRIBUTE.toLowerCase(), NAME, NILLABLE.toLowerCase(), PART_OF_ATTRIBUTE.toLowerCase(),
			RANGE_MAX.toLowerCase(), RANGE_MIN.toLowerCase(), READ_ONLY.toLowerCase(), REF_ENTITY.toLowerCase(),
			VISIBLE.toLowerCase(), UNIQUE.toLowerCase(),
			org.molgenis.data.meta.AttributeMetaDataMetaData.TAGS.toLowerCase());

	private final DataService dataService;
	private final MetaDataService metaDataService;

	public EmxMetaDataParser(DataService dataService, MetaDataService metaDataService)
	{
		this.dataService = dataService;
		this.metaDataService = metaDataService;
	}

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

		Repository attributesRepo = source.getRepositoryByEntityName(ATTRIBUTES);
		for (AttributeMetaData attr : attributesRepo.getEntityMetaData().getAtomicAttributes())
		{
			if (!SUPPORTED_ATTRIBUTE_ATTRIBUTES.contains(attr.getName().toLowerCase()))
			{
				throw new IllegalArgumentException("Unsupported attribute metadata: attributes. " + attr.getName());
			}
		}

		Map<String, Map<String, DefaultAttributeMetaData>> attributesMap = new LinkedHashMap<String, Map<String, DefaultAttributeMetaData>>();

		// 1st pass: create attribute stubs
		int i = 1;// Header
		for (Entity attributeEntity : attributesRepo)
		{
			i++;

			String attributeName = attributeEntity.getString(NAME);
			if (attributeName == null) throw new IllegalArgumentException("attributes.name is missing on line " + i);

			String entityName = attributeEntity.getString(ENTITY);
			if (entityName == null) throw new IllegalArgumentException(
					"attributes.entity is missing for attribute named: " + attributeName + " on line " + i);

			// create attribute
			DefaultAttributeMetaData attribute = new DefaultAttributeMetaData(attributeName);

			Map<String, DefaultAttributeMetaData> entitiesMap = attributesMap.get(entityName);
			if (entitiesMap == null)
			{
				entitiesMap = new LinkedHashMap<String, DefaultAttributeMetaData>();
				attributesMap.put(entityName, entitiesMap);
			}
			entitiesMap.put(attributeName, attribute);
		}

		// 2nd pass: set all properties on attribute stubs except for attribute relations
		i = 1;// Header
		for (Entity attributeEntity : attributesRepo)
		{
			i++;

			String entityName = attributeEntity.getString(ENTITY);
			Map<String, DefaultAttributeMetaData> entityMap = attributesMap.get(entityName);

			String attributeName = attributeEntity.getString(NAME);
			DefaultAttributeMetaData attribute = entityMap.get(attributeName);

			String attributeDataType = attributeEntity.getString(DATA_TYPE);
			String refEntityName = attributeEntity.getString(REF_ENTITY);

			if (attributeDataType != null)
			{
				FieldType t = MolgenisFieldTypes.getType(attributeDataType);
				if (t == null) throw new IllegalArgumentException("attributes.dataType error on line " + i + ": "
						+ attributeDataType + " unknown data type");
				attribute.setDataType(t);
			}
			else
			{
				attribute.setDataType(MolgenisFieldTypes.STRING);
			}

			Boolean attributeNillable = attributeEntity.getBoolean(NILLABLE);
			Boolean attributeIdAttribute = attributeEntity.getBoolean(ID_ATTRIBUTE);
			Boolean attributeVisible = attributeEntity.getBoolean(VISIBLE);
			Boolean attributeAggregateable = attributeEntity.getBoolean(AGGREGATEABLE);
			Boolean lookupAttribute = attributeEntity.getBoolean(LOOKUP_ATTRIBUTE);
			Boolean labelAttribute = attributeEntity.getBoolean(LABEL_ATTRIBUTE);
			Boolean readOnly = attributeEntity.getBoolean(READ_ONLY);
			Boolean unique = attributeEntity.getBoolean(UNIQUE);

			if (attributeNillable != null) attribute.setNillable(attributeNillable);
			if (attributeIdAttribute != null) attribute.setIdAttribute(attributeIdAttribute);
			if (attributeVisible != null) attribute.setVisible(attributeVisible);
			if (attributeAggregateable != null) attribute.setAggregateable(attributeAggregateable);
			if (refEntityName != null) attribute.setRefEntity(entities.get(refEntityName));
			if (readOnly != null) attribute.setReadOnly(readOnly);
			if (unique != null) attribute.setUnique(unique);

			if (lookupAttribute != null)
			{
				if (lookupAttribute
						&& ((attribute.getDataType() instanceof XrefField) || (attribute.getDataType() instanceof MrefField)))
				{
					throw new IllegalArgumentException("attributes.lookupAttribute error on line " + i + " ("
							+ entityName + "." + attributeName + "): lookupAttribute cannot be of type "
							+ attribute.getDataType());
				}

				attribute.setLookupAttribute(lookupAttribute);
			}

			if (labelAttribute != null)
			{
				if (labelAttribute
						&& ((attribute.getDataType() instanceof XrefField) || (attribute.getDataType() instanceof MrefField)))
				{
					throw new IllegalArgumentException("attributes.labelAttribute error on line " + i + " ("
							+ entityName + "." + attributeName + "): labelAttribute cannot be of type "
							+ attribute.getDataType());
				}

				attribute.setLabelAttribute(labelAttribute);
			}

			attribute.setLabel(attributeEntity.getString(LABEL));
			attribute.setDescription(attributeEntity.getString(DESCRIPTION));

			if (attribute.getDataType() instanceof EnumField)
			{
				List<String> enumOptions = attributeEntity.getList(ENUM_OPTIONS);
				if ((enumOptions == null) || enumOptions.isEmpty())
				{
					throw new IllegalArgumentException("Missing enum options for attribute [" + attribute.getName()
							+ "] of entity [" + entityName + "]");
				}
				attribute.setEnumOptions(enumOptions);
			}

			if (((attribute.getDataType() instanceof XrefField) || (attribute.getDataType() instanceof MrefField))
					&& StringUtils.isEmpty(refEntityName))
			{
				throw new IllegalArgumentException("Missing refEntity on line " + i + " (" + entityName + "."
						+ attributeName + ")");
			}

			if (((attribute.getDataType() instanceof XrefField) || (attribute.getDataType() instanceof MrefField))
					&& attribute.isNillable() && attribute.isAggregateable())
			{
				throw new IllegalArgumentException("attributes.aggregatable error on line " + i + " (" + entityName
						+ "." + attributeName + "): aggregatable nillable attribute cannot be of type "
						+ attribute.getDataType());
			}

			Long rangeMin;
			Long rangeMax;
			try
			{
				rangeMin = attributeEntity.getLong(RANGE_MIN);
			}
			catch (ConversionFailedException e)
			{
				throw new MolgenisDataException("Invalid range rangeMin [" + attributeEntity.getString(RANGE_MIN)
						+ "] value for attribute [" + attributeName + "] of entity [" + entityName
						+ "], should be a long");
			}

			try
			{
				rangeMax = attributeEntity.getLong(RANGE_MAX);
			}
			catch (ConversionFailedException e)
			{
				throw new MolgenisDataException("Invalid rangeMax value [" + attributeEntity.getString(RANGE_MAX)
						+ "] for attribute [" + attributeName + "] of entity [" + entityName + "], should be a long");
			}

			if ((rangeMin != null) || (rangeMax != null))
			{
				if (!(attribute.getDataType() instanceof IntField) && !(attribute.getDataType() instanceof LongField))
				{
					throw new MolgenisDataException("Range not supported for [" + attribute.getDataType().getEnumType()
							+ "] fields only int and long are supported. (attribute [" + attribute.getName()
							+ "] of entity [" + entityName + "])");
				}

				attribute.setRange(new Range(rangeMin, rangeMax));
			}
		}

		// 3rd pass: validate and create attribute relationships
		Map<String, Set<String>> rootAttributes = new LinkedHashMap<String, Set<String>>();
		i = 1;// Header
		for (Entity attributeEntity : attributesRepo)
		{
			i++;

			String entityName = attributeEntity.getString(ENTITY);
			Map<String, DefaultAttributeMetaData> entityMap = attributesMap.get(entityName);

			String attributeName = attributeEntity.getString(NAME);
			DefaultAttributeMetaData attribute = entityMap.get(attributeName);

			// register attribute parent-children relations for compound attributes
			String partOfAttribute = attributeEntity.getString(PART_OF_ATTRIBUTE);
			if (partOfAttribute != null && !partOfAttribute.isEmpty())
			{
				DefaultAttributeMetaData compoundAttribute = entityMap.get(partOfAttribute);

				if (compoundAttribute.getDataType().getEnumType() != FieldTypeEnum.COMPOUND)
				{
					throw new IllegalArgumentException("partOfAttribute [" + partOfAttribute + "] of attribute ["
							+ attributeName + "] of entity [" + entityName + "] must refer to a attribute of type ["
							+ FieldTypeEnum.COMPOUND + "] on line " + i);
				}

				compoundAttribute.addAttributePart(attribute);
			}
			else
			{
				Set<String> entityRootAttributes = rootAttributes.get(entityName);
				if (entityRootAttributes == null)
				{
					entityRootAttributes = new LinkedHashSet<String>();
					rootAttributes.put(entityName, entityRootAttributes);
				}
				entityRootAttributes.add(attributeName);
			}
		}

		// store attributes with entities
		for (Map.Entry<String, Map<String, DefaultAttributeMetaData>> entry : attributesMap.entrySet())
		{
			String entityName = entry.getKey();
			Map<String, DefaultAttributeMetaData> attributes = entry.getValue();

			// create entity if not yet defined
			EditableEntityMetaData editableEntityMetaData = entities.get(entityName);
			if (editableEntityMetaData == null)
			{
				editableEntityMetaData = new DefaultEntityMetaData(entityName);
				entities.put(entityName, editableEntityMetaData);
			}

			// add root attributes to entity
			Set<String> entityAttributeNames = rootAttributes.get(entityName);
			if (entityAttributeNames != null)
			{
				for (DefaultAttributeMetaData attribute : attributes.values())
				{
					if (entityAttributeNames.contains(attribute.getName()))
					{
						editableEntityMetaData.addAttributeMetaData(attribute);
					}
				}
			}
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

	private Map<String, ? extends org.molgenis.data.Package> parsePackagesSheet(RepositoryCollection source)
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
		Map<String, ? extends Package> packages = parsePackagesSheet(source);

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

	@Override
	public ParsedMetaData parse(final RepositoryCollection source)
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

		return new ParsedMetaData(resolveEntityDependencies(metadataList), parsePackagesSheet(source));
	}

	private List<EntityMetaData> resolveEntityDependencies(List<EntityMetaData> metaDataList)
	{
		Set<EntityMetaData> allMetaData = Sets.newLinkedHashSet(metaDataList);
		Iterable<EntityMetaData> existingMetaData = metaDataService.getEntityMetaDatas();
		Iterables.addAll(allMetaData, existingMetaData);

		// Use all metadata for dependency resolving
		List<EntityMetaData> resolved = DependencyResolver.resolve(allMetaData);

		// Only import source
		resolved.retainAll(metaDataList);
		return resolved;
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

	@Override
	public EntitiesValidationReport validate(RepositoryCollection source)
	{
		MyEntitiesValidationReport report = new MyEntitiesValidationReport();

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
				// check if sheet is known
				report = report.addEntity(sheet, metaDataMap.containsKey(sheet));

				// check the fields
				Repository s = source.getRepositoryByEntityName(sheet);
				EntityMetaData target = metaDataMap.get(sheet);

				if (target != null)
				{
					for (AttributeMetaData att : s.getEntityMetaData().getAttributes())
					{
						boolean known = target.getAttribute(att.getName()) != null;
						report = report.addAttribute(att.getName(),
								known ? AttributeState.IMPORTABLE : AttributeState.UNKNOWN);
					}
					for (AttributeMetaData att : target.getAttributes())
					{
						if (!(att.getDataType() instanceof CompoundField))
						{
							if (!att.isAuto() && !report.getFieldsImportable().get(sheet).contains(att.getName()))
							{
								boolean required = !att.isNillable();
								report = report.addAttribute(att.getName(),
										required ? AttributeState.REQUIRED : AttributeState.AVAILABLE);
							}
						}
					}
				}
			}
		}

		return report;
	}

}
