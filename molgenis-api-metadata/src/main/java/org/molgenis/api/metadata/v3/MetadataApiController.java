package org.molgenis.api.metadata.v3;

import static java.util.Objects.requireNonNull;

import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.metadata.v3.model.AttributesResponse;
import org.molgenis.api.metadata.v3.model.CreateEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.EntityTypeResponse;
import org.molgenis.api.metadata.v3.model.EntityTypesResponse;
import org.molgenis.api.metadata.v3.model.ReadAttributesRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypeRequest;
import org.molgenis.api.metadata.v3.model.ReadEntityTypesRequest;
import org.molgenis.api.model.Sort;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(MetadataApiController.API_META_PATH)
class MetadataApiController extends ApiController {
  private static final String API_META_ID = "metadata";
  static final String API_META_PATH = ApiNamespace.API_PATH + '/' + API_META_ID;

  private final MetadataServiceImpl metadataService;
  private final MetadataV3Mapper entityTypeMapper;

  MetadataApiController(MetadataServiceImpl metadataService, MetadataV3Mapper entityTypeMapper) {
    super(API_META_ID, 1);
    this.metadataService = requireNonNull(metadataService);
    this.entityTypeMapper = requireNonNull(entityTypeMapper);
  }

  @GetMapping("/")
  public EntityTypesResponse getEntityTypes(@Valid ReadEntityTypesRequest entitiesRequest) {
    int size = entitiesRequest.getSize();
    int page = entitiesRequest.getPage();
    Sort sort = entitiesRequest.getSort();

    EntityTypes entityTypes =
        metadataService.findEntityTypes(entitiesRequest.getQ().orElse(null), sort, size, page);

    return entityTypeMapper.toEntityTypeResponse(entityTypes, size, page, entityTypes.getTotal());
  }

  @GetMapping("/{entityTypeId}")
  public EntityTypeResponse getEntityType(@Valid ReadEntityTypeRequest readEntityTypeRequest) {

    EntityType entityType = metadataService.findEntityType(readEntityTypeRequest.getEntityTypeId());

    return entityTypeMapper.toEntityTypeResponse(entityType);
  }

  @PostMapping("/")
  public void createEntityType(@RequestBody CreateEntityTypeRequest createEntityTypeRequest) {
    EntityType entityType = entityTypeMapper.toEntityType(createEntityTypeRequest);
    metadataService.createEntityType(entityType);
  }

  @GetMapping("/{entityTypeId}/attributes")
  public AttributesResponse getAttributes(@Valid ReadAttributesRequest readAttributesRequest) {
    int size = readAttributesRequest.getSize();
    int page = readAttributesRequest.getPage();
    Sort sort = readAttributesRequest.getSort();

    Attributes attributes =
        metadataService.findAttributes(
            readAttributesRequest.getEntityTypeId(),
            readAttributesRequest.getQ().orElse(null),
            sort,
            size,
            page);

    return entityTypeMapper.mapAttributes(attributes, size, page, attributes.getTotal());
  }
}
