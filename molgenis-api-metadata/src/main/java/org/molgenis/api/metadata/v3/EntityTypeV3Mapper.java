package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static org.molgenis.api.PageUtils.getPageResponse;
import static org.molgenis.api.data.v3.EntityController.API_ENTITY_PATH;
import static org.molgenis.data.util.EntityTypeUtils.isReferenceType;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.molgenis.api.metadata.v3.exception.EmptyAttributesException;
import org.molgenis.api.metadata.v3.exception.InvalidKeyException;
import org.molgenis.api.metadata.v3.exception.ReadOnlyFieldException;
import org.molgenis.api.metadata.v3.exception.UnsupportedFieldException;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypeResponseData;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.PackageResponse;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.support.LinksUtils;
import org.molgenis.data.InvalidValueTypeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageMetadata;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
class EntityTypeV3Mapper {

  private static final String ATTRIBUTES = "attributes";
  private final EntityTypeFactory entityTypeFactory;
  private final AttributeV3Mapper attributeV3Mapper;
  private final MetaDataService metaDataService;

  EntityTypeV3Mapper(
      EntityTypeFactory entityTypeFactory,
      AttributeV3Mapper attributeV3Mapper,
      MetaDataService metaDataService) {
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attributeV3Mapper = requireNonNull(attributeV3Mapper);
    this.metaDataService = requireNonNull(metaDataService);
  }

  EntityTypesResponse toEntityTypesResponse(
      EntityTypes entityTypes, int pageSize, int pageNumber, int totalElements) {

    List<EntityTypeResponse> results = new ArrayList<>();
    for (EntityType entityType : entityTypes.getEntityTypes()) {
      results.add(mapInternal(entityType, false, true, false, false));
    }

    return EntityTypesResponse.create(
        LinksUtils.createLinksResponse(pageNumber, pageSize, totalElements),
        results,
        getPageResponse(
            entityTypes.getEntityTypes().size(), pageNumber * pageSize, totalElements, pageSize));
  }

  EntityTypeResponse toEntityTypeResponse(
      EntityType entityType, boolean flattenAttrs, boolean i18n) {
    return mapInternal(entityType, flattenAttrs, true, true, i18n);
  }

  EntityType toEntityType(CreateEntityTypeRequest entityTypeRequest) {
    EntityType entityType = entityTypeFactory.create();
    String entityTypeId = entityTypeRequest.getId();
    if (entityTypeId != null) {
      entityType.setId(entityTypeId);
    }
    String packageId = entityTypeRequest.getPackage();
    Package pack = null;
    if (packageId != null) {
      pack =
          metaDataService
              .getPackage(packageId)
              .orElseThrow(() -> new UnknownPackageException(entityTypeRequest.getPackage()));
    }
    entityType.setPackage(pack);
    String extendsEntityTypeId = entityTypeRequest.getExtends();
    if (extendsEntityTypeId != null) {
      EntityType extendsEntityType =
          metaDataService
              .getEntityType(extendsEntityTypeId)
              .orElseThrow(() -> new UnknownEntityTypeException(extendsEntityTypeId));
      entityType.setExtends(extendsEntityType);
    }

    processI18nLabel(entityTypeRequest.getLabel(), entityType);
    processI18nDescription(entityTypeRequest.getDescription(), entityType);
    List<Attribute> ownAttributes =
        attributeV3Mapper.toAttributes(
            entityTypeRequest.getAttributes(), entityTypeRequest, entityType);
    entityType.setOwnAllAttributes(ownAttributes);
    Boolean abstractEntityType = entityTypeRequest.getAbstract();
    if (abstractEntityType != null) {
      entityType.setAbstract(abstractEntityType);
    }
    entityType.setBackend(metaDataService.getDefaultBackend().getName());

    processSelfReferencingAttributes(entityType, ownAttributes);

    return entityType;
  }

  private void processSelfReferencingAttributes(
      EntityType entityType, Collection<Attribute> attributes) {
    for (Attribute attribute : attributes) {
      if (isReferenceType(attribute)
          && attribute.getRefEntity().getId().equals(entityType.getId())) {
        attribute.setRefEntity(entityType);
      }
    }
  }

  private EntityTypeResponse mapInternal(
      EntityType entityType,
      boolean flattenAttrs,
      boolean includeData,
      boolean expandAttrs,
      boolean i18n) {
    EntityTypeResponse.Builder entityTypeResponseBuilder = EntityTypeResponse.builder();
    entityTypeResponseBuilder.setLinks(
        LinksResponse.create(null, createEntityTypeResponseUri(entityType), null));

    if (includeData) {
      EntityTypeResponseData.Builder builder = EntityTypeResponseData.builder();
      builder.setId(entityType.getId());
      Package pack = entityType.getPackage();
      if (pack != null) {
        builder.setPackage(
            PackageResponse.builder()
                .setLinks(LinksResponse.create(null, createPackageResponseUri(pack), null))
                .build());
      }
      builder.setLabel(entityType.getLabel(LocaleContextHolder.getLocale().getLanguage()));
      builder.setDescription(
          entityType.getDescription(LocaleContextHolder.getLocale().getLanguage()));
      if (i18n) {
        builder.setLabelI18n(getI18nEntityTypeLabel(entityType));
        getI18nEntityTypeDesc(entityType).ifPresent(builder::setDescriptionI18n);
      }
      AttributesResponse.Builder attributesResponseBuilder =
          AttributesResponse.builder()
              .setLinks(LinksResponse.create(null, createAttributesResponseUri(entityType), null));
      if (expandAttrs) {
        attributesResponseBuilder.setItems(
            flattenAttrs
                ? attributeV3Mapper.mapInternal(entityType.getAllAttributes(), i18n)
                : attributeV3Mapper.mapInternal(entityType.getOwnAllAttributes(), i18n));
      }
      builder.setAttributes(attributesResponseBuilder.build());
      builder.setAbstract(entityType.isAbstract());
      EntityType parent = entityType.getExtends();
      builder.setExtends(parent != null ? mapInternal(parent, false, false, false, i18n) : null);
      builder.setIndexingDepth(entityType.getIndexingDepth());
      entityTypeResponseBuilder.setData(builder.build());
    }

    return entityTypeResponseBuilder.build();
  }

  private URI createEntityTypeResponseUri(EntityType entityType) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(MetadataApiController.API_META_PATH)
            .pathSegment(entityType.getId());
    return uriComponentsBuilder.build().toUri();
  }

  private URI createPackageResponseUri(Package aPackage) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(API_ENTITY_PATH)
            .pathSegment(PackageMetadata.PACKAGE)
            .pathSegment(aPackage.getId());
    return uriComponentsBuilder.build().toUri();
  }

  private URI createAttributesResponseUri(EntityType entityType) {
    UriComponentsBuilder uriComponentsBuilder =
        fromCurrentRequestUri()
            .replacePath(null)
            .path(MetadataApiController.API_META_PATH)
            .pathSegment(entityType.getId())
            .pathSegment(ATTRIBUTES);
    return uriComponentsBuilder.build().toUri();
  }

  private void processI18nLabel(I18nValue i18nValue, EntityType entityType) {
    if (i18nValue != null) {
      entityType.setLabel(i18nValue.getDefaultValue());
      getLanguageCodes()
          .forEach(
              languageCode -> {
                Map<String, String> translations = i18nValue.getTranslations();
                if (translations != null) {
                  entityType.setLabel(languageCode, translations.get(languageCode));
                }
              });
    }
  }

  private void processI18nDescription(I18nValue i18nValue, EntityType entityType) {
    if (i18nValue != null) {
      entityType.setDescription(i18nValue.getDefaultValue());
      getLanguageCodes()
          .forEach(
              languageCode -> {
                Map<String, String> translations = i18nValue.getTranslations();
                if (translations != null) {
                  entityType.setDescription(languageCode, translations.get(languageCode));
                }
              });
    }
  }

  private I18nValue getI18nEntityTypeLabel(EntityType entityType) {
    String defaultValue = entityType.getLabel();
    ImmutableMap<String, String> translations =
        MetadataUtils.getI18n(entityType, EntityTypeMetadata.LABEL);
    return I18nValue.create(defaultValue, translations);
  }

  private Optional<I18nValue> getI18nEntityTypeDesc(EntityType entityType) {
    String defaultValue = entityType.getDescription();
    if (defaultValue == null) {
      return Optional.empty();
    }
    ImmutableMap<String, String> translations =
        MetadataUtils.getI18n(entityType, EntityTypeMetadata.DESCRIPTION);
    return Optional.of(I18nValue.create(defaultValue, translations));
  }

  public void toEntityType(EntityType entityType, Map<String, Object> entityTypeValues) {
    for (Entry<String, Object> entry : entityTypeValues.entrySet()) {
      if (entry.getValue() == null) {
        entityType.set(entry.getKey(), null);
      } else {
        updateEntityType(entityType, entry);
      }
    }
  }

  @SuppressWarnings({
    "squid:S1192"
  }) // ATTRIBUTES constant in this class is not related to the one in this switch
  private void updateEntityType(EntityType entityType, Entry<String, Object> entry) {
    switch (entry.getKey()) {
      case "package":
        String packageId = String.valueOf(entry.getValue());
        Package pack =
            metaDataService
                .getPackage(packageId)
                .orElseThrow(() -> new UnknownPackageException(packageId));
        entityType.setPackage(pack);
        break;
      case "id":
      case "abstract":
        throw new ReadOnlyFieldException(entry.getKey(), "entityType");
      case "extends":
        String extendsValue = String.valueOf(entry.getValue());
        EntityType parent =
            metaDataService
                .getEntityType(extendsValue)
                .orElseThrow(() -> new UnknownEntityTypeException(extendsValue));
        entityType.setExtends(parent);
        break;
      case "label":
        I18nValue label = attributeV3Mapper.mapI18nValue(entry.getValue());
        processI18nLabel(label, entityType);
        break;
      case "description":
        I18nValue description = attributeV3Mapper.mapI18nValue(entry.getValue());
        processI18nDescription(description, entityType);
        break;
      case "attributes":
        if (entry.getValue() != null) {
          Iterable<Attribute> attributes = mapAttributes(entityType, entry);
          entityType.setOwnAllAttributes(attributes);
        } else {
          throw new EmptyAttributesException();
        }
        break;
      case "tags":
      case "backend":
        throw new UnsupportedFieldException(entry.getKey());
      default:
        throw new InvalidKeyException("entityType", entry.getKey());
    }
  }

  private Iterable<Attribute> mapAttributes(EntityType entityType, Entry<String, Object> entry) {
    if (!(entry.getValue() instanceof Iterable<?>)) {
      throw new InvalidValueTypeException(entry.getValue().toString(), "list", null);
    }
    List<Object> attrValues = (List<Object>) entry.getValue();
    List<Map<String, Object>> requestAttributes = new ArrayList<>();
    for (Object attrValue : attrValues) {
      Map<String, Object> valueMap = (Map<String, Object>) attrValue;
      requestAttributes.add(valueMap);
    }
    return mapAttributes(requestAttributes, entityType);
  }

  private Iterable<Attribute> mapAttributes(
      List<Map<String, Object>> values, EntityType entityType) {
    return attributeV3Mapper.toAttributes(values, entityType).values();
  }
}
