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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.molgenis.api.metadata.v3.exception.EmptyAttributesException;
import org.molgenis.api.metadata.v3.exception.IdModificationException;
import org.molgenis.api.metadata.v3.exception.InvalidKeyException;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypeResponseData;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.api.metadata.v3.model.PackageResponse;
import org.molgenis.api.model.response.LinksResponse;
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
import org.molgenis.web.support.MolgenisServletUriComponentsBuilder;
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

    processI18nLabel(entityTypeRequest.getLabel(), entityType);
    processI18nDescription(entityTypeRequest.getDescription(), entityType);
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
      if (isReferenceType(attribute) && attribute.getRefEntity().getId().equals(entityType.getId())) {
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

  private void processI18nDescription(
      I18nValue i18nValue, EntityType entityType) {
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

  public void toEntityType(EntityType entityType, Map<String, Object> entityTypeValues) {
    Iterable<Attribute> updatedAttributes = null;
    for (Entry<String, Object> entry : entityTypeValues.entrySet()) {
      if (entry.getKey().equals("id")) {
        throw new IdModificationException();
      }
      if (entry.getValue() == null) {
        entityType.set(entry.getKey(), null);
      } else {
        switch (entry.getKey()) {
          case "package_":
            String packageId = String.valueOf(entry.getValue());
            Package pack = metaDataService.getPackage(packageId)
                .orElseThrow(() -> new UnknownPackageException(packageId));
            entityType.setPackage(pack);
            break;
          case "abstract_":
            String stringValue = entry.getValue().toString();
            if (!stringValue.equalsIgnoreCase("true") && !stringValue.equalsIgnoreCase("false")) {
              throw new InvalidValueTypeException(stringValue, "boolean", null);
            }
            Boolean isAbstract = Boolean.valueOf(stringValue);
            entityType.setAbstract(isAbstract);
            break;
          case "extends_":
            String extendsValue = String.valueOf(entry.getValue());
            EntityType parent = metaDataService.getEntityType(extendsValue)
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
              updatedAttributes = mapAttributes(entityType, entry);
            } else {
              throw new EmptyAttributesException();
            }
            break;
          case "idAttribute":
          case "labelAttribute":
          case "lookupAttributes":
            //get a list of attributes if not already set, process values for these cases after other updates are done.
            if (updatedAttributes == null) {
              updatedAttributes = entityType.getOwnAllAttributes();
            }
            break;
          default:
            throw new InvalidKeyException("entityType", entry.getKey());
        }
      }
    }
    if (updatedAttributes != null) {
      setSpecialAttributes(entityType, entityTypeValues, updatedAttributes);
    }
  }

  private void setSpecialAttributes(EntityType entityType, Map<String, Object> entityTypeValues,
      Iterable<Attribute> updatedAttributes) {
    List currentLookupAttributes = StreamSupport
        .stream(entityType.getLookupAttributes().spliterator(), false)
        .map(Attribute::getIdentifier).collect(
            Collectors.toList());
    String currentLabelAttributeId =
        entityType.getLabelAttribute() != null ? entityType.getLabelAttribute().getIdentifier()
            : null;
    String currentIdAttributeId =
        entityType.getIdAttribute() != null ? entityType.getIdAttribute().getIdentifier()
            : null;

    String newIdAttributeId = null;
    String newLabelAttributeId = null;
    List newLookupAttributes = null;
    for (Entry<String, Object> entry : entityTypeValues.entrySet()) {
      switch (entry.getKey()) {
        case "idAttribute":
          newIdAttributeId = entry.getValue() != null ? entry.getValue().toString() : null;
          break;
        case "labelAttribute":
          newLabelAttributeId = entry.getValue() != null ? entry.getValue().toString() : null;
          break;
        case "lookupAttributes":
          if (entry.getValue() instanceof Iterable) {
            newLookupAttributes = (List) entry.getValue();
          } else {
            throw new InvalidValueTypeException(entry.getValue().toString(), "list", null);
          }
          break;
        default:
          break;
      }
    }
    updateSpecialAttributes(entityType, updatedAttributes, currentLookupAttributes,
        currentLabelAttributeId, currentIdAttributeId, newIdAttributeId, newLabelAttributeId,
        newLookupAttributes);
  }

  private void updateSpecialAttributes(EntityType entityType, Iterable<Attribute> updatedAttributes,
      List currentLookupAttributes, String currentLabelAttributeId, String currentIdAttributeId,
      String newIdAttributeId, String newLabelAttributeId, List newLookupAttributes) {
    String idAttributeId = newIdAttributeId != null ? newIdAttributeId : currentIdAttributeId;
    updatedAttributes.forEach(attribute -> attribute
        .setIdAttribute(attribute.getIdentifier().equals(idAttributeId)));
    String labelAttributeId =
        newLabelAttributeId != null ? newLabelAttributeId : currentLabelAttributeId;
    updatedAttributes.forEach(attribute -> attribute
        .setLabelAttribute(attribute.getIdentifier().equals(labelAttributeId)));
    setLookupAttributes(updatedAttributes, currentLookupAttributes, newLookupAttributes);
    entityType.setOwnAllAttributes(updatedAttributes);
  }

  private void setLookupAttributes(Iterable<Attribute> updatedAttributes,
      List currentLookupAttributes, List newLookupAttributes) {
    for (Attribute attribute : updatedAttributes) {
      List lookupAttributes =
          newLookupAttributes != null ? newLookupAttributes : currentLookupAttributes;
      int lookupAttributeIndex = lookupAttributes.indexOf(attribute.getIdentifier());
      if (lookupAttributeIndex != -1) {
        attribute.setLookupAttributeIndex(lookupAttributeIndex);
      } else {
        attribute.setLookupAttributeIndex(null);
      }
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

  private Iterable<Attribute> mapAttributes(List<Map<String, Object>> values,
      EntityType entityType) {
    return attributeV3Mapper.toAttributes(values, entityType).values();
  }
}
