package org.molgenis.integrationtest.data.abstracts.model;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.file.FileMeta;

public class AbstractFileDatatypeIT extends AbstractDatatypeIT
{
	@Override
	public EntityMetaData createMetaData()
	{
		EntityMetaData entityMetaData = new EntityMetaDataImpl("FileTest");
		entityMetaData.addAttribute("identifier", ROLE_ID).setNillable(false);
		//		entityMetaData.addAttribute("file").setDataType(FILE).setRefEntity(FileMeta.META_DATA); // FIXME

		return entityMetaData;
	}

	@Override
	public void populateTestEntity(Entity entity) throws Exception
	{
		entity.set("identifier", "one");

		FileMeta file = null;//new FileMeta(dataService);// FIXME
		file.setId("image");
		file.setContentType("image/jpg");
		file.setFilename("image.jpg");
		file.setSize(Long.valueOf(2000L));
		file.setUrl("http://www.myurl.com/image.jpg");
		//dataService.add(FileMeta.META_DATA.getName(), file); // FIXME

		entity.set("file", file);
	}

	@Override
	public void verifyTestEntityAfterInsert(Entity entity) throws Exception
	{
		assertEquals(entity.get("identifier"), "one");

		FileMeta fileMeta = entity.getEntity("file", FileMeta.class);
		assertNotNull(fileMeta);
		assertEquals(fileMeta.getId(), "image");
		assertEquals(fileMeta.getFilename(), "image.jpg");
		assertEquals(fileMeta.getSize(), Long.valueOf(2000L));
		assertEquals(fileMeta.getContentType(), "image/jpg");
		assertEquals(fileMeta.getUrl(), "http://www.myurl.com/image.jpg");
		assertEquals(fileMeta.getOwnerUsername(), "admin");
	}

	@Override
	public void updateTestEntity(Entity entity) throws Exception
	{
		FileMeta file = null;//new FileMeta(dataService); // FIXME
		file.setId("image1");
		file.setContentType("image/jpg");
		file.setFilename("image1.jpg");
		file.setSize(Long.valueOf(2000L));
		file.setUrl("http://www.myurl.com/image1.jpg");
		//dataService.add(FileMeta.META_DATA.getName(), file); // FIXME

		entity.set("file", file);
	}

	@Override
	public void verifyTestEntityAfterUpdate(Entity entity) throws Exception
	{
		FileMeta fileMeta = null; // entity.getEntity("file", FileMeta.class); // FIXME
		assertNotNull(fileMeta);
		assertEquals(fileMeta.getId(), "image1");
		assertEquals(fileMeta.getFilename(), "image1.jpg");
		assertEquals(fileMeta.getSize(), Long.valueOf(2000L));
		assertEquals(fileMeta.getContentType(), "image/jpg");
		assertEquals(fileMeta.getUrl(), "http://www.myurl.com/image1.jpg");
		assertEquals(fileMeta.getOwnerUsername(), "admin");
	}

}
