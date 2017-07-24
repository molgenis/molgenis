package org.molgenis.model.registry.utils;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.semanticsearch.service.impl.UntypedTagService;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.model.registry.mappers.TreeNodeMapper;
import org.molgenis.model.registry.services.MetaDataSearchService;
import org.molgenis.model.registry.services.MetaDataSearchServiceImpl;
import org.molgenis.model.registry.services.TreeNodeService;
import org.molgenis.model.registry.services.TreeNodeServiceImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

/**
 * @author sido
 */
@Configuration
@Import({ TestHarnessConfig.class, ModelRegistryTestHarness.class })
public class ModelRegistryServiceConfig
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
	public TreeNodeMapper treeNodeMapper()
	{
		return new TreeNodeMapper(tagService());
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
	public TreeNodeService treeNodeService() { return new TreeNodeServiceImpl(treeNodeMapper()); }

}
