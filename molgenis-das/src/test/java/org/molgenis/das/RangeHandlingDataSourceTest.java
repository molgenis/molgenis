package org.molgenis.das;

import org.mockito.Mockito;
import org.molgenis.das.impl.MolgenisDasTarget;
import org.molgenis.data.DataService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;

public class RangeHandlingDataSourceTest
{
	RangeHandlingDataSource source;
	private DasFeature dasFeature;
	private DataService dataService;

	@BeforeMethod
	public void setUp() throws DataSourceException, MalformedURLException
	{
		dataService = mock(DataService.class);
		HashMap<URL, String> linkout = new HashMap<>();
		linkout.put(new URL("http://www.molgenis.org/"), "Link");

		List<DasTarget> dasTarget = new ArrayList<>();

		dasTarget.add(new MolgenisDasTarget("vatiant_identifier", 0, 1000, "name,variant_description"));
		List<String> notes = new ArrayList<>();
		notes.add("track:");
		notes.add("source:MOLGENIS");
		dasFeature = new DasFeature("vatiant_identifier", "name,variant_description", new DasType("0", "", "", "type"),
				new DasMethod("not_recorded", "not_recorded", "ECO:0000037"), 0, 1000, new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes, linkout,
				dasTarget, new ArrayList<>(), null);
		source = new TestDataSource();
	}

	@AfterMethod
	public void teardown()
	{
		Mockito.reset(dataService);
	}

	@Test()
	public void createDasFeature() throws UnimplementedFeatureException, DataSourceException, MalformedURLException
	{
		DasFeature dasFeatureUnderTest = source.createDasFeature(0, 1000, "vatiant_identifier", "name",
				"variant_description", "http://www.molgenis.org/", new DasType("0", "", "", "type"),
				new DasMethod("not_recorded", "not_recorded", "ECO:0000037"), "", "", new ArrayList<>());
		assertEquals(dasFeature, dasFeatureUnderTest);
	}

	@Test(expectedExceptions = UnimplementedFeatureException.class)
	public void getFeatures() throws UnimplementedFeatureException, DataSourceException
	{
		source.getFeatures(new ArrayList<>(), new Integer(-1), null);
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
	public void getFeatures1() throws BadReferenceObjectException, CoordinateErrorException, DataSourceException,
			UnimplementedFeatureException
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
		source.getFeatures(new ArrayList<>(), new Integer(-1));
	}

	@Test(expectedExceptions = BadReferenceObjectException.class)
	public void getFeatures4() throws BadReferenceObjectException, DataSourceException
	{
		source.getFeatures("", new Integer(-1));
	}

}
