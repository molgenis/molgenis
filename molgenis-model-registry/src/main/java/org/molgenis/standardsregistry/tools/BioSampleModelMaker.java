package org.molgenis.standardsregistry.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.Writable;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.support.MapEntity;
import org.springframework.util.ResourceUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class BioSampleModelMaker
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			new BioSampleModelMaker().makeModelFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void makeModelFile() throws InvalidFormatException, IOException
	{
		File biosampleFile = ResourceUtils.getFile("src/test/resources/BioSample-Model.xlsx");

		ExcelRepositoryCollection modelFile = new ExcelRepositoryCollection(biosampleFile);
		Repository entities = modelFile.getRepositoryByEntityName("entities");
		Writable attributes = modelFile.createWritable("attributes", Arrays.asList("entity", "name", "dataType",
				"refEntity", "nillable", "idAttribute", "description", "unique"));

		Set<String> refEntities = Sets.newHashSet();
		for (Entity entity : entities)
		{
			String entityName = entity.getString("name");
			createAttributes(entityName, attributes, modelFile, refEntities);
		}

		modelFile.save(new FileOutputStream(biosampleFile));
		System.out.println("Ref entities:\n" + Joiner.on("\n").join(refEntities));
	}

	private void createAttributes(String entityName, Writable attributes, ExcelRepositoryCollection modelFile,
			Set<String> refEntities) throws IOException
	{
		System.out.println("Writing attributes of entity [" + entityName + "]");

		String url = String.format("https://submit.ncbi.nlm.nih.gov/biosample/template/?package=%s&action=definition",
				entityName.replaceAll("_", "."));

		Document doc = Jsoup.connect(url).get();
		Elements attributeRows = doc.select(".zebra tbody tr");

		for (Element attributeRow : attributeRows)
		{
			String attributeName = attributeRow.select(".attr-name span").get(0).text();
			boolean nillable = attributeRow.select(".attr-name .req").isEmpty();
			boolean idAttribute = attributeName.equalsIgnoreCase("sample_name");
			String description = attributeRow.select("td").get(1).text();
			String valueFormat = attributeRow.select("td").get(2).text();

			String dataType = "string";
			if (attributeName.equalsIgnoreCase("description")) dataType = "text";
			if (attributeName.endsWith("date")) dataType = "datetime";

			String refEntity = null;
			if (StringUtils.isNotBlank(valueFormat))
			{
				description = String.format("%s. Value format: %s", description, valueFormat);
				if (valueFormat.contains(" | "))
				{
					if (!refEntities.contains(attributeName))
					{
						refEntities.add(attributeName);
						Writable w = modelFile.createWritable(attributeName, Arrays.asList("name"));
						for (String value : valueFormat.split("[|]"))
						{
							w.add(new MapEntity("name", value.trim()));
						}
					}

					refEntity = attributeName;
					dataType = "categorical";
				}
			}

			Entity entity = new MapEntity();
			entity.set("entity", entityName);
			entity.set("name", attributeName);
			entity.set("dataType", dataType);
			entity.set("refEntity", refEntity);
			entity.set("nillable", nillable);
			entity.set("idAttribute", idAttribute);
			entity.set("description", description);
			entity.set("unique", idAttribute);
			attributes.add(entity);
		}
	}
}
