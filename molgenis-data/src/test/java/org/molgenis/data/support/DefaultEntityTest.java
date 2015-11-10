package org.molgenis.data.support;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultEntityTest
{
	private DefaultEntityMetaData refEmd;
	private DefaultEntity refEntity1;
	private DefaultEntity refEntity2;

	private DefaultEntityMetaData emd;
	private DefaultEntity entity;
	private Date utilDate = new Date();
	@Mock
	DataService dataService;

	@BeforeMethod
	public void beforeClass()
	{
		MockitoAnnotations.initMocks(this);

		refEmd = new DefaultEntityMetaData("refEntity");
		refEmd.addAttributeMetaData(new DefaultAttributeMetaData("id").setIdAttribute(true));

		refEntity1 = new DefaultEntity(refEmd, dataService);
		refEntity1.set("id", "test");

		refEntity2 = new DefaultEntity(refEmd, dataService);
		refEntity2.set("id", "test2");

		when(dataService.findOne("refEntity", "test")).thenReturn(refEntity1);
		when(dataService.findOne("refEntity", "test2")).thenReturn(refEntity2);

		emd = new DefaultEntityMetaData("Entity");
		emd.addAttributeMetaData(new DefaultAttributeMetaData("id").setIdAttribute(true));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("xdatetime", FieldTypeEnum.DATE_TIME));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("xref", FieldTypeEnum.XREF).setRefEntity(refEmd));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("mref", FieldTypeEnum.MREF).setRefEntity(refEmd));

		entity = new DefaultEntity(emd, dataService);
		entity.set("xdatetime", utilDate);
		entity.set("xref", refEntity1);
		entity.set("mref", Arrays.asList(refEntity1, refEntity2));
	}

	@Test
	public void testGetEntity()
	{
		Object actual = entity.getEntity("xref");
		assertEquals(actual.getClass(), DefaultEntity.class);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testGetEntityForAttributeOtherThanXrefOrCategorical1()
	{
		entity.getEntity("xdatetime");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testGetEntityForAttributeOtherThanXrefOrCategorical2()
	{
		entity.getEntity("mref");
	}

	@Test
	public void testGetEntities()
	{
		Iterator<Entity> result = entity.getEntities("mref").iterator();
		assertEquals(result.next(), refEntity1);
		assertEquals(result.next(), refEntity2);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testGetEntitiesForAttributeOtherThanRef()
	{
		entity.getEntities("xdatetime");
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
