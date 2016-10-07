package org.molgenis.data.semanticsearch.utils;

import java.text.DecimalFormat;

public class NumberToEnglishWords
{
	private static final String[] TEN_NAMES = { "", " ten", " twenty", " thirty", " forty", " fifty", " sixty",
			" seventy", " eighty", " ninety" };

	private static final String[] NUM_NAMES = { "", " one", " two", " three", " four", " five", " six", " seven",
			" eight", " nine", " ten", " eleven", " twelve", " thirteen", " fourteen", " fifteen", " sixteen",
			" seventeen", " eighteen", " nineteen" };

	private static String convertLessThanOneThousand(int number)
	{
		String soFar;

		if (number % 100 < 20)
		{
			soFar = NUM_NAMES[number % 100];
			number /= 100;
		}
		else
		{
			soFar = NUM_NAMES[number % 10];
			number /= 10;

			soFar = TEN_NAMES[number % 10] + soFar;
			number /= 10;
		}
		if (number == 0) return soFar;
		return NUM_NAMES[number] + " hundred" + soFar;
	}

	public static String convert(long number)
	{
		// 0 to 999 999 999 999
		if (number == 0)
		{
			return "zero";
		}

		String snumber = Long.toString(number);

		// pad with "0"
		String mask = "000000000000";
		DecimalFormat df = new DecimalFormat(mask);
		snumber = df.format(number);

		// XXXnnnnnnnnn
		int billions = Integer.parseInt(snumber.substring(0, 3));
		// nnnXXXnnnnnn
		int millions = Integer.parseInt(snumber.substring(3, 6));
		// nnnnnnXXXnnn
		int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
		// nnnnnnnnnXXX
		int thousands = Integer.parseInt(snumber.substring(9, 12));

		String tradBillions;
		switch (billions)
		{
			case 0:
				tradBillions = "";
				break;
			case 1:
				tradBillions = convertLessThanOneThousand(billions) + " billion ";
				break;
			default:
				tradBillions = convertLessThanOneThousand(billions) + " billion ";
		}
		String result = tradBillions;

		String tradMillions;
		switch (millions)
		{
			case 0:
				tradMillions = "";
				break;
			case 1:
				tradMillions = convertLessThanOneThousand(millions) + " million ";
				break;
			default:
				tradMillions = convertLessThanOneThousand(millions) + " million ";
		}
		result = result + tradMillions;

		String tradHundredThousands;
		switch (hundredThousands)
		{
			case 0:
				tradHundredThousands = "";
				break;
			case 1:
				tradHundredThousands = "one thousand ";
				break;
			default:
				tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " thousand ";
		}
		result = result + tradHundredThousands;

		String tradThousand;
		tradThousand = convertLessThanOneThousand(thousands);
		result = result + tradThousand;

		// remove extra spaces!
		return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
	}
}
