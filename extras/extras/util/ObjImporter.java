package util;

import com.jogamp.opengl.GL;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A helper class to import a PolygonMesh object from an OBJ file. It imports
 * only position, normal and texture coordinate data (if present)
 */
public class ObjImporter {
  public static <K extends IVertexData> PolygonMesh<K> importFile
          (VertexProducer<K> producer, InputStream in, boolean scaleAndCenter) throws IllegalArgumentException {
    ArrayList<Vector4f> vertices;
    ArrayList<Vector4f> normals;
    ArrayList<Vector4f> texcoords;
    ArrayList<Integer> triangles, triangle_texture_indices, triangle_normal_indices;
    int i, j;
    int lineno;
    PolygonMesh<K> mesh = new PolygonMesh<K>();

    vertices = new ArrayList<Vector4f>();
    normals = new ArrayList<Vector4f>();
    texcoords = new ArrayList<Vector4f>();
    triangles = new ArrayList<Integer>();
    triangle_texture_indices = new ArrayList<Integer>();
    triangle_normal_indices = new ArrayList<Integer>();

    Scanner sc = new Scanner(in);

    lineno = 0;

    String line;


    while (sc.hasNext()) {
      line = sc.nextLine();
      lineno++;
      if ((line.length() <= 0) || (line.charAt(0) == '#')) {
        //line is a comment, ignore
        continue;
      }

      Scanner str = new Scanner(line);

      String[] tokens = line.split("[ \\t\\n\\x0B\\f\\r]");

      if (tokens[0].compareTo("v") == 0) {
        if ((tokens.length < 4) || (tokens.length > 7))
          throw new IllegalArgumentException("Line " + lineno + ": Vertex coordinate has an invalid number of values");
        float num;
        Vector4f v = new Vector4f();

        v.x = Float.parseFloat(tokens[1]);
        v.y = Float.parseFloat(tokens[2]);
        v.z = Float.parseFloat(tokens[3]);
        v.w = 1.0f;

        if (tokens.length == 5) {
          num = Float.parseFloat(tokens[4]);
          if (num != 0) {
            v.x /= num;
            v.y /= num;
            v.z /= num;
          }
        }

        vertices.add(v);
      } else if (tokens[0].compareTo("vt") == 0) {
        if ((tokens.length < 3) || (tokens.length > 4))
          throw new IllegalArgumentException("Line " + lineno + ": Texture coordinate has an invalid number of values");
        Vector4f v = new Vector4f();

        float n;

        v.x = Float.parseFloat(tokens[1]);
        v.y = Float.parseFloat(tokens[2]);
        v.z = 0.0f;
        v.w = 1.0f;

        if (tokens.length > 3) {
          v.z = Float.parseFloat(tokens[3]);
        }

        texcoords.add(v);
      } else if (tokens[0].compareTo("vn") == 0) {
        if (tokens.length != 4)
          throw new IllegalArgumentException("Line " + lineno + ": Normal has an invalid number of values");

        float num;
        Vector3f v = new Vector3f();

        v.x = Float.parseFloat(tokens[1]);
        v.y = Float.parseFloat(tokens[2]);
        v.z = Float.parseFloat(tokens[3]);

        v = v.normalize();
        normals.add(new Vector4f(v, 0.0f));
      } else if (tokens[0].compareTo("f") == 0) {
        if (tokens.length < 4)
          throw new IllegalArgumentException("Line " + lineno + ": Face has too few vertices, must be at least 3");

        ArrayList<Integer> t_triangles, t_tex, t_normal;

        t_triangles = new ArrayList<Integer>();
        t_tex = new ArrayList<Integer>();
        t_normal = new ArrayList<Integer>();

        for (i = 1; i < tokens.length; i++) {
          String[] data = tokens[i].split("[/]");

          if ((data.length < 1) && (data.length > 3))
            throw new IllegalArgumentException("Line " + lineno + ": Face specification has an incorrect number of values");

          //in OBJ file format all indices begin at 1, so must subtract 1 here
          t_triangles.add(Integer.parseInt(data[0]) - 1); //vertex index
          if (data.length > 1) {
            if (data[1].length() > 0) //a vertex texture index exists
            {
              t_tex.add(Integer.parseInt(data[1]) - 1);
            }


            if (data.length > 2) //a vertex normal index exists
            {
              t_normal.add(Integer.parseInt(data[2]) - 1);
            }

          }
        }

        if (t_triangles.size() < 3) {
          throw new IllegalArgumentException("Line " + lineno + ": Fewer than 3 vertices for a polygon");
        }

        //if face has more than 3 vertices, break down into a triangle fan
        for (i = 2; i < t_triangles.size(); i++) {
          triangles.add(t_triangles.get(0));
          triangles.add(t_triangles.get(i - 1));
          triangles.add(t_triangles.get(i));

          if (t_tex.size() > 0) {
            triangle_texture_indices.add(t_tex.get(0));
            triangle_texture_indices.add(t_tex.get(i - 1));
            triangle_texture_indices.add(t_tex.get(i));
          }

          if (t_normal.size() > 0) {
            triangle_normal_indices.add(t_normal.get(0));
            triangle_normal_indices.add(t_normal.get(i - 1));
            triangle_normal_indices.add(t_normal.get(i));
          }

        }


      }
    }

    if (scaleAndCenter) {
      //center about the origin and within a cube of side 1 centered at the origin
      //find the centroid
      Vector4f center = new Vector4f(vertices.get(0));

      Vector4f minimum = new Vector4f(center);
      Vector4f maximum = new Vector4f(center);

      for (i = 1; i < vertices.size(); i++) {
        //center = center.add(vertices.get(i).x,vertices.get(i).y,vertices.get(i).z,0.0f);
        minimum = minimum.min(vertices.get(i));
        maximum = maximum.max(vertices.get(i));
      }

            /*
            center.x = center.x * (1.0f/vertices.size());
            center.y = center.y * (1.0f/vertices.size());
            center.z = center.z * (1.0f/vertices.size());
*/
      center = new Vector4f(minimum).add(maximum).mul(0.5f);


      float longest;


      longest = Math.max(maximum.x - minimum.x, Math.max(maximum.y - minimum.y, maximum.z - minimum.z));

      //first translate and then scale
      Matrix4f transformMatrix = new Matrix4f().mul(new Matrix4f().scale(1.0f / longest, 1.0f / longest, 1.0f / longest))
              .mul(new Matrix4f().translate(-center.x, -center.y, -center.z));

      //scale down each other
      for (i = 0; i < vertices.size(); i++) {
        vertices.set(i, transformMatrix.transform(vertices.get(i)));
      }
    }

    List<K> vertexData = new ArrayList<K>();

    for (i = 0; i < vertices.size(); i++) {
      K v = producer.produce();
      v.setData("position", new float[]{vertices.get(i).x,
              vertices.get(i).y,
              vertices.get(i).z,
              vertices.get(i).w});
      if (texcoords.size() == vertices.size()) {
        v.setData("texcoord", new float[]{texcoords.get(i).x,
                texcoords.get(i).y,
                texcoords.get(i).z,
                texcoords.get(i).w});
      }
      if (normals.size() == vertices.size()) {
        v.setData("normal", new float[]{normals.get(i).x,
                normals.get(i).y,
                normals.get(i).z,
                normals.get(i).w});
      }

      vertexData.add(v);
    }

    if ((normals.size() == 0) || (normals.size() != vertices.size()))
      mesh.computeNormals();

    mesh.setVertexData(vertexData);
    mesh.setPrimitives(triangles);
    mesh.setPrimitiveType(GL.GL_TRIANGLES);
    mesh.setPrimitiveSize(3);
    return mesh;
  }
};
