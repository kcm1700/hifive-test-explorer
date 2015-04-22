package com.htmlhifive.testexplorer.cache;

import java.util.ArrayList;

import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.event.WorkerEvent;
import com.htmlhifive.testexplorer.event.WorkerEventArgs;

public class Worker implements Runnable {
	private ArrayList<WorkerEvent> listeners = new ArrayList<WorkerEvent>();
	private Task task;
	private ScreenshotRepository screenshotRepo;
	
	public Worker(Task task) {
		this.task = task;
	}
	
	public void Raise(Object sender, WorkerEventArgs args)
	{
	}
	
	public void Subscribe(WorkerEvent obj)
	{
	}
	
	public void Unsubscribe(WorkerEvent obj)
	{
	}
	
	public void run() {
		task.run();
		Raise(this, null);
	}
}
