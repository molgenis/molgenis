package org.molgenis.standardsregistry.utils;

import org.molgenis.data.DataService;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.semanticsearch.service.impl.UntypedTagService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.standardsregistry.services.MetaDataSearchService;
import org.molgenis.standardsregistry.services.MetaDataSearchServiceImpl;
import org.molgenis.standardsregistry.services.TreeNodeService;
import org.molgenis.standardsregistry.services.TreeNodeServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

/**
 * @author sido
 */
@Configuration
@Import({TestHarnessConfig.class, StandardRegistryTestHarness.class })
public class StandardRegistryTestHarnessConfig {

    @Bean
    public MetaDataService metaDataService()
    {
        return mock(MetaDataService.class);
    }

    @Bean
    public DataService dataService()
    {
        return mock(DataService.class);
    }

    @Bean
    public TagService<LabeledResource, LabeledResource> tagService()
    {
        return mock(UntypedTagService.class);
    }

    @Bean
    public MolgenisPermissionService molgenisPermissionService()
    {
        return mock(MolgenisPermissionService.class);
    }

    @Bean
    public MetaDataSearchService metaDataSearchService()
    {
        return new MetaDataSearchServiceImpl(dataService(), metaDataService(), tagService(), molgenisPermissionService());
    }

    @Bean
    public TreeNodeService treeNodeService()
    {
        return new TreeNodeServiceImpl(tagService());
    }

}
