package org.molgenis.data.vcf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;

import com.google.common.collect.ImmutableSet;

public class VcfRepositoryCollection extends FileRepositoryCollection
{
	private static final String EXTENSION_VCF = "vcf";
	private static final String EXTENSION_VCF_GZ = "vcf.gz";
	static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_VCF, EXTENSION_VCF_GZ);

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
	public Repository getRepositoryByEntityName(String name)
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
}
