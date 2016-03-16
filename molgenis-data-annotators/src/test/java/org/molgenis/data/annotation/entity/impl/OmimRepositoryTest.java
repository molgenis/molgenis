package org.molgenis.data.annotation.entity.impl;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.common.collect.Lists.newArrayList;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OmimRepositoryTest
{
	private OmimRepository repo;
	private File omimFile = new File("src/test/resources/omim/omim.txt");

	@BeforeClass
	public void setUp()
	{
		repo = new OmimRepository(omimFile);
	}

	@AfterClass
	public void shutDown() throws IOException
	{
		repo.close();
	}

	@Test
	public void count()
	{
		// There are 43 unique genes in the test file
		assertEquals(repo.count(), 43);
	}

	@Test
	public void iterator()
	{
		Iterator<Entity> iterator = repo.iterator();
		List<String> expectedIteratorContent = getExpectedIteratorContentList();
		int counter = 0;
		while (iterator.hasNext())
		{
			Entity entity = iterator.next();
			assertEquals(entity.toString(), expectedIteratorContent.get(counter));
			counter++;
		}
	}

	@Test
	public void findAllWithEmptyQuery() throws IOException
	{

		List<Entity> omimEntities = repo.findAll(new QueryImpl()).collect(toList());
		assertEquals(omimEntities.toString(),
				"[OMIM=[GeneSymbols=HADH2,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[GeneSymbols=CR3A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[GeneSymbols=ITGAM,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[GeneSymbols=MAC1A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[GeneSymbols=FOXE1,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[GeneSymbols=CRV,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[GeneSymbols=THPH2,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]], OMIM=[GeneSymbols=IFG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]], OMIM=[GeneSymbols=TTF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[GeneSymbols=TTF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[GeneSymbols=HSD17B10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[GeneSymbols=IFI,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]], OMIM=[GeneSymbols=CYP17A1,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]], OMIM=[GeneSymbols=DHTKD1,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[GeneSymbols=KIAA1304,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]], OMIM=[GeneSymbols=MYB,Phenotype=[{T-cell acute lymphoblastic leukemia} (3)],MIMNumber=[189990],CytoLocation=[6q23.3]], OMIM=[GeneSymbols=NKX2-1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[GeneSymbols=FKHL15,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[GeneSymbols=CYP17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]], OMIM=[GeneSymbols=CMT2Q,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[GeneSymbols=SRGAP1,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]], OMIM=[GeneSymbols=CD32,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]], OMIM=[GeneSymbols=RPRGL1,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]], OMIM=[GeneSymbols=NKX2A,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[GeneSymbols=TREX1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[GeneSymbols=TITF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[GeneSymbols=MTHFR,Phenotype=[{Thromboembolism, susceptibility to}, 188050 (3)],MIMNumber=[607093],CytoLocation=[1p36.22]], OMIM=[GeneSymbols=TITF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[GeneSymbols=NMTC4,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[GeneSymbols=NMTC2,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]], OMIM=[GeneSymbols=NMTC1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[GeneSymbols=HERNS,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[GeneSymbols=ERAB,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[GeneSymbols=MRXS10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[GeneSymbols=CD11B,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[GeneSymbols=F5,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]], OMIM=[GeneSymbols=IFNG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]], OMIM=[GeneSymbols=KIAA1630,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[GeneSymbols=AMOXAD,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[GeneSymbols=SLEB6,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[GeneSymbols=P450C17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]], OMIM=[GeneSymbols=AGS1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[GeneSymbols=FCGR2B,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]]]");
	}

	@Test
	public void findAllWithQuery() throws IOException
	{
		List<Entity> omimEntities = repo
				.findAll(new QueryImpl().eq(OmimRepository.OMIM_GENE_SYMBOLS_COL_NAME, "CYP17A1")).collect(toList());
		assertEquals(omimEntities.toString(),
				"[OMIM=[GeneSymbols=CYP17A1,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]]");

	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "The only query allowed on this Repository is gene EQUALS")
	public void findAllWithBadQuery()
	{
		repo.findAll(new QueryImpl().like(OmimRepository.OMIM_PHENOTYPE_COL_NAME, "test_phenotype"));
	}

	private List<String> getExpectedIteratorContentList()
	{
		return newArrayList(
				"OMIM=[GeneSymbols=HADH2,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[GeneSymbols=CR3A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[GeneSymbols=ITGAM,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[GeneSymbols=MAC1A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[GeneSymbols=FOXE1,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[GeneSymbols=CRV,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[GeneSymbols=THPH2,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]]",
				"OMIM=[GeneSymbols=IFG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]]",
				"OMIM=[GeneSymbols=TTF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[GeneSymbols=TTF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[GeneSymbols=HSD17B10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[GeneSymbols=IFI,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]]",
				"OMIM=[GeneSymbols=CYP17A1,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]",
				"OMIM=[GeneSymbols=DHTKD1,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[GeneSymbols=KIAA1304,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]]",
				"OMIM=[GeneSymbols=MYB,Phenotype=[{T-cell acute lymphoblastic leukemia} (3)],MIMNumber=[189990],CytoLocation=[6q23.3]]",
				"OMIM=[GeneSymbols=NKX2-1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[GeneSymbols=FKHL15,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[GeneSymbols=CYP17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]",
				"OMIM=[GeneSymbols=CMT2Q,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[GeneSymbols=SRGAP1,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]]",
				"OMIM=[GeneSymbols=CD32,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]]",
				"OMIM=[GeneSymbols=RPRGL1,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]]",
				"OMIM=[GeneSymbols=NKX2A,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[GeneSymbols=TREX1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[GeneSymbols=TITF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[GeneSymbols=MTHFR,Phenotype=[{Thromboembolism, susceptibility to}, 188050 (3)],MIMNumber=[607093],CytoLocation=[1p36.22]]",
				"OMIM=[GeneSymbols=TITF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[GeneSymbols=NMTC4,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[GeneSymbols=NMTC2,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]]",
				"OMIM=[GeneSymbols=NMTC1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[GeneSymbols=HERNS,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[GeneSymbols=ERAB,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[GeneSymbols=MRXS10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[GeneSymbols=CD11B,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[GeneSymbols=F5,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]]",
				"OMIM=[GeneSymbols=IFNG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]]",
				"OMIM=[GeneSymbols=KIAA1630,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[GeneSymbols=AMOXAD,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[GeneSymbols=SLEB6,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[GeneSymbols=P450C17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]",
				"OMIM=[GeneSymbols=AGS1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[GeneSymbols=FCGR2B,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]]");
	}
}
