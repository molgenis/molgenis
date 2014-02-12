package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.codehaus.jackson.map.ObjectMapper;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class performs a system call to cross reference a chromosome 
 * and genomic location with a tabix indexed file. A match can result in 1, 2 or 3 hits.
 * These matches are reduced to one based on a reference and alternative nucleotide base.
 * The remaining hit will be used to parse two CADD scores.
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
public class CaddServiceAnnotator implements RepositoryAnnotator
{
	// the cadd service is dependant on these four values,
	// without them no CADD score can be returned
	private static final String CHROMOSOME = "chrom";
	private static final String POSITION = "pos";
	private static final String REFERENCE = "ref";
	private static final String ALTERNATIVE = "alt";

	// the cadd service returns these two values
	private static final String CADD_SCALED = "CADD_SCALED";
	private static final String CADD_ABS = "CADD_ABS";

	@Autowired
	DataService dataService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Override
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		List<Entity> results = new ArrayList<Entity>();
		String systemCall = molgenisSettings.getProperty("CADD_Command");
		
		while (source.hasNext())
		{
			Entity entity = source.next();

			String chromosome = entity.get(CHROMOSOME).toString();
			String position = entity.get(POSITION).toString();
			String reference = entity.get(REFERENCE).toString();
			String alternative = entity.get(ALTERNATIVE).toString();

			String caddAbs = "";
			String caddScaled = "";
			
			try
			{
				Runtime runTime = Runtime.getRuntime();
				Process process = runTime.exec(systemCall + chromosome + ":" + position + "-" + position);

				process.waitFor();

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

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
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

		}

		return results.iterator();
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		String[] caddFeatures = new String[]
		{ CADD_ABS, CADD_SCALED };

		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

		for (String attribute : caddFeatures)
		{
			metadata.addAttributeMetaData(new DefaultAttributeMetaData(attribute, FieldTypeEnum.STRING));
		}

		return metadata;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHROMOSOME, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POSITION, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REFERENCE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALTERNATIVE, FieldTypeEnum.STRING));

		return metadata;
	}

	@Override
	public Boolean canAnnotate(EntityMetaData inputMetaData)
	{
		boolean canAnnotate = true;
		Iterable<AttributeMetaData> inputAttributes = getInputMetaData().getAttributes();
		for(AttributeMetaData attribute : inputAttributes){
			if(inputMetaData.getAttribute(attribute.getName()) == null){
				//all attributes from the inputmetadata must be present to annotate.
				canAnnotate = false;
			}
		}
		return canAnnotate;
	}

	@Override
	public String getName()
	{
		return "CADD";
	}

}
