package org.molgenis.data.file.minio;

import static java.util.Objects.requireNonNull;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

/** Simplified API for {@link io.minio.MinioClient}. */
class MinioClientFacade {
  private static final Logger LOG = LoggerFactory.getLogger(MinioClientFacade.class);

  private final MinioClient minioClient;
  private final String bucketName;

  MinioClientFacade(MinioClient minioClient, String bucketName) {
    this.minioClient = requireNonNull(minioClient);
    this.bucketName = requireNonNull(bucketName);
  }

  /** @see MinioClient#putObject(String, String, InputStream, String) */
  void putObject(String objectName, InputStream stream, String contentType)
      throws InvalidBucketNameException, NoSuchAlgorithmException, IOException, InvalidKeyException,
          NoResponseException, XmlPullParserException, ErrorResponseException, InternalException,
          InvalidArgumentException, InsufficientDataException {
    LOG.trace("Putting object '{}' in bucket '{}' ...", objectName, bucketName);
    minioClient.putObject(bucketName, objectName, stream, contentType);
    LOG.debug("Put object '{}' in bucket '{}'", objectName, bucketName);
  }

  /** @see MinioClient#statObject(String, String) */
  ObjectStat statObject(String objectName)
      throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
          IOException, InvalidKeyException, NoResponseException, XmlPullParserException,
          ErrorResponseException, InternalException {
    LOG.trace("Retrieving metadata for object '{}' in bucket '{}' ...", objectName, bucketName);
    ObjectStat objectStat = minioClient.statObject(bucketName, objectName);
    LOG.debug("Retrieved metadata for object '{}' in bucket '{}'", objectName, bucketName);
    return objectStat;
  }

  /** @see MinioClient#removeObject(String, String) */
  void removeObject(String objectName)
      throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
          IOException, InvalidKeyException, NoResponseException, XmlPullParserException,
          ErrorResponseException, InternalException, InvalidArgumentException {
    LOG.trace("Removing object '{}' in bucket '{}' ...", objectName, bucketName);
    minioClient.removeObject(bucketName, objectName);
    LOG.debug("Removed object '{}' in bucket '{}'", objectName, bucketName);
  }

  /** @see io.minio.MinioClient#getObject(java.lang.String, java.lang.String) */
  InputStream getObject(String objectName)
      throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
          IOException, InvalidKeyException, NoResponseException, XmlPullParserException,
          ErrorResponseException, InternalException, InvalidArgumentException {
    LOG.trace("Streaming object '{}' in bucket '{}' ...", objectName, bucketName);
    return minioClient.getObject(bucketName, objectName);
  }
}
