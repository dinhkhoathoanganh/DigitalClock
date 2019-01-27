package util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;

import util.Material;

import org.joml.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.util.GLBuffers;

/**
 * This class represents an "object" to be drawn. The object's geometry is
 * represented in an underlying PolygonMesh object.
 *
 * This class encapsulates within it all the details of drawing the mesh using
 * OpenGL 3.x. This includes the vertex array object, vertex buffer objects,
 * etc.
 */

public class ObjectInstance {
  protected IntBuffer vao; //our VAO
  protected IntBuffer vbo;//all our Vertex Buffer Object IDs
  protected util.PolygonMesh<?> mesh;
  protected String name; //a unique "name" for this object


  /**
   * Create a blank ObjectInstance using a GL context and a name
   *
   * @param gl   the GL context to be used for all opengl commands on this
   *             object
   * @param name a name of the object
   */

  public ObjectInstance(GL3 gl, String name) {
    vao = IntBuffer.allocate(1);
    vbo = IntBuffer.allocate(2); //by default, just position and index
    //create the VAO ID for this object
    gl.glGenVertexArrays(1, vao);
    //bind the VAO
    gl.glBindVertexArray(vao.get(0));
    //create as many VBO IDs as you need, in this case just position and index
    gl.glGenBuffers(2, vbo);
    //set the name
    setName(name);
    //default material

  }

  /**
   * Create an ObjectInstance and prepare it for rendering. Preparation includes
   * sending all relevant data to the GPU and enabling various buffers, which is
   * why it requires an opengl context and the shader details
   *
   * @param gl                         the GL context within which this object
   *                                   will be rendered
   * @param program                    the shader program used to render this
   *                                   object. This is used to set all the
   *                                   necessary buffers
   * @param shaderLocations            the location of various shader variables
   *                                   relevant to the rendering of this object
   * @param shaderVarsToAttributeNames a mapping between shader variables and
   *                                   vertex attributes in the polygon mesh
   * @param mesh                       the underlying mesh that stores the
   *                                   geometry to be drawn with all relevant
   *                                   vertex attributes
   * @param name                       a name of the object
   */

  public <K extends IVertexData> ObjectInstance(GL3 gl, util.ShaderProgram program, util.ShaderLocationsVault shaderLocations, Map<String, String> shaderVarsToAttributeNames, util.PolygonMesh<K> mesh, String name) {


    vao = IntBuffer.allocate(1);
    vbo = IntBuffer.allocate(2);

    //create the VAO ID for this object
    gl.glGenVertexArrays(1, vao);
    //bind the VAO
    gl.glBindVertexArray(vao.get(0));
    //create as many VBO IDs as you need
    gl.glGenBuffers(2, vbo);
    //set the name
    setName(name);
    //default material

    initPolygonMesh(gl, program,shaderLocations, shaderVarsToAttributeNames,
            mesh);
  }

  /**
   * Create an ObjectInstance and prepare it for rendering. Preparation includes
   * sending all relevant data to the GPU and enabling various buffers, which is
   * why it requires an opengl context and the shader details. THIS ASSUMES THAT
   * THE SHADER PROGRAM HAS BEEN ENABLED BEFORE THIS OBJECT IS CREATED!
   *
   * @param gl                         the GL context within which this object
   *                                   will be rendered
   * @param shaderLocations            the location of various shader variables
   *                                   relevant to the rendering of this object
   * @param shaderVarsToAttributeNames a mapping between shader variables and
   *                                   vertex attributes in the polygon mesh
   * @param mesh                       the underlying mesh that stores the
   *                                   geometry to be drawn with all relevant
   *                                   vertex attributes
   * @param name                       a name of the object
   */

  public <K extends IVertexData> ObjectInstance(GL3 gl, util.ShaderLocationsVault shaderLocations, Map<String, String> shaderVarsToAttributeNames, util.PolygonMesh<K> mesh, String name) {


    vao = IntBuffer.allocate(1);
    vbo = IntBuffer.allocate(2);

    //create the VAO ID for this object
    gl.glGenVertexArrays(1, vao);
    //bind the VAO
    gl.glBindVertexArray(vao.get(0));
    //create as many VBO IDs as you need
    gl.glGenBuffers(2, vbo);
    //set the name
    setName(name);
    //default material

    initPolygonMesh(gl, shaderLocations, shaderVarsToAttributeNames, mesh);
  }

  /**
   * A helper method that sets this object up for rendering
   *
   * @param gl                         the GL context within which this object
   *                                   is to be rendered
   * @param program                    the shader program to be used to render
   *                                   this object
   * @param shaderLocations            the locations of various shader variables
   *                                   relevant to this object
   * @param shaderVarsToAttributeNames a mapping of shader variable -> vertex
   *                                   attributes in the underlying mesh
   * @param mesh                       the underlying polygon mesh
   */
  protected <K extends IVertexData> void initPolygonMesh(GL3 gl, util.ShaderProgram program, util.ShaderLocationsVault shaderLocations, Map<String, String> shaderVarsToAttributeNames, util.PolygonMesh<K> mesh) {
    int i, j;

    //get a list of all the vertex attributes from the mesh
    List<K> vertexDataList = mesh.getVertexAttributes();
    List<Integer> primitives = mesh.getPrimitives();

    //get the indices for the mesh as a IntBuffer
    int[] primitivesAsArray = new int[primitives.size()];
    for (i = 0; i < primitives.size(); i++) {
      primitivesAsArray[i] = primitives.get(i);
    }
    IntBuffer indexBuffer = IntBuffer.wrap(primitivesAsArray);

    //create a floatbuffer for all vertex attributes. When we convert an IVertexData
    //to a bunch of floats, we must remember where each attribute starts, because
    //we will need to give it to glVertexAttribPointer below

    int sizeOfOneVertex = 0;
    Map<String, Integer> offsets = new HashMap<String, Integer>();

    for (Map.Entry<String, String> e : shaderVarsToAttributeNames.entrySet()) {
      offsets.put(e.getValue(), sizeOfOneVertex);
      sizeOfOneVertex += vertexDataList.get(0).getData(e.getValue()).length;
    }

    int stride;

    if (shaderVarsToAttributeNames.size() > 1)
      stride = sizeOfOneVertex;
    else
      stride = 0;

    float[] vertexDataAsFloats = new float[sizeOfOneVertex * vertexDataList.size()];
    i = 0;
    for (IVertexData v : vertexDataList) {
      for (Map.Entry<String, String> e : shaderVarsToAttributeNames.entrySet()) {
        float[] data = v.getData(e.getValue());
        for (j = 0; j < data.length; j++, i++) {
          vertexDataAsFloats[i] = data[j];
        }
      }
    }

    FloatBuffer vertexDataAsBuffer = FloatBuffer.wrap(vertexDataAsFloats);


    this.mesh = mesh;

    this.mesh.computeBoundingBox();



		/*
         *Bind the VAO as the current VAO, so that all subsequent commands affect it
		 */
    gl.glBindVertexArray(vao.get(0));

    //enable the program
    program.enable(gl);

    //copy all the data to the vbo[0]
    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(0));
    gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexDataAsBuffer.capacity() * GLBuffers.SIZEOF_FLOAT, vertexDataAsBuffer, GL3.GL_STATIC_DRAW);


    /**
     * go through all variables and enable that
     * attribute.
     */


    for (Map.Entry<String, String> e : shaderVarsToAttributeNames.entrySet()) {
      /**
       * e.key: the name of the shader variable
       * e.value: the name of the corresponding vertex attribute in polygon mesh
       */


      int shaderLocation = shaderLocations.getLocation(e.getKey());

      if (shaderLocation >= 0) {
        //tell opengl how to interpret the above data
        gl.glVertexAttribPointer(shaderLocation, vertexDataList.get(0).getData(e.getValue()).length, GL3.GL_FLOAT, false, Float.BYTES * stride, Float.BYTES * offsets.get(e.getValue()));
        //enable this attribute so that when rendered, this is sent to the vertex shader
        gl.glEnableVertexAttribArray(shaderLocation);
      }
    }





		/*
         *Allocate the VBO for triangle indices and send it to GPU
		 */
    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));
    gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * GLBuffers.SIZEOF_INT, indexBuffer, GL3.GL_STATIC_DRAW);


		/*
		 *Unbind the VAO to prevent accidental change to all the settings
		 *so at this point, this VAO has two VBOs and two enabled VertexAttribPointers.
		 * It is going to remember all of that!
		 */
    gl.glBindVertexArray(0);
    program.disable(gl);
  }

  /**
   * A helper method that sets this object up for rendering
   *
   * @param gl                         the GL context within which this object
   *                                   is to be rendered
   * @param shaderLocations            the locations of various shader variables
   *                                   relevant to this object
   * @param shaderVarsToAttributeNames a mapping of shader variable -> vertex
   *                                   attributes in the underlying mesh
   * @param mesh                       the underlying polygon mesh
   */
  protected <K extends IVertexData> void initPolygonMesh(GL3 gl, util.ShaderLocationsVault shaderLocations, Map<String, String> shaderVarsToAttributeNames, util.PolygonMesh<K> mesh) {
    int i, j;

    //get a list of all the vertex attributes from the mesh
    List<K> vertexDataList = mesh.getVertexAttributes();
    List<Integer> primitives = mesh.getPrimitives();

    //get the indices for the mesh as a IntBuffer
    int[] primitivesAsArray = new int[primitives.size()];
    for (i = 0; i < primitives.size(); i++) {
      primitivesAsArray[i] = primitives.get(i);
    }
    IntBuffer indexBuffer = IntBuffer.wrap(primitivesAsArray);

    //create a floatbuffer for all vertex attributes. When we convert an IVertexData
    //to a bunch of floats, we must remember where each attribute starts, because
    //we will need to give it to glVertexAttribPointer below

    int sizeOfOneVertex = 0;
    Map<String, Integer> offsets = new HashMap<String, Integer>();

    for (Map.Entry<String, String> e : shaderVarsToAttributeNames.entrySet()) {
      offsets.put(e.getValue(), sizeOfOneVertex);
      sizeOfOneVertex += vertexDataList.get(0).getData(e.getValue()).length;
    }

    int stride;

    if (shaderVarsToAttributeNames.size() > 1)
      stride = sizeOfOneVertex;
    else
      stride = 0;

    float[] vertexDataAsFloats = new float[sizeOfOneVertex * vertexDataList.size()];
    i = 0;
    for (IVertexData v : vertexDataList) {
      for (Map.Entry<String, String> e : shaderVarsToAttributeNames.entrySet()) {
        float[] data = v.getData(e.getValue());
        for (j = 0; j < data.length; j++, i++) {
          vertexDataAsFloats[i] = data[j];
        }
      }
    }

    FloatBuffer vertexDataAsBuffer = FloatBuffer.wrap(vertexDataAsFloats);


    this.mesh = mesh;

    this.mesh.computeBoundingBox();



		/*
         *Bind the VAO as the current VAO, so that all subsequent commands affect it
		 */
    gl.glBindVertexArray(vao.get(0));


    //copy all the data to the vbo[0]
    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(0));
    gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexDataAsBuffer.capacity() * GLBuffers.SIZEOF_FLOAT, vertexDataAsBuffer, GL3.GL_STATIC_DRAW);


    /**
     * go through all variables and enable that
     * attribute.
     */


    for (Map.Entry<String, String> e : shaderVarsToAttributeNames.entrySet()) {
      /**
       * e.key: the name of the shader variable
       * e.value: the name of the corresponding vertex attribute in polygon mesh
       */


      int shaderLocation = shaderLocations.getLocation(e.getKey());

      if (shaderLocation >= 0) {
        //tell opengl how to interpret the above data
        gl.glVertexAttribPointer(shaderLocation, vertexDataList.get(0).getData(e.getValue()).length, GL3.GL_FLOAT, false, Float.BYTES * stride, Float.BYTES * offsets.get(e.getValue()));
        //enable this attribute so that when rendered, this is sent to the vertex shader
        gl.glEnableVertexAttribArray(shaderLocation);
      }
    }





		/*
		 *Allocate the VBO for triangle indices and send it to GPU
		 */
    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));
    gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * GLBuffers.SIZEOF_INT, indexBuffer, GL3.GL_STATIC_DRAW);


		/*
		 *Unbind the VAO to prevent accidental change to all the settings
		 *so at this point, this VAO has two VBOs and two enabled VertexAttribPointers.
		 * It is going to remember all of that!
		 */
    gl.glBindVertexArray(0);
  }


  public void cleanup(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();
    if (vao.get(0) != 0) {
      //give back the VBO IDs to OpenGL, so that they can be reused
      gl.glDeleteBuffers(2, vbo);
      //give back the VAO ID to OpenGL, so that it can be reused
      gl.glDeleteVertexArrays(1, vao);
    }
  }

  /**
   * Draw this ObjectInstance. This assumes that the object has been setup for
   * rendering prior to calling this method.
   *
   * @param gla the context within which this object is to be drawn
   */
  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();
    //draw the object


    //1. bind its VAO
    gl.glBindVertexArray(vao.get(0));
    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(0));
    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, vbo.get(1));

    //2. execute the "superpower" command
    //this effectively reads the index buffer, grabs the vertex data using
    //the indices and sends them to the shader
    gl.glDrawElements(mesh.getPrimitiveType(), mesh.getPrimitiveCount(), GL.GL_UNSIGNED_INT, 0);
    gl.glBindVertexArray(0);
  }

  public PolygonMesh getMesh() {
    return mesh;
  }


  /*
   *Set the name of this object
   */
  public void setName(String name) {
    this.name = name;
  }

  /*
   *Gets the name of this object
  */
  public String getName() {
    return name;
  }


  public Vector4f getMinimumBounds() {
    return mesh.getMinimumBounds();
  }

  public Vector4f getMaximumBounds() {
    return mesh.getMaximumBounds();
  }

}
