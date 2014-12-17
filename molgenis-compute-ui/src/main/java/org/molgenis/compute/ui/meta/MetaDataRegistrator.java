package org.molgenis.compute.ui.meta;

import org.molgenis.compute.ui.model.decorator.UIWorkflowDecorator;
import org.molgenis.compute.ui.workflow.WorkflowHandlerRegistratorService;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class MetaDataRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final MysqlRepositoryCollection repositoryCollection;
	private final MetaDataService metaDataService;
	private final WorkflowHandlerRegistratorService workflowHandlerRegistratorService;

	@Autowired
	public MetaDataRegistrator(MysqlRepositoryCollection repositoryCollection, MetaDataService metaDataService,
			WorkflowHandlerRegistratorService workflowHandlerRegistratorService)
	{
		this.repositoryCollection = repositoryCollection;
		this.metaDataService = metaDataService;
		this.workflowHandlerRegistratorService = workflowHandlerRegistratorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		repositoryCollection.add(UIParameterMetaData.INSTANCE);
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
						workflowHandlerRegistratorService);
			}
		});
		repositoryCollection.add(UIParameterValueMetaData.INSTANCE);
		repositoryCollection.add(AnalysisJobMetaData.INSTANCE);
		repositoryCollection.add(UIBackendMetaData.INSTANCE);
		repositoryCollection.add(AnalysisMetaData.INSTANCE);
		metaDataService.refreshCaches();
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE;
	}
}