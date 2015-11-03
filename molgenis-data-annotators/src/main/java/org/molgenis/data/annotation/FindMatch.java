package org.molgenis.data.annotation;

import java.util.List;

public class FindMatch
{
	public static boolean match(String ref1, String ref2, String alt1, List<String> alts2)
	{
		if (ref1.equals(ref2) && alts2.contains(alt1))
		{
            return true;
		}
		else if (ref1.indexOf(ref2)==0)//must start with same sequence, otherwise we have a position shift
		{
			String postFix = ref1.substring(ref2.length());
			for (String alt : alts2)
			{
				if ((alt + postFix).equals(alt1))
				{
					return true;
				}
			}
			return false;
		}
		else if (ref2.indexOf(ref1) == 0)//must start with same sequence, otherwise we have a position shift
		{
			String postFix = ref2.substring(ref1.length());
            for (String alt : alts2)
			{
				if (alt.equals(alt1 + postFix))
				{
					return true;
				}
			}
			return false;
		}
		return false;
	}
}
