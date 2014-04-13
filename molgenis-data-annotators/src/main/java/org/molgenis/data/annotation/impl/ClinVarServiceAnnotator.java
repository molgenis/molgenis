package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.HgncLocationsUtils;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("clinvarService")
public class ClinVarServiceAnnotator extends LocusAnnotator
{
	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	private final HgncLocationsProvider hgncLocationsProvider;

	private static final String NAME = "Clinvar";
	public static final String CLINVAR_FILE_LOCATION_PROPERTY = "clinvar_location";

	public final static String ALLELEID = "AlleleID";
	public final static String TYPE = "Type";
	public final static String GENE_NAME = "Name";
	public final static String GENEID = "GeneID";
	public final static String GENESYMBOL = "GeneSymbol";
	public final static String CLINICALSIGNIFICANCE = "ClinicalSignificance";
	public final static String RS_DBSNP = "RS (dbSNP)";
	public final static String NSV_DBVAR = "nsv (dbVar)";
	public final static String RCVACCESSION = "RCVaccession";
	public final static String TESTEDINGTR = "TestedInGTR";
	public final static String PHENOTYPEIDS = "PhenotypeIDs";
	public final static String ORIGIN = "Origin";
	public final static String ASSEMBLY = "Assembly";
	public final static String CLINVAR_CHROMOSOME = "Chromosome";
	public final static String START = "Start";
	public final static String STOP = "Stop";
	public final static String CYTOGENETIC = "Cytogenetic";
	public final static String REVIEWSTATUS = "ReviewStatus";
	public final static String HGVS_C = "HGVS(c.)";
	public final static String HGVS_P = "HGVS(p.)";
	public final static String NUMBERSUBMITTERS = "NumberSubmitters";
	public final static String LASTEVALUATED = "LastEvaluated";
	public final static String GUIDELINES = "Guidelines";
	public final static String OTHERIDS = "OtherIDs";

	@Autowired
	public ClinVarServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService,
			HgncLocationsProvider hgncLocationsProvider) throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
		this.hgncLocationsProvider = hgncLocationsProvider;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	protected boolean annotationDataExists()
	{
		return new File(molgenisSettings.getProperty(CLINVAR_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		List<Entity> results = new ArrayList<Entity>();

		String chromosome = entity.getString(CHROMOSOME);
		Long position = entity.getLong(POSITION);

		List<String> geneSymbols = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(),
				new Locus(chromosome, position));

		try
		{
			if (!isAllNulls(geneSymbols))
			{
				List<String> fileLines = IOUtils.readLines(new InputStreamReader(new FileInputStream(new File(
						molgenisSettings.getProperty(CLINVAR_FILE_LOCATION_PROPERTY))), "UTF-8"));

				for (String line : fileLines)
				{
					if (!line.startsWith("#"))
					{
						String[] split = line.split("\t");
						for (String gene : geneSymbols)
						{
							if (gene.equals(split[4]))

							{
								HashMap<String, Object> resultMap = new HashMap<String, Object>();

								resultMap.put(ALLELEID, split[0]);
								resultMap.put(TYPE, split[1]);
								resultMap.put(GENE_NAME, split[2]);
								resultMap.put(GENEID, split[3]);
								resultMap.put(GENESYMBOL, split[4]);
								resultMap.put(CLINICALSIGNIFICANCE, split[5]);
								resultMap.put(RS_DBSNP, split[6]);
								resultMap.put(NSV_DBVAR, split[7]);
								resultMap.put(RCVACCESSION, split[8]);
								resultMap.put(TESTEDINGTR, split[9]);
								resultMap.put(PHENOTYPEIDS, split[10]);
								resultMap.put(ORIGIN, split[11]);
								resultMap.put(ASSEMBLY, split[12]);
								resultMap.put(CLINVAR_CHROMOSOME, split[13]);
								resultMap.put(START, split[14]);
								resultMap.put(STOP, split[15]);
								resultMap.put(CYTOGENETIC, split[16]);
								resultMap.put(REVIEWSTATUS, split[17]);
								resultMap.put(HGVS_C, split[18]);
								resultMap.put(HGVS_P, split[19]);
								resultMap.put(NUMBERSUBMITTERS, split[20]);
								resultMap.put(LASTEVALUATED, split[21]);
								resultMap.put(GUIDELINES, split[22]);
								resultMap.put(OTHERIDS, split[23]);

								resultMap.put(CHROMOSOME, chromosome);
								resultMap.put(POSITION, position);

								results.add(new MapEntity(resultMap));
							}
						}
					}
				}
			}
			else
			{
				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(CHROMOSOME, chromosome);
				resultMap.put(POSITION, position);
				results.add(new MapEntity(resultMap));
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}

	private boolean isAllNulls(Iterable<?> array)
	{
		for (Object element : array)
			if (element != null) return false;
		return true;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALLELEID, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(TYPE, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GENE_NAME, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GENEID, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GENESYMBOL, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CLINICALSIGNIFICANCE, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(RS_DBSNP, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(NSV_DBVAR, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(RCVACCESSION, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(TESTEDINGTR, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PHENOTYPEIDS, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ORIGIN, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ASSEMBLY, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CLINVAR_CHROMOSOME, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(START, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(STOP, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CYTOGENETIC, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REVIEWSTATUS, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HGVS_C, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HGVS_P, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(NUMBERSUBMITTERS, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(LASTEVALUATED, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GUIDELINES, FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OTHERIDS, FieldTypeEnum.TEXT));

		return metadata;
	}

}
