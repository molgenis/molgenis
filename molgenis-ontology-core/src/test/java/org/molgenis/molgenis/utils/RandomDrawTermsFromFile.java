package org.molgenis.molgenis.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomDrawTermsFromFile
{
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		if (args.length == 2)
		{
			for (String term : RandomDrawTermsFromFile.process(args[0], Integer.parseInt(args[1])))
			{
				System.out.println(term);
			}
		}
	}

	public static Set<String> process(String filePath, int number) throws IOException
	{
		Set<String> listOfTerms = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		try
		{
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null)
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
				listOfTerms.add(line);
			}
		}
		finally
		{
			br.close();
		}

		return randomDraw(listOfTerms, number);
	}

	private static Set<String> randomDraw(Set<String> uniqueSetOfTerms, int number)
	{
		if (number > uniqueSetOfTerms.size()) return Collections.emptySet();

		List<String> wholeListOFTerms = new ArrayList<String>(uniqueSetOfTerms);

		Random random = new Random();

		Set<String> randomListOfTerms = new HashSet<String>();

		while (randomListOfTerms.size() != number)
		{
			randomListOfTerms.add(wholeListOFTerms.get(random.nextInt(wholeListOFTerms.size())));
		}
		return randomListOfTerms;
	}
}