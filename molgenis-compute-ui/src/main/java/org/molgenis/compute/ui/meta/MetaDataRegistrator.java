package org.molgenis.compute.ui.meta;

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

	@Autowired
	public MetaDataRegistrator(MysqlRepositoryCollection repositoryCollection, MetaDataService metaDataService)
	{
		this.repositoryCollection = repositoryCollection;
		this.metaDataService = metaDataService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		repositoryCollection.add(UIParameterMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowParameterMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowProtocolMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowNodeMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowMetaData.INSTANCE);
		repositoryCollection.add(UIParameterValueMetaData.INSTANCE);
		repositoryCollection.add(AnalysisJobMetaData.INSTANCE);
		repositoryCollection.add(UIBackendMetaData.INSTANCE);
		repositoryCollection.add(AnalysisMetaData.INSTANCE);
		metaDataService.refreshCaches();
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}
}