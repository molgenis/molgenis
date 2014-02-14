package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.*;
import org.molgenis.data.annotation.impl.datastructures.HGNCLoc;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
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

	// output
	private static final String OMIM_DISO = "Disease_OMIM";
	private static final String OMIM_LINK = "Hyperlink_OMIM";
	private static final String OMIM_ALL = "Full_info_HPO";
	private static final String HPO_DESC = "Symptoms_HPO";
	private static final String HPO_LINK = "Hyperlinks_HPO";
	private static final String HPO_ALL = "Full_info_HPO";

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

	@Override
	public String getName()
	{
		return "OmimHpo";
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_DISO, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_LINK, MolgenisFieldTypes.FieldTypeEnum.HTML));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(OMIM_ALL, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_DESC, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_LINK, MolgenisFieldTypes.FieldTypeEnum.HTML));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_ALL, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		return metadata;
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
				Locus l = new Locus(chromosome, position);
				loci.add(l);
			}

			List<HGNCLoc> hgncLocs = getHgncLocs();
			List<String> geneSymbols = locationToHGNC(hgncLocs, loci);
			List<HPOTerm> hpoTerms = hpoTerms();
			List<OMIMTerm> omimTerms = omimTerms();
			Map<String, List<HPOTerm>> geneToHpoTerm = geneToHpoTerms(hpoTerms);
			Map<String, List<OMIMTerm>> geneToOmimTerm = geneToOmimTerms(omimTerms);

			for (Locus l : loci)
			{
				HashMap<String, Object> resultMap = new HashMap<String, Object>();

				resultMap.put(CHROMOSOME, l.getChrom());
				resultMap.put(POSITION, l.getPos());
				resultMap.put(OMIM_DISO, "todo");
				resultMap.put(OMIM_LINK, "todo");
				resultMap.put(OMIM_ALL, "todo");
				resultMap.put(HPO_DESC, "todo");
				resultMap.put(HPO_LINK, "todo");
				resultMap.put(HPO_ALL, "todo");

				results.add(new MapEntity(resultMap));
			}

			// for(String gene : geneSymbols)
			// {
			// System.out.println(gene);
			// System.out.println("\t" + geneToHpoTerm.get(gene));
			// System.out.println("\t" + geneToOmimTerm.get(gene));
			// }

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
		List<HPOTerm> res = new ArrayList<HPOTerm>();
		ArrayList<String> hpoRaw = readLinesFromURL(
				"http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt",
				"ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt");

		if (hpoRaw.get(0).startsWith("#"))
		{
			hpoRaw.remove(0);
		}

		for (String s : hpoRaw)
		{
			String[] split = s.split("\t");
			String diseaseDb = split[0].split(":")[0];
			int diseaseDbID = Integer.parseInt(split[0].split(":")[1]);
			String geneSymbol = split[1];
			int geneEntrezId = Integer.parseInt(split[2]);
			String hpoId = split[3];
			String description = split[4];
			HPOTerm h = new HPOTerm(hpoId, description, diseaseDb, diseaseDbID, geneSymbol, geneEntrezId);
			res.add(h);
			// System.out.println(h);
		}

		return res;
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
		List<OMIMTerm> res = new ArrayList<OMIMTerm>();
		ArrayList<String> omimRaw = readLinesFromURL("ftp://ftp.omim.org/omim/morbidmap", "morbidmap");

		for (String s : omimRaw)
		{

			String[] pipeSplit = s.split("\\|");

			// System.out.println(pipeSplit[0]);

			Integer omimEntry = null;
			try
			{
				String entry = pipeSplit[0].substring(pipeSplit[0].length() - 10, pipeSplit[0].length() - 4);
				// System.out.println(id);
				omimEntry = Integer.parseInt(entry);
			}
			catch (Exception e)
			{
			}
			;

			if (omimEntry != null)
			{
				String name = pipeSplit[0].substring(0, pipeSplit[0].length() - 12);
				int type = Integer
						.parseInt(pipeSplit[0].substring(pipeSplit[0].length() - 2, pipeSplit[0].length() - 1));
				List<String> genes = Arrays.asList(pipeSplit[1].split(", "));
				int mutationId = Integer.parseInt(pipeSplit[2]);
				String cytoLoc = pipeSplit[3];

				OMIMTerm ot = new OMIMTerm(omimEntry, name, type, mutationId, cytoLoc, genes);

				// System.out.println(ot);
				res.add(ot);
			}

		}

		return res;
	}

	/**
	 * output: map of GeneSymbol to HGNCLoc(GeneSymbol, Start, End, Chrom)
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<HGNCLoc> getHgncLocs() throws IOException
	{
		ArrayList<String> geneLocs = readLinesFromURL(
				"https://molgenis26.target.rug.nl/downloads/5gpm/GRCh37p13_HGNC_GeneLocations_noPatches.tsv",
				"GRCh37p13_HGNC_GeneLocations_noPatches.tsv");
		List<HGNCLoc> res = new ArrayList<HGNCLoc>();
		for (String s : geneLocs)
		{
			String[] split = s.split("\t");
			HGNCLoc h = new HGNCLoc(split[0], Long.parseLong(split[1]), Long.parseLong(split[2]), split[3]);
			if (h.getChrom().matches("[0-9]+|X"))
			{
				res.add(h);
			}
		}
		return res;
	}

	public static ArrayList<String> readLinesFromFile(String location) throws FileNotFoundException
	{
		ArrayList<String> out = new ArrayList<String>();
		Scanner s = new Scanner(new File(location));
		while (s.hasNextLine())
		{
			out.add(s.nextLine());
		}
		return out;
	}

	public static ArrayList<String> readLinesFromURL(String url, String cacheName) throws IOException
	{
		ArrayList<String> out = new ArrayList<String>();

		File cacheLoc = new File(System.getProperty("java.io.tmpdir"), cacheName);
		if (cacheLoc.exists())
		{
			System.out.println("Retrieving " + cacheName + " from cached location" + cacheLoc.getAbsolutePath());
			Scanner s = new Scanner(cacheLoc);
			while (s.hasNextLine())
			{
				out.add(s.nextLine());
			}
			s.close();
		}
		else
		{
			System.out.println("Retrieving " + cacheName + " from web and saving to " + cacheLoc.getAbsolutePath());
			URL urll = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(urll.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				out.add(inputLine);
			in.close();

			FileWriter writer = new FileWriter(cacheLoc);
			for (String str : out)
			{
				writer.write(str + "\n");
			}
			writer.close();
		}

		return out;
	}

	public static void main(String[] args) throws Exception
	{

		List<Locus> loci = new ArrayList<Locus>(Arrays.asList(new Locus("2", 58453844l), new Locus("2", 71892329l),
				new Locus("2", 73679116l)));

		List<HGNCLoc> hgncLocs = getHgncLocs();
		List<String> geneSymbols = locationToHGNC(hgncLocs, loci);
		List<HPOTerm> hpoTerms = hpoTerms();
		List<OMIMTerm> omimTerms = omimTerms();
		Map<String, List<HPOTerm>> geneToHpoTerm = geneToHpoTerms(hpoTerms);
		Map<String, List<OMIMTerm>> geneToOmimTerm = geneToOmimTerms(omimTerms);

		for (String gene : geneSymbols)
		{
			System.out.println(gene);
			System.out.println("\t" + geneToHpoTerm.get(gene));
			System.out.println("\t" + geneToOmimTerm.get(gene));
		}

	}

	private static Map<String, List<OMIMTerm>> geneToOmimTerms(List<OMIMTerm> omimTerms)
	{
		Map<String, List<OMIMTerm>> res = new HashMap<String, List<OMIMTerm>>();
		for (OMIMTerm o : omimTerms)
		{
			for (String geneSymbol : o.getHgncIds())
			{
				if (res.containsKey(geneSymbol))
				{
					res.get(geneSymbol).add(o);
				}
				else
				{
					ArrayList<OMIMTerm> bla = new ArrayList<OMIMTerm>();
					bla.add(o);
					res.put(geneSymbol, bla);
				}
			}
		}
		return res;
	}

	private static Map<String, List<HPOTerm>> geneToHpoTerms(List<HPOTerm> hpoTerms)
	{
		Map<String, List<HPOTerm>> res = new HashMap<String, List<HPOTerm>>();
		for (HPOTerm h : hpoTerms)
		{
			if (res.containsKey(h.getGeneName()))
			{
				res.get(h.getGeneName()).add(h);
			}
			else
			{
				ArrayList<HPOTerm> bla = new ArrayList<HPOTerm>();
				bla.add(h);
				res.put(h.getGeneName(), bla);
			}
		}
		return res;
	}

	//
	// for each locus in the list, get the HGNC symbol in the output list
	//
	public static List<String> locationToHGNC(List<HGNCLoc> hgncLocs, List<Locus> loci) throws Exception
	{
		List<String> hgncSymbols = new ArrayList<String>();
		locusLoop: for (Locus l : loci)
		{
			boolean variantMapped = false;
			for (HGNCLoc h : hgncLocs)
			{
				if (h.getChrom().equals(l.getChrom()) && l.getPos() >= (h.getStart() - 5)
						&& l.getPos() <= (h.getEnd() + 5))
				{
					hgncSymbols.add(h.getHgnc());
					variantMapped = true;
					continue locusLoop;
				}
			}

			if (!variantMapped)
			{
				hgncSymbols.add(null);
			}

		}

		return hgncSymbols;
	}

}
