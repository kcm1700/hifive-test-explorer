package com.htmlhifive.testexplorer.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Comparator;

import com.htmlhifive.testexplorer.event.WorkerEvent;
import com.htmlhifive.testexplorer.event.WorkerEventArgs;

public class Scheduler implements WorkerEvent {
	private ArrayList<Worker> workers = new ArrayList<Worker>();
	private HashSet<Task> queueMap = new HashSet<Task>();
	private PriorityQueue<Task> queue = new PriorityQueue<Task>(10, new Comparator<Task>(){
		public int compare(Task lhs, Task rhs) {
			return 0;
		}
	});
	
	public void AddTask(Task task)
	{
		// check if there's the task already.
		// 만약 있으면 priority를 조정하고 바로 리턴
		// 없으면 추가.
		
	}
	
	public void CancelTask(Task task)
	{
		
	}

	@Override
	public void workerFinished(Object sender, WorkerEventArgs args) {
	}
}
