package org.molgenis.api.metadata.v3.job;

import static com.google.common.collect.Streams.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.meta.AttributeType.getValueString;
import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.molgenis.api.metadata.v3.job.util.ImmutableListDeserializer;
import org.molgenis.api.metadata.v3.job.util.ImmutableMapDeserializer;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.model.TagMetadata;
import org.molgenis.web.converter.AutoValueTypeAdapterFactory;
import org.springframework.stereotype.Component;

@Component
public class EntityTypeSerializerImpl implements EntityTypeSerializer {

  private final DataService dataService;
  private final EntityTypeFactory entityTypeFactory;
  private final AttributeFactory attributeFactory;
  private final Gson gson;

  EntityTypeSerializerImpl(
      DataService dataService,
      EntityTypeFactory entityTypeFactory,
      AttributeFactory attributeFactory) {
    this.dataService = requireNonNull(dataService);
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attributeFactory = requireNonNull(attributeFactory);
    gson =
        new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueTypeAdapterFactory())
              .registerTypeAdapter(ImmutableList.class, new ImmutableListDeserializer())
              .registerTypeAdapter(ImmutableMap.class, new ImmutableMapDeserializer())
            .create();
  }

  @Override
  public String serializeEntityType(EntityType entityType) {
    SerializableEntityType serializableEntityType = toSerializableEntityType(entityType);
    return gson.toJson(serializableEntityType);
  }

  @Override
  public EntityType deserializeEntityType(String serializedEntityType) {
    SerializableEntityType serializableEntityType =
        gson.fromJson(serializedEntityType, SerializableEntityType.class);
    return toEntityType(serializableEntityType);
  }

  private static SerializableEntityType toSerializableEntityType(EntityType entityType) {
    Package aPackage = entityType.getPackage();
    String packageId = aPackage != null ? aPackage.getId() : null;
    EntityType anExtends = entityType.getExtends();
    String extendsId = anExtends != null ? anExtends.getId() : null;

    return SerializableEntityType.builder()
        .setId(entityType.getId())
        .setPackageId(packageId)
        .setLabel(entityType.getLabel())
        .setLabelI18n(
            ImmutableMap.copyOf(
                getLanguageCodes()
                    .filter(
                        languageCode ->
                            entityType.getString(
                                    getI18nAttributeName(EntityTypeMetadata.LABEL, languageCode))
                                != null)
                    .collect(
                        toMap(
                            Function.identity(),
                            languageCode ->
                                entityType.getString(
                                    getI18nAttributeName(
                                        EntityTypeMetadata.LABEL, languageCode))))))
        .setDescription(entityType.getDescription())
        .setDescriptionI18n(
            ImmutableMap.copyOf(
                getLanguageCodes()
                    .filter(
                        languageCode ->
                            entityType.getString(
                                    getI18nAttributeName(
                                        EntityTypeMetadata.DESCRIPTION, languageCode))
                                != null)
                    .collect(
                        toMap(
                            Function.identity(),
                            languageCode ->
                                entityType.getString(
                                    getI18nAttributeName(
                                        EntityTypeMetadata.DESCRIPTION, languageCode))))))
        .setAttributes(
            ImmutableList.copyOf(
                stream(entityType.getOwnAllAttributes())
                    .map(EntityTypeSerializerImpl::toSerializableAttribute)
                    .collect(toList())))
        .setAbstract(entityType.isAbstract())
        .setExtendsId(extendsId)
        .setTagIds(
            ImmutableList.copyOf(stream(entityType.getTags()).map(Tag::getId).collect(toList())))
        .setBackend(entityType.getBackend())
        .setIndexingDepth(entityType.getIndexingDepth())
        .build();
  }

  private static SerializableAttribute toSerializableAttribute(Attribute attribute) {
    EntityType refEntityType = attribute.hasRefEntity() ? attribute.getRefEntity() : null;
    String refEntityTypeId = refEntityType != null ? refEntityType.getId() : null;
    Attribute mappedBy = attribute.getMappedBy();
    String mappedById = mappedBy != null ? mappedBy.getIdentifier() : null;
    Sort sort = attribute.getOrderBy();
    String orderBy = sort != null ? sort.toSortString() : null;
    Attribute parent = attribute.getParent();
    String parentId = parent != null ? parent.getIdentifier() : null;
    Boolean cascadeDelete = attribute.getCascadeDelete();
    boolean isCascadeDelete = cascadeDelete != null ? cascadeDelete : false;

    return SerializableAttribute.builder()
        .setId(attribute.getIdentifier())
        .setName(attribute.getName())
        .setSequenceNr(attribute.getSequenceNumber())
        .setType(getValueString(attribute.getDataType()))
        .setIdAttribute(attribute.isIdAttribute())
        .setLabelAttribute(attribute.isLabelAttribute())
        .setLookupAttributeIndex(attribute.getLookupAttributeIndex())
        .setRefEntityTypeId(refEntityTypeId)
        .setCascadeDelete(isCascadeDelete)
        .setMappedById(mappedById)
        .setOrderBy(orderBy)
        .setLabel(attribute.getLabel())
        .setLabelI18n(
            ImmutableMap.copyOf(
                getLanguageCodes()
                    .filter(
                        languageCode ->
                            attribute.getString(
                                    getI18nAttributeName(EntityTypeMetadata.LABEL, languageCode))
                                != null)
                    .collect(
                        toMap(
                            Function.identity(),
                            languageCode ->
                                attribute.getString(
                                    getI18nAttributeName(
                                        EntityTypeMetadata.LABEL, languageCode))))))
        .setDescription(attribute.getDescription())
        .setDescriptionI18n(
            ImmutableMap.copyOf(
                getLanguageCodes()
                    .filter(
                        languageCode ->
                            attribute.getString(
                                    getI18nAttributeName(
                                        EntityTypeMetadata.DESCRIPTION, languageCode))
                                != null)
                    .collect(
                        toMap(
                            Function.identity(),
                            languageCode ->
                                attribute.getString(
                                    getI18nAttributeName(
                                        EntityTypeMetadata.DESCRIPTION, languageCode))))))
        .setNullable(attribute.isNillable())
        .setAuto(attribute.isAuto())
        .setVisible(attribute.isVisible())
        .setUnique(attribute.isUnique())
        .setReadOnly(attribute.isReadOnly())
        .setAggregatable(attribute.isAggregatable())
        .setExpression(attribute.getExpression())
        .setEnumOptions(ImmutableList.copyOf(attribute.getEnumOptions()))
        .setRangeMin(attribute.getRangeMin())
        .setRangeMax(attribute.getRangeMax())
        .setParentId(parentId)
        .setTagIds(
            ImmutableList.copyOf(stream(attribute.getTags()).map(Tag::getId).collect(toList())))
        .setNullableExpression(attribute.getNullableExpression())
        .setVisibleExpression(attribute.getVisibleExpression())
        .setValidationExpression(attribute.getValidationExpression())
        .setDefaultValue(attribute.getDefaultValue())
        .build();
  }

  private EntityType toEntityType(SerializableEntityType serializableEntityType) {
    String packageId = serializableEntityType.getPackageId();
    Package aPackage =
        packageId != null
            ? dataService.findOneById(
                PackageMetadata.PACKAGE,
                packageId,
                new Fetch().field(PackageMetadata.ID),
                Package.class)
            : null;
    String extendsId = serializableEntityType.getExtendsId();
    EntityType anExtends =
        extendsId != null
            ? dataService.findOneById(
                EntityTypeMetadata.ENTITY_TYPE_META_DATA,
                extendsId,
                new Fetch().field(EntityTypeMetadata.ID),
                EntityType.class)
            : null;
    ImmutableList<String> tagIds = serializableEntityType.getTagIds();
    List<Tag> tags =
        !tagIds.isEmpty()
            ? dataService
                .findAll(
                    TagMetadata.TAG,
                    (Stream<Object>) (Stream<?>) tagIds.stream(),
                    new Fetch().field(TagMetadata.ID),
                    Tag.class)
                .collect(toList())
            : emptyList();

    EntityType entityType = entityTypeFactory.create();
    entityType.setId(serializableEntityType.getId());
    entityType.setPackage(aPackage);
    entityType.setLabel(serializableEntityType.getLabel());
    serializableEntityType.getLabelI18n().forEach(entityType::setLabel);
    entityType.setDescription(serializableEntityType.getDescription());
    serializableEntityType.getDescriptionI18n().forEach(entityType::setDescription);
    entityType.setOwnAllAttributes(
        serializableEntityType.getAttributes().stream().map(this::toAttribute).collect(toList()));
    entityType.setAbstract(serializableEntityType.isAbstract());
    entityType.setExtends(anExtends);
    entityType.setTags(tags);
    entityType.setBackend(serializableEntityType.getBackend());
    entityType.setIndexingDepth(serializableEntityType.getIndexingDepth());
    return entityType;
  }

  private Attribute toAttribute(SerializableAttribute serializableAttribute) {
    String refEntityTypeId = serializableAttribute.getRefEntityTypeId();
    EntityType refEntityType =
        refEntityTypeId != null
            ? dataService.findOneById(
                EntityTypeMetadata.ENTITY_TYPE_META_DATA,
                refEntityTypeId,
                new Fetch().field(EntityTypeMetadata.ID),
                EntityType.class)
            : null;
    String mappedById = serializableAttribute.getMappedById();
    Attribute mappedByAttr =
        mappedById != null
            ? dataService.findOneById(
                AttributeMetadata.ATTRIBUTE_META_DATA,
                mappedById,
                new Fetch().field(AttributeMetadata.ID),
                Attribute.class)
            : null;
    String orderBy = serializableAttribute.getOrderBy();
    Sort sort = orderBy != null ? Sort.parse(orderBy) : null;
    String parentId = serializableAttribute.getParentId();
    Attribute parent =
        parentId != null
            ? dataService.findOneById(
                AttributeMetadata.ATTRIBUTE_META_DATA,
                parentId,
                new Fetch().field(AttributeMetadata.ID),
                Attribute.class)
            : null;

    ImmutableList<String> tagIds = serializableAttribute.getTagIds();
    List<Tag> tags =
        !tagIds.isEmpty()
            ? dataService
                .findAll(
                    TagMetadata.TAG,
                    (Stream<Object>) (Stream<?>) tagIds.stream(),
                    new Fetch().field(TagMetadata.ID),
                    Tag.class)
                .collect(toList())
            : emptyList();

    Attribute attribute = attributeFactory.create();
    attribute.setIdentifier(serializableAttribute.getId());
    attribute.setName(serializableAttribute.getName());
    attribute.setSequenceNumber(serializableAttribute.getSequenceNr());
    attribute.setDataType(AttributeType.toEnum(serializableAttribute.getType()));
    attribute.setIdAttribute(serializableAttribute.isIdAttribute());
    attribute.setLabelAttribute(serializableAttribute.isLabelAttribute());
    attribute.setLookupAttributeIndex(serializableAttribute.getLookupAttributeIndex());
    attribute.setRefEntity(refEntityType);
    attribute.setCascadeDelete(serializableAttribute.isCascadeDelete());
    attribute.setMappedBy(mappedByAttr);
    attribute.setOrderBy(sort);
    attribute.setLabel(serializableAttribute.getLabel());
    serializableAttribute.getLabelI18n().forEach(attribute::setLabel);
    attribute.setDescription(serializableAttribute.getDescription());
    serializableAttribute.getDescriptionI18n().forEach(attribute::setDescription);
    attribute.setNillable(serializableAttribute.isNullable());
    attribute.setAuto(serializableAttribute.isAuto());
    attribute.setVisible(serializableAttribute.isVisible());
    attribute.setUnique(serializableAttribute.isUnique());
    attribute.setReadOnly(serializableAttribute.isReadOnly());
    attribute.setAggregatable(serializableAttribute.isAggregatable());
    attribute.setExpression(serializableAttribute.getExpression());
    attribute.setEnumOptions(serializableAttribute.getEnumOptions());
    attribute.setRangeMin(serializableAttribute.getRangeMin());
    attribute.setRangeMax(serializableAttribute.getRangeMax());
    attribute.setParent(parent);
    attribute.setTags(tags);
    attribute.setNullableExpression(serializableAttribute.getNullableExpression());
    attribute.setVisibleExpression(serializableAttribute.getVisibleExpression());
    attribute.setValidationExpression(serializableAttribute.getValidationExpression());
    attribute.setDefaultValue(serializableAttribute.getDefaultValue());
    return attribute;
  }
}
