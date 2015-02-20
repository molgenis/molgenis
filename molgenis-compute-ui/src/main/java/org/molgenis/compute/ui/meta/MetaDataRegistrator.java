package org.molgenis.compute.ui.meta;

import org.molgenis.compute.ui.analysis.event.AnalysisHandlerRegistratorService;
import org.molgenis.compute.ui.model.decorator.AnalysisDecorator;
import org.molgenis.compute.ui.model.decorator.AnalysisJobDecorator;
import org.molgenis.compute.ui.model.decorator.UIWorkflowDecorator;
import org.molgenis.compute.ui.workflow.event.WorkflowHandlerRegistratorService;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
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

	private final MysqlRepositoryCollection repositoryCollection;
	private final DataService dataService;
	private final MetaDataService metaDataService;
	private final WorkflowHandlerRegistratorService workflowHandlerRegistratorService;
	private final AnalysisHandlerRegistratorService analysisHandlerRegistratorService;

	@Autowired
	public MetaDataRegistrator(MysqlRepositoryCollection repositoryCollection, DataService dataService,
			MetaDataService metaDataService, WorkflowHandlerRegistratorService workflowHandlerRegistratorService,
			AnalysisHandlerRegistratorService analysisHandlerRegistratorService)
	{
		this.repositoryCollection = repositoryCollection;
		this.dataService = dataService;
		this.metaDataService = metaDataService;
		this.workflowHandlerRegistratorService = workflowHandlerRegistratorService;
		this.analysisHandlerRegistratorService = analysisHandlerRegistratorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		LOG.info("Registering ComputeUI MySQL repositories ...");
		repositoryCollection.add(MolgenisUserKeyMetaData.INSTANCE);
		repositoryCollection.add(UIParameterMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowParameterValueMetaData.INSTANCE);
		repositoryCollection.add(UIParameterMappingMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowParameterMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowProtocolMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowNodeMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowMetaData.INSTANCE, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				if (!(repository instanceof CrudRepository))
				{
					throw new RuntimeException("Repository [" + repository.getName() + "] must be a CrudRepository");
				}
				return new UIWorkflowDecorator((CrudRepository) repository, repositoryCollection,
						workflowHandlerRegistratorService, analysisHandlerRegistratorService);
			}
		});
		repositoryCollection.add(UIParameterValueMetaData.INSTANCE);
		repositoryCollection.add(UIBackendMetaData.INSTANCE);
		repositoryCollection.add(AnalysisMetaData.INSTANCE, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				if (!(repository instanceof CrudRepository))
				{
					throw new RuntimeException("Repository [" + repository.getName() + "] must be a CrudRepository");
				}
				return new AnalysisDecorator((CrudRepository) repository, dataService);
			}
		});
		repositoryCollection.add(AnalysisJobMetaData.INSTANCE, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				if (!(repository instanceof CrudRepository))
				{
					throw new RuntimeException("Repository [" + repository.getName() + "] must be a CrudRepository");
				}
				return new AnalysisJobDecorator((CrudRepository) repository, dataService);
			}
		});
		metaDataService.refreshCaches();

		LOG.info("Registered ComputeUI MySQL repositories");
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}
}