package org.molgenis.ngs.controller;

import static org.molgenis.ngs.controller.BarcodeController.URI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.HandleRequestDelegationException;
import org.molgenis.util.tuple.Tuple;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class BarcodeController extends MolgenisPlugin
{
	public static final String URI = "/plugin/barcode";

	private static final Logger logger = Logger.getLogger(BarcodeController.class);

	private List<Tuple> barcodeTuples = new ArrayList<Tuple>();
	private Set<String> barcodeTypes = new HashSet<String>(); // holds all types available (e.g. RPI, GAF, ...)

	private String currentType = "";
	private Integer currentNumber = 0;
	private List<List<Tuple>> optimalCombinations = new ArrayList<List<Tuple>>();
	private Integer minimumDistance = Integer.MIN_VALUE;
	private Double averageDistance = new Double(0);

	private final String TYPE = "type";
	private final String ID = "id";
	private final String BARCODE = "barcode";

	private boolean isException = false;

	public BarcodeController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		reset();
		
		model.addAttribute("barcodes", getBarcodeTypes());
		model.addAttribute("isException", this.isException);
		model.addAttribute("optimalCombinations", getOptimalCombinations());
		model.addAttribute("currentNumber", this.currentNumber);
		model.addAttribute("averageDistance", this.averageDistance);
		model.addAttribute("minimumDistance", this.minimumDistance);
		return "view-barcode";
	}

	@RequestMapping(value = "/calculate", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public String calculate(HttpServletRequest request, Model model) throws HandleRequestDelegationException, Exception
	{
		reset();
		this.currentType = request.getParameter("type");
		String currentNumberString = request.getParameter("number");
		try
		{
			this.currentNumber = Integer.parseInt(currentNumberString);
		}
		catch (NumberFormatException e)
		{
			logger.error("input for currentNumber is not a number");
			currentNumber = null;
		}

		if ("".equals(currentType) || currentNumber == null)
		{
			logger.error("one of the input values for barcode selector is empty");
		}
		else{

			if (null != this.currentType && this.currentNumber != null && 0 < this.currentNumber
					&& this.barcodeTypes.contains(this.currentType))
			{
				if (this.currentType.equalsIgnoreCase("RPI") && this.currentNumber <= 4)
				{
					this.isException = true;
				}
				else
				{
					this.isException = false;
					calcBarcodeSet();
				}
			}
		}
		model.addAttribute("barcodes", getBarcodeTypes());
		model.addAttribute("isException", this.isException);
		model.addAttribute("optimalCombinations", getOptimalCombinations());
		model.addAttribute("currentNumber", this.currentNumber);
		model.addAttribute("averageDistance", this.averageDistance);
		model.addAttribute("minimumDistance", this.minimumDistance);
		return "view-barcode";
	}

	public void calcBarcodeSet()
	{
		List<Tuple> barcodeTypeList = getBarcodeTypeList(this.barcodeTuples, this.currentType);

		this.optimalCombinations = new ArrayList<List<Tuple>>();

		if (barcodeTypeList.size() < this.currentNumber)
		{ // more samples than barcodes
			return;
		}
		else
		{
			List<List<Tuple>> defaults = defaultBarcodeSets(this.currentType, this.currentNumber);

			if (null != defaults)
			{ // default option was given
				this.optimalCombinations = defaults;
			}
			else
			{ // we have to do the work ourselves

				// determine inter-barcode distances in terms of SNPs
				int[][] dist = getDistance(barcodeTypeList);

				List<List<Integer>> indices = getCombinations(barcodeTypeList.size(), this.currentNumber);

				List<Integer> minDistance = new ArrayList<Integer>();
				List<Integer> sumDistance = new ArrayList<Integer>();
				Integer maximumOfMinima = Integer.MIN_VALUE;

				// For each given set of barcodes (= element of indices), determine the pair wise distance between each pair in this set
				Integer d;
				for (int i = 0; i < indices.size(); i++)
				{
					Integer min = Integer.MAX_VALUE;
					Integer sum = 0;

					for (List<Integer> pairs : getCombinations(indices.get(i), 2))
					{
						d = dist[pairs.get(0)][pairs.get(1)];
						min = Math.min(min, d);
						sum += d;
					}

					minDistance.add(min);
					sumDistance.add(sum);
					maximumOfMinima = Math.max(maximumOfMinima, min);
				}

				// select barcode-combinations with highest sum from the combi's that have the highest minimum

				// determine highest mean from those with highest min
				Integer highestSumDistance = Integer.MIN_VALUE;
				for (int j = 0; j < minDistance.size(); j++)
				{
					if (minDistance.get(j) == maximumOfMinima)
					{
						highestSumDistance = Math.max(highestSumDistance, sumDistance.get(j));
					}
				}

				// now extract the winners!
				for (int j = 0; j < minDistance.size(); j++)
				{
					if (maximumOfMinima.equals(minDistance.get(j)) && highestSumDistance.equals(sumDistance.get(j)))
					{
						// get all tuples in this solution
						List<Tuple> solution = new ArrayList<Tuple>();
						for (int k : indices.get(j))
						{
							solution.add(barcodeTypeList.get(k));
						}
						// add this solution to the result list
						this.optimalCombinations.add(solution);
					}
				}

				// also make distance information global
				this.minimumDistance = maximumOfMinima;
				this.averageDistance = (new Double(highestSumDistance)) / getCombinations(indices.get(0), 2).size();
			}
		}
	}

	private List<List<Integer>> getCombinationsHelper(List<Integer> source, List<Integer> target, int k)
	{
		List<List<Integer>> lst = new ArrayList<List<Integer>>();
		if (k == 0)
		{
			lst.add(target);
		}
		else
		{
			for (int i = 0; i < source.size(); i++)
			{
				Integer n = source.get(i);

				List<Integer> newTarget = cloneList(target);
				newTarget.add(n);

				lst.addAll(getCombinationsHelper(source.subList(i + 1, source.size()), newTarget, k - 1));
			}
		}

		return lst;
	}

	private List<Integer> cloneList(List<Integer> lst)
	{
		List<Integer> result = new ArrayList<Integer>();
		for (Integer i : lst)
			result.add(i);
		return result;
	}

	private List<List<Integer>> getCombinations(int N, int k)
	{
		List<Integer> seq = new ArrayList<Integer>();
		for (int i = 0; i < N; i++)
			seq.add(i);

		return getCombinationsHelper(seq, new ArrayList<Integer>(), k);
	}

	private List<List<Integer>> getCombinations(List<Integer> seq, int k)
	{
		return getCombinationsHelper(seq, new ArrayList<Integer>(), k);
	}

	private int[][] getDistance(List<Tuple> barcodeTypeList)
	{
		Integer n_barcodes = barcodeTypeList.size();
		int[][] dist = new int[n_barcodes][n_barcodes];

		// initialize
		for (int i = 0; i < n_barcodes; i++)
			for (int j = 0; j < n_barcodes; j++)
			{
				dist[i][j] = -1;
			}

		for (int i = 0; i < n_barcodes - 1; i++)
		{
			for (int j = i + 1; j < n_barcodes; j++)
			{
				String bc_i = barcodeTypeList.get(i).getString(BARCODE);
				String bc_j = barcodeTypeList.get(j).getString(BARCODE);
				Integer n_nucl = bc_i.length();

				dist[i][j] = 0;
				for (int k = 0; k < n_nucl; k++)
				{
					if (bc_i.charAt(k) != bc_j.charAt(k)) dist[i][j]++;
				}
			}
		}

		return dist;
	}

	/*
	 * For future use: This function may be implemented if we want to return the default barcode sets in the same way as we do with the calculated sets... Currently we just show a hard coded html table in this case... 
	 */
	private List<List<Tuple>> defaultBarcodeSets(String currentType2, Integer currentNumber2)
	{
		return null;
	}

	private List<Tuple> getBarcodeTypeList(List<Tuple> tuples, String type)
	{
		List<Tuple> typeList = new ArrayList<Tuple>();

		for (Tuple t : tuples)
		{
			if (type.equalsIgnoreCase(t.getString(TYPE)))
			{
				typeList.add(t);
			}
		}

		return typeList;
	}

	public void parse(String barcodeFile) throws IOException
	{
		this.barcodeTuples = new ArrayList<Tuple>();
		this.barcodeTypes = new HashSet<String>();

		try
		{
			@SuppressWarnings("resource")
			CsvReader reader = new CsvReader(new BufferedReader(new FileReader(barcodeFile)));

			for (Tuple row : reader)
			{
				// check value
				if (row.isNull(TYPE)) throw new IOException("required column '" + TYPE + "' is missing in row " + row);
				if (row.isNull(ID)) throw new IOException("required column '" + ID + "' is missing in row " + row);
				if (row.isNull(BARCODE)) throw new IOException("required column '" + BARCODE + "' is missing in row "
						+ row);

				this.barcodeTuples.add(row);
				this.barcodeTypes.add(row.getString(TYPE));
			}

		}
		catch (IOException e)
		{
			throw new IOException("Parsing of barcode file failed: " + e.getMessage()
					+ ".\nThe barcode csv requires columns " + TYPE + "," + ID + "," + BARCODE + ".");
		}
	}

	public List<String> getBarcodeTypes()
	{
		List<String> lst = new ArrayList<String>();
		lst.addAll(barcodeTypes);
		Collections.sort(lst); // to ensure the same order each time page is loaded
		return lst;
	}

	/**
	 * 
	 * @return three nested list. From inside to outside the lists are: - barcode type, id, barcode - a solution - list
	 *         of solutions
	 */
	public List<List<List<String>>> getOptimalCombinations()
	{
		List<List<List<String>>> resultList = new ArrayList<List<List<String>>>();
		for (List<Tuple> combi : this.optimalCombinations)
		{ // for each solution:
			List<List<String>> solution = new ArrayList<List<String>>();
			for (Tuple t : combi)
			{ // for each barcode in the solution
				List<String> barcode = new ArrayList<String>();
				barcode.add(t.getString(TYPE));
				barcode.add(t.getString(ID));
				barcode.add(t.getString(BARCODE));
				solution.add(barcode); // add barcode to the solution
			}
			resultList.add(solution);
		}

		return resultList;
	}
	
	private void reset()
	{
		barcodeTuples = new ArrayList<Tuple>();
		barcodeTypes = new HashSet<String>(); // holds all types available (e.g. RPI, GAF, ...)
		try
		{
			parse("src/main/resources/templates/barcode/barcodes.csv");
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
		
		this.currentType = "";
		this.currentNumber = 0;
		this.optimalCombinations = new ArrayList<List<Tuple>>();
		this.minimumDistance = Integer.MIN_VALUE;
		this.averageDistance = new Double(0);
		this.isException = false;
	}
}
