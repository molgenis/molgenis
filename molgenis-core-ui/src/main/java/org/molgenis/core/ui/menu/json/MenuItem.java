package org.molgenis.core.ui.menu.json;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MenuItem.class)
public abstract class MenuItem
{
	public static MenuItem create(String id, String label, String href, UiMenuItemType type,
			List<MenuItem> items)
	{
		return new AutoValue_MenuItem(id, label, href, type, items);
	}

	public static MenuItem create(UiMenuItem item)
	{
		List<MenuItem> items = null;
		if (item.getType() == UiMenuItemType.MENU)
		{
			UiMenu menu = (UiMenu) item;
			items = menu.getItems().stream().map(menuItem -> MenuItem.create(menuItem)).collect(Collectors.toList());
		}
		return new AutoValue_MenuItem(item.getId(), item.getName(), item.getUrl(), item.getType(), items);
	}

	abstract String getId();

	abstract String getLabel();

	@Nullable
	abstract String getHref();

	abstract UiMenuItemType getType();

	@Nullable
	abstract List<MenuItem> getItems();
}
