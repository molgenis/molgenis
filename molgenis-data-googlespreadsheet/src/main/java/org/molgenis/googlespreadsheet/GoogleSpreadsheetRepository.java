package org.molgenis.googlespreadsheet;

import com.google.common.collect.Iterables;
import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DynamicEntity;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.STRING;

public class GoogleSpreadsheetRepository extends AbstractRepository
{
	public enum Visibility
	{
		PUBLIC, PRIVATE;

		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}

	private final SpreadsheetService spreadsheetService;
	private final String spreadsheetKey;
	private final String worksheetId;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;
	private final Visibility visibility;

	private EntityType entityType;

	public GoogleSpreadsheetRepository(SpreadsheetService spreadsheetService, String spreadsheetKey, String worksheetId,
			EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory) throws IOException, ServiceException
	{
		this(spreadsheetService, spreadsheetKey, worksheetId, entityTypeFactory, attrMetaFactory, Visibility.PUBLIC);
	}

	public GoogleSpreadsheetRepository(SpreadsheetService spreadsheetService, String spreadsheetKey, String worksheetId,
			EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory, Visibility visibility)
	{
		this.spreadsheetService = requireNonNull(spreadsheetService);
		this.spreadsheetKey = requireNonNull(spreadsheetKey);
		this.worksheetId = requireNonNull(worksheetId);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.visibility = requireNonNull(visibility);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		if (entityType == null) entityType = getEntityType();

		ListFeed feed;
		try
		{
			feed = spreadsheetService.getFeed(FeedURLFactory.getDefault()
															.getListFeedUrl(spreadsheetKey, worksheetId,
																	visibility.toString(), "full"), ListFeed.class);
		}
		catch (IOException | ServiceException e)
		{
			throw new RuntimeException(e);
		}

		final Iterator<ListEntry> it = feed.getEntries().iterator();
		return new Iterator<Entity>()
		{
			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Entity next()
			{
				Entity entity = new DynamicEntity(getEntityType());
				CustomElementCollection customElements = it.next().getCustomElements();
				for (Attribute attribute : entityType.getAttributes())
				{
					// see remark in getEntityType
					String colName = attribute.getLabel();
					String normalizedColName = colName.replaceAll("_", "").toLowerCase();
					String value = customElements.getValue(normalizedColName);
					entity.set(colName, value);
				}
				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public EntityType getEntityType()
	{
		if (entityType == null)
		{
			// ListFeed does not give you the true column names, use CellFeed instead
			CellFeed feed;
			try
			{
				URL cellFeedUrl = FeedURLFactory.getDefault()
												.getCellFeedUrl(spreadsheetKey, worksheetId, visibility.toString(),
														"full");
				cellFeedUrl = new URL(cellFeedUrl.toString() + "?min-row=1&max-row=1");
				feed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
			}
			catch (IOException | ServiceException e)
			{
				throw new RuntimeException(e);
			}

			EntityType entityType = entityTypeFactory.create(feed.getTitle().getPlainText());

			for (CellEntry cellEntry : feed.getEntries())
			{
				Cell cell = cellEntry.getCell();
				if (cell.getRow() == 1)
				{
					entityType.addAttribute(attrMetaFactory.create().setName(cell.getValue()).setDataType(STRING));
				}
			}
			this.entityType = entityType;
		}

		return entityType;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

	@Override
	public long count()
	{
		return Iterables.size(this);
	}
}
