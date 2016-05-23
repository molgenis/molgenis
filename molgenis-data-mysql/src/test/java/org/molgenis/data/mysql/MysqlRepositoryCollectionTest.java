package org.molgenis.data.mysql;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.Locale;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MysqlTestConfig;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MysqlTestConfig.class)
public class MysqlRepositoryCollectionTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection coll;

	@Test
	public void test()
	{
		// delete old stuff
		if (coll.getRepository("coll_person") != null) coll.deleteEntityMeta("coll_person");

		// create collection, add repo, destroy and reload
		DefaultEntityMetaData personMD = new DefaultEntityMetaData("coll_person");
		personMD.addAttribute("email", ROLE_ID);
		personMD.addAttribute("firstName");
		personMD.addAttribute("lastName");
		personMD.addAttribute("birthday").setDataType(MolgenisFieldTypes.DATE);
		personMD.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		personMD.addAttribute("active").setDataType(MolgenisFieldTypes.BOOL);

		// autowired ds
		coll.addEntityMeta(personMD);

		// destroy and rebuild
		Assert.assertNotNull(coll.getRepository("coll_person"));

		Repository repo = coll.getRepository("coll_person");
		String[] locale = Locale.getISOCountries();
		for (int i = 0; i < 10; i++)
		{
			Entity e = new MapEntity();
			e.set("email", i + "@localhost");
			e.set("firstName", locale[i]);
			e.set("height", 170 + i);
			e.set("birthday", "1992-03-1" + i);
			e.set("active", i % 2 == 0);
			repo.add(e);
		}

		// and again
		repo = coll.getRepository("coll_person");
		Assert.assertEquals(repo.count(), 10);
	}
}
