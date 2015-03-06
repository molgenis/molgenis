package org.molgenis.compute.ui.meta;

import java.util.Arrays;

import org.molgenis.compute.ui.analysis.event.AnalysisHandlerRegistratorService;
import org.molgenis.compute.ui.model.decorator.AnalysisDecorator;
import org.molgenis.compute.ui.model.decorator.AnalysisJobDecorator;
import org.molgenis.compute.ui.model.decorator.UIWorkflowDecorator;
import org.molgenis.compute.ui.workflow.event.WorkflowHandlerRegistratorService;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class MetaDataRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(MetaDataRegistrator.class);

	private final DataService dataService;
	private final WorkflowHandlerRegistratorService workflowHandlerRegistratorService;
	private final AnalysisHandlerRegistratorService analysisHandlerRegistratorService;

	@Autowired
	public MetaDataRegistrator(DataService dataService,
			WorkflowHandlerRegistratorService workflowHandlerRegistratorService,
			AnalysisHandlerRegistratorService analysisHandlerRegistratorService)
	{
		this.dataService = dataService;
		this.workflowHandlerRegistratorService = workflowHandlerRegistratorService;
		this.analysisHandlerRegistratorService = analysisHandlerRegistratorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		LOG.info("Registering ComputeUI MySQL repositories ...");
		dataService.getMeta().addEntityMeta(MolgenisUserKeyMetaData.INSTANCE);
		dataService.getMeta().addEntityMeta(UIParameterMetaData.INSTANCE);
		dataService.getMeta().addEntityMeta(UIWorkflowParameterValueMetaData.INSTANCE);
		dataService.getMeta().addEntityMeta(UIParameterMappingMetaData.INSTANCE);
		dataService.getMeta().addEntityMeta(UIWorkflowParameterMetaData.INSTANCE);
		dataService.getMeta().addEntityMeta(UIWorkflowProtocolMetaData.INSTANCE);
		dataService.getMeta().addEntityMeta(UIWorkflowNodeMetaData.INSTANCE);
		dataService.getMeta().add(UIWorkflowMetaData.INSTANCE, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				if (!(repository.getCapabilities().containsAll(Arrays.asList(RepositoryCapability.QUERYABLE,
						RepositoryCapability.UPDATEABLE, RepositoryCapability.WRITABLE))))
				{
					throw new RuntimeException("Repository [" + repository.getName() + "] must be a Crud Repository");
				}
				return new UIWorkflowDecorator(repository, dataService, workflowHandlerRegistratorService,
						analysisHandlerRegistratorService);
			}
		});
		dataService.getMeta().addEntityMeta(UIParameterValueMetaData.INSTANCE);
		dataService.getMeta().addEntityMeta(UIBackendMetaData.INSTANCE);
		dataService.getMeta().add(AnalysisMetaData.INSTANCE, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				if (!(repository.getCapabilities().containsAll(Arrays.asList(RepositoryCapability.QUERYABLE,
						RepositoryCapability.UPDATEABLE, RepositoryCapability.WRITABLE))))
				{
					throw new RuntimeException("Repository [" + repository.getName() + "] must be a Crud Repository");
				}
				return new AnalysisDecorator(repository, dataService);
			}
		});
		dataService.getMeta().add(AnalysisJobMetaData.INSTANCE, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				if (!(repository.getCapabilities().containsAll(Arrays.asList(RepositoryCapability.QUERYABLE,
						RepositoryCapability.UPDATEABLE, RepositoryCapability.WRITABLE))))
				{
					throw new RuntimeException("Repository [" + repository.getName() + "] must be a Crud Repository");
				}
				return new AnalysisJobDecorator(repository, dataService);
			}
		});

		LOG.info("Registered ComputeUI repositories");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}
}