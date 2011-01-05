package org.diylc.presenter;

import java.util.Comparator;

import org.diylc.core.IDIYComponent;

public class ComparatorFactory {

	private static ComparatorFactory instance;

	private Comparator<IDIYComponent> componentNameComparator;

	public static ComparatorFactory getInstance() {
		if (instance == null) {
			instance = new ComparatorFactory();
		}
		return instance;
	}

	public Comparator<IDIYComponent> getComponentNameComparator() {
		if (componentNameComparator == null) {
			componentNameComparator = new Comparator<IDIYComponent>() {

				@Override
				public int compare(IDIYComponent o1, IDIYComponent o2) {
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
}
