package org.molgenis.omx.das;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Chromosome;
import org.molgenis.omx.xgap.Track;
import org.molgenis.omx.xgap.Variant;
import org.molgenis.util.ApplicationContextProvider;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasEntryPointOrientation;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.model.Range;

public class DasOmxDataSource implements RangeHandlingAnnotationDataSource
{
	private final DataService dataService;
	private final DasType mutationType;
	private final DasMethod method;
	private String dataset;

	@Override
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig) throws DataSourceException
	{
		this.dataset = dataSourceConfig.getDataSourceProperties().get("dataset").getValue();

	}

	// for unit test
	public DasOmxDataSource(DataService dataService) throws DataSourceException
	{
		this.dataService = dataService;
		mutationType = new DasType("mutation", null, "?", "mutation");
		method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
		this.dataset = "test";
	}

	public DasOmxDataSource() throws DataSourceException
	{
		dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);
		mutationType = new DasType("mutation", null, "?", "mutation");
		method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
	}

	@Override
	public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException
	{
		return "1.00";
	}

	@Override
	public Collection<DasEntryPoint> getEntryPoints(Integer segmentId, Integer notUsed)
			throws UnimplementedFeatureException, DataSourceException
	{
		List<DasEntryPoint> entryPoints = new ArrayList<DasEntryPoint>();

		Chromosome chromosome = getChromosome(segmentId.toString());
		entryPoints.add(new DasEntryPoint(chromosome.getName(), 0, chromosome.getBpLength(), "Chromosome", "VERSION",
				DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION, "test", false));

		return entryPoints;
	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException
	{

		String[] segmentParts = segmentId.split(",");
		String patient = null;
		Patient patientObject = null;
		if (segmentParts.length > 1)
		{
			segmentId = segmentParts[0];
			patient = segmentParts[1];
			patientObject = findPatient(patient);
		}
		if (maxbins == null) maxbins = -1;
		List<Variant> variants = queryVariants(segmentId, start, stop, patientObject);
		List<DasFeature> features = new ArrayList<DasFeature>();

		for (Variant variant : variants)
		{
			DasFeature feature;
			try
			{
				feature = createDasFeature(variant);
			}
			catch (MalformedURLException e)
			{
				throw new RuntimeException(e);
			}
			features.add(feature);
		}

		DasAnnotatedSegment segment = new DasAnnotatedSegment(segmentId, start, stop, "1.00", segmentId, features);
		return segment;

	}

	protected Patient findPatient(String patient)
	{
		return dataService.findOne(Patient.ENTITY_NAME, new QueryImpl().eq(Patient.IDENTIFIER, patient));
	}

	protected List<Variant> queryVariants(String segmentId, int start, int stop, Patient patientObject)
	{
		List<Variant> variants = new ArrayList<Variant>();
		Chromosome chromosome = getChromosome(segmentId);
		Track track = dataService.findOne(Track.ENTITY_NAME, new QueryImpl().eq(Track.IDENTIFIER, dataset));

		QueryImpl variantQuery = new QueryImpl();
		variantQuery.ge(Variant.BPSTART, start);
		variantQuery.le(Variant.BPEND, stop);
		variantQuery.eq(Variant.CHROMOSOME, chromosome);
		variantQuery.eq(Variant.TRACK, track);

		if (patientObject != null)
		{
			Variant allele1 = patientObject.getAllele1();
			Variant allele2 = patientObject.getAllele2();
			if (allele1 != null || allele2 != null)
			{
				if (allele1 != null) variantQuery.eq(Variant.IDENTIFIER, allele1.getIdentifier());
				if (allele1 != null && allele2 != null) variantQuery.or();
				if (allele2 != null) variantQuery.eq(Variant.IDENTIFIER, allele2.getIdentifier());
				variants = dataService.findAllAsList(Variant.ENTITY_NAME, variantQuery);
			}
		}
		else
		{
			variants = dataService.findAllAsList(Variant.ENTITY_NAME, variantQuery);
		}
		return variants;
	}

	protected DasFeature createDasFeature(Variant variant) throws MalformedURLException, DataSourceException
	{
		List<String> notes = new ArrayList<String>();
		Map<URL, String> linkout = new HashMap<URL, String>();
		linkout.put(new URL("http://www.molgenis.org/"), "Link");
		List<DasTarget> dasTarget = new ArrayList<DasTarget>();
		dasTarget.add(new MolgenisDasTarget(variant.getIdentifier(), variant.getBpStart().intValue(), variant
				.getBpEnd().intValue(), variant.getDescription()));
		List<String> parents = new ArrayList<String>();
		DasFeature feature = new DasFeature(variant.getIdentifier().toString(), variant.getDescription(), mutationType,
				method, variant.getBpStart().intValue(), variant.getBpEnd().intValue(), new Double(0),
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes, linkout,
				dasTarget, parents, null);
		return feature;
	}

	@Override
	public Integer getTotalCountForType(DasType type) throws DataSourceException
	{
		return new Long(dataService.count(Variant.ENTITY_NAME, new QueryImpl())).intValue();
	}

	@Override
	public Collection<DasType> getTypes() throws DataSourceException
	{
		List<DasType> types = new ArrayList<DasType>();
		types.add(mutationType);
		return types;
	}

	protected Chromosome getChromosome(String segmentId)
	{
		return dataService.findOne(Chromosome.ENTITY_NAME, new QueryImpl().eq(Chromosome.IDENTIFIER, segmentId));
	}

	// unimplemented functions
	@Override
	public void destroy()
	{
		// Mandatory to override this function, but no destroy needed.
	}

	@Override
	public DasAnnotatedSegment getFeatures(String arg0, Integer arg1) throws BadReferenceObjectException,
			DataSourceException
	{
		throw new BadReferenceObjectException(arg0, "The handling of this request is not supported");
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> arg0, Integer arg1, Range arg2)
			throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public URL getLinkURL(String arg0, String arg1) throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public DasAnnotatedSegment getFeatures(String arg0, int arg1, int arg2, Integer arg3, Range arg4)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException,
			UnimplementedFeatureException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public DasAnnotatedSegment getFeatures(String arg0, Integer arg1, Range arg2) throws BadReferenceObjectException,
			DataSourceException, UnimplementedFeatureException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}

	@Override
	public Collection<DasAnnotatedSegment> getFeatures(Collection<String> arg0, Integer arg1)
			throws UnimplementedFeatureException, DataSourceException
	{
		throw new UnimplementedFeatureException("Not implemented");
	}
}