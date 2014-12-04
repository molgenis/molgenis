package org.molgenis.ontology.matching;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class MatchingTaskEntityRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger logger = Logger.getLogger(MatchingTaskEntityRegistrator.class);
	private final DataService dataService;
	private final MysqlRepositoryCollection mysqlRepositoryCollection;

	@Autowired
	public MatchingTaskEntityRegistrator(DataService dataService, MysqlRepositoryCollection mysqlRepositoryCollection)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		if (mysqlRepositoryCollection == null) throw new IllegalArgumentException("MysqlRepositoryCollection is null");
		this.dataService = dataService;
		this.mysqlRepositoryCollection = mysqlRepositoryCollection;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (!dataService.hasRepository(MatchingTaskEntity.ENTITY_NAME))
		{
			logger.info("Created table " + MatchingTaskEntity.ENTITY_NAME);
			mysqlRepositoryCollection.add(MatchingTaskEntity.getEntityMetaData());
		}
		else
		{
			logger.info("Table " + MatchingTaskEntity.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(MatchingTaskContentEntity.ENTITY_NAME))
		{
			logger.info("Created table " + MatchingTaskContentEntity.ENTITY_NAME);
			mysqlRepositoryCollection.add(MatchingTaskContentEntity.getEntityMetaData());
		}
		else
		{
			logger.info("Table " + MatchingTaskContentEntity.ENTITY_NAME + " existed");
		}
	}
}
