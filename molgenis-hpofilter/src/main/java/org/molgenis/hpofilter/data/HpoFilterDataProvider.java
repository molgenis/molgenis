package org.molgenis.hpofilter.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HpoFilterDataProvider
{
	public final static ExecutorService THREADPOOL = Executors.newFixedThreadPool(2);
	// the heirarchy files list all HPO terms, Genes or not, and their parents.
	// TODO: Make into local copy
	public static final String KEY_HPO_HEIRARCHY = "plugin.annotators.hpo.heirarchy.url";
	public static final String DEFAULT_HPO_HEIRARCHY_LOCATION = "http://compbio.charite.de/hudson/job/"
			+ "hpo/lastStableBuild/artifact/hp/hp.obo";
	// This uses the same mapping location as the OMIMHPO annotator
	// TODO: Make into local copy
	public static final String KEY_HPO_MAPPING= "plugin.annotators.hpo.mapping.url";
	public static final String DEFAULT_HPO_MAPPING_LOCATION = "http://compbio.charite.de/hudson/job/"
	+ "hpo.annotations.monthly/lastStableBuild/artifact/"
	+ "annotation/ALL_SOURCES_ALL_FREQUENCIES_"
	+ "diseases_to_genes_to_phenotypes.txt";

	// TODO use multimap?
	HashMap<String, HashSet<String>> hpoMap;
	HashMap<String, List<String>> hpoHeirarchy;
	HashMap<String, String> descriptionMap;
	MolgenisSettings molgenisSettings;
	private boolean assocIsParsed = false;
	private boolean hpoIsParsed = false;
			

	@Autowired
	public HpoFilterDataProvider(MolgenisSettings molgenisSettings)  
	{
		this.molgenisSettings = molgenisSettings;
		
		hpoMap = new HashMap<String, HashSet<String>>();
		/* using a hashmap for heirarchy instead of a tree.
		 * no duplicate values should occur, hashtable has O(1)
		 * insertion and search complexity, and no entries will 
		 * be removed, making it faster than a tree with 
		 * O(log(n))-complexity, even if it's marginally faster.
		 */
		hpoHeirarchy = new HashMap<String, List<String>>();
		/*
		 * This HashMap contains the description -> HPO
		 * mappings, for autocomplete.
		 */
		descriptionMap = new HashMap<String, String>();
	}
	
	public HashMap<String, HashSet<String>> getAssocData() {
		if (!assocIsParsed)
			getData();
		return hpoMap;
	}

	public HashMap<String, String> getDescriptionMap() {
		if (!hpoIsParsed)
			getData();
		return descriptionMap;
	}
	
	// loads gene and hpo data when needed
	public void getData() {
		try {
			if (hpoMap.isEmpty() || hpoHeirarchy.isEmpty()) {
				// process gene association and hpo heirarchy simultaneuously 
				THREADPOOL.submit(new Runnable() {
					@Override
					public void run()  {
						hpoIsParsed = parseHPOHeirarchy();
					}
				});
				THREADPOOL.submit(new Runnable() {
					@Override
					public void run() {
						assocIsParsed = parseHPOMapping();
					}
				});
				// but block until processing is done
				while (!hpoIsParsed || !assocIsParsed) {
					Thread.sleep(1);
				}
			}
		}catch (InterruptedException e){
			throw new RuntimeException("Error while waiting for HPO filter data to finish building in function getData()");
		}
	}
	
	public HashMap<String, List<String>> getHPOData() {
		if (!hpoIsParsed)
			getData();
		return hpoHeirarchy;
	}
	
	/**
	 * check whether HPO and association files have been parsed
	 */
	public boolean isReady()
	{
		return (hpoIsParsed == true && assocIsParsed == true);
	}

	/**
	 * this parses the HPO file found on
	 * http://compbio.charite.de/hudson/job/hpo/lastStableBuild/artifact/hp/hp.obo
	 * and populates the hpoHeirarchy with heirarchy information
	 * @return true on success
	 */
	boolean parseHPOHeirarchy() 
	{
		try {
			// temporary, make local copy & check if newer!
			URL url = new URL(molgenisSettings.getProperty(KEY_HPO_HEIRARCHY, DEFAULT_HPO_HEIRARCHY_LOCATION));
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()), 1200);
			String line;
			String hpo = null;
			String parent = null;
			String name = null;
			Long start, stop, time = 0L;
			start = System.currentTimeMillis();
			
			while ((line = in.readLine()) != null) {
				// process a new term
				if (line.equals("[term]")) {
					hpo = null;
					parent = null;
				}
				if (line.startsWith("id:")) {
					hpo = line.substring(4);
					if (!hpoHeirarchy.containsKey(hpo))
						hpoHeirarchy.put(hpo, new ArrayList<String>());
				}
				if (line.startsWith("name:")) {
					name = line.substring(6);
					if (null != hpo) {
						descriptionMap.put(name, hpo);
					}
				}
				if (line.startsWith("is_a:") && hpo != null) {
					parent = line.substring(6,16);
					if (!hpoHeirarchy.containsKey(parent))
						hpoHeirarchy.put(parent, new ArrayList<>());
					hpoHeirarchy.get(parent).add(hpo);
				}
			}
			stop  = System.currentTimeMillis();
			time += stop - start;
			System.out.println("Parsing HPO file took "+time+" millis on thread "+Thread.currentThread().getId());
			
			in.close();
			return true;
		}catch (IOException e) {
			throw new RuntimeException("Error while downloading or reading HPO file.");
		}
	}
	
	/**
	 * Parses association file to associate a stack of genes
	 * with a HPO term
	 * @return true on success.
	 */
	boolean parseHPOMapping()
	{
		Stack<String> out = new Stack<String>();
		Long start, stop, time = 0L;
		start = System.currentTimeMillis();
		try {
			// temporary, make local copy & check if newer!
			URL url = new URL(molgenisSettings.getProperty(KEY_HPO_MAPPING, DEFAULT_HPO_MAPPING_LOCATION));
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()), 150);
			String hpo;
			String gene;
			String line;
			String[] lineSplit;
			int HPOColumn = -1;
			int GeneColumn = -1;
			
			while ((line = in.readLine()) != null) {
				/* this reads the order of columns in the mapping file,
				 * which has changed before and might change again.
				 */
				if (line.startsWith("#")) {
					lineSplit = line.substring(9).split("<tab>");
					for (int i = 0; i < lineSplit.length; i++) {
						if (lineSplit[i].equals("HPO-ID")) 
							HPOColumn = i;
						if (lineSplit[i].equals("gene-symbol"))
							GeneColumn = i;
					}
				}
				if (line.contains("HP:") && HPOColumn > -1 && GeneColumn > -1) {
					lineSplit = line.split("\t");
					hpo = lineSplit[HPOColumn];
					if (!hpoMap.containsKey(hpo))
						hpoMap.put(hpo, new HashSet<String>());
					gene = lineSplit[GeneColumn];
					if (hpoMap.containsKey(hpo))
						if (!hpoMap.get(hpo).contains(gene))
							hpoMap.get(hpo).add(gene);
				}
				out.removeAllElements();
			}
			stop  = System.currentTimeMillis();
			time += stop - start;
			System.out.println("Parsing association file took "+time+" millis on thread "+Thread.currentThread().getId());
			
			in.close();
			return true;
		}catch (IOException e) {
			throw new RuntimeException("Error while downloading or reading association file");
		}
	}
}