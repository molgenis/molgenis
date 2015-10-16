package org.molgenis.data.support;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultEntityTest
{
	private DefaultEntityMetaData emd;
	private DefaultEntity entity;
	private Date utilDate = new Date();
	@Mock
	DataService dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		emd = new DefaultEntityMetaData("Entity");
		emd.addAttributeMetaData(new DefaultAttributeMetaData("id").setIdAttribute(true));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("xdatetime", FieldTypeEnum.DATE_TIME));
		entity = new DefaultEntity(emd, dataService);
		entity.set("xdatetime", utilDate);
	}

	@Test
	public void testGetForAttributeWithTypeDateTime()
	{
		Object actual = entity.get("xdatetime");
		Assert.assertEquals(Date.class, actual.getClass());
		assertEquals(utilDate, actual);
	}
	
	@Test
	public void testGetUtilDateForAttributeWithTypeDateTime()
	{
		Object actual = entity.getUtilDate("xdatetime");
		Assert.assertEquals(Date.class, actual.getClass());
		assertEquals(utilDate, actual);
	}
	
	@Test
	public void testGetDateForAttributeWithTypeDateTime()
	{
		Object actual = entity.getDate("xdatetime");
		Assert.assertEquals(java.sql.Date.class, actual.getClass());
		assertEquals(new java.sql.Date(utilDate.getTime()), actual);
	}
}
