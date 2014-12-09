package org.molgenis.compute.ui.meta;

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

	@Autowired
	public MetaDataRegistrator(MysqlRepositoryCollection repositoryCollection)
	{
		this.repositoryCollection = repositoryCollection;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		repositoryCollection.add(UIParameterMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowParameterMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowProtocolMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowNodeMetaData.INSTANCE);
		repositoryCollection.add(UIWorkflowMetaData.INSTANCE);
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}
}