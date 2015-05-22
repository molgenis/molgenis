package org.molgenis.data.annotation.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeltaGeneDataProvider
{
	HashMap<String, Stack<String>> hpoGeneData;
	HashMap<String, List<String>> hpoHeirarchy;
	MolgenisSettings molgenisSettings;
	private boolean assocIsParsed = false;
	private boolean hpoIsParsed = false;
	
	public final static ExecutorService THREADPOOL = Executors.newFixedThreadPool(2);

	// loads gene and hpo data when needed
	public void getData() {
		try {
			if (hpoGeneData.isEmpty() || hpoHeirarchy.isEmpty()) {
				// process gene association and hpo heirarchy simultaneuously 
				Future<?> hpoFuture = THREADPOOL.submit(new Runnable() {
					@Override
					public void run()  {
						hpoIsParsed = parseHPOFile();
					}
				});
				Future<?> assocFuture = THREADPOOL.submit(new Runnable() {
					@Override
					public void run() {
						assocIsParsed = parseAssociationFile();
					}
				});
				// but block until processing is done
				while (!hpoFuture.isDone() || !assocFuture.isDone()) {
					Thread.sleep(1);
				}
			}
		}catch (InterruptedException e){
			throw new RuntimeException("Error while waiting for Deltagene data to finish building in function getData()");
		}
	}
	
	public Stack<String> getHPOGeneStack (String hpo) {
		if (!isReady())
			getData();
		Stack<String> out = new Stack<String>();
		if (hpoHeirarchy.containsKey(hpo)) {
			for (String child : hpoHeirarchy.get(hpo)) 
				out.addAll(getHPOGeneStack(child));
			if (hpoGeneData.containsKey(hpo)){
				out.addAll(hpoGeneData.get(hpo));
				return out;
			}
			else
				return out;
		}else{
			return out;
		}
	}
	
	@Autowired
	public DeltaGeneDataProvider(MolgenisSettings molgenisSettings)  
	{
		this.molgenisSettings = molgenisSettings;
		
		hpoGeneData = new HashMap<String, Stack<String>>();
		
		/* using a hashmap for heirarchy instead of a tree.
		 * no duplicate values should occur, hashtable has O(1)
		 * insertion and search complexity, and no entries will 
		 * be removed, making it faster than a tree with 
		 * O(log(n))-complexity
		 */
		hpoHeirarchy = new HashMap<String, List<String>>();
	}
	
	/**
	 * this parses the HPO file found on
	 * http://compbio.charite.de/hudson/job/hpo/lastStableBuild/artifact/hp/hp.obo
	 * and populates the hpoHeirarchy with heirarchy information
	 * @return true on success
	 */
	boolean parseHPOFile() 
	{
		try {
			// temporary, make local copy & check if newer!
			URL url = new URL("http://compbio.charite.de/hudson/job/"
					+ "hpo/lastStableBuild/artifact/hp/hp.obo");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()), 1200);
			String line;
			String hpo = null;
			String parent = null;
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
	boolean parseAssociationFile()
	{
		Stack<String> out = new Stack<String>();
		Long start, stop, time = 0L;
		start = System.currentTimeMillis();
		try {
			// temporary, make local copy & check if newer!
			URL url = new URL("http://compbio.charite.de/hudson/job/"
					+ "hpo.annotations.monthly/lastStableBuild/artifact/"
					+ "annotation/ALL_SOURCES_ALL_FREQUENCIES_"
					+ "diseases_to_genes_to_phenotypes.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()), 150);
			String hpo;
			String gene;
			String line;
			String[] lineSplit;
			int HPOColumn = -1;
			int GeneColumn = -1;
			
			while ((line = in.readLine()) != null) {
				/* this if reads the order of columns in the association
				 * file, which has changed before and might change again.
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
					if (!hpoGeneData.containsKey(hpo))
						hpoGeneData.put(hpo, new Stack<String>());
					gene = lineSplit[GeneColumn];
					if (hpoGeneData.containsKey(hpo))
						hpoGeneData.get(hpo).add(gene);
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
	
	/**
	 * check whether HPO and association files have been parsed
	 */
	public boolean isReady()
	{
		return (hpoIsParsed == true && assocIsParsed == true);
	}
}