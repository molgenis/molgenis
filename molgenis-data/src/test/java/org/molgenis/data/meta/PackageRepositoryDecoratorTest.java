package org.molgenis.data.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class PackageRepositoryDecoratorTest
{
	private Repository<Package> decoratedRepo;
	private PackageRepositoryDecorator packageRepositoryDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		decoratedRepo = mock(Repository.class);
		EntityTypeDependencyResolver entityTypeDependencyResolver = mock(EntityTypeDependencyResolver.class);
		DataService dataService = mock(DataService.class);
		packageRepositoryDecorator = new PackageRepositoryDecorator(decoratedRepo, dataService,
				entityTypeDependencyResolver);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPackageRepositoryDecorator() throws Exception
	{
		new PackageRepositoryDecorator(null, null, null);
	}

	@Test
	public void testDelegate() throws Exception
	{
		assertEquals(packageRepositoryDecorator.delegate(), decoratedRepo);
	}
}