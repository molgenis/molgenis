package org.molgenis.data.jpa;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.data.DataService;
import org.molgenis.data.excel.ExcelRepositorySource;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseJpaTest
{
	protected DataService dataService;
	private EntityManager entityManager;
	protected JpaRepository repo;

	@BeforeMethod
	public void beforeMethod()
	{
		entityManager = Persistence.createEntityManagerFactory("molgenis").createEntityManager();
		dataService = new DataServiceImpl();
		dataService.addFileRepositorySourceClass(ExcelRepositorySource.class, ExcelRepositorySource.EXTENSIONS);
		repo = new JpaRepository(entityManager, Person.class, new PersonMetaData(), new DefaultEntityValidator(
				dataService, new EntityAttributesValidator()));
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
