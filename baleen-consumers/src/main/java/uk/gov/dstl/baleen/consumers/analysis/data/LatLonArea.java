// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.consumers.analysis.data;

/** Unit of area, with center-point Lat lon coordinate */
public class LatLonArea extends LatLon implements Comparable<LatLonArea> {

  private final double area;

  /**
   * @param lat Center-Point Latitude
   * @param lon Center-Point Longitude
   * @param area The Area
   */
  public LatLonArea(final double lat, final double lon, final double area) {
    super(lat, lon);
    this.area = area;
  }

  /** @return The Area */
  public double getArea() {
    return area;
  }

  @Override
  public int compareTo(final LatLonArea o) {
    // Largest area to smallest
    return Double.compare(o.getArea(), getArea());
  }
}
