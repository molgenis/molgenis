package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Map;
import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.data.v3.EntityController;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.DeleteAttributeRequest;
import org.molgenis.api.metadata.v3.model.DeleteAttributesRequest;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.DeleteEntityTypesRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.ReadAttributeRequest;
import org.molgenis.api.metadata.v3.model.ReadAttributesRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypesRequest;
import org.molgenis.api.model.Sort;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(MetadataApiController.API_META_PATH)
class MetadataApiController extends ApiController {

  private static final String API_META_ID = "metadata";
  static final String API_META_PATH = ApiNamespace.API_PATH + '/' + API_META_ID;

  private final MetadataApiService metadataApiService;
  private final MetadataApiJobService metadataApiJobService;
  private final EntityTypeV3Mapper entityTypeV3Mapper;
  private final AttributeV3Mapper attributeV3Mapper;

  MetadataApiController(
      MetadataApiService metadataApiService,
      MetadataApiJobService metadataApiJobService,
      EntityTypeV3Mapper entityTypeV3Mapper,
      AttributeV3Mapper attributeV3Mapper) {
    super(API_META_ID, 3);
    this.metadataApiService = requireNonNull(metadataApiService);
    this.metadataApiJobService = requireNonNull(metadataApiJobService);
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
  public EntityTypeResponse getEntityType(@Valid ReadEntityTypeRequest readEntityTypeRequest) {
    EntityType entityType =
        metadataApiService.findEntityType(readEntityTypeRequest.getEntityTypeId());

    return entityTypeV3Mapper.toEntityTypeResponse(
        entityType, readEntityTypeRequest.isFlattenAttrs(), readEntityTypeRequest.isI18n());
  }

  @Transactional(readOnly = true)
  @GetMapping("/{entityTypeId}/attributes/{attributeId}")
  public AttributeResponse getEntityTypeAttribute(
      @Valid ReadAttributeRequest readAttributeRequest) {
    // TODO pass entity type identifier to findAttribute and check if exists in service
    Attribute attribute = metadataApiService.findAttribute(readAttributeRequest.getAttributeId());
    return attributeV3Mapper.mapAttribute(attribute, false);
  }

  @Transactional
  @PostMapping
  public ResponseEntity createEntityType(
      @RequestBody CreateEntityTypeRequest createEntityTypeRequest) {
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
  public AttributesResponse getAttributes(@Valid ReadAttributesRequest readAttributesRequest) {
    int size = readAttributesRequest.getSize();
    int page = readAttributesRequest.getPage();
    Sort sort = readAttributesRequest.getSort();

    Attributes attributes =
        metadataApiService.findAttributes(
            readAttributesRequest.getEntityTypeId(),
            readAttributesRequest.getQ().orElse(null),
            sort,
            size,
            page);

    return attributeV3Mapper.mapAttributes(attributes, size, page, attributes.getTotal());
  }

  @Transactional
  @DeleteMapping("/{entityTypeId}/attributes/{attributeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity deleteAttribute(@Valid DeleteAttributeRequest deleteAttributeRequest) {
    JobExecution jobExecution =
        metadataApiJobService.scheduleDeleteAttribute(
            deleteAttributeRequest.getEntityTypeId(), deleteAttributeRequest.getAttributeId());
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @DeleteMapping("/{entityTypeId}/attributes")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity deleteAttributes(@Valid DeleteAttributesRequest deleteAttributesRequest) {
    JobExecution jobExecution =
        metadataApiJobService.scheduleDeleteAttribute(
            deleteAttributesRequest.getEntityTypeId(), deleteAttributesRequest.getQ());
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @PutMapping("/{entityTypeId}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity updateEntityType(
      @PathVariable("entityTypeId") String entityTypeId,
      @RequestBody CreateEntityTypeRequest createEntityTypeRequest) {
    EntityType entityType = entityTypeV3Mapper.toEntityType(createEntityTypeRequest);
    entityType.setId(entityTypeId);

    JobExecution jobExecution = metadataApiJobService.scheduleUpdate(entityType);
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @PatchMapping("/{entityTypeId}")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ResponseEntity updatePartialEntityType(
      @PathVariable("entityTypeId") String entityTypeId,
      @RequestBody Map<String, Object> entityTypeValues) {
    EntityType entityType = metadataApiService.findEntityType(entityTypeId);

    // TODO modify entity type based on entity type values
    // note: we can't use CreateEntityTypeRequest because we can't distinguish between null and not
    // defined values then)

    JobExecution jobExecution = metadataApiJobService.scheduleUpdate(entityType);
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @DeleteMapping("/{entityTypeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity deleteEntityType(@Valid DeleteEntityTypeRequest deleteEntityTypeRequest) {
    JobExecution jobExecution =
        metadataApiJobService.scheduleDeleteEntityType(deleteEntityTypeRequest.getEntityTypeId());
    return toLocationResponse(jobExecution);
  }

  @Transactional
  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity deleteEntityTypes(
      @Valid DeleteEntityTypesRequest deleteEntityTypesRequest) {
    JobExecution jobExecution =
        metadataApiJobService.scheduleDeleteEntityType(deleteEntityTypesRequest.getQ());
    return toLocationResponse(jobExecution);
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
