package com.diyfever.diylc.plugins.file;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.presenter.ComponentProcessor;

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
			String name = ComponentProcessor.getInstance().extractBomName(component);
			String value = ComponentProcessor.getInstance().extractBomValue(component);
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
