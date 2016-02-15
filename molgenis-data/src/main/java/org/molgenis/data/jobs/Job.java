package org.molgenis.data.jobs;

import java.util.Date;

public class Job implements Comparable<Job>
{
	private String identifier;
	private String entityName;
	private String progressMessage;
	private JobMetaData.Status status;
	private Integer progressInt;
	private Integer progessMax;
	private Date startDate;
	private Date endDate;
	private Date submissionDate;
	private String type;

	private Job(){}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private final Job job = new Job();

		public Builder identifier(String identifier)
		{
			job.identifier = identifier;
			return this;
		}

		public Builder entityName(String entityName)
		{
			job.entityName = entityName;
			return this;
		}

		public Builder progressMessage(String progressMessage)
		{
			job.progressMessage = progressMessage;
			return this;
		}

		public Builder status(JobMetaData.Status status)
		{
			job.status = status;
			return this;
		}

		public Builder progressInt(Integer progressInt)
		{
			job.progressInt = progressInt;
			return this;
		}

		public Builder progressMax(Integer progressMax)
		{
			job.progessMax = progressMax;
			return this;
		}

		public Builder startDate(Date startDate)
		{
			job.startDate = startDate;
			return this;
		}

		public Builder endDate(Date endDate)
		{
			job.endDate = endDate;
			return this;
		}

		public Builder submissionDate(Date submissionDate)
		{
			job.submissionDate = submissionDate;
			return this;
		}

		public Builder type(String type)
		{
			job.type = type;
			return this;
		}

		public Job build()
		{
			return job;
		}
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public String getEntityName()
	{
		return entityName;
	}

	public String getProgressMessage()
	{
		return progressMessage;
	}

	public JobMetaData.Status getStatus()
	{
		return status;
	}

	public Integer getProgressInt()
	{
		return progressInt;
	}

	public Integer getProgessMax()
	{
		return progessMax;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public Date getEndDate()
	{
		return endDate;
	}

	public Date getSubmissionDate()
	{
		return submissionDate;
	}

	public String getType()
	{
		return type;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Job other = (Job) obj;
		if (identifier == null)
		{
			if (other.identifier != null) return false;
		}
		else if (!identifier.equals(other.identifier)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Job [identifier=" + identifier + ", entityName=" + entityName + ", progressMessage=" + progressMessage
				+ ", status=" + status + ", progressInt=" + progressInt + ", progessMax=" + progessMax + ", startDate="
				+ startDate + ", endDate=" + endDate + ", submissionDate=" + submissionDate + ", type=" + type + "]";
	}

	@Override
	public int compareTo(Job other)
	{
		return other.getSubmissionDate().compareTo(this.getSubmissionDate());
	}
}
