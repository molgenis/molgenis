package org.molgenis.data.jpa;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryResolver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseJpaTest
{
	protected DataService dataService;
	private EntityManager entityManager;
	protected JpaRepository repo;
	protected FileRepositoryCollectionFactory fileRepositorySourceFactory;

	@BeforeMethod
	public void beforeMethod()
	{
		entityManager = Persistence.createEntityManagerFactory("molgenis").createEntityManager();
		fileRepositorySourceFactory = new FileRepositoryCollectionFactory();
		dataService = new DataServiceImpl();
		fileRepositorySourceFactory.addFileRepositoryCollectionClass(ExcelRepositoryCollection.class,
				ExcelRepositoryCollection.EXTENSIONS);
		repo = new JpaRepository(entityManager, new PersonMetaData(), new QueryResolver(dataService));
		dataService.addRepository(repo);
		entityManager.getTransaction().begin();
	}

	@AfterMethod
	public void afterMethod()
	{
		entityManager.getTransaction().rollback();
		entityManager.close();
	}

}
