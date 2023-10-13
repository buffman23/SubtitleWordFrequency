package gui;

import java.security.KeyStore.LoadStoreParameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import SubtitleWordFrq.Utils;

public class SortedListModel<E extends Comparable<E>> extends AbstractListModel<E> {
	private List<E> list;
	
	public SortedListModel()
	{
		this(Collections.emptyList());
	}
	
	public SortedListModel(List<E> list)
	{
		list.sort(E::compareTo);
		this.list = list;
	}
	
	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public E getElementAt(int index) {
		return list.get(index);
	}

	public void add(E item)
	{
		int insertIdx = Utils.insertSorted(list, item);
		fireIntervalAdded(this, insertIdx, insertIdx);
	}
	
	public boolean remove(E item)
	{
		int removeIdx = Collections.binarySearch(list, item);
		if(removeIdx < 0) {
			return false;
		}
		list.remove(removeIdx);
		fireIntervalRemoved(this, removeIdx, removeIdx);
		return true;
	}
	
	public E remove(int index)
	{
		return list.remove(index);
	}
	
	public List<E> toList()
	{
		return new ArrayList<E>(list);
	}
	
	public boolean contains(E element)
	{
		return Collections.binarySearch(list, element) >= 0;
	}
	
	public int indexOf(E element)
	{
		return Collections.binarySearch(list, element);
	}
}
