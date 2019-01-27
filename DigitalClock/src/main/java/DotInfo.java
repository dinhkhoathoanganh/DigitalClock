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

public class DotInfo {

    private PolygonMesh mesh = new PolygonMesh();
    private Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();


    private Matrix4f modelview = new Matrix4f();
    private FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);

    //radius of the dot
    private float radius = 25;
    private int SLICES = 50;
    private  int noDots = 2;

    //translation of the dots into a colon formation
    private static int[][] translationTable = {
            { 0,-120,0 }, // lower dot
            { 0,120,0 }}; // upper dot


    public PolygonMesh<IVertexData> getMesh() {
        return mesh;
    }

    public Map<String, String> getShaderToVertexAttribute() {
        return shaderToVertexAttribute;
    }

    // drawing of the circle dot, called in method init()
    public DotInfo() {

        List<Vector4f> positions = new ArrayList<Vector4f>();

        //push the center of the circle as the first vertex
        positions.add(new Vector4f(0,0,0,1));
        for (int i=0;i<SLICES;i++) {
            float theta = (float)(i*2*Math.PI/SLICES);
            positions.add(new Vector4f(
                    (float)Math.cos(theta),
                    (float)Math.sin(theta),
                    0,
                    1));
        }

        //we add the last vertex to make the circle watertight
        positions.add(new Vector4f(1,0,0,1));

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
        /* we will use a GL_TRIANGLE_FAN, because this seems tailormade for
    what we want to do here. This mode will use the indices in order
    (0,1,2), (0,2,3), (0,3,4), ..., (0,n-1,n)
     */
        for (int i=0;i<positions.size();i++) {
            indices.add(i);
        }

        //now we create a polygon mesh object

        mesh.setVertexData(vertexData);
        mesh.setPrimitives(indices);

        mesh.setPrimitiveType(GL.GL_TRIANGLE_FAN);
        mesh.setPrimitiveSize(3);

        shaderToVertexAttribute.put("vPosition", "position");
        shaderToVertexAttribute.put("vColor", "color");
    }



    // draw 7 polygons into a "8" formation, called in method draw()
    public void DotDrawable(GL3 gl, GLAutoDrawable gla, ObjectInstance obj, int offset, Matrix4f proj, ShaderLocationsVault shaderLocations){

        gl.glUniformMatrix4fv(
                shaderLocations.getLocation("projection"),
                1, false, proj.get(fb16));

        for (int i = 0; i < noDots; i++) {

            //scale then translate
            modelview = new Matrix4f().translate(translationTable[i][0]+offset,translationTable[i][1],translationTable[i][2])
                    .scale(radius,radius,radius);

            gl.glUniformMatrix4fv(
                    shaderLocations.getLocation("modelview"),
                    1, false, modelview.get(fb16));

            obj.draw(gla);
        }


    }
}
