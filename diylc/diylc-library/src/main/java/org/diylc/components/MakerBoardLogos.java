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
package org.diylc.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;

/**
 * Vector artwork for the manufacturer logos silkscreened onto maker boards. The paths are traced
 * from the official brand marks and are drawn in white, the way the boards themselves print them.
 *
 * @author Branislav Stojkovic
 */
public class MakerBoardLogos {

  private static final Area RASPBERRY_PI_LOGO_AREA = createRaspberryPiLogoArea();

  private MakerBoardLogos() {}

  /**
   * Helper to draw the official Arduino infinity logo (white infinity symbol with '-' and '+').
   * Default bounding box size is 95.56 x 45.33 px.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate of the logo bounding box
   * @param y Top-left Y coordinate of the logo bounding box
   */
  public static void drawArduinoLogo(Graphics2D g2d, double x, double y) {
    drawArduinoLogo(g2d, x, y, 1.0);
  }

  /**
   * Helper to draw the official Arduino infinity logo with custom scaling.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate of the logo bounding box
   * @param y Top-left Y coordinate of the logo bounding box
   * @param scale Scale factor (1.0 = 95.56 x 45.33 px)
   */
  public static void drawArduinoLogo(Graphics2D g2d, double x, double y, double scale) {
    AffineTransform oldTx = g2d.getTransform();
    g2d.translate(x, y);
    if (scale != 1.0) {
      g2d.scale(scale, scale);
    }

    Path2D.Double infinityBody = new Path2D.Double(Path2D.WIND_EVEN_ODD);
    // Outer loop
    infinityBody.moveTo(47.56, 14.67);
    infinityBody.curveTo(50.00, 12.00, 51.78, 9.78, 54.00, 7.78);
    infinityBody.curveTo(62.22, 0.44, 71.56, -2.44, 82.22, 2.22);
    infinityBody.curveTo(92.22, 6.44, 97.78, 16.89, 95.56, 27.56);
    infinityBody.curveTo(93.56, 37.33, 84.00, 44.89, 73.78, 45.33);
    infinityBody.curveTo(64.22, 45.56, 56.89, 41.33, 50.89, 34.44);
    infinityBody.curveTo(50.00, 33.33, 49.11, 32.22, 48.00, 30.89);
    infinityBody.curveTo(47.11, 31.78, 46.44, 32.67, 46.00, 33.33);
    infinityBody.curveTo(39.56, 41.33, 31.56, 46.00, 20.89, 45.33);
    infinityBody.curveTo(10.44, 44.67, 0.67, 34.89, 0.00, 24.44);
    infinityBody.curveTo(-0.89, 11.11, 9.33, 0.22, 23.33, 0.00);
    infinityBody.curveTo(32.67, 0.00, 40.00, 4.67, 45.78, 12.00);
    infinityBody.curveTo(46.44, 12.89, 47.11, 13.56, 47.78, 14.67);
    infinityBody.closePath();

    // Left hole cutout
    infinityBody.moveTo(24.44, 7.56);
    infinityBody.curveTo(14.67, 7.56, 7.56, 13.78, 6.89, 21.78);
    infinityBody.curveTo(6.22, 28.89, 11.78, 36.00, 19.56, 37.78);
    infinityBody.curveTo(27.33, 39.33, 33.33, 36.22, 38.00, 30.67);
    infinityBody.curveTo(45.11, 22.22, 44.89, 23.56, 38.00, 15.11);
    infinityBody.curveTo(34.22, 10.44, 29.11, 7.78, 24.22, 7.56);
    infinityBody.closePath();

    // Right hole cutout
    infinityBody.moveTo(72.67, 7.33);
    infinityBody.curveTo(71.56, 7.33, 70.67, 7.33, 69.78, 7.33);
    infinityBody.curveTo(61.56, 8.89, 56.44, 14.44, 52.44, 21.56);
    infinityBody.curveTo(52.22, 22.00, 52.22, 23.11, 52.44, 23.56);
    infinityBody.curveTo(56.00, 29.56, 60.22, 34.89, 67.33, 37.11);
    infinityBody.curveTo(74.00, 39.33, 80.00, 37.56, 84.67, 32.22);
    infinityBody.curveTo(88.89, 27.56, 89.78, 22.00, 87.11, 16.44);
    infinityBody.curveTo(84.22, 10.44, 78.89, 7.78, 72.67, 7.33);
    infinityBody.lineTo(72.67, 7.33);
    infinityBody.closePath();

    g2d.setColor(Color.WHITE);
    g2d.fill(infinityBody);

    // Minus sign in left loop
    Path2D.Double minus = new Path2D.Double();
    minus.moveTo(31.33, 24.44);
    minus.curveTo(31.33, 24.44, 31.33, 24.89, 30.89, 24.89);
    minus.lineTo(16.44, 24.89);
    minus.curveTo(16.44, 24.89, 16.00, 24.89, 16.00, 24.44);
    minus.lineTo(16.00, 20.00);
    minus.curveTo(16.00, 20.00, 16.00, 19.56, 16.44, 19.56);
    minus.lineTo(30.89, 19.56);
    minus.curveTo(30.89, 19.56, 31.33, 19.56, 31.33, 20.00);
    minus.lineTo(31.33, 24.44);
    minus.closePath();
    g2d.fill(minus);

    // Plus sign in right loop
    Path2D.Double plus = new Path2D.Double();
    plus.moveTo(78.89, 20.22);
    plus.curveTo(78.89, 20.22, 78.89, 19.78, 78.44, 19.78);
    plus.lineTo(74.22, 19.78);
    plus.curveTo(74.22, 19.78, 73.78, 19.78, 73.78, 19.33);
    plus.lineTo(73.78, 15.11);
    plus.curveTo(73.78, 15.11, 73.78, 14.67, 73.33, 14.67);
    plus.lineTo(68.89, 14.67);
    plus.curveTo(68.89, 14.67, 68.44, 14.67, 68.44, 15.11);
    plus.lineTo(68.44, 19.33);
    plus.curveTo(68.44, 19.33, 68.44, 19.78, 68.00, 19.78);
    plus.lineTo(63.78, 19.78);
    plus.curveTo(63.78, 19.78, 63.33, 19.78, 63.33, 20.22);
    plus.lineTo(63.33, 24.67);
    plus.curveTo(63.33, 24.67, 63.33, 25.11, 63.78, 25.11);
    plus.lineTo(68.00, 25.11);
    plus.curveTo(68.00, 25.11, 68.44, 25.11, 68.44, 25.56);
    plus.lineTo(68.44, 29.78);
    plus.curveTo(68.44, 29.78, 68.44, 30.22, 68.89, 30.22);
    plus.lineTo(73.33, 30.22);
    plus.curveTo(73.33, 30.22, 73.78, 30.22, 73.78, 29.78);
    plus.lineTo(73.78, 25.56);
    plus.curveTo(73.78, 25.56, 73.78, 25.11, 74.22, 25.11);
    plus.lineTo(78.44, 25.11);
    plus.curveTo(78.44, 25.11, 78.89, 25.11, 78.89, 24.67);
    plus.lineTo(78.89, 20.22);
    plus.closePath();
    g2d.fill(plus);

    g2d.setTransform(oldTx);
  }

  /**
   * Helper to draw the Raspberry Pi logo in white using vector paths.
   * Native bounding box is 72.51 x 92.60 px.
   *
   * @param g2d Graphics2D context
   * @param x Top-left X coordinate
   * @param y Top-left Y coordinate
   * @param size Target height in pixels
   */
  public static void drawRaspberryPiLogo(Graphics2D g2d, double x, double y, double size) {
    AffineTransform oldTx = g2d.getTransform();
    g2d.translate(x, y);
    double scale = size / 92.604;
    g2d.scale(scale, scale);

    g2d.setColor(Color.WHITE);
    g2d.fill(RASPBERRY_PI_LOGO_AREA);
    g2d.setTransform(oldTx);
  }

  private static Area createRaspberryPiLogoArea() {
    Path2D.Double p0 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p0.moveTo(19.6295, 0.0014);
    p0.curveTo(19.1612, 0.0159, 18.6569, 0.1889, 18.0849, 0.6402);
    p0.curveTo(16.6837, 0.0999, 15.3251, -0.0878, 14.1102, 1.0122);
    p0.curveTo(12.2342, 0.7689, 11.6245, 1.2710, 11.1626, 1.8572);
    p0.curveTo(10.7507, 1.8487, 8.0807, 1.4339, 6.8564, 3.2603);
    p0.curveTo(3.7794, 2.8963, 2.8070, 5.0701, 3.9089, 7.0973);
    p0.curveTo(3.2805, 8.0702, 2.6293, 9.0313, 4.0989, 10.8859);
    p0.curveTo(3.5790, 11.9189, 3.9013, 13.0394, 5.1259, 14.3954);
    p0.curveTo(4.8027, 15.8475, 5.4379, 16.8720, 6.5775, 17.6704);
    p0.curveTo(6.3644, 19.6575, 8.3997, 20.8128, 9.0075, 21.2245);
    p0.curveTo(9.2408, 22.3823, 9.7272, 23.4749, 12.0520, 24.0790);
    p0.curveTo(12.4354, 25.8045, 13.8326, 26.1024, 15.1856, 26.4646);
    p0.curveTo(10.7138, 29.0639, 6.8789, 32.4840, 6.9049, 40.8755);
    p0.lineTo(6.2500, 42.0440);
    p0.curveTo(1.1224, 45.1622, -3.4908, 55.1843, 3.7230, 63.3304);
    p0.curveTo(4.1942, 65.8804, 4.9845, 67.7119, 5.6881, 69.7390);
    p0.curveTo(6.7405, 77.9072, 13.6088, 81.7320, 15.4203, 82.1843);
    p0.curveTo(18.0749, 84.2063, 20.9021, 86.1249, 24.7280, 87.4689);
    p0.curveTo(28.3346, 91.1885, 32.2420, 92.6062, 36.1706, 92.6039);
    p0.curveTo(36.2282, 92.6039, 36.2868, 92.6045, 36.3445, 92.6039);
    p0.curveTo(40.2731, 92.6063, 44.1803, 91.1887, 47.7870, 87.4689);
    p0.curveTo(51.6128, 86.1249, 54.4401, 84.2063, 57.0947, 82.1843);
    p0.curveTo(58.9062, 81.7319, 65.7745, 77.9072, 66.8269, 69.7390);
    p0.curveTo(67.5305, 67.7119, 68.3208, 65.8803, 68.7920, 63.3304);
    p0.curveTo(76.0057, 55.1837, 71.3926, 45.1609, 66.2650, 42.0427);
    p0.lineTo(65.6090, 40.8744);
    p0.curveTo(65.6350, 32.4834, 61.8002, 29.0634, 57.3284, 26.4634);
    p0.curveTo(58.6814, 26.1014, 60.0786, 25.8034, 60.4619, 24.0779);
    p0.curveTo(62.7867, 23.4737, 63.2732, 22.3811, 63.5065, 21.2234);
    p0.curveTo(64.1143, 20.8118, 66.1495, 19.6564, 65.9365, 17.6693);
    p0.curveTo(67.0759, 16.8708, 67.7113, 15.8463, 67.3880, 14.3943);
    p0.curveTo(68.6127, 13.0383, 68.9350, 11.9177, 68.4150, 10.8847);
    p0.curveTo(69.8849, 9.0309, 69.2328, 8.0698, 68.6052, 7.0970);
    p0.curveTo(69.7063, 5.0698, 68.7346, 2.8959, 65.6566, 3.2600);
    p0.curveTo(64.4326, 1.4335, 61.7634, 1.8484, 61.3506, 1.8569);
    p0.curveTo(60.8887, 1.2707, 60.2793, 0.7685, 58.4032, 1.0119);
    p0.curveTo(57.1883, -0.0882, 55.8298, 0.0995, 54.4285, 0.6399);
    p0.curveTo(52.7646, -0.6731, 51.6635, 0.3794, 50.4059, 0.7773);
    p0.curveTo(48.3914, 0.1191, 47.9309, 1.0207, 46.9410, 1.3879);
    p0.curveTo(44.7440, 0.9236, 44.0764, 1.9344, 43.0232, 3.0012);
    p0.lineTo(41.7982, 2.9769);
    p0.curveTo(38.4844, 4.9297, 36.8382, 8.9061, 36.2548, 10.9503);
    p0.curveTo(35.6710, 8.9057, 34.0286, 4.9293, 30.7155, 2.9769);
    p0.lineTo(29.4904, 3.0012);
    p0.curveTo(28.4359, 1.9344, 27.7683, 0.9236, 25.5714, 1.3879);
    p0.curveTo(24.5815, 1.0207, 24.1222, 0.1191, 22.1064, 0.7773);
    p0.curveTo(21.2810, 0.5162, 20.5218, -0.0267, 19.6279, 0.0010);
    p0.closePath();

    Path2D.Double p1 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p1.moveTo(13.0322, 8.5925);
    p1.curveTo(21.8236, 13.1250, 26.9346, 16.7915, 29.7345, 19.9142);
    p1.curveTo(28.3006, 25.6612, 20.8204, 25.9235, 18.0852, 25.7623);
    p1.curveTo(18.6452, 25.5016, 19.1125, 25.1894, 19.2782, 24.7097);
    p1.curveTo(18.5919, 24.2219, 16.1583, 24.6583, 14.4594, 23.7037);
    p1.curveTo(15.1120, 23.5686, 15.4173, 23.4369, 15.7225, 22.9552);
    p1.curveTo(14.1174, 22.4433, 12.3885, 22.0022, 11.3715, 21.1541);
    p1.curveTo(11.9203, 21.1609, 12.4327, 21.2768, 13.1493, 20.7798);
    p1.curveTo(11.7116, 20.0050, 10.1775, 19.3910, 8.9856, 18.2068);
    p1.curveTo(9.7289, 18.1885, 10.5303, 18.1994, 10.7633, 17.9260);
    p1.curveTo(9.4475, 17.1108, 8.3373, 16.2042, 7.4183, 15.2126);
    p1.curveTo(8.4585, 15.3381, 8.8977, 15.2300, 9.1493, 15.0488);
    p1.curveTo(8.1546, 14.0300, 6.8956, 13.1698, 6.2954, 11.9143);
    p1.curveTo(7.0678, 12.1805, 7.7744, 12.2825, 8.2838, 11.8909);
    p1.curveTo(7.9458, 11.1284, 6.4976, 10.6787, 5.6638, 8.8967);
    p1.curveTo(6.4770, 8.9756, 7.3394, 9.0741, 7.5119, 8.8967);
    p1.curveTo(7.1345, 7.3591, 6.4869, 6.4947, 5.8518, 5.5991);
    p1.curveTo(7.5920, 5.5732, 10.2286, 5.6058, 10.1092, 5.4587);
    p1.lineTo(9.0332, 4.3593);
    p1.curveTo(10.7330, 3.9016, 12.4723, 4.4328, 13.7350, 4.8271);
    p1.curveTo(14.3020, 4.3797, 13.7250, 3.8141, 13.0332, 3.2365);
    p1.curveTo(14.4778, 3.4293, 15.7832, 3.7615, 16.9630, 4.2189);
    p1.curveTo(17.5935, 3.6497, 16.5537, 3.0805, 16.0507, 2.5113);
    p1.curveTo(18.2825, 2.9347, 19.2281, 3.5297, 20.1677, 4.1253);
    p1.curveTo(20.8496, 3.4719, 20.2068, 2.9165, 19.7467, 2.3476);
    p1.curveTo(21.4295, 2.9709, 22.2962, 3.7756, 23.2088, 4.5698);
    p1.curveTo(23.5182, 4.1523, 23.9948, 3.8463, 23.4193, 2.8388);
    p1.curveTo(24.6141, 3.5275, 25.5139, 4.3390, 26.1796, 5.2482);
    p1.curveTo(26.9189, 4.7775, 26.6200, 4.1338, 26.6241, 3.5406);
    p1.curveTo(27.8657, 4.5506, 28.6537, 5.6255, 29.6183, 6.6751);
    p1.curveTo(29.8126, 6.5336, 29.9827, 6.0538, 30.1329, 5.2949);
    p1.curveTo(33.0952, 8.1688, 37.2809, 15.4074, 31.2089, 18.2774);
    p1.curveTo(26.0413, 14.0154, 19.8697, 10.9176, 13.0304, 8.5937);
    p1.closePath();
    p1.moveTo(59.6793, 8.5925);
    p1.curveTo(50.8889, 13.1254, 45.7783, 16.7909, 42.9783, 19.9142);
    p1.curveTo(44.4122, 25.6612, 51.8924, 25.9235, 54.6276, 25.7623);
    p1.curveTo(54.0676, 25.5016, 53.6003, 25.1894, 53.4346, 24.7097);
    p1.curveTo(54.1210, 24.2219, 56.5545, 24.6583, 58.2535, 23.7037);
    p1.curveTo(57.6008, 23.5686, 57.2955, 23.4369, 56.9903, 22.9552);
    p1.curveTo(58.5954, 22.4433, 60.3244, 22.0022, 61.3413, 21.1541);
    p1.curveTo(60.7925, 21.1609, 60.2801, 21.2768, 59.5635, 20.7798);
    p1.curveTo(61.0012, 20.0050, 62.5353, 19.3910, 63.7272, 18.2068);
    p1.curveTo(62.9839, 18.1885, 62.1825, 18.1994, 61.9495, 17.9260);
    p1.curveTo(63.2653, 17.1108, 64.3755, 16.2042, 65.2946, 15.2126);
    p1.curveTo(64.2543, 15.3381, 63.8151, 15.2300, 63.5636, 15.0488);
    p1.curveTo(64.5582, 14.0300, 65.8172, 13.1698, 66.4174, 11.9143);
    p1.curveTo(65.6450, 12.1805, 64.9384, 12.2825, 64.4290, 11.8909);
    p1.curveTo(64.7670, 11.1284, 66.2152, 10.6787, 67.0490, 8.8967);
    p1.curveTo(66.2358, 8.9756, 65.3734, 9.0741, 65.2010, 8.8967);
    p1.curveTo(65.5790, 7.3585, 66.2266, 6.4940, 66.8617, 5.5984);
    p1.curveTo(65.1215, 5.5726, 62.4849, 5.6052, 62.6043, 5.4581);
    p1.lineTo(63.6803, 4.3587);
    p1.curveTo(61.9805, 3.9010, 60.2411, 4.4322, 58.9785, 4.8265);
    p1.curveTo(58.4115, 4.3791, 58.9885, 3.8135, 59.6802, 3.2358);
    p1.curveTo(58.2357, 3.4287, 56.9302, 3.7608, 55.7504, 4.2183);
    p1.curveTo(55.1200, 3.6491, 56.1598, 3.0799, 56.6627, 2.5107);
    p1.curveTo(54.4310, 2.9341, 53.4854, 3.5290, 52.5457, 4.1247);
    p1.curveTo(51.8639, 3.4713, 52.5067, 2.9159, 52.9667, 2.3470);
    p1.curveTo(51.2840, 2.9703, 50.4172, 3.7750, 49.5047, 4.5692);
    p1.curveTo(49.1953, 4.1517, 48.7187, 3.8456, 49.2941, 2.8382);
    p1.curveTo(48.0994, 3.5268, 47.1995, 4.3384, 46.5338, 5.2476);
    p1.curveTo(45.7946, 4.7769, 46.0934, 4.1332, 46.0894, 3.5399);
    p1.curveTo(44.8477, 4.5499, 44.0597, 5.6249, 43.0952, 6.6744);
    p1.curveTo(42.9009, 6.5330, 42.7307, 6.0532, 42.5805, 5.2943);
    p1.curveTo(39.6183, 8.1682, 35.4325, 15.4067, 41.5045, 18.2768);
    p1.curveTo(46.6695, 14.0139, 52.8407, 10.9163, 59.6805, 8.5924);
    p1.closePath();

    Path2D.Double p2 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p2.moveTo(47.0024, 67.1140);
    p2.curveTo(47.0188, 70.5889, 45.0112, 73.8170, 41.7504, 75.5588);
    p2.curveTo(38.4896, 77.3006, 34.4542, 77.3006, 31.1934, 75.5588);
    p2.curveTo(27.9326, 73.8170, 25.9250, 70.5889, 25.9414, 67.1140);
    p2.curveTo(25.9250, 63.6391, 27.9326, 60.4110, 31.1934, 58.6692);
    p2.curveTo(34.4542, 56.9274, 38.4896, 56.9274, 41.7504, 58.6692);
    p2.curveTo(45.0112, 60.4110, 47.0188, 63.6391, 47.0024, 67.1140);
    p2.closePath();
    p2.moveTo(30.3786, 39.3746);
    p2.curveTo(33.2055, 41.2176, 34.6522, 44.7077, 34.1649, 48.5090);
    p2.curveTo(33.6776, 52.3102, 31.3275, 55.8665, 28.0144, 57.8164);
    p2.curveTo(24.7013, 59.7663, 20.9097, 59.8245, 18.0912, 57.9688);
    p2.curveTo(15.2643, 56.1258, 13.8176, 52.6357, 14.3049, 48.8344);
    p2.curveTo(14.7922, 45.0332, 17.1423, 41.4769, 20.4554, 39.5270);
    p2.curveTo(23.7685, 37.5771, 27.5601, 37.5189, 30.3786, 39.3746);
    p2.closePath();

    Path2D.Double p3 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p3.moveTo(42.1557, 38.8570);
    p3.curveTo(37.8215, 41.6966, 37.0284, 48.2301, 40.3987, 53.3303);
    p3.curveTo(43.7691, 58.4306, 50.1089, 60.2909, 54.4431, 57.4513);
    p3.curveTo(58.7773, 54.6117, 59.5704, 48.0782, 56.2001, 42.9780);
    p3.curveTo(52.8297, 37.8777, 46.4899, 36.0174, 42.1557, 38.8570);
    p3.closePath();
    p3.moveTo(8.5710, 44.0484);
    p3.curveTo(13.2819, 42.7856, 10.1614, 63.5377, 6.3284, 61.8350);
    p3.curveTo(2.1120, 58.4438, 0.7540, 48.5122, 8.5710, 44.0484);
    p3.closePath();

    Path2D.Double p4 = new Path2D.Double(Path2D.WIND_NON_ZERO);
    p4.moveTo(63.0123, 43.7896);
    p4.curveTo(58.3009, 42.5272, 61.4219, 63.2802, 65.2549, 61.5775);
    p4.curveTo(69.4712, 58.1859, 70.8292, 48.2535, 63.0123, 43.7896);
    p4.closePath();
    p4.moveTo(47.0060, 28.3359);
    p4.curveTo(55.1360, 26.9631, 61.9009, 31.7933, 61.6279, 40.6091);
    p4.curveTo(61.3605, 43.9888, 44.0109, 28.8391, 47.0061, 28.3359);
    p4.closePath();
    p4.moveTo(24.5436, 28.0771);
    p4.curveTo(16.4130, 26.7043, 9.6488, 31.5356, 9.9218, 40.3506);
    p4.curveTo(10.1892, 43.7301, 27.5388, 28.5804, 24.5436, 28.0771);
    p4.closePath();
    p4.moveTo(36.2218, 26.0212);
    p4.curveTo(31.3695, 25.8950, 26.7127, 29.6225, 26.7014, 31.7845);
    p4.curveTo(26.6879, 34.4115, 30.5378, 37.1013, 36.2548, 37.1695);
    p4.curveTo(42.0931, 37.2113, 45.8185, 35.0165, 45.8372, 32.3055);
    p4.curveTo(45.8585, 29.2338, 40.5274, 25.9737, 36.2217, 26.0211);
    p4.closePath();
    p4.moveTo(36.5181, 79.8920);
    p4.curveTo(40.7486, 79.7073, 46.4253, 81.2546, 46.4364, 83.3072);
    p4.curveTo(46.5066, 85.3003, 41.2882, 89.8037, 36.2375, 89.7166);
    p4.curveTo(31.0069, 89.9423, 25.8778, 85.4319, 25.9449, 83.8686);
    p4.curveTo(25.8666, 81.5764, 32.3140, 79.7867, 36.5181, 79.8919);
    p4.closePath();
    p4.moveTo(20.8923, 67.7271);
    p4.curveTo(23.9043, 71.3559, 25.2773, 77.7311, 22.7637, 79.6104);
    p4.curveTo(20.3857, 81.0450, 14.6109, 80.4542, 10.5063, 74.5576);
    p4.curveTo(7.7381, 69.6096, 8.0949, 64.5746, 10.0385, 63.0956);
    p4.curveTo(12.9450, 61.3250, 17.4358, 63.7165, 20.8925, 67.7271);
    p4.closePath();
    p4.moveTo(51.5410, 66.5770);
    p4.curveTo(48.2822, 70.3940, 46.4676, 77.3560, 48.8449, 79.5982);
    p4.curveTo(51.1179, 81.3402, 57.2199, 81.0967, 61.7272, 74.8425);
    p4.curveTo(65.0002, 70.6421, 63.9035, 63.6272, 62.0340, 61.7643);
    p4.curveTo(59.2569, 59.6163, 55.2701, 62.3653, 51.5410, 66.5759);
    p4.closePath();

    Area area = new Area(p0);
    area.subtract(new Area(p1));
    area.subtract(new Area(p2));
    area.subtract(new Area(p3));
    area.subtract(new Area(p4));

    return area;
  }
}
