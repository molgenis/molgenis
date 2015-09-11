package org.molgenis.data.vcf.format;

import static org.elasticsearch.common.base.Preconditions.checkNotNull;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.ALT_META;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.CHROM_META;
import static org.molgenis.data.vcf.VcfRepository.FILTER;
import static org.molgenis.data.vcf.VcfRepository.FILTER_META;
import static org.molgenis.data.vcf.VcfRepository.ID;
import static org.molgenis.data.vcf.VcfRepository.ID_META;
import static org.molgenis.data.vcf.VcfRepository.INFO;
import static org.molgenis.data.vcf.VcfRepository.INTERNAL_ID;
import static org.molgenis.data.vcf.VcfRepository.NAME;
import static org.molgenis.data.vcf.VcfRepository.ORIGINAL_NAME;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.POS_META;
import static org.molgenis.data.vcf.VcfRepository.QUAL;
import static org.molgenis.data.vcf.VcfRepository.QUAL_META;
import static org.molgenis.data.vcf.VcfRepository.REF;
import static org.molgenis.data.vcf.VcfRepository.REF_META;
import static org.molgenis.data.vcf.VcfRepository.SAMPLES;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.genotype.Allele;
import org.molgenis.genotype.GenotypeDataException;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaFormat;
import org.molgenis.vcf.meta.VcfMetaInfo;

import com.google.common.collect.Lists;

public class VcfToEntity
{
	private final EntityMetaData entityMetaData;
	private final EntityMetaData sampleEntityMetaData;
	private final VcfMeta vcfMeta;

	public VcfToEntity(String entityName, VcfMeta vcfMeta)
	{
		this.vcfMeta = checkNotNull(vcfMeta);
		sampleEntityMetaData = createSampleEntityMetaData(checkNotNull(entityName),
				checkNotNull(vcfMeta.getFormatMeta()));
		entityMetaData = createEntityMetaData(entityName, vcfMeta);
	}

	private EntityMetaData createSampleEntityMetaData(String entityName, Iterable<VcfMetaFormat> formatMetaData)
	{
		DefaultEntityMetaData result = null;
		if (formatMetaData.iterator().hasNext())
		{
			result = new DefaultEntityMetaData(entityName + "_Sample");
			DefaultAttributeMetaData idAttributeMetaData = new DefaultAttributeMetaData(ID,
					MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true);
			idAttributeMetaData.setIdAttribute(true);
			idAttributeMetaData.setVisible(false);

			result.addAttributeMetaData(idAttributeMetaData);
			DefaultAttributeMetaData nameAttributeMetaData = new DefaultAttributeMetaData(NAME,
					MolgenisFieldTypes.FieldTypeEnum.TEXT).setAggregateable(true);
			nameAttributeMetaData.setLabelAttribute(true).setLookupAttribute(true);
			result.addAttributeMetaData(nameAttributeMetaData);
			for (VcfMetaFormat meta : formatMetaData)
			{
				AttributeMetaData attributeMetaData = new DefaultAttributeMetaData(meta.getId(),
						vcfFieldTypeToMolgenisFieldType(meta)).setAggregateable(true);
				result.addAttributeMetaData(attributeMetaData);
			}
		}
		return result;
	}

	private EntityMetaData createEntityMetaData(String entityName, VcfMeta vcfMeta)
	{
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(entityName);
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
			String postFix = "";
			for (AttributeMetaData attributeMetaData : entityMetaData.getAtomicAttributes())
			{
				if (attributeMetaData.getName().equals(info.getId()))
				{
					postFix = "_" + entityName;
				}
			}
			DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(info.getId() + postFix,
					vcfReaderFormatToMolgenisType(info)).setAggregateable(true);

			attributeMetaData.setDescription(StringUtils.isBlank(info.getDescription())
					? VcfRepository.DEFAULT_ATTRIBUTE_DESCRIPTION : info.getDescription());
			metadataInfoField.add(attributeMetaData);
		}
		infoMetaData.setAttributesMetaData(metadataInfoField);
		entityMetaData.addAttributeMetaData(infoMetaData);
		if (sampleEntityMetaData != null)
		{
			DefaultAttributeMetaData samplesAttributeMeta = new DefaultAttributeMetaData(SAMPLES,
					MolgenisFieldTypes.FieldTypeEnum.MREF).setRefEntity(sampleEntityMetaData).setLabel("SAMPLES");
			entityMetaData.addAttributeMetaData(samplesAttributeMeta);
		}
		entityMetaData.setIdAttribute(INTERNAL_ID);
		entityMetaData.setLabelAttribute(ID);
		return entityMetaData;
	}

	private static MolgenisFieldTypes.FieldTypeEnum vcfReaderFormatToMolgenisType(VcfMetaInfo vcfMetaInfo)
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

	private static MolgenisFieldTypes.FieldTypeEnum vcfFieldTypeToMolgenisFieldType(VcfMetaFormat format)
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

	public Entity toEntity(String[] tokens)
	{
		return toEntity(new VcfRecord(vcfMeta, tokens));
	}

	public Entity toEntity(VcfRecord vcfRecord)
	{
		Entity entity = new MapEntity(entityMetaData);
		entity.set(CHROM, vcfRecord.getChromosome());
		entity.set(ALT, StringUtils.join(Lists.transform(vcfRecord.getAlternateAlleles(), Allele::toString), ','));
		entity.set(POS, vcfRecord.getPosition());
		entity.set(REF, vcfRecord.getReferenceAllele().toString());
		entity.set(FILTER, vcfRecord.getFilterStatus());
		entity.set(QUAL, vcfRecord.getQuality());
		entity.set(ID, StringUtils.join(vcfRecord.getIdentifiers(), ','));

		String id = VcfUtils.createId(entity);
		entity.set(INTERNAL_ID, id);

		writeInfoFieldsToEntity(vcfRecord, entity);
		if (sampleEntityMetaData != null)
		{
			List<Entity> samples = createSampleEntities(vcfRecord, entity.get(POS) + "_" + entity.get(ALT), id);
			entity.set(SAMPLES, samples);
		}
		return entity;
	}

	protected List<Entity> createSampleEntities(VcfRecord vcfRecord, String entityPosAlt, String entityId)
	{
		List<Entity> samples = new ArrayList<Entity>();
		Iterator<VcfSample> sampleIterator = vcfRecord.getSamples().iterator();
		if (vcfRecord.getNrSamples() > 0)
		{
			Iterator<String> sampleNameIterator = vcfMeta.getSampleNames().iterator();
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
				String original_name = sampleNameIterator.next();
				sampleEntity.set(NAME, entityPosAlt + "_" + original_name);
				sampleEntity.set(ORIGINAL_NAME, original_name);
				samples.add(sampleEntity);
			}
		}
		return samples;
	}

	protected void writeInfoFieldsToEntity(VcfRecord vcfRecord, Entity entity)
	{
		// set all flag fields default on false.
		for (VcfMetaInfo info : vcfMeta.getInfoMeta())
		{
			String postFix = "";
			List<String> names = new ArrayList<>();
			for (AttributeMetaData attributeMetaData : entityMetaData.getAttributes())
			{
				if (attributeMetaData.getName().equals(info.getId()))
				{
					names.add(attributeMetaData.getName());
				}
			}
			if (names.contains(info.getId())) postFix = "_" + entity.getEntityMetaData().getName();
			if (info.getType().equals(VcfMetaInfo.Type.FLAG))
			{
				entity.set(info.getId() + postFix, false);
			}
		}

		for (VcfInfo vcfInfo : vcfRecord.getInformation())
		{
			String postFix = "";
			List<String> names = new ArrayList<>();
			for (AttributeMetaData attributeMetaData : entityMetaData.getAttributes())
			{
				if (attributeMetaData.getName().equals(vcfInfo.getKey()))
				{
					names.add(attributeMetaData.getName());
				}
			}
			if (vcfInfo.getKey().equals("."))
			{
				continue;
			}
			Object val = vcfInfo.getVal();
			if (val instanceof List<?>)
			{
				// TODO support list of primitives datatype
				val = StringUtils.join((List<?>) val, ',');
			}
			if (val instanceof Float && Float.isNaN((Float) val))
			{
				val = null;
			}
			// if a flag field exists in the line, then this field is true, although the value is null

			if (val == null)
			{
				if (names.contains(vcfInfo.getKey()))
				{
					postFix = "_" + entity.getEntityMetaData().getName();
				}
				if (!(vcfInfo.getKey() + postFix).equals(".")
						&& entityMetaData.getAttribute(vcfInfo.getKey() + postFix) != null
						&& entityMetaData.getAttribute(vcfInfo.getKey() + postFix).getDataType().getEnumType()
								.equals(MolgenisFieldTypes.FieldTypeEnum.BOOL))
				{
					val = true;
				}
			}
			if (val != null)
			{
				entity.set(vcfInfo.getKey() + postFix, val);
			}
		}
	}

	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}
}
