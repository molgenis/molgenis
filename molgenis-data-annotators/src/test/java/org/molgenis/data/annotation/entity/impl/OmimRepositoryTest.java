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
		assertEquals(repo.count(), 42);
	}

	@Test
	public void iterator()
	{
		List<String> iterator = newArrayList(Iterators.transform(repo.iterator(), Object::toString));
		List<String> expectedIteratorContent = getExpectedIteratorContentList();
		Assert.assertEquals(iterator, expectedIteratorContent);
	}

	@Test
	public void findAllWithEmptyQuery() throws IOException
	{
		List<Entity> omimEntities = repo.findAll(new QueryImpl()).collect(toList());
		assertEquals(omimEntities.toString(),
				"[OMIM=[Gene_Name=HADH2,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3], OMIM=[Gene_Name=CR3A,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3], OMIM=[Gene_Name=ITGAM,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3], OMIM=[Gene_Name=MAC1A,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3], OMIM=[Gene_Name=FOXE1,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3], OMIM=[Gene_Name=CRV,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3], OMIM=[Gene_Name=THPH2,Phenotype={Thrombophilia, susceptibility to, due to factor V Leiden},MIMNumber=612309,CytoLocation=1q24.2,OmimEntry=188055,OmimType=3], OMIM=[Gene_Name=IFG,Phenotype={TSC2 angiomyolipomas, renal, modifier of},MIMNumber=147570,CytoLocation=12q15,OmimEntry=613254,OmimType=3], OMIM=[Gene_Name=TTF2,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3], OMIM=[Gene_Name=TTF1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3], OMIM=[Gene_Name=HSD17B10,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3], OMIM=[Gene_Name=IFI,Phenotype={TSC2 angiomyolipomas, renal, modifier of},MIMNumber=147570,CytoLocation=12q15,OmimEntry=613254,OmimType=3], OMIM=[Gene_Name=CYP17A1,Phenotype=17,20-lyase deficiency, isolated,17-alpha-hydroxylase/17,20-lyase deficiency,MIMNumber=609300,609300,CytoLocation=10q24.32,10q24.32,OmimEntry=202110,202110,OmimType=3,3], OMIM=[Gene_Name=DHTKD1,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3], OMIM=[Gene_Name=KIAA1304,Phenotype={Thyroid cancer, nonmedullary, 2},MIMNumber=606523,CytoLocation=12q14.2,OmimEntry=188470,OmimType=3], OMIM=[Gene_Name=NKX2-1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3], OMIM=[Gene_Name=FKHL15,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3], OMIM=[Gene_Name=CYP17,Phenotype=17,20-lyase deficiency, isolated,17-alpha-hydroxylase/17,20-lyase deficiency,MIMNumber=609300,609300,CytoLocation=10q24.32,10q24.32,OmimEntry=202110,202110,OmimType=3,3], OMIM=[Gene_Name=CMT2Q,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3], OMIM=[Gene_Name=SRGAP1,Phenotype={Thyroid cancer, nonmedullary, 2},MIMNumber=606523,CytoLocation=12q14.2,OmimEntry=188470,OmimType=3], OMIM=[Gene_Name=CD32,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=604590,CytoLocation=1q23.3,OmimEntry=152700,OmimType=3], OMIM=[Gene_Name=RPRGL1,Phenotype={Thrombophilia, susceptibility to, due to factor V Leiden},MIMNumber=612309,CytoLocation=1q24.2,OmimEntry=188055,OmimType=3], OMIM=[Gene_Name=NKX2A,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3], OMIM=[Gene_Name=TREX1,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3], OMIM=[Gene_Name=TITF1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3], OMIM=[Gene_Name=MTHFR,Phenotype={Thromboembolism, susceptibility to},MIMNumber=607093,CytoLocation=1p36.22,OmimEntry=188050,OmimType=3], OMIM=[Gene_Name=TITF2,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3], OMIM=[Gene_Name=NMTC4,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3], OMIM=[Gene_Name=NMTC2,Phenotype={Thyroid cancer, nonmedullary, 2},MIMNumber=606523,CytoLocation=12q14.2,OmimEntry=188470,OmimType=3], OMIM=[Gene_Name=NMTC1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3], OMIM=[Gene_Name=HERNS,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3], OMIM=[Gene_Name=ERAB,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3], OMIM=[Gene_Name=MRXS10,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3], OMIM=[Gene_Name=CD11B,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3], OMIM=[Gene_Name=F5,Phenotype={Thrombophilia, susceptibility to, due to factor V Leiden},MIMNumber=612309,CytoLocation=1q24.2,OmimEntry=188055,OmimType=3], OMIM=[Gene_Name=IFNG,Phenotype={TSC2 angiomyolipomas, renal, modifier of},MIMNumber=147570,CytoLocation=12q15,OmimEntry=613254,OmimType=3], OMIM=[Gene_Name=KIAA1630,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3], OMIM=[Gene_Name=AMOXAD,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3], OMIM=[Gene_Name=SLEB6,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3], OMIM=[Gene_Name=P450C17,Phenotype=17,20-lyase deficiency, isolated,17-alpha-hydroxylase/17,20-lyase deficiency,MIMNumber=609300,609300,CytoLocation=10q24.32,10q24.32,OmimEntry=202110,202110,OmimType=3,3], OMIM=[Gene_Name=AGS1,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3], OMIM=[Gene_Name=FCGR2B,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=604590,CytoLocation=1q23.3,OmimEntry=152700,OmimType=3]]");
	}

	@Test
	public void findAllWithQuery() throws IOException
	{
		List<Entity> omimEntities = repo
				.findAll(new QueryImpl().eq(OmimRepository.OMIM_GENE_SYMBOLS_COL_NAME, "CYP17A1")).collect(toList());
		assertEquals(omimEntities.toString(),
				"[OMIM=[Gene_Name=CYP17A1,Phenotype=17,20-lyase deficiency, isolated,17-alpha-hydroxylase/17,20-lyase deficiency,MIMNumber=609300,609300,CytoLocation=10q24.32,10q24.32,OmimEntry=202110,202110,OmimType=3,3]]");

	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "The only query allowed on this Repository is gene EQUALS")
	public void findAllWithBadQuery()
	{
		repo.findAll(new QueryImpl().like(OmimRepository.OMIM_PHENOTYPE_COL_NAME, "test_phenotype"));
	}

	private List<String> getExpectedIteratorContentList()
	{
		return newArrayList(
				"OMIM=[Gene_Name=HADH2,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3]",
				"OMIM=[Gene_Name=CR3A,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3]",
				"OMIM=[Gene_Name=ITGAM,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3]",
				"OMIM=[Gene_Name=MAC1A,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3]",
				"OMIM=[Gene_Name=FOXE1,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3]",
				"OMIM=[Gene_Name=CRV,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3]",
				"OMIM=[Gene_Name=THPH2,Phenotype={Thrombophilia, susceptibility to, due to factor V Leiden},MIMNumber=612309,CytoLocation=1q24.2,OmimEntry=188055,OmimType=3]",
				"OMIM=[Gene_Name=IFG,Phenotype={TSC2 angiomyolipomas, renal, modifier of},MIMNumber=147570,CytoLocation=12q15,OmimEntry=613254,OmimType=3]",
				"OMIM=[Gene_Name=TTF2,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3]",
				"OMIM=[Gene_Name=TTF1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3]",
				"OMIM=[Gene_Name=HSD17B10,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3]",
				"OMIM=[Gene_Name=IFI,Phenotype={TSC2 angiomyolipomas, renal, modifier of},MIMNumber=147570,CytoLocation=12q15,OmimEntry=613254,OmimType=3]",
				"OMIM=[Gene_Name=CYP17A1,Phenotype=17,20-lyase deficiency, isolated,17-alpha-hydroxylase/17,20-lyase deficiency,MIMNumber=609300,609300,CytoLocation=10q24.32,10q24.32,OmimEntry=202110,202110,OmimType=3,3]",
				"OMIM=[Gene_Name=DHTKD1,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3]",
				"OMIM=[Gene_Name=KIAA1304,Phenotype={Thyroid cancer, nonmedullary, 2},MIMNumber=606523,CytoLocation=12q14.2,OmimEntry=188470,OmimType=3]",
				"OMIM=[Gene_Name=NKX2-1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3]",
				"OMIM=[Gene_Name=FKHL15,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3]",
				"OMIM=[Gene_Name=CYP17,Phenotype=17,20-lyase deficiency, isolated,17-alpha-hydroxylase/17,20-lyase deficiency,MIMNumber=609300,609300,CytoLocation=10q24.32,10q24.32,OmimEntry=202110,202110,OmimType=3,3]",
				"OMIM=[Gene_Name=CMT2Q,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3]",
				"OMIM=[Gene_Name=SRGAP1,Phenotype={Thyroid cancer, nonmedullary, 2},MIMNumber=606523,CytoLocation=12q14.2,OmimEntry=188470,OmimType=3]",
				"OMIM=[Gene_Name=CD32,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=604590,CytoLocation=1q23.3,OmimEntry=152700,OmimType=3]",
				"OMIM=[Gene_Name=RPRGL1,Phenotype={Thrombophilia, susceptibility to, due to factor V Leiden},MIMNumber=612309,CytoLocation=1q24.2,OmimEntry=188055,OmimType=3]",
				"OMIM=[Gene_Name=NKX2A,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3]",
				"OMIM=[Gene_Name=TREX1,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3]",
				"OMIM=[Gene_Name=TITF1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3]",
				"OMIM=[Gene_Name=MTHFR,Phenotype={Thromboembolism, susceptibility to},MIMNumber=607093,CytoLocation=1p36.22,OmimEntry=188050,OmimType=3]",
				"OMIM=[Gene_Name=TITF2,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3]",
				"OMIM=[Gene_Name=NMTC4,Phenotype={Thyroid cancer, nonmedullary, 4},MIMNumber=602617,CytoLocation=9q22.33,OmimEntry=616534,OmimType=3]",
				"OMIM=[Gene_Name=NMTC2,Phenotype={Thyroid cancer, nonmedullary, 2},MIMNumber=606523,CytoLocation=12q14.2,OmimEntry=188470,OmimType=3]",
				"OMIM=[Gene_Name=NMTC1,Phenotype={Thyroid cancer, monmedullary, 1},MIMNumber=600635,CytoLocation=14q13.3,OmimEntry=188550,OmimType=3]",
				"OMIM=[Gene_Name=HERNS,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3]",
				"OMIM=[Gene_Name=ERAB,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3]",
				"OMIM=[Gene_Name=MRXS10,Phenotype=17-beta-hydroxysteroid dehydrogenase X deficiency,MIMNumber=300256,CytoLocation=Xp11.22,OmimEntry=300438,OmimType=3]",
				"OMIM=[Gene_Name=CD11B,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3]",
				"OMIM=[Gene_Name=F5,Phenotype={Thrombophilia, susceptibility to, due to factor V Leiden},MIMNumber=612309,CytoLocation=1q24.2,OmimEntry=188055,OmimType=3]",
				"OMIM=[Gene_Name=IFNG,Phenotype={TSC2 angiomyolipomas, renal, modifier of},MIMNumber=147570,CytoLocation=12q15,OmimEntry=613254,OmimType=3]",
				"OMIM=[Gene_Name=KIAA1630,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3]",
				"OMIM=[Gene_Name=AMOXAD,Phenotype=2-aminoadipic 2-oxoadipic aciduria,MIMNumber=614984,CytoLocation=10p14,OmimEntry=204750,OmimType=3]",
				"OMIM=[Gene_Name=SLEB6,Phenotype={Systemic lupus erythematous, association with susceptibility to, 6},MIMNumber=120980,CytoLocation=16p11.2,OmimEntry=609939,OmimType=3]",
				"OMIM=[Gene_Name=P450C17,Phenotype=17,20-lyase deficiency, isolated,17-alpha-hydroxylase/17,20-lyase deficiency,MIMNumber=609300,609300,CytoLocation=10q24.32,10q24.32,OmimEntry=202110,202110,OmimType=3,3]",
				"OMIM=[Gene_Name=AGS1,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=606609,CytoLocation=3p21.31,OmimEntry=152700,OmimType=3]",
				"OMIM=[Gene_Name=FCGR2B,Phenotype={Systemic lupus erythematosus, susceptibility to},MIMNumber=604590,CytoLocation=1q23.3,OmimEntry=152700,OmimType=3]");
	}
}
