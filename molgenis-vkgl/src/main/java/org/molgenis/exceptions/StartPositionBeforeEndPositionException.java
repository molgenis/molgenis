package org.molgenis.exceptions;

public class StartPositionBeforeEndPositionException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3729846119486729869L;
	
	public StartPositionBeforeEndPositionException(){
		
	}
	public StartPositionBeforeEndPositionException(String msg){
		super(msg);
	}
}
