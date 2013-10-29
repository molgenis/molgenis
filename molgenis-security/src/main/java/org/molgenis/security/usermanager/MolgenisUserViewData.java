package org.molgenis.security.usermanager;

public class MolgenisUserViewData
{
	private Integer id =  null;
	private String username =  null;
	
	MolgenisUserViewData(Integer id, String username){
		this.id = id;
		this.username = username;
	}
	
	public Integer getId()
	{
		return id;
	}
	
	public void setId(Integer id)
	{
		this.id = id;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
}
