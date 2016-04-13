package org.molgenis.data.annotation.entity.impl.hpo;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public class HPORepository extends AbstractRepository
{
	public static final String HPO_DISEASE_ID_COL_NAME = "diseaseId";
	public static final String HPO_GENE_SYMBOL_COL_NAME = "gene-symbol";
	public static final String HPO_ID_COL_NAME = "HPO-ID";
	public static final String HPO_TERM_COL_NAME = "HPO-term-name";
	private Map<String, List<Entity>> entitiesByGeneSymbol;
	private final File file;

	public HPORepository(File file)
	{
		this.file = file;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		DefaultEntityMetaData entityMeta = new DefaultEntityMetaData("HPO");
		entityMeta.addAttribute(HPO_DISEASE_ID_COL_NAME);
		entityMeta.addAttribute(HPO_GENE_SYMBOL_COL_NAME);
		entityMeta.addAttribute(HPO_ID_COL_NAME, ROLE_ID);
		entityMeta.addAttribute(HPO_TERM_COL_NAME);
		return entityMeta;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return getEntities().iterator();
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		if (q.getRules().isEmpty()) return getEntities().stream();
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
			entitiesByGeneSymbol = new LinkedHashMap<>();

			try (CSVReader csvReader = new CSVReader(
					new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")), '\t',
					CSVParser.DEFAULT_QUOTE_CHARACTER, 1))
			{
				String[] values = csvReader.readNext();
				while (values != null)
				{
					String geneSymbol = values[1];

					Entity entity = new MapEntity(getEntityMetaData());
					entity.set(HPO_DISEASE_ID_COL_NAME, values[0]);
					entity.set(HPO_GENE_SYMBOL_COL_NAME, geneSymbol);
					entity.set(HPO_ID_COL_NAME, values[3]);
					entity.set(HPO_TERM_COL_NAME, values[4]);

					List<Entity> entities = entitiesByGeneSymbol.get(geneSymbol);
					if (entities == null)
					{
						entities = new ArrayList<>();
						entitiesByGeneSymbol.put(geneSymbol, entities);
					}
					entities.add(entity);

					values = csvReader.readNext();
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
