package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.*;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by jvelde on 2/12/14.
 * 
 * uses:
 * 
 * http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/
 * ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt
 * https://molgenis26.target.rug.nl/downloads/5gpm/GRCh37p13_HGNC_GeneLocations_noPatches.tsv
 * ftp://ftp.omim.org/omim/morbidmap
 * 
 * Todo: more fancy symptom information by using
 * http://compbio.charite.de/hudson/job/hpo.annotations/lastStableBuild/artifact/misc/phenotype_annotation.tab
 * 
 * Gene 1..N DiseaseID 1..N HPO-term
 * 
 * example lines
 * 
 * ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt: OMIM:614887 PEX14 5195 HP:0002240 Hepatomegaly
 * 
 * morbidmap: Occipital horn syndrome, 304150 (3)|ATP7A, MNK, MK, OHS, SMAX3|300011|Xq21.1
 * 
 * phenotype_annotation.tab: OMIM 102370 ACROMICRIC DYSPLASIA HP:0001156 ORPHANET:969 TAS hallmark O
 * http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=969 2014.02.06 orphanet
 * 
 * 
 */
@Component("omimHpoService")
public class OmimHpoAnnotator extends LocusAnnotator
{
	private static final String GRCH37P13_HGNC_GENE_LOCATIONS_OUTPUT_FILE = "GRCh37p13_HGNC_GeneLocations_noPatches.tsv";
	private static final String GRCH37P13_HGNC_GENE_LOCATIONS = "https://molgenis26.target.rug.nl/downloads/5gpm/GRCh37p13_HGNC_GeneLocations_noPatches.tsv";
	private static final String MORBIDMAP = "morbidmap";
	private static final String FTP_FTP_OMIM_ORG_OMIM_MORBIDMAP = "ftp://ftp.omim.org/omim/morbidmap";
	private static final String HPO_ANNOTATIONS_STABLE_BUILD_DISEASES_TO_GENES_TO_PHENOTYPES = "http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt";
	private static final String DISEASES_TO_GENES_TO_PHENOTYPES_OUTPUT_FILE = "ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt";

	public static final String OMIM_DISO = "Disease_OMIM";
	public static final String OMIM_LINK = "Hyperlink_OMIM";
	public static final String OMIM_ALL = "Full_info_OMIM";
	public static final String HPO_DESC = "Symptoms_HPO";
	public static final String HPO_LINK = "Hyperlinks_HPO";
	public static final String HPO_ALL = "Full_info_HPO";

	private static final String NAME = "OmimHpo";

	/**
	 * full info fields can be big due to many symptoms or diseases..
	 * 
	 * KCNJ13 [HPOTerm{id='HP:0000486', description='Strabismus', diseaseDb='OMIM', diseaseDbEntry=614186,
	 * geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0000007', description='Autosomal recessive inheritance',
	 * diseaseDb='OMIM', diseaseDbEntry=614186, geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0000639',
	 * description='Nystagmus', diseaseDb='OMIM', diseaseDbEntry=614186, geneName='KCNJ13', geneEntrezID=3769},
	 * HPOTerm{id='HP:0000518', description='Cataract', diseaseDb='OMIM', diseaseDbEntry=614186, geneName='KCNJ13',
	 * geneEntrezID=3769}, HPOTerm{id='HP:0000505', description='Visual impairment', diseaseDb='OMIM',
	 * diseaseDbEntry=614186, geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0100543', description='Cognitive
	 * impairment', diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769},
	 * HPOTerm{id='HP:0000365', description='Hearing impairment', diseaseDb='ORPHANET', diseaseDbEntry=65,
	 * geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0004374', description='Hemiplegia/hemiparesis',
	 * diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0000512',
	 * description='Abnormal electroretinogram', diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13',
	 * geneEntrezID=3769}, HPOTerm{id='HP:0000639', description='Nystagmus', diseaseDb='ORPHANET', diseaseDbEntry=65,
	 * geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0002084', description='Encephalocele',
	 * diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0007360',
	 * description='Aplasia/Hypoplasia of the cerebellum', diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13',
	 * geneEntrezID=3769}, HPOTerm{id='HP:0000518', description='Cataract', diseaseDb='ORPHANET', diseaseDbEntry=65,
	 * geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0000505', description='Visual impairment',
	 * diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0007703',
	 * description='Abnormal retinal pigmentation', diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13',
	 * geneEntrezID=3769}, HPOTerm{id='HP:0000648', description='Optic atrophy', diseaseDb='ORPHANET',
	 * diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0100689', description='Decreased corneal
	 * thickness', diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769},
	 * HPOTerm{id='HP:0001252', description='Muscular hypotonia', diseaseDb='ORPHANET', diseaseDbEntry=65,
	 * geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0002269', description='Abnormality of neuronal migration',
	 * diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769}, HPOTerm{id='HP:0001250',
	 * description='Seizures', diseaseDb='ORPHANET', diseaseDbEntry=65, geneName='KCNJ13', geneEntrezID=3769},
	 * HPOTerm{id='HP:0000518', description='Cataract', diseaseDb='OMIM', diseaseDbEntry=193230, geneName='KCNJ13',
	 * geneEntrezID=3769}, HPOTerm{id='HP:0000006', description='Autosomal dominant inheritance', diseaseDb='OMIM',
	 * diseaseDbEntry=193230, geneName='KCNJ13', geneEntrezID=3769}] [OMIMTerm{entry=614186, name='Leber congenital
	 * amaurosis 16', type=3, causedBy=603208, cytoLoc='2q37.1', hgncIds=[KCNJ13, SVD, LCA16]}, OMIMTerm{entry=193230,
	 * name='Snowflake vitreoretinal degeneration', type=3, causedBy=603208, cytoLoc='2q37.1', hgncIds=[KCNJ13, SVD,
	 * LCA16]}]
	 */

	@Autowired
	DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	AnnotationService annotatorService;

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
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		List<Entity> results = new ArrayList<Entity>();

		try
		{
			List<Locus> loci = new ArrayList<Locus>();

			while (source.hasNext())
			{
				Entity entity = source.next();

				String chromosome = entity.getString(CHROMOSOME);
				Long position = entity.getLong(POSITION);
				Locus locus = new Locus(chromosome, position);
				loci.add(locus);
			}

			HashMap<String, HGNCLoc> hgncLocs = getHgncLocs();
			List<String> geneSymbols = locationToHGNC(hgncLocs, loci);

			List<HPOTerm> hpoTerms = hpoTerms();
			Map<String, List<HPOTerm>> geneToHpoTerm = geneToHpoTerms(hpoTerms);

			List<OMIMTerm> omimTerms = omimTerms();
			Map<String, List<OMIMTerm>> geneToOmimTerm = geneToOmimTerms(omimTerms);

			for (int i = 0; i < loci.size(); i++)
			{
				Locus locus = loci.get(i);
				String geneSymbol = geneSymbols.get(i);

				if (geneSymbol != null && geneToOmimTerm.containsKey(geneSymbol)
						&& geneToHpoTerm.containsKey(geneSymbol))
				{
					StringBuffer omimDisorders = new StringBuffer();
					StringBuffer omimLinks = new StringBuffer();
					StringBuffer hpoDescriptions = new StringBuffer();
					StringBuffer hpoLinks = new StringBuffer();

					for (OMIMTerm omimTerm : geneToOmimTerm.get(geneSymbol))
					{
						omimDisorders.append(omimTerm.getName() + " / ");
						omimLinks.append("http://www.omim.org/entry/" + omimTerm.getEntry());
					}

					omimDisorders.delete(omimDisorders.length() - 3, omimDisorders.length());
					omimLinks.delete(omimLinks.length() - 3, omimLinks.length());

					for (HPOTerm hpoTerm : geneToHpoTerm.get(geneSymbol))
					{
						hpoDescriptions.append(hpoTerm.getDescription() + " / ");
						hpoLinks.append("http://www.human-phenotype-ontology.org/hpoweb/showterm?id=" + hpoTerm.getId());
					}

					hpoDescriptions.delete(hpoDescriptions.length() - 3, hpoDescriptions.length());
					hpoLinks.delete(hpoLinks.length() - 3, hpoLinks.length());

					HashMap<String, Object> resultMap = new HashMap<String, Object>();

					resultMap.put(OMIM_DISO, omimDisorders.toString());
					resultMap.put(OMIM_LINK, omimLinks.toString());
					resultMap.put(OMIM_ALL, geneToOmimTerm.get(geneSymbol));
					resultMap.put(HPO_DESC, hpoDescriptions.toString());
					resultMap.put(HPO_LINK, hpoLinks.toString());
					resultMap.put(HPO_ALL, geneToHpoTerm.get(geneSymbol));
					resultMap.put(CHROMOSOME, locus.getChrom());
					resultMap.put(POSITION, locus.getPos());

					results.add(new MapEntity(resultMap));
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results.iterator();
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
	public static List<HPOTerm> hpoTerms() throws IOException
	{
		List<HPOTerm> hpoTermsList = new ArrayList<HPOTerm>();
		ArrayList<String> hpoRaw = readLinesFromURL(HPO_ANNOTATIONS_STABLE_BUILD_DISEASES_TO_GENES_TO_PHENOTYPES,
				DISEASES_TO_GENES_TO_PHENOTYPES_OUTPUT_FILE);

		for (String line : hpoRaw)
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
				hpoTermsList.add(hpoTerm);
			}
		}

		return hpoTermsList;
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
	public static List<OMIMTerm> omimTerms() throws IOException
	{
		List<OMIMTerm> omimTermList = new ArrayList<OMIMTerm>();
		ArrayList<String> omimRaw = readLinesFromURL(FTP_FTP_OMIM_ORG_OMIM_MORBIDMAP, MORBIDMAP);

		for (String line : omimRaw)
		{
			String[] split = line.split("\\|");
			Integer omimEntry = null;

			try
			{
				String entry = split[0].substring(split[0].length() - 10, split[0].length() - 4);
				if (entry.matches("[0-9]+"))
				{
					List<String> genes = Arrays.asList(split[1].split(", "));

					String name = split[0].substring(0, split[0].length() - 12);
					String cytoLoc = split[3];

					Integer mutationId = Integer.parseInt(split[2]);
					Integer type = Integer.parseInt(split[0].substring(split[0].length() - 2, split[0].length() - 1));

					omimEntry = Integer.parseInt(entry);

					OMIMTerm omimTerm = new OMIMTerm(omimEntry, name, type, mutationId, cytoLoc, genes);

					omimTermList.add(omimTerm);
				}

			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		return omimTermList;
	}

	/**
	 * output: map of GeneSymbol to HGNCLoc(GeneSymbol, Start, End, Chrom)
	 * 
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, HGNCLoc> getHgncLocs() throws IOException
	{
		ArrayList<String> geneLocs = readLinesFromURL(GRCH37P13_HGNC_GENE_LOCATIONS,
				GRCH37P13_HGNC_GENE_LOCATIONS_OUTPUT_FILE);

		HashMap<String, HGNCLoc> res = new HashMap<String, HGNCLoc>();
		for (String line : geneLocs)
		{
			String[] split = line.split("\t");
			HGNCLoc hgncLoc = new HGNCLoc(split[0], Long.parseLong(split[1]), Long.parseLong(split[2]), split[3]);
			if (hgncLoc.getChrom().matches("[0-9]+|X"))
			{
				res.put(hgncLoc.getHgnc(), hgncLoc);
			}
		}
		return res;
	}

	public static ArrayList<String> readLinesFromFile(String location) throws FileNotFoundException
	{
		ArrayList<String> out = new ArrayList<String>();
		Scanner scanner = new Scanner(new File(location));

		while (scanner.hasNextLine())
		{
			out.add(scanner.nextLine());
		}

		return out;
	}

	public static ArrayList<String> readLinesFromURL(String webLocation, String cacheName) throws IOException
	{
		ArrayList<String> outputLines = new ArrayList<String>();
		File cacheLocation = new File(System.getProperty("java.io.tmpdir"), cacheName);

		if (cacheLocation.exists())
		{
			Scanner scanner = new Scanner(cacheLocation);

			while (scanner.hasNextLine())
			{
				outputLines.add(scanner.nextLine());
			}

			scanner.close();
		}
		else
		{
			URL url = new URL(webLocation);

			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			FileWriter writer = new FileWriter(cacheLocation);

			String inputLine;

			while ((inputLine = in.readLine()) != null)
			{
				outputLines.add(inputLine);
			}

			in.close();

			for (String line : outputLines)
			{
				writer.write(line + "\n");
			}

			writer.close();
		}

		return outputLines;
	}

	private static Map<String, List<OMIMTerm>> geneToOmimTerms(List<OMIMTerm> omimTermsList)
	{
		Map<String, List<OMIMTerm>> omimTermListMap = new HashMap<String, List<OMIMTerm>>();

		for (OMIMTerm omimTerm : omimTermsList)
		{
			for (String geneSymbol : omimTerm.getHgncIds())
			{
				if (omimTermListMap.containsKey(geneSymbol))
				{
					omimTermListMap.get(geneSymbol).add(omimTerm);
				}
				else
				{
					ArrayList<OMIMTerm> omimTermList = new ArrayList<OMIMTerm>();
					omimTermList.add(omimTerm);
					omimTermListMap.put(geneSymbol, omimTermList);
				}
			}
		}

		return omimTermListMap;
	}

	private static Map<String, List<HPOTerm>> geneToHpoTerms(List<HPOTerm> hpoTermsList)
	{
		Map<String, List<HPOTerm>> hpoTermListMap = new HashMap<String, List<HPOTerm>>();
		for (HPOTerm hpoTerm : hpoTermsList)
		{
			if (hpoTermListMap.containsKey(hpoTerm.getGeneName()))
			{
				hpoTermListMap.get(hpoTerm.getGeneName()).add(hpoTerm);
			}
			else
			{
				ArrayList<HPOTerm> hpoTermList = new ArrayList<HPOTerm>();
				hpoTermList.add(hpoTerm);
				hpoTermListMap.put(hpoTerm.getGeneName(), hpoTermList);
			}
		}

		return hpoTermListMap;
	}

	public static List<String> locationToHGNC(HashMap<String, HGNCLoc> hgncLocations, List<Locus> locusList)
			throws Exception
	{
		List<String> hgncSymbols = new ArrayList<String>();

		for (Locus locus : locusList)
		{
			boolean variantMapped = false;
			for (HGNCLoc hgncLocation : hgncLocations.values())
			{
				if (hgncLocation.getChrom().equals(locus.getChrom()) && locus.getPos() >= (hgncLocation.getStart() - 5)
						&& locus.getPos() <= (hgncLocation.getEnd() + 5))
				{
					hgncSymbols.add(hgncLocation.getHgnc());
					variantMapped = true;
					break;
				}
			}

			if (!variantMapped)
			{
				hgncSymbols.add(null);
			}
		}
		return hgncSymbols;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_DISO, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_LINK, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_ALL, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_DESC, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_LINK, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_ALL, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		return metadata;
	}

	public static void main(String[] args) throws Exception
	{
		// includes a gene without HGNC symbol, and a gene not related to OMIM/HPO terms
		List<Locus> loci = new ArrayList<Locus>(Arrays.asList(new Locus("2", 58453844l), new Locus("2", 71892329l),
				new Locus("2", 73679116l), new Locus("10", 112360316l), new Locus("11", 2017661l), new Locus("1",
						18151726l), new Locus("1", -1l), new Locus("11", 6637740l)));

		List<Entity> inputs = new ArrayList<Entity>();
		for (Locus l : loci)
		{
			HashMap<String, Object> inputMap = new HashMap<String, Object>();
			inputMap.put(CHROMOSOME, l.getChrom());
			inputMap.put(POSITION, l.getPos());
			inputs.add(new MapEntity(inputMap));
		}

		Iterator<Entity> res = new OmimHpoAnnotator().annotate(inputs.iterator());
		while (res.hasNext())
		{
			System.out.println(res.next().toString());
		}

	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
