package org.molgenis.util;

/**
 * Helper functions for HTML interaction
 * 
 * @author joerivandervelde
 * 
 */
public class HtmlTools
{

	/**
	 * Helper function to convert any string into URL-safe encoding.
	 * 
	 * @param input
	 * @return
	 */
	public static String toSafeUrlString(String input)
	{
		if (input.length() == 0)
		{
			return "";
		}
		StringBuilder encBuilder = new StringBuilder();
		for (char c : input.toCharArray())
		{
			encBuilder.append((int) c).append('.');
		}
		return encBuilder.substring(0, encBuilder.length() - 1);
	}

	/**
	 * Helper function to convert any string into URL-safe encoding. Output
	 * string is less easy to translate back to the original.
	 * 
	 * @param input
	 * @return
	 */
	public static String toSafeUrlStringO_b_f(String input)
	{
		if (input.length() == 0)
		{
			return "";
		}
		StringBuilder encBuilder = new StringBuilder();
		for (char c : input.toCharArray())
		{
			encBuilder.append((int) (Math.pow((c), 2) - Math.pow(10, 4))).append('.');
		}
		return encBuilder.substring(0, encBuilder.length() - 1);
	}

	/**
	 * Helper function to convert an URL-safe string (passed from eg. a REST
	 * interface) back to the original string. Input string passes an extra
	 * translation step.
	 * 
	 * @param input
	 * @return
	 */
	public static String fromSafeUrlStringO_b_f(String input)
	{
		StringBuilder decBuilder = new StringBuilder();
		for (String nr : input.split("\\."))
		{
			int i = (int) Math.sqrt(Integer.parseInt(nr) + Math.pow(10, 4));
			char c = (char) i;
			decBuilder.append(c);
		}
		return decBuilder.toString();
	}

	/**
	 * Helper function to convert any string into URL-safe encoding.
	 * 
	 * @param input
	 * @return
	 */
	public static String toSafeUrlStringObv(String input)
	{
		if (input.length() == 0)
		{
			return "";
		}
		StringBuilder encBuilder = new StringBuilder();
		for (char c : input.toCharArray())
		{
			encBuilder.append((int) (Math.pow((c), 2) - 4321)).append('.');
		}
		return encBuilder.substring(0, encBuilder.length() - 1);
	}

	/**
	 * Helper function to convert an URL-safe string (passed from eg. a REST
	 * interface) back to the original string.
	 * 
	 * @param input
	 * @return
	 */
	public static String fromSafeUrlString(String input)
	{
		StringBuilder decBuilder = new StringBuilder();
		for (String nr : input.split("\\."))
		{
			int i = Integer.parseInt(nr);
			char c = (char) i;
			decBuilder.append(c);
		}
		return decBuilder.toString();
	}

	/**
	 * Example for toSafeUrlString() and fromSafeUrlString():
	 * 
	 * String example = "abcabc!@#$%^&*(){}:,./;|'\"<>"; String enc =
	 * HtmlTools.toSafeUrlString(example); System.out.println(enc); if
	 * (example.equals(HtmlTools.fromSafeUrlString(enc))) {
	 * System.out.println("success"); }
	 */

}
