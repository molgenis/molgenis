package org.molgenis.file.ingest.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.molgenis.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

import static org.molgenis.file.ingest.amazon.BucketUtils.getClient;
import static org.molgenis.file.ingest.amazon.BucketUtils.handleAmazonClientException;
import static org.molgenis.file.ingest.amazon.BucketUtils.handleAmazonServiceException;

/*
*
* Install the amazon web services command line interface Instruction here: https://aws.amazon.com/cli/
*
* Configure the awscli: aws configure --profile [PROFILENAME]
* Fill out AWS Access Key ID en AWS Secret Access Key. (you should have received these keys with your account)
* Default region name en Default output format can be left empty, default values are used in this case.
*
**/

public class AmazonBucketUploader
{
	private static String bucketName = "com.cartagenia.consortium.vkgl";
	private static String keyName = "lab1/testfile17feb.txt";
	private static String uploadFileName = "testfile17feb.txt";

	public static void main(String[] args) throws IOException
	{
		AmazonS3 s3client = getClient("bcharbon");
		try
		{
			System.out.println("Uploading a new object to S3 from a file\n");
			File file = ResourceUtils.getFile(uploadFileName);

			// Request server-side encryption.
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
			PutObjectRequest putRequest = new PutObjectRequest(bucketName, keyName, file);
			putRequest.setMetadata(objectMetadata);

			s3client.putObject(putRequest);

		}
		catch (AmazonServiceException ase)
		{
			handleAmazonServiceException(ase);
		}
		catch (AmazonClientException ace)
		{
			handleAmazonClientException(ace);
		}
	}
}