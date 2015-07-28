package org.molgenis.ui;

public interface MolgenisUiMenuItem
{
	String getId();

	String getName();

	String getUrl();

	MolgenisUiMenuItemType getType();

	MolgenisUiMenu getParentMenu();

	boolean isAuthorized();
}
