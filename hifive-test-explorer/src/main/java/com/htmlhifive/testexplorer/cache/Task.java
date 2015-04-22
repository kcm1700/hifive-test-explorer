package com.htmlhifive.testexplorer.cache;

public class Task implements Runnable {
	private String screenshotId;
	private int priority;

	public String getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(String screenshotId) {
		this.screenshotId = screenshotId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public int hashCode() {
		return screenshotId.hashCode();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
