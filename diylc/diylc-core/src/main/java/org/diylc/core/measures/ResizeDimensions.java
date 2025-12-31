/*
 *
 *    DIY Layout Creator (DIYLC).
 *    Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 *    This file is part of DIYLC.
 *
 *    DIYLC is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    DIYLC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.diylc.core.measures;

/**
 * Record containing resize dimensions for a component being resized.
 * 
 * @param width the width of the component (in mm or in, depending on configuration)
 * @param height the height of the component (in mm or in, depending on configuration)
 * @param length the length of the component (if it implements IHaveLength), null otherwise
 */
public record ResizeDimensions(Size width, Size height, Size length) {
}

