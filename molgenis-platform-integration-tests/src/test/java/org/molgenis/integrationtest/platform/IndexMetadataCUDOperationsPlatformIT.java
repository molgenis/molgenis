package org.molgenis.integrationtest.platform;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.integrationtest.platform.PlatformIT.waitForWorkToBeFinished;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.index.IndexActionScheduler;
import org.molgenis.data.index.SearchService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexMetadataCUDOperationsPlatformIT {

  private static final Logger LOG =
      LoggerFactory.getLogger(IndexMetadataCUDOperationsPlatformIT.class);

  public static void testIndexCreateMetaData(
      SearchService searchService,
      EntityType entityTypeStatic,
      EntityType entityTypeDynamic,
      MetaDataService metaDataService) {
    System.out.println("START - testIndexCreateMetaData");

    System.out.println(
        "entityTypeStatic id: "
            + entityTypeStatic.getId()
            + " package: "
            + entityTypeStatic.getPackage());

    Query<Entity> q1 = new QueryImpl<>();
    q1.eq(EntityTypeMetadata.ID, entityTypeStatic.getId())
        .and()
        .eq(EntityTypeMetadata.PACKAGE, entityTypeStatic.getPackage());
    var count =
        searchService.count(
            metaDataService
                .getEntityType(ENTITY_TYPE_META_DATA)
                .orElseThrow(() -> new UnknownEntityTypeException(entityTypeStatic.getId())),
            q1);

    System.out.println("COUNT - entityTypeStatic with package: " + count);
    assertEquals(1, count);

    System.out.println(
        "entityTypeDynamic id: "
            + entityTypeDynamic.getId()
            + " package: "
            + entityTypeDynamic.getPackage());
    Query<Entity> q2 = new QueryImpl<>();
    q2.eq(EntityTypeMetadata.ID, entityTypeDynamic.getId())
        .and()
        .eq(EntityTypeMetadata.PACKAGE, entityTypeDynamic.getPackage());
    var count2 =
        searchService.count(
            metaDataService
                .getEntityType(ENTITY_TYPE_META_DATA)
                .orElseThrow(() -> new UnknownEntityTypeException(entityTypeDynamic.getId())),
            q2);

    System.out.println("COUNT - entityTypeDynamic with package: " + count2);
    assertEquals(1, count2);
  }

  public static void testIndexUpdateMetaDataRemoveCompoundAttribute(
      EntityType entityType,
      AttributeFactory attributeFactory,
      ElasticsearchService searchService,
      MetaDataService metaDataService,
      IndexActionScheduler indexService) {
    // 1. Create new compound to test delete
    Attribute compound = attributeFactory.create();
    compound.setName("test_compound");
    compound.setDataType(AttributeType.COMPOUND);

    Attribute compoundChild = attributeFactory.create();
    compoundChild.setName("test_compound_child");
    compoundChild.setParent(compound);

    entityType.addAttributes(newArrayList(compound, compoundChild));
    runAsSystem(() -> metaDataService.updateEntityType(entityType));
    waitForWorkToBeFinished(indexService, LOG);
    assertTrue(searchService.hasIndex(entityType));

    // 2. Verify compound and child got added
    EntityType afterAddEntityType =
        metaDataService
            .getEntityType(entityType.getId())
            .orElseThrow(() -> new UnknownEntityTypeException(entityType.getId()));
    assertNotNull(afterAddEntityType.getAttribute("test_compound"));
    assertNotNull(afterAddEntityType.getAttribute("test_compound_child"));

    // 3. Delete compound
    afterAddEntityType.removeAttribute(compound);
    runAsSystem(() -> metaDataService.updateEntityType(afterAddEntityType));
    waitForWorkToBeFinished(indexService, LOG);
    assertTrue(searchService.hasIndex(afterAddEntityType));

    // 4. Verify that compound + child was removed
    EntityType afterRemoveEntityType =
        metaDataService
            .getEntityType(entityType.getId())
            .orElseThrow(
                () ->
                    new UnknownEntityException(
                        EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityType.getId()));
    org.junit.jupiter.api.Assertions.assertNull(
        afterRemoveEntityType.getAttribute(compound.getName()));
    org.junit.jupiter.api.Assertions.assertNull(
        afterRemoveEntityType.getAttribute(compoundChild.getName()));

    EntityType attributeMetadata =
        metaDataService
            .getEntityType(AttributeMetadata.ATTRIBUTE_META_DATA)
            .orElseThrow(
                () ->
                    new UnknownEntityException(
                        EntityTypeMetadata.ENTITY_TYPE_META_DATA,
                        AttributeMetadata.ATTRIBUTE_META_DATA));

    Query<Entity> compoundQuery = new QueryImpl<>().eq(AttributeMetadata.ID, compound.getIdValue());
    Query<Entity> childQuery =
        new QueryImpl<>().eq(AttributeMetadata.ID, compoundChild.getIdValue());

    assertEquals(0, searchService.count(attributeMetadata, compoundQuery));
    assertEquals(0, searchService.count(attributeMetadata, childQuery));
  }
}
