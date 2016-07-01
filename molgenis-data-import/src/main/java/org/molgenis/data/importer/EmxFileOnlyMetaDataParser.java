package org.molgenis.data.importer;

import com.google.common.collect.*;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.Range;
import org.molgenis.data.i18n.I18nUtils;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.LanguageMetaData;
import org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState;
import org.molgenis.data.meta.MetaValidationUtils;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.util.DependencyResolver;
import org.springframework.util.StringUtils;

import java.util.*;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.i18n.I18nUtils.getLanguageCode;
import static org.molgenis.data.i18n.I18nUtils.isI18n;
import static org.molgenis.data.importer.EmxMetaDataParser.EmxAttribute;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.*;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.LABEL;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.TAGS;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.support.EntityMetaDataUtils.isReferenceType;
import static org.molgenis.data.support.EntityMetaDataUtils.isStringType;

/**
 * Parser for the EMX metadata. This class is stateless, but it passes state between methods using
 * {@link IntermediateParseResults}.
 */
public class EmxFileOnlyMetaDataParser implements MetaDataParser
{
	private static final String ID_ATTRIBUTE = "idAttribute";
	private static final String LOOKUP_ATTRIBUTE = "lookupAttribute";
	private static final String LABEL_ATTRIBUTE = "labelAttribute";

	// Table names in the source
	public static final String EMX_ENTITIES = "entities";
	public static final String EMX_PACKAGES = "packages";
	public static final String EMX_TAGS = "tags";
	public static final String EMX_ATTRIBUTES = "attributes";
	public static final String EMX_LANGUAGES = "languages";
	public static final String EMX_I18NSTRINGS = "i18nstrings";

	private static final Map<String, String> EMX_NAME_TO_REPO_NAME_MAP;

	static
	{
		EMX_NAME_TO_REPO_NAME_MAP = new HashMap<>();
		EMX_NAME_TO_REPO_NAME_MAP.put(EMX_ENTITIES, EntityMetaDataMetaData.ENTITY_META_DATA);
		EMX_NAME_TO_REPO_NAME_MAP.put(EMX_PACKAGES, PackageMetaData.PACKAGE);
		EMX_NAME_TO_REPO_NAME_MAP.put(EMX_TAGS, TagMetaData.TAG);
		EMX_NAME_TO_REPO_NAME_MAP.put(EMX_ATTRIBUTES, AttributeMetaDataMetaData.ATTRIBUTE_META_DATA);
		EMX_NAME_TO_REPO_NAME_MAP.put(EMX_LANGUAGES, LanguageMetaData.LANGUAGE);
		EMX_NAME_TO_REPO_NAME_MAP.put(EMX_I18NSTRINGS, I18nStringMetaData.I18N_STRING);
	}

	private static final String ENTITY = "entity";
	private static final String PART_OF_ATTRIBUTE = "partOfAttribute";

	private static final List<String> SUPPORTED_ENTITY_ATTRIBUTES = Arrays
			.asList(EntityMetaDataMetaData.LABEL.toLowerCase(), EntityMetaDataMetaData.DESCRIPTION.toLowerCase(),
					"name", ABSTRACT.toLowerCase(), EXTENDS.toLowerCase(), "package", EntityMetaDataMetaData.TAGS,
					BACKEND);

	private static final List<String> SUPPORTED_ATTRIBUTE_ATTRIBUTES = Arrays
			.asList(AGGREGATEABLE.toLowerCase(), DATA_TYPE.toLowerCase(), DESCRIPTION.toLowerCase(),
					ENTITY.toLowerCase(), ENUM_OPTIONS.toLowerCase(), ID_ATTRIBUTE.toLowerCase(), LABEL.toLowerCase(),
					LABEL_ATTRIBUTE.toLowerCase(), LOOKUP_ATTRIBUTE.toLowerCase(), NAME, NILLABLE.toLowerCase(),
					PART_OF_ATTRIBUTE.toLowerCase(), RANGE_MAX.toLowerCase(), RANGE_MIN.toLowerCase(),
					READ_ONLY.toLowerCase(), REF_ENTITY.toLowerCase(), VISIBLE.toLowerCase(), UNIQUE.toLowerCase(),
					TAGS.toLowerCase(), EXPRESSION.toLowerCase(), VALIDATION_EXPRESSION.toLowerCase(),
					DEFAULT_VALUE.toLowerCase());

	private static final String AUTO = "auto";

	private final PackageFactory packageFactory;
	private final AttributeMetaDataFactory attrMetaFactory;

	public EmxFileOnlyMetaDataParser(PackageFactory packageFactory,
			AttributeMetaDataFactory attrMetaFactory)
	{
		this.packageFactory = requireNonNull(packageFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
	}

	/**
	 * Parses metadata from a collection of repositories.
	 *
	 * @param source the {@link RepositoryCollection} containing the metadata to parse
	 * @return {@link IntermediateParseResults} containing the parsed metadata
	 */
	private IntermediateParseResults getEntityMetaDataFromSource(RepositoryCollection source)
	{
		// TODO: this task is actually a 'merge' instead of 'import'
		// so we need to consider both new metadata and existing ...
		IntermediateParseResults intermediateResults = parseTagsSheet(source.getRepository(EMX_TAGS));

		parsePackagesSheet(source.getRepository(EMX_PACKAGES), intermediateResults);
		parsePackageTags(source.getRepository(EMX_PACKAGES), intermediateResults);
		parseEntitiesSheet(source.getRepository(EMX_ENTITIES), intermediateResults);
		parseAttributesSheet(source.getRepository(EMX_ATTRIBUTES), intermediateResults);
		reiterateToMapRefEntity(source.getRepository(EMX_ATTRIBUTES), intermediateResults);

		// languages tab
		if (source.hasRepository(EMX_LANGUAGES))
		{
			parseLanguages(source.getRepository(EMX_LANGUAGES), intermediateResults);
		}

		// i18nstrings tab
		if (source.hasRepository(EMX_I18NSTRINGS))
		{
			parseI18nStrings(source.getRepository(EMX_I18NSTRINGS), intermediateResults);
		}

		return intermediateResults;
	}

	/**
	 * Parses all tags defined in the tags repository.
	 *
	 * @param tagRepository the {@link Repository} that contains the tags entity
	 * @return Map mapping tag Identifier to tag {@link Entity}, will be empty if no tags repository was found
	 */
	private IntermediateParseResults parseTagsSheet(Repository<Entity> tagRepository)
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
	 * @param attributesRepo      Repository for the attributes
	 * @param intermediateResults {@link IntermediateParseResults} with the tags already parsed
	 */
	private void parseAttributesSheet(Repository<Entity> attributesRepo, IntermediateParseResults intermediateResults)
	{
		for (AttributeMetaData attr : attributesRepo.getEntityMetaData().getAtomicAttributes())
		{
			if (!SUPPORTED_ATTRIBUTE_ATTRIBUTES.contains(attr.getName().toLowerCase()) && !((
					I18nUtils.isI18n(attr.getName()) && (attr.getName().toLowerCase().startsWith(LABEL) || attr
							.getName().toLowerCase().startsWith(DESCRIPTION)))))
			{
				throw new IllegalArgumentException("Unsupported attribute metadata: attributes. " + attr.getName());
			}
		}

		Map<String, Map<String, EmxAttribute>> attributesMap = Maps.newLinkedHashMap();

		// 1st pass: create attribute stubs
		int i = 1;// Header
		for (Entity attributeEntity : attributesRepo)
		{
			i++;

			String attributeName = attributeEntity.getString(NAME);
			if (attributeName == null)
			{
				throw new IllegalArgumentException(format("attributes.name is missing on line [%d]", i));
			}

			String entityName = attributeEntity.getString(ENTITY);
			if (entityName == null)
			{
				throw new IllegalArgumentException(
						format("attributes.entity is missing for attribute named: %s on line [%d]", attributeName, i));
			}

			// create attribute
			AttributeMetaData attribute = attrMetaFactory.create().setName(attributeName);

			Map<String, EmxAttribute> entitiesMap = attributesMap.get(entityName);
			if (entitiesMap == null)
			{
				entitiesMap = Maps.newLinkedHashMap();
				attributesMap.put(entityName, entitiesMap);
			}
			entitiesMap.put(attributeName, new EmxAttribute(attribute));
		}

		// 2nd pass: set all properties on attribute stubs except for attribute relations
		i = 1;// Header
		for (Entity emxAttrEntity : attributesRepo)
		{
			i++;

			String emxEntityName = emxAttrEntity.getString(ENTITY);
			Map<String, EmxAttribute> entityMap = attributesMap.get(emxEntityName);

			String emxName = emxAttrEntity.getString(NAME);
			EmxAttribute emxAttr = entityMap.get(emxName);
			AttributeMetaData attr = emxAttr.getAttr();

			String emxDataType = emxAttrEntity.getString(DATA_TYPE);
			String emxRefEntity = emxAttrEntity.getString(REF_ENTITY);

			if (emxDataType != null)
			{
				AttributeType t = AttributeType.toEnum(emxDataType);
				if (t == null)
				{
					throw new IllegalArgumentException(
							"attributes.dataType error on line " + i + ": " + emxDataType + " unknown data type");
				}
				attr.setDataType(t);
			}
			else
			{
				attr.setDataType(STRING);
			}

			String emxAttrNillable = emxAttrEntity.getString(NILLABLE);
			String emxIsIdAttr = emxAttrEntity.getString(ID_ATTRIBUTE);
			String emxAttrVisible = emxAttrEntity.getString(VISIBLE);
			String emxAggregateable = emxAttrEntity.getString(AGGREGATEABLE);
			String emxIsLookupAttr = emxAttrEntity.getString(LOOKUP_ATTRIBUTE);
			String emxIsLabelAttr = emxAttrEntity.getString(LABEL_ATTRIBUTE);
			String emxReadOnly = emxAttrEntity.getString(READ_ONLY);
			String emxUnique = emxAttrEntity.getString(UNIQUE);
			String expression = emxAttrEntity.getString(EXPRESSION);
			List<String> tagIds = DataConverter.toList(emxAttrEntity.get(TAGS));
			String validationExpression = emxAttrEntity.getString(VALIDATION_EXPRESSION);
			String defaultValue = emxAttrEntity.getString(DEFAULT_VALUE);

			if (emxAttrNillable != null)
			{
				attr.setNillable(parseBoolean(emxAttrNillable, i, NILLABLE));
			}
			if (emxIsIdAttr != null)
			{
				emxAttr.setIdAttr(emxIsIdAttr.equalsIgnoreCase(AUTO) || parseBoolean(emxIsIdAttr, i, ID_ATTRIBUTE));
			}
			if (emxAttrVisible != null)
			{
				if (emxAttrVisible.equalsIgnoreCase("true") || emxAttrVisible.equalsIgnoreCase("false"))
				{
					attr.setVisible(parseBoolean(emxAttrVisible, i, VISIBLE));
				}
				else
				{
					attr.setVisibleExpression(emxAttrVisible);
				}
			}
			if (emxAggregateable != null)
			{
				attr.setAggregatable(parseBoolean(emxAggregateable, i, AGGREGATEABLE));
			}
			if (emxReadOnly != null)
			{
				attr.setReadOnly(parseBoolean(emxReadOnly, i, READ_ONLY));
			}
			if (emxUnique != null)
			{
				attr.setUnique(parseBoolean(emxUnique, i, UNIQUE));
			}
			if (expression != null)
			{
				attr.setExpression(expression);
			}
			if (validationExpression != null)
			{
				attr.setValidationExpression(validationExpression);
			}
			if (defaultValue != null)
			{
				attr.setDefaultValue(defaultValue);
			}
			attr.setAuto(emxIsIdAttr != null && emxIsIdAttr.equalsIgnoreCase(AUTO));

			if ((emxIsIdAttr != null) && !emxIsIdAttr.equalsIgnoreCase("true") && !emxIsIdAttr.equalsIgnoreCase("false")
					&& !emxIsIdAttr.equalsIgnoreCase(AUTO))
			{
				throw new IllegalArgumentException(
						format("Attributes error on line [%d]. Illegal idAttribute value. Allowed values are 'TRUE', 'FALSE' or 'AUTO'",
								i));
			}

			if (attr.isAuto() && !isStringType(attr))
			{
				throw new IllegalArgumentException(
						format("Attributes error on line [%d]. Auto attributes can only be of data type 'string'", i));
			}

			if (emxIsLookupAttr != null)
			{
				boolean isLookAttr = parseBoolean(emxIsLookupAttr, i, LOOKUP_ATTRIBUTE);
				if (isLookAttr && isReferenceType(attr))
				{
					throw new IllegalArgumentException(
							format("attributes.lookupAttribute error on line [%d] (%s.%s) lookupAttribute cannot be of type %s",
									i, emxEntityName, emxName, attr.getDataType().toString()));
				}

				emxAttr.setLookupAttr(isLookAttr);
			}

			if (emxIsLabelAttr != null)
			{
				boolean isLabelAttr = parseBoolean(emxIsLabelAttr, i, LABEL_ATTRIBUTE);
				if (isLabelAttr && isReferenceType(attr))
				{
					throw new IllegalArgumentException(
							format("attributes.labelAttribute error on line [%d] (%s.%s): labelAttribute cannot be of type %s",
									i, emxEntityName, emxName, attr.getDataType().toString()));
				}

				emxAttr.setLabelAttr(isLabelAttr);
			}

			attr.setLabel(emxAttrEntity.getString(LABEL));

			for (String attrName : emxAttrEntity.getAttributeNames())
			{
				if (isI18n(attrName))
				{
					if (attrName.startsWith(LABEL))
					{
						String label = emxAttrEntity.getString(attrName);
						if (label != null)
						{
							String languageCode = getLanguageCode(attrName);
							attr.setLabel(languageCode, label);
						}
					}
					else if (attrName.startsWith(DESCRIPTION))
					{
						String description = emxAttrEntity.getString(attrName);
						if (description != null)
						{
							String languageCode = getLanguageCode(attrName);
							attr.setDescription(languageCode, description);
						}
					}
				}
			}

			attr.setDescription(emxAttrEntity.getString(DESCRIPTION));

			if (attr.getDataType() == ENUM)
			{
				List<String> enumOptions = DataConverter.toList(emxAttrEntity.get(ENUM_OPTIONS));
				if (enumOptions == null || enumOptions.isEmpty())
				{
					throw new IllegalArgumentException(
							format("Missing enum options for attribute [%s] of entity [%s]", attr.getName(),
									emxEntityName));
				}
				attr.setEnumOptions(enumOptions);
			}

			if (isReferenceType(attr) && StringUtils.isEmpty(emxRefEntity))
			{
				throw new IllegalArgumentException(
						format("Missing refEntity on line [%d] (%s.%s)", i, emxEntityName, emxName));
			}

			if (isReferenceType(attr) && attr.isNillable() && attr.isAggregatable())
			{
				throw new IllegalArgumentException(
						format("attributes.aggregatable error on line [%d] (%s.%s): aggregatable nillable attribute cannot be of type %s",
								i, emxEntityName, emxName, attr.getDataType().toString()));
			}

			String emxRangeMin = emxAttrEntity.getString(RANGE_MIN);
			Long rangeMin;
			if (emxRangeMin != null)
			{
				try
				{
					rangeMin = Long.valueOf(emxRangeMin);
				}
				catch (NumberFormatException e)
				{
					throw new MolgenisDataException(
							format("Invalid range rangeMin [%s] value for attribute [%s] of entity [%s], should be a long",
									emxRangeMin, emxName, emxEntityName));
				}
			}
			else
			{
				rangeMin = null;
			}

			String emxRangeMax = emxAttrEntity.getString(RANGE_MAX);
			Long rangeMax;
			if (emxRangeMax != null)
			{
				try
				{
					rangeMax = Long.valueOf(emxRangeMax);
				}
				catch (NumberFormatException e)
				{
					throw new MolgenisDataException(
							format("Invalid range rangeMax [%s] value for attribute [%s] of entity [%s], should be a long",
									emxRangeMax, emxName, emxEntityName));
				}
			}
			else
			{
				rangeMax = null;
			}

			if (rangeMin != null || rangeMax != null)
			{
				attr.setRange(new Range(rangeMin, rangeMax));
			}

			if (tagIds != null)
			{
				for (String tagId : tagIds)
				{
					Entity tagEntity = intermediateResults.getTagEntity(tagId);
					if (tagEntity == null)
					{
						throw new MolgenisDataException(
								"Unknown tag: " + tagId + " for attribute [" + attr.getName() + "] of entity ["
										+ emxEntityName + "]). Please specify on the " + EMX_TAGS + " sheet.");
					}
					intermediateResults.addAttributeTag(emxEntityName, SemanticTag.asTag(attr, tagEntity));
				}
			}

		}

		// 3rd pass: validate and create attribute relationships
		Map<String, Set<String>> rootAttributes = Maps.newLinkedHashMap();
		i = 1;// Header
		for (Entity attributeEntity : attributesRepo)

		{
			i++;

			String entityName = attributeEntity.getString(ENTITY);
			Map<String, EmxAttribute> entityMap = attributesMap.get(entityName);

			String attributeName = attributeEntity.getString(NAME);
			AttributeMetaData attribute = entityMap.get(attributeName).getAttr();

			// bootstrap attribute parent-children relations for compound attributes
			String partOfAttribute = attributeEntity.getString(PART_OF_ATTRIBUTE);
			if (partOfAttribute != null && !partOfAttribute.isEmpty())
			{
				AttributeMetaData compoundAttribute = entityMap.get(partOfAttribute).getAttr();

				if (compoundAttribute == null)
				{
					throw new IllegalArgumentException(
							"partOfAttribute [" + partOfAttribute + "] of attribute [" + attributeName + "] of entity ["
									+ entityName + "] must refer to an existing compound attribute on line " + i);
				}

				if (compoundAttribute.getDataType() != COMPOUND)
				{
					throw new IllegalArgumentException(
							"partOfAttribute [" + partOfAttribute + "] of attribute [" + attributeName + "] of entity ["
									+ entityName + "] must refer to a attribute of type [" + COMPOUND + "] on line "
									+ i);
				}

				compoundAttribute.addAttributePart(attribute);
			}
			else
			{
				Set<String> entityRootAttributes = rootAttributes.get(entityName);
				if (entityRootAttributes == null)
				{
					entityRootAttributes = new LinkedHashSet<>();
					rootAttributes.put(entityName, entityRootAttributes);
				}
				entityRootAttributes.add(attributeName);
			}
		}

		// store attributes with entities
		for (Map.Entry<String, Map<String, EmxAttribute>> entry : attributesMap.entrySet())

		{
			String entityName = entry.getKey();
			Map<String, EmxAttribute> attributes = entry.getValue();

			List<EmxAttribute> editableEntityMetaData = Lists.newArrayList();
			// add root attributes to entity
			Set<String> entityAttributeNames = rootAttributes.get(entityName);
			if (entityAttributeNames != null)
			{
				for (EmxAttribute attribute : attributes.values())
				{
					if (entityAttributeNames.contains(attribute.getAttr().getName()))
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
	 * @param entitiesRepo        the Repository for the entities
	 * @param intermediateResults {@link IntermediateParseResults} containing the attributes already parsed
	 */
	private void parseEntitiesSheet(Repository<Entity> entitiesRepo, IntermediateParseResults intermediateResults)
	{
		if (entitiesRepo != null)
		{
			for (AttributeMetaData attr : entitiesRepo.getEntityMetaData().getAtomicAttributes())
			{
				if (!EmxFileOnlyMetaDataParser.SUPPORTED_ENTITY_ATTRIBUTES.contains(attr.getName().toLowerCase()) && !(
						I18nUtils.isI18n(attr.getName()) && (
								attr.getName().startsWith(EntityMetaDataMetaData.DESCRIPTION) || attr.getName()
										.startsWith(EntityMetaDataMetaData.LABEL))))
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
				if (packageName != null && !PACKAGE_DEFAULT.equals(packageName))
				{
					entityName = packageName + PACKAGE_SEPARATOR + entityName;
				}

				EntityMetaData md = intermediateResults.getEntityMetaData(entityName);
				if (md == null)
				{
					md = intermediateResults.addEntityMetaData(entityName);
				}

				if (packageName != null)
				{
					Package p = intermediateResults.getPackage(packageName);
					if (p == null)
					{
						throw new MolgenisDataException(
								"Unknown package: '" + packageName + "' for entity '" + entity.getString("name")
										+ "'. Please specify the package on the " + EMX_PACKAGES
										+ " sheet and use the fully qualified package and entity names.");
					}
					md.setPackage(p);
				}

				md.setLabel(entity.getString(EntityMetaDataMetaData.LABEL));
				md.setDescription(entity.getString(EntityMetaDataMetaData.DESCRIPTION));

				for (String attributeName : entity.getAttributeNames())
				{
					if (isI18n(attributeName))
					{
						if (attributeName.startsWith(EntityMetaDataMetaData.DESCRIPTION))
						{
							String description = entity.getString(attributeName);
							if (description != null)
							{
								String languageCode = getLanguageCode(attributeName);
								md.setDescription(languageCode, description);
							}
						}
						else if (attributeName.startsWith(EntityMetaDataMetaData.LABEL))
						{
							String label = entity.getString(attributeName);
							if (label != null)
							{
								String languageCode = getLanguageCode(attributeName);
								md.setLabel(languageCode, label);
							}
						}
					}
				}

				if (entity.getBoolean(ABSTRACT) != null) md.setAbstract(entity.getBoolean(ABSTRACT));
				List<String> tagIds = DataConverter.toList(entity.get(TAGS));

				String extendsEntityName = entity.getString(EXTENDS);
				if (extendsEntityName != null)
				{
					EntityMetaData extendsEntityMeta = null;
					if (intermediateResults.knowsEntity(extendsEntityName))
						extendsEntityMeta = intermediateResults.getEntityMetaData(extendsEntityName);

					if (extendsEntityMeta == null)
					{
						throw new MolgenisDataException(
								"Missing super entity " + extendsEntityName + " for entity " + entityName + " on line "
										+ i);
					}

					md.setExtends(extendsEntityMeta);
				}

				// FIXME fix tag import
				//				if (tagIds != null)
				//				{
				//					for (String tagId : tagIds)
				//					{
				//						Entity tagEntity = intermediateResults.getTagEntity(tagId);
				//						if (tagEntity == null)
				//						{
				//							throw new MolgenisDataException("Unknown tag: " + tagId + " for entity [" + entityName
				//									+ "]). Please specify on the " + EMX_TAGS + " sheet.");
				//						}
				//						intermediateResults.addEntityTag(Tag.<EntityMetaData> asTag(md, tagEntity));
				//					}
				//				}
			}
		}
	}

	/**
	 * Parses the packages sheet
	 *
	 * @param repo                {@link Repository} for the packages
	 * @param intermediateResults {@link IntermediateParseResults} containing the parsed tag entities
	 */
	private void parsePackagesSheet(Repository<Entity> repo, IntermediateParseResults intermediateResults)
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
			String description = pack.getString(PackageMetaData.DESCRIPTION);
			String parentName = pack.getString(PackageMetaData.PARENT);
			Package parent = null;
			if (parentName != null)
			{
				if (!name.toLowerCase().startsWith(parentName.toLowerCase())) throw new MolgenisDataException(
						"Inconsistent package structure. Package: '" + name + "', parent: '" + parentName + "'");
				simpleName = name.substring(parentName.length() + 1);// subpackage_package
				parent = intermediateResults.getPackage(parentName);
			}

			intermediateResults.addPackage(name,
					packageFactory.create().setSimpleName(simpleName).setDescription(description).setParent(parent));
		}
	}

	private void parseLanguages(Repository<Entity> repo, IntermediateParseResults intermediateResults)
	{
		repo.forEach(intermediateResults::addLanguage);
	}

	private void parseI18nStrings(Repository<Entity> repo, IntermediateParseResults intermediateResults)
	{
		repo.forEach(intermediateResults::addI18nString);
	}

	private void parsePackageTags(Repository<Entity> repo, IntermediateParseResults intermediateResults)
	{
		if (repo != null)
		{
			for (Entity pack : repo)
			{
				Iterable<String> tagIdentifiers = DataConverter.toList(pack.get(TAGS));
				if (tagIdentifiers != null)
				{
					String name = pack.getString(NAME);
					Package p = intermediateResults.getPackage(name);
					if (p == null) throw new IllegalArgumentException("Unknown package '" + name + "'");

					for (String tagIdentifier : tagIdentifiers)
					{
						Entity tagEntity = intermediateResults.getTagEntity(tagIdentifier);
						if (tagEntity == null)
						{
							throw new IllegalArgumentException("Unknown tag '" + tagIdentifier + "'");
						}
						//p.addTag(Tag.<Package> asTag(p, tagEntity)); // FIXME
						throw new UnsupportedOperationException();
					}
				}
			}
		}
	}

	private List<Entity> resolvePackages(Repository<Entity> packageRepo)
	{
		List<Entity> resolved = new ArrayList<>();
		if ((packageRepo == null) || Iterables.isEmpty(packageRepo)) return resolved;

		List<Entity> unresolved = new ArrayList<>();
		Map<String, Entity> resolvedByName = new HashMap<>();

		for (Entity pack : packageRepo)
		{
			String name = pack.getString(NAME);
			String parentName = pack.getString(PackageMetaData.PARENT);

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
				Entity parent = resolvedByName.get(pack.getString(PackageMetaData.PARENT));
				if (parent != null)
				{
					String name = pack.getString(NAME);
					ready.add(pack);
					resolvedByName.put(name, pack);
				}
			}

			if (ready.isEmpty())
				throw new IllegalArgumentException("Could not resolve packages. Is there a circular reference?");
			resolved.addAll(ready);
			unresolved.removeAll(ready);
			ready.clear();
		}

		return resolved;
	}

	/**
	 * re-iterate to map the mrefs/xref refEntity (or give error if not found) TODO consider also those in existing db
	 *
	 * @param attributeRepo       the attributes {@link Repository}
	 * @param intermediateResults {@link ParsedMetaData} to add the ref entities to
	 */
	private void reiterateToMapRefEntity(Repository<Entity> attributeRepo, IntermediateParseResults intermediateResults)
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
				EntityMetaData EntityMetaData = intermediateResults.getEntityMetaData(entityName);
				AttributeMetaData AttributeMetaData = (AttributeMetaData) EntityMetaData.getAttribute(attributeName);

				AttributeMetaData.setRefEntity(intermediateResults.getEntityMetaData(refEntityName));
			}
		}
	}

	@Override
	public ParsedMetaData parse(final RepositoryCollection source, String defaultPackage)
	{
		if (source.getRepository(EmxMetaDataParser.EMX_ATTRIBUTES) != null)
		{
			IntermediateParseResults intermediateResults = getEntityMetaDataFromSource(source);
			List<EntityMetaData> entities;
			if ((defaultPackage == null) || PACKAGE_DEFAULT.equalsIgnoreCase(defaultPackage))
			{
				entities = intermediateResults.getEntities();
			}
			else
			{
				entities = putEntitiesInDefaultPackage(intermediateResults, defaultPackage);
			}

			return new ParsedMetaData(resolveEntityDependencies(entities), intermediateResults.getPackages(),
					intermediateResults.getAttributeTags(), intermediateResults.getEntityTags(),
					intermediateResults.getLanguages(), intermediateResults.getI18nStrings());
		}
		else
		{
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Put the entities that are not in a package in the selected package
	 *
	 * @param intermediateResults
	 * @param defaultPackageName
	 * @return
	 */
	private List<EntityMetaData> putEntitiesInDefaultPackage(IntermediateParseResults intermediateResults,
			String defaultPackageName)
	{
		Package p = intermediateResults.getPackage(defaultPackageName);
		if (p == null) throw new IllegalArgumentException("Unknown package '" + defaultPackageName + "'");

		List<EntityMetaData> entities = new ArrayList<>();
		for (EntityMetaData entityMetaData : intermediateResults.getEntities())
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
	 * @param metaDataList {@link EntityMetaData} to put in the right order
	 * @return List of {@link EntityMetaData}, in the import order
	 */
	private List<EntityMetaData> resolveEntityDependencies(List<? extends EntityMetaData> metaDataList)
	{
		Map<String, EntityMetaData> allEntityMetaDataMap = new HashMap<>();
		Set<EntityMetaData> allMetaData = Sets.newLinkedHashSet(metaDataList);
		for (EntityMetaData emd : allMetaData)
		{
			allEntityMetaDataMap.put(emd.getName(), emd);
		}

		// Use all metadata for dependency resolving
		List<EntityMetaData> resolved = DependencyResolver.resolve(Sets.newLinkedHashSet(allEntityMetaDataMap.values()));

		// Only import source
		resolved.retainAll(metaDataList);

		return resolved;
	}

	private ImmutableMap<String, EntityMetaData> getEntityMetaDataMap(RepositoryCollection source)
	{
		return getEntityMetaDataFromSource(source).getEntityMap();
	}

	private ImmutableMap<String, EntityMetaData> getEntityMetaDataFromDataService(DataService dataService,
			Iterable<String> emxEntityNames)
	{
		ImmutableMap.Builder<String, EntityMetaData> builder = ImmutableMap.builder();
		for (String emxName : emxEntityNames)
		{
			String repoName = EMX_NAME_TO_REPO_NAME_MAP.get(emxName);
			if (repoName == null) repoName = emxName;
			builder.put(emxName, dataService.getRepository(repoName).getEntityMetaData());
		}
		return builder.build();
	}

	@Override
	public EntitiesValidationReport validate(RepositoryCollection source)
	{
		MyEntitiesValidationReport report = new MyEntitiesValidationReport();

		Map<String, EntityMetaData> metaDataMap = getEntityMetaDataMap(source);

		for (EntityMetaData emd : metaDataMap.values())
		{
			MetaValidationUtils.validateEntityMetaData(emd);
		}

		for (String sheet : source.getEntityNames())
		{
			if (EMX_PACKAGES.equals(sheet))
			{
				IntermediateParseResults parseResult = new IntermediateParseResults();
				parsePackagesSheet(source.getRepository(sheet), parseResult);
				for (String packageName : parseResult.getPackages().keySet())
				{
					report.addPackage(packageName);
				}
			}
			else if (!EMX_ENTITIES.equals(sheet) && !EMX_ATTRIBUTES.equals(sheet) && !EMX_TAGS.equals(sheet)
					&& !EMX_LANGUAGES.equals(sheet) && !EMX_I18NSTRINGS.equals(sheet))
			{
				// check if sheet is known
				report = report.addEntity(sheet, metaDataMap.containsKey(sheet));

				// check the fields
				Repository<Entity> s = source.getRepository(sheet);
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
						if (!(att.getDataType() == COMPOUND))
						{
							if (!att.isAuto() && att.getExpression() == null && !report.getFieldsImportable().get(sheet)
									.contains(att.getName()))
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

	/**
	 * Validates whether a EMX value for a boolean attribute is valid and returns the parsed boolean value.
	 *
	 * @param booleanStr boolean string
	 * @param i          row index
	 * @param colName    column name
	 * @return true or false
	 * @throws IllegalArgumentException if the given boolean string value if not one of [true, false] (case insensitive)
	 */
	private boolean parseBoolean(String booleanStr, int i, String colName)
	{
		if (booleanStr.equalsIgnoreCase(TRUE.toString()))
		{
			return true;
		}
		else if (booleanStr.equalsIgnoreCase(FALSE.toString()))
		{
			return false;
		}
		else
		{
			throw new IllegalArgumentException(
					format("attributes.[%s] error on line [%d]: Invalid value [%s] (expected true or false)", colName,
							i, booleanStr));
		}
	}
}
