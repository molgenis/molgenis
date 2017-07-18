package org.molgenis.standardsregistry.utils;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.semanticsearch.service.impl.UntypedTagService;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.standardsregistry.StandardsRegistryController;
import org.molgenis.standardsregistry.StandardsRegistryControllerTest;
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
@Import({ TestHarnessConfig.class, StandardRegistryTestHarness.class })
public class StandardRegistryServiceConfig
{

	@Bean
	public EntityTestHarness entityTestHarness() {
		return new EntityTestHarness();
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry() {
		return new MolgenisPluginRegistryImpl();
	}

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
		return new MetaDataSearchServiceImpl(dataService(), metaDataService(), tagService(),
				molgenisPermissionService());
	}

	@Bean
	public TreeNodeService treeNodeService()
	{
		return new TreeNodeServiceImpl(tagService());
	}

}
