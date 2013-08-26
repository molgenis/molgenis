package org.molgenis.ui;

public interface MolgenisUiMenuItem
{
	String getId();

	String getName();

	MolgenisUiMenuItemType getType();

	boolean isAuthorized();
}
