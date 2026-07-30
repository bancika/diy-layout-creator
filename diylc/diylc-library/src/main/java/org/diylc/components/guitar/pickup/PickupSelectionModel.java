/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.components.guitar.pickup;

import java.util.List;

import org.diylc.components.guitar.AbstractGuitarPickup;

/**
 * Backing model for {@code PickupLibraryDialog}, kept completely free of any Swing dependency so
 * its filtering/pre-fill/preselection behaviour can be unit tested without a UI. The dialog is a
 * thin view over this model: it renders {@link #getResults()}, writes user input back into the
 * filter setters, and reads {@link #getSelectedDefinitionId()} to know what to apply.
 */
public class PickupSelectionModel {

  private final PickupLibrary library;
  private final PickupFormat initialFormat;
  private final String initialDefinitionId;

  private String searchText;
  private PickupFormat formatFilter;
  private String manufacturerFilter;
  private String selectedDefinitionId;

  public PickupSelectionModel(PickupLibrary library, PickupFormat initialFormat, String initialDefinitionId) {
    this.library = library;
    this.initialFormat = initialFormat;
    this.initialDefinitionId = initialDefinitionId;
    this.formatFilter = initialFormat;
    this.selectedDefinitionId = initialDefinitionId;
  }

  /**
   * Builds a model pre-filled from a clicked pickup component, per the task's dialog UX: the
   * format filter defaults to the component's own format, and if the component already
   * references a definition, that definition is preselected.
   */
  public static PickupSelectionModel forComponent(PickupLibrary library, AbstractGuitarPickup component) {
    PickupFormat format = PickupComponentFactory.getFormat(component);
    String existingId = component == null ? null : component.getPickupDefinitionId();
    return new PickupSelectionModel(library, format, existingId);
  }

  /** @return the format the dialog was initially opened with (i.e. the clicked component's own format). */
  public PickupFormat getInitialFormat() {
    return initialFormat;
  }

  /** @return the definition id the dialog was initially opened with, or {@code null}. */
  public String getInitialDefinitionId() {
    return initialDefinitionId;
  }

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }

  /** @return the current format filter, or {@code null} meaning "any format". */
  public PickupFormat getFormatFilter() {
    return formatFilter;
  }

  /** Sets the format filter. The user is always free to clear it ({@code null}) or change it. */
  public void setFormatFilter(PickupFormat formatFilter) {
    this.formatFilter = formatFilter;
  }

  /** @return the current manufacturer filter, or {@code null}/blank meaning "any manufacturer". */
  public String getManufacturerFilter() {
    return manufacturerFilter;
  }

  public void setManufacturerFilter(String manufacturerFilter) {
    this.manufacturerFilter = manufacturerFilter;
  }

  /** @return every definition currently matching the search text and format/manufacturer filters. */
  public List<PickupDefinition> getResults() {
    return library.search(searchText, formatFilter, manufacturerFilter);
  }

  /** @return the distinct manufacturers in the library, for populating a manufacturer filter control. */
  public List<String> getAvailableManufacturers() {
    return library.getManufacturers();
  }

  public String getSelectedDefinitionId() {
    return selectedDefinitionId;
  }

  public void setSelectedDefinitionId(String selectedDefinitionId) {
    this.selectedDefinitionId = selectedDefinitionId;
  }

  /** @return the full selected definition, or {@code null} if nothing is selected or it can't be found. */
  public PickupDefinition getSelectedDefinition() {
    return library.findById(selectedDefinitionId);
  }

  /** @return {@code true} if the given definition is the currently selected one, for row highlighting. */
  public boolean isSelected(PickupDefinition definition) {
    return definition != null && definition.id().equals(selectedDefinitionId);
  }

  public PickupLibrary getLibrary() {
    return library;
  }
}
