package org.molgenis.data.annotation.core.entity.impl.hpo;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DynamicEntity;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;

public class HPORepository extends AbstractRepository
{
	public static final String HPO_DISEASE_ID_COL_NAME = "diseaseId";
	public static final String HPO_GENE_SYMBOL_COL_NAME = "gene-symbol";
	public static final String HPO_ID_COL_NAME = "HPO-ID";
	public static final String HPO_TERM_COL_NAME = "HPO-term-name";
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attributeFactory;
	private Map<String, List<Entity>> entitiesByGeneSymbol;
	private final File file;

	public HPORepository(File file, EntityTypeFactory entityTypeFactory, AttributeFactory attributeFactory)
	{
		this.file = file;
		this.entityTypeFactory = entityTypeFactory;
		this.attributeFactory = attributeFactory;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public EntityType getEntityType()
	{
		EntityType entityType = entityTypeFactory.create("HPO");
		entityType.addAttribute(attributeFactory.create().setName(HPO_DISEASE_ID_COL_NAME));
		entityType.addAttribute(attributeFactory.create().setName(HPO_GENE_SYMBOL_COL_NAME));
		entityType.addAttribute(attributeFactory.create().setName(HPO_ID_COL_NAME), ROLE_ID);
		entityType.addAttribute(attributeFactory.create().setName(HPO_TERM_COL_NAME));
		return entityType;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return getEntities().iterator();
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
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

			try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(file), UTF_8), '\t',
					CSVParser.DEFAULT_QUOTE_CHARACTER, 1))
			{
				String[] values = csvReader.readNext();
				while (values != null)
				{
					String geneSymbol = values[1];

					Entity entity = new DynamicEntity(getEntityType());
					entity.set(HPO_DISEASE_ID_COL_NAME, values[0]);
					entity.set(HPO_GENE_SYMBOL_COL_NAME, geneSymbol);
					entity.set(HPO_ID_COL_NAME, values[3]);
					entity.set(HPO_TERM_COL_NAME, values[4]);

					List<Entity> entities = entitiesByGeneSymbol.computeIfAbsent(geneSymbol, k -> new ArrayList<>());
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
