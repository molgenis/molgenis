package org.molgenis.web;

public interface UiMenuItem
{
	String getId();

	String getName();

	String getUrl();

	UiMenuItemType getType();

	UiMenu getParentMenu();

	boolean isAuthorized();
}
