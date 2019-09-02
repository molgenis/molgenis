package org.molgenis.api.meta;

import static java.util.Objects.requireNonNull;

import javax.validation.Valid;
import org.molgenis.api.ApiController;
import org.molgenis.api.ApiNamespace;
import org.molgenis.api.meta.model.EntityTypeResponse;
import org.molgenis.api.meta.model.EntityTypesResponse;
import org.molgenis.api.meta.model.ReadEntityTypeRequest;
import org.molgenis.api.meta.model.ReadEntityTypesRequest;
import org.molgenis.api.model.Selection;
import org.molgenis.api.model.Sort;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(MetaApiController.API_META_PATH)
class MetaApiController extends ApiController {
  private static final String API_META_ID = "meta";
  static final String API_META_PATH = ApiNamespace.API_PATH + '/' + API_META_ID;

  private final MetadataServiceImpl metadataService;
  private final EntityTypeV1Mapper entityTypeMapper;

  MetaApiController(MetadataServiceImpl metadataService, EntityTypeV1Mapper entityTypeMapper) {
    super(API_META_ID, 1);
    this.metadataService = requireNonNull(metadataService);
    this.entityTypeMapper = requireNonNull(entityTypeMapper);
  }

  @GetMapping("/")
  public EntityTypesResponse getEntityTypes(@Valid ReadEntityTypesRequest entitiesRequest) {
    Selection filter = entitiesRequest.getFilter();
    Selection expand = entitiesRequest.getExpand();
    int size = entitiesRequest.getSize();
    int page = entitiesRequest.getPage();
    Sort sort = entitiesRequest.getSort();

    EntityTypes entityTypes =
        metadataService.findEntityTypes(
            entitiesRequest.getQ().orElse(null), filter, expand, sort, size, page);

    return entityTypeMapper.map(entityTypes, filter, expand, size, page, entityTypes.getTotal());
  }

  @GetMapping("/{entityTypeId}")
  public EntityTypeResponse getEntityType(@Valid ReadEntityTypeRequest readEntityTypeRequest) {
    Selection filter = readEntityTypeRequest.getFilter();
    Selection expand = readEntityTypeRequest.getExpand();

    EntityType entityType =
        metadataService.findEntityType(readEntityTypeRequest.getEntityTypeId(), filter, expand);

    return entityTypeMapper.map(entityType, filter, expand);
  }
}
