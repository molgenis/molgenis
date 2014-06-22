package org.molgenis.data.examples;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class DataApiExample extends AbstractTestNGSpringContextTests
{
	@Autowired
	DataService dataService;

	@Test
	public void test()
	{
		Repository r = dataService.getRepositoryByEntityName("Users");

		for (Entity e : r)
		{
			System.out.println(e);
            //outputs
		}

		Writable repo = dataService.getWritableRepository("Users");

		// untyped
		MapEntity user = new MapEntity();
		user.set("username", "john");
		user.set("active", true);

		repo.add(user);

		// typed, if we used good ol' XML -> JPA code generator
		User u = new User();
		u.setUsername("jane");
		u.setActive(true);

		repo.add(u);

		// streaming
		repo.add(new CsvRepository("users.csv"));

		// User{name:username, active:true}

		Query q = dataService.query("Users");

		// iterator
		for (Entity e : q.eq("username", "john"))
		{
			System.out.println(e);
		}

		// count
		System.out.println(q.gt("age", 65).count());

		// type safe
		for (User p : q.findAll(User.class))
		{
			System.out.println(p.getUsername());
		}

		Updateable dao = dataService.getCrudRepository("Users");

		u.setUsername("jane2");

		// update
		dao.update(u);

		// update streaming
		dao.update(new CsvRepository("updatedUsers.csv"));

		// delete
		dao.delete(u);

		// delete streaming
		dao.delete(q.lt("age", 21));

		// adding a repo
		dataService.addRepository(new CsvRepository("browseThis.csv"));

	}

}
