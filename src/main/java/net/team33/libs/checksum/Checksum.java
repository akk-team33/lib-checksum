package net.team33.libs.checksum;

import java.io.IOException;
import java.io.InputStream;
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

    public static Builder start(final byte... salt) {
        return builder(salt.length)
                .add(salt)
                .setCount(0);
    }

    public static Builder builder(final int byteLength) {
        return new Builder(byteLength);
    }

    @Override
    public final int hashCode() {
        return backing.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return (this == obj) || ((obj instanceof Checksum) && ((Checksum) obj).backing.equals(backing));
    }

    @Override
    public final String toString() {
        return backing.toString(Character.MAX_RADIX);
    }

    public static class Builder {

        private final int chunkSize = 16;
        private final byte[] bytes;
        private long count = 0;

        private Builder(final int byteLength) {
            bytes = new byte[byteLength];
        }

        public final Builder add(final byte[] values, final int offset, final int length) {
            final int limit = offset + length;
            for (int index = offset; index < limit; ++index) {
                bytes[nextIndex()] += values[index];
            }
            return this;
        }

        public final Builder add(final byte[] values) {
            return add(values, 0, values.length);
        }

        public final Builder add(final InputStream in) throws IOException {
            final byte[] input = new byte[chunkSize];
            int read = in.read(input);
            while (0 < read) {
                add(input, 0, read);
                read = in.read(input);
            }
            return this;
        }

        public final Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public final Checksum build() {
            return new Checksum(bytes);
        }

        private int nextIndex() {
            int result = (int) (count % this.bytes.length);
            count += 1;
            return result;
        }
    }
}
