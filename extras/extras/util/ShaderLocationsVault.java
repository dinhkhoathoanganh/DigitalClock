package util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a table of shader variables and their values. A shader
 * program would put them here, and the rendering code can use them whenever it
 * can
 */
public class ShaderLocationsVault {
  private Map<String, Integer> vars;

  public ShaderLocationsVault() {
    vars = new HashMap<String, Integer>();
  }

  /**
   * Add a new shader variable and location
   */
  public void add(String var, Integer location) {
    vars.put(var, location);
  }

  /**
   * Return the location of a shader variable, else return -1
   *
   * @param var the shader variable name whose location is being sought
   * @return the location if found, else -1
   */
  public Integer getLocation(String var) {
    if (vars.containsKey(var))
      return vars.get(var);
    else
      return -1;
  }
}
