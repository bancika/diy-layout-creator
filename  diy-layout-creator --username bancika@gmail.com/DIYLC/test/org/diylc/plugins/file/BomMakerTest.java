package org.diylc.plugins.file;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.diylc.components.MockDIYComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.plugins.file.BomEntry;
import org.diylc.plugins.file.BomMaker;
import org.junit.Test;


public class BomMakerTest {

	@Test
	public void testCreateBom() {
		List<IDIYComponent> components = new ArrayList<IDIYComponent>();
		MockDIYComponent c1 = new MockDIYComponent();
		c1.setC(new Capacitance(3d, CapacitanceUnit.pF));
		components.add(c1);
		components.add(new MockDIYComponent());
		components.add(new MockDIYComponent());
		List<BomEntry> bom = BomMaker.getInstance().createBom(components);
		assertNotNull(bom);
		assertEquals(2, bom.size());
		BomEntry entry1 = bom.get(0);
		assertEquals("something", entry1.getName());
		assertEquals(2, (int) entry1.getQuantity());
		BomEntry entry2 = bom.get(1);
		assertEquals("something", entry2.getName());
		assertEquals(1, (int) entry2.getQuantity());
	}
}
