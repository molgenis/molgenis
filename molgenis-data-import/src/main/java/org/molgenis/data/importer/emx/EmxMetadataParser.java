package org.molgenis.data.importer.emx;

import static com.google.common.collect.ImmutableMap.builder;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.DataConverter.toList;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.molgenis.data.i18n.I18nUtils.getLanguageCode;
import static org.molgenis.data.i18n.I18nUtils.isI18n;
import static org.molgenis.data.i18n.model.L10nStringMetadata.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState.AVAILABLE;
import static org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState.IMPORTABLE;
import static org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState.REQUIRED;
import static org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState.UNKNOWN;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.toEnum;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.util.AttributeUtils.isIdAttributeTypeAllowed;
import static org.molgenis.data.util.EntityTypeUtils.isReferenceType;
import static org.molgenis.data.util.EntityTypeUtils.isStringType;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Range;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.UnknownRepositoryCollectionException;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.i18n.model.LanguageFactory;
import org.molgenis.data.importer.EntitiesValidationReport;
import org.molgenis.data.importer.MetadataParser;
import org.molgenis.data.importer.MyEntitiesValidationReport;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagFactory;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.data.validation.meta.AttributeValidator;
import org.molgenis.data.validation.meta.AttributeValidator.ValidationMode;
import org.molgenis.data.validation.meta.EntityTypeValidator;
import org.molgenis.data.validation.meta.TagValidator;
import org.molgenis.i18n.LanguageService;

/**
 * Parser for the EMX metadata. This class is stateless, but it passes state between methods using
 * {@link IntermediateParseResults}.
 */
public class EmxMetadataParser implements MetadataParser {
  // Table names in the emx file
  public static final String EMX_PACKAGES = "packages";
  public static final String EMX_ENTITIES = "entities";
  public static final String EMX_ATTRIBUTES = "attributes";
  static final String EMX_TAGS = "tags";
  static final String EMX_LANGUAGES = "languages";
  static final String EMX_I18NSTRINGS = "i18nstrings";

  // Column names in the package sheet
  public static final String EMX_PACKAGE_NAME = "name";
  public static final String EMX_PACKAGE_DESCRIPTION = "description";
  public static final String EMX_PACKAGE_PARENT = "parent";
  public static final String EMX_PACKAGE_TAGS = "tags";
  public static final String EMX_PACKAGE_LABEL = "label";

  // Column names in the entities sheet
  public static final String EMX_ENTITIES_NAME = "name";
  public static final String EMX_ENTITIES_PACKAGE = "package";
  public static final String EMX_ENTITIES_LABEL = "label";
  public static final String EMX_ENTITIES_DESCRIPTION = "description";
  public static final String EMX_ENTITIES_ABSTRACT = "abstract";
  public static final String EMX_ENTITIES_EXTENDS = "extends";
  public static final String EMX_ENTITIES_BACKEND = "backend";
  public static final String EMX_ENTITIES_TAGS = "tags";

  // Column names in the attributes sheet
  public static final String EMX_ATTRIBUTES_NAME = "name";
  public static final String EMX_ATTRIBUTES_ENTITY = "entity";
  public static final String EMX_ATTRIBUTES_REF_ENTITY = "refEntity";
  public static final String EMX_ATTRIBUTES_MAPPED_BY = "mappedBy";
  public static final String EMX_ATTRIBUTES_DEFAULT_VALUE = "defaultValue";
  public static final String EMX_ATTRIBUTES_ID_ATTRIBUTE = "idAttribute";
  public static final String EMX_ATTRIBUTES_LOOKUP_ATTRIBUTE = "lookupAttribute";
  public static final String EMX_ATTRIBUTES_LABEL_ATTRIBUTE = "labelAttribute";
  public static final String EMX_ATTRIBUTES_PART_OF_ATTRIBUTE = "partOfAttribute";
  public static final String EMX_ATTRIBUTES_AGGREGATEABLE = "aggregateable";
  public static final String EMX_ATTRIBUTES_DATA_TYPE = "dataType";
  public static final String EMX_ATTRIBUTES_EXPRESSION = "expression";
  public static final String EMX_ATTRIBUTES_NILLABLE = "nillable";
  public static final String EMX_ATTRIBUTES_VISIBLE = "visible";
  public static final String EMX_ATTRIBUTES_LABEL = "label";
  public static final String EMX_ATTRIBUTES_DESCRIPTION = "description";
  public static final String EMX_ATTRIBUTES_ENUM_OPTIONS = "enumOptions";
  public static final String EMX_ATTRIBUTES_RANGE_MIN = "rangeMin";
  public static final String EMX_ATTRIBUTES_RANGE_MAX = "rangeMax";
  public static final String EMX_ATTRIBUTES_READ_ONLY = "readOnly";
  public static final String EMX_ATTRIBUTES_UNIQUE = "unique";
  public static final String EMX_ATTRIBUTES_VALIDATION_EXPRESSION = "validationExpression";
  public static final String EMX_ATTRIBUTES_TAGS = "tags";

  // NOT YET SUPPORTED
  // private static final String EMX_ATTRIBUTES_AUTO = "auto";
  // private static final String EMX_ATTRIBUTES_VISIBLE_EXPRESSION = "visibleExpression";

  // Column names in the tag sheet
  static final String EMX_TAG_IDENTIFIER = "identifier";
  static final String EMX_TAG_OBJECT_IRI = "objectIRI";
  static final String EMX_TAG_LABEL = "label";
  static final String EMX_TAG_RELATION_LABEL = "relationLabel";
  static final String EMX_TAG_CODE_SYSTEM = "codeSystem";
  static final String EMX_TAG_RELATION_IRI = "relationIRI";

  // Column names in the language sheet
  private static final String EMX_LANGUAGE_CODE = "code";
  private static final String EMX_LANGUAGE_NAME = "name";

  // Column names in the i18nstring sheet
  private static final String EMX_I18N_STRING_MSGID = "msgid";
  private static final String EMX_I18N_STRING_DESCRIPTION = "description";
  private static final String EMX_I18N_STRING_NAMESPACE = "namespace";
  private static final String DEFAULT_NAMESPACE = "default";

  private static final Map<String, String> EMX_NAME_TO_REPO_NAME_MAP = newHashMap();

  static {
    EMX_NAME_TO_REPO_NAME_MAP.put(EMX_ENTITIES, ENTITY_TYPE_META_DATA);
    EMX_NAME_TO_REPO_NAME_MAP.put(EMX_PACKAGES, PackageMetadata.PACKAGE);
    EMX_NAME_TO_REPO_NAME_MAP.put(EMX_TAGS, TAG);
    EMX_NAME_TO_REPO_NAME_MAP.put(EMX_ATTRIBUTES, ATTRIBUTE_META_DATA);
    EMX_NAME_TO_REPO_NAME_MAP.put(EMX_LANGUAGES, LANGUAGE);
    EMX_NAME_TO_REPO_NAME_MAP.put(EMX_I18NSTRINGS, L10N_STRING);
  }

  private static final List<String> EMX_ENTITIES_ALLOWED_ATTRS =
      Arrays.asList(
          EMX_ENTITIES_NAME.toLowerCase(),
          EMX_ENTITIES_PACKAGE.toLowerCase(),
          EMX_ENTITIES_LABEL.toLowerCase(),
          EMX_ENTITIES_DESCRIPTION,
          EMX_ENTITIES_ABSTRACT.toLowerCase(),
          EMX_ENTITIES_EXTENDS.toLowerCase(),
          EMX_ENTITIES_BACKEND.toLowerCase(),
          EMX_ENTITIES_TAGS.toLowerCase());

  private static final List<String> SUPPORTED_ATTRIBUTE_ATTRIBUTES =
      Arrays.asList(
          EMX_ATTRIBUTES_AGGREGATEABLE,
          EMX_ATTRIBUTES_DATA_TYPE,
          EMX_ATTRIBUTES_DESCRIPTION,
          EMX_ATTRIBUTES_ENTITY,
          EMX_ATTRIBUTES_ENUM_OPTIONS,
          EMX_ATTRIBUTES_ID_ATTRIBUTE,
          EMX_ATTRIBUTES_LABEL,
          EMX_ATTRIBUTES_LABEL_ATTRIBUTE,
          EMX_ATTRIBUTES_LOOKUP_ATTRIBUTE,
          EMX_ATTRIBUTES_NAME,
          EMX_ATTRIBUTES_NILLABLE,
          EMX_ATTRIBUTES_PART_OF_ATTRIBUTE,
          EMX_ATTRIBUTES_RANGE_MAX,
          EMX_ATTRIBUTES_RANGE_MIN,
          EMX_ATTRIBUTES_READ_ONLY,
          EMX_ATTRIBUTES_REF_ENTITY,
          EMX_ATTRIBUTES_MAPPED_BY,
          EMX_ATTRIBUTES_VISIBLE,
          EMX_ATTRIBUTES_UNIQUE,
          EMX_ATTRIBUTES_EXPRESSION,
          EMX_ATTRIBUTES_VALIDATION_EXPRESSION,
          EMX_ATTRIBUTES_DEFAULT_VALUE,
          EMX_ATTRIBUTES_TAGS);

  public static final String AUTO = "auto";

  private final DataService dataService;
  private final PackageFactory packageFactory;
  private final AttributeFactory attrMetaFactory;
  private final EntityTypeFactory entityTypeFactory;
  private final TagFactory tagFactory;
  private final LanguageFactory languageFactory;
  private final L10nStringFactory l10nStringFactory;
  private final EntityTypeValidator entityTypeValidator;
  private final AttributeValidator attributeValidator;
  private final TagValidator tagValidator;
  private final EntityTypeDependencyResolver entityTypeDependencyResolver;

  EmxMetadataParser(
      DataService dataService,
      PackageFactory packageFactory,
      AttributeFactory attrMetaFactory,
      EntityTypeFactory entityTypeFactory,
      TagFactory tagFactory,
      LanguageFactory languageFactory,
      L10nStringFactory l10nStringFactory,
      EntityTypeValidator entityTypeValidator,
      AttributeValidator attributeValidator,
      TagValidator tagValidator,
      EntityTypeDependencyResolver entityTypeDependencyResolver) {
    this.dataService = requireNonNull(dataService);
    this.packageFactory = requireNonNull(packageFactory);
    this.attrMetaFactory = requireNonNull(attrMetaFactory);
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.tagFactory = requireNonNull(tagFactory);
    this.languageFactory = requireNonNull(languageFactory);
    this.l10nStringFactory = requireNonNull(l10nStringFactory);
    this.entityTypeValidator = requireNonNull(entityTypeValidator);
    this.attributeValidator = requireNonNull(attributeValidator);
    this.tagValidator = requireNonNull(tagValidator);
    this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
  }

  @Override
  // FIXME The source is parsed twice!!! Once by determineImportableEntities and once by doImport
  public ParsedMetaData parse(
      final RepositoryCollection source, @Nullable @CheckForNull String packageId) {
    if (source.getRepository(EMX_ATTRIBUTES) != null) {
      IntermediateParseResults intermediateResults = getEntityTypeFromSource(source);
      List<EntityType> entities;
      if (packageId == null) {
        entities = intermediateResults.getEntities();
      } else {
        entities = putEntitiesInDefaultPackage(intermediateResults, packageId);
      }

      return new ParsedMetaData(
          entityTypeDependencyResolver.resolve(entities),
          intermediateResults.getPackages(),
          intermediateResults.getTags(),
          intermediateResults.getLanguages(),
          intermediateResults.getL10nStrings());
    } else {
      List<EntityType> metadataList = new ArrayList<>();
      for (String emxName : source.getEntityTypeIds()) {
        String repoName = EMX_NAME_TO_REPO_NAME_MAP.get(emxName);
        if (repoName == null) repoName = emxName;
        metadataList.add(dataService.getRepository(repoName).getEntityType());
      }
      IntermediateParseResults intermediateResults = parseTagsSheet(source.getRepository(EMX_TAGS));
      parsePackagesSheet(source.getRepository(EMX_PACKAGES), intermediateResults);

      if (source.hasRepository(EMX_LANGUAGES)) {
        parseLanguages(source.getRepository(EMX_LANGUAGES), intermediateResults);
      }

      if (source.hasRepository(EMX_I18NSTRINGS)) {
        parseI18nStrings(source.getRepository(EMX_I18NSTRINGS), intermediateResults);
      }

      return new ParsedMetaData(
          entityTypeDependencyResolver.resolve(metadataList),
          intermediateResults.getPackages(),
          intermediateResults.getTags(),
          intermediateResults.getLanguages(),
          intermediateResults.getL10nStrings());
    }
  }

  @Override
  public EntitiesValidationReport validate(RepositoryCollection source) {
    MyEntitiesValidationReport report = new MyEntitiesValidationReport();
    Map<String, EntityType> metaDataMap = getEntityTypeMap(dataService, source);
    return buildValidationReport(source, report, metaDataMap);
  }

  private ImmutableMap<String, EntityType> getEntityTypeMap(
      DataService dataService, RepositoryCollection source) {
    // If there is no attribute sheet, we assume the entity is already known in MOLGENIS
    if (source.getRepository(EMX_ATTRIBUTES) != null) {
      return getEntityTypeFromSource(source).getEntityMap();
    } else {
      ImmutableMap<String, EntityType> entityTypeMap =
          getEntityTypeFromDataService(dataService, source.getEntityTypeIds());
      if (source.getRepository(EMX_PACKAGES) != null) {
        IntermediateParseResults intermediateParseResults =
            new IntermediateParseResults(entityTypeFactory);
        parsePackagesSheet(source.getRepository(EMX_PACKAGES), intermediateParseResults);
        return ImmutableMap.<String, EntityType>builder()
            .putAll(intermediateParseResults.getEntityMap())
            .putAll(entityTypeMap)
            .build();
      }
      return entityTypeMap;
    }
  }

  private EntitiesValidationReport buildValidationReport(
      RepositoryCollection source,
      MyEntitiesValidationReport report,
      Map<String, EntityType> metaDataMap) {
    metaDataMap.values().forEach(entityTypeValidator::validate);
    metaDataMap
        .values()
        .stream()
        .map(EntityType::getAllAttributes)
        .forEach(
            attributes ->
                attributes.forEach(
                    attr ->
                        attributeValidator.validate(
                            attr, ValidationMode.ADD_SKIP_ENTITY_VALIDATION)));

    // validate package/entity/attribute tags
    metaDataMap
        .values()
        .stream()
        .map(EntityType::getPackage)
        .filter(Objects::nonNull)
        .forEach(aPackage -> aPackage.getTags().forEach(tagValidator::validate));
    metaDataMap
        .values()
        .forEach(entityType -> entityType.getTags().forEach(tagValidator::validate));
    metaDataMap
        .values()
        .stream()
        .map(EntityType::getAllAttributes)
        .forEach(
            attributes ->
                attributes.forEach(attr -> attr.getTags().forEach(tagValidator::validate)));

    report = generateEntityValidationReport(source, report, metaDataMap);

    // Add entities without data
    for (String entityTypeId : metaDataMap.keySet()) {
      if (!report.getSheetsImportable().containsKey(entityTypeId))
        report.addEntity(entityTypeId, true);
    }
    return report;
  }

  /**
   * Parses metadata from a collection of repositories.
   *
   * @param source the {@link RepositoryCollection} containing the metadata to parse
   * @return {@link IntermediateParseResults} containing the parsed metadata
   */
  private IntermediateParseResults getEntityTypeFromSource(RepositoryCollection source) {
    // TODO: this task is actually a 'merge' instead of 'import'
    // so we need to consider both new metadata and existing ...
    IntermediateParseResults intermediateResults = parseTagsSheet(source.getRepository(EMX_TAGS));

    parsePackagesSheet(source.getRepository(EMX_PACKAGES), intermediateResults);
    parseEntitiesSheet(source.getRepository(EMX_ENTITIES), intermediateResults);
    parseAttributesSheet(source.getRepository(EMX_ATTRIBUTES), intermediateResults);
    reiterateToMapRefEntity(source.getRepository(EMX_ATTRIBUTES), intermediateResults);

    // languages tab
    if (source.hasRepository(EMX_LANGUAGES))
      parseLanguages(source.getRepository(EMX_LANGUAGES), intermediateResults);

    // i18nstrings tab
    if (source.hasRepository(EMX_I18NSTRINGS))
      parseI18nStrings(source.getRepository(EMX_I18NSTRINGS), intermediateResults);

    postProcessMetadata(intermediateResults);

    return intermediateResults;
  }

  /**
   * Post process metadata: - Select label attributes that are not defined in EMX - Select lookup
   * attributes that are not defined in EMX
   *
   * @param intermediateResults intermediate parse results
   */
  private static void postProcessMetadata(IntermediateParseResults intermediateResults) {
    intermediateResults
        .getEntities()
        .forEach(
            entityType -> {
              // in case no label attribute was defined:
              // try to set own id attribute or another own attribute as label attribute
              if (entityType.getLabelAttribute() == null) {
                Attribute ownIdAttr = entityType.getOwnIdAttribute();
                if (ownIdAttr != null && ownIdAttr.isVisible()) {
                  ownIdAttr.setLabelAttribute(true);
                } else {
                  // select the first required visible owned attribute as label attribute
                  for (Attribute ownAttr : entityType.getOwnAtomicAttributes()) {
                    if (!ownAttr.isNillable()
                        && ownAttr.isVisible()
                        && !EntityTypeUtils.isReferenceType(ownAttr)) {
                      ownAttr.setLabelAttribute(true);
                      ownAttr.setNillable(false);
                      break;
                    }
                  }
                }
              }

              // in case no lookup attributes were defined:
              // try to set own id attribute and own label attribute as lookup attributes
              if (Iterables.size(entityType.getLookupAttributes()) == 0) {
                int lookupAttrIdx = 0;

                Attribute ownIdAttr = entityType.getOwnIdAttribute();
                if (ownIdAttr != null && ownIdAttr.isVisible()) {
                  ownIdAttr.setLookupAttributeIndex(lookupAttrIdx++);
                }

                Attribute ownLabelAttr = entityType.getOwnLabelAttribute();
                if (ownLabelAttr != null
                    && (ownIdAttr == null || !ownIdAttr.getName().equals(ownLabelAttr.getName()))) {
                  ownLabelAttr.setLookupAttributeIndex(lookupAttrIdx);
                }
              }
            });
  }

  /**
   * Parses all tags defined in the tags repository.
   *
   * @param tagRepository the {@link Repository} that contains the tags entity
   * @return Map mapping tag Identifier to tag {@link Entity}, will be empty if no tags repository
   *     was found
   */
  private IntermediateParseResults parseTagsSheet(Repository<Entity> tagRepository) {
    IntermediateParseResults intermediateParseResults =
        new IntermediateParseResults(entityTypeFactory);
    if (tagRepository != null) {
      for (Entity tagEntity : tagRepository) {
        String id = tagEntity.getString(EMX_TAG_IDENTIFIER);
        if (id != null) {
          intermediateParseResults.addTag(id, entityToTag(id, tagEntity));
        }
      }
    }
    return intermediateParseResults;
  }

  /**
   * Parses the packages sheet
   *
   * @param repo {@link Repository} for the packages
   * @param intermediateResults {@link IntermediateParseResults} containing the parsed tag entities
   */
  private void parsePackagesSheet(
      Repository<Entity> repo, IntermediateParseResults intermediateResults) {
    if (repo == null) return;

    // Collect packages
    int rowIndex = 1;
    for (Entity packageEntity : resolvePackages(repo)) {
      rowIndex++;

      // Package name is required
      String name = packageEntity.getString(EMX_PACKAGE_NAME);
      if (Strings.isNullOrEmpty(name)) {
        throw new IllegalArgumentException("package.name is missing on line " + rowIndex);
      }

      Package aPackage = packageFactory.create(name);
      aPackage.setDescription(packageEntity.getString(EMX_PACKAGE_DESCRIPTION));
      String label = packageEntity.getString(EMX_PACKAGE_LABEL);
      if (label == null) {
        label = name;
      }
      aPackage.setLabel(label);

      // Set parent package
      String parentName = packageEntity.getString(EMX_PACKAGE_PARENT);
      if (parentName != null) {
        if (!name.toLowerCase().startsWith(parentName.toLowerCase()))
          throw new MolgenisDataException(
              "Inconsistent package structure. Package: '"
                  + name
                  + "', parent: '"
                  + parentName
                  + '\'');

        aPackage.setParent(intermediateResults.getPackage(parentName));
      }

      // Set package tags
      List<String> tagIdentifiers = toList(packageEntity.getString(EMX_PACKAGE_TAGS));
      if (tagIdentifiers != null && !tagIdentifiers.isEmpty()) {
        List<Tag> tags = toTags(intermediateResults, tagIdentifiers);
        aPackage.setTags(tags);
      }

      // Add the complete package to the parse results
      intermediateResults.addPackage(name, aPackage);
    }
  }

  /** Convert tag identifiers to tags */
  private static List<Tag> toTags(
      IntermediateParseResults intermediateResults, List<String> tagIdentifiers) {
    if (tagIdentifiers.isEmpty()) {
      return emptyList();
    }

    List<Tag> tags = new ArrayList<>(tagIdentifiers.size());
    for (String tagIdentifier : tagIdentifiers) {
      Tag tag = intermediateResults.getTag(tagIdentifier);
      if (tag == null) {
        throw new IllegalArgumentException("Unknown tag '" + tagIdentifier + '\'');
      }
      tags.add(tag);
    }
    return tags;
  }

  /** Transforms an {@link Entity} to a {@link Tag} */
  private Tag entityToTag(String id, Entity tagEntity) {
    Tag tag = tagFactory.create(id);
    tag.setObjectIri(tagEntity.getString(EMX_TAG_OBJECT_IRI));
    tag.setLabel(tagEntity.getString(EMX_TAG_LABEL));
    tag.setRelationLabel(tagEntity.getString(EMX_TAG_RELATION_LABEL));
    tag.setCodeSystem(tagEntity.getString(EMX_TAG_CODE_SYSTEM));
    tag.setRelationIri(tagEntity.getString(EMX_TAG_RELATION_IRI));

    return tag;
  }

  /**
   * Load all entities (optional)
   *
   * @param entitiesRepo the Repository for the entities
   * @param intermediateResults {@link IntermediateParseResults} containing the attributes already
   *     parsed
   */
  private void parseEntitiesSheet(
      Repository<Entity> entitiesRepo, IntermediateParseResults intermediateResults) {
    if (entitiesRepo != null) {
      for (Attribute attr : entitiesRepo.getEntityType().getAtomicAttributes()) {
        if (!EMX_ENTITIES_ALLOWED_ATTRS.contains(attr.getName().toLowerCase())
            && !(isI18n(attr.getName())
                && (attr.getName().startsWith(EMX_ENTITIES_DESCRIPTION)
                    || attr.getName().startsWith(EMX_ENTITIES_LABEL)))) {
          throw new IllegalArgumentException(
              "Unsupported entity metadata: entities." + attr.getName());
        }
      }

      int i = 1;
      for (Entity entity : entitiesRepo) {
        i++;
        parseSingleEntityType(intermediateResults, i, entity);
      }
    }
  }

  private void parseSingleEntityType(
      IntermediateParseResults intermediateResults, int i, Entity entity) {
    String emxEntityName = entity.getString(EMX_ENTITIES_NAME);
    String emxEntityPackage = entity.getString(EMX_ENTITIES_PACKAGE);
    String emxEntityLabel = entity.getString(EMX_ENTITIES_LABEL);
    String emxEntityDescription = entity.getString(EMX_ENTITIES_DESCRIPTION);
    String emxEntityAbstract = entity.getString(EMX_ENTITIES_ABSTRACT);
    String emxEntityExtends = entity.getString(EMX_ENTITIES_EXTENDS);
    String emxEntityBackend = entity.getString(EMX_ENTITIES_BACKEND);
    String emxEntityTags = entity.getString(EMX_ENTITIES_TAGS);

    // required
    if (Strings.isNullOrEmpty(emxEntityName)) {
      throw new IllegalArgumentException("entity.name is missing on line " + i);
    }

    String entityTypeId;
    if (emxEntityPackage != null) {
      entityTypeId = emxEntityPackage + PACKAGE_SEPARATOR + emxEntityName;
    } else {
      entityTypeId = emxEntityName;
    }

    EntityType entityType = intermediateResults.getEntityType(entityTypeId);
    if (entityType == null) {
      entityType = intermediateResults.addEntityType(entityTypeId);
    }

    if (emxEntityBackend != null) {
      if (!dataService.getMeta().hasBackend(emxEntityBackend)) {
        throw new UnknownRepositoryCollectionException(emxEntityBackend);
      }
    } else {
      emxEntityBackend = dataService.getMeta().getDefaultBackend().getName();
    }
    entityType.setBackend(emxEntityBackend);

    if (emxEntityPackage != null) {
      Package p = intermediateResults.getPackage(emxEntityPackage);
      if (p == null) {
        throw new MolgenisDataException(
            "Unknown package: '"
                + emxEntityPackage
                + "' for entity '"
                + emxEntityName
                + "'. Please specify the package on the "
                + EMX_PACKAGES
                + " sheet and use the fully qualified package and entity names.");
      }
      entityType.setPackage(p);
    }

    if (emxEntityLabel == null) {
      emxEntityLabel = emxEntityName;
    }
    entityType.setLabel(emxEntityLabel);

    entityType.setDescription(emxEntityDescription);

    for (String attributeName : entity.getAttributeNames()) {
      if (isI18n(attributeName)) {
        if (attributeName.startsWith(EMX_ENTITIES_DESCRIPTION)) {
          String description = entity.getString(attributeName);
          if (description != null) {
            String languageCode = getLanguageCode(attributeName);
            entityType.setDescription(languageCode, description);
          }
        } else if (attributeName.startsWith(EMX_ENTITIES_LABEL)) {
          String label = entity.getString(attributeName);
          if (label != null) {
            String languageCode = getLanguageCode(attributeName);
            entityType.setLabel(languageCode, label);
          }
        }
      }
    }

    if (emxEntityAbstract != null) {
      entityType.setAbstract(parseBoolean(emxEntityAbstract, i, EMX_ENTITIES_ABSTRACT));
    }

    if (emxEntityExtends != null) {
      EntityType extendsEntityType = null;
      if (intermediateResults.knowsEntity(emxEntityExtends)) {
        extendsEntityType = intermediateResults.getEntityType(emxEntityExtends);
      } else {
        extendsEntityType = dataService.getMeta().getEntityType(emxEntityExtends).orElse(null);
      }

      if (extendsEntityType == null) {
        throw new MolgenisDataException(
            "Missing super entity "
                + emxEntityExtends
                + " for entity "
                + emxEntityName
                + " on line "
                + i);
      }

      entityType.setExtends(extendsEntityType);
    }

    List<String> tagIdentifiers = toList(emxEntityTags);
    if (tagIdentifiers != null && !tagIdentifiers.isEmpty()) {
      entityType.setTags(toTags(intermediateResults, tagIdentifiers));
    }
  }

  /** Resolves package fullNames by looping through all the packages and their parents */
  private static List<Entity> resolvePackages(Repository<Entity> packageRepo) {
    List<Entity> resolved = new ArrayList<>();
    if ((packageRepo == null) || Iterables.isEmpty(packageRepo)) return resolved;

    List<Entity> unresolved = new ArrayList<>();
    Map<String, Entity> resolvedByName = new HashMap<>();

    for (Entity pack : packageRepo) {
      String name = pack.getString(EMX_PACKAGE_NAME);
      String parentName = pack.getString(EMX_PACKAGE_PARENT);

      if (parentName == null) {
        resolved.add(pack);
        resolvedByName.put(name, pack);
      } else {
        unresolved.add(pack);
      }
    }

    if (resolved.isEmpty())
      throw new IllegalArgumentException(
          "Missing root package. There must be at least one package without a parent.");

    List<Entity> ready = new ArrayList<>();
    while (!unresolved.isEmpty()) {
      for (Entity pack : unresolved) {
        Entity parent = resolvedByName.get(pack.getString(EMX_PACKAGE_PARENT));
        if (parent != null) {
          String name = pack.getString(EMX_PACKAGE_NAME);
          ready.add(pack);
          resolvedByName.put(name, pack);
        }
      }

      if (ready.isEmpty())
        throw new IllegalArgumentException(
            "Could not resolve packages. Is there a circular reference?");
      resolved.addAll(ready);
      unresolved.removeAll(ready);
      ready.clear();
    }

    return resolved;
  }

  /**
   * Load all attributes from the source repository and add it to the {@link
   * IntermediateParseResults}.
   *
   * @param attributesRepo Repository for the attributes
   * @param intermediateResults {@link IntermediateParseResults} with the tags already parsed
   */
  private void parseAttributesSheet(
      Repository<Entity> attributesRepo, IntermediateParseResults intermediateResults) {
    for (Attribute attr : attributesRepo.getEntityType().getAtomicAttributes()) {
      if (!SUPPORTED_ATTRIBUTE_ATTRIBUTES.contains(attr.getName())
          && !((isI18n(attr.getName())
              && (attr.getName().startsWith(EMX_ATTRIBUTES_LABEL)
                  || attr.getName().startsWith(EMX_ATTRIBUTES_DESCRIPTION))))) {
        // check if casing mismatch
        SUPPORTED_ATTRIBUTE_ATTRIBUTES.forEach(
            emxAttrMetaAttr -> {
              if (emxAttrMetaAttr.equalsIgnoreCase(attr.getName())) {
                throw new IllegalArgumentException(
                    format(
                        "Unsupported attribute metadata: attributes.%s, did you mean attributes.%s?",
                        attr.getName(), emxAttrMetaAttr));
              }
            });
        throw new IllegalArgumentException(
            "Unsupported attribute metadata: attributes." + attr.getName());
      }
    }

    Map<String, Map<String, EmxAttribute>> attributesMap = newLinkedHashMap();

    // 1st pass: create attribute stubs
    int rowIndex = 1; // Header
    for (Entity attributeEntity : attributesRepo) {
      rowIndex++;

      String attributeName = attributeEntity.getString(EMX_ATTRIBUTES_NAME);
      String entityTypeId = attributeEntity.getString(EMX_ATTRIBUTES_ENTITY);

      checkRequiredAttributeAttributes(rowIndex, attributeName, entityTypeId);

      // create attribute
      Attribute attribute = attrMetaFactory.create().setName(attributeName);

      Map<String, EmxAttribute> entitiesMap =
          attributesMap.computeIfAbsent(entityTypeId, k -> newLinkedHashMap());

      if (entitiesMap.containsKey(attributeName)) {
        throw new MolgenisDataException(
            format(
                "Duplicate attribute name [%s] for entity type [%s]", attributeName, entityTypeId));
      }

      entitiesMap.put(attributeName, new EmxAttribute(attribute));
    }

    // 2nd pass: set all properties on attribute stubs except for attribute relations
    rowIndex = 1; // Header
    for (Entity emxAttrEntity : attributesRepo) {
      rowIndex++;

      String emxEntityName = emxAttrEntity.getString(EMX_ATTRIBUTES_ENTITY);
      Map<String, EmxAttribute> entityMap = attributesMap.get(emxEntityName);

      // If an entity is defined in the attribute sheet only,
      // make sure to create EntityType and set the backend
      EntityType md = intermediateResults.getEntityType(emxEntityName);
      if (md == null) {
        md = intermediateResults.addEntityType(emxEntityName);
        md.setBackend(dataService.getMeta().getDefaultBackend().getName());
      }

      String emxName = emxAttrEntity.getString(EMX_ATTRIBUTES_NAME);
      EmxAttribute emxAttr = entityMap.get(emxName);
      Attribute attr = emxAttr.getAttr();

      String emxRefEntity = emxAttrEntity.getString(EMX_ATTRIBUTES_REF_ENTITY);
      String emxDataType = emxAttrEntity.getString(EMX_ATTRIBUTES_DATA_TYPE);

      if (!Strings.isNullOrEmpty(emxDataType)) {
        AttributeType type = toEnum(emxDataType);
        if (type == null) {
          throw new IllegalArgumentException(
              "attributes.dataType error on line "
                  + rowIndex
                  + ": "
                  + emxDataType
                  + " unknown data type");
        }
        attr.setDataType(type);
      } else {
        attr.setDataType(STRING);
      }

      String emxAttrNillable = emxAttrEntity.getString(EMX_ATTRIBUTES_NILLABLE);
      String emxIdAttrValue = emxAttrEntity.getString(EMX_ATTRIBUTES_ID_ATTRIBUTE);
      String emxAttrVisible = emxAttrEntity.getString(EMX_ATTRIBUTES_VISIBLE);
      String emxAggregatable = emxAttrEntity.getString(EMX_ATTRIBUTES_AGGREGATEABLE);
      String emxIsLookupAttr = emxAttrEntity.getString(EMX_ATTRIBUTES_LOOKUP_ATTRIBUTE);
      String emxIsLabelAttr = emxAttrEntity.getString(EMX_ATTRIBUTES_LABEL_ATTRIBUTE);
      String emxReadOnly = emxAttrEntity.getString(EMX_ATTRIBUTES_READ_ONLY);
      String emxUnique = emxAttrEntity.getString(EMX_ATTRIBUTES_UNIQUE);
      String expression = emxAttrEntity.getString(EMX_ATTRIBUTES_EXPRESSION);
      String validationExpression = emxAttrEntity.getString(EMX_ATTRIBUTES_VALIDATION_EXPRESSION);
      String defaultValue = emxAttrEntity.getString(EMX_ATTRIBUTES_DEFAULT_VALUE);
      Object emxAttrTags = emxAttrEntity.get(EMX_ENTITIES_TAGS);

      List<String> tagIdentifiers = toList(emxAttrTags);
      if (tagIdentifiers != null && !tagIdentifiers.isEmpty()) {
        attr.setTags(toTags(intermediateResults, tagIdentifiers));
      }

      if (emxAttrNillable != null) {
        if (emxAttrNillable.equalsIgnoreCase(TRUE.toString())
            || emxAttrNillable.equalsIgnoreCase(FALSE.toString())) {
          attr.setNillable(parseBoolean(emxAttrNillable, rowIndex, EMX_ATTRIBUTES_NILLABLE));
        } else {
          attr.setNullableExpression(emxAttrNillable);
        }
      }
      if (emxIdAttrValue != null) {
        if (!emxIdAttrValue.equalsIgnoreCase(TRUE.toString())
            && !emxIdAttrValue.equalsIgnoreCase(FALSE.toString())
            && !emxIdAttrValue.equalsIgnoreCase(AUTO)) {
          throw new IllegalArgumentException(
              format(
                  "Attributes error on line [%d]. Illegal idAttribute value. Allowed values are 'TRUE', 'FALSE' or 'AUTO'",
                  rowIndex));
        }
        if (emxIdAttrValue.equalsIgnoreCase("true") && !isIdAttributeTypeAllowed(attr)) {
          throw new MolgenisDataException(
              "Identifier is of type ["
                  + attr.getDataType()
                  + "]. Id attributes can only be of type 'STRING', 'INT' or 'LONG'");
        }

        attr.setAuto(emxIdAttrValue.equalsIgnoreCase(AUTO));
        if (!attr.isAuto())
          emxAttr.setIdAttr(parseBoolean(emxIdAttrValue, rowIndex, EMX_ATTRIBUTES_ID_ATTRIBUTE));
        else emxAttr.setIdAttr(true); // If it is auto, set idAttr to true
      }

      if (attr.isAuto() && !isStringType(attr)) {
        throw new IllegalArgumentException(
            format(
                "Attributes error on line [%d]. Auto attributes can only be of data type 'string'",
                rowIndex));
      }
      if (emxAttrVisible != null) {
        if (emxAttrVisible.equalsIgnoreCase("true") || emxAttrVisible.equalsIgnoreCase("false")) {
          attr.setVisible(parseBoolean(emxAttrVisible, rowIndex, EMX_ATTRIBUTES_VISIBLE));
        } else {
          attr.setVisibleExpression(emxAttrVisible);
        }
      }
      if (emxAggregatable != null)
        attr.setAggregatable(parseBoolean(emxAggregatable, rowIndex, EMX_ATTRIBUTES_AGGREGATEABLE));
      if (emxReadOnly != null)
        attr.setReadOnly(parseBoolean(emxReadOnly, rowIndex, EMX_ATTRIBUTES_READ_ONLY));
      if (emxUnique != null)
        attr.setUnique(parseBoolean(emxUnique, rowIndex, EMX_ATTRIBUTES_UNIQUE));
      if (expression != null) attr.setExpression(expression);
      if (validationExpression != null) attr.setValidationExpression(validationExpression);
      if (defaultValue != null) attr.setDefaultValue(defaultValue);
      if (emxIsLookupAttr != null) {
        boolean isLookAttr =
            parseBoolean(emxIsLookupAttr, rowIndex, EMX_ATTRIBUTES_LOOKUP_ATTRIBUTE);
        if (isLookAttr && isReferenceType(attr)) {
          throw new IllegalArgumentException(
              format(
                  "attributes.lookupAttribute error on line [%d] (%s.%s) lookupAttribute cannot be of type %s",
                  rowIndex, emxEntityName, emxName, attr.getDataType().toString()));
        }

        emxAttr.setLookupAttr(isLookAttr);
      }

      if (emxIsLabelAttr != null) {
        boolean isLabelAttr =
            parseBoolean(emxIsLabelAttr, rowIndex, EMX_ATTRIBUTES_LABEL_ATTRIBUTE);
        if (isLabelAttr && isReferenceType(attr)) {
          throw new IllegalArgumentException(
              format(
                  "attributes.labelAttribute error on line [%d] (%s.%s): labelAttribute cannot be of type %s",
                  rowIndex, emxEntityName, emxName, attr.getDataType().toString()));
        }

        emxAttr.setLabelAttr(isLabelAttr);
      }

      attr.setLabel(emxAttrEntity.getString(EMX_ATTRIBUTES_LABEL));

      for (String attrName : emxAttrEntity.getAttributeNames()) {
        if (isI18n(attrName)) {
          if (attrName.startsWith(EMX_ATTRIBUTES_LABEL)) {
            String label = emxAttrEntity.getString(attrName);
            if (label != null) {
              String languageCode = getLanguageCode(attrName);
              attr.setLabel(languageCode, label);
            }
          } else if (attrName.startsWith(EMX_ATTRIBUTES_DESCRIPTION)) {
            String description = emxAttrEntity.getString(attrName);
            if (description != null) {
              String languageCode = getLanguageCode(attrName);
              attr.setDescription(languageCode, description);
            }
          }
        }
      }

      attr.setDescription(emxAttrEntity.getString(EMX_ATTRIBUTES_DESCRIPTION));

      if (attr.getDataType() == ENUM) {
        List<String> enumOptions =
            DataConverter.toList(emxAttrEntity.get(EMX_ATTRIBUTES_ENUM_OPTIONS));
        if (enumOptions == null || enumOptions.isEmpty()) {
          throw new IllegalArgumentException(
              format(
                  "Missing enum options for attribute [%s] of entity [%s]",
                  attr.getName(), emxEntityName));
        }
        attr.setEnumOptions(enumOptions);
      }

      checkReferenceDatatypeRules(rowIndex, emxEntityName, emxName, attr, emxRefEntity);

      String emxRangeMin = emxAttrEntity.getString(EMX_ATTRIBUTES_RANGE_MIN);
      Long rangeMin;
      if (emxRangeMin != null) {
        try {
          rangeMin = Long.valueOf(emxRangeMin);
        } catch (NumberFormatException e) {
          throw new MolgenisDataException(
              format(
                  "Invalid range rangeMin [%s] value for attribute [%s] of entity [%s], should be a long",
                  emxRangeMin, emxName, emxEntityName));
        }
      } else {
        rangeMin = null;
      }

      String emxRangeMax = emxAttrEntity.getString(EMX_ATTRIBUTES_RANGE_MAX);
      Long rangeMax;
      if (emxRangeMax != null) {
        try {
          rangeMax = Long.valueOf(emxRangeMax);
        } catch (NumberFormatException e) {
          throw new MolgenisDataException(
              format(
                  "Invalid range rangeMax [%s] value for attribute [%s] of entity [%s], should be a long",
                  emxRangeMax, emxName, emxEntityName));
        }
      } else {
        rangeMax = null;
      }

      if (rangeMin != null || rangeMax != null) {
        attr.setRange(new Range(rangeMin, rangeMax));
      }
    }

    // 3rd pass: validate and create attribute relationships
    Map<String, Set<String>> rootAttributes = newLinkedHashMap();
    rowIndex = 1; // Header
    for (Entity attributeEntity : attributesRepo) {

      rowIndex++;

      String entityTypeId = attributeEntity.getString(EMX_ATTRIBUTES_ENTITY);
      Map<String, EmxAttribute> entityMap = attributesMap.get(entityTypeId);

      String attributeName = attributeEntity.getString(EMX_ATTRIBUTES_NAME);
      Attribute attribute = entityMap.get(attributeName).getAttr();

      // bootstrap attribute parent-children relations for compound attributes
      String partOfAttribute = attributeEntity.getString(EMX_ATTRIBUTES_PART_OF_ATTRIBUTE);
      if (partOfAttribute != null && !partOfAttribute.isEmpty()) {
        EmxAttribute emxCompoundAttribute = entityMap.get(partOfAttribute);
        if (emxCompoundAttribute == null) {
          throw new IllegalArgumentException(
              format(
                  "partOfAttribute [%s] of attribute [%s] of entity [%s] must refer to an existing compound attribute on line %d",
                  partOfAttribute, attributeName, entityTypeId, rowIndex));
        }
        Attribute compoundAttribute = emxCompoundAttribute.getAttr();

        if (compoundAttribute.getDataType() != COMPOUND) {
          throw new IllegalArgumentException(
              format(
                  "partOfAttribute [%s] of attribute [%s] of entity [%s] must refer to a attribute of type [%s] on line %d",
                  partOfAttribute, attributeName, entityTypeId, COMPOUND, rowIndex));
        }

        if (attributeName.equals(partOfAttribute)) {
          throw new IllegalArgumentException(
              format(
                  "partOfAttribute [%s] of attribute [%s] of entity [%s] cannot refer to itself on line %d",
                  partOfAttribute, attributeName, entityTypeId, rowIndex));
        }
        attribute.setParent(compoundAttribute);
      }

      Set<String> entityRootAttributes =
          rootAttributes.computeIfAbsent(entityTypeId, k -> new LinkedHashSet<>());
      entityRootAttributes.add(attributeName);
    }

    // store attributes with entities
    for (Map.Entry<String, Map<String, EmxAttribute>> entry : attributesMap.entrySet()) {

      String entityTypeId = entry.getKey();
      Map<String, EmxAttribute> attributes = entry.getValue();

      List<EmxAttribute> editableEntityType = newArrayList();
      // add root attributes to entity
      Set<String> entityAttributeNames = rootAttributes.get(entityTypeId);
      if (entityAttributeNames != null) {
        for (EmxAttribute attribute : attributes.values()) {
          if (entityAttributeNames.contains(attribute.getAttr().getName())) {
            editableEntityType.add(attribute);
          }
        }
      }

      intermediateResults.addAttributes(entityTypeId, editableEntityType);
    }
  }

  protected void checkReferenceDatatypeRules(
      int rowIndex, String emxEntityName, String emxName, Attribute attr, String emxRefEntity) {
    // Only if an attribute is not of type file we apply the normal reference rules
    if (attr.getDataType() != FILE
        && isReferenceType(attr)
        && Strings.isNullOrEmpty(emxRefEntity)) {
      throw new IllegalArgumentException(
          format("Missing refEntity on line [%d] (%s.%s)", rowIndex, emxEntityName, emxName));
    }

    if (isReferenceType(attr) && attr.isNillable() && attr.isAggregatable()) {
      throw new IllegalArgumentException(
          format(
              "attributes.isAggregatable error on line [%d] (%s.%s): isAggregatable nillable attribute cannot be of type %s",
              rowIndex, emxEntityName, emxName, attr.getDataType().toString()));
    }
  }

  protected void checkRequiredAttributeAttributes(
      int rowIndex, String attributeName, String entityTypeId) {
    if (Strings.isNullOrEmpty(attributeName)) {
      throw new IllegalArgumentException(
          format("attributes.name is missing on line [%d]", rowIndex));
    }
    if (Strings.isNullOrEmpty(entityTypeId)) {
      throw new IllegalArgumentException(
          format(
              "attributes.entity is missing for attribute named: %s on line [%d]",
              attributeName, rowIndex));
    }
  }

  /**
   * re-iterate to map the mrefs/xref refEntity (or give error if not found)
   *
   * @param attributeRepo the attributes {@link Repository}
   * @param intermediateResults {@link ParsedMetaData} to add the ref entities to
   */
  private void reiterateToMapRefEntity(
      Repository<Entity> attributeRepo, IntermediateParseResults intermediateResults) {
    int rowIndex = 1;
    for (Entity attributeEntity : attributeRepo) {
      final String entityTypeId = attributeEntity.getString(EMX_ATTRIBUTES_ENTITY);
      final String attributeName = attributeEntity.getString(EMX_ATTRIBUTES_NAME);
      final String refEntityName = (String) attributeEntity.get(EMX_ATTRIBUTES_REF_ENTITY);
      final String mappedByAttrName = (String) attributeEntity.get(EMX_ATTRIBUTES_MAPPED_BY);
      EntityType entityType = intermediateResults.getEntityType(entityTypeId);
      Attribute attribute = entityType.getAttribute(attributeName);

      if (attribute.getDataType().equals(FILE)) {
        // If attribute is of type file, set refEntity to file meta and continue to the next
        // attribute
        attribute.setRefEntity(dataService.getEntityType(FILE_META));
        continue;
      }

      rowIndex++;
      if (refEntityName != null) {
        EntityType refEntityType;
        if (intermediateResults.knowsEntity(refEntityName)) {
          refEntityType = intermediateResults.getEntityType(refEntityName);
        } else {
          if (dataService.hasEntityType(refEntityName)) {
            refEntityType = dataService.getEntityType(refEntityName);
          } else {
            throw new IllegalArgumentException(
                "attributes.refEntity error on line "
                    + rowIndex
                    + ": "
                    + refEntityName
                    + " unknown");
          }
        }
        attribute.setRefEntity(refEntityType);

        if (mappedByAttrName != null) {
          Attribute mappedByAttr = refEntityType.getAttribute(mappedByAttrName);
          if (mappedByAttr == null) {
            throw new IllegalArgumentException(
                "attributes.mappedBy error on line "
                    + rowIndex
                    + ": "
                    + mappedByAttrName
                    + " unknown");
          }
          attribute.setMappedBy(mappedByAttr);
        }
      }
    }
  }

  /** Put the entities that are not in a package in the selected package */
  private List<EntityType> putEntitiesInDefaultPackage(
      IntermediateParseResults intermediateResults, String defaultPackageId) {
    Package p = getPackage(intermediateResults, defaultPackageId);
    if (p == null) {
      throw new IllegalArgumentException(format("Unknown package [%s]", defaultPackageId));
    }

    List<EntityType> entities = newArrayList();
    for (EntityType entityType : intermediateResults.getEntities()) {
      if (entityType.getPackage() == null) {
        entityType.setId(defaultPackageId + PACKAGE_SEPARATOR + entityType.getId());
        entityType.setPackage(p);
      }
      entities.add(entityType);
    }
    return entities;
  }

  /**
   * Retrieves a {@link Package} by name from parsed data or existing data.
   *
   * @param intermediateResults parsed data
   * @param packageId package name
   * @return package or <code>null</code> if no package with the given name exists in parsed or
   *     existing data
   */
  private Package getPackage(IntermediateParseResults intermediateResults, String packageId) {
    Package aPackage = intermediateResults.getPackage(packageId);
    if (aPackage == null) {
      aPackage = dataService.findOneById(PackageMetadata.PACKAGE, packageId, Package.class);
    }
    return aPackage;
  }

  private static ImmutableMap<String, EntityType> getEntityTypeFromDataService(
      DataService dataService, Iterable<String> emxEntityNames) {
    ImmutableMap.Builder<String, EntityType> builder = builder();
    emxEntityNames.forEach(
        emxName -> {
          String repoName = EMX_NAME_TO_REPO_NAME_MAP.get(emxName);
          if (repoName == null) repoName = emxName;
          builder.put(emxName, dataService.getRepository(repoName).getEntityType());
        });
    return builder.build();
  }

  /**
   * Validates whether an EMX value for a boolean attribute is valid and returns the parsed boolean
   * value.
   *
   * @param booleanString boolean string
   * @param rowIndex row index
   * @param columnName column name
   * @return true or false
   * @throws IllegalArgumentException if the given boolean string value is not one of [true, false]
   *     (case insensitive)
   */
  private static boolean parseBoolean(String booleanString, int rowIndex, String columnName) {
    if (booleanString.equalsIgnoreCase(TRUE.toString())) return true;
    else if (booleanString.equalsIgnoreCase(FALSE.toString())) return false;
    else
      throw new IllegalArgumentException(
          format(
              "attributes.[%s] error on line [%d]: Invalid value [%s] (expected true or false)",
              columnName, rowIndex, booleanString));
  }

  private void parseLanguages(
      Repository<Entity> emxLanguageRepo, IntermediateParseResults intermediateParseResults) {
    emxLanguageRepo.forEach(
        emxLanguageEntity -> {
          Language language = toLanguage(emxLanguageEntity);
          intermediateParseResults.addLanguage(language);
        });
  }

  /**
   * Creates a language entity from a EMX entity describing a language
   *
   * @param emxLanguageEntity EMX language entity
   * @return language entity
   */
  private Language toLanguage(Entity emxLanguageEntity) {
    Language language = languageFactory.create();
    language.setCode(emxLanguageEntity.getString(EMX_LANGUAGE_CODE));
    language.setName(emxLanguageEntity.getString(EMX_LANGUAGE_NAME));
    return language;
  }

  private void parseI18nStrings(
      Repository<Entity> emxI18nStringRepo, IntermediateParseResults intermediateParseResults) {
    emxI18nStringRepo.forEach(
        emxI18nStringEntity -> {
          L10nString l10nString = toL10nString(emxI18nStringEntity);
          intermediateParseResults.addL10nString(l10nString);
        });
  }

  private L10nString toL10nString(Entity emxI18nStringEntity) {
    L10nString l10nString = l10nStringFactory.create();
    l10nString.setMessageID(emxI18nStringEntity.getString(EMX_I18N_STRING_MSGID));
    l10nString.setDescription(emxI18nStringEntity.getString(EMX_I18N_STRING_DESCRIPTION));
    String namespace = emxI18nStringEntity.getString(EMX_I18N_STRING_NAMESPACE);
    if (namespace == null) {
      namespace = DEFAULT_NAMESPACE;
    }
    l10nString.setNamespace(namespace);

    LanguageService.getLanguageCodes()
        .forEach(lang -> l10nString.set(lang, emxI18nStringEntity.getString(lang)));
    return l10nString;
  }

  /**
   * Goes through all the sheets in the source EMX and creates an {@link MyEntitiesValidationReport}
   */
  private MyEntitiesValidationReport generateEntityValidationReport(
      RepositoryCollection source,
      MyEntitiesValidationReport report,
      Map<String, EntityType> metaDataMap) {
    for (String sheet : source.getEntityTypeIds()) {
      if (EMX_PACKAGES.equals(sheet)) {
        IntermediateParseResults parseResult = parseTagsSheet(source.getRepository(EMX_TAGS));
        parsePackagesSheet(source.getRepository(sheet), parseResult);
        parseResult.getPackages().keySet().forEach(report::addPackage);
      } else if (!EMX_ENTITIES.equals(sheet)
          && !EMX_ATTRIBUTES.equals(sheet)
          && !EMX_TAGS.equals(sheet)
          && !EMX_LANGUAGES.equals(sheet)
          && !EMX_I18NSTRINGS.equals(sheet)) {
        // check if sheet is known
        report = report.addEntity(sheet, metaDataMap.containsKey(sheet));

        // check the fields
        Repository<Entity> sourceRepository = source.getRepository(sheet);
        EntityType target = metaDataMap.get(sheet);

        if (target != null) {
          for (Attribute att : sourceRepository.getEntityType().getAttributes()) {
            Attribute attribute = target.getAttribute(att.getName());

            // Bi-directional one-to-many attributes mapped by another attribute can only be
            // imported
            // through the owning side: one-to-many attribute 'books' of entity 'author' mapped by
            // attribute
            //  'author' of entity 'book' cannot be imported, only the owning attribute 'book' can
            // be
            // imported.
            boolean known =
                attribute != null
                    && attribute.getExpression() == null
                    && !(attribute.getDataType() == ONE_TO_MANY && attribute.isMappedBy());
            report = report.addAttribute(att.getName(), known ? IMPORTABLE : UNKNOWN);
          }
          for (Attribute att : target.getAttributes()) {
            // See comment above regarding import of bi-directional one-to-many data
            if (att.getDataType() != COMPOUND
                && !(att.getDataType() == ONE_TO_MANY && att.isMappedBy())
                && !att.isAuto()
                && att.getExpression() == null
                && !report.getFieldsImportable().get(sheet).contains(att.getName())) {
              boolean required = !att.isNillable() && !att.isAuto();
              report = report.addAttribute(att.getName(), required ? REQUIRED : AVAILABLE);
            }
          }
        }
      }
    }
    return report;
  }
}
