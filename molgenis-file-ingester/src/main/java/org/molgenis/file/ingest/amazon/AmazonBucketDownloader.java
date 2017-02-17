package org.molgenis.file.ingest.amazon;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;

import static org.molgenis.file.ingest.amazon.BucketUtils.*;

public class AmazonBucketDownloader
{
	private static String bucketName = "com.cartagenia.consortium.vkgl";
	private static String key = "lab1/testfile17feb.txt";

	/*
	*
	* Install the amazon web services command line interface Instruction here: https://aws.amazon.com/cli/
	*
	* Configure the awscli: aws configure --profile [PROFILENAME]
	* Fill out AWS Access Key ID en AWS Secret Access Key. (you should have received these keys with your account)
	* Default region name en Default output format can be left empty, default values are used in this case.
	*
	**/

	public static void main(String[] args) throws IOException
	{
		downloadFile();
	}

	private static void downloadFile() throws IOException
	{
		AmazonS3 s3Client = getClient("bcharbon");

		try
		{
			System.out.println("Downloading an object...");
			S3Object s3object = s3Client.getObject(new GetObjectRequest(bucketName, key));
			System.out.println("Content-Type: " + s3object.getObjectMetadata().getContentType());
			displayTextInputStream(s3object.getObjectContent());

			// Get a range of bytes from an object.

			GetObjectRequest rangeObjectRequest = new GetObjectRequest(bucketName, key);
			rangeObjectRequest.setRange(0, 20);
			S3Object objectPortion = s3Client.getObject(rangeObjectRequest);

			System.out.println("Printing bytes retrieved...");
			displayTextInputStream(objectPortion.getObjectContent());

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