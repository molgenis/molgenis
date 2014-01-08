package org.molgenis.googlespreadsheet;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheetRepository extends AbstractRepository<Entity>
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

	private final ListFeed feed;

	private EntityMetaData entityMetaData;

	public GoogleSpreadsheetRepository(SpreadsheetService spreadsheetService, String spreadsheetKey, String worksheetId)
			throws IOException, ServiceException
	{
		this(spreadsheetService, spreadsheetKey, worksheetId, Visibility.PUBLIC);
	}

	public GoogleSpreadsheetRepository(SpreadsheetService spreadsheetService, String spreadsheetKey,
			String worksheetId, Visibility visibility) throws IOException, ServiceException
	{
		if (spreadsheetService == null) throw new IllegalArgumentException("spreadsheetService is null");
		if (spreadsheetKey == null) throw new IllegalArgumentException("spreadsheetKey is null");
		if (worksheetId == null) throw new IllegalArgumentException("worksheetId is null");
		if (visibility == null) throw new IllegalArgumentException("visibility is null");
		this.feed = spreadsheetService.getFeed(
				FeedURLFactory.getDefault().getListFeedUrl(spreadsheetKey, worksheetId, visibility.toString(), "full"),
				ListFeed.class);
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return MapEntity.class;
	}

	@Override
	public Iterator<Entity> iterator()
	{
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
				MapEntity entity = new MapEntity();
				CustomElementCollection customElements = it.next().getCustomElements();
				for (String colName : customElements.getTags())
				{
					String value = customElements.getValue(colName);
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

	@Override
	public void close() throws IOException
	{
		// noop
	}

	@Override
	protected EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(feed.getTitle().getPlainText());

			CustomElementCollection customElements = feed.getEntries().iterator().next().getCustomElements();
			for (String colName : customElements.getTags())
			{
				((DefaultEntityMetaData) entityMetaData).addAttributeMetaData(new DefaultAttributeMetaData(colName,
						FieldTypeEnum.STRING));
			}
		}

		return entityMetaData;
	}

}
