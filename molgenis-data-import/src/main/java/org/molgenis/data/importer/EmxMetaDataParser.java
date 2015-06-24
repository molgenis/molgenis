package org.molgenis.data.importer;

import static org.molgenis.data.meta.AttributeMetaDataMetaData.AGGREGATEABLE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DATA_TYPE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ENUM_OPTIONS;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.EXPRESSION;
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
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VALIDATION_EXPRESSION;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.VISIBLE;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.meta.EntityMetaDataMetaData.BACKEND;
import static org.molgenis.data.meta.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.meta.EntityMetaDataMetaData.PACKAGE;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaValidationUtils;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.meta.PackageMetaData;
import org.molgenis.data.meta.TagMetaData;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.fieldtypes.CompoundField;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.IntField;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.util.DependencyResolver;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Parser for the EMX metadata. This class is stateless, but it passes state between methods using
 * {@link IntermediateParseResults}.
 */
public class EmxMetaDataParser implements MetaDataParser
{

	public static final String PACKAGES = PackageMetaData.ENTITY_NAME;
	public static final String TAGS = TagMetaData.ENTITY_NAME;
	public static final String ATTRIBUTES = AttributeMetaDataMetaData.ENTITY_NAME;

	public static final String ENTITY = "entity";
	public static final String PART_OF_ATTRIBUTE = "partOfAttribute";

	// Sheet names
	public static final String ENTITIES = EntityMetaDataMetaData.ENTITY_NAME;
	static final List<String> SUPPORTED_ENTITY_ATTRIBUTES = Arrays.asList(
			org.molgenis.data.meta.EntityMetaDataMetaData.LABEL.toLowerCase(),
			org.molgenis.data.meta.EntityMetaDataMetaData.DESCRIPTION.toLowerCase(), "name", ABSTRACT.toLowerCase(),
			EXTENDS.toLowerCase(), "package", EntityMetaDataMetaData.TAGS, BACKEND);
	static final List<String> SUPPORTED_ATTRIBUTE_ATTRIBUTES = Arrays.asList(AGGREGATEABLE.toLowerCase(),
			DATA_TYPE.toLowerCase(), DESCRIPTION.toLowerCase(), ENTITY.toLowerCase(), ENUM_OPTIONS.toLowerCase(),
			ID_ATTRIBUTE.toLowerCase(), LABEL.toLowerCase(), LABEL_ATTRIBUTE.toLowerCase(),
			LOOKUP_ATTRIBUTE.toLowerCase(), NAME, NILLABLE.toLowerCase(), PART_OF_ATTRIBUTE.toLowerCase(),
			RANGE_MAX.toLowerCase(), RANGE_MIN.toLowerCase(), READ_ONLY.toLowerCase(), REF_ENTITY.toLowerCase(),
			VISIBLE.toLowerCase(), UNIQUE.toLowerCase(), TAGS.toLowerCase(), EXPRESSION.toLowerCase(),
			VALIDATION_EXPRESSION.toLowerCase());
	static final String AUTO = "auto";

	private final DataService dataService;

	public EmxMetaDataParser(DataService dataService)
	{
		this.dataService = dataService;
	}

	/**
	 * Parses metadata from a collection of repositories.
	 * 
	 * @param source
	 *            the {@link RepositoryCollection} containing the metadata to parse
	 * @return {@link IntermediateParseResults} containing the parsed metadata
	 */
	private IntermediateParseResults getEntityMetaDataFromSource(RepositoryCollection source)
	{
		// TODO: this task is actually a 'merge' instead of 'import'
		// so we need to consider both new metadata and existing ...
		IntermediateParseResults intermediateResults = parseTagsSheet(source.getRepository(TAGS));

		parsePackagesSheet(source.getRepository(PACKAGES), intermediateResults);
		parsePackageTags(source.getRepository(PACKAGES), intermediateResults);
		parseAttributesSheet(source.getRepository(ATTRIBUTES), intermediateResults);
		parseEntitiesSheet(source.getRepository(ENTITIES), intermediateResults);
		reiterateToMapRefEntity(source.getRepository(ATTRIBUTES), intermediateResults);

		return intermediateResults;
	}

	/**
	 * Parses all tags defined in the tags repository.
	 * 
	 * @param source
	 *            the {@link Repository} that contains the tags entity
	 * @return Map mapping tag Identifier to tag {@link Entity}, will be empty if no tags repository was found
	 */
	private IntermediateParseResults parseTagsSheet(Repository tagRepository)
	{
		IntermediateParseResults result = new IntermediateParseResults();
		if (tagRepository != null)
		{
			for (Entity tag : tagRepository)
			{
				String id = tag.getString(TagMetaData.IDENTIFIER);
				if (id != null)
				{
					result.addTagEntity(id, tag);
				}
			}
		}
		return result;
	}

	/**
	 * Load all attributes from the source repository and add it to the {@link IntermediateParseResults}.
	 * 
	 * @param source
	 *            Repository for the attributes
	 * @param intermediateResults
	 *            {@link IntermediateParseResults} with the tags already parsed
	 */
	private void parseAttributesSheet(Repository attributesRepo, IntermediateParseResults intermediateResults)
	{
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
			String attributeIdAttribute = attributeEntity.getString(ID_ATTRIBUTE);
			Boolean attributeAggregateable = attributeEntity.getBoolean(AGGREGATEABLE);
			Boolean lookupAttribute = attributeEntity.getBoolean(LOOKUP_ATTRIBUTE);
			Boolean labelAttribute = attributeEntity.getBoolean(LABEL_ATTRIBUTE);
			Boolean readOnly = attributeEntity.getBoolean(READ_ONLY);
			Boolean unique = attributeEntity.getBoolean(UNIQUE);
			String expression = attributeEntity.getString(EXPRESSION);
			List<String> tagIds = attributeEntity.getList(TAGS);
			String validationExpression = attributeEntity.getString(VALIDATION_EXPRESSION);

			if (attributeNillable != null) attribute.setNillable(attributeNillable);

			boolean isIdAttr = attributeIdAttribute != null
					&& (attributeIdAttribute.equalsIgnoreCase("true") || attributeIdAttribute.equalsIgnoreCase(AUTO));
			attribute.setIdAttribute(isIdAttr);

			if (isIdAttr)
			{
				if ((attributeNillable != null) && attributeNillable)
				{
					throw new IllegalArgumentException("Attributes error on line " + i
							+ ". Id attributes cannot be nillable");
				}
				attribute.setNillable(false);
			}

			String attributeVisible = attributeEntity.getString(VISIBLE);
			if (attributeVisible != null)
			{
				if (attributeVisible.equalsIgnoreCase("true") || attributeVisible.equalsIgnoreCase("false"))
				{
					attribute.setVisible(Boolean.parseBoolean(attributeVisible));
				}
				else
				{
					attribute.setVisibleExpression(attributeVisible);
				}
			}

			if (attributeAggregateable != null) attribute.setAggregateable(attributeAggregateable);
			// cannot update ref entities yet, will do so later on
			if (readOnly != null) attribute.setReadOnly(readOnly);
			if (unique != null) attribute.setUnique(unique);
			if (expression != null) attribute.setExpression(expression);
			if (validationExpression != null) attribute.setValidationExpression(validationExpression);
			attribute.setAuto(attributeIdAttribute != null && attributeIdAttribute.equalsIgnoreCase(AUTO));

			if ((attributeIdAttribute != null) && !attributeIdAttribute.equalsIgnoreCase("true")
					&& !attributeIdAttribute.equalsIgnoreCase("false") && !attributeIdAttribute.equalsIgnoreCase(AUTO))
			{
				throw new IllegalArgumentException("Attributes error on line " + i
						+ ". Illegal idAttribute value. Allowed values are 'TRUE', 'FALSE' or 'AUTO'");
			}

			if (attribute.isAuto() && !(attribute.getDataType() instanceof StringField))
			{
				throw new IllegalArgumentException("Attributes error on line " + i
						+ ". Auto attributes can only be of data type 'string'");
			}

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

			if (tagIds != null)
			{
				for (String tagId : tagIds)
				{
					Entity tagEntity = intermediateResults.getTagEntity(tagId);
					if (tagEntity == null)
					{
						throw new MolgenisDataException("Unknown tag: " + tagId + " for attribute ["
								+ attribute.getName() + "] of entity [" + entityName + "]). Please specify on the "
								+ TAGS + " sheet.");
					}
					intermediateResults.addAttributeTag(entityName,
							TagImpl.<AttributeMetaData> asTag(attribute, tagEntity));
				}
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

				if (compoundAttribute == null)
				{
					throw new IllegalArgumentException("partOfAttribute [" + partOfAttribute + "] of attribute ["
							+ attributeName + "] of entity [" + entityName
							+ "] must refer to an existing compound attribute on line " + i);
				}

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

			List<AttributeMetaData> editableEntityMetaData = new ArrayList<AttributeMetaData>();
			// add root attributes to entity
			Set<String> entityAttributeNames = rootAttributes.get(entityName);
			if (entityAttributeNames != null)
			{
				for (DefaultAttributeMetaData attribute : attributes.values())
				{
					if (entityAttributeNames.contains(attribute.getName()))
					{
						editableEntityMetaData.add(attribute);
					}
				}
			}

			intermediateResults.addAttributes(entityName, editableEntityMetaData);
		}
	}

	/**
	 * Load all entities (optional)
	 * 
	 * @param entitiesRepo
	 *            the Repository for the entities
	 * @param intermediateResults
	 *            {@link IntermediateParseResults} containing the attributes already parsed
	 */
	private void parseEntitiesSheet(Repository entitiesRepo, IntermediateParseResults intermediateResults)
	{
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

				String packageName = entity.getString(PACKAGE);
				if (packageName != null && !Package.DEFAULT_PACKAGE_NAME.equals(packageName))
				{
					entityName = packageName + Package.PACKAGE_SEPARATOR + entityName;
				}

				EditableEntityMetaData md = intermediateResults.getEntityMetaData(entityName);
				if (md == null)
				{
					md = intermediateResults.addEntityMetaData(entityName);
				}

				String backend = entity.getString(BACKEND);
				if (backend != null)
				{
					if (dataService.getMeta().getBackend(backend) == null)
					{
						throw new MolgenisDataException("Unknown backend '" + backend + "'");
					}
					md.setBackend(backend);
				}

				if (packageName != null)
				{
					PackageImpl p = intermediateResults.getPackage(packageName);
					if (p == null)
					{
						throw new MolgenisDataException("Unknown package: '" + packageName + "' for entity '"
								+ entity.getString("name") + "'. Please specify the package on the " + PACKAGES
								+ " sheet and use the fully qualified package and entity names.");
					}
					md.setPackage(p);
				}

				md.setLabel(entity.getString(org.molgenis.data.meta.EntityMetaDataMetaData.LABEL));
				md.setDescription(entity.getString(org.molgenis.data.meta.EntityMetaDataMetaData.DESCRIPTION));
				if (entity.getBoolean(ABSTRACT) != null) md.setAbstract(entity.getBoolean(ABSTRACT));
				List<String> tagIds = entity.getList(TAGS);

				String extendsEntityName = entity.getString(EXTENDS);
				if (extendsEntityName != null)
				{
					EntityMetaData extendsEntityMeta = null;
					if (intermediateResults.knowsEntity(extendsEntityName))
					{
						extendsEntityMeta = intermediateResults.getEntityMetaData(extendsEntityName);

					}
					else
					{
						extendsEntityMeta = dataService.getMeta().getEntityMetaData(extendsEntityName);

					}

					if (extendsEntityMeta == null)
					{
						throw new MolgenisDataException("Missing super entity " + extendsEntityName + " for entity "
								+ entityName + " on line " + i);
					}

					md.setExtends(extendsEntityMeta);
				}

				if (tagIds != null)
				{
					for (String tagId : tagIds)
					{
						Entity tagEntity = intermediateResults.getTagEntity(tagId);
						if (tagEntity == null)
						{
							throw new MolgenisDataException("Unknown tag: " + tagId + " for entity [" + entityName
									+ "]). Please specify on the " + TAGS + " sheet.");
						}
						intermediateResults.addEntityTag(TagImpl.<EntityMetaData> asTag(md, tagEntity));
					}
				}
			}
		}
	}

	/**
	 * Parses the packages sheet
	 * 
	 * @param repo
	 *            {@link Repository} for the packages
	 * @param intermediateResults
	 *            {@link IntermediateParseResults} containing the parsed tag entities
	 */
	private void parsePackagesSheet(Repository repo, IntermediateParseResults intermediateResults)
	{
		if (repo == null) return;

		// Collect packages
		int i = 1;
		for (Entity pack : resolvePackages(repo))
		{
			i++;
			String name = pack.getString(NAME);

			// required
			if (name == null) throw new IllegalArgumentException("package.name is missing on line " + i);

			String simpleName = name;
			String description = pack.getString(org.molgenis.data.meta.PackageMetaData.DESCRIPTION);
			String parentName = pack.getString(org.molgenis.data.meta.PackageMetaData.PARENT);
			PackageImpl parent = null;
			if (parentName != null)
			{
				if (!name.toLowerCase().startsWith(parentName.toLowerCase())) throw new MolgenisDataException(
						"Inconsistent package structure. Package: '" + name + "', parent: '" + parentName + "'");
				simpleName = name.substring(parentName.length() + 1);// subpackage_package
				parent = intermediateResults.getPackage(parentName);
			}

			intermediateResults.addPackage(name, new PackageImpl(simpleName, description, parent));
		}
	}

	private void parsePackageTags(Repository repo, IntermediateParseResults intermediateResults)
	{
		if (repo != null)
		{
			for (Entity pack : repo)
			{
				Iterable<String> tagIdentifiers = pack.getList(org.molgenis.data.meta.PackageMetaData.TAGS);
				if (tagIdentifiers != null)
				{
					String name = pack.getString(NAME);
					PackageImpl p = intermediateResults.getPackage(name);
					if (p == null) throw new IllegalArgumentException("Unknown package '" + name + "'");

					for (String tagIdentifier : tagIdentifiers)
					{
						Entity tagEntity = intermediateResults.getTagEntity(tagIdentifier);
						if (tagEntity == null)
						{
							throw new IllegalArgumentException("Unknown tag '" + tagIdentifier + "'");
						}
						p.addTag(TagImpl.<Package> asTag(p, tagEntity));
					}
				}
			}
		}
	}

	private List<Entity> resolvePackages(Repository packageRepo)
	{
		List<Entity> resolved = new ArrayList<>();
		if ((packageRepo == null) || Iterables.isEmpty(packageRepo)) return resolved;

		List<Entity> unresolved = new ArrayList<>();
		Map<String, Entity> resolvedByName = new HashMap<>();

		for (Entity pack : packageRepo)
		{
			String name = pack.getString(NAME);
			String parentName = pack.getString(org.molgenis.data.meta.PackageMetaData.PARENT);

			if (parentName == null)
			{
				resolved.add(pack);
				resolvedByName.put(name, pack);
			}
			else
			{
				unresolved.add(pack);
			}
		}

		if (resolved.isEmpty()) throw new IllegalArgumentException(
				"Missing root package. There must be at least one package without a parent.");

		List<Entity> ready = new ArrayList<>();
		while (!unresolved.isEmpty())
		{
			for (Entity pack : unresolved)
			{
				Entity parent = resolvedByName.get(pack.getString(org.molgenis.data.meta.PackageMetaData.PARENT));
				if (parent != null)
				{
					String name = pack.getString(NAME);
					ready.add(pack);
					resolvedByName.put(name, pack);
				}
			}

			if (ready.isEmpty()) throw new IllegalArgumentException(
					"Could not resolve packages. Is there a circular reference?");
			resolved.addAll(ready);
			unresolved.removeAll(ready);
			ready.clear();
		}

		return resolved;
	}

	/**
	 * re-iterate to map the mrefs/xref refEntity (or give error if not found) TODO consider also those in existing db
	 * 
	 * @param attributeRepo
	 *            the attributes {@link Repository}
	 * @param intermediateResults
	 *            {@link ParsedMetaData} to add the ref entities to
	 */
	private void reiterateToMapRefEntity(Repository attributeRepo, IntermediateParseResults intermediateResults)
	{
		int i = 1;
		for (Entity attribute : attributeRepo)
		{
			final String refEntityName = (String) attribute.get(REF_ENTITY);
			final String entityName = attribute.getString(ENTITY);
			final String attributeName = attribute.getString(NAME);
			i++;
			if (refEntityName != null)
			{
				EntityMetaData defaultEntityMetaData = intermediateResults.getEntityMetaData(entityName);
				DefaultAttributeMetaData defaultAttributeMetaData = (DefaultAttributeMetaData) defaultEntityMetaData
						.getAttribute(attributeName);

				if (intermediateResults.knowsEntity(refEntityName))
				{
					defaultAttributeMetaData.setRefEntity(intermediateResults.getEntityMetaData(refEntityName));
				}
				else
				{
					EntityMetaData refEntityMeta;
					try
					{
						refEntityMeta = dataService.getEntityMetaData(refEntityName);
					}
					catch (UnknownEntityException e)
					{
						throw new IllegalArgumentException("attributes.refEntity error on line " + i + ": "
								+ refEntityName + " unknown");
					}

					// allow computed xref attributes to refer to pre-existing entities
					defaultAttributeMetaData.setRefEntity(refEntityMeta);
				}

			}
		}
	}

	@Override
	public ParsedMetaData parse(final RepositoryCollection source, String defaultPackage)
	{
		if (source.getRepository(EmxMetaDataParser.ATTRIBUTES) != null)
		{
			IntermediateParseResults intermediateResults = getEntityMetaDataFromSource(source);
			List<EditableEntityMetaData> entities;
			if ((defaultPackage == null) || Package.DEFAULT_PACKAGE_NAME.equalsIgnoreCase(defaultPackage))
			{
				entities = intermediateResults.getEntities();
			}
			else
			{
				entities = putEntitiesInDefaultPackage(intermediateResults, defaultPackage);
			}

			return new ParsedMetaData(resolveEntityDependencies(entities), intermediateResults.getPackages(),
					intermediateResults.getAttributeTags(), intermediateResults.getEntityTags());
		}
		else
		{
			List<EntityMetaData> metadataList = new ArrayList<EntityMetaData>();
			for (String name : source.getEntityNames())
			{
				metadataList.add(dataService.getRepository(name).getEntityMetaData());
			}
			IntermediateParseResults intermediateResults = parseTagsSheet(source.getRepository(TAGS));
			parsePackagesSheet(source.getRepository(PACKAGES), intermediateResults);
			parsePackageTags(source.getRepository(PACKAGES), intermediateResults);
			return new ParsedMetaData(resolveEntityDependencies(metadataList), intermediateResults.getPackages(),
					intermediateResults.getAttributeTags(), intermediateResults.getEntityTags());
		}

	}

	/**
	 * Put the entities that are not in a package in the selected package
	 * 
	 * @param metaDataList
	 * @param defaultPackageName
	 * @return
	 */
	private List<EditableEntityMetaData> putEntitiesInDefaultPackage(IntermediateParseResults intermediateResults,
			String defaultPackageName)
	{
		PackageImpl p = intermediateResults.getPackage(defaultPackageName);
		if (p == null) throw new IllegalArgumentException("Unknown package '" + defaultPackageName + "'");

		List<EditableEntityMetaData> entities = new ArrayList<>();
		for (EditableEntityMetaData entityMetaData : intermediateResults.getEntities())
		{
			if (entityMetaData.getPackage() == null)
			{
				entityMetaData.setPackage(p);
			}
			entities.add(entityMetaData);
		}

		return entities;
	}

	/**
	 * Puts EntityMetaData in the right import order.
	 * 
	 * @param metaDataList
	 *            {@link EntityMetaData} to put in the right order
	 * @return List of {@link EntityMetaData}, in the import order
	 */
	private List<EntityMetaData> resolveEntityDependencies(List<? extends EntityMetaData> metaDataList)
	{
		Set<EntityMetaData> allMetaData = Sets.newLinkedHashSet(metaDataList);
		Iterable<EntityMetaData> existingMetaData = dataService.getMeta().getEntityMetaDatas();
		Iterables.addAll(allMetaData, existingMetaData);

		// Use all metadata for dependency resolving
		List<EntityMetaData> resolved = DependencyResolver.resolve(allMetaData);

		// Only import source
		resolved.retainAll(metaDataList);

		return resolved;
	}

	private ImmutableMap<String, EntityMetaData> getEntityMetaDataMap(DataService dataService,
			RepositoryCollection source)
	{
		if (source.getRepository(EmxMetaDataParser.ATTRIBUTES) != null)
		{
			return getEntityMetaDataFromSource(source).getEntityMap();
		}
		else
		{
			return getEntityMetaDataFromDataService(dataService, source.getEntityNames());
		}
	}

	private ImmutableMap<String, EntityMetaData> getEntityMetaDataFromDataService(DataService dataService,
			Iterable<String> entityNames)
	{
		ImmutableMap.Builder<String, EntityMetaData> builder = ImmutableMap.<String, EntityMetaData> builder();
		for (String name : entityNames)
		{
			builder.put(name, dataService.getRepository(name).getEntityMetaData());
		}
		return builder.build();
	}

	@Override
	public EntitiesValidationReport validate(RepositoryCollection source)
	{
		MyEntitiesValidationReport report = new MyEntitiesValidationReport();

		Map<String, EntityMetaData> metaDataMap = getEntityMetaDataMap(dataService, source);

		for (EntityMetaData emd : metaDataMap.values())
		{
			MetaValidationUtils.validateEntityMetaData(emd);
		}

		for (String sheet : source.getEntityNames())
		{
			if (PACKAGES.equals(sheet))
			{
				IntermediateParseResults parseResult = new IntermediateParseResults();
				parsePackagesSheet(source.getRepository(sheet), parseResult);
				for (String packageName : parseResult.getPackages().keySet())
				{
					report.addPackage(packageName);
				}
			}
			else if (!ENTITIES.equals(sheet) && !ATTRIBUTES.equals(sheet) && !TAGS.equals(sheet))
			{
				// check if sheet is known
				report = report.addEntity(sheet, metaDataMap.containsKey(sheet));

				// check the fields
				Repository s = source.getRepository(sheet);
				EntityMetaData target = metaDataMap.get(sheet);

				if (target != null)
				{
					for (AttributeMetaData att : s.getEntityMetaData().getAttributes())
					{
						AttributeMetaData attribute = target.getAttribute(att.getName());
						boolean known = attribute != null && attribute.getExpression() == null;
						report = report.addAttribute(att.getName(),
								known ? AttributeState.IMPORTABLE : AttributeState.UNKNOWN);
					}
					for (AttributeMetaData att : target.getAttributes())
					{
						if (!(att.getDataType() instanceof CompoundField))
						{
							if (!att.isAuto() && att.getExpression() == null
									&& !report.getFieldsImportable().get(sheet).contains(att.getName()))
							{
								boolean required = !att.isNillable() && !att.isAuto();
								report = report.addAttribute(att.getName(),
										required ? AttributeState.REQUIRED : AttributeState.AVAILABLE);
							}
						}
					}
				}
			}
		}

		// Add entities without data
		for (String entityName : metaDataMap.keySet())
		{
			if (!report.getSheetsImportable().containsKey(entityName)) report.addEntity(entityName, true);
		}

		return report;
	}

}
