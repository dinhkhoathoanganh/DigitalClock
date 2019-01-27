package util;

import org.joml.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

/**
 * This class represents a polygon mesh. This class works with any
 * representation of vertex attributes that implements the @link{IVertexData}
 * interface.
 *
 * It stores a polygon mesh as follows:
 *
 * <ul> <li>A list of vertices: Each vertex is represented by an object that
 * stores various attributes of that vertex like position, normal, texture
 * coordinates and others.</li>
 *
 * <li>A list of indices: these are indices into the above array. This is called
 * indexed representation and allows us to share vertices between polygons
 * efficiently.</li> <li>Data about how to interpret the indices (e.g. read 3 at
 * a time to form a triangle, read to make a triangle fan, etc.)</li> <li>How
 * many indices make a polygon (2 for line, 3 for triangle, 4 for quad,
 * etc.)</li> </ul>
 */

public class PolygonMesh<VertexType extends IVertexData> {
  protected List<VertexType> vertexData;
  protected List<Integer> primitives;
  protected int primitiveType;
  protected int primitiveSize;

  protected Vector4f minBounds, maxBounds; //bounding box

  public PolygonMesh() {
    vertexData = new ArrayList<VertexType>();
    primitives = new ArrayList<Integer>();
    primitiveType = primitiveSize = 0;
    minBounds = new Vector4f();
    maxBounds = new Vector4f();
  }

  /**
   * Set the primitive type. The primitive type is represented by an integer.
   * For example in OpenGL, these would be GL_TRIANGLES, GL_TRIANGLE_FAN,
   * GL_QUADS, etc.
   */
  public void setPrimitiveType(int v) {
    primitiveType = v;
  }

  public int getPrimitiveType() {
    return primitiveType;
  }

  /**
   * Sets how many indices make up a primitive.
   */
  public void setPrimitiveSize(int s) {
    primitiveSize = s;
  }

  public int getPrimitiveSize() {
    return primitiveSize;
  }

  public int getPrimitiveCount() {
    return primitives.size();
  }

  public int getVertexCount() {
    return vertexData.size();
  }


  public Vector4f getMinimumBounds() {
    return new Vector4f(minBounds);
  }

  public Vector4f getMaximumBounds() {
    return new Vector4f(maxBounds);
  }


  public List<VertexType> getVertexAttributes() {
    return new ArrayList<VertexType>(vertexData);
  }

  public List<Integer> getPrimitives() {
    return new ArrayList<Integer>(primitives);
  }


  public void setVertexData(List<VertexType> vp) {
    vertexData = new ArrayList<VertexType>(vp);
    computeBoundingBox();
  }


  public void setPrimitives(List<Integer> t) {
    primitives = new ArrayList<Integer>(t);
  }

  /**
   * Compute the bounding box of this polygon mesh, if there is position data
   */

  protected void computeBoundingBox() {
    int j;

    if (vertexData.size() <= 0)
      return;

    if (!vertexData.get(0).hasData("position")) {
      return;
    }

    List<Vector4f> positions = new ArrayList<Vector4f>();

    for (IVertexData v : vertexData) {
      float[] data = v.getData("position");
      Vector4f pos = new Vector4f(0, 0, 0, 1);
      switch (data.length) {
        case 4:
          pos.w = data[3];
        case 3:
          pos.z = data[2];
        case 2:
          pos.y = data[1];
        case 1:
          pos.x = data[0];
      }
      positions.add(pos);
    }

    minBounds = new Vector4f(positions.get(0));
    maxBounds = new Vector4f(positions.get(0));

    for (j = 0; j < positions.size(); j++) {
      Vector4f p = positions.get(j);

      if (p.x < minBounds.x) {
        minBounds.x = p.x;
      }

      if (p.x > maxBounds.x) {
        maxBounds.x = p.x;
      }

      if (p.y < minBounds.y) {
        minBounds.y = p.y;
      }

      if (p.y > maxBounds.y) {
        maxBounds.y = p.y;
      }

      if (p.z < minBounds.z) {
        minBounds.z = p.z;
      }

      if (p.z > maxBounds.z) {
        maxBounds.z = p.z;
      }
    }
  }

  /**
   * Compute vertex normals in this polygon mesh using Newell's method, if
   * position data exists
   */

  public void computeNormals() {
    int i, j, k;

    if (vertexData.size() <= 0)
      return;

    if (!vertexData.get(0).hasData("position")) {
      return;
    }

    if (!vertexData.get(0).hasData("normal"))
      return;

    List<Vector4f> positions = new ArrayList<Vector4f>();

    for (IVertexData v : vertexData) {
      float[] data = v.getData("position");
      Vector4f pos = new Vector4f(0, 0, 0, 1);
      switch (data.length) {
        case 4:
          pos.w = data[3];
        case 3:
          pos.z = data[2];
        case 2:
          pos.y = data[1];
        case 1:
          pos.x = data[0];
      }
      positions.add(pos);
    }
    List<Vector4f> normals = new ArrayList<Vector4f>();

    for (i = 0; i < positions.size(); i++) {
      normals.add(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f));
    }

    for (i = 0; i < primitives.size(); i += primitiveSize) {
      Vector4f norm = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);


      //compute the normal of this triangle
      int[] v = new int[primitiveSize];

      for (k = 0; k < primitiveSize; k++) {
        v[k] = primitives.get(i + k);
      }

      //the newell's method to calculate normal

      for (k = 0; k < primitiveSize; k++) {
        norm.x += (positions.get(v[k]).y - positions.get(v[(k + 1) % primitiveSize]).y) * (positions.get(v[k]).z + positions.get(v[(k + 1) % primitiveSize]).z);
        norm.y += (positions.get(v[k]).z - positions.get(v[(k + 1) % primitiveSize]).z) * (positions.get(v[k]).x + positions.get(v[(k + 1) % primitiveSize]).x);
        norm.z += (positions.get(v[k]).x - positions.get(v[(k + 1) % primitiveSize]).x) * (positions.get(v[k]).y + positions.get(v[(k + 1) % primitiveSize]).y);
      }
      norm = norm.normalize();


      for (k = 0; k < primitiveSize; k++) {
        normals.set(v[k], normals.get(v[k]).add(norm));
      }
    }

    for (i = 0; i < normals.size(); i++) {
      normals.set(i, normals.get(i).normalize());
    }
    for (i = 0; i < vertexData.size(); i++) {
      vertexData.get(i).setData("normal", new float[]{normals.get(i).x,
              normals.get(i).y,
              normals.get(i).z,
              normals.get(i).w});
    }
  }
}
