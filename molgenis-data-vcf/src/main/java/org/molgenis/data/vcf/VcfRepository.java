package org.molgenis.data.vcf;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.vcf.format.VcfToEntity;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static final String NAME = "NAME";
	public static final String ORIGINAL_NAME = "ORIGINAL_NAME";
	public static final String PREFIX = "##";

	private final String entityName;
	protected Supplier<VcfToEntity> vcfToEntitySupplier;
	private VcfReaderFactory vcfReaderFactory;

	public VcfRepository(File file, String entityName) throws IOException
	{
		this(new VcfReaderFactoryImpl(file), entityName);
	}

	protected VcfRepository(VcfReaderFactory vcfReaderFactory, String entityName)
	{
		this.entityName = requireNonNull(entityName);
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
