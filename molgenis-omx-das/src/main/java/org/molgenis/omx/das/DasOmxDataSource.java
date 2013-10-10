package org.molgenis.omx.das;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.omx.patient.Patient;
import org.molgenis.omx.xgap.Chromosome;
import org.molgenis.omx.xgap.Variant;
import org.molgenis.util.ApplicationUtil;

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
	private Database database;
	private DasType mutationType;
	private DasMethod method;

	public DasOmxDataSource() throws DataSourceException
	{
		database = ApplicationUtil.getDatabase();
		mutationType = new DasType("mutation", null, "?", "mutation");
		method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
	}

	@Override
	public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException
	{
		return "1.00";
	}

	@Override
	public Collection<DasEntryPoint> getEntryPoints(Integer arg0, Integer arg1) throws UnimplementedFeatureException,
			DataSourceException
	{
		Set<DasEntryPoint> entryPoints = new TreeSet<DasEntryPoint>();
		try
		{
			entryPoints.add(
							new DasEntryPoint(getChromosome(arg0.toString()).getName(), 0, getChromosome(arg0.toString()).getBpLength(), "Chromosome", "VERSION",
							DasEntryPointOrientation.NO_INTRINSIC_ORIENTATION, "test", false));
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException("error getting das entrypoints. "+e);
		}
		
		return entryPoints;
	}

	public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException
	{
		try
		{
			String[] segmentParts = segmentId.split(",");
			String patient = null;
			Patient patientObject = null;
			if(segmentParts.length >1){
				segmentId = segmentParts[0];
				patient = segmentParts[1];				
				patientObject = queryPatients(patient);
			}
		if (maxbins == null) maxbins = -1;
			List<Variant> variants = queryVariants(segmentId, start, stop, patientObject);
			List<DasFeature> features = new ArrayList<DasFeature>();

			for (Variant variant : variants)
			{
				DasFeature feature = createDasFeature(variant);
				features.add(feature);
			}

			DasAnnotatedSegment segment = new DasAnnotatedSegment(segmentId, start, stop, "1.00", segmentId, features);
			return segment;
		}
		catch (Exception e)
		{
			throw new RuntimeException("error getting das features. "+e);
		}
	}

	private Patient queryPatients(String patient) throws DatabaseException
	{
		Patient patientObject;
		Query<Patient> patientQuery = database.query(Patient.class);
		patientQuery.equals(Patient.ID, patient);
		List<Patient> patients = patientQuery.find();
		patientObject = patients.get(0);
		return patientObject;
	}

	private List<Variant> queryVariants(String segmentId, int start, int stop, Patient patientObject)
			throws DatabaseException
	{
		Chromosome chromosome = getChromosome(segmentId);

		Query<Variant> variantQuery = database.query(Variant.class);
		variantQuery.greaterOrEqual(Variant.BPSTART, start);
		variantQuery.lessOrEqual(Variant.BPEND, stop);
		variantQuery.equals(Variant.CHROMOSOME, chromosome.getId());
		if(patientObject!=null){
			Variant allele1 = patientObject.getAllele1();
			Variant allele2 = patientObject.getAllele2();
			if(allele1 == null && allele2 ==null){
				return new ArrayList<Variant>();//no mutations for selected patient, return empty list
			}
			if(allele1 != null)variantQuery.equals(Variant.IDENTIFIER,allele1.getIdentifier());
			if(allele1 != null && allele2 !=null) variantQuery.or();
			if(allele2 != null)variantQuery.equals(Variant.IDENTIFIER,allele2.getIdentifier());
		}
		List<Variant> variants = variantQuery.find();
		return variants;
	}

	private DasFeature createDasFeature(Variant variant) throws MalformedURLException, DataSourceException
	{
		List<String> notes = new ArrayList<String>();
		Map<URL, String> linkout = new HashMap<URL, String>();
		linkout.put(new URL("http://www.molgenis.org/"),"Link");
		List<DasTarget> dasTarget = new ArrayList<DasTarget>();
		dasTarget.add(new DasTarget(variant.getIdentifier(), variant.getBpStart().intValue(), variant.getBpEnd().intValue(), variant.getDescription()));
		List<String> parents = new ArrayList<String>();
		DasFeature feature = new DasFeature(variant.getIdentifier().toString(), variant.getDescription(), mutationType,
				method, variant.getBpStart().intValue(), variant.getBpEnd().intValue(), null,
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE
				,notes,linkout,dasTarget,parents,null
				);
		return feature;
	}

	@Override
	public Integer getTotalCountForType(DasType arg0) throws DataSourceException
	{
		try
		{
			return database.count(Variant.class);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException("error getting das total type count. "+e);
		}
	}

	@Override
	public Collection<DasType> getTypes() throws DataSourceException
	{
		List<DasType> types = new ArrayList<DasType>();
		types.add(mutationType);
		return types;
	}

	private Chromosome getChromosome(String segmentId) throws DatabaseException
	{
		Query<Chromosome> variantQuery = database.query(Chromosome.class);
		variantQuery.greaterOrEqual(Chromosome.ID, segmentId);
		Chromosome chromosome = variantQuery.find().get(0);
		return chromosome;
	}

	// unimplemented functions
	@Override
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig) throws DataSourceException
	{
		// Mandatory to override this function, but no destroy needed.
	}

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