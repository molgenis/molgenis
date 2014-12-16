package org.molgenis.data.annotation.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.HgncLocationsUtils;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.HPOTerm;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.impl.datastructures.OMIMTerm;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.annotation.provider.HpoMappingProvider;
import org.molgenis.data.annotation.provider.OmimMorbidMapProvider;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by jvelde on 2/12/14.
 * 
 * uses:
 * <p>
 * - http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/
 * ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt -
 * https://molgenis26.target.rug.nl/downloads/5gpm/GRCh37p13_HGNC_GeneLocations_noPatches.tsv -
 * ftp://ftp.omim.org/omim/morbidmap
 * </p>
 * 
 */
@Component("omimHpoService")
public class OmimHpoAnnotator extends LocusAnnotator
{
	private static final String NAME = "OmimHpoAnnotator";
	private static final String LABEL = "OmimHpo";

	public static final String OMIM_CAUSAL_IDENTIFIER = "OMIM_Causal_ID";
	public static final String OMIM_DISORDERS = "OMIM_Disorders";
	public static final String OMIM_IDENTIFIERS = "OMIM_IDs";
	public static final String OMIM_TYPE = "OMIM_Type";
	public static final String OMIM_HGNC_IDENTIFIERS = "OMIM_HGNC_IDs";
	public static final String OMIM_CYTOGENIC_LOCATION = "OMIM_Cytogenic_Location";
	public static final String OMIM_ENTRY = "OMIM_Entry";

	public static final String HPO_IDENTIFIERS = "HPO_IDs";
	public static final String HPO_GENE_NAME = "HPO_Gene_Name";
	public static final String HPO_DESCRIPTIONS = "HPO_Descriptions";
	public static final String HPO_DISEASE_DATABASE = "HPO_Disease_Database";
	public static final String HPO_DISEASE_DATABASE_ENTRY = "HPO_Disease_Database_Entry";
	public static final String HPO_ENTREZ_ID = "HPO_Entrez_ID";

	private final AnnotationService annotatorService;
	private List<HPOTerm> hpoTerms;
	private List<OMIMTerm> omimTerms;
	private Map<String, List<HPOTerm>> geneToHpoTerms;
	private Map<String, List<OMIMTerm>> geneToOmimTerms;
	private final OmimMorbidMapProvider omimMorbidMapProvider;
	private final HgncLocationsProvider hgncLocationsProvider;
	private final HpoMappingProvider hpoMappingProvider;

	@Autowired
	public OmimHpoAnnotator(AnnotationService annotatorService, OmimMorbidMapProvider omimMorbidMapProvider,
			HgncLocationsProvider hgncLocationsProvider, HpoMappingProvider hpoMappingProvider) throws IOException
	{
		if (annotatorService == null) throw new IllegalArgumentException("annotatorService is null");
		if (omimMorbidMapProvider == null) throw new IllegalArgumentException("omimMorbidMapProvider is null");
		if (hgncLocationsProvider == null) throw new IllegalArgumentException("hgncLocationsProvider is null");
		if (hpoMappingProvider == null) throw new IllegalArgumentException("hpoMappingProvider is null");
		this.annotatorService = annotatorService;
		this.omimMorbidMapProvider = omimMorbidMapProvider;
		this.hgncLocationsProvider = hgncLocationsProvider;
		this.hpoMappingProvider = hpoMappingProvider;
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
	public String getLabel()
	{
		return LABEL;
	}

	@Override
	public boolean annotationDataExists()
	{
		boolean dataExists = true;

		// TODO Check if online resources are available

		return dataExists;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException
	{
		List<Entity> results = new ArrayList<Entity>();

		String chromosome = entity.getString(CHROMOSOME);
		Long position = entity.getLong(POSITION);

		Locus locus = new Locus(chromosome, position);

		List<String> geneSymbols = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(), locus);

		Map<String, List<HPOTerm>> geneToHpoTerms = getGeneToHpoTerms();
		Map<String, List<OMIMTerm>> geneToOmimTerms = getGeneToOmimTerms();
		try
		{
			for (String geneSymbol : geneSymbols)
			{
				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				if (geneSymbol != null && geneToOmimTerms.containsKey(geneSymbol)
						&& geneToHpoTerms.containsKey(geneSymbol))
				{
					Set<String> OMIMDisorders = new HashSet<String>();
					Set<String> OMIMCytoLocations = new HashSet<String>();
					Set<String> OMIMHgncIdentifiers = new HashSet<String>();
					Set<Integer> OMIMEntries = new HashSet<Integer>();
					Set<Integer> OMIMTypes = new HashSet<Integer>();
					Set<Integer> OMIMCausedBy = new HashSet<Integer>();

					Set<String> HPOPDescriptions = new HashSet<String>();
					Set<String> HPOIdentifiers = new HashSet<String>();
					Set<Integer> HPODiseaseDatabaseEntries = new HashSet<Integer>();
					Set<String> HPODiseaseDatabases = new HashSet<String>();
					Set<String> HPOGeneNames = new HashSet<String>();
					Set<Integer> HPOEntrezIdentifiers = new HashSet<Integer>();

					for (OMIMTerm omimTerm : geneToOmimTerms.get(geneSymbol))
					{
						OMIMDisorders.add(omimTerm.getName());
						OMIMEntries.add(omimTerm.getEntry());
						OMIMTypes.add(omimTerm.getType());
						OMIMCausedBy.add(omimTerm.getCausedBy());
						OMIMCytoLocations.add(omimTerm.getCytoLoc());

						for (String hgncSymbol : omimTerm.getHgncIds())
						{
							OMIMHgncIdentifiers.add(hgncSymbol);
						}

					}

					for (HPOTerm hpoTerm : geneToHpoTerms.get(geneSymbol))
					{
						HPOPDescriptions.add(hpoTerm.getDescription());
						HPOIdentifiers.add(hpoTerm.getId());
						HPOGeneNames.add(hpoTerm.getGeneName());
						HPOEntrezIdentifiers.add(hpoTerm.getGeneEntrezID());
						HPODiseaseDatabaseEntries.add(hpoTerm.getDiseaseDbEntry());
						HPODiseaseDatabases.add(hpoTerm.getDiseaseDb());
					}

					resultMap.put(CHROMOSOME, locus.getChrom());
					resultMap.put(POSITION, locus.getPos());
					resultMap.put(OMIM_DISORDERS, OMIMDisorders);
					resultMap.put(HPO_DESCRIPTIONS, HPOPDescriptions);
					resultMap.put(OMIM_CAUSAL_IDENTIFIER, OMIMCausedBy);
					resultMap.put(OMIM_TYPE, OMIMTypes);
					resultMap.put(OMIM_HGNC_IDENTIFIERS, OMIMHgncIdentifiers);
					resultMap.put(OMIM_CYTOGENIC_LOCATION, OMIMCytoLocations);
					resultMap.put(OMIM_ENTRY, OMIMEntries);
					resultMap.put(HPO_IDENTIFIERS, HPOIdentifiers);
					resultMap.put(HPO_GENE_NAME, HPOGeneNames);
					resultMap.put(HPO_DISEASE_DATABASE, HPODiseaseDatabases);
					resultMap.put(HPO_DISEASE_DATABASE_ENTRY, HPODiseaseDatabaseEntries);
					resultMap.put(HPO_ENTREZ_ID, HPOEntrezIdentifiers);

					results.add(getAnnotatedEntity(entity, resultMap));
				}
				else
				{
					results.add(getAnnotatedEntity(entity, resultMap));
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}

	/**
	 * e.g. OMIM:614887 PEX14 5195 HP:0002240 Hepatomegaly
	 * 
	 * becomes: HPOTerm{id='HP:0002240', description='Hepatomegaly', diseaseDb='OMIM', diseaseDbEntry=614887,
	 * geneName='PEX14', geneEntrezID=5195}
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<HPOTerm> getHpoTerms() throws IOException
	{
		if (this.hpoTerms == null)
		{
			hpoTerms = new ArrayList<HPOTerm>();

			List<String> hpoLines = IOUtils.readLines(hpoMappingProvider.getHpoMapping());

			for (String line : hpoLines)
			{
				if (!line.startsWith("#"))
				{
					String[] split = line.split("\t");
					String diseaseDb = split[0].split(":")[0];
					String geneSymbol = split[1];
					String hpoId = split[3];
					String description = split[4];

					Integer diseaseDbID = Integer.parseInt(split[0].split(":")[1]);
					Integer geneEntrezId = Integer.parseInt(split[2]);

					HPOTerm hpoTerm = new HPOTerm(hpoId, description, diseaseDb, diseaseDbID, geneSymbol, geneEntrezId);
					hpoTerms.add(hpoTerm);
				}
			}
		}
		return hpoTerms;
	}

	/**
	 * Get and parse OMIM entries.
	 * 
	 * Do not store entries without OMIM identifier... e.g. this one: Leukemia, acute myelogenous (3)|KRAS, KRAS2,
	 * RASK2, NS, CFC2|190070|12p12.1
	 * 
	 * But do store this one: Leukemia, acute myelogenous, 601626 (3)|GMPS|600358|3q25.31
	 * 
	 * becomes: OMIMTerm{id=601626, name='Leukemia, acute myelogenous', type=3, causedBy=600358, cytoLoc='3q25.31',
	 * hgncIds=[GMPS]}
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<OMIMTerm> getOmimTerms() throws IOException
	{
		if (omimTerms == null)
		{
			omimTerms = new ArrayList<OMIMTerm>();

			try
			{
				List<String> omimLines = IOUtils.readLines(omimMorbidMapProvider.getOmimMorbidMap());
				for (String line : omimLines)
				{
					String[] split = line.split("\\|");

					// trim mapping method field, example: (3)
					String entry = split[0];
					entry = entry.substring(0, entry.length() - 3);

					// trim trailing whitespace
					entry = entry.trim();

					// last six characters should be OMIM id
					entry = entry.substring(entry.length() - 6);
					if (entry.matches("[0-9]+"))
					{
						List<String> genes = Arrays.asList(split[1].split(", "));

						String name = split[0].substring(0, split[0].length() - 12);
						String cytoLoc = split[3];

						Integer mutationId = Integer.parseInt(split[2]);
						Integer type = Integer
								.parseInt(split[0].substring(split[0].length() - 2, split[0].length() - 1));

						Integer omimEntry = Integer.parseInt(entry);

						OMIMTerm omimTerm = new OMIMTerm(omimEntry, name, type, mutationId, cytoLoc, genes);

						omimTerms.add(omimTerm);
					}
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return omimTerms;
	}

	private Map<String, List<OMIMTerm>> getGeneToOmimTerms() throws IOException
	{
		if (geneToOmimTerms == null)
		{
			geneToOmimTerms = new HashMap<String, List<OMIMTerm>>();

			for (OMIMTerm omimTerm : getOmimTerms())
			{
				for (String geneSymbol : omimTerm.getHgncIds())
				{
					if (geneToOmimTerms.containsKey(geneSymbol))
					{
						geneToOmimTerms.get(geneSymbol).add(omimTerm);
					}
					else
					{
						ArrayList<OMIMTerm> omimTermList = new ArrayList<OMIMTerm>();
						omimTermList.add(omimTerm);
						geneToOmimTerms.put(geneSymbol, omimTermList);
					}
				}
			}
		}
		return geneToOmimTerms;
	}

	private Map<String, List<HPOTerm>> getGeneToHpoTerms() throws IOException
	{
		if (geneToHpoTerms == null)
		{
			geneToHpoTerms = new HashMap<String, List<HPOTerm>>();
			for (HPOTerm hpoTerm : getHpoTerms())
			{
				if (geneToHpoTerms.containsKey(hpoTerm.getGeneName()))
				{
					geneToHpoTerms.get(hpoTerm.getGeneName()).add(hpoTerm);
				}
				else
				{
					ArrayList<HPOTerm> hpoTermList = new ArrayList<HPOTerm>();
					hpoTermList.add(hpoTerm);
					geneToHpoTerms.put(hpoTerm.getGeneName(), hpoTermList);
				}
			}
		}
		return geneToHpoTerms;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_CAUSAL_IDENTIFIER,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_DISORDERS,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_IDENTIFIERS,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_TYPE, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_HGNC_IDENTIFIERS,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_CYTOGENIC_LOCATION,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_ENTRY, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_IDENTIFIERS,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_GENE_NAME, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_DESCRIPTIONS,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_DISEASE_DATABASE,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_DISEASE_DATABASE_ENTRY,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_ENTREZ_ID, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		return metadata;
	}
}
