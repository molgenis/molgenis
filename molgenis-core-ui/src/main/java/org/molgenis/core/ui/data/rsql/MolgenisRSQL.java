package org.molgenis.core.ui.data.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

/**
 * Rsql/fiql parser
 * <p>
 * Creates a Query object from a rsql/fiql query string.
 *
 * @see <a href="https://github.com/jirutka/rsql-parser">https://github.com/jirutka/rsql-parser</a>
 */
@Service
public class MolgenisRSQL
{
	private final RSQLParser rsqlParser;

	public MolgenisRSQL(RSQLParser rsqlParser)
	{
		this.rsqlParser = requireNonNull(rsqlParser);
	}

	public Query<Entity> createQuery(String rsql, EntityType entityType)
	{
		Node rootNode = rsqlParser.parse(rsql);
		MolgenisRSQLVisitor visitor = new MolgenisRSQLVisitor(entityType);

		return rootNode.accept(visitor);
	}
}
