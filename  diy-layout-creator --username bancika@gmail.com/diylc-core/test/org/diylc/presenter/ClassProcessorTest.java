package org.diylc.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.diylc.common.PropertyWrapper;
import org.diylc.testcomponents.MockDIYComponent;
import org.junit.Test;


public class ClassProcessorTest {

	@Test
	public void testExtractProperties() {
		List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(
				MockDIYComponent.class);
		assertNotNull(properties);
		assertEquals(4, properties.size());
	};
}
