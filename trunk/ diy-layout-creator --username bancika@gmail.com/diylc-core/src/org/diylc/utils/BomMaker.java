package org.diylc.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Map<String, BomEntry> entryMap = new LinkedHashMap<String, BomEntry>();
		List<IDIYComponent<?>> sortedComponents = new ArrayList<IDIYComponent<?>>(
				components);
		Collections.sort(sortedComponents, new Comparator<IDIYComponent<?>>() {

			@Override
			public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
				String name1 = o1.getName();
				String name2 = o2.getName();
				Pattern p = Pattern.compile("(\\D+)(\\d+)");
				Matcher m1 = p.matcher(name1);
				Matcher m2 = p.matcher(name2);
				if (m1.matches() && m2.matches()) {
					String prefix1 = m1.group(1);
					int value1 = Integer.parseInt(m1.group(2));
					String prefix2 = m2.group(1);
					int value2 = Integer.parseInt(m2.group(2));
					int compare = prefix1.compareToIgnoreCase(prefix2);
					if (compare != 0) {
						return compare;
					}
					return new Integer(value1).compareTo(value2);
				}				
				return 0;
			}			
		});
		for (IDIYComponent<?> component : sortedComponents) {
			ComponentType type = ComponentProcessor.getInstance()
					.extractComponentTypeFrom(
							(Class<? extends IDIYComponent<?>>) component
									.getClass());
			if (type.getBomPolicy() == BomPolicy.NEVER_SHOW)
				continue;
			String name = component.getName();
			String value = component.getValueForDisplay();
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
		return bom;
	}
}
