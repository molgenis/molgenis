package org.molgenis.annotationServices;

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
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class...
 * </p>
 * 
 * <p>
 * CADD returns: CADD score Absolute, CADD score Scaled
 * </p>
 * 
 * @author mdehaan
 * 
 * */
@Component("caddService")
public class CaddServiceAnnotator implements RepositoryAnnotator
{
	// the cadd service is dependant on these three values,
	// without them no CADD score can be returned
	private static final String CHROMOSOME = "chrom";
	private static final String POSITION = "pos";
	private static final String REFERENCE = "ref";
	private static final String ALTERNATIVE = "alt";

	// the cadd service returns these two values
	private static final String CADD_SCALED = "CADD_SCALED";
	private static final String CADD_ABS = "CADD_ABS";

	// system call used to call tabix, includes the file
	private static final String SYSTEM_CALL = "/Users/mdehaan/bin/tools/tabix-0.2.6/tabix /Users/mdehaan/Downloads/1000G.vcf.gz ";

	@Autowired
	DataService dataService;

	/**
	 * <p>
	 * This method...
	 * </p>
	 * 
	 * @return Iterator<Entity>
	 * 
	 * */
	@Override
	public Iterator<Entity> annotate(Iterator<Entity> source)
	{
		List<Entity> results = new ArrayList<Entity>();

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
				Process process = runTime.exec(SYSTEM_CALL + chromosome + ":" + position + "-" + position);

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
	public Boolean canAnnotate()
	{
		return true;
	}

}
