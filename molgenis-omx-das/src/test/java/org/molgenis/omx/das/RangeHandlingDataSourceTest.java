package org.molgenis.omx.das;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.omx.das.impl.MolgenisDasTarget;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.*;

public class RangeHandlingDataSourceTest{
	RangeHandlingDataSource source;
	private DasFeature dasFeature;
	private DataService dataService;

    @SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{	
		dataService = mock(DataService.class);
        HashMap<URL,String> linkout = new HashMap<URL, String>();
		linkout.put(new URL("http://www.molgenis.org/"),"Link");

		List<DasTarget> dasTarget = new ArrayList<DasTarget>();

		dasTarget.add(new MolgenisDasTarget("vatiant_identifier", 0, 1000, "name,variant_description"));
		dasFeature = new DasFeature("vatiant_identifier", "name,variant_description", new DasType("type", null, "?", "type"),
				new DasMethod("not_recorded", "not_recorded", "ECO:0000037"), 0, 1000, new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE
				,new ArrayList<String>(),linkout,dasTarget,new ArrayList<String>(),null
				);
        source = new TestDataSource();
	}

	@AfterMethod
	public void teardown(){
		Mockito.reset(dataService);
	}

	@Test()
	public void createDasFeature() throws UnimplementedFeatureException, DataSourceException, MalformedURLException
	{
		DasFeature dasFeatureUnderTest = source.createDasFeature(0, 1000, "vatiant_identifier", "name",
                "variant_description", "http://www.molgenis.org/", new DasType("type", null, "?", "type"), new DasMethod("not_recorded", "not_recorded", "ECO:0000037"));
		assertEquals(dasFeature, dasFeatureUnderTest);
	}
	
	@Test(expectedExceptions = UnimplementedFeatureException.class)
	public void getFeatures() throws UnimplementedFeatureException, DataSourceException
	{
		source.getFeatures(new ArrayList<String>(), new Integer(-1), null);
	}
	
	@Test(expectedExceptions = UnimplementedFeatureException.class)
	public void getLinkURL() throws UnimplementedFeatureException, DataSourceException
	{
		source.getLinkURL("", "");
	}
	
	@Test(expectedExceptions = UnimplementedFeatureException.class)
	public void getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException
	{
		source.getTotalEntryPoints();
	}
	
	@Test(expectedExceptions = UnimplementedFeatureException.class)
	public void getFeatures1() throws BadReferenceObjectException, CoordinateErrorException, DataSourceException, UnimplementedFeatureException
	{
		source.getFeatures("", -1, -1, new Integer(-1), null);
	}
	
	@Test(expectedExceptions = UnimplementedFeatureException.class)
	public void getFeatures2() throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException
	{
		source.getFeatures("", -1, null);
	}
	
	@Test(expectedExceptions = UnimplementedFeatureException.class)
	public void getFeatures3() throws UnimplementedFeatureException, DataSourceException
	{
		source.getFeatures(new ArrayList<String>(), new Integer(-1));
	}
	
	@Test(expectedExceptions = BadReferenceObjectException.class)
	public void getFeatures4() throws BadReferenceObjectException, DataSourceException
	{
		source.getFeatures("", new Integer(-1));
	}

}
