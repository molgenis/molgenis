package org.molgenis.ui.style;

import java.util.List;

public interface StyleService
{
	public List<Style> getAvailableStyles();

	public void setSelectedStyle(Style style);

	public Style getSelectedStyle();
}
