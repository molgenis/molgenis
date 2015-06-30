package org.molgenis.vortext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents all blocks on the right side opf the screen
 */
public class Marginalia implements Iterable<Marginalis>
{
	private List<Marginalis> marginalia;

	public List<Marginalis> getMarginalia()
	{
		if (marginalia == null)
		{
			marginalia = new ArrayList<Marginalis>();
		}

		return marginalia;
	}

	public void addMarginalis(Marginalis marginalis)
	{
		getMarginalia().add(marginalis);
	}

	public void setMarginalia(List<Marginalis> marginalia)
	{
		this.marginalia = marginalia;
	}

	@Override
	public Iterator<Marginalis> iterator()
	{
		return getMarginalia().iterator();
	}
}
