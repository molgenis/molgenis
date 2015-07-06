package org.molgenis.hpofilter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.hpofilter.data.HpoFilterDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;




public class HpoFilterLogicTest {
	
	private HpoFilterLogic hpoFilterLogic;
	private HpoFilterDataProvider hpoFilterDataProvider;
	@Mock
	private MolgenisSettings molgenisSettings;
	
	
	HpoFilterLogicTest() {
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(molgenisSettings.getProperty(HpoFilterDataProvider.KEY_HPO_HIERARCHY, "http://compbio.charite.de/hudson/job/"
				+ "hpo/lastStableBuild/artifact/hp/hp.obo")).thenReturn("file://src/test/resources/testhpo.obo");
		Mockito.when(molgenisSettings.getProperty(HpoFilterDataProvider.KEY_HPO_MAPPING, HpoFilterDataProvider.DEFAULT_HPO_MAPPING_LOCATION)).thenReturn("file://src/test/resources/testassoc.txt");
		
		hpoFilterDataProvider = new HpoFilterDataProvider(molgenisSettings);
	}
	
	@Test
	public void testGetAssocData() {
		try
		{
			System.out.println(new URL("file://src/test/resources/testhpo.obo").toString());
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<String, HashSet<String>> assocData = hpoFilterDataProvider.getAssocData();
		HashMap<String, HashSet<String>> expected = new HashMap<>();
		Assert.assertEquals(assocData, expected);
		
		Assert.assertEquals(assocData.size(),4);
		
	}
	
}