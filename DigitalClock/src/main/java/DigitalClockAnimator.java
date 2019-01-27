import javax.swing.*;

/**
 * Created by dinh khoat hoang anh on 01/25/2019.
 */
public class DigitalClockAnimator {
  public static void main(String[] args) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

  private static void createAndShowGUI() {
    JFrame frame = new JOGLFrame("Digital clock");
    frame.setVisible(true);
  }
}
