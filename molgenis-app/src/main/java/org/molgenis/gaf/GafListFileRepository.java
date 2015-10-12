package org.molgenis.gaf;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.processor.CellProcessor;

/**
 * Google spreadsheet repository that only exposes valid GAF list runs
 */
public class GafListFileRepository extends CsvRepository
{
	private final GafListValidationReport report;

	public GafListFileRepository(File tmpFile, @Nullable List<CellProcessor> cellProcessors, Character separator,
			GafListValidationReport report) throws IOException
	{
		super(tmpFile, null, separator);
		this.report = report;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final Iterator<Entity> it = super.iterator();
		return new Iterator<Entity>()
		{
			private Entity nextEntity = null;

			@Override
			public boolean hasNext()
			{
				if (nextEntity != null)
				{
					return true;
				}
				else
				{
					nextEntity = getNext();
					return nextEntity != null;
				}
			}

			@Override
			public Entity next()
			{
				if (hasNext())
				{
					Entity tmpEntity = nextEntity;
					nextEntity = null;
					return tmpEntity;
				}
				else throw new NoSuchElementException();
			}

			private Entity getNext()
			{
				Entity nextEntity = null;
				if (null != report)
				{
					while (it.hasNext())
					{
						Entity entity = it.next();
						if (null != entity)
						{
							String runId = entity.getString(GAFCol.RUN.toString());
							if (runId != null && !report.hasErrors(runId))
							{
								addBarcodeTypeAndBarcodeToEntity(entity);
								nextEntity = entity;
								break;
							}
						}
					}
				}
				else
				{
					if (it.hasNext())
					{
						Entity entity = it.next();
						addBarcodeTypeAndBarcodeToEntity(entity);
						nextEntity = entity;
					}
				}
				return nextEntity;
			}

			/**
			 * Split the barcodeMenu value into barcodeType and barcode values. Then add the to the entity.
			 * 
			 * @param entity
			 *            sample entity.
			 * 
			 */
			private void addBarcodeTypeAndBarcodeToEntity(Entity entity)
			{
				String barcodeMenu = entity.getString(GAFCol.BARCODE_1.toString());
				String barcodeType = "";
				String barcode = "";

				if (barcodeMenu != null)
				{
					String[] barcodeMenuAsArray = barcodeMenu.split(" ", 3);
					if (barcodeMenuAsArray.length == 3)
					{
						barcodeType = barcodeMenuAsArray[0];
						barcode = barcodeMenuAsArray[2];
					}
				}

				entity.set(GAFCol.BARCODE_TYPE.toString(), barcodeType);
				entity.set(GAFCol.BARCODE.toString(), barcode);
			}

			@Override
			public void remove()
			{
				it.remove();
			}
		};
	}
}
