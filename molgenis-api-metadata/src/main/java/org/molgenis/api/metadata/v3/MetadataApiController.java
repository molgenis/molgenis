package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;

import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.metadata.v3.model.AttributeResponse;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(MetadataApiController.API_META_PATH)
class MetadataApiController extends ApiController {
  private static final String API_META_ID = "metadata";
  static final String API_META_PATH = ApiNamespace.API_PATH + '/' + API_META_ID;

  private final MetadataApiService metadataApiService;
  private final MetadataV3Mapper metadataV3Mapper;

  MetadataApiController(MetadataApiService metadataApiService, MetadataV3Mapper metadataV3Mapper) {
    super(API_META_ID, 3);
    this.metadataApiService = requireNonNull(metadataApiService);
    this.metadataV3Mapper = requireNonNull(metadataV3Mapper);
  }

  @Transactional(readOnly = true)
  @GetMapping
  public EntityTypesResponse getEntityTypes(@Valid ReadEntityTypesRequest entitiesRequest) {
    int size = entitiesRequest.getSize();
    int page = entitiesRequest.getPage();
    Sort sort = entitiesRequest.getSort();

    EntityTypes entityTypes =
        metadataApiService.findEntityTypes(entitiesRequest.getQ().orElse(null), sort, size, page);

    return metadataV3Mapper.toEntityTypeResponse(entityTypes, size, page, entityTypes.getTotal());
  }

  @Transactional(readOnly = true)
  @GetMapping("/{entityTypeId}")
  public EntityTypeResponse getEntityType(@Valid ReadEntityTypeRequest readEntityTypeRequest) {
    EntityType entityType =
        metadataApiService.findEntityType(readEntityTypeRequest.getEntityTypeId());

    return metadataV3Mapper.toEntityTypeResponse(
        entityType, readEntityTypeRequest.isFlattenAttrs());
  }

  @Transactional(readOnly = true)
  @GetMapping("/{entityTypeId}/attributes/{attributeId}")
  public AttributeResponse getEntityTypeAttribute(
      @Valid ReadAttributeRequest readAttributeRequest) {
    // TODO pass entity type identifier to findAttribute and check if exists in service
    Attribute attribute = metadataApiService.findAttribute(readAttributeRequest.getAttributeId());
    return metadataV3Mapper.mapAttribute(attribute);
  }

  @Transactional
  @PostMapping
  public void createEntityType(@RequestBody CreateEntityTypeRequest createEntityTypeRequest) {
    EntityType entityType = metadataV3Mapper.toEntityType(createEntityTypeRequest);
    metadataApiService.createEntityType(entityType);
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

    return metadataV3Mapper.mapAttributes(attributes, size, page, attributes.getTotal());
  }

  @Transactional
  @DeleteMapping("/{entityTypeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteEntityType(@Valid DeleteEntityTypeRequest deleteEntityTypeRequest) {
    metadataApiService.deleteEntityType(deleteEntityTypeRequest.getEntityTypeId());
  }

  @Transactional
  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteEntityTypes(@Valid DeleteEntityTypesRequest deleteEntityTypesRequest) {
    metadataApiService.deleteEntityTypes(deleteEntityTypesRequest.getQ());
  }
}
