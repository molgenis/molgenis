package org.molgenis.data.file.minio;

import static java.util.Objects.requireNonNull;

import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.molgenis.data.file.BlobMetadata;
import org.molgenis.data.file.BlobStore;
import org.molgenis.data.populate.IdGenerator;
import org.xmlpull.v1.XmlPullParserException;

class MinioBlobStore implements BlobStore {
  private final MinioClientFacade minioClientFacade;
  private final IdGenerator idGenerator;

  MinioBlobStore(MinioClientFacade minioClientFacade, IdGenerator idGenerator) {
    this.minioClientFacade = requireNonNull(minioClientFacade);
    this.idGenerator = requireNonNull(idGenerator);
  }

  @Override
  public BlobMetadata store(ReadableByteChannel fromChannel) {
    String blobId = generateBlobId();

    // The "octet-stream" subtype is used to indicate that a body contains arbitrary binary data.
    String contentType = "application/octet-stream";
    InputStream inputStream = Channels.newInputStream(fromChannel);
    try {
      minioClientFacade.putObject(blobId, inputStream, contentType);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | ErrorResponseException
        | InternalException
        | InvalidArgumentException
        | InsufficientDataException e) {
      throw new UncheckedIOException(new IOException(e));
    }

    ObjectStat objectStat;
    try {
      objectStat = minioClientFacade.statObject(blobId);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | ErrorResponseException
        | InternalException e) {
      throw new UncheckedIOException(new IOException(e));
    }
    long size = objectStat.length();

    return BlobMetadata.builder().setId(blobId).setSize(size).build();
  }

  @Override
  public void delete(String blobId) {
    try {
      minioClientFacade.removeObject(blobId);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | ErrorResponseException
        | InternalException
        | InvalidArgumentException e) {
      throw new UncheckedIOException(new IOException(e));
    }
  }

  @Override
  public ReadableByteChannel newChannel(String blobId) {
    InputStream inputStream;
    try {
      inputStream = minioClientFacade.getObject(blobId);
    } catch (InvalidBucketNameException
        | NoSuchAlgorithmException
        | InsufficientDataException
        | InvalidKeyException
        | NoResponseException
        | XmlPullParserException
        | ErrorResponseException
        | InternalException
        | InvalidArgumentException e) {
      throw new UncheckedIOException(new IOException(e));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return Channels.newChannel(inputStream);
  }

  private String generateBlobId() {
    return idGenerator.generateId();
  }
}
