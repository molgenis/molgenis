package org.molgenis.data.vcf;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.genotype.GenotypeDataException;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Repository implementation for vcf files.
 * 
 * The filename without the extension is considered to be the entityname
 */
public class VcfRepository extends AbstractRepository
{
	public static final String BASE_URL = "vcf://";
	public static final String CHROM = "#CHROM";
	public static final String ALT = "ALT";
	public static final String POS = "POS";
	public static final String REF = "REF";
	public static final String FILTER = "FILTER";
	public static final String QUAL = "QUAL";
	public static final String ID = "ID";

	private DefaultEntityMetaData entityMetaData;
	private final File file;
	private VcfReader vcfReader;

	public VcfRepository(File file)
	{
		super(BASE_URL + file.getName());
		this.file = file;
		try
		{
			FileReader reader = new FileReader(file);
			this.vcfReader = new VcfReader(reader);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			Iterator<VcfRecord> vcfRecordIterator = vcfReader.iterator();

			@Override
			public boolean hasNext()
			{
				return vcfRecordIterator.hasNext();
			}

			@Override
			public Entity next()
			{

				Entity entity = new MapEntity();
				try
				{
					VcfRecord record = vcfRecordIterator.next();
					entity.set(CHROM, record.getChromosome());
					entity.set(ALT, StringUtils.arrayToCommaDelimitedString(record.getAlternateAlleles().toArray()));
					entity.set(POS, record.getPosition());
					entity.set(REF, record.getReferenceAllele());
					entity.set(FILTER, record.getFilterStatus());
					entity.set(QUAL, record.getQuality());
					entity.set(ID, StringUtils.arrayToCommaDelimitedString(record.getIdentifiers().toArray()));
					for (VcfInfo informationColumn : record.getInformation())
					{
						entity.set(informationColumn.getKey(), informationColumn.getVal());
					}
					// TODO: deal with samples
					/**
					 * int i = 0; Iterator<VcfSample> sampleIterator = record.getSamples().iterator(); while
					 * (sampleIterator.hasNext()) { VcfSample sample = sampleIterator.next(); //FIXME: how to deal with
					 * this/lists? entity.set(vcfReader.getVcfMeta().getSampleName(i), sample.getAlleles().toString());
					 * i++; }
					 **/
				}
				catch (Exception e)
				{

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
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(StringUtils.stripFilenameExtension(file.getName()));
			try
			{
				VcfMeta metadata = vcfReader.getVcfMeta();
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(CHROM,
						MolgenisFieldTypes.FieldTypeEnum.STRING));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ALT,
						MolgenisFieldTypes.FieldTypeEnum.STRING));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(POS,
						MolgenisFieldTypes.FieldTypeEnum.LONG));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(REF,
						MolgenisFieldTypes.FieldTypeEnum.STRING));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FILTER,
						MolgenisFieldTypes.FieldTypeEnum.STRING));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(QUAL,
						MolgenisFieldTypes.FieldTypeEnum.STRING));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ID,
						MolgenisFieldTypes.FieldTypeEnum.STRING));
				Iterator<VcfMetaInfo> metaInfoIterator = metadata.getInfoMeta().iterator();
				while (metaInfoIterator.hasNext())
				{
					VcfMetaInfo info = metaInfoIterator.next();
					entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(info.getId(),
							vcfReaderFormatToMolgenisType(info)));
				}
				// TODO: deal with samples
				/**
				 * Iterator<VcfMetaSample> sampleInfoIterator = metadata.getSampleMeta().iterator(); while
				 * (sampleInfoIterator.hasNext()) { VcfMetaSample info = sampleInfoIterator.next();
				 * entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(info.getId(),
				 * MolgenisFieldTypes.FieldTypeEnum.STRING)); }
				 **/
				entityMetaData.setIdAttribute(ID);
				entityMetaData.setLabelAttribute(ID);
			}
			catch (IOException e)
			{

			}
		}
		return entityMetaData;
	}

	private MolgenisFieldTypes.FieldTypeEnum vcfReaderFormatToMolgenisType(VcfMetaInfo vcfMetaInfo)
	{
		String number = vcfMetaInfo.getNumber();
		boolean isListValue;
		try
		{
			isListValue = number.equals("A") || number.equals("R") || number.equals("G") || number.equals(".")
					|| Integer.valueOf(number) > 1;
		}
		catch (NumberFormatException ex)
		{
			throw new GenotypeDataException("Error parsing length of vcf info field. " + number
					+ " is not a valid int or expected preset (A, R, G, .)", ex);
		}
		switch (vcfMetaInfo.getType())
		{
			case CHARACTER:
				if (isListValue)
				{
					// FIXME: how to deal with this/lists?
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.STRING;

			case FLAG:
				return MolgenisFieldTypes.FieldTypeEnum.BOOL;
			case FLOAT:
				if (isListValue)
				{
					// FIXME: how to deal with this/lists?
					return MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
				}
				return MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
			case INTEGER:
				if (isListValue)
				{
					// FIXME: how to deal with this/lists?
					return MolgenisFieldTypes.FieldTypeEnum.INT;
				}
				return MolgenisFieldTypes.FieldTypeEnum.INT;

			case STRING:
				if (isListValue)
				{
					// FIXME: how to deal with this/lists?
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.STRING;

			default:
				throw new MolgenisDataException("unknown vcf info type [" + vcfMetaInfo.getType() + "]");
		}
	}

	@Override
	public void close() throws IOException
	{
	}
}
