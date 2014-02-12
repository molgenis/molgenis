package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.*;
import org.molgenis.data.annotation.impl.datastructures.HGNCLoc;
import org.molgenis.data.annotation.impl.datastructures.HPOTerm;
import org.molgenis.data.annotation.impl.datastructures.HGNCLoc;
import org.molgenis.data.annotation.impl.datastructures.OMIMTerm;
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
 * http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt
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
 * ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt:
 * OMIM:614887	PEX14	5195	HP:0002240	Hepatomegaly
 *
 * morbidmap:
 * Occipital horn syndrome, 304150 (3)|ATP7A, MNK, MK, OHS, SMAX3|300011|Xq21.1
 *
 * phenotype_annotation.tab:
 * OMIM	102370	ACROMICRIC DYSPLASIA		HP:0001156	ORPHANET:969	TAS		hallmark		O	http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=969	2014.02.06	orphanet
 *
 *
 */
@Component("omimHpoService")
public class OmimHpoAnnotator implements RepositoryAnnotator {

    // the OmimHpoAnnotator is dependant on these two values
    private static final String CHROMOSOME = "chrom";
    private static final String POSITION = "pos";

    // output
    private static final String IETS = "iets";

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
    public Boolean canAnnotate(EntityMetaData inputMetaData)
    {
        boolean canAnnotate = true;
        Iterable<AttributeMetaData> inputAttributes = getInputMetaData().getAttributes();
        for(AttributeMetaData attribute : inputAttributes){
            if(inputMetaData.getAttribute(attribute.getName()) == null){
                //all attributes from the inputmetadata must be present to annotate.
                canAnnotate = false;
            }
        }
        return canAnnotate;
    }

    @Override
    public EntityMetaData getOutputMetaData()
    {
        String[] caddFeatures = new String[]
                { IETS };

        DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

        for (String attribute : caddFeatures)
        {
            metadata.addAttributeMetaData(new DefaultAttributeMetaData(attribute, MolgenisFieldTypes.FieldTypeEnum.STRING));
        }

        return metadata;
    }

    @Override
    public EntityMetaData getInputMetaData()
    {
        DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

        metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHROMOSOME, MolgenisFieldTypes.FieldTypeEnum.STRING));
        metadata.addAttributeMetaData(new DefaultAttributeMetaData(POSITION, MolgenisFieldTypes.FieldTypeEnum.STRING));

        return metadata;
    }



    @Override
    public Iterator<Entity> annotate(Iterator<Entity> source)
    {
        List<Entity> results = new ArrayList<Entity>();
        String systemCall = molgenisSettings.getProperty("OmimHpoAnnotator_data_location");

        HashMap<String, Object> resultMap = new HashMap<String, Object>();

        try{

        Map<String, HGNCLoc> hgncLocs = getHgncLocs();

            while (source.hasNext())
            {
                Entity entity = source.next();

                String chromosome = entity.get(CHROMOSOME).toString();
                String position = entity.get(POSITION).toString();

                String caddAbs = "";
                String caddScaled = "";




            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        return results.iterator();
    }

    public static List<HPOTerm> hpoTerms() throws IOException {
        List<HPOTerm> res = new ArrayList<HPOTerm>();
        ArrayList<String> omimRaw = readLinesFromURL(
                "http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt", "ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt");



        return res;
    }

    /**
     * Get and parse OMIM entries.
     *
     * Do not store entries without OMIM identifier... e.g. this one:
     * Leukemia, acute myelogenous (3)|KRAS, KRAS2, RASK2, NS, CFC2|190070|12p12.1
     *
     * But do store this one:
     * Leukemia, acute myelogenous, 601626 (3)|GMPS|600358|3q25.31
     *
     * @return
     * @throws IOException
     */
    public static List<OMIMTerm> omimTerms() throws IOException {
        List<OMIMTerm> res = new ArrayList<OMIMTerm>();
        ArrayList<String> omimRaw = readLinesFromURL(
                "ftp://ftp.omim.org/omim/morbidmap", "morbidmap");

        for(String s : omimRaw)
        {

            String[] pipeSplit = s.split("\\|");

            // System.out.println(pipeSplit[0]);

            Integer entryID = null;
            try{
                String id = pipeSplit[0].substring(pipeSplit[0].length()-10, pipeSplit[0].length()-4);
                //        System.out.println(id);
                entryID = Integer.parseInt(id);
            }catch(Exception e){};

            if(entryID!=null)
            {
                String name = pipeSplit[0].substring(0, pipeSplit[0].length()-12);
                int type = Integer.parseInt(pipeSplit[0].substring(pipeSplit[0].length()-2, pipeSplit[0].length()-1));
                List<String> genes = Arrays.asList(pipeSplit[1].split(", "));
                int mutationId = Integer.parseInt(pipeSplit[2]);
                String cytoLoc = pipeSplit[3];

                OMIMTerm ot = new OMIMTerm(entryID, name, type, mutationId, cytoLoc, genes);

                System.out.println(ot);
                res.add(ot);
            }

        }

        return res;
    }

    public static Map<String, HGNCLoc> getHgncLocs() throws IOException {
        ArrayList<String> geneLocs = readLinesFromURL(
                "https://molgenis26.target.rug.nl/downloads/5gpm/GRCh37p13_HGNC_GeneLocations_noPatches.tsv",
                "GRCh37p13_HGNC_GeneLocations_noPatches.tsv");

        Map<String, HGNCLoc> res = new HashMap<String,HGNCLoc>();

        for(String s : geneLocs)
        {
            String[] split = s.split("\t");
            HGNCLoc h = new HGNCLoc(split[0],Long.parseLong(split[1]), Long.parseLong(split[2]), split[3]);
            if(h.getChrom().matches("[0-9]+|X")){
                res.put(split[0], h);
            }
        }
        return res;
    }

    public static ArrayList<String> readLinesFromFile(String location) throws FileNotFoundException {
        ArrayList<String> out = new ArrayList<String>();
        Scanner s = new Scanner(new File(location));
        while(s.hasNextLine())
        {
            out.add(s.nextLine());
        }
        return out;
    }

    public static ArrayList<String> readLinesFromURL(String url, String cacheName)
            throws IOException {
        ArrayList<String> out = new ArrayList<String>();

        File cacheLoc = new File(System.getProperty("java.io.tmpdir"),
                cacheName);
        if (cacheLoc.exists()) {
            System.out.println("Retrieving " + cacheName
                    + " from cached location" + cacheLoc.getAbsolutePath());
            Scanner s = new Scanner(cacheLoc);
            while (s.hasNextLine()) {
                out.add(s.nextLine());
            }
            s.close();
        } else {
            System.out.println("Retrieving " + cacheName
                    + " from web and saving to " + cacheLoc.getAbsolutePath());
            URL urll = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urll.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                out.add(inputLine);
            in.close();

            FileWriter writer = new FileWriter(cacheLoc);
            for (String str : out) {
                writer.write(str + "\n");
            }
            writer.close();
        }

        return out;
    }



    public static void main(String [ ] args) throws IOException {
        omimTerms();

    }
}
