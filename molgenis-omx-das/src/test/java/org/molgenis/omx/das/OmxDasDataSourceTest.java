package org.molgenis.omx.das;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;
import static org.testng.AssertJUnit.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Chromosome;
import org.molgenis.omx.xgap.Variant;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasEntryPointOrientation;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;

public class OmxDasDataSourceTest {
	DasOmxDataSource source = new DasOmxDataSource(null,null,null);
	private Variant variant;
	private DasFeature dasFeature;
	private DasEntryPoint expectedEntryPoint;
	private Database database;
	private Query<Patient> patientQuery;
	private Query<Variant> variantQuery;
	private Query chromosomeQuery;
	
	@SuppressWarnings("unchecked")
	@BeforeTest
	public void setUp() throws HandleRequestDelegationException, Exception
	{		
		database = mock(Database.class);
		Map<URL, String> linkout = new HashMap<URL, String>();
		linkout.put(new URL("http://www.molgenis.org/"),"Link");

		variant = new Variant();
		variant.setIdentifier("vatiant_identifier");
		variant.setBpStart((long)0);
		variant.setBpEnd((long)1000);
		variant.setDescription("variant_description");

		List<DasTarget> dasTarget = new ArrayList<DasTarget>();
		
		dasTarget.add(new MolgenisDasTarget(variant.getIdentifier(), variant.getBpStart().intValue(), variant.getBpEnd().intValue(), variant.getDescription()));
		dasFeature = new DasFeature("vatiant_identifier", "variant_description", new DasType("mutation", null, "?", "mutation"),
				new DasMethod("not_recorded", "not_recorded", "ECO:0000037"), 0, 1000, new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE
				,new ArrayList<String>(),linkout,dasTarget,new ArrayList<String>(),null
				);
		expectedEntryPoint = new DasEntryPoint("Chromosome_name", new Integer(0), new Integer(48000000), "Chromosome", "VERSION", DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION, "test", false);
		source = new DasOmxDataSource(database,new DasType("mutation", null, "?", "mutation"),new DasMethod("not_recorded", "not_recorded", "ECO:0000037"));

		List<Chromosome> chromosomeList = new ArrayList<Chromosome>();
		Chromosome chromosome = mock(Chromosome.class);
		chromosomeList.add(chromosome);
		Patient patient = new Patient();
		List<Patient> patientList = new ArrayList<Patient>();
		patientList.add(patient);
				
		chromosomeQuery = mock(Query.class);
		patientQuery = mock(Query.class);
		variantQuery = mock(Query.class);
		
		when(patientQuery.find()).thenReturn(patientList);
		when(chromosome.getIdentifier()).thenReturn("Chromosome_identifier");
		when(chromosome.getName()).thenReturn("Chromosome_name");
		when(chromosome.getBpLength()).thenReturn(48000000);
		when(database.query(Chromosome.class)).thenReturn(chromosomeQuery);
		when(database.query(Patient.class)).thenReturn(patientQuery);
		when(database.query(Variant.class)).thenReturn(variantQuery);
		when(chromosomeQuery.find()).thenReturn(chromosomeList);
	}
	
	@Test()
	public void getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException
	{
		assertEquals("1.00",source.getEntryPointVersion());
	}
	
	@Test()
	public void createDasFeature() throws UnimplementedFeatureException, DataSourceException, MalformedURLException
	{
		DasFeature dasFeatureUnderTest = source.createDasFeature(variant);
		assertEquals(dasFeature, dasFeatureUnderTest);
	}
	
	@Test()
	public void getEntryPoints() throws UnimplementedFeatureException, DataSourceException
	{
		Collection<DasEntryPoint> entryPoints = source.getEntryPoints(123, -1);
		Iterator it = entryPoints.iterator();
		DasEntryPoint point = (DasEntryPoint)it.next();
		assertEquals(expectedEntryPoint.getDescription(), point.getDescription());
		assertEquals(expectedEntryPoint.getOrientation(), point.getOrientation());
		assertEquals(expectedEntryPoint.getSegmentId(), point.getSegmentId());
		assertEquals(expectedEntryPoint.getStartCoordinate(), point.getStartCoordinate());
		assertEquals(expectedEntryPoint.getStopCoordinate(), point.getStopCoordinate());
		assertEquals(expectedEntryPoint.getType(), point.getType());
		assertEquals(expectedEntryPoint.getVersion(), point.getVersion());
	}
	
	@Test()
	public void getFeaturesRange() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException
	{
		source.getFeatures("segment",1,100000,100);
		verify(patientQuery, never()).equals(Patient.ID,"");		
		verify(variantQuery).greaterOrEqual(Variant.BPSTART, 1);
		verify(variantQuery).lessOrEqual(Variant.BPEND, 100000);
		verify(variantQuery).equals(Variant.CHROMOSOME, 0);
		verify(variantQuery).equals(Variant.TRACK_IDENTIFIER, "test");
		
	}
	
	@Test()
	public void getFeaturesRangePatient() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException, DatabaseException
	{
		source.getFeatures("segment,123",1,100000,100);
		verify(patientQuery).equals(eq(Patient.ID), eq("123"));		
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
	public void getTotalEntryPoints() throws DatabaseException, UnimplementedFeatureException, DataSourceException
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
