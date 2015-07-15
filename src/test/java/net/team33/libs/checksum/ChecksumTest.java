package net.team33.libs.checksum;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ChecksumTest {

    private static final byte[] SALT1 = BigInteger.valueOf(0x241e6810ceb6e6b8L).toByteArray();
    private static final byte[] SALT2 = BigInteger.valueOf(0x1234567012345670L).toByteArray();
    private static final byte[] SEQUENCE = {
            0, 11, 22, 33, 44, 55, 66, 77, 88, 99, 100,
            1, 12, 23, 34, 45, 56, 67, 78, 89, 90, 101,
            2, 13, 24, 35, 46, 57, 68, 79, 80, 91, 102,
            3, 14, 25, 36, 47, 58, 69, 70, 81, 92, 103,
            4, 15, 26, 37, 48, 59, 60, 71, 82, 93, 104,
            5, 16, 27, 38, 49, 50, 61, 72, 83, 94, 105,
            6, 17, 28, 39, 40, 51, 62, 73, 84, 95, 106,
            7, 18, 29, 30, 41, 52, 63, 74, 85, 96, 107,
            8, 19, 20, 31, 42, 53, 64, 75, 86, 97, 108,
            9, 10, 21, 32, 43, 54, 65, 76, 87, 98, 109
    };
    private static final byte[] RESULT = {
            45,
            (byte) 145, (byte) 245, (byte) 345, (byte) 445, (byte) 545,
            (byte) 645, (byte) 745, (byte) 845, (byte) 945, (byte) 1045
    };

    @Test
    public void testNull() throws Exception {
        final Checksum result = Checksum.builder(4).build();
        assertEquals(BigInteger.ZERO.toString(36), result.toString());
    }

    @Test
    public void testSALT() throws Exception {
        final Checksum result = Checksum.start(SALT1).build();
        assertEquals(new BigInteger(SALT1).toString(36), result.toString());
    }

    @Test
    public void test2SALT() throws Exception {
        final Checksum result = Checksum.start(SALT2).add(SALT2).build();
        assertEquals(BigInteger.valueOf(0x2468ace02468ace0L).toString(36), result.toString());
    }

    @Test
    public void testSEQUENCE() throws Exception {
        final Checksum result = Checksum.builder(11).add(SEQUENCE).build();
        assertEquals(new BigInteger(RESULT).toString(36), result.toString());
    }

    @Test
    public void testStream() throws Exception {
        try (final InputStream stream = new ByteArrayInputStream(SEQUENCE)) {
            final Checksum result = Checksum.builder(11).add(stream).build();
            assertEquals(new BigInteger(RESULT).toString(36), result.toString());
        }
    }

    @Test
    public void testDuplicatedFiles() throws Exception {

        final Map<Checksum, List<Path>> pathMap = new HashMap<>(0);
        final Map<Checksum, List<Path>> duplicated = new HashMap<>(0);
        testFiles(
                Paths.get("/Users").toAbsolutePath().normalize(),
                new FileTester() {
                    @Override
                    public void test(Path path, Checksum checksum) {
                        final List<Path> paths;
                        if (pathMap.containsKey(checksum)) {
                            paths = pathMap.get(checksum);
                        } else {
                            paths = new ArrayList<>(0);
                            pathMap.put(checksum, paths);
                        }
                        paths.add(path);
                        if (2 == paths.size()) {
                            duplicated.put(checksum, paths);
                        }
                    }
                });

        for (final Map.Entry<Checksum, List<Path>> entry : duplicated.entrySet()) {
            System.out
                    .append("[")
                    .append(entry.getKey().toString())
                    .append("]")
                    .println();
            for (final Path path : entry.getValue()) {
                System.out.println(path);
            }
        }

        assertEquals(duplicated.size(), pathMap.size());
    }

    @Test
    public void testFiles() throws Exception {
        testFiles(
                Paths.get("..").toAbsolutePath().normalize(),
                new FileTester() {
                    @Override
                    public void test(Path path, Checksum checksum) {
                        System.out
                                .append("[")
                                .append(path.toString())
                                .append("] -> <")
                                .append(checksum.toString())
                                .append(">")
                                .println();
                    }
                });
    }

    private void testFiles(final Path path, final FileTester tester) {
        try (final DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            for (final Path entry : paths) {
                if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS))
                    testFiles(entry, tester);
                else
                    testFile(entry, tester);
            }
        } catch (final IOException ignored) {
        }
    }

    private void testFile(final Path path, final FileTester tester) throws IOException {
        try (final InputStream in = Files.newInputStream(path)) {
            final Checksum checksum = Checksum.builder(15).add(in).build();
            tester.test(path, checksum);
        } catch (final IOException ignored) {
        }
    }

    private interface FileTester {
        void test(Path path, Checksum checksum);
    }
}