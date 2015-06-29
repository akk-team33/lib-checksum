package net.team33.libs.checksum;

import java.math.BigInteger;

public class Checksum {

    private final BigInteger backing;

    private Checksum(byte[] bytes) {
        this.backing = new BigInteger(zeroPrefixed(bytes));
    }

    private static byte[] zeroPrefixed(byte[] values) {
        if (0 < values.length && 0 <= values[0]) {
            return values;
        } else {
            final byte[] result = new byte[values.length + 1];
            result[0] = 0;
            for (int index = 0, limit = values.length; index < limit; ++index) {
                result[index + 1] = values[index];
            }
            return result;
        }
    }

    public static Builder builder(final byte... salt) {
        return builder(salt.length, salt);
    }

    private static Builder builder(final int byteLength, final byte... salt) {
        return new Builder(byteLength, salt);
    }

    @Override
    public final String toString() {
        return backing.toString(16);
    }

    public static class Builder {

        private final byte[] bytes;
        private long count = 0;

        private Builder(final int byteLength, final byte[] salt) {
            bytes = new byte[byteLength];
            add(salt);
        }

        public final Checksum build() {
            return new Checksum(bytes);
        }

        public final Builder add(final byte... values) {
            for (byte value : values) {
                bytes[nextIndex()] += value;
            }
            return this;
        }

        private int nextIndex() {
            int result = (int) (count % this.bytes.length);
            count += 1;
            return result;
        }
    }
}
