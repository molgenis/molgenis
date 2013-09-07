package org.molgenis.catalog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.molgenis.study.StudyDefinition;

public class CatalogModelBuilder
{
	private CatalogModelBuilder()
	{
	}

	public static CatalogModel create(Catalog catalog)
	{
		return create(catalog, null);
	}

	public static CatalogModel create(Catalog catalog, StudyDefinition studyDefinition)
	{
		return create(catalog, null, false);
	}

	public static CatalogModel create(Catalog catalog, StudyDefinition studyDefinition, boolean selectedOnly)
	{
		CatalogModel catalogModel = new CatalogModel();
		catalogModel.setTitle(catalog.getName());
		catalogModel.setDescription(catalog.getDescription());
		catalogModel.setVersion(catalog.getVersion());
		catalogModel.setAuthors(catalog.getAuthors());

		createCatalogModelRec(catalogModel, catalog, studyDefinition, selectedOnly);
		if (hasDescendantItems(catalogModel))
		{
			sortByName(catalogModel);
		}

		return catalogModel;
	}

	private static void createCatalogModelRec(CatalogModelFolder catalogModelFolder, CatalogFolder catalogFolder,
			StudyDefinition studyDefinition, boolean selectedOnly)
	{
		List<CatalogItem> items = catalogFolder.getItems();
		if (items != null)
		{
			for (CatalogItem item : items)
			{
				boolean selected = studyDefinition != null && studyDefinition.containsItem(item);
				if (!selectedOnly || (selectedOnly && selected))
				{
					catalogModelFolder.addItem(new CatalogModelItem(item.getId(), item.getName(), selected));
				}
			}
		}

		List<CatalogFolder> children = catalogFolder.getChildren();
		if (children != null)
		{
			for (CatalogFolder childFolder : children)
			{
				CatalogModelFolder childCatalogModelFolder = new CatalogModelFolder();
				createCatalogModelRec(childCatalogModelFolder, childFolder, studyDefinition, selectedOnly);
				if (!selectedOnly || (selectedOnly && hasDescendantItems(childCatalogModelFolder)))
				{
					catalogModelFolder.addChild(childCatalogModelFolder);
				}
			}
		}

		catalogModelFolder.setId(catalogFolder.getId());
		catalogModelFolder.setName(catalogFolder.getName());
		catalogModelFolder.setSelected(false);
	}

	private static boolean hasDescendantItems(CatalogModelFolder catalogModelFolder)
	{
		List<CatalogModelItem> items = catalogModelFolder.getItems();
		if (items != null && !items.isEmpty()) return true;

		List<CatalogModelFolder> children = catalogModelFolder.getChildren();
		if (children != null)
		{
			for (CatalogModelFolder child : children)
				if (hasDescendantItems(child)) return true;
		}
		return false;
	}

	private static void sortByName(CatalogModelFolder catalogModelFolder)
	{
		List<CatalogModelFolder> children = catalogModelFolder.getChildren();
		if (children != null && children.size() > 1)
		{
			Collections.sort(children, new Comparator<CatalogModelFolder>()
			{
				@Override
				public int compare(CatalogModelFolder node1, CatalogModelFolder node2)
				{
					return node1.getName().compareTo(node2.getName());
				}
			});
			for (CatalogModelFolder child : children)
			{
				sortByName(child);
			}
		}

		List<CatalogModelItem> items = catalogModelFolder.getItems();
		if (items != null && items.size() > 1)
		{
			Collections.sort(items, new Comparator<CatalogModelItem>()
			{
				@Override
				public int compare(CatalogModelItem item1, CatalogModelItem item2)
				{
					return item1.getName().compareTo(item2.getName());
				}
			});
		}
	}
}
