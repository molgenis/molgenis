package org.molgenis.standardsregistry.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.springframework.util.ResourceUtils;

public class ChadoModelMaker
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			new ChadoModelMaker().makeModelFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void makeModelFile() throws InvalidFormatException, IOException
	{
		File chadoModelFile = ResourceUtils.getFile("src/test/resources/Chado-Model.xlsx");

		ExcelRepositoryCollection modelFile = new ExcelRepositoryCollection(chadoModelFile);
		Writable entities = modelFile.createWritable("entities", Arrays.asList("name", "package", "description"));
		Writable attributes = modelFile.createWritable("attributes", Arrays.asList("name", "entity", "dataType",
				"refEntity", "nillable", "idAttribute", "description", "unique"));

		Document doc = Jsoup.connect("http://gmod.org/wiki/Chado_Tables").get();

		for (Element table : doc.select("table"))
		{
			String caption = table.select("caption").text();
			if (caption.contains("Constraints")) continue;

			String entityName = caption.substring(0, caption.length() - " Structure".length());
			String entityDescription = table.previousElementSibling().text();

			System.out.println("Writing " + entityName);
			Entity entity = new MapEntity();
			entity.set("name", entityName);
			entity.set("package", "Chado");
			entity.set("description", entityDescription);
			entities.add(entity);

			for (Element tr : table.select("tr:not(thead tr)"))
			{
				Elements cells = tr.select("td");
				String dataType = "string";
				String refEntity = null;

				String fk = cells.get(0).text().trim();
				if (StringUtils.isNotBlank(fk))
				{
					dataType = "xref";
					refEntity = fk;
				}

				String name = cells.get(1).text().trim();
				if (!dataType.equalsIgnoreCase("xref"))
				{
					String type = cells.get(2).text().trim();
					if (type.equalsIgnoreCase("serial")) dataType = "int";
					if (type.equalsIgnoreCase("text")) dataType = "text";
					if (type.equalsIgnoreCase("integer")) dataType = "int";
					if (type.equalsIgnoreCase("date")) dataType = "datetime";
					if (type.equalsIgnoreCase("boolean")) dataType = "bool";
					if (type.equalsIgnoreCase("smallint")) dataType = "int";
					if (type.contains("timestamp")) dataType = "datetime";
					if (type.contains("double")) dataType = "decimal";
				}

				boolean nillable = true;
				boolean unique = false;
				boolean idAttribute = false;
				String attrDescription = cells.get(3).text().trim();
				if (attrDescription.contains("PRIMARY KEY"))
				{
					idAttribute = true;
					nillable = false;
					unique = true;
				}
				else
				{
					if (attrDescription.contains("UNIQUE") && !attrDescription.contains("UNIQUE#")) unique = true;
					if (attrDescription.contains("NOT NULL")) nillable = false;
				}

				if (unique && dataType.equalsIgnoreCase("text")) dataType = "string";// Mysql cannot have TEXT and
																						// unique

				Entity attr = new MapEntity();
				attr.set("entity", entityName);
				attr.set("name", name);
				attr.set("dataType", dataType);
				attr.set("refEntity", refEntity);
				attr.set("nillable", nillable);
				attr.set("unique", unique);
				attr.set("idAttribute", idAttribute);
				attr.set("description", attrDescription);

				attributes.add(attr);
			}

		}

		modelFile.save(new FileOutputStream(chadoModelFile));
	}
}
