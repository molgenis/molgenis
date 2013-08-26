package org.molgenis.ui;

import java.util.List;

public interface MolgenisUiMenu extends MolgenisUiMenuItem
{
	List<MolgenisUiMenuItem> getItems();

	MolgenisUiMenuItem getActiveItem();
}
