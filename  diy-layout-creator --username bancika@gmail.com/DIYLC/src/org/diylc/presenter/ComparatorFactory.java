package org.diylc.presenter;

import java.util.Comparator;
import java.util.List;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;

public class ComparatorFactory {

	private static ComparatorFactory instance;

	private Comparator<IDIYComponent<?>> componentNameComparator;
	private Comparator<ComponentType> componentTypeComparator;

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

	public Comparator<ComponentType> getComponentTypeComparator() {
		if (componentTypeComparator == null) {
			componentTypeComparator = new Comparator<ComponentType>() {

				@Override
				public int compare(ComponentType o1, ComponentType o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			};
		}
		return componentTypeComparator;
	}

	public Comparator<IDIYComponent<?>> getComponentZOrderComparator(
			final List<IDIYComponent<?>> components) {
		Comparator<IDIYComponent<?>> componentZOrderComparator = new Comparator<IDIYComponent<?>>() {

			@Override
			public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
				int index1 = components.indexOf(o1);
				int index2 = components.indexOf(o2);
				return new Integer(index1).compareTo(index2);
			}
		};
		return componentZOrderComparator;
	}
}
