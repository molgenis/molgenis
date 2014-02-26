package org.molgenis.data.annotation.impl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
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
 *
 */
@Component("omimHpoService")
public class KeggAnnotator extends LocusAnnotator {

    // output
    private static final String KEGG_GENE_ID = "KEGG_gene_id";
    private static final String KEGG_PATHWAYS_IDS = "KEGG_pathway_ids";
    private static final String KEGG_PATHWAYS_NAMES = "KEGG_pathway_names";

    @Autowired
    AnnotationService annotatorService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        annotatorService.addAnnotator(this);
    }

    @Autowired
    DataService dataService;

    @Autowired
    private MolgenisSettings molgenisSettings;

    @Override
    public String getName()
    {
        return "KEGG";
    }

    @Override
    public EntityMetaData getOutputMetaData()
    {
        DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());
        metadata.addAttributeMetaData(new DefaultAttributeMetaData(KEGG_GENE_ID, MolgenisFieldTypes.FieldTypeEnum.STRING));
        //
        //todo
        //
        return metadata;
    }

    @Override
    public Iterator<Entity> annotate(Iterator<Entity> source)
    {
        List<Entity> results = new ArrayList<Entity>();

        try{

            List<Locus> loci = new ArrayList<Locus>();

            while (source.hasNext())
            {
                Entity entity = source.next();

                String chromosome = entity.getString(CHROMOSOME);
                Long position = entity.getLong(POSITION);
                Locus l = new Locus(chromosome, position);
                loci.add(l);
            }

            HashMap<String, HGNCLoc> hgncLocs = OmimHpoAnnotator.getHgncLocs();
            List<String> geneSymbols = OmimHpoAnnotator.locationToHGNC(hgncLocs, loci);
            Map<String, KeggGene> keggGenes = getKeggGenes();
            Map<String, String> hgncToKeggGeneId = hgncToKeggGeneId(hgncLocs, keggGenes);
            Map<String, ArrayList<String>> keggPathwayGenes = getKeggPathwayGenes();
            Map<String, ArrayList<String>> keggGenePathways = getKeggGenePathways(keggPathwayGenes);
            Map<String, String> pathwayInfo = getKeggPathwayInfo();

            for(int i=0; i<loci.size();i++)
            {

                Locus l = loci.get(i);
                String geneSymbol = geneSymbols.get(i);

                if(geneSymbol != null)
                {
                    HashMap<String, Object> resultMap = new HashMap<String, Object>();

                    resultMap.put(CHROMOSOME, l.getChrom());
                    resultMap.put(POSITION, l.getPos());

                    resultMap.put(KEGG_GENE_ID, hgncToKeggGeneId.get(geneSymbol));

                    StringBuilder sb = new StringBuilder();
                    for(String pathwayId : keggGenePathways.get(geneSymbol)){
                        sb.append(pathwayId + ", ");
                    }
                    sb.delete(sb.length()-2, sb.length());
                    resultMap.put(KEGG_PATHWAYS_IDS, sb.toString());

                    sb = new StringBuilder();
                    for(String pathwayId : keggGenePathways.get(geneSymbol)){
                        sb.append(pathwayInfo.get(pathwayId) + ", ");
                    }
                    sb.delete(sb.length()-2, sb.length());
                    resultMap.put(KEGG_PATHWAYS_NAMES, sb.toString());

                    results.add(new MapEntity(resultMap));
                }

            }

        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        return results.iterator();
    }

    //
    // KEGG genes for homo sapiens
    //
    // stored in KeggID - KeggGene map, though KeggGene also has this identifier
    //
    // example:
    // hsa:4351	MPI, CDG1B, PMI, PMI1; mannose phosphate isomerase (EC:5.3.1.8); K01809 mannose-6-phosphate isomerase [EC:5.3.1.8]
    //
    public static Map<String, KeggGene> getKeggGenes() throws IOException {
        Map<String, KeggGene> res = new HashMap<String, KeggGene>();
        ArrayList<String> keggGenes = OmimHpoAnnotator.readLinesFromURL("http://rest.kegg.jp/list/hsa", "keggGenesHsa");
        for (String s : keggGenes) {
            String[] line = s.split("\t");
            String[] allSymbolsAndProteins = line[1].split("; ");

            String id = line[0];
            List<String> symbols = new ArrayList<String>(Arrays.asList(allSymbolsAndProteins[0].split(", ")));
            List<String> proteins = new ArrayList<String>(Arrays.asList(allSymbolsAndProteins).subList(1, allSymbolsAndProteins.length));

            KeggGene kg = new KeggGene(id, symbols, proteins);
            // System.out.println(kg);
            res.put(id, kg);

        }
        return res;
    }

    /**
     * Convert map of (pathway -> genes) to (gene -> pathways)
     * @param pathwayGenes
     * @return
     */
    public static Map<String, ArrayList<String>> getKeggGenePathways(Map<String, ArrayList<String>> pathwayGenes)
    {
        Map<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();

        for(String pathwayId : pathwayGenes.keySet())
        {
            for(String geneId : pathwayGenes.get(pathwayId))
            {
                if(res.containsKey(geneId))
                {
                    res.get(geneId).add(pathwayId);
                }
                else
                {
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(pathwayId);
                    res.put(geneId, list);
                }
            }
        }
        return  res;
    }

    //
    // KEGG pathway and gene info
    //
    // stored in map of pathwayId - List of KeggIDs
    //
    // example:
    // path:hsa00010	hsa:92483
    // path:hsa00010	hsa:92579
    // path:hsa00020	hsa:1431
    // path:hsa00020	hsa:1737
    //
    public static Map<String, ArrayList<String>> getKeggPathwayGenes()  throws IOException {
        ArrayList<String> keggGenesToPathway = OmimHpoAnnotator.readLinesFromURL(
                "http://rest.kegg.jp/link/hsa/pathway", "keggPathwaysHsa");
        Map<String, ArrayList<String>> res = new HashMap<String, ArrayList<String>>();
        for(String s : keggGenesToPathway)
        {
            String[] split = s.split("\t");

            if (res.containsKey(split[0]))
            {
                res.get(split[0]).add(split[1]);
            }
            else
            {
                res.put(split[0], new ArrayList<String>(Arrays.asList(split[1])));
            }
        }
        return res;
    }

    //
    // KEGG pathway and gene info
    //
    // stored in map of pathwayId - info
    //
    // example:
    // path:hsa00010	Glycolysis / Gluconeogenesis - Homo sapiens (human)
    //
    public static Map<String, String> getKeggPathwayInfo()  throws IOException {
        ArrayList<String> keggPathwayInfo = OmimHpoAnnotator.readLinesFromURL(
                "http://rest.kegg.jp/list/pathway/hsa", "keggPathwayInfo");

        Map<String, String> res = new HashMap<String, String>();

        for(String s : keggPathwayInfo)
        {
            String[] split = s.split("\t");
            res.put(split[0], split[1]);
        }

        return res;

    }

    //
    // map HGNC to a KeggGene identifier
    //
    public static Map<String, String> hgncToKeggGeneId(HashMap<String, HGNCLoc> hgncLocs, Map<String, KeggGene> keggGenes) throws Exception {
        Map<String, String> res = new HashMap<String, String>();

        for(String keggId : keggGenes.keySet())
        {
            KeggGene k = keggGenes.get(keggId);

            for(String symbol : k.getSymbols())
            {
                if(hgncLocs.containsKey(symbol))
                {
                    res.put(symbol, keggId);
                    break;
                }
            }
        }

        int mapped = 0;
        for(String hgnc : hgncLocs.keySet())
        {
            if(res.containsKey(hgnc))
            {
                mapped++;
            }
        }

        System.out.println("Mapping HGNC symbols to KEGG gene identifiers: mapped " + mapped + " out of " + hgncLocs.size());

        return res;
    }

    public static void main(String [ ] args) throws Exception
    {
        //includes a gene without HGNC symbol, and a gene not related to OMIM/HPO terms
        List<Locus> loci = new ArrayList<Locus>(Arrays.asList(new Locus("2", 58453844l), new Locus("2", 71892329l), new Locus("2", 73679116l), new Locus("10", 112360316l), new Locus("11", 2017661l), new Locus("1", 18151726l), new Locus("1", -1l), new Locus("11", 6637740l)));

        List<Entity> inputs = new ArrayList<Entity>();
        for(Locus l : loci)
        {
            HashMap<String, Object> inputMap = new HashMap<String, Object>();
            inputMap.put(CHROMOSOME, l.getChrom());
            inputMap.put(POSITION, l.getPos());
            inputs.add(new MapEntity(inputMap));
        }

        Iterator<Entity> res = new KeggAnnotator().annotate(inputs.iterator());
        while(res.hasNext())
        {
            System.out.println(res.next().toString());
        }

    }

}
