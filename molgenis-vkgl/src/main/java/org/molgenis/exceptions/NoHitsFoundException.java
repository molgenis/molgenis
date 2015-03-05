package org.molgenis.exceptions;

public class NoHitsFoundException extends Exception
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4859944250729596155L;

	public NoHitsFoundException(){
		
	}
	
	public NoHitsFoundException(String msg){
		super(msg);
	}

}
