package org.molgenis.data.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Package;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class PackageRepositoryDecoratorTest
{
	private Repository<Package> delegateRepository;
	private PackageRepositoryDecorator packageRepositoryDecorator;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUpBeforeMethod()
	{
		delegateRepository = mock(Repository.class);
		EntityTypeDependencyResolver entityTypeDependencyResolver = mock(EntityTypeDependencyResolver.class);
		DataService dataService = mock(DataService.class);
		packageRepositoryDecorator = new PackageRepositoryDecorator(delegateRepository, dataService,
				entityTypeDependencyResolver);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPackageRepositoryDecorator() throws Exception
	{
		new PackageRepositoryDecorator(null, null, null);
	}
}