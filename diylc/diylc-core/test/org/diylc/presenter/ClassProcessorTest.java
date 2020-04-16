/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
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
    List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(MockDIYComponent.class);
    assertNotNull(properties);
    assertEquals(5, properties.size());
  };
}
