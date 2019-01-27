import org.joml.Vector4f;

/**
 * This is an extension of the VertexAttrib with one extra attribute: color as a
 * 4-tuple
 */
public class VertexAttribWithColor implements util.IVertexData {
  private Vector4f position, color;


  @Override
  public boolean hasData(String attribName) {
    switch (attribName) {
      case "position":
      case "color":
        return true;
      default:
        return false;
    }
  }

  @Override
  public float[] getData(String attribName) throws IllegalArgumentException {
    float[] result;
    switch (attribName) {
      case "position":
        result = new float[4];
        result[0] = position.x;
        result[1] = position.y;
        result[2] = position.z;
        result[3] = position.w;
        return result;
      case "color":
        result = new float[4];
        result[0] = color.x;
        result[1] = color.y;
        result[2] = color.z;
        result[3] = color.w;
        return result;
      default:
        throw new IllegalArgumentException("No attribute: " + attribName + " found!");
    }
  }

  @Override
  public void setData(String attribName, float[] data) throws IllegalArgumentException {
    switch (attribName) {
      case "position":
        position = new Vector4f(0, 0, 0, 1);
        switch (data.length) {
          case 4:
            position.w = data[3];
          case 3:
            position.z = data[2];
          case 2:
            position.y = data[1];
          case 1:
            position.x = data[0];
            break;
          default:
            throw new IllegalArgumentException("Too much data for attribute: " + attribName);
        }
        break;
      case "color":
        color = new Vector4f(0, 0, 0, 1);
        switch (data.length) {
          case 4:
            color.w = data[3];
          case 3:
            color.z = data[2];
          case 2:
            color.y = data[1];
          case 1:
            color.x = data[0];
            break;
          default:
            throw new IllegalArgumentException("Too much data for attribute: " + attribName);
        }
        break;

      default:
        throw new IllegalArgumentException("Attribute: " + attribName + " unsupported!");
    }
  }

  @Override
  public String[] getAllAttributes() {
    return new String[]{"position", "color"};
  }
}
