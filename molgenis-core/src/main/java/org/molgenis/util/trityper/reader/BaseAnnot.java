package org.molgenis.util.trityper.reader;

/**
 * 
 * @author harmjan
 */
public class BaseAnnot
{
	public static final byte A = 65;
	public static final byte C = 67;
	public static final byte T = 84;
	public static final byte G = 71;
	public static final byte U = 85;
	public static final byte N = 0;

	public static String toString(byte x)
	{
		if (x == A)
		{
			return "A";
		}
		if (x == T)
		{
			return "T";
		}
		if (x == U)
		{
			return "U";
		}
		if (x == C)
		{
			return "C";
		}
		if (x == G)
		{
			return "G";
		}
		return null;
	}

	public static byte getComplement(byte x)
	{
		if (x == A)
		{
			return T;
		}
		if (x == T)
		{
			return A;
		}
		if (x == U)
		{
			return A;
		}
		if (x == C)
		{
			return G;
		}
		if (x == G)
		{
			return C;
		}
		return N;
	}

	public static String getComplement(String x)
	{
		if (x.equals("A"))
		{
			return "T";
		}
		if (x.equals("T"))
		{
			return "A";
		}
		if (x.equals("U"))
		{
			return "A";
		}
		if (x.equals("C"))
		{
			return "G";
		}
		if (x.equals("G"))
		{
			return "C";
		}
		return "N";
	}
}
