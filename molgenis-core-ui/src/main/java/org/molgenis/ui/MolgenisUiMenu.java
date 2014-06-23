package org.molgenis.ui;

import java.util.List;

public interface MolgenisUiMenu extends MolgenisUiMenuItem
{
	List<MolgenisUiMenuItem> getItems();

	boolean containsItem(String itemId);

	MolgenisUiMenuItem getActiveItem();

	List<MolgenisUiMenu> getBreadcrumb();
}
