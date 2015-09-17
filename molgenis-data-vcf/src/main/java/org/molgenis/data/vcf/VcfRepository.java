package org.molgenis.data.vcf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.format.VcfToEntity;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * Repository implementation for vcf files.
 * 
 * The filename without the extension is considered to be the entityname
 */
public class VcfRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfRepository.class);
	public static final String DEFAULT_ATTRIBUTE_DESCRIPTION = "Description not provided";

	public static final String CHROM = "#CHROM";
	public static final String ALT = "ALT";
	public static final String POS = "POS";
	public static final String REF = "REF";
	public static final String FILTER = "FILTER";
	public static final String QUAL = "QUAL";
	public static final String ID = "ID";
	public static final String INTERNAL_ID = "INTERNAL_ID";
	public static final String INFO = "INFO";
	public static final String FORMAT_GT = "GT";
	public static final String SAMPLES = "SAMPLES_ENTITIES";
	public static final String NAME = "NAME";
	public static final String ORIGINAL_NAME = "ORIGINAL_NAME";
	public static final String PREFIX = "##";

	public static final AttributeMetaData CHROM_META = new DefaultAttributeMetaData(CHROM,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(false)
					.setDescription("The chromosome on which the variant is observed");
	// TEXT instead of STRING to handle large insertions/deletions
	public static final AttributeMetaData ALT_META = new DefaultAttributeMetaData(ALT,
			MolgenisFieldTypes.FieldTypeEnum.TEXT).setAggregateable(true).setNillable(false)
					.setDescription("The alternative allele observed");
	public static final AttributeMetaData POS_META = new DefaultAttributeMetaData(POS,
			MolgenisFieldTypes.FieldTypeEnum.LONG).setAggregateable(true).setNillable(false)
					.setDescription("The position on the chromosome which the variant is observed");
	// TEXT instead of STRING to handle large insertions/deletions
	public static final AttributeMetaData REF_META = new DefaultAttributeMetaData(REF,
			MolgenisFieldTypes.FieldTypeEnum.TEXT).setAggregateable(true).setNillable(false)
					.setDescription("The reference allele");
	public static final AttributeMetaData FILTER_META = new DefaultAttributeMetaData(FILTER,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(true)
					.setDescription(DEFAULT_ATTRIBUTE_DESCRIPTION);
	public static final AttributeMetaData QUAL_META = new DefaultAttributeMetaData(QUAL,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setAggregateable(true).setNillable(true)
					.setDescription(DEFAULT_ATTRIBUTE_DESCRIPTION);
	public static final AttributeMetaData ID_META = new DefaultAttributeMetaData(ID,
			MolgenisFieldTypes.FieldTypeEnum.STRING).setNillable(true).setDescription(DEFAULT_ATTRIBUTE_DESCRIPTION);

	private final String entityName;
	protected Supplier<VcfToEntity> vcfToEntitySupplier;
	private VcfReaderFactory vcfReaderFactory;

	public VcfRepository(File file, String entityName) throws IOException
	{
		this(new VcfReaderFactoryImpl(file), entityName);
	}

	protected VcfRepository(VcfReaderFactory vcfReaderFactory, String entityName)
	{
		this.entityName = Preconditions.checkNotNull(entityName);
		this.vcfReaderFactory = vcfReaderFactory;
		this.vcfToEntitySupplier = Suppliers.<VcfToEntity> memoize(this::parseVcfMeta);
	}

	private VcfToEntity parseVcfMeta()
	{
		VcfReader reader = vcfReaderFactory.get();
		try
		{
			VcfMeta vcfMeta = reader.getVcfMeta();
			return new VcfToEntity(entityName, vcfMeta);
		}
		catch (Exception e)
		{
			LOG.error("Failed to read VCF Metadata from file", e);
			return null;
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				LOG.info("Failed to close VcfReader", e);
			}
		}
	}

	@Override
	public Iterator<Entity> iterator()
	{
		Iterator<VcfRecord> vcfRecordIterator = Iterators.unmodifiableIterator(vcfReaderFactory.get().iterator());
		VcfToEntity vcfToEntity = vcfToEntitySupplier.get();
		return Iterators.transform(vcfRecordIterator, vcfToEntity::toEntity);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return vcfToEntitySupplier.get().getEntityMetaData();
	}

	@Override
	public void close() throws IOException
	{
		vcfReaderFactory.close();
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
