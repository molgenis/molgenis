package org.molgenis.data.mysql;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.TreeSet;

import org.molgenis.AppConfig;
import org.molgenis.data.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class PackageRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MysqlPackageRepository packageRepository;

	@Autowired
	private EntityMetaDataRepository entityMetaDataRepository;

	@Autowired
	private AttributeMetaDataRepository attributeMetaDataRepository;

	@BeforeMethod
	@AfterClass
	public void beforeMethod()
	{
		attributeMetaDataRepository.deleteAll();
		entityMetaDataRepository.deleteAll();
		packageRepository.deleteAll();
		packageRepository.addDefaultPackage();
	}

	@Test
	public void addAndGetPackage()
	{
		PackageImpl test = new PackageImpl("ase", "The ASE package.");
		packageRepository.addPackage(test);

		Package retrieved = packageRepository.getPackage("ase");
		assertEquals(test, retrieved);
	}

	@Test
	public void getPackages()
	{
		PackageImpl test = new PackageImpl("ase", "The ASE package.");
		packageRepository.addPackage(test);

		PackageImpl molgenis = new PackageImpl("molgenis", "The Molgenis package.");
		packageRepository.addPackage(molgenis);

		Package defaultPackage = packageRepository.getPackage("default");

		assertEquals(new TreeSet<Package>(Arrays.asList(test, defaultPackage, molgenis)),
				packageRepository.getPackages());
	}
}
