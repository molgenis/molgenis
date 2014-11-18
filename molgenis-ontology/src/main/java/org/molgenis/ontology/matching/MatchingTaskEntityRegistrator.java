package org.molgenis.ontology.matching;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class MatchingTaskEntityRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private static final Logger logger = Logger.getLogger(MatchingTaskEntityRegistrator.class);
	private final static String MATCHING_TASK_ENTITY_NAME = "MatchingTask";
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
		if (!dataService.hasRepository(MATCHING_TASK_ENTITY_NAME))
		{
			logger.info("Created table " + MATCHING_TASK_ENTITY_NAME);
			mysqlRepositoryCollection.add(getEntityMetaData());
		}
	}

	private EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(MATCHING_TASK_ENTITY_NAME);
		DefaultAttributeMetaData identifierAttr = new DefaultAttributeMetaData("Identifier");
		identifierAttr.setIdAttribute(true);
		identifierAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(identifierAttr);
		DefaultAttributeMetaData dataCreatedAttr = new DefaultAttributeMetaData("Data_created", FieldTypeEnum.DATE_TIME);
		dataCreatedAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(dataCreatedAttr);
		DefaultAttributeMetaData molgenisUserAttr = new DefaultAttributeMetaData("Molgenis_user");
		molgenisUserAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(molgenisUserAttr);
		DefaultAttributeMetaData codeSystemAttr = new DefaultAttributeMetaData("Code_system");
		codeSystemAttr.setNillable(false);
		entityMetaData.addAttributeMetaData(codeSystemAttr);
		return entityMetaData;
	}
}
