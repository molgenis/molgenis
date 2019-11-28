package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.data.v3.EntityController;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateAttributeRequest;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.DeleteAttributesRequest;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypesRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.ReadAttributesRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypesRequest;
import org.molgenis.api.model.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(MetadataApiController.API_META_PATH)
class MetadataApiController extends ApiController {

  private static final String API_META_ID = "metadata";
  static final String API_META_PATH = ApiNamespace.API_PATH + '/' + API_META_ID;

  private final MetadataApiService metadataApiService;
  private final EntityTypeV3Mapper entityTypeV3Mapper;
  private final AttributeV3Mapper attributeV3Mapper;

  MetadataApiController(
      MetadataApiService metadataApiService,
      EntityTypeV3Mapper entityTypeV3Mapper,
      AttributeV3Mapper attributeV3Mapper) {
    super(API_META_ID, 3);
    this.metadataApiService = requireNonNull(metadataApiService);
    this.entityTypeV3Mapper = requireNonNull(entityTypeV3Mapper);
    this.attributeV3Mapper = requireNonNull(attributeV3Mapper);
  }

  @Transactional(readOnly = true)
  @GetMapping
  public EntityTypesResponse getEntityTypes(@Valid ReadEntityTypesRequest entitiesRequest) {
    int size = entitiesRequest.getSize();
    int page = entitiesRequest.getPage();
    Sort sort = entitiesRequest.getSort();

    EntityTypes entityTypes =
        metadataApiService.findEntityTypes(entitiesRequest.getQ().orElse(null), sort, size, page);

    return entityTypeV3Mapper.toEntityTypesResponse(
        entityTypes, size, page, entityTypes.getTotal());
  }

  @Transactional(readOnly = true)
  @GetMapping("/{entityTypeId}")
  public EntityTypeResponse getEntityType(
      @PathVariable("entityTypeId") String entityTypeId,
      @Valid ReadEntityTypeRequest readEntityTypeRequest) {
    EntityType entityType = metadataApiService.findEntityType(entityTypeId);

    return entityTypeV3Mapper.toEntityTypeResponse(
        entityType, readEntityTypeRequest.isFlattenAttrs(), readEntityTypeRequest.isI18n());
  }

  @Transactional(readOnly = true)
  @GetMapping("/{entityTypeId}/attributes/{attributeId}")
  public AttributeResponse getAttribute(
      @PathVariable("entityTypeId") String entityTypeId,
      @PathVariable("attributeId") String attributeId) {
    Attribute attribute = metadataApiService.findAttribute(entityTypeId, attributeId);
    return attributeV3Mapper.mapAttribute(attribute, false);
  }

  @Transactional
  @PostMapping
  public ResponseEntity createEntityType(
      @Valid @RequestBody CreateEntityTypeRequest createEntityTypeRequest) {
    EntityType entityType = entityTypeV3Mapper.toEntityType(createEntityTypeRequest);
    metadataApiService.createEntityType(entityType);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(API_META_PATH)
            .pathSegment(entityType.getId())
            .build()
            .toUri();
    return ResponseEntity.created(location).build();
  }

  @Transactional(readOnly = true)
  @GetMapping("/{entityTypeId}/attributes")
  public AttributesResponse getAttributes(
      @PathVariable("entityTypeId") String entityTypeId,
      @Valid ReadAttributesRequest readAttributesRequest) {
    int size = readAttributesRequest.getSize();
    int page = readAttributesRequest.getPage();
    Sort sort = readAttributesRequest.getSort();

    Attributes attributes =
        metadataApiService.findAttributes(
            entityTypeId, readAttributesRequest.getQ().orElse(null), sort, size, page);

    return attributeV3Mapper.mapAttributes(attributes, size, page, attributes.getTotal());
  }

  @Transactional
  @DeleteMapping("/{entityTypeId}/attributes/{attributeId}")
  public ResponseEntity deleteAttribute(
      @PathVariable("entityTypeId") String entityTypeId,
      @PathVariable("attributeId") String attributeId) {
    JobExecution jobExecution = metadataApiService.deleteAttributeAsync(entityTypeId, attributeId);
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @DeleteMapping("/{entityTypeId}/attributes")
  public ResponseEntity deleteAttributes(
      @PathVariable("entityTypeId") String entityTypeId,
      @Valid DeleteAttributesRequest deleteAttributesRequest) {
    JobExecution jobExecution =
        metadataApiService.deleteAttributesAsync(entityTypeId, deleteAttributesRequest.getQ());
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @PutMapping("/{entityTypeId}")
  public ResponseEntity updateEntityType(
      @PathVariable("entityTypeId") String entityTypeId,
      @Valid @RequestBody CreateEntityTypeRequest createEntityTypeRequest) {
    EntityType entityType = entityTypeV3Mapper.toEntityType(createEntityTypeRequest);
    entityType.setId(entityTypeId);

    JobExecution jobExecution = metadataApiService.updateEntityTypeAsync(entityType);
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @PutMapping("/{entityTypeId}/attributes/{attributeId}")
  public ResponseEntity updateAttribute(
      @PathVariable("entityTypeId") String entityTypeId,
      @PathVariable("attributeId") String attributeId,
      @Valid @RequestBody CreateAttributeRequest createAttributeRequest) {
    EntityType entityType = metadataApiService.findEntityType(entityTypeId);
    Attribute currentAttribute = entityType.getOwnAttributeById(attributeId);

    Attribute updatedAttribute = attributeV3Mapper.toAttribute(createAttributeRequest, entityType);
    updatedAttribute.setIdentifier(attributeId);
    if (updatedAttribute.getSequenceNumber() == null) {
      updatedAttribute.setSequenceNumber(currentAttribute.getSequenceNumber());
    }

    replaceAttribute(entityType, updatedAttribute);

    JobExecution jobExecution = metadataApiService.updateEntityTypeAsync(entityType);
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @PatchMapping("/{entityTypeId}")
  public ResponseEntity updatePartialEntityType(
      @PathVariable("entityTypeId") String entityTypeId,
      @RequestBody Map<String, Object> entityTypeValues) {
    EntityType entityType = metadataApiService.findEntityType(entityTypeId);
    entityTypeV3Mapper.toEntityType(entityType, entityTypeValues);
    JobExecution jobExecution = metadataApiService.updateEntityTypeAsync(entityType);
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @DeleteMapping("/{entityTypeId}")
  public ResponseEntity deleteEntityType(@PathVariable("entityTypeId") String entityTypeId) {
    JobExecution jobExecution = metadataApiService.deleteEntityTypeAsync(entityTypeId);
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @DeleteMapping
  public ResponseEntity deleteEntityTypes(
      @Valid DeleteEntityTypesRequest deleteEntityTypesRequest) {
    JobExecution jobExecution =
        metadataApiService.deleteEntityTypesAsync(deleteEntityTypesRequest.getQ());
    return toLocationResponse(jobExecution);
  }

  private void replaceAttribute(EntityType entityType, Attribute attribute) {
    List<Attribute> updatedAttributes = new ArrayList<>();
    entityType
        .getOwnAllAttributes()
        .forEach(
            currentAttribute -> {
              if (currentAttribute.getIdentifier().equals(attribute.getIdentifier())) {
                updatedAttributes.add(attribute);
              } else {
                updatedAttributes.add(currentAttribute);
              }
            });
    entityType.setOwnAllAttributes(updatedAttributes);
  }

  private ResponseEntity toLocationResponse(JobExecution jobExecution) {
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(EntityController.API_ENTITY_PATH)
            .pathSegment(jobExecution.getEntityType().getId(), jobExecution.getIdentifier())
            .build()
            .toUri();
    return ResponseEntity.accepted().location(location).build();
  }
}
