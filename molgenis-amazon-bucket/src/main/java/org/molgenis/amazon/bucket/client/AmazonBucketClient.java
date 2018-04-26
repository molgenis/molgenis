package org.molgenis.amazon.bucket.client;

import com.amazonaws.services.s3.AmazonS3;
import org.molgenis.data.file.FileStore;

import java.io.File;
import java.io.IOException;

public interface AmazonBucketClient
{
	AmazonS3 getClient(String accessKey, String secretKey, String region);

	File downloadFile(AmazonS3 s3Client, FileStore fileStore, String jobIdentifier, String bucketName, String keyName,
			String extension, boolean isExpression, String targetEntityType) throws IOException;
}
