import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import com.jogamp.opengl.GL;
import util.IVertexData;
import util.ObjectInstance;
import util.PolygonMesh;
import util.ShaderLocationsVault;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentInfo {

    private PolygonMesh mesh = new PolygonMesh(); //mesh for one segment
    private Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

    private Matrix4f modelview = new Matrix4f();
    private FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);

    //no of polygons
    private int noSegment = 7;

    //translation table for the 7 polygons into an "8" shape
    private static int[][] translationTable = {
            { 0,245,0 }, // a
            { 125,120,0 }, // b
            { 125,-120,0 }, // c
            { 0,-245,0 }, // d
            { -125,-120,0 }, // e
            { -125,120,0 }, // f
            { 0,0,0 }, // g
    };

    //rotation table for the 7 polygons into an "8" shape
    private static int[] rotationTable = {90, 0, 0, 90, 0, 0, 90}; // a-g

    public PolygonMesh<IVertexData> getMesh() {
        return mesh;
    }

    public Map<String, String> getShaderToVertexAttribute() {
        return shaderToVertexAttribute;
    }

    // drawing of the circle dot, called in method init()
    public SegmentInfo() {

        List<Vector4f> positions = new ArrayList<Vector4f>();
        positions.add(new Vector4f(-15.0f, -100.0f, 0, 1.0f));
        positions.add(new Vector4f(0.0f, -115.0f, 0, 1.0f)); // bottom
        positions.add(new Vector4f(15.0f, -100.0f, 0, 1.0f));
        positions.add(new Vector4f(15.0f, 100.0f, 0, 1.0f));
        positions.add(new Vector4f(00.0f, 115.0f, 0, 1.0f)); // top
        positions.add(new Vector4f(-15.0f, 100.0f, 0, 1.0f));

        Vector4f colors = new Vector4f(0, 1, 0, 1); // green


        //set up vertex attributes
        List<IVertexData> vertexData = new ArrayList<IVertexData>();
        VertexAttribWithColorProducer producer = new VertexAttribWithColorProducer();
        for (int i = 0; i < positions.size(); i++) {
            IVertexData v = producer.produce();
            v.setData("position", new float[]{positions.get(i).x,
                    positions.get(i).y,
                    positions.get(i).z,
                    positions.get(i).w});
            v.setData("color", new float[]{colors.x,colors.y,colors.z,colors.w});
            vertexData.add(v);
        }


        List<Integer> indices = new ArrayList<Integer>();
        indices.add(0);
        indices.add(1);
        indices.add(2);

        indices.add(0);
        indices.add(2);
        indices.add(3);

        indices.add(0);
        indices.add(3);
        indices.add(5);

        indices.add(3);
        indices.add(4);
        indices.add(5);

        //now we create a polygon mesh object
        mesh.setVertexData(vertexData);
        mesh.setPrimitives(indices);

        mesh.setPrimitiveType(GL.GL_TRIANGLES);
        mesh.setPrimitiveSize(3);

        shaderToVertexAttribute.put("vPosition", "position");
        shaderToVertexAttribute.put("vColor", "color");

    }


    //draw circles into a colon formation, called in method draw()
    public void SegmentDrawable(GL3 gl, GLAutoDrawable gla, ObjectInstance obj, Boolean[] sevenSegmentEncoder, int offset, Matrix4f proj, ShaderLocationsVault shaderLocations) {

        gl.glUniformMatrix4fv(
                shaderLocations.getLocation("projection"),
                1, false, proj.get(fb16));

        for (int i = 0; i < noSegment; i++) {
            if (sevenSegmentEncoder[i]) {
                modelview = new Matrix4f();

                //rotate then translate
                modelview = modelview.translate(translationTable[i][0] + offset, translationTable[i][1], translationTable[i][2])
                        .rotate((float) Math.toRadians(rotationTable[i]), 0, 0, 1);

                gl.glUniformMatrix4fv(
                        shaderLocations.getLocation("modelview"),
                        1, false, modelview.get(fb16));


                obj.draw(gla);
            }
        }

    }
}
