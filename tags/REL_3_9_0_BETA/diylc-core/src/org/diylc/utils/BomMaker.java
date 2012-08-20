package org.diylc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.presenter.ComponentProcessor;

public class BomMaker {

	private static BomMaker instance;

	public static BomMaker getInstance() {
		if (instance == null) {
			instance = new BomMaker();
		}
		return instance;
	}

	private BomMaker() {
	}

	public List<BomEntry> createBom(List<IDIYComponent<?>> components) {
		Map<String, BomEntry> entryMap = new HashMap<String, BomEntry>();
		for (IDIYComponent<?> component : components) {
			ComponentType type = ComponentProcessor.getInstance()
					.extractComponentTypeFrom(
							(Class<? extends IDIYComponent<?>>) component
									.getClass());
			if (type.getBomPolicy() == BomPolicy.NEVER_SHOW)
				continue;
			String name = component.getName();
			String value = component.getValue() == null ? null : component
					.getValue().toString();
			if ((name != null) && (value != null)) {
				String key = type.getName() + "|" + value;
				if (entryMap.containsKey(key)) {
					BomEntry entry = entryMap.get(key);
					entry.setQuantity(entry.getQuantity() + 1);
					if (type.getBomPolicy() == BomPolicy.SHOW_ALL_NAMES) {
						entry.setName(entry.getName() + ", " + name);
					}				
				} else {
					entryMap.put(key, new BomEntry(type.getName(), type
							.getBomPolicy() == BomPolicy.SHOW_ALL_NAMES ? name
							: type.getName(), value, 1));
				}
			}
		}
		List<BomEntry> bom = new ArrayList<BomEntry>(entryMap.values());
		Collections.sort(bom, new Comparator<BomEntry>() {

			@Override
			public int compare(BomEntry o1, BomEntry o2) {
				int compare = o1.getName().compareToIgnoreCase(o2.getName());
				if (compare != 0) {
					return compare;
				}
				return o1.getValue().compareToIgnoreCase(o2.getValue());
			}
		});
		return bom;
	}
}
