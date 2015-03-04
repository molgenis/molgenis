package org.molgenis.exceptions;

public class MalformedQueryException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	MalformedQueryException(){
		
	}
	public MalformedQueryException(String msg){
		super(msg);
	}
}
