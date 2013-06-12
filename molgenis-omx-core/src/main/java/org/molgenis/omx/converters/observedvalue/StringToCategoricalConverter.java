package org.molgenis.omx.converters.observedvalue;

import static org.molgenis.framework.db.QueryRule.Operator.AND;
import static org.molgenis.framework.db.QueryRule.Operator.EQUALS;

import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;

public class StringToCategoricalConverter implements StringConverter<String>
{
	private static final Logger LOG = Logger.getLogger(StringToCategoricalConverter.class);

	@Override
	public String fromString(String value, Database db, ObservableFeature feature)
	{
		if (value == null)
		{
			return null;
		}

		try
		{
			List<Category> categories = db.find(Category.class, new QueryRule(Category.VALUECODE, EQUALS, value),
					new QueryRule(AND),
					new QueryRule(Category.OBSERVABLEFEATURE_IDENTIFIER, EQUALS, feature.getIdentifier()));

			if (categories.isEmpty())
			{
				LOG.warn("Value [" + value + "] is not a valid valueCode for ObservableFeature [" + feature.getName()
						+ "]");
				return value;
			}

			return categories.get(0).getName();
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException("DatabaseException finding category valueCode [" + value
					+ "] for ObservableFeature [" + feature.getName() + "]", e);
		}
	}
}
