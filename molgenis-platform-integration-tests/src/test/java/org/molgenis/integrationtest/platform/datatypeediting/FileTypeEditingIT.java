package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class FileTypeEditingIT extends AbstractTestNGSpringContextTests
{
	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private MetaDataService metaDataService;

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Attribute data type update from \\[FILE\\] to \\[STRING\\] not allowed, allowed types are \\[\\]")
	public void testNoConversionsAllowed()
	{
		EntityType entityType = entityTypeFactory.create("FileEntity").setLabel("File entity");
		entityType.setBackend("PostgreSQL");

		entityType.addAttribute(attributeFactory.create().setName("id").setIdAttribute(true), ROLE_ID);
		entityType.addAttribute(attributeFactory.create()
												.setName("fileRef")
												.setDataType(FILE)
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
