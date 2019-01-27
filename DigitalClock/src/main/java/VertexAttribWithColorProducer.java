public class VertexAttribWithColorProducer implements util.VertexProducer<VertexAttribWithColor> {
  @Override
  public VertexAttribWithColor produce() {
    return new VertexAttribWithColor();
  }
}
