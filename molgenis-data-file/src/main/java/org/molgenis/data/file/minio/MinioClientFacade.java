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
import org.xmlpull.v1.XmlPullParserException;

/** Simplified API for {@link io.minio.MinioClient}. */
class MinioClientFacade {
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
    minioClient.putObject(bucketName, objectName, stream, contentType);
  }

  /** @see MinioClient#statObject(String, String) */
  ObjectStat statObject(String objectName)
      throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
          IOException, InvalidKeyException, NoResponseException, XmlPullParserException,
          ErrorResponseException, InternalException {
    return minioClient.statObject(bucketName, objectName);
  }

  /** @see MinioClient#removeObject(String, String) */
  void removeObject(String objectName)
      throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
          IOException, InvalidKeyException, NoResponseException, XmlPullParserException,
          ErrorResponseException, InternalException, InvalidArgumentException {
    minioClient.removeObject(bucketName, objectName);
  }

  /** @see io.minio.MinioClient#getObject(java.lang.String, java.lang.String) */
  InputStream getObject(String objectName)
      throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
          IOException, InvalidKeyException, NoResponseException, XmlPullParserException,
          ErrorResponseException, InternalException, InvalidArgumentException {
    return minioClient.getObject(bucketName, objectName);
  }
}
