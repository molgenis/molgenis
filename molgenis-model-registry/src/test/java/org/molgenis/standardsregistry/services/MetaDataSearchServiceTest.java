package org.molgenis.standardsregistry.services;

import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.standardsregistry.model.PackageSearchRequest;
import org.molgenis.standardsregistry.model.PackageSearchResponse;
import org.molgenis.standardsregistry.model.StandardRegistryEntity;
import org.molgenis.standardsregistry.model.StandardRegistryTag;
import org.molgenis.standardsregistry.utils.StandardRegistryTestHarness;
import org.molgenis.standardsregistry.utils.StandardRegistryTestHarnessConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

/**
 * @author sido
 */
@ContextConfiguration(classes = StandardRegistryTestHarnessConfig.class)
public class MetaDataSearchServiceTest extends AbstractMolgenisSpringTest {

    @Autowired
    private MetaDataService metaDataService;
    @Autowired
    private DataService dataService;
    @Autowired
    private TagService<LabeledResource, LabeledResource> tagService;

    @Autowired
    private PackageFactory packageFactory;

    @Autowired
    private StandardRegistryTestHarness standardRegistryTestHarness;
    @Autowired
    private MetaDataSearchService metaDataSearchService;

    @Test
    public void testGetEntitiesInPackage()
    {
        String TEST_PACKAGE = "test-package";
        EntityType e1 = standardRegistryTestHarness.createEntityType(false);
        Package pkg = packageFactory.create(TEST_PACKAGE);
        pkg.set(PackageMetadata.ENTITY_TYPES, Lists.newArrayList(e1));

        when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(pkg);

        List<StandardRegistryEntity> standardRegistryEntities = metaDataSearchService.getEntitiesInPackage(TEST_PACKAGE);
        assertEquals(1, standardRegistryEntities.size());
    }

    @Test
    public void testGetTagsForPackage()
    {
        String TEST_PACKAGE = "test-package";
        Package pkg = packageFactory.create(TEST_PACKAGE);
        Iterable<SemanticTag<Package, LabeledResource, LabeledResource>> semanticTags = Lists.newArrayList(standardRegistryTestHarness.createSemanticTag(pkg));
        when(tagService.getTagsForPackage(pkg)).thenReturn(semanticTags);
        List<StandardRegistryTag> tags = metaDataSearchService.getTagsForPackage(pkg);
        assertEquals(1, tags.size());
    }

    @Ignore
//    @Test
    public void testSearch()
    {
        String TEST_QUERY = "test-query";
        String TEST_PACKAGE = "test-package";
        String TEST_DESCRIPTION = "test-description";
        String TEST_LABEL = "test-label";

        PackageSearchRequest request = new PackageSearchRequest();
        request.setQuery(TEST_QUERY);

        Package pkg = packageFactory.create(TEST_PACKAGE, TEST_DESCRIPTION);
        Iterable<Package> packages = Lists.newArrayList(pkg);

        // metadataservice mocks
        when(metaDataService.getRootPackages()).thenReturn(packages);
        when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(pkg);

        // dataservice mocks
        Query<Package> packageQuery = new QueryImpl<Package>().search(TEST_QUERY);
        Stream<Package> packageStream = Stream.of(packageFactory.create(TEST_PACKAGE, TEST_LABEL));
        when(dataService.findAll(PackageMetadata.PACKAGE, packageQuery, Package.class)).thenReturn(packageStream);

        Query<EntityType> entityTypeQuery = new QueryImpl<EntityType>().search(TEST_QUERY);
        Stream<EntityType> entityStream = Stream.of(standardRegistryTestHarness.createEntityType(true));
        when(dataService.findAll(ENTITY_TYPE_META_DATA, entityTypeQuery, EntityType.class)).thenReturn(entityStream);

        // tagservice mocks
        Iterable<SemanticTag<Package, LabeledResource, LabeledResource>> symanticTags = Lists.newArrayList(standardRegistryTestHarness.createSemanticTag(pkg));
        when(tagService.getTagsForPackage(pkg)).thenReturn(symanticTags);


        PackageSearchResponse response = metaDataSearchService.search(request);


    }







}
