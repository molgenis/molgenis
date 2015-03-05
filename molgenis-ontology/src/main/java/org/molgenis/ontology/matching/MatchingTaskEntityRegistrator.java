package org.molgenis.ontology.matching;

import org.molgenis.data.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class MatchingTaskEntityRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger LOG = LoggerFactory.getLogger(MatchingTaskEntityRegistrator.class);

	private final DataService dataService;

	@Autowired
	public MatchingTaskEntityRegistrator(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (!dataService.hasRepository(MatchingTaskEntityMetaData.ENTITY_NAME))
		{
			LOG.info("Created table " + MatchingTaskEntityMetaData.ENTITY_NAME);
			dataService.getMeta().addEntityMeta(MatchingTaskEntityMetaData.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + MatchingTaskEntityMetaData.ENTITY_NAME + " existed");
		}

		if (!dataService.hasRepository(MatchingTaskContentEntity.ENTITY_NAME))
		{
			LOG.info("Created table " + MatchingTaskContentEntity.ENTITY_NAME);
			dataService.getMeta().addEntityMeta(MatchingTaskContentEntity.getEntityMetaData());
		}
		else
		{
			LOG.info("Table " + MatchingTaskContentEntity.ENTITY_NAME + " existed");
		}
	}
}
