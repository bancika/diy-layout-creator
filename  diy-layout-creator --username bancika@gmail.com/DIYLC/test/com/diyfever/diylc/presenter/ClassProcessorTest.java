package com.diyfever.diylc.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.diylc.common.ControlPointWrapper;
import org.diylc.common.PropertyWrapper;
import org.diylc.presenter.ComponentProcessor;
import org.junit.Test;

import com.diyfever.diylc.components.MockComponentInstance;

public class ClassProcessorTest {

	@Test
	public void testExtractProperties() {
		List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(
				MockComponentInstance.class);
		assertNotNull(properties);
		assertEquals(4, properties.size());
	};

	@Test
	public void testExtractControlPoints() {
		List<ControlPointWrapper> controlPoints = ComponentProcessor.getInstance()
				.extractControlPoints(MockComponentInstance.class);
		assertNotNull(controlPoints);
		assertEquals(1, controlPoints.size());
	};
}
