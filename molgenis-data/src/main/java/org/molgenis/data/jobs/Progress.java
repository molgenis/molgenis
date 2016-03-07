package org.molgenis.data.jobs;

public interface Progress
{
	void start();
	
	void setProgressMax(int max);

	void progress(int progress, String message);
	
	void status(String message);

	void failed(Exception ex);

	void canceled();

	void success();
	
	Long timeRunning();
}
