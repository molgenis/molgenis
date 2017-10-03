package org.molgenis.data.vcf;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.vcf.format.VcfToEntity;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Repository implementation for vcf files.
 * <p>
 * The filename without the extension is considered to be the entityname
 */
public class VcfRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfRepository.class);
	public static final String DEFAULT_ATTRIBUTE_DESCRIPTION = "Description not provided";

	public static final String NAME = "NAME";
	public static final String ORIGINAL_NAME = "ORIGINAL_NAME";
	public static final String PREFIX = "##";

	private final VcfReaderFactory vcfReaderFactory;
	private final String entityTypeId;
	private final VcfAttributes vcfAttributes;
	private final EntityTypeFactory entityTypeFactory;
	private final AttributeFactory attrMetaFactory;
	protected final Supplier<VcfToEntity> vcfToEntitySupplier;

	public VcfRepository(File file, String entityTypeId, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory)
	{
		this(new VcfReaderFactoryImpl(file), entityTypeId, vcfAttributes, entityTypeFactory, attrMetaFactory);
	}

	protected VcfRepository(VcfReaderFactory vcfReaderFactory, String entityTypeId, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attrMetaFactory)
	{
		this.vcfReaderFactory = requireNonNull(vcfReaderFactory);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.vcfAttributes = requireNonNull(vcfAttributes);
		this.entityTypeFactory = requireNonNull(entityTypeFactory);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.vcfToEntitySupplier = Suppliers.memoize(this::parseVcfMeta);
	}

	private VcfToEntity parseVcfMeta()
	{
		VcfReader reader = vcfReaderFactory.get();
		try
		{
			VcfMeta vcfMeta = reader.getVcfMeta();
			return new VcfToEntity(entityTypeId, vcfMeta, vcfAttributes, entityTypeFactory, attrMetaFactory);
		}
		catch (IOException | RuntimeException e)
		{
			throw new MolgenisDataException("Failed to read VCF Metadata from file", e);
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

	/**
	 * Returns an iterator for this repository.
	 * <p>
	 * Use with caution! Multiple iterators will all point to the same line in the VCF file, leading to unpredictable
	 * behaviour. If you want to get the EntityType of this repository and you can't access getEntityType(),
	 * convert the iterator to a PeekingIterator and peek the first Entity.
	 */
	@Override
	public Iterator<Entity> iterator()
	{
		Iterator<VcfRecord> vcfRecordIterator = Iterators.unmodifiableIterator(vcfReaderFactory.get().iterator());
		VcfToEntity vcfToEntity = vcfToEntitySupplier.get();
		return Iterators.transform(vcfRecordIterator, vcfToEntity::toEntity);
	}

	public EntityType getEntityType()
	{
		return vcfToEntitySupplier.get().getEntityType();
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
