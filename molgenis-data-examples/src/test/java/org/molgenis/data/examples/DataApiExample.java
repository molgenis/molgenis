package org.molgenis.data.examples;

import static org.molgenis.data.support.QueryImpl.EQ;

import java.io.File;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class DataApiExample extends AbstractTestNGSpringContextTests
{
	@Autowired
	DataService dataService;

	// @Test
	public void testStatic()
	{
		// Add some users
		dataService.add(UserMetaData.ENTITY_NAME, new User("Piet", true));
		dataService.add(UserMetaData.ENTITY_NAME, new User("Klaas", false));

		// Retrieve them and print
		printUsers();

		// Find Klaas
		User klaas = dataService.findOne(UserMetaData.ENTITY_NAME, "Klaas", User.class);
		System.out.println(klaas);

		// Make klaas active
		klaas.setActive(true);
		dataService.update(UserMetaData.ENTITY_NAME, klaas);

		// Find all active
		dataService.findAll(UserMetaData.ENTITY_NAME, EQ(UserMetaData.ACTIVE, true), User.class)
				.forEach(System.out::println);
		// OR ??
		dataService.getRepository(UserMetaData.ENTITY_NAME).query().eq(UserMetaData.ACTIVE, true)
				.forEach(System.out::println);

		// Delete one
		dataService.delete(UserMetaData.ENTITY_NAME, "Piet");
		printUsers();

		// Discover capabilities of repo
		dataService.getCapabilities(UserMetaData.ENTITY_NAME).forEach(System.out::println);

		// Add streaming
		File usersCsv = ResourceUtils.getFile("users.csv");
		dataService.add(UserMetaData.ENTITY_NAME, new CsvRepository(usersCsv, null).stream());
		printUsers();
	}

	private void printUsers()
	{
		dataService.findAll(UserMetaData.ENTITY_NAME, User.class).forEach(System.out::println);
	}

	// @Test
	public void testDynamic()
	{
		// Create new dynamic repo
		DefaultEntityMetaData emd = new DefaultEntityMetaData("City");
		emd.addAttribute("name").setIdAttribute(true).setNillable(false);
		emd.addAttribute("population").setDataType(MolgenisFieldTypes.INT);

		Repository repo = dataService.getMeta().addEntityMeta(emd);

		// Add entities to it
		Entity amsterdam = new MapEntity();
		amsterdam.set("name", "Amsterdam");
		amsterdam.set("population", 813562);
		repo.add(amsterdam);

		Entity london = new MapEntity();
		london.set("name", "London");
		london.set("population", 8416535);
		repo.add(london);

		// Retrieve all entities of repo
		dataService.findAll("City").forEach((entity) -> System.out.println(entity.get("name")));

		// Add attribute
		emd.addAttribute("country");
		dataService.getMeta().updateEntityMeta(emd);

		// Print attributes
		dataService.getEntityMetaData("City").getAtomicAttributes()
				.forEach((attr) -> System.out.println(attr.getName()));

		// Update entity
		amsterdam.set("country", "Netherlands");
		dataService.update("City", amsterdam);
		Entity entity = dataService.findOne("City", "Amsterdam");
		System.out.println(entity.get("name") + ": " + entity.get("country"));
	}

	@Test
	public void testRepositoryCollections()
	{
		// Print all available backends
		dataService.getMeta().forEach((backend) -> System.out.println(backend.getName()));

		// Add cities to MyRepo
		DefaultEntityMetaData emd = new DefaultEntityMetaData("City1");
		emd.setBackend("MyRepos");
		emd.addAttribute("name").setIdAttribute(true).setNillable(false);
		emd.addAttribute("population").setDataType(MolgenisFieldTypes.INT);

		Repository repo = dataService.getMeta().addEntityMeta(emd);
		System.out.println(repo);

		// Add entities to it
		Entity amsterdam = new MapEntity();
		amsterdam.set("name", "Amsterdam");
		amsterdam.set("population", 813562);
		repo.add(amsterdam);

		Entity london = new MapEntity();
		london.set("name", "London");
		london.set("population", 8416535);
		repo.add(london);

		// Retrieve all entities of repo
		repo.forEach((entity) -> System.out.println(entity.get("name")));
	}
}
