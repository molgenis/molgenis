package org.molgenis.lifelines.hl7.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class Code
{
	private String code;
	private String codeSystem;
	private String codeSystemName;
	private String displayName;

	@XmlAttribute
	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	@XmlAttribute
	public String getCodeSystem()
	{
		return codeSystem;
	}

	public void setCodeSystem(String codeSystem)
	{
		this.codeSystem = codeSystem;
	}

	@XmlAttribute
	public String getCodeSystemName()
	{
		return codeSystemName;
	}

	public void setCodeSystemName(String codeSystemName)
	{
		this.codeSystemName = codeSystemName;
	}

	@XmlAttribute
	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Code [code=").append(code).append(", codeSystem=").append(codeSystem)
				.append(", codeSystemName=").append(codeSystemName).append(", displayName=").append(displayName)
				.append("]");
		return builder.toString();
	}
}
