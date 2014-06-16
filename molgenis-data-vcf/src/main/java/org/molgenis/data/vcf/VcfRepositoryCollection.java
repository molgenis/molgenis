package org.molgenis.data.vcf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Reads vcf files.
 * 
 * The exposes the files as {@link org.molgenis.data.Repository}. The names of the repositories are the names of the
 * files without the extension
 */
public class VcfRepositoryCollection extends FileRepositoryCollection
{
	public static final String EXTENSION_VCF = "vcf";
	public static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_VCF);
	private final File file;
	private final String entityName;
	private List<String> entityNames;
	private List<String> entityNamesLowerCase;

	public VcfRepositoryCollection(File file, String entityName) throws IOException
	{
		super(EXTENSIONS);
		this.file = file;
		this.entityName = entityName;

		loadEntityName();
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return entityNames;
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		if (!entityNamesLowerCase.contains(name.toLowerCase()))
		{
			return null;
		}

		return new VcfRepository(file);
	}

	private void loadEntityName()
	{
		entityNames = Lists.newArrayList();
		entityNamesLowerCase = Lists.newArrayList();

		String name = getRepositoryName(entityName);
		entityNames.add(name);
		entityNamesLowerCase.add(name.toLowerCase());
	}

	private String getRepositoryName(String fileName)
	{
		return StringUtils.stripFilenameExtension(StringUtils.getFilename(fileName));
	}

}
