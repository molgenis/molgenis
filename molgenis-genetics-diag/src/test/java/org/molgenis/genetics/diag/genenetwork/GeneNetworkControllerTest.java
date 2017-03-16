package org.molgenis.genetics.diag.genenetwork;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.genetics.diag.genenetwork.meta.GeneNetworkScoreFactory;
import org.molgenis.genetics.diag.genenetwork.meta.GeneNetworkScoreMetaData;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = { GeneNetworkControllerTest.Config.class, GeneNetworkController.class })
public class GeneNetworkControllerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private GeneNetworkController geneNetworkController;

	@Autowired
	DataService dataService;

	List<String> hpo = Arrays.asList("hpo1", "hpo2", "hpo3");
	Map<String, String> genes = new HashMap<>();

	@BeforeClass
	public void beforeClass() throws IOException
	{
		genes.put("E1", "H1");
		genes.put("E2", "H2");
		genes.put("E3", "H3");
	}

	@Test
	public void testCreateHpoTermList() throws IOException
	{
		List result = geneNetworkController
				.createHpoTermList(new Scanner(ResourceUtils.getFile(getClass(), "/testfile.txt")));
		for (String value : hpo)
		{
			assertTrue(result.contains(value));
		}
	}

	@Test
	public void testCreateEnsembleHugoMap() throws IOException
	{
		Map<String, String> result = geneNetworkController
				.createEnsembleHugoMap(ResourceUtils.getFile(getClass(), "/mart_export.txt").getPath());
		assertEquals(result.keySet().size(), 3);
		for (String key : genes.keySet())
		{
			assertEquals(result.get(key), genes.get(key));
		}
	}

	@Test
	public void testImport() throws IOException
	{

		List<Entity> entities = geneNetworkController.processSingleInputLine(new Scanner("E1\t1\t2\t3"), hpo, genes);
		assertEquals(entities.size(), 3);
		assertEquals(entities.get(0).get(GeneNetworkScoreMetaData.ENSEMBL_ID), "E1");
		assertEquals(entities.get(0).get(GeneNetworkScoreMetaData.SCORE), 1.0);
		assertEquals(entities.get(0).get(GeneNetworkScoreMetaData.HUGO_SYMBOL), "H1");
		assertEquals(entities.get(0).get(GeneNetworkScoreMetaData.HPO), "hpo1");

		assertEquals(entities.get(1).get(GeneNetworkScoreMetaData.ENSEMBL_ID), "E1");
		assertEquals(entities.get(1).get(GeneNetworkScoreMetaData.SCORE), 2.0);
		assertEquals(entities.get(1).get(GeneNetworkScoreMetaData.HUGO_SYMBOL), "H1");
		assertEquals(entities.get(1).get(GeneNetworkScoreMetaData.HPO), "hpo2");

		assertEquals(entities.get(2).get(GeneNetworkScoreMetaData.ENSEMBL_ID), "E1");
		assertEquals(entities.get(2).get(GeneNetworkScoreMetaData.SCORE), 3.0);
		assertEquals(entities.get(2).get(GeneNetworkScoreMetaData.HUGO_SYMBOL), "H1");
		assertEquals(entities.get(2).get(GeneNetworkScoreMetaData.HPO), "hpo3");

	}

	@Configuration
	@Import({ GeneNetworkScoreMetaData.class, GeneNetworkScoreFactory.class })
	public static class Config
	{
		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		MolgenisPluginRegistry pluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}
	}
}
