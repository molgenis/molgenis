package org.molgenis.gavin.job;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.gavin.job.input.model.LineType;

import static org.molgenis.gavin.job.input.model.LineType.*;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.*;

public class GavinJobExecution extends JobExecution
{
	private static final long serialVersionUID = 1L;
	private static final String GAVIN = "gavin";

	public GavinJobExecution(Entity entity)
	{
		super(entity);
		setType(GAVIN);
	}

	public GavinJobExecution(EntityType entityType)
	{
		super(entityType);
		setType(GAVIN);
	}

	public GavinJobExecution(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
		setType(GAVIN);
	}

	public String getFilename()
	{
		return getString(FILENAME);
	}

	public void setFilename(String filename)
	{
		set(FILENAME, filename);
	}

	public void setLineTypes(Multiset<LineType> lineTypes)
	{
		set(COMMENTS, lineTypes.count(COMMENT));
		set(VCFS, lineTypes.count(VCF));
		set(CADDS, lineTypes.count(CADD));
		set(ERRORS, lineTypes.count(ERROR));
		set(SKIPPEDS, lineTypes.count(SKIPPED));
	}

	public Multiset<LineType> getLineTypes()
	{
		ImmutableMultiset.Builder<LineType> builder = ImmutableMultiset.builder();
		builder.addCopies(COMMENT, getInt(COMMENTS));
		builder.addCopies(VCF, getInt(VCFS));
		builder.addCopies(CADD, getInt(CADDS));
		builder.addCopies(ERROR, getInt(ERRORS));
		builder.addCopies(SKIPPED, getInt(SKIPPEDS));
		return builder.build();
	}

}
