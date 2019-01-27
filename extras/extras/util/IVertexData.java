package util;

import java.util.Map;

/**
 * Interface for any class that stores and processes vertex attributes Each
 * attribute will have a unique name.
 */
public interface IVertexData {
  /**
   * Query if attribute by the given name is present
   *
   * @param attribName the name of the attribute that is being queried
   * @return true if data for this name is present, false otherwise
   */
  boolean hasData(String attribName);

  /**
   * Returns the data for the supplied attribute name as a float array, for
   * maximum flexibility
   *
   * @param attribName the (unique) name of the attribute
   * @return the attribute data as a float array
   * @throws IllegalArgumentException if no attribute by the given name is
   *                                  found
   */
  float[] getData(String attribName) throws IllegalArgumentException;

  /**
   * set the data for the given attribute. If attribute is not already present,
   * it will be added now. If data is already present for the attribute, it will
   * be overwritten
   *
   * @param attribName the name of the attribute to be added/set
   * @param data       the data as an array of floats for maximum flexibility
   * @throws IllegalArgumentException if an implementation does not support that
   *                                  attribute
   */
  void setData(String attribName, float[] data) throws IllegalArgumentException;

  /**
   * Get a list of all the attribute names supported by an implementation
   *
   * @return an array of String objects storing the name of all supported
   * attribute names
   */
  String[] getAllAttributes();


}
