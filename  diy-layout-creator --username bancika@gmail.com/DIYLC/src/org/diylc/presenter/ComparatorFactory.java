package org.diylc.presenter;

import java.util.Comparator;
import java.util.Map;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;

public class ComparatorFactory {

	private static ComparatorFactory instance;

	private Comparator<IDIYComponent<?>> componentNameComparator;
	private Comparator<IDIYComponent<?>> componentZOrderComparator;

	public static ComparatorFactory getInstance() {
		if (instance == null) {
			instance = new ComparatorFactory();
		}
		return instance;
	}

	public Comparator<IDIYComponent<?>> getComponentNameComparator() {
		if (componentNameComparator == null) {
			componentNameComparator = new Comparator<IDIYComponent<?>>() {

				@Override
				public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
					String name1 = o1.getName();
					String name2 = o2.getName();
					if (name1 == null || name2 == null) {
						return 0;
					}
					return name1.compareToIgnoreCase(name2);
				}
			};
		}
		return componentNameComparator;
	}

	public Comparator<IDIYComponent<?>> getComponentZOrderComparator(
			final Map<Class<? extends IDIYComponent>, ComponentType> componentTypeMap) {
		if (componentZOrderComparator == null) {
			componentZOrderComparator = new Comparator<IDIYComponent<?>>() {

				@Override
				public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
					int zIndex1 = componentTypeMap.get(o1.getClass()).getLayer().ordinal();
					int zIndex2 = componentTypeMap.get(o2.getClass()).getLayer().ordinal();
					return new Integer(zIndex1).compareTo(zIndex2);
				}
			};
		}
		return componentZOrderComparator;
	}
}
