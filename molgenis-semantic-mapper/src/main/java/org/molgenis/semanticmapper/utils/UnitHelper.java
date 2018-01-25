package org.molgenis.semanticmapper.utils;

public class UnitHelper
{
	public static String superscriptToNumber(String str)
	{
		str = str.replaceAll("⁰", "0");
		str = str.replaceAll("¹", "1");
		str = str.replaceAll("²", "2");
		str = str.replaceAll("³", "3");
		str = str.replaceAll("⁴", "4");
		str = str.replaceAll("⁵", "5");
		str = str.replaceAll("⁶", "6");
		str = str.replaceAll("⁷", "7");
		str = str.replaceAll("⁸", "8");
		str = str.replaceAll("⁹", "9");
		return str;
	}

	public static String numberToSuperscript(String str)
	{
		str = str.replaceAll("0", "⁰");
		str = str.replaceAll("1", "¹");
		str = str.replaceAll("2", "²");
		str = str.replaceAll("3", "³");
		str = str.replaceAll("4", "⁴");
		str = str.replaceAll("5", "⁵");
		str = str.replaceAll("6", "⁶");
		str = str.replaceAll("7", "⁷");
		str = str.replaceAll("8", "⁸");
		str = str.replaceAll("9", "⁹");
		return str;
	}
}
