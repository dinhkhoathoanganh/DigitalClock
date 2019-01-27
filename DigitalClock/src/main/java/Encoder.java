public class Encoder {
    //encoder to map each segment to be on/off
    private static final byte[] lookupEncoder = {0x7e, 0x30, 0x6d, 0x79, 0x33, 0x5b, 0x5f, 0x70, 0x7f, 0x7b};
    private Boolean[] sevenSegmentEncoder = new Boolean[7];
    public Boolean[] getSevenSegmentEncoder() {
        return sevenSegmentEncoder;
    }

    public Encoder(int digit) {
        // determine the segment from a-g to be on or off
        sevenSegmentEncoder[0] = ((lookupEncoder[digit] & 0b1000000) != 0);
        sevenSegmentEncoder[1] = ((lookupEncoder[digit] & 0b100000) != 0);
        sevenSegmentEncoder[2] = ((lookupEncoder[digit] & 0b10000) != 0);
        sevenSegmentEncoder[3] = ((lookupEncoder[digit] & 0b1000) != 0);
        sevenSegmentEncoder[4] = ((lookupEncoder[digit] & 0b100) != 0);
        sevenSegmentEncoder[5] = ((lookupEncoder[digit] & 0b10) != 0);
        sevenSegmentEncoder[6] = ((lookupEncoder[digit] & 0b1) != 0);
    }
}