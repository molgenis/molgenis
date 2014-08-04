package org.molgenis.data.mongodb;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;
import org.testng.internal.annotations.Sets;

import com.google.common.collect.Iterables;

public class MRefTest extends AbstractMongoRepositoryTest
{
	@Test
	public void mrefTest()
	{
		DefaultEntityMetaData userMD = new DefaultEntityMetaData("user");
		userMD.addAttribute("email").setIdAttribute(true).setNillable(false);
		userMD.addAttribute("name");

		DefaultEntityMetaData groupMD = new DefaultEntityMetaData("group");
		groupMD.addAttribute("name").setIdAttribute(true).setNillable(false);
		groupMD.addAttribute("users").setDataType(MolgenisFieldTypes.MREF).setRefEntity(userMD);

		MongoRepository userRepo = createRepo(userMD);
		MongoRepository groupRepo = createRepo(groupMD);

		// Add entities
		Entity user1 = new MapEntity();
		user1.set("email", "piet@gmail.com");
		user1.set("name", "Piet");
		userRepo.add(user1);

		Entity user2 = new MapEntity();
		user2.set("email", "klaas@gmail.com");
		user2.set("name", "Klaas");
		userRepo.add(user2);
		assertEquals(userRepo.count(), 2);

		Entity group = new MapEntity();
		group.set("name", "All users");
		group.set("users", Arrays.asList(user1, user2));
		groupRepo.add(group);
		assertEquals(groupRepo.count(), 1);

		// Retrieve
		Entity retrievedGroup = groupRepo.iterator().next();
		Iterable<Entity> refs = retrievedGroup.getEntities("users");
		Set<String> names = Sets.newHashSet();
		for (Entity user : refs)
		{
			names.add(user.getString("name"));
		}
		assertEquals(names.size(), 2);
		assertTrue(names.contains("Piet"));
		assertTrue(names.contains("Klaas"));

		// Update
		retrievedGroup.set("users", Arrays.asList(user1));
		groupRepo.update(retrievedGroup);
		user1.set("name", "Karel");
		userRepo.update(user1);

		retrievedGroup = groupRepo.iterator().next();
		refs = retrievedGroup.getEntities("users");
		assertEquals(Iterables.size(refs), 1);
		assertEquals(refs.iterator().next().get("name"), "Karel");
	}

}
