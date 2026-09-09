package org.diylc.components.maker;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.AbstractMakerBoard.UsbPortType;
import org.diylc.components.MakerBoardPainter;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class UsbPortTest {

  @Test
  public void testUsbConstants() {
    // Micro-USB
    Assert.assertEquals(7.5d, AbstractMakerBoard.USB_MICRO_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_MICRO_WIDTH.getUnit());
    Assert.assertEquals(5.6d, AbstractMakerBoard.USB_MICRO_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_MICRO_LENGTH.getUnit());
    Assert.assertEquals(0.5d, AbstractMakerBoard.USB_MICRO_OVERHANG.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_MICRO_OVERHANG.getUnit());

    // USB Type-C
    Assert.assertEquals(8.94d, AbstractMakerBoard.USB_C_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_C_WIDTH.getUnit());
    Assert.assertEquals(7.5d, AbstractMakerBoard.USB_C_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_C_LENGTH.getUnit());
    Assert.assertEquals(0.5d, AbstractMakerBoard.USB_C_OVERHANG.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_C_OVERHANG.getUnit());

    // USB Type-A
    Assert.assertEquals(14.5d, AbstractMakerBoard.USB_A_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_A_WIDTH.getUnit());
    Assert.assertEquals(14.0d, AbstractMakerBoard.USB_A_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_A_LENGTH.getUnit());
    Assert.assertEquals(17.5d, AbstractMakerBoard.USB_A_DUAL_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.mm, AbstractMakerBoard.USB_A_DUAL_LENGTH.getUnit());

    // USB Type-B
    Assert.assertEquals(0.45d, AbstractMakerBoard.USB_B_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.in, AbstractMakerBoard.USB_B_WIDTH.getUnit());
    Assert.assertEquals(0.51d, AbstractMakerBoard.USB_B_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.in, AbstractMakerBoard.USB_B_LENGTH.getUnit());
    Assert.assertEquals(0.14d, AbstractMakerBoard.USB_B_OVERHANG.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.in, AbstractMakerBoard.USB_B_OVERHANG.getUnit());

    // Mini-USB
    Assert.assertEquals(0.30d, AbstractMakerBoard.USB_MINI_WIDTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.in, AbstractMakerBoard.USB_MINI_WIDTH.getUnit());
    Assert.assertEquals(0.36d, AbstractMakerBoard.USB_MINI_LENGTH.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.in, AbstractMakerBoard.USB_MINI_LENGTH.getUnit());
    Assert.assertEquals(0.05d, AbstractMakerBoard.USB_MINI_OVERHANG.getValue(), 0.001);
    Assert.assertEquals(SizeUnit.in, AbstractMakerBoard.USB_MINI_OVERHANG.getUnit());
  }

  @Test
  public void testUsbPortTypeEnum() {
    Assert.assertEquals(5, UsbPortType.values().length);
    Assert.assertNotNull(UsbPortType.valueOf("MICRO"));
    Assert.assertNotNull(UsbPortType.valueOf("TYPE_C"));
    Assert.assertNotNull(UsbPortType.valueOf("TYPE_A"));
    Assert.assertNotNull(UsbPortType.valueOf("TYPE_B"));
    Assert.assertNotNull(UsbPortType.valueOf("MINI"));
  }

  @Test
  public void testUsbDrawingMethods() {
    BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    MakerBoardPainter.drawMicroUsb(g2d, 10, 200, 60, 40, "USB");
    MakerBoardPainter.drawMicroUsb(g2d, 100, 200, 40, 60, "USB");
    MakerBoardPainter.drawUsbC(g2d, 10, 260, 60, 40, "Type-C");
    MakerBoardPainter.drawUsbC(g2d, 100, 260, 40, 60, "Type-C");
    MakerBoardPainter.drawUsbA(g2d, 10, 320, 60, 40, "USB 3.0");
    MakerBoardPainter.drawUsbA(g2d, 100, 320, 40, 60, "USB 2.0");
    MakerBoardPainter.drawUsbB(g2d, 200, 10, 60, 40, "USB");
    MakerBoardPainter.drawMetalConnector(g2d, 200, 150, 60, 40, "SHIELD");
    MakerBoardPainter.drawMetalConnector(g2d, 200, 220, 60, 40, null);
    MakerBoardPainter.drawMetalConnector(g2d, 250, 220, 60, 40, "");

    g2d.dispose();
  }
}
