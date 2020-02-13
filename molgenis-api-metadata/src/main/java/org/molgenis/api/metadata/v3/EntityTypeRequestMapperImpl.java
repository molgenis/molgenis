package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.util.EntityTypeUtils.isReferenceType;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.api.metadata.v3.exception.EmptyAttributesException;
import org.molgenis.api.metadata.v3.exception.InvalidKeyException;
import org.molgenis.api.metadata.v3.exception.ReadOnlyFieldException;
import org.molgenis.api.metadata.v3.exception.UnsupportedFieldException;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.data.InvalidValueTypeException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.UnknownPackageException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.springframework.stereotype.Component;

@Component
public class EntityTypeRequestMapperImpl implements EntityTypeRequestMapper {

  private final EntityTypeFactory entityTypeFactory;
  private final AttributeRequestMapper attributeRequestMapper;
  private final MetaDataService metaDataService;

  EntityTypeRequestMapperImpl(
      EntityTypeFactory entityTypeFactory,
      AttributeRequestMapper attributeRequestMapper,
      MetaDataService metaDataService) {
    this.entityTypeFactory = requireNonNull(entityTypeFactory);
    this.attributeRequestMapper = requireNonNull(attributeRequestMapper);
    this.metaDataService = requireNonNull(metaDataService);
  }

  @Override
  public EntityType toEntityType(CreateEntityTypeRequest entityTypeRequest) {
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
        attributeRequestMapper.toAttributes(
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

  @Override
  public void updateEntityType(EntityType entityType, Map<String, Object> entityTypeValues) {
    for (Entry<String, Object> entry : entityTypeValues.entrySet()) {
      if (entry.getValue() == null) {
        entityType.set(entry.getKey(), null);
      } else {
        updateEntityType(entityType, entry);
      }
    }
  }

  @SuppressWarnings({
    "java:S1192"
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
        I18nValue label = I18nValueMapper.toI18nValue(entry.getValue());
        processI18nLabel(label, entityType);
        break;
      case "description":
        I18nValue description = I18nValueMapper.toI18nValue(entry.getValue());
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
    @SuppressWarnings("unchecked")
    List<Object> attrValues = (List<Object>) entry.getValue();
    List<Map<String, Object>> requestAttributes = new ArrayList<>();
    for (Object attrValue : attrValues) {
      @SuppressWarnings("unchecked")
      Map<String, Object> valueMap = (Map<String, Object>) attrValue;
      requestAttributes.add(valueMap);
    }
    return mapAttributes(requestAttributes, entityType);
  }

  private Iterable<Attribute> mapAttributes(
      List<Map<String, Object>> values, EntityType entityType) {
    return attributeRequestMapper.toAttributes(values, entityType).values();
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
}
