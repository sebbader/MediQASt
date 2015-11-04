package naiveanalyzer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import naivemapper.impl.MappedQuery;

public class MQueryList<T> extends ArrayList<MappedQuery> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	public boolean contains(Object o) {
		MappedQuery mappedQuery = (MappedQuery) o;
		
		Iterator<MappedQuery> iter = super.iterator();
		while (iter.hasNext()) {
			MappedQuery m = iter.next();
			if (m.getQuery().containsAll(mappedQuery.getQuery()) && mappedQuery.getQuery().containsAll(m.getQuery())) {
				return true;
			}
		}
		return false;
	}
	
	public void addDistinct(MappedQuery mappedQuery) {
		if (!contains(mappedQuery)) {
			add(mappedQuery);
		}
	}

}
