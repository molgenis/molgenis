package org.molgenis.ui;


public interface MolgenisUiMenuItem
{
	String getId();

	String getName();

	MolgenisUiMenuItemType getType();

	MolgenisUiMenu getParentMenu();

	boolean isAuthorized();
}
