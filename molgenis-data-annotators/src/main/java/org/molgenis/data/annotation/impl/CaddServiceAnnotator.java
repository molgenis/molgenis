package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class performs a system call to cross reference a chromosome and genomic location with a tabix indexed file. A
 * match can result in 1, 2 or 3 hits. These matches are reduced to one based on a reference and alternative nucleotide
 * base. The remaining hit will be used to parse two CADD scores.
 * </p>
 * 
 * <p>
 * <b>CADD returns:</b> CADD score Absolute, CADD score Scaled
 * </p>
 * 
 * @author mdehaan
 * 
 * */
@Component("caddService")
public class CaddServiceAnnotator extends VariantAnnotator
{
	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	DataService dataService;

	@Autowired
	AnnotationService annotatorService;

	// the cadd service returns these two values
	static final String CADD_SCALED = "CADD_SCALED";
	static final String CADD_ABS = "CADD_ABS";

	public static final String TABIX_LOCATION_PROPERTY = "tabix_location";
	public static final String CADD_FILE_LOCATION_PROPERTY = "cadd_location";

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getName()
	{
		return "CADD";
	}

	private String getFileLocation()
	{
		return molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY);
	}

	private String getToolLocation()
	{
		return molgenisSettings.getProperty(TABIX_LOCATION_PROPERTY);
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_ABS, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_SCALED, FieldTypeEnum.DECIMAL));

		return metadata;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		List<Entity> results = new ArrayList<Entity>();
		String caddFile = getFileLocation();
		String tabix = getToolLocation();

		BufferedReader bufferedReader = null;

		String chromosome = entity.getString(CHROMOSOME);
		Long position = entity.getLong(POSITION);
		String reference = entity.getString(REFERENCE);
		String alternative = entity.getString(ALTERNATIVE);

		String caddAbs = "";
		String caddScaled = "";

		try
		{
			Runtime runTime = Runtime.getRuntime();
			Process process = runTime.exec(tabix + " " + caddFile + " " + chromosome + ":" + position + "-" + position);

			process.waitFor();

			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line = "";
			String[] split = null;

			while ((line = bufferedReader.readLine()) != null)
			{
				if (!line.equals(null))
				{
					split = line.split("\t");

					if (split[2].equals(reference) && split[3].equals(alternative))
					{
						caddAbs = split[4];
						caddScaled = split[5];
					}
				}
			}

			HashMap<String, Object> resultMap = new HashMap<String, Object>();

			resultMap.put(CADD_ABS, caddAbs);
			resultMap.put(CADD_SCALED, caddScaled);
			resultMap.put(CHROMOSOME, chromosome);
			resultMap.put(POSITION, position);
			resultMap.put(ALTERNATIVE, alternative);
			resultMap.put(REFERENCE, reference);

			results.add(new MapEntity(resultMap));

		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				bufferedReader.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		return results;
	}
}
