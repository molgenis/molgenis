package org.molgenis.ngs.plugins.barcode;

import org.elasticsearch.common.mvel2.optimizers.impl.refl.nodes.ArrayLength;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;
import org.molgenis.util.HandleRequestDelegationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.tuple.Tuple;

/**
 *
 */
public class BarcodePlugin extends PluginModel<Entity>
{
	private List<Tuple> barcodeTuples = new ArrayList<Tuple>();
	private Set<String> barcodeTypes = new HashSet<String>(); // holds all types available (e.g. RPI, GAF, ...)

	private String currentType = "";
	private Integer currentNumber = 0;
	private List<List<Tuple>> optimalCombinations = new ArrayList<List<Tuple>>();
	private Integer minimumDistance = Integer.MIN_VALUE;
	private Double averageDistance = new Double(0);

	private boolean isException = false;

	private final String TYPE = "type";
	private final String ID = "id";
	private final String BARCODE = "barcode";

	private static final long serialVersionUID = 1L;

	public BarcodePlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);

		getModel().setLabel("Barcode");

	}

	@Override
	public String getViewTemplate()
	{
		// TODO Auto-generated method stub
		return "templates/barcode/BarcodePlugin.ftl";
	}

	@Override
	public String getViewName()
	{
		// TODO Auto-generated method stub
		return BarcodePlugin.class.getSimpleName();
	}

	@Override
	public void reload(Database db)
	{
		try
		{
			parse("src/main/resources/templates/barcode/barcodes.csv");
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws HandleRequestDelegationException, Exception
	{
		this.currentType = request.getString("type");
		this.currentNumber = request.getInt("number");

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

				// determine inter-barcode distances
				int[][] dist = getDistance(barcodeTypeList);

				List<List<Integer>> indices = getCombinations(barcodeTypeList.size(), this.currentNumber);

				List<Integer> minDistance = new ArrayList<Integer>();
				List<Integer> sumDistance = new ArrayList<Integer>();
				Integer maximumOfMinima = Integer.MIN_VALUE;

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

	@Override
	public boolean isVisible()
	{
		return true;
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

	public Integer getCurrentNumber()
	{
		return this.currentNumber;
	}

	public Integer getMinimumDistance()
	{
		return minimumDistance;
	}

	public Double getAverageDistance()
	{
		return averageDistance;
	}

	public boolean isException()
	{
		return isException;
	}
}