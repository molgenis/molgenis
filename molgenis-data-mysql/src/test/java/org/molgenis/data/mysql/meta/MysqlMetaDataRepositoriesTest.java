package org.molgenis.data.mysql.meta;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.MysqlTestConfig;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.Range;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@ContextConfiguration(classes = MysqlTestConfig.class)
public class MysqlMetaDataRepositoriesTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetaDataServiceImpl metaDataService;

	@Autowired
	private MysqlRepositoryCollection coll;

	@BeforeMethod
	@AfterClass
	public void beforeMethod()
	{
		try
		{
			metaDataService.recreateMetaDataRepositories();
		}
		catch (UnknownEntityException e)
		{

		}
	}

	@Test
	public void addAndGetAttributeMetaData()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("test");
		emd.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(emd);

		List<String> enumOptions = Arrays.asList("enum1", "enum2");
		AttributeMetaData enumAttr = emd.addAttribute("enum0").setDataType(new EnumField()).setEnumOptions(enumOptions);
		metaDataService.addAttribute(emd.getName(), enumAttr);

		AttributeMetaData intRangeAttr = emd.addAttribute("intrange").setDataType(MolgenisFieldTypes.INT)
				.setRange(new Range(1l, 5l));
		metaDataService.addAttribute(emd.getName(), intRangeAttr);

		List<AttributeMetaData> retrieved = Lists.newArrayList(metaDataService.getEntityMetaData(emd.getName())
				.getAttributes());

		assertNotNull(retrieved);
		assertEquals(retrieved.size(), 3);

		assertEquals(retrieved.get(0).getName(), "id");
		assertNotNull(retrieved.get(0).getDataType());
		assertEquals(retrieved.get(0).getDataType().getEnumType(), FieldTypeEnum.STRING);

		assertEquals(retrieved.get(1).getName(), "enum0");
		assertNotNull(retrieved.get(1).getDataType());
		assertEquals(retrieved.get(1).getDataType().getEnumType(), FieldTypeEnum.ENUM);
		assertEquals(retrieved.get(1).getEnumOptions(), enumOptions);

		assertEquals(retrieved.get(2).getName(), "intrange");
		assertNotNull(retrieved.get(2).getDataType());
		assertEquals(retrieved.get(2).getDataType().getEnumType(), FieldTypeEnum.INT);
		assertNotNull(retrieved.get(2).getRange());
		assertEquals(retrieved.get(2).getRange().getMin(), Long.valueOf(1l));
		assertEquals(retrieved.get(2).getRange().getMax(), Long.valueOf(5l));
	}

	@Test
	public void addAndGetEntityMetaData()
	{
		DefaultEntityMetaData test = new DefaultEntityMetaData("testje");
		test.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test);

		DefaultEntityMetaData extendsTest = new DefaultEntityMetaData("extendstest");
		extendsTest.setExtends(test);
		metaDataService.addEntityMeta(extendsTest);

		EntityMetaData retrieved = metaDataService.getEntityMetaData("extendstest");
		assertNotNull(retrieved);
		assertEquals(retrieved.getName(), "extendstest");
		assertEquals(retrieved.getExtends(), test);
	}

	@Test
	public void getEntityMetaDataNotFound()
	{
		assertNull(metaDataService.getEntityMetaData("unknown"));
	}

	@Test
	public void getEntityMetaDatas()
	{
		DefaultEntityMetaData test = new DefaultEntityMetaData("test");
		test.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test);

		DefaultEntityMetaData test1 = new DefaultEntityMetaData("test1");
		test1.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test1);

		DefaultEntityMetaData test2 = new DefaultEntityMetaData("test2");
		test2.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test2);

		List<EntityMetaData> meta = Lists.newArrayList(metaDataService.getEntityMetaDatas());
		assertNotNull(meta);
		assertEquals(meta.size(), 3);
		assertTrue(meta.contains(test));
		assertTrue(meta.contains(test1));
		assertTrue(meta.contains(test2));
	}

	@Test
	public void getEntityMetaDatasForPackage()
	{

		PackageImpl p1 = new PackageImpl("p1", "Package1", null);
		metaDataService.addPackage(p1);

		Package p2 = new PackageImpl("p2", "Package2", p1);
		metaDataService.addPackage(p2);

		DefaultEntityMetaData test = new DefaultEntityMetaData("test");
		test.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test);

		DefaultEntityMetaData test1 = new DefaultEntityMetaData("test1", p1);
		test1.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test1);

		DefaultEntityMetaData test2 = new DefaultEntityMetaData("test2", p2);
		test2.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test2);

		DefaultEntityMetaData test3 = new DefaultEntityMetaData("test3", p2);
		test3.addAttribute("id").setIdAttribute(true).setNillable(false);
		metaDataService.addEntityMeta(test3);

		assertEquals(metaDataService.getPackage("p1_p2").getEntityMetaDatas(),
				Collections.unmodifiableList(Arrays.asList(test2, test3)));
	}

	@Test
	public void addAndGetPackage()
	{
		PackageImpl test = new PackageImpl("ase", "The ASE package.");
		metaDataService.addPackage(test);

		Package retrieved = metaDataService.getPackage("ase");
		assertEquals(retrieved, test);
	}

	@Test
	public void getPackages()
	{
		PackageImpl test = new PackageImpl("ase", "The ASE package.");
		metaDataService.addPackage(test);

		PackageImpl molgenis = new PackageImpl("molgenis", "The Molgenis package.");
		metaDataService.addPackage(molgenis);

		Package defaultPackage = metaDataService.getPackage("base");

		assertEquals(metaDataService.getRootPackages(), Arrays.asList(test, molgenis, defaultPackage));
	}

}
