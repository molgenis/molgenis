package org.molgenis.data.annotation.entity.impl;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.QueryImpl;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;

public class OmimRepositoryTest
{
	private OmimRepository repo;
	private File omimFile = getFile(getClass(), "/omim/omim.txt");

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
		List<String> iterator  = newArrayList(Iterators.transform(repo.iterator(), Object::toString));
		List<String> expectedIteratorContent = getExpectedIteratorContentList();
		Assert.assertEquals(iterator, expectedIteratorContent);
	}

	@Test
	public void findAllWithEmptyQuery() throws IOException
	{

		List<Entity> omimEntities = repo.findAll(new QueryImpl()).collect(toList());
		assertEquals(omimEntities.toString(),
				"[OMIM=[Gene_Name=HADH2,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[Gene_Name=CR3A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[Gene_Name=ITGAM,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[Gene_Name=MAC1A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[Gene_Name=FOXE1,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[Gene_Name=CRV,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[Gene_Name=THPH2,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]], OMIM=[Gene_Name=IFG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]], OMIM=[Gene_Name=TTF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[Gene_Name=TTF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[Gene_Name=HSD17B10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[Gene_Name=IFI,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]], OMIM=[Gene_Name=CYP17A1,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]], OMIM=[Gene_Name=DHTKD1,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[Gene_Name=KIAA1304,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]], OMIM=[Gene_Name=MYB,Phenotype=[{T-cell acute lymphoblastic leukemia} (3)],MIMNumber=[189990],CytoLocation=[6q23.3]], OMIM=[Gene_Name=NKX2-1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[Gene_Name=FKHL15,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[Gene_Name=CYP17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]], OMIM=[Gene_Name=CMT2Q,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[Gene_Name=SRGAP1,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]], OMIM=[Gene_Name=CD32,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]], OMIM=[Gene_Name=RPRGL1,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]], OMIM=[Gene_Name=NKX2A,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[Gene_Name=TREX1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[Gene_Name=TITF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[Gene_Name=MTHFR,Phenotype=[{Thromboembolism, susceptibility to}, 188050 (3)],MIMNumber=[607093],CytoLocation=[1p36.22]], OMIM=[Gene_Name=TITF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[Gene_Name=NMTC4,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]], OMIM=[Gene_Name=NMTC2,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]], OMIM=[Gene_Name=NMTC1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]], OMIM=[Gene_Name=HERNS,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[Gene_Name=ERAB,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[Gene_Name=MRXS10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]], OMIM=[Gene_Name=CD11B,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[Gene_Name=F5,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]], OMIM=[Gene_Name=IFNG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]], OMIM=[Gene_Name=KIAA1630,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[Gene_Name=AMOXAD,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]], OMIM=[Gene_Name=SLEB6,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]], OMIM=[Gene_Name=P450C17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]], OMIM=[Gene_Name=AGS1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]], OMIM=[Gene_Name=FCGR2B,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]]]");
	}

	@Test
	public void findAllWithQuery() throws IOException
	{
		List<Entity> omimEntities = repo
				.findAll(new QueryImpl().eq(OmimRepository.OMIM_GENE_SYMBOLS_COL_NAME, "CYP17A1")).collect(toList());
		assertEquals(omimEntities.toString(),
				"[OMIM=[Gene_Name=CYP17A1,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]]");

	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "The only query allowed on this Repository is gene EQUALS")
	public void findAllWithBadQuery()
	{
		repo.findAll(new QueryImpl().like(OmimRepository.OMIM_PHENOTYPE_COL_NAME, "test_phenotype"));
	}

	private List<String> getExpectedIteratorContentList()
	{
		return newArrayList(
				"OMIM=[Gene_Name=HADH2,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[Gene_Name=CR3A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[Gene_Name=ITGAM,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[Gene_Name=MAC1A,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[Gene_Name=FOXE1,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[Gene_Name=CRV,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[Gene_Name=THPH2,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]]",
				"OMIM=[Gene_Name=IFG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]]",
				"OMIM=[Gene_Name=TTF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[Gene_Name=TTF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[Gene_Name=HSD17B10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[Gene_Name=IFI,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]]",
				"OMIM=[Gene_Name=CYP17A1,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]",
				"OMIM=[Gene_Name=DHTKD1,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[Gene_Name=KIAA1304,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]]",
				"OMIM=[Gene_Name=MYB,Phenotype=[{T-cell acute lymphoblastic leukemia} (3)],MIMNumber=[189990],CytoLocation=[6q23.3]]",
				"OMIM=[Gene_Name=NKX2-1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[Gene_Name=FKHL15,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[Gene_Name=CYP17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]",
				"OMIM=[Gene_Name=CMT2Q,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[Gene_Name=SRGAP1,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]]",
				"OMIM=[Gene_Name=CD32,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]]",
				"OMIM=[Gene_Name=RPRGL1,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]]",
				"OMIM=[Gene_Name=NKX2A,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[Gene_Name=TREX1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[Gene_Name=TITF1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[Gene_Name=MTHFR,Phenotype=[{Thromboembolism, susceptibility to}, 188050 (3)],MIMNumber=[607093],CytoLocation=[1p36.22]]",
				"OMIM=[Gene_Name=TITF2,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[Gene_Name=NMTC4,Phenotype=[{Thyroid cancer, nonmedullary, 4}, 616534 (3)],MIMNumber=[602617],CytoLocation=[9q22.33]]",
				"OMIM=[Gene_Name=NMTC2,Phenotype=[{Thyroid cancer, nonmedullary, 2}, 188470 (3)],MIMNumber=[606523],CytoLocation=[12q14.2]]",
				"OMIM=[Gene_Name=NMTC1,Phenotype=[{Thyroid cancer, monmedullary, 1}, 188550 (3)],MIMNumber=[600635],CytoLocation=[14q13.3]]",
				"OMIM=[Gene_Name=HERNS,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[Gene_Name=ERAB,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[Gene_Name=MRXS10,Phenotype=[17-beta-hydroxysteroid dehydrogenase X deficiency, 300438 (3)],MIMNumber=[300256],CytoLocation=[Xp11.22]]",
				"OMIM=[Gene_Name=CD11B,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[Gene_Name=F5,Phenotype=[{Thrombophilia, susceptibility to, due to factor V Leiden}, 188055 (3)],MIMNumber=[612309],CytoLocation=[1q24.2]]",
				"OMIM=[Gene_Name=IFNG,Phenotype=[{TSC2 angiomyolipomas, renal, modifier of}, 613254 (3)],MIMNumber=[147570],CytoLocation=[12q15]]",
				"OMIM=[Gene_Name=KIAA1630,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[Gene_Name=AMOXAD,Phenotype=[2-aminoadipic 2-oxoadipic aciduria, 204750 (3)],MIMNumber=[614984],CytoLocation=[10p14]]",
				"OMIM=[Gene_Name=SLEB6,Phenotype=[{Systemic lupus erythematous, association with susceptibility to, 6}, 609939 (3)],MIMNumber=[120980],CytoLocation=[16p11.2]]",
				"OMIM=[Gene_Name=P450C17,Phenotype=[17,20-lyase deficiency, isolated, 202110 (3), 17-alpha-hydroxylase/17,20-lyase deficiency, 202110 (3)],MIMNumber=[609300, 609300],CytoLocation=[10q24.32, 10q24.32]]",
				"OMIM=[Gene_Name=AGS1,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[606609],CytoLocation=[3p21.31]]",
				"OMIM=[Gene_Name=FCGR2B,Phenotype=[{Systemic lupus erythematosus, susceptibility to}, 152700 (3)],MIMNumber=[604590],CytoLocation=[1q23.3]]");
	}
}
