package org.molgenis.ui.menu.json;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.ui.MolgenisUiMenuItemType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MenuItem.class)
public abstract class MenuItem
{
	public static MenuItem create(String id, String label, String href, MolgenisUiMenuItemType type,
			List<MenuItem> items)
	{
		return new AutoValue_MenuItem(id, label, href, type, items);
	}

	public static MenuItem create(MolgenisUiMenuItem item)
	{
		List<MenuItem> items = null;
		if (item.getType() == MolgenisUiMenuItemType.MENU)
		{
			MolgenisUiMenu menu = (MolgenisUiMenu) item;
			items = menu.getItems().stream().map(menuItem -> MenuItem.create(menuItem)).collect(Collectors.toList());
		}
		return new AutoValue_MenuItem(item.getId(), item.getName(), item.getUrl(), item.getType(), items);
	}

	abstract String getId();

	abstract String getLabel();

	@Nullable
	abstract String getHref();

	abstract MolgenisUiMenuItemType getType();

	@Nullable
	abstract List<MenuItem> getItems();
}
