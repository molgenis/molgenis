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
		else if (ref1.contains(ref2))
		{
			int start = ref1.indexOf(ref2);
			int stop = start + ref2.length();
			String prefix = ref1.substring(0, start);
			String postFix = ref1.substring(stop);
			for (String alt : alts2)
			{
				if ((prefix + alt + postFix).equals(alt1))
				{
					return true;
				}
			}
			return false;
		}
		else if (ref2.contains(ref1))
		{
			int start = ref2.indexOf(ref1);
			int stop = start + ref1.length();
			String prefix = ref2.substring(0, start);
			String postFix = ref2.substring(stop);
            for (String alt : alts2)
			{
				if (alt.equals(prefix + alt1 + postFix))
				{
					return true;
				}
			}
			return false;
		}
		return false;
	}
}
