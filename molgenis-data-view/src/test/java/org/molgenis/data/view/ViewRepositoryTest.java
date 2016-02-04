package org.molgenis.data.view;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.mem.InMemoryRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeClass;

public class ViewRepositoryTest
{
	EditableEntityMetaData entityMetaA;
	EditableEntityMetaData entityMetaB;
	EditableEntityMetaData entityMetaC;

	Repository repoA;
	Repository repoB;
	Repository repoC;

	@BeforeClass
	public void setupBeforeClass()
	{
		// entity A (original entity)
		entityMetaA = new DefaultEntityMetaData("entityA");
		entityMetaA.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaA.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaA.addAttribute("A1").setDataType(STRING);
		entityMetaA.addAttribute("A2").setDataType(STRING);
		entityMetaA.addAttribute("A3").setDataType(STRING);

		// entity B
		entityMetaB = new DefaultEntityMetaData("entityB");
		entityMetaB.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaB.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaB.addAttribute("B1").setDataType(STRING);
		entityMetaB.addAttribute("B2").setDataType(STRING);
		entityMetaB.addAttribute("B3").setDataType(STRING);

		// entity C
		entityMetaC = new DefaultEntityMetaData("entityC");
		entityMetaC.addAttribute("id", ROLE_ID).setDataType(STRING);
		entityMetaC.addAttribute("chrom").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("pos").setDataType(STRING).setNillable(false);
		entityMetaC.addAttribute("C1").setDataType(STRING);
		entityMetaC.addAttribute("C2").setDataType(STRING);
		entityMetaC.addAttribute("C3").setDataType(STRING);

		// make repositories
		repoA = new InMemoryRepository(entityMetaA);
		repoB = new InMemoryRepository(entityMetaB);
		repoC = new InMemoryRepository(entityMetaC);

		// entityA2 joins with entityB2
		// entityA3 joins with entityB3
		Entity entityA1 = new MapEntity(entityMetaA);
		entityA1.set("id", "1");
		entityA1.set("chrom", "1");
		entityA1.set("pos", "25");
		entityA1.set("A1", "testA1");

		Entity entityA2 = new MapEntity(entityMetaA);
		entityA2.set("id", "2");
		entityA2.set("chrom", "2");
		entityA2.set("pos", "50");
		entityA2.set("A1", "testA2");

		Entity entityA3 = new MapEntity(entityMetaA);
		entityA3.set("id", "3");
		entityA3.set("chrom", "2");
		entityA3.set("pos", "75");
		entityA3.set("A1", "testA3");

		Entity entityB1 = new MapEntity(entityMetaB);
		entityB1.set("id", "1");
		entityB1.set("chrom", "1");
		entityB1.set("pos", "10");
		entityB1.set("B1", "testB1");

		Entity entityB2 = new MapEntity(entityMetaB);
		entityB2.set("id", "2");
		entityB2.set("chrom", "2");
		entityB2.set("pos", "50");
		entityB2.set("B1", "testA2");

		Entity entityB3 = new MapEntity(entityMetaB);
		entityB3.set("id", "3");
		entityB3.set("chrom", "2");
		entityB3.set("pos", "85");
		entityB3.set("B1", "testA3");

		// populate repositories
		repoA.add(entityA1);
		repoA.add(entityA2);
		repoA.add(entityA3);

		repoB.add(entityB1);
		repoB.add(entityB2);
		repoB.add(entityB3);
	}
}
