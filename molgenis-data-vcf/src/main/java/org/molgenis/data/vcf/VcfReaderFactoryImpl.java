package org.molgenis.data.vcf;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.vcf.VcfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class VcfReaderFactoryImpl implements VcfReaderFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(VcfReaderFactoryImpl.class);

	private File file;
	private List<VcfReader> vcfReaderRegistry = Lists.newArrayList();

	public VcfReaderFactoryImpl(File file)
	{
		this.file = requireNonNull(file);
		Preconditions.checkArgument(file.exists());
	}

	@Override
	public VcfReader get()
	{
		try
		{
			InputStream inputStream = new FileInputStream(file);
			if (file.getName().endsWith(".gz"))
			{
				inputStream = new GZIPInputStream(inputStream);
			}
			else if (file.getName().endsWith(".zip"))
			{
				ZipFile zipFile = new ZipFile(file.getPath());
				Enumeration<? extends ZipEntry> e = zipFile.entries();
				ZipEntry entry = e.nextElement(); // your only file
				inputStream = zipFile.getInputStream(entry);
			}
			VcfReader reader = new VcfReader(new InputStreamReader(inputStream, UTF_8));
			// bootstrap reader so close() can close all readers
			vcfReaderRegistry.add(reader);
			return reader;

		}
		catch (IOException e)
		{
			throw new MolgenisDataException("Failed to create VCF Reader for file" + file.getAbsolutePath(), e);
		}
	}

	protected void tryCloseVcfReader(VcfReader reader)
	{
		try
		{
			LOG.debug("Close VcfReader.");
			reader.close();
		}
		catch (Exception ex)
		{
			LOG.info("Failed to close VcfReader.", ex);
		}
	}

	@Override
	public void close() throws IOException
	{
		vcfReaderRegistry.forEach(this::tryCloseVcfReader);
	}
}
