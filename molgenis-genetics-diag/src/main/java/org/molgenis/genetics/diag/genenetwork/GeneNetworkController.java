package org.molgenis.genetics.diag.genenetwork;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.genetics.diag.genenetwork.meta.GeneNetworkScoreFactory;
import org.molgenis.genetics.diag.genenetwork.meta.GeneNetworkScoreMetaData;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.molgenis.genetics.diag.genenetwork.GeneNetworkController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class GeneNetworkController extends MolgenisPluginController
{
	public static final String GN_APP = "genenetwork";
	public static final String URI = PLUGIN_URI_PREFIX + GN_APP;
	public static final int BATCH_SIZE = 10;//Number of rows in the original gene networks input file to process and insert into the database in one batch

	private final DataService dataService;
	private final GeneNetworkScoreFactory geneNetworkScoreFactory;

	@Autowired
	public GeneNetworkController(DataService dataService, GeneNetworkScoreFactory geneNetworkScoreFactory)
	{
		super(URI);
		this.dataService = dataService;
		this.geneNetworkScoreFactory = geneNetworkScoreFactory;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		return "view-gn-import";
	}

	@RequestMapping(value = "/import", method = POST)
	@ResponseStatus(HttpStatus.OK)
	public void importScores(@RequestParam(value = "genenetworkfile") String geneNetworkFile,
			@RequestParam(value = "genemappingfile") String geneMappingFile) throws IOException
	{
		try
		{
			File input = new File(geneNetworkFile);
			Scanner scanner = new Scanner(input, "UTF-8");
			Scanner hpoScanner = new Scanner(scanner.nextLine());
			List<String> hpoTerms = createHpoTermList(hpoScanner);
			Map<String, String> geneMap = createEnsembleHugoMap(geneMappingFile);
			List<Entity> entities = new ArrayList<>();
			int rowNr = 0;
			while (scanner.hasNext())
			{
				entities.addAll(processSingleInputLine(scanner, hpoTerms, geneMap));
				persistGeneNetworkScoreEntities(rowNr, entities);
				rowNr++;
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	protected List<Entity> processSingleInputLine(Scanner scanner, List<String> hpoTerms, Map<String, String> geneMap)
	{
		Scanner rowScanner = new Scanner(scanner.nextLine());
		String gene = null;
		if (rowScanner.hasNext()) gene = rowScanner.next();
		int i = 0;
		List<Entity> entities = new ArrayList<>();
		while (rowScanner.hasNext())
		{
			createEntities(hpoTerms, geneMap, rowScanner, gene, i, entities);
			++i;
		}
		return entities;
	}

	private void persistGeneNetworkScoreEntities(int rowNr, List<Entity> entities)
	{
		if ((rowNr % BATCH_SIZE) == 0)
		{
			System.out.println("inserting batch into database.");
			dataService
					.add(PACKAGE_SYSTEM + PACKAGE_SEPARATOR + GeneNetworkScoreMetaData.SIMPLE_NAME, entities.stream());
			entities.clear();
		}
	}

	protected List<String> createHpoTermList(Scanner hpoScanner)
	{
		List<String> hpoTerms = new ArrayList<>();
		hpoScanner.next();
		while (hpoScanner.hasNext())
		{
			hpoTerms.add(hpoScanner.next().replace(":","_"));
		}
		return hpoTerms;
	}

	protected void createEntities(List<String> hpoTerms, Map<String, String> geneMap, Scanner rowScanner, String gene,
			int i, List<Entity> entities)
	{
		Entity entity = geneNetworkScoreFactory.create();
		entity.set(GeneNetworkScoreMetaData.ENSEMBL_ID, gene);
		entity.set(GeneNetworkScoreMetaData.HPO, hpoTerms.get(i));
		entity.set(GeneNetworkScoreMetaData.SCORE, Double.parseDouble(rowScanner.next()));
		entity.set(GeneNetworkScoreMetaData.HUGO_SYMBOL, geneMap.get(gene));
		entities.add(entity);
	}

	protected Map<String, String> createEnsembleHugoMap(String geneMappingFilePath) throws FileNotFoundException
	{
		File geneMappingFile = new File(geneMappingFilePath);
		Scanner geneMappingScanner = new Scanner(geneMappingFile, "UTF-8");
		Map<String, String> geneMap = new HashMap<>();
		geneMappingScanner.nextLine();//skip header
		while (geneMappingScanner.hasNext())
		{
			String hugo = "";
			String ensembl;
			Scanner geneScanner = new Scanner(geneMappingScanner.nextLine());
			if (geneScanner.hasNext()) ensembl = geneScanner.next();
			else throw new RuntimeException("every line should have at lease an ensembl ID");
			if (geneScanner.hasNext()) hugo = geneScanner.next();
			geneMap.put(ensembl, hugo);
		}
		return geneMap;
	}
}
