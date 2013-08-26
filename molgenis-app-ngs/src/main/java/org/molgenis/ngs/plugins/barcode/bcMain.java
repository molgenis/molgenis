package org.molgenis.ngs.plugins.barcode;

import java.util.ArrayList;
import java.util.List;

public class bcMain
{
	private static List<List<Integer>> getCombinationsHelper(List<Integer> source, List<Integer> target, int k)
	{
		List<List<Integer>> lst = new ArrayList<List<Integer>>();
		if (k == 0){
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

	private static List<Integer> cloneList(List<Integer> lst)
	{
		List<Integer> result = new ArrayList<Integer>();
		for (Integer i : lst)
			result.add(i);
		return result;
	}
	
	private static List<List<Integer>> getCombinations(int N, int k)
	{
		List<Integer> seq = new ArrayList<Integer>();
		for (int i = 0; i < N; i++) seq.add(i);
	
		return getCombinationsHelper(seq, new ArrayList<Integer>(), k);
	}

	public static void main(String[] args)
	{
		for (List<Integer> lst : getCombinations(3, 2)) {
			System.out.println(">> " + lst);;
		}	
	}

}
