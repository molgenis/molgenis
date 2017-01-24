package org.molgenis.data.vcf;

import com.google.common.collect.ImmutableSet;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class VcfRepositoryCollection extends FileRepositoryCollection
{
	public static final String NAME = "VCF";
	private static final String EXTENSION_VCF = "vcf";
	private static final String EXTENSION_VCF_GZ = "vcf.gz";
	private static final String EXTENSION_VCF_ZIP = "vcf.zip";
	static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_VCF, EXTENSION_VCF_GZ, EXTENSION_VCF_ZIP);

	@Autowired
	private VcfAttributes vcfAttributes;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private final File file;
	private final String entityName;

	public VcfRepositoryCollection(File file) throws IOException
	{
		super(EXTENSIONS);
		this.file = requireNonNull(file);

		String name = file.getName();
		if (name.endsWith(EXTENSION_VCF))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + EXTENSION_VCF));
		}
		else if (name.endsWith(EXTENSION_VCF_GZ))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + EXTENSION_VCF_GZ));
		}
		else if (name.endsWith(EXTENSION_VCF_ZIP))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + EXTENSION_VCF_ZIP));
		}
		else
		{
			throw new IllegalArgumentException("Not a VCF file [" + file.getName() + "]");
		}
	}

	@Override
	public void init() throws IOException
	{
		// no operation
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return Collections.singleton(entityName);
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		if (!entityName.equals(name)) throw new MolgenisDataException("Unknown entity name [" + name + "]");
		try
		{
			return new VcfRepository(file, name, vcfAttributes, entityTypeFactory, attrMetaFactory);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return new Iterator<Repository<Entity>>()
		{
			Iterator<String> it = getEntityNames().iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository<Entity> next()
			{
				return getRepository(it.next());
			}

		};
	}

	@Override
	public boolean hasRepository(String name)
	{
		if (null == name) return false;
		Iterator<String> entityNames = getEntityNames().iterator();
		while (entityNames.hasNext())
		{
			if (entityNames.next().equals(name)) return true;
		}
		return false;
	}

	@Override
	public boolean hasRepository(EntityType entityType)
	{
		return hasRepository(entityType.getFullyQualifiedName());
	}
}
