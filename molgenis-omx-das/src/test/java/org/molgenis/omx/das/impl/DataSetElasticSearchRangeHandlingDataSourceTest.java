package org.molgenis.omx.das.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Chromosome;
import org.molgenis.omx.xgap.Track;
import org.molgenis.omx.xgap.Variant;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.*;

public class DataSetElasticSearchRangeHandlingDataSourceTest {
	DatasetElasticSearchRangeHandlingDataSource source;
	private DasFeature dasFeature;
	private DasEntryPoint expectedEntryPoint;
	private Patient patient;
	private Chromosome chromosome;
    private SearchService searchService;
    private ArrayList resultList;
    private ArrayList featureList;

    @SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
        searchService = mock(SearchService.class);
        DasType type = new DasType("type", null, "?", "type");
        DasMethod method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
        source = new DatasetElasticSearchRangeHandlingDataSource(searchService,type,method);

        Map<URL, String> linkout = new HashMap<URL, String>();
        linkout.put(new URL("http://www.molgenis.org/"),"Link");

        List<DasTarget> dasTarget = new ArrayList<DasTarget>();
        dasTarget.add(new MolgenisDasTarget("mutation id", 10, 1000, "mutation name,description"));
        dasFeature = new DasFeature("mutation id", "mutation name,description", type,
				method, 10, 1000, new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE
				,new ArrayList<String>(),linkout,dasTarget,new ArrayList<String>(),null
				);
        expectedEntryPoint = new DasEntryPoint("Chromosome_name", new Integer(0), new Integer(48000000), "Chromosome", "VERSION", DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION, "test", false);

        Query q = new QueryImpl().eq(source.MUTATION_CHROMOSOME, "1");
        q.pageSize(100);
        SearchRequest searchRequest = new SearchRequest("dataset", q, null);
        SearchResult result = mock(SearchResult.class);
        Map map =new HashMap();
        map.put(source.MUTATION_STOP_POSITION,1000);
        map.put(source.MUTATION_LINK,"http://www.molgenis.org/");
        map.put(source.MUTATION_NAME,"mutation name");
        map.put(source.MUTATION_DESCRIPTION,"description");
        map.put(source.MUTATION_START_POSITION, 10);
        map.put(source.MUTATION_ID,"mutation id");
        map.put(source.MUTATION_CHROMOSOME,"1");

        resultList = new ArrayList();
        resultList.add(new Hit("","",map));
        featureList = new ArrayList();
        featureList.add(dasFeature);
        when(searchService.search(searchRequest)).thenReturn(result);
        when(result.iterator()).thenReturn(resultList.iterator());
	}

	@AfterMethod
	public void teardown(){
		Mockito.reset(searchService);
	}

	@Test()
	public void getFeaturesRange() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException
	{
		assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getFeatures(), source.getFeatures("1,dataset_dataset", 1, 100000, 100).getFeatures());
        assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getSegmentId(), source.getFeatures("1,dataset_dataset", 1, 100000, 100).getSegmentId());
        assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStartCoordinate(), source.getFeatures("1,dataset_dataset", 1, 100000, 100).getStartCoordinate());
        assertEquals(new DasAnnotatedSegment("1", 1, 100000, "1.00", "1", featureList).getStopCoordinate(), source.getFeatures("1,dataset_dataset", 1, 100000, 100).getStopCoordinate());
	}
	
	@Test()
	public void getTypes() throws UnimplementedFeatureException, DataSourceException, BadReferenceObjectException, CoordinateErrorException
	{
		List<DasType> types = new ArrayList<DasType>();
		types.add(new DasType("type", null, "?", "type"));
		assertEquals(types,source.getTypes());
	}
}
