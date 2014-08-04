package org.molgenis.data.mongodb;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class XRefTest extends AbstractMongoRepositoryTest
{

	@Test
	public void testXref()
	{
		DefaultEntityMetaData forestMD = new DefaultEntityMetaData("Forest");
		forestMD.addAttribute("name").setIdAttribute(true);
		forestMD.addAttribute("country");

		DefaultEntityMetaData treeMD = new DefaultEntityMetaData("Tree");
		treeMD.addAttribute("name").setIdAttribute(true);
		treeMD.addAttribute("forest").setDataType(MolgenisFieldTypes.XREF).setRefEntity(forestMD);

		MongoRepository forestRepo = createRepo(forestMD);
		MongoRepository treeRepo = createRepo(treeMD);

		Entity forest = new MapEntity();
		forest.set("name", "veluwe");
		forest.set("country", "Netherlands");
		forestRepo.add(forest);

		Entity oak = new MapEntity();
		oak.set("name", "oak");
		oak.set("forest", forest);
		treeRepo.add(oak);

		Entity retrieved = treeRepo.findOne("oak");
		assertNotNull(retrieved);
		assertNotNull(retrieved.getEntity("forest"));
		assertEquals(retrieved.getEntity("forest").get("name"), "veluwe");
		assertEquals(retrieved.getEntity("forest").get("country"), "Netherlands");
	}

}
