package util;

/**
 * Created by ashesh on 9/3/2016.
 */
public interface VertexProducer<K extends IVertexData> {

  /**
   * produce an object of type K
   */
  K produce();
}
