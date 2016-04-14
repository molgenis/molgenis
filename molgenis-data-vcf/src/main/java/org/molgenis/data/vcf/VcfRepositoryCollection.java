package org.molgenis.data.vcf;

import com.google.common.collect.ImmutableSet;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class VcfRepositoryCollection extends FileRepositoryCollection
{
	public static final String NAME = "VCF";
	private static final String EXTENSION_VCF = "vcf";
	private static final String EXTENSION_VCF_GZ = "vcf.gz";
	private static final String EXTENSION_VCF_ZIP = "vcf.zip";
	static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_VCF, EXTENSION_VCF_GZ, EXTENSION_VCF_ZIP);

	private final File file;
	private final String entityName;

	public VcfRepositoryCollection(File file) throws IOException
	{
		super(EXTENSIONS);
		if (file == null) throw new IllegalArgumentException("file is null");
		this.file = file;

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
	public Iterable<String> getEntityNames()
	{
		return Collections.singleton(entityName);
	}

	@Override
	public Repository getRepository(String name)
	{
		if (!entityName.equals(name)) throw new MolgenisDataException("Unknown entity name [" + name + "]");
		try
		{
			return new VcfRepository(file, name);
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
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		return getRepository(entityMeta.getName());
	}

	@Override
	public Iterator<Repository> iterator()
	{
		return new Iterator<Repository>()
		{
			Iterator<String> it = getEntityNames().iterator();

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public Repository next()
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
}
