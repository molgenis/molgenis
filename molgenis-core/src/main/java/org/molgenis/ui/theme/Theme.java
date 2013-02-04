package org.molgenis.ui.theme;

import org.molgenis.ui.MolgenisComponent;

public interface Theme
{
	public <E extends MolgenisComponent> String render(E e) throws RenderException;
}
