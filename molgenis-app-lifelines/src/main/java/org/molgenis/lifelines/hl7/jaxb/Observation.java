package org.molgenis.lifelines.hl7.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class Observation
{
	private String classCode;
	private String moodCode;
	private Code code;

	@XmlAttribute
	public String getClassCode()
	{
		return classCode;
	}

	public void setClassCode(String classCode)
	{
		this.classCode = classCode;
	}

	@XmlAttribute
	public String getMoodCode()
	{
		return moodCode;
	}

	public void setMoodCode(String moodCode)
	{
		this.moodCode = moodCode;
	}

	public Code getCode()
	{
		return code;
	}

	public void setCode(Code code)
	{
		this.code = code;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Observation [classCode=").append(classCode).append(", moodCode=").append(moodCode)
				.append(", code=").append(code).append("]");
		return builder.toString();
	}
}
