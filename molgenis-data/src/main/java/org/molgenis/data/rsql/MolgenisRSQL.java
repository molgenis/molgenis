package org.molgenis.data.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.Node;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityMetaData;
import org.springframework.stereotype.Service;

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
	private final RSQLParser rsqlParser = new RSQLParser();

	public Query<Entity> createQuery(String rsql, EntityMetaData entityMetaData) throws RSQLParserException
	{
		Node rootNode = rsqlParser.parse(rsql);
		MolgenisRSQLVisitor visitor = new MolgenisRSQLVisitor(entityMetaData);

		return rootNode.accept(visitor);
	}
}
