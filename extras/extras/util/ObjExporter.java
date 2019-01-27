package util;

import org.joml.Vector4f;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to export a PolygonMesh object to file using the OBJ file
 * format This exporter only writes the position, normal and texture coordinate
 * data. It ignores any other attributes
 */
public class ObjExporter {
  public static boolean exportFile(PolygonMesh<IVertexData> mesh,
                                   OutputStream out) throws
          IllegalArgumentException{
    PrintWriter printer = new PrintWriter(out);
    int i, j;

    List<IVertexData> vertexData = mesh.getVertexAttributes();
    if (vertexData.size() == 0)
      return true;

    List<Vector4f> vertices = new ArrayList<Vector4f>();
    List<Vector4f> normals = new ArrayList<Vector4f>();
    List<Vector4f> texcoords = new ArrayList<Vector4f>();
    List<Integer> primitives = mesh.getPrimitives();

    for (IVertexData v : vertexData) {
      if (v.hasData("position")) {
        float[] data = v.getData("position");
        printer.print("v ");
        for (j = 0; j < data.length; j++) {
          printer.print(data[j] + " ");
        }
        printer.println();
      }

    }

    for (IVertexData v : vertexData) {
      if (v.hasData("normal")) {
        float[] data = v.getData("normal");
        printer.print("vn ");
        if (data.length<3) {
          throw new IllegalArgumentException("Too few numbers for normal, must "
                  + "be 3 or 4, with the 4th number being 0");
        }
        for (j = 0; j < 3; j++) {
          printer.print(data[j] + " ");
        }
        printer.println();
      }

    }

    for (IVertexData v : vertexData) {
      if (v.hasData("texcoord")) {
        float[] data = v.getData("texcoord");
        printer.print("vt ");
        if (data.length<3) {
          throw new IllegalArgumentException("Too few numbers for texture "
                  + "coordinate, must "
                  + "be 3 or 4, with the 4th number being 1");
        }
        for (j = 0; j < 3; j++) {
          printer.print(data[j] + " ");
        }
        printer.println();
      }

    }


    //polygons

    for (i = 0; i < primitives.size(); i += mesh.getPrimitiveSize()) {
      printer.print("f ");
      for (j = 0; j < mesh.getPrimitiveSize(); j++) {
        //in OBJ file format indices begin at 1, so we must add 1 here
        printer.print(primitives.get(i + j) + 1 + " ");
      }
      printer.println();
    }
    printer.close();
    return true;
  }
}
