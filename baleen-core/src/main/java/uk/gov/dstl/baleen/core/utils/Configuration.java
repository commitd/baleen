// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.core.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A helper to deal with configuration in its basic map representation.
 *
 * <p>Configuration can be accessed by a path representation e.g.:
 *
 * <pre>
 * "logging.enabled"
 * </pre>
 *
 * <p>This class also contains helper methods to get a type safe value from the configuration.
 */
public interface Configuration {

  /**
   * Get the path as a list of items.
   *
   * @param path
   * @return list or empty list if doesn't exist
   */
  <T> List<T> getAsList(String path);

  /**
   * Get the list of objects as a list of maps.
   *
   * @param path
   * @return list of maps, empty if doesn't exist.
   */
  List<Map<String, Object>> getAsListOfMaps(String path);

  /**
   * Get a value as a path.
   *
   * <p>Note type unsafe conversion (so may through a runtime exception)
   *
   * @param path
   * @return
   */
  <T> Optional<T> get(String path);

  /**
   * Get a value from a path, returning returning default value if missing.
   *
   * @param path
   * @param defaultValue
   * @return
   */
  <T> T get(String path, T defaultValue);

  /**
   * @return the original config, as a string
   * @throws Exception if original can not be obtained
   */
  String originalConfig() throws IOException;
}
