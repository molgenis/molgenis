package org.molgenis.data.vcf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCapability;
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
import org.molgenis.vcf.meta.VcfMetaFormat;
import org.molgenis.vcf.meta.VcfMetaInfo;
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

	public static final String CHROM = "#CHROM";
	public static final String ALT = "ALT";
	public static final String POS = "POS";
	public static final String REF = "REF";
	public static final String FILTER = "FILTER";
	public static final String QUAL = "QUAL";
	public static final String ID = "ID";
	public static final String INTERNAL_ID = "INTERNAL_ID";
	public static final String INFO = "INFO";
	public static final String SAMPLES = "SAMPLES_ENTITIES";
	public static final String NAME = "NAME";
	public static final String PREFIX = "##";

	public static final AttributeMetaData CHROM_META = new DefaultAttributeMetaData(CHROM,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(false)
			.setDescription("The chromosome on which the variant is observed");
	public static final AttributeMetaData ALT_META = new DefaultAttributeMetaData(ALT,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(false)
			.setDescription("The alternative allele observed");
	public static final AttributeMetaData POS_META = new DefaultAttributeMetaData(POS,
			MolgenisFieldTypes.FieldTypeEnum.LONG).setAggregateable(true).setNillable(false)
			.setDescription("The position on the chromosome which the variant is observed");
	public static final AttributeMetaData REF_META = new DefaultAttributeMetaData(REF,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(false)
			.setDescription("The reference allele");
	public static final AttributeMetaData FILTER_META = new DefaultAttributeMetaData(FILTER,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(true);
	public static final AttributeMetaData QUAL_META = new DefaultAttributeMetaData(QUAL,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(true);
	public static final AttributeMetaData ID_META = new DefaultAttributeMetaData(ID,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setNillable(true);
	private final File file;
	private final String entityName;

	private DefaultEntityMetaData entityMetaData;
	private List<VcfReader> vcfReaderRegistry;
	private DefaultEntityMetaData sampleEntityMetaData;
	private boolean hasFormatMetaData;

	public VcfRepository(File file, String entityName) throws IOException
	{
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

				Entity entity = new MapEntity(getEntityMetaData());
				try
				{
					VcfRecord vcfRecord = vcfRecordIterator.next();
					parseVcfRecord(vcfReader, entity, vcfRecord);
				}
				catch (IOException e)
				{
					LOG.error("Unable to load VCF metadata.", e);
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

	protected void parseVcfRecord(final VcfReader vcfReader, Entity entity, VcfRecord vcfRecord) throws IOException
	{
		parseFixedFields(entity, vcfRecord);
		vcfRecord.getInformation().forEach(vcfInfo -> parseInformation(entity, vcfInfo));
		if (hasFormatMetaData)
		{
			Iterable<String> sampleNames = vcfReader.getVcfMeta().getSampleNames();
			parseSamples(entity, vcfRecord, sampleNames);
		}
	}

	private static void parseInformation(Entity entity, VcfInfo vcfInfo)
	{
		Object val = vcfInfo.getVal();
		if (val instanceof List<?>)
		{
			// TODO support list of primitives datatype
			val = StringUtils.join((List<?>) val, ',');
		}
		entity.set(getInfoPrefix() + vcfInfo.getKey(), val);
	}

	private void parseSamples(Entity entity, VcfRecord vcfRecord, Iterable<String> sampleNames)
	{
		String entityId = entity.getString(INTERNAL_ID);
		List<Entity> samples = new ArrayList<Entity>();
		Iterator<VcfSample> sampleIterator = vcfRecord.getSamples().iterator();
		if (vcfRecord.getNrSamples() > 0)
		{
			Iterator<String> sampleNameIterator = sampleNames.iterator();
			for (int j = 0; sampleIterator.hasNext(); ++j)
			{
				String[] format = vcfRecord.getFormat();
				VcfSample sample = sampleIterator.next();
				Entity sampleEntity = new MapEntity(sampleEntityMetaData);
				for (int i = 0; i < format.length; i = i + 1)
				{
					sampleEntity.set(format[i], sample.getData(i));
				}
				sampleEntity.set(ID, entityId + j);

				// FIXME remove entity ID from Sample label after #1400 is fixed, see also:
				// jquery.molgenis.table.js line 152
				sampleEntity.set(NAME, entity.get(POS) + "_" + entity.get(ALT) + "_" + sampleNameIterator.next());
				samples.add(sampleEntity);
			}
		}
		entity.set(SAMPLES, samples);
	}

	private static String parseFixedFields(Entity entity, VcfRecord vcfRecord)
	{
		entity.set(CHROM, vcfRecord.getChromosome());
		entity.set(ALT,
				StringUtils.join(Lists.transform(vcfRecord.getAlternateAlleles(), new Function<Allele, String>()
				{
					@Override
					public String apply(Allele allele)
					{
						return allele.toString();
					}
				}), ','));

		entity.set(POS, vcfRecord.getPosition());
		entity.set(REF, vcfRecord.getReferenceAllele().toString());
		entity.set(FILTER, vcfRecord.getFilterStatus());
		entity.set(QUAL, vcfRecord.getQuality());
		entity.set(ID, StringUtils.join(vcfRecord.getIdentifiers(), ','));

		StringBuilder id = new StringBuilder();
		id.append(StringUtils.strip(entity.get(CHROM).toString()));
		id.append("_");
		id.append(StringUtils.strip(entity.get(POS).toString()));
		id.append("_");
		id.append(StringUtils.strip(entity.get(REF).toString()));
		id.append("_");
		id.append(StringUtils.strip(entity.get(ALT).toString()));
		entity.set(INTERNAL_ID, id.toString());
		return id.toString();
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
					createSampleEntityMetaData(vcfMeta.getFormatMeta());
				}
				finally
				{
					if (vcfReader != null) vcfReader.close();
				}
				entityMetaData.addAttributeMetaData(CHROM_META);
				entityMetaData.addAttributeMetaData(ALT_META);
				entityMetaData.addAttributeMetaData(POS_META);
				entityMetaData.addAttributeMetaData(REF_META);
				entityMetaData.addAttributeMetaData(FILTER_META);
				entityMetaData.addAttributeMetaData(QUAL_META);
				entityMetaData.addAttributeMetaData(ID_META);
				DefaultAttributeMetaData idAttributeMetaData = new DefaultAttributeMetaData(INTERNAL_ID,
						MolgenisFieldTypes.FieldTypeEnum.STRING);
				idAttributeMetaData.setNillable(false);
				idAttributeMetaData.setIdAttribute(true);
				idAttributeMetaData.setVisible(false);
				entityMetaData.addAttributeMetaData(idAttributeMetaData);
				DefaultAttributeMetaData infoMetaData = new DefaultAttributeMetaData(INFO,
						MolgenisFieldTypes.FieldTypeEnum.COMPOUND).setNillable(true);
				List<AttributeMetaData> metadataInfoField = new ArrayList<AttributeMetaData>();
				for (VcfMetaInfo info : vcfMeta.getInfoMeta())
				{
					DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(getInfoPrefix()
							+ info.getId(), vcfReaderFormatToMolgenisType(info)).setAggregateable(true);
					attributeMetaData.setDescription(info.getDescription());
					metadataInfoField.add(attributeMetaData);
				}
				infoMetaData.setAttributesMetaData(metadataInfoField);
				entityMetaData.addAttributeMetaData(infoMetaData);
				if (hasFormatMetaData)
				{
					DefaultAttributeMetaData samplesAttributeMeta = new DefaultAttributeMetaData(SAMPLES,
							MolgenisFieldTypes.FieldTypeEnum.MREF).setRefEntity(sampleEntityMetaData).setLabel(
							"SAMPLES");
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

	/**
	 * Prefix to make INFO column names safe-ish. For example, 'Samples' is sometimes used as an INFO field and clashes
	 * with the 'Samples' key used by Genotype-IO to store sample data in memory. By prefixing a tag we hope to create
	 * unique INFO field names that do not clash.
	 * 
	 * @return
	 */
	public static String getInfoPrefix()
	{
		return INFO + "_";
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
			nameAttributeMetaData.setLabelAttribute(true).setLookupAttribute(true);
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
					return MolgenisFieldTypes.FieldTypeEnum.TEXT;
				}
				return MolgenisFieldTypes.FieldTypeEnum.TEXT;
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
		VcfReader reader = new VcfReader(new InputStreamReader(createInputStream(), Charset.forName("UTF-8")));
		// register reader so close() can close all readers
		if (vcfReaderRegistry == null) vcfReaderRegistry = new ArrayList<VcfReader>();
		vcfReaderRegistry.add(reader);

		return reader;
	}

	protected InputStream createInputStream() throws IOException
	{
		return new FileInputStream(file);
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
					LOG.warn("Failed to close reader", e);
				}
			}
		}
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
