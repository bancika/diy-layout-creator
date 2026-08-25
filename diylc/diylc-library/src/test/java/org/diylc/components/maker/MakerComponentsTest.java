/*
 * 
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.maker;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.diylc.common.ComponentType;
import org.diylc.common.DefaultComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.displays.*;
import org.diylc.components.electromechanical.*;
import org.diylc.components.micro.*;
import org.diylc.components.modules.*;
import org.diylc.components.robotics.*;
import org.diylc.components.sensors.*;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.presenter.ComponentProcessor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.diylc.appframework.miscutils.ConfigurationManager;

public class MakerComponentsTest {

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  private final List<Class<? extends IDIYComponent<?>>> allMakerComponentClasses = Arrays.<Class<? extends IDIYComponent<?>>>asList(
      // Controllers
      ArduinoUno.class,
      ArduinoNano.class,
      ArduinoMega.class,
      RaspberryPi.class,
      RaspberryPiZero.class,
      RaspberryPiPico.class,
      ESP32DevKit.class,
      ESP8266NodeMCU.class,
      WemosD1Mini.class,

      // Sensors
      UltrasonicSensor.class,
      PIRMotionSensor.class,
      DHTSensor.class,
      MPU6050.class,
      LDRSensorModule.class,
      BME280Sensor.class,
      SoilMoistureSensor.class,
      TCRT5000Sensor.class,
      IRReceiverModule.class,

      // Displays & Outputs
      CharacterLCD.class,
      OLEDDisplay.class,
      SevenSegmentDisplay.class,
      LEDMatrix8x8.class,
      TFTDisplay.class,
      WS2812BStick.class,
      WS2812BRing.class,

      // Modules & Breakouts
      LogicLevelConverter.class,
      L298NMotorDriver.class,
      A4988StepperDriver.class,
      RelayModule.class,
      SDCardModule.class,
      RTCModule.class,
      MB102PowerSupply.class,
      TP4056Charger.class,
      LM2596BuckConverter.class,
      MT3608BoostConverter.class,
      AnalogJoystickKY023.class,
      RotaryEncoderKY040.class,
      NRF24L01Transceiver.class,
      HC05Bluetooth.class,
      RFIDRC522.class,
      ULN2003Driver.class,
      PCA9685ServoDriver.class,
      MOSFETSwitchModule.class,
      ActiveBuzzerModule.class,
      GPSModuleNEO6M.class,
      INA219CurrentSensor.class,

      // Electro-Mechanical
      BatteryHolder18650.class,

      // Robotics
      MicroServoSG90.class,
      NEMA17Stepper.class,
      StepperMotor28BYJ48.class,
      DCHobbyMotor.class
  );

  @Test
  public void testAllComponentDiscoveryAndCategories() {
    Map<String, List<ComponentType>> categories = ComponentProcessor.getInstance().getComponentTypes();

    Assert.assertTrue("Categories should contain 'Controllers'", categories.containsKey("Controllers"));
    Assert.assertTrue("Categories should contain 'Sensors'", categories.containsKey("Sensors"));
    Assert.assertTrue("Categories should contain 'Displays & Outputs'", categories.containsKey("Displays & Outputs"));
    Assert.assertTrue("Categories should contain 'Modules & Breakouts'", categories.containsKey("Modules & Breakouts"));
    Assert.assertTrue("Categories should contain 'Robotics'", categories.containsKey("Robotics"));

    for (Class<? extends IDIYComponent<?>> clazz : allMakerComponentClasses) {
      ComponentType type = ComponentProcessor.getInstance().extractComponentTypeFrom(clazz);
      Assert.assertNotNull("Component type should be extracted for " + clazz.getSimpleName(), type);
      Assert.assertNotNull("Icon should be non-null for " + clazz.getSimpleName(), type.getIcon());
      Assert.assertNotNull("Category should be non-null for " + clazz.getSimpleName(), type.getCategory());
      Assert.assertNotNull("Name should be non-null for " + clazz.getSimpleName(), type.getName());
    }
  }

  @Test
  public void testComponentInstantiationAndControlPoints() throws Exception {
    for (Class<? extends IDIYComponent<?>> clazz : allMakerComponentClasses) {
      IDIYComponent<?> component = clazz.getDeclaredConstructor().newInstance();
      Assert.assertNotNull("Component instance must not be null", component);
      int count = component.getControlPointCount();
      Assert.assertTrue("Component " + clazz.getSimpleName() + " must have at least 1 control point", count > 0);

      for (int i = 0; i < count; i++) {
        Point2D p = component.getControlPoint(i);
        Assert.assertNotNull("Control point " + i + " must not be null for " + clazz.getSimpleName(), p);
        String name = component.getControlPointNodeName(i);
        Assert.assertNotNull("Node name for point " + i + " must not be null for " + clazz.getSimpleName(), name);
        Assert.assertFalse("Node name for point " + i + " must not be empty for " + clazz.getSimpleName(), name.trim().isEmpty());
      }
    }
  }

  @Test
  public void testComponentDrawingAndOutlineMode() throws Exception {
    BufferedImage img = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setClip(new Rectangle(0, 0, 600, 600));

    Project project = new Project();
    IDrawingObserver observer = new IDrawingObserver() {
      @Override public void startTracking() {}
      @Override public void stopTracking() {}
      @Override public void startTrackingContinuityArea(boolean positive) {}
      @Override public void stopTrackingContinuityArea() {}
      @Override public boolean isTrackingContinuityArea() { return false; }
      @Override public void setContinuityMarker(String marker) {}
    };

    for (Class<? extends IDIYComponent<?>> clazz : allMakerComponentClasses) {
      IDIYComponent<?> component = clazz.getDeclaredConstructor().newInstance();
      component.setControlPoint(new Point2D.Double(200, 200), 0);

      // Normal mode
      component.draw(g2d, ComponentState.NORMAL, false, project, observer);
      // Selected mode
      component.draw(g2d, ComponentState.SELECTED, false, project, observer);
      // Outline mode
      component.draw(g2d, ComponentState.NORMAL, true, project, observer);
      // Icon drawing
      component.drawIcon(g2d, 32, 32);
    }
    g2d.dispose();
  }

  @Test
  public void testCachingBounds() throws Exception {
    for (Class<? extends IDIYComponent<?>> clazz : allMakerComponentClasses) {
      IDIYComponent<?> component = clazz.getDeclaredConstructor().newInstance();
      component.setControlPoint(new Point2D.Double(200, 200), 0);

      java.awt.geom.Rectangle2D bounds = component.getCachingBounds();
      Assert.assertNotNull("Caching bounds must not be null for " + clazz.getSimpleName(), bounds);
      Assert.assertTrue("Caching bounds width must be > 0 for " + clazz.getSimpleName(), bounds.getWidth() > 0);
      Assert.assertTrue("Caching bounds height must be > 0 for " + clazz.getSimpleName(), bounds.getHeight() > 0);
    }
  }

  @Test
  public void testComponentRotation() throws Exception {
    DefaultComponentTransformer transformer = new DefaultComponentTransformer();
    Point2D center = new Point2D.Double(200, 200);

    for (Class<? extends IDIYComponent<?>> clazz : allMakerComponentClasses) {
      IDIYComponent<?> component = clazz.getDeclaredConstructor().newInstance();
      component.setControlPoint(new Point2D.Double(200, 200), 0);

      Assert.assertTrue("Component " + clazz.getSimpleName() + " should be rotatable", transformer.canRotate(component));

      // Rotate 4 times clockwise (360 degrees back to original)
      for (int r = 0; r < 4; r++) {
        transformer.rotate(component, center, 1);
      }
    }
  }

  @Test
  public void testCanPointMoveFreelyReturnsFalse() throws Exception {
    for (Class<? extends IDIYComponent<?>> clazz : allMakerComponentClasses) {
      IDIYComponent<?> component = clazz.getDeclaredConstructor().newInstance();
      int count = component.getControlPointCount();
      for (int i = 0; i < count; i++) {
        Assert.assertFalse("canPointMoveFreely(" + i + ") must return false for " + clazz.getSimpleName(),
            component.canPointMoveFreely(i));
      }
    }
  }

  @Test
  public void testControlPointsAreDistinct() throws Exception {
    for (Class<? extends IDIYComponent<?>> clazz : allMakerComponentClasses) {
      IDIYComponent<?> component = clazz.getDeclaredConstructor().newInstance();
      int count = component.getControlPointCount();
      for (int i = 0; i < count; i++) {
        Point2D p1 = component.getControlPoint(i);
        for (int j = i + 1; j < count; j++) {
          Point2D p2 = component.getControlPoint(j);
          Assert.assertTrue("Duplicate control point coordinates detected between point " + i + " and point " + j + " in " + clazz.getSimpleName(),
              p1.distance(p2) > 0.001);
        }
      }
    }
  }
}
