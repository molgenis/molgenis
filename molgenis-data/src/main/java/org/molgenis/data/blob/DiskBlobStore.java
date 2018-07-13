package org.molgenis.data.blob;

import com.google.common.io.ByteStreams;
import org.molgenis.data.populate.IdGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.requireNonNull;

public class DiskBlobStore implements BlobStore
{
	private final Path storePath;
	private final IdGenerator idGenerator;

	public DiskBlobStore(Path storePath, IdGenerator idGenerator)
	{
		this.storePath = requireNonNull(storePath);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	public WritableBlobChannel newChannel()
	{
		String blobId = generateBlobId();
		Path blobPath = getBlobPath(blobId);
		WritableByteChannel toChannel;
		try
		{
			toChannel = Files.newByteChannel(blobPath, EnumSet.of(CREATE_NEW, WRITE));
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return WritableBlobChannel.create(blobId, toChannel);
	}

	@Override
	public BlobMetadata store(ReadableByteChannel fromChannel)
	{
		String blobId = generateBlobId();
		Path blobPath = getBlobPath(blobId);
		long nrBytesWritten;
		try
		{
			WritableByteChannel toChannel = Files.newByteChannel(blobPath, EnumSet.of(CREATE_NEW, WRITE));
			nrBytesWritten = ByteStreams.copy(fromChannel, toChannel);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return BlobMetadata.builder().setId(blobId).setSize(nrBytesWritten).build();
	}

	@Override
	public void delete(String blobId)
	{
		Path blobPath = getBlobPath(blobId);
		try
		{
			Files.delete(blobPath);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public ReadableByteChannel newChannel(String blobId)
	{

		Path blobPath = getBlobPath(blobId);

		try
		{
			return Files.newByteChannel(blobPath, READ);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private String generateBlobId()
	{
		return idGenerator.generateId();
	}

	private Path getBlobPath(String blobId)
	{
		return storePath.resolve(blobId);
	}
}