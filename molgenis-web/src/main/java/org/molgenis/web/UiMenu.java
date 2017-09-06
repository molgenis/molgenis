package org.molgenis.web;

import java.util.List;

public interface UiMenu extends UiMenuItem
{
	List<UiMenuItem> getItems(); // FIXME iterable instead of list

	boolean containsItem(String itemId);

	UiMenuItem getActiveItem();

	List<UiMenu> getBreadcrumb();
}
