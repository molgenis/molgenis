package org.molgenis.framework.server;

import java.io.Serializable;
import java.util.Date;

public class Token implements Serializable
{
	private static final long serialVersionUID = 1412910190619758373L;
	private String userName;
	private Date expiresAt;
	private Date createdAt;

	public Token(String userName, Date createdAt, Date expiresAt)
	{
		super();
		this.userName = userName;
		this.expiresAt = expiresAt;
		this.createdAt = createdAt;
	}

	public String getUserName()
	{
		return userName;
	}

	public Date getExpiresAt()
	{
		return expiresAt;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

}
