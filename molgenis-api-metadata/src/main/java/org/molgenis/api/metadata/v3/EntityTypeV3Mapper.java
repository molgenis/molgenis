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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypeResponseData;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.PackageResponse;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.api.support.MolgenisServletUriComponentsBuilder;
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
public class EntityTypeV3Mapper {

  public static final String ATTRIBUTES = "attributes";
  public static final String PAGE = "page";
  private final EntityTypeFactory entityTypeFactory;
  private final AttributeV3Mapper attributeV3Mapper;
  private final MetaDataService metaDataService;

  public EntityTypeV3Mapper(
      EntityTypeFactory entityTypeFactory,
      AttributeV3Mapper attributeV3Mapper,
      MetaDataService metaDataService) {
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attributeV3Mapper = requireNonNull(attributeV3Mapper);
    this.metaDataService = requireNonNull(metaDataService);
  }

  public EntityTypesResponse toEntityTypesResponse(
      EntityTypes entityTypes, int pagesize, int pagenumber, int totalElements) {

    List<EntityTypeResponse> results = new ArrayList<>();
    for (EntityType entityType : entityTypes.getEntityTypes()) {
      results.add(mapInternal(entityType, false, true, false, false));
    }

    return EntityTypesResponse.create(
        createLinksResponse(pagenumber, pagesize, totalElements),
        results,
        getPageResponse(
            entityTypes.getEntityTypes().size(), pagenumber * pagesize, totalElements, pagesize));
  }

  public EntityTypeResponse toEntityTypeResponse(
      EntityType entityType, boolean flattenAttrs, boolean i18n) {
    return mapInternal(entityType, flattenAttrs, true, true, i18n);
  }

  public EntityType toEntityType(CreateEntityTypeRequest entityTypeRequest) {
    EntityType entityType = entityTypeFactory.create();
    entityType.setId(entityTypeRequest.getId());
    String packageId = entityTypeRequest.getPackage();
    Optional<Package> pack =
        packageId != null ? metaDataService.getPackage(packageId) : Optional.empty();
    entityType.setPackage(
        pack.orElseThrow(() -> new UnknownPackageException(entityTypeRequest.getPackage())));
    String extendsEntityTypeId = entityTypeRequest.getExtends();
    if (extendsEntityTypeId != null) {
      EntityType extendsEntityType =
          metaDataService
              .getEntityType(extendsEntityTypeId)
              .orElseThrow(() -> new UnknownEntityTypeException(extendsEntityTypeId));
      entityType.setExtends(extendsEntityType);
    }

    processI18nLabel(entityTypeRequest, entityType);
    processI18nDescription(entityTypeRequest, entityType);
    Map<String, Attribute> ownAttributes =
        attributeV3Mapper.toAttributes(
            entityTypeRequest.getAttributes(), entityTypeRequest, entityType);
    String idAttribute = entityTypeRequest.getIdAttribute();
    if (idAttribute != null) {
      ownAttributes.get(idAttribute).setIdAttribute(true);
    }
    String labelAttribute = entityTypeRequest.getLabelAttribute();
    if (labelAttribute != null) {
      ownAttributes.get(labelAttribute).setLabelAttribute(true);
    }
    AtomicInteger count = new AtomicInteger();
    entityTypeRequest
        .getLookupAttributes()
        .forEach(
            lookupAttribute ->
                ownAttributes
                    .get(lookupAttribute)
                    .setLookupAttributeIndex(count.getAndIncrement()));
    entityType.setOwnAllAttributes(new ArrayList<>(ownAttributes.values()));
    entityType.setAbstract(entityTypeRequest.isAbstract());
    entityType.setBackend(metaDataService.getDefaultBackend().getName());

    processSelfReferencingAttributes(entityType, ownAttributes.values());

    return entityType;
  }

  private void processSelfReferencingAttributes(
      EntityType entityType, Collection<Attribute> attributes) {
    for (Attribute attribute : attributes) {
      if (isReferenceType(attribute)) {
        if (attribute.getRefEntity().getId().equals(entityType.getId())) {
          attribute.setRefEntity(entityType);
        }
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
        builder.setPackage_(
            PackageResponse.builder()
                .setLinks(LinksResponse.create(null, createPackageResponseUri(pack), null))
                .build());
      }
      builder.setLabel(entityType.getLabel(LocaleContextHolder.getLocale().getLanguage()));
      builder.setDescription(
          entityType.getDescription(LocaleContextHolder.getLocale().getLanguage()));
      if (i18n) {
        builder.setLabelI18n(getI18nEntityTypeLabel(entityType));
        builder.setDescriptionI18n(getI18nEntityTypeDesc(entityType));
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
      builder.setAbstract_(entityType.isAbstract());
      EntityType parent = entityType.getExtends();
      builder.setExtends_(parent != null ? mapInternal(parent, false, false, false, i18n) : null);
      builder.setIndexingDepth(entityType.getIndexingDepth());
      entityTypeResponseBuilder.setData(builder.build());
    }

    return entityTypeResponseBuilder.build();
  }

  private LinksResponse createLinksResponse(int number, int size, int total) {
    URI self = createEntitiesResponseUri();
    URI previous = null;
    URI next = null;
    if (number > 0) {
      previous = createEntitiesResponseUri(number - 1);
    }
    if ((number * size) + size < total) {
      next = createEntitiesResponseUri(number + 1);
    }
    return LinksResponse.create(previous, self, next);
  }

  private URI createEntitiesResponseUri() {
    UriComponentsBuilder builder =
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    return builder.build().toUri();
  }

  private URI createEntitiesResponseUri(Integer pageNumber) {
    UriComponentsBuilder builder =
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery();
    if (pageNumber != null) {
      builder.replaceQueryParam(PAGE, pageNumber);
    }
    return builder.build().toUri();
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

  private void processI18nLabel(CreateEntityTypeRequest entityTypeRequest, EntityType entityType) {
    I18nValue i18nValue = entityTypeRequest.getLabel();
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

  private void processI18nDescription(
      CreateEntityTypeRequest entityTypeRequest, EntityType entityType) {
    I18nValue i18nValue = entityTypeRequest.getDescription();
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

  private I18nValue getI18nEntityTypeDesc(EntityType entityType) {
    String defaultValue = entityType.getDescription();
    ImmutableMap<String, String> translations =
        MetadataUtils.getI18n(entityType, EntityTypeMetadata.DESCRIPTION);
    return I18nValue.create(defaultValue, translations);
  }
}
