package com.diyfever.diylc.plugins.file;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.model.annotations.BomName;
import com.diyfever.diylc.model.annotations.BomValue;

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

	public List<BomEntry> createBom(List<IComponentInstance> components) {
		Map<String, BomEntry> entryMap = new HashMap<String, BomEntry>();
		for (IComponentInstance component : components) {
			String name = null;
			String value = null;
			for (Method method : component.getClass().getMethods()) {
				if (method.isAnnotationPresent(BomName.class)) {
					try {
						name = method.invoke(component).toString();
					} catch (Exception e) {
						name = null;
					}
				}
				if (method.isAnnotationPresent(BomValue.class)) {
					try {
						value = method.invoke(component).toString();
					} catch (Exception e) {
						value = null;
					}
				}
			}
			if ((name != null) && (value != null)) {
				String key = name + "|" + value;
				if (entryMap.containsKey(key)) {
					BomEntry entry = entryMap.get(key);
					entry.setQuantity(entry.getQuantity() + 1);
				} else {
					entryMap.put(key, new BomEntry(name, value, 1));
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
