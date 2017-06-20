package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.elasticsearch.index.job.IndexJobScheduler;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.slf4j.LoggerFactory.getLogger;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class FileTypeEditing extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = getLogger(FileTypeEditing.class);

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

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Attribute data type update from \\[FILE\\] to \\[STRING\\] not allowed, allowed types are \\[\\]")
	public void testNoConversionsAllowed()
	{
		EntityType entityType = entityTypeFactory.create("FileEntity").setLabel("File entity");
		entityType.setBackend("PostgreSQL");

		entityType.addAttribute(attributeFactory.create().setName("id").setIdAttribute(true), ROLE_ID);
		entityType.addAttribute(attributeFactory.create().setName("fileRef").setDataType(FILE)
				.setRefEntity(dataService.getEntityType(FILE_META)));

		runAsSystem(() ->
		{
			metaDataService.addEntityType(entityType);

			entityType.getAttribute("fileRef").setDataType(STRING);
			entityType.getAttribute("fileRef").setRefEntity(null);
			metaDataService.updateEntityType(entityType);
		});
	}
}
