package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.slf4j.LoggerFactory.getLogger;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class OneToManyTypeEditing extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = getLogger(OneToManyTypeEditing.class);

	@Autowired
	IndexJobScheduler indexService;

	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	DataService dataService;

	@Autowired
	EntityManager entityManager;

	@Autowired
	MetaDataService metaDataService;

	// FIXME Illegal attribute type [ONE_TO_MANY]
	public void testNoConversionsAllowed()
	{
		EntityType entityType = entityTypeFactory.create("1");
		entityType.setBackend("PostgreSQL");

		EntityType refEntity = entityTypeFactory.create("2");
		refEntity.setBackend("PostgreSQL");

		entityType.addAttribute(attributeFactory.create().setName("id").setIdAttribute(true), ROLE_ID);
		entityType.addAttribute(
				attributeFactory.create().setName("oneToMany").setDataType(ONE_TO_MANY).setRefEntity(refEntity));

		refEntity.addAttribute(attributeFactory.create().setName("id").setIdAttribute(true), ROLE_ID);
		refEntity.addAttribute(attributeFactory.create().setName("backRef").setDataType(XREF).setRefEntity(entityType));

		runAsSystem(() ->
		{
			metaDataService.addEntityType(entityType);
			metaDataService.addEntityType(refEntity);

			entityType.getAttribute("oneToMany").setDataType(STRING);
			entityType.getAttribute("oneToMany").setRefEntity(null);
			metaDataService.updateEntityType(entityType);
		});
	}
}
