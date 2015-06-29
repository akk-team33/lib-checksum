package net.team33.libs.checksum;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class ChecksumTest {

    private static final byte[] SALT = BigInteger.valueOf(0x12345678DEADFACEL).toByteArray();

    @Test
    public void testSALT() throws Exception {
        final Checksum result = Checksum.builder(SALT).build();
        assertEquals(new BigInteger(SALT).toString(16), result.toString());
    }

    @Test
    public void test2SALT() throws Exception {
        final Checksum result = Checksum.builder(SALT).add(SALT).build();
        assertEquals(BigInteger.valueOf(0x2468acf0bc5af49cL).toString(16), result.toString());
    }
}