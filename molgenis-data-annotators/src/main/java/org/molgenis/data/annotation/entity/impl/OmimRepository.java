package org.molgenis.data.annotation.entity.impl;

import static au.com.bytecode.opencsv.CSVParser.DEFAULT_QUOTE_CHARACTER;
import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;
import static java.nio.charset.Charset.forName;
import static java.util.Collections.emptySet;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.annotation.entity.impl.OmimAnnotator.SEPARATOR;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.common.collect.Iterables;

import au.com.bytecode.opencsv.CSVReader;

public class OmimRepository extends AbstractRepository
{
	public static final String OMIM_AUTO_ID_COL_NAME = "ID";
	public static final String OMIM_PHENOTYPE_COL_NAME = "Phenotype";
	public static final String OMIM_GENE_SYMBOLS_COL_NAME = "Gene_Name";
	public static final String OMIM_MIM_NUMBER_COL_NAME = "MIMNumber";
	public static final String OMIM_CYTO_LOCATION_COL_NAME = "CytoLocation";

	private Map<String, List<Entity>> entitiesByGeneSymbol;

	private final File file;

	public OmimRepository(File file)
	{
		this.file = file;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return emptySet();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(OmimAnnotator.NAME);
		entityMetaData.addAttribute(OMIM_GENE_SYMBOLS_COL_NAME, ROLE_ID);
		entityMetaData.addAttribute(OMIM_PHENOTYPE_COL_NAME);
		entityMetaData.addAttribute(OMIM_MIM_NUMBER_COL_NAME);
		entityMetaData.addAttribute(OMIM_CYTO_LOCATION_COL_NAME);
		return entityMetaData;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return getEntities().iterator();
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		if (q.getRules().isEmpty())
		{
			return getEntities().stream();
		}

		if ((q.getRules().size() != 1) || (q.getRules().get(0).getOperator() != Operator.EQUALS))
		{
			throw new MolgenisDataException("The only query allowed on this Repository is gene EQUALS");
		}

		String geneSymbol = (String) q.getRules().get(0).getValue();
		List<Entity> entities = getEntitiesByGeneSymbol().get(geneSymbol);

		return entities != null ? entities.stream() : Stream.empty();
	}

	@Override
	public long count()
	{
		return Iterables.size(this);
	}

	private List<Entity> getEntities()
	{
		List<Entity> entities = new ArrayList<>();
		getEntitiesByGeneSymbol().forEach((geneSymbol, geneSymbolEntities) -> entities.addAll(geneSymbolEntities));
		return entities;
	}

	private Map<String, List<Entity>> getEntitiesByGeneSymbol()
	{
		if (entitiesByGeneSymbol == null)
		{
			Map<String, List<List<String>>> omimEntriesByGeneSymbol = new HashMap<>();
			entitiesByGeneSymbol = new LinkedHashMap<>();

			try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(file), forName("UTF-8")),
					SEPARATOR, DEFAULT_QUOTE_CHARACTER, 1))
			{

				String[] values = csvReader.readNext();
				while (values != null)
				{
					String geneSymbols = values[1];
					String phenotype = values[0];
					String mim_number = values[2];
					String cyto_location = values[3];

					String[] separateGeneSymbols = geneSymbols.split(", ");

					for (String geneSymbol : separateGeneSymbols)
					{
						if (omimEntriesByGeneSymbol.containsKey(geneSymbol))
						{
							omimEntriesByGeneSymbol.get(geneSymbol).get(0).add(phenotype); // first list is phenoype
							omimEntriesByGeneSymbol.get(geneSymbol).get(1).add(mim_number); // second is mim number
							omimEntriesByGeneSymbol.get(geneSymbol).get(2).add(cyto_location); // third is cyto location
						}
						else
						{
							LinkedList<List<String>> mapList = new LinkedList<>();
							mapList.add(newArrayList(phenotype));
							mapList.add(newArrayList(mim_number));
							mapList.add(newArrayList(cyto_location));
							omimEntriesByGeneSymbol.put(geneSymbol, mapList);
						}
					}
					values = csvReader.readNext();
				}

				for (String geneSymbol : omimEntriesByGeneSymbol.keySet())
				{
					Entity entity = new MapEntity(getEntityMetaData());
					entity.set(OMIM_GENE_SYMBOLS_COL_NAME, geneSymbol);
					entity.set(OMIM_PHENOTYPE_COL_NAME, omimEntriesByGeneSymbol.get(geneSymbol).get(0));
					entity.set(OMIM_MIM_NUMBER_COL_NAME, omimEntriesByGeneSymbol.get(geneSymbol).get(1));
					entity.set(OMIM_CYTO_LOCATION_COL_NAME, omimEntriesByGeneSymbol.get(geneSymbol).get(2));

					List<Entity> entities = entitiesByGeneSymbol.get(geneSymbol);
					if (entities == null)
					{
						entities = new ArrayList<>();
						entitiesByGeneSymbol.put(geneSymbol, entities);
					}
					entities.add(entity);
				}

			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		}
		return entitiesByGeneSymbol;
	}
}
