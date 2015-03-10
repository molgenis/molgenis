package org.molgenis.data.vcf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.genotype.Allele;
import org.molgenis.genotype.GenotypeDataException;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaAlt;
import org.molgenis.vcf.meta.VcfMetaContig;
import org.molgenis.vcf.meta.VcfMetaFormat;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.molgenis.vcf.meta.VcfMetaSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Repository implementation for vcf files.
 * 
 * The filename without the extension is considered to be the entityname
 */
public class VcfRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfRepository.class);

	public static final String BASE_URL = "vcf://";
	public static final String CHROM = "#CHROM";
	public static final String ALLELE1 = "ALLELE1";
	public static final String ALLELE2 = "ALLELE2";
	public static final String POS = "POS";
	public static final String REF = "REF";
	public static final String FILTER = "FILTER";
	public static final String QUAL = "QUAL";
	public static final String ID = "ID";
	public static final String INTERNAL_ID = "INTERNAL_ID";
	public static final String INFO = "INFO";
	public static final String SAMPLES = "SAMPLES";
	public static final String NAME = "NAME";
	public static final String REFERENCE = "reference";
	public static final String SOURCENAME = "SOURCENAME";

	private final File file;
	private final String entityName;

	private DefaultEntityMetaData entityMetaData;
	private List<VcfReader> vcfReaderRegistry;
	private DefaultEntityMetaData sampleEntityMetaData;
	private boolean hasFormatMetaData;

	public VcfRepository(File file, String entityName) throws IOException
	{
		super(BASE_URL + file.getName());
		this.file = file;
		this.entityName = entityName;
	}

	@Override
	public Iterator<Entity> iterator()
	{

		final VcfReader vcfReader;
		try
		{
			vcfReader = createVcfReader();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		final VcfReader finalVcfReader = vcfReader;

		return new Iterator<Entity>()
		{
			Iterator<VcfRecord> vcfRecordIterator = finalVcfReader.iterator();
			private VcfRecord vcfRecord;
			private int numberOfSamples;
			Iterator<VcfSample> sampleIterator = null;
			String reference;
		

			@Override
			public boolean hasNext()
			{
				// If this is set to null the record has been drained dry and no more samples can be split into
				// individual entities
				if (vcfRecord != null)
				{
					return true;
				}
				Boolean hasNext = vcfRecordIterator.hasNext();

				// If the original record has a next element
				if (vcfRecordIterator.hasNext())
				{
					// Set the next vcfRecord
					vcfRecord = vcfRecordIterator.next();
					
					// If there are no samples it will not pass this filter
					numberOfSamples = vcfRecord.getNrSamples();
					if (numberOfSamples == 0)
					{

						return false;
					}
					reference = null;
					try
					{
						reference = finalVcfReader.getVcfMeta().get(REFERENCE);

					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// While the record does not pass the filter check till a valid record is found
					// while (!vkglChromosomeAccessionFilter(vcfRecord.getChromosome().toString())
					// && !vkglReferenceGenomeFilter(reference))

					while (!vkglReferenceGenomeFilter(reference))
					{
						
						if (vcfRecordIterator.hasNext())
						{
							vcfRecord = vcfRecordIterator.next();
						}
						// If no next element is present return false
						else
						{

							return false;
						}
					}
				}

				return hasNext;

			}

			@Override
			public Entity next()
			{
				Entity entity = new MapEntity();
				try
				{
					entity.set(CHROM, vcfRecord.getChromosome());
					// entity.set(
					// ALT,
					// StringUtils.join(
					// Lists.transform(vcfRecord.getAlternateAlleles(), new Function<Allele, String>()
					// {
					// @Override
					// public String apply(Allele allele)
					// {
					// return allele.toString();
					// }
					// }), ','));

					entity.set(POS, vcfRecord.getPosition());
					entity.set(REF, vcfRecord.getReferenceAllele().toString());
					entity.set(FILTER, vcfRecord.getFilterStatus());
					entity.set(QUAL, vcfRecord.getQuality());
					entity.set(ID, StringUtils.join(vcfRecord.getIdentifiers(), ','));
					entity.set(REFERENCE, reference);
					entity.set(SOURCENAME, file.getName());

					StringBuilder id = new StringBuilder();

					id.append(StringUtils.strip(entity.get(CHROM).toString()));
					id.append("_");
					id.append(StringUtils.strip(entity.get(POS).toString()));
					id.append("_");
					id.append(StringUtils.strip(entity.get(REF).toString()));
					id.append("_");

					for (VcfInfo vcfInfo : vcfRecord.getInformation())
					{
						Object val = vcfInfo.getVal();
						if (val instanceof List<?>)
						{
							// TODO support list of primitives datatype
							val = StringUtils.join((List<?>) val, ',');
						}
						entity.set(vcfInfo.getKey(), val);
					}
					if (hasFormatMetaData)
					{
						List<Entity> samples = new ArrayList<Entity>();

						if (vcfRecord.getNrSamples() > 0)
						{
							// If sampleIterator is null get a new one
							if (sampleIterator == null)
							{
								sampleIterator = vcfRecord.getSamples().iterator();
							}

							Iterator<String> sampleNameIterator = finalVcfReader.getVcfMeta().getSampleNames()
									.iterator();

							if (sampleIterator.hasNext())
							{
								String[] format = vcfRecord.getFormat();
								VcfSample sample = sampleIterator.next();
								Entity sampleEntity = new MapEntity(sampleEntityMetaData);

								for (int i = 0; i < format.length; i++)
								{
									sampleEntity.set(format[i], sample.getData(i));
								}

								List<String> alleles = new ArrayList<String>();
								String[] genoTypes = null;
								if (sampleEntity.getString("GT").contains("/"))
								{
									genoTypes = sampleEntity.getString("GT").split("/");
						
								}
								else if (sampleEntity.getString("GT").contains("|"))
								{
									genoTypes = sampleEntity.getString("GT").split("\\|");
						
									
							
								}
								
								for (String genoType : genoTypes)
								{
									if (genoType.equals("0"))
									{

										alleles.add(entity.get(REF).toString());
							
									}
									else
									{
							
										String allele2 = vcfRecord.getAlternateAlleles()
												.get(Integer.parseInt(genoType) - 1).toString();
								
										alleles.add(allele2);
									}

								}
								entity.set(ALLELE1, alleles.get(0));
								entity.set(ALLELE2, alleles.get(1));
								id.append(StringUtils.strip(entity.get(ALLELE1).toString()));
								id.append("_");
								id.append(StringUtils.strip(entity.get(ALLELE2).toString()));
								entity.set(INTERNAL_ID, id.toString());
								sampleEntity.set(ID, UUID.randomUUID());
								// FIXME remove entity ID from Sample label after #1400 is fixed, see also:
								// jquery.molgenis.table.js line 152
								sampleEntity.set(NAME,
										entity.get(POS) + "_" + entity.get(ALLELE1) + "_" + entity.get(ALLELE2)
												+ "_" + sampleNameIterator.next());
								samples.add(sampleEntity);
							}
							else
							{
								sampleIterator = null;
							}
						}
						entity.set(SAMPLES, samples);

					}
				}

				catch (IOException e)
				{
					LOG.error("Unable to load VCF metadata. ", e);
				}
				// If the next iteration means hold no more samples set vcfRecord and sampleIterator to null
				if (--numberOfSamples == 0)
				{
					vcfRecord = null;
					sampleIterator = null;
				}

				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			/**
			 * Checks is chromosome has valid format if not return false.
			 * 
			 * @param chromosome
			 * @return boolean
			 */
			private boolean vkglChromosomeAccessionFilter(String chromosome)
			{

				if (!chromosome.contains("|"))
				{
					return false;
				}
				return true;
			}

			/**
			 * Checks if there is a reference otherwise returns false
			 * 
			 * @param reference
			 * @return boolean
			 */
			private boolean vkglReferenceGenomeFilter(String reference)
			{

				if (reference.isEmpty())
				{
					return false;
				}
				return true;
			}

		};
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(entityName);
			try
			{
				VcfReader vcfReader = createVcfReader();
				VcfMeta vcfMeta;
				try
				{
					vcfMeta = vcfReader.getVcfMeta();
					Iterable<VcfMetaFormat> formatMetaData = vcfReader.getVcfMeta().getFormatMeta();
					createSampleEntityMetaData(formatMetaData);
				}
				finally
				{
					if (vcfReader != null) vcfReader.close();
				}
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(CHROM,
						MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ALLELE1,
						MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ALLELE2,
						MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(POS,
						MolgenisFieldTypes.FieldTypeEnum.LONG).setAggregateable(true));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(REF,
						MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FILTER,
						MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(QUAL,
						MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true));
				entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ID,
						MolgenisFieldTypes.FieldTypeEnum.STRING));
				DefaultAttributeMetaData idAttributeMetaData = new DefaultAttributeMetaData(INTERNAL_ID,
						MolgenisFieldTypes.FieldTypeEnum.STRING);
				idAttributeMetaData.setNillable(false);
				idAttributeMetaData.setIdAttribute(true);
				idAttributeMetaData.setVisible(false);
				entityMetaData.addAttributeMetaData(idAttributeMetaData);
				DefaultAttributeMetaData infoMetaData = new DefaultAttributeMetaData(INFO,
						MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
				List<AttributeMetaData> metadataInfoField = new ArrayList<AttributeMetaData>();
				for (VcfMetaInfo info : vcfMeta.getInfoMeta())
				{
					DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(info.getId(),
							vcfReaderFormatToMolgenisType(info)).setAggregateable(true);
					attributeMetaData.setDescription(info.getDescription());
					metadataInfoField.add(attributeMetaData);
				}
				infoMetaData.setAttributesMetaData(metadataInfoField);
				entityMetaData.addAttributeMetaData(infoMetaData);
				if (hasFormatMetaData)
				{
					DefaultAttributeMetaData samplesAttributeMeta = new DefaultAttributeMetaData(SAMPLES,
							MolgenisFieldTypes.FieldTypeEnum.MREF);
					samplesAttributeMeta.setRefEntity(sampleEntityMetaData);
					entityMetaData.addAttributeMetaData(samplesAttributeMeta);
				}
				entityMetaData.setIdAttribute(INTERNAL_ID);
				entityMetaData.setLabelAttribute(ID);
			}
			catch (IOException e)
			{

			}
		}
		return entityMetaData;
	}

	void createSampleEntityMetaData(Iterable<VcfMetaFormat> formatMetaData)
	{
		hasFormatMetaData = formatMetaData.iterator().hasNext();
		if (hasFormatMetaData)
		{
			sampleEntityMetaData = new DefaultEntityMetaData(entityName + "_Sample");
			DefaultAttributeMetaData idAttributeMetaData = new DefaultAttributeMetaData(ID,
					MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true);
			idAttributeMetaData.setIdAttribute(true);
			idAttributeMetaData.setVisible(false);
			sampleEntityMetaData.addAttributeMetaData(idAttributeMetaData);
			DefaultAttributeMetaData nameAttributeMetaData = new DefaultAttributeMetaData(NAME,
					MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true);
			nameAttributeMetaData.setLabelAttribute(true);
			sampleEntityMetaData.addAttributeMetaData(nameAttributeMetaData);
			for (VcfMetaFormat meta : formatMetaData)
			{
				AttributeMetaData attributeMetaData = new DefaultAttributeMetaData(meta.getId(),
						vcfFieldTypeToMolgenisFieldType(meta)).setAggregateable(true);
				sampleEntityMetaData.addAttributeMetaData(attributeMetaData);
			}
		}
	}

	private MolgenisFieldTypes.FieldTypeEnum vcfReaderFormatToMolgenisType(VcfMetaInfo vcfMetaInfo)
	{
		String number = vcfMetaInfo.getNumber();
		boolean isListValue;
		try
		{
			isListValue = number.equals("A") || number.equals("R") || number.equals("G") || number.equals(".")
					|| Integer.parseInt(number) > 1;
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
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.STRING;
			case FLAG:
				return MolgenisFieldTypes.FieldTypeEnum.BOOL;
			case FLOAT:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
			case INTEGER:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.INT;
			case STRING:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.STRING;
			default:
				throw new MolgenisDataException("unknown vcf info type [" + vcfMetaInfo.getType() + "]");
		}
	}

	private MolgenisFieldTypes.FieldTypeEnum vcfFieldTypeToMolgenisFieldType(VcfMetaFormat format)
	{
		String number = format.getNumber();
		boolean isListValue;
		try
		{
			isListValue = number.equals("A") || number.equals("R") || number.equals("G") || number.equals(".")
					|| Integer.parseInt(number) > 1;
		}
		catch (NumberFormatException ex)
		{
			throw new GenotypeDataException("Error parsing length of vcf info field. " + number
					+ " is not a valid int or expected preset (A, R, G, .)", ex);
		}
		switch (format.getType())
		{
			case CHARACTER:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.STRING;
			case FLOAT:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
			case INTEGER:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.INT;
			case STRING:
				if (isListValue)
				{
					// TODO support list of primitives datatype
					return MolgenisFieldTypes.FieldTypeEnum.STRING;
				}
				return MolgenisFieldTypes.FieldTypeEnum.STRING;
			default:
				throw new MolgenisDataException("unknown vcf field type [" + format.getType() + "]");
		}
	}

	private VcfReader createVcfReader() throws IOException
	{
		VcfReader reader = new VcfReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		// register reader so close() can close all readers
		if (vcfReaderRegistry == null) vcfReaderRegistry = new ArrayList<VcfReader>();
		vcfReaderRegistry.add(reader);

		return reader;
	}

	@Override
	public void close() throws IOException
	{
		if (vcfReaderRegistry != null)
		{
			for (VcfReader vcfReader : vcfReaderRegistry)
			{
				try
				{
					vcfReader.close();
				}
				catch (IOException e)
				{
					LOG.warn("", e);
				}
			}
		}
	}

}
