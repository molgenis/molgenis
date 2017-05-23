package org.molgenis.amazon.bucket.meta;

import org.molgenis.data.jobs.model.JobExecutionMetaData;
import org.molgenis.data.jobs.model.JobPackage;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.file.model.FileMetaMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.JobPackage.PACKAGE_JOB;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.XREF;

@Component
public class AmazonBucketJobExecutionMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "AmazonBucketJobExecution";

	public static final String FILE = "file";
	public static final String BUCKET = "bucket";
	public static final String KEY = "key";
	public static final String EXPRESSION = "expression";
	public static final String ACCESS_KEY = "accessKey";
	public static final String SECRET_KEY = "secretKey";
	public static final String TARGET_ENTITY_ID = "targetEntityId";
	public static final String REGION = "region";

	private final FileMetaMetaData fileMetaMetaData;
	private final JobExecutionMetaData jobExecutionMetaData;
	private final JobPackage jobPackage;

	public static final String AMAZON_BUCKET_JOB_TYPE = "AmazonBucketJob";

	@Autowired
	AmazonBucketJobExecutionMetaData(FileMetaMetaData fileMetaMetaData, JobExecutionMetaData jobExecutionMetaData,
			JobPackage jobPackage)
	{
		super(SIMPLE_NAME, PACKAGE_JOB);
		this.fileMetaMetaData = requireNonNull(fileMetaMetaData);
		this.jobExecutionMetaData = requireNonNull(jobExecutionMetaData);
		this.jobPackage = requireNonNull(jobPackage);
	}

	@Override
	public void init()
	{
		setLabel("Amazon Bucket file ingest job execution");
		setExtends(jobExecutionMetaData);
		setPackage(jobPackage);
		addAttribute(BUCKET).setLabel("Bucket name").setDescription("The name of the amazon bucket.")
				.setNillable(false);
		addAttribute(KEY).setLabel("Key").setDescription("Expression to match the file key").setNillable(false);
		addAttribute(EXPRESSION).setDataType(BOOL).setLabel("Is key expression")
				.setDescription("Is the key an expression or an exact match").setNillable(true);
		addAttribute(ACCESS_KEY).setLabel("the access key to be used to login to the amazon bucket").setNillable(false);
		addAttribute(SECRET_KEY).setLabel("the secret key to be used to login to the amazon bucket").setNillable(false);
		addAttribute(REGION).setLabel("Region").setDescription("The region of the amazon bucket.").setNillable(false);
		addAttribute(TARGET_ENTITY_ID).setLabel("Target EntityType ID").setNillable(true);
		addAttribute(FILE).setLabel("File").setDescription("The imported file.").setDataType(XREF)
				.setRefEntity(fileMetaMetaData).setNillable(true);
	}
}
