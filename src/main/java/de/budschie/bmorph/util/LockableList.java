package de.budschie.bmorph.util;

import java.util.ArrayList;
import java.util.List;

public class LockableList<T>
{
	private boolean locked = false;
	private List<T> list;
	private ArrayList<T> addAfterUnlock = new ArrayList<>();
	private ArrayList<T> removeAfterUnlock = new ArrayList<>();
	
	public LockableList()
	{
		this.list = new ArrayList<>();
	}
	
	public LockableList(List<T> list)
	{
		this.list = list;
	}
	
	public List<T> getList()
	{
		return list;
	}
	
	public void safeAdd(T toAdd)
	{
		if(locked)
		{
			this.addAfterUnlock.add(toAdd);
		}
		else
		{
			this.list.add(toAdd);
		}
	}
	
	public void safeRemove(T toRemove)
	{
		if(locked)
		{
			this.removeAfterUnlock.add(toRemove);
		}
		else
		{
			this.list.remove(toRemove);
		}
	}
	
	public void lock()
	{
		this.locked = true;
	}
	
	public void unlock()
	{
		this.locked = false;
		
		for(T removable : removeAfterUnlock)
			list.remove(removable);
		
		for(T addable : addAfterUnlock)
			list.add(addable);
		
		addAfterUnlock.clear();
		removeAfterUnlock.clear();
	}
	
	public boolean isLocked()
	{
		return locked;
	}
}
