import com.jogamp.opengl.*;
import util.*;
import org.joml.Matrix4f;
import java.text.SimpleDateFormat;
import java.util.*;

public class View {
  private int WINDOW_WIDTH, WINDOW_HEIGHT, ORTHO_VERTICAL = 900, ORTHO_HORIZONTAL = 1500;
  private Matrix4f proj;
  private ObjectInstance digitObj, dotObj;
  private ShaderLocationsVault shaderLocations;

  private DotInfo dotInfo = new DotInfo();
  private SegmentInfo segmentInfo = new SegmentInfo();


  ShaderProgram program;


  public View() {
    proj = new Matrix4f();
    proj.identity();

    digitObj = null;
    shaderLocations = null;
    WINDOW_WIDTH = WINDOW_HEIGHT = 0;
  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new ShaderProgram();
    program.createProgram(gl, "shaders/default.vert", "shaders/default.frag");

    shaderLocations = program.getAllShaderVariables(gl);

    //create a segment object
    digitObj = new ObjectInstance(gl, program, shaderLocations, segmentInfo.getShaderToVertexAttribute(), segmentInfo.getMesh(), "triangles");

    //create a segment object
    dotObj = new ObjectInstance(gl, program, shaderLocations, dotInfo.getShaderToVertexAttribute(), dotInfo.getMesh(), "triangles");


    program.disable(gl);
  }


  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

    //set the background color to be black
    gl.glClearColor(0, 0, 0, 0);
    //clear the background
    gl.glClear(gl.GL_COLOR_BUFFER_BIT);
    //enable the shader program
    program.enable(gl);

    ///////////////////////// Draw digits /////////////////////////

    //get the current time
    int timeNow = getTimeNow();

    //draw from right to left
    //add spacing between each digit, and an extra space between HH:mm:ss
    int offset = 975; int offsetDiff = 410;
    int separateSpace = 50;
    Boolean separate = false;


    for (int i=0;i<6;i++) { // pad any zeros for hours if the time is less than 10:00:00
      Encoder code = new Encoder(timeNow % 10);
      segmentInfo.SegmentDrawable(gl, gla, digitObj, code.getSevenSegmentEncoder(), offset, proj, shaderLocations);
      timeNow = timeNow / 10;

      //determine spacing for the next digit
      offset -=offsetDiff;
      if (separate) {
        offset -=separateSpace;
      }
      separate = !separate;
    }
     //////////////////////////////////////////////////////////////


    ////////////////////////// Draw dot ///////////////////////////
    //offset for the colons
    offset = -525; offsetDiff = 850;
      for (int i=0; i<2; i++){ //draw 2 colons
          dotInfo.DotDrawable(gl, gla, dotObj, offset, proj, shaderLocations);
          offset += offsetDiff;
      }
    //////////////////////////////////////////////////////////////

    gl.glFlush();
    //disable the program
    program.disable(gl);
  }

  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height, int  ORG_WINDOW_WIDTH, int ORG_WINDOW_HEIGHT) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);
    System.out.println("Viewport height: " + height);

    //calculate new window ration to make sure the drawings are resized proportionately and not stretched
    //the drawings will keep their original ratio
    float newWindowRatio = ((float) WINDOW_WIDTH / (float) WINDOW_HEIGHT);

    //ortho is calculated based on the original window size

    //if the window is to stretched out horizontally
    if (newWindowRatio > (((float) ORG_WINDOW_WIDTH / (float) ORG_WINDOW_HEIGHT))){
      proj = new Matrix4f().ortho2D(-ORTHO_VERTICAL * newWindowRatio, ORTHO_VERTICAL * newWindowRatio, -ORTHO_VERTICAL, ORTHO_VERTICAL);
    }

    //if the window is to stretched out vertically
    else {
      proj = new Matrix4f().ortho2D(-ORTHO_HORIZONTAL, ORTHO_HORIZONTAL, -ORTHO_HORIZONTAL / newWindowRatio, ORTHO_HORIZONTAL / newWindowRatio);
    }

  }

  public void dispose(GLAutoDrawable gla) {
    digitObj.cleanup(gla);
  }

  public int getTimeNow(){
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    String timeNow = sdf.format(cal.getTime()).replace(":","");
    int result = Integer.parseInt(timeNow);
    return result;
  }
}
