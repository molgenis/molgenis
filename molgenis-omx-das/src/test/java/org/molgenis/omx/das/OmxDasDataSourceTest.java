package org.molgenis.omx.das;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Chromosome;
import org.molgenis.omx.xgap.Variant;
import org.molgenis.util.HandleRequestDelegationException;
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
	DasOmxDataSource source;
	private Variant variant;
	private DasFeature dasFeature;
	private DasEntryPoint expectedEntryPoint;
	private Database database;
	private Query chromosomeQuery;
	private Patient patient;
	private Variant variant2;
	private ArrayList<Patient> patientList;
	
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
		
		variant2 = new Variant();
		variant2.setIdentifier("vatiant_identifier2");
		variant2.setBpStart((long)0);
		variant2.setBpEnd((long)1000);
		variant2.setDescription("variant_description2");

		List<DasTarget> dasTarget = new ArrayList<DasTarget>();
		
		dasTarget.add(new MolgenisDasTarget(variant.getIdentifier(), variant.getBpStart().intValue(), variant.getBpEnd().intValue(), variant.getDescription()));
		dasFeature = new DasFeature("vatiant_identifier", "variant_description", new DasType("mutation", null, "?", "mutation"),
				new DasMethod("not_recorded", "not_recorded", "ECO:0000037"), 0, 1000, new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE
				,new ArrayList<String>(),linkout,dasTarget,new ArrayList<String>(),null
				);
		expectedEntryPoint = new DasEntryPoint("Chromosome_name", new Integer(0), new Integer(48000000), "Chromosome", "VERSION", DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION, "test", false);
		source = new DasOmxDataSource(database);

		List<Chromosome> chromosomeList = new ArrayList<Chromosome>();
		Chromosome chromosome = mock(Chromosome.class);
		chromosomeList.add(chromosome);
		patient = mock(Patient.class);
		patientList = new ArrayList<Patient>();
		patientList.add(patient);
				
		chromosomeQuery = mock(Query.class);
		
		when(chromosome.getIdentifier()).thenReturn("Chromosome_identifier");
		when(chromosome.getName()).thenReturn("Chromosome_name");
		when(chromosome.getBpLength()).thenReturn(48000000);
		when(database.query(Chromosome.class)).thenReturn(chromosomeQuery);
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
	public void getFeaturesRange() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException, DatabaseException
	{
		Query patientQuery = mock(Query.class);
		when(database.query(Patient.class)).thenReturn(patientQuery);
		when(patientQuery.find()).thenReturn(patientList);
		Query variantQuery = mock(Query.class);
		when(database.query(Variant.class)).thenReturn(variantQuery);
		
		source.getFeatures("segment",1,100000,100);
		verify(patientQuery, never()).equals(Patient.ID,"");
		verify(variantQuery).equals(eq(Variant.TRACK_IDENTIFIER), eq("test"));
		verify(variantQuery).equals(eq(Variant.CHROMOSOME), eq(0));
		verify(variantQuery).greaterOrEqual(eq(Variant.BPSTART), eq(1));
		verify(variantQuery).lessOrEqual(eq(Variant.BPEND), eq(100000));
	}
	
	@Test()
	public void getFeaturesRangePatientOneAllele() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException, DatabaseException
	{
		Query patientQuery = mock(Query.class);
		when(patientQuery.find()).thenReturn(patientList);
		when(database.query(Patient.class)).thenReturn(patientQuery);
		Query variantQuery = mock(Query.class);
		when(database.query(Variant.class)).thenReturn(variantQuery);
		
		when(patient.getAllele1()).thenReturn(variant);
		when(patient.getAllele2()).thenReturn(new Variant());
		source.getFeatures("segment,123",1,100000,100);
		verify(patientQuery).equals(eq(Patient.ID),eq("123"));		
		verify(variantQuery).equals(eq(Variant.TRACK_IDENTIFIER), eq("test"));
		verify(variantQuery).equals(eq(Variant.CHROMOSOME), eq(0));
		verify(variantQuery).greaterOrEqual(eq(Variant.BPSTART), eq(1));
		verify(variantQuery).lessOrEqual(eq(Variant.BPEND), eq(100000));
		verify(variantQuery).equals(eq(Variant.IDENTIFIER),eq("vatiant_identifier"));	
	}
	
	@Test()
	public void getFeaturesRangePatientTwoAlleles() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException, DatabaseException
	{
		Query patientQuery = mock(Query.class);
		when(patientQuery.find()).thenReturn(patientList);		
		when(database.query(Patient.class)).thenReturn(patientQuery);
		Query variantQuery = mock(Query.class);
		when(database.query(Variant.class)).thenReturn(variantQuery);
		when(patient.getAllele1()).thenReturn(variant);
		when(patient.getAllele2()).thenReturn(variant2);
		
		source.getFeatures("segment,123",1,100000,100);
		verify(patientQuery).equals(eq(Patient.ID),eq("123"));		
		verify(variantQuery).equals(eq(Variant.TRACK_IDENTIFIER), eq("test"));
		verify(variantQuery).equals(eq(Variant.CHROMOSOME), eq(0));
		verify(variantQuery).greaterOrEqual(eq(Variant.BPSTART), eq(1));
		verify(variantQuery).lessOrEqual(eq(Variant.BPEND), eq(100000));
		verify(variantQuery).equals(eq(Variant.IDENTIFIER),eq("vatiant_identifier"));	
		verify(variantQuery).or();	
		verify(variantQuery).equals(eq(Variant.IDENTIFIER),eq("vatiant_identifier2"));	
	}
	
	@Test()
	public void getFeaturesRangePatientNoAlleles() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException, DatabaseException
	{
		Query patientQuery = mock(Query.class);
		when(patientQuery.find()).thenReturn(patientList);		
		when(database.query(Patient.class)).thenReturn(patientQuery);
		Query variantQuery = mock(Query.class);
		when(database.query(Variant.class)).thenReturn(variantQuery);
		when(patient.getAllele1()).thenReturn(null);
		when(patient.getAllele2()).thenReturn(null);
		
		source.getFeatures("segment,123",1,100000,100);
		verify(patientQuery).equals(eq(Patient.ID),eq("123"));		
		verify(variantQuery).equals(eq(Variant.TRACK_IDENTIFIER), eq("test"));
		verify(variantQuery).equals(eq(Variant.CHROMOSOME), eq(0));
		verify(variantQuery).greaterOrEqual(eq(Variant.BPSTART), eq(1));
		verify(variantQuery).lessOrEqual(eq(Variant.BPEND), eq(100000));
		verify(variantQuery, never()).equals(eq(Variant.IDENTIFIER),eq("vatiant_identifier"));	
		verify(variantQuery, never()).equals(eq(Variant.IDENTIFIER),eq("vatiant_identifier2"));	
	}
	
	@Test()
	public void getTotalCountForType() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException, DatabaseException
	{
		source.getTotalCountForType(new DasType("mutation", null, "?", "mutation"));
		verify(database).count(eq(Variant.class));		
	}
	
	@Test()
	public void getTypes() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException, DatabaseException
	{
		List<DasType> types = new ArrayList<DasType>();
		types.add(new DasType("mutation", null, "?", "mutation"));
		assertEquals(types,source.getTypes());
		
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
