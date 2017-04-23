package pw.stego.util;

import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Created by LabunskyA
 * GitHub: github.com/LabunskyA
 * VK: vk.com/labunsky
 */
public class Patterns {
    private static int JUMP_LENGTH = 17;

    public enum Type {SIMPLE, EVENTUALY_DISTRIBUTED}

    public static String createPattern(Type type, int messageLength, int imagePixels, int shift) {
        switch (type) {
            case SIMPLE:
                return newSimplePattern(messageLength, imagePixels);
            case EVENTUALY_DISTRIBUTED:
                return newEvenlyDistributedPatter(messageLength, imagePixels, shift);

            default:
                throw new NoSuchElementException("Not supported container type");
        }
    }

    private static String newSimplePattern(int messageLength, int imagePixels) {
        int empty = imagePixels - messageLength;
        if (empty < 0)
            throw new ArrayIndexOutOfBoundsException("Not enough space in image");

        int start = new Random().nextInt(empty);
        return getJump(start) + messageLength;
    }

    private static String newCumulativeDistributedPattern(int messageLength, int imagePixels) {
        final StringBuilder pattern = new StringBuilder();
        final Random r = new Random();

        int empty = imagePixels - messageLength;
        if (empty < 0)
            throw new ArrayIndexOutOfBoundsException("Not enough space in image");

        int current = 0;
        int from = 0;

        while (current < messageLength && empty > 0) {
            int delta = r.nextInt(empty) + 1;
            from += delta;

            int section = r.nextInt(messageLength - current);
            if (current + section >= messageLength)
                section = messageLength - current;

            pattern.append(getJump(from));
            if (section != 0)
                pattern.append(section);

            empty -= delta + 17;
            current += section;
            from += section + 17;
        }

        if (current < messageLength) try {
            int lastSection = Integer.parseInt(pattern.substring(pattern.lastIndexOf(">") + 1, pattern.length()));

            pattern.delete(pattern.lastIndexOf(">") + 1, pattern.length());
            pattern.append(lastSection + messageLength - current);
        } catch (NumberFormatException e) {
            pattern.append(messageLength - current);
        }
        
        return pattern.toString();
    }

    private static String newEvenlyDistributedPatter(int messageLength, int imagePixels, int shift) {
        final Random r = new Random();
        final StringBuilder pattern = new StringBuilder();

        final int start = r.nextInt(imagePixels);
        int unused = imagePixels - messageLength;

        int delta = 1;

        pattern.append(getJump(start));
        if (unused > 0 && r.nextBoolean()) {
            pattern.append("<i>");
            unused--;
            delta = -delta;
        }

        if (unused > 0 && r.nextBoolean()) {
            pattern.append("<t>");
            unused--;
            delta *= shift;
        }

        final int frequency = Math.max(imagePixels / 6666, 300);
        final int space = imagePixels / frequency / 7 * 2;

        int i, id = 0, skip, d = 0;
        for (i = start; id < messageLength; i = modInc(i, delta, imagePixels)) {
            id++;
            d++;

            if (unused > space && r.nextInt() % frequency == 0) {
                pattern.append(d);
                d = 0;

                skip = space + (-1 * r.nextInt(2)) * r.nextInt(space / 10);
                if (skip + JUMP_LENGTH >= unused)
                    skip = unused - JUMP_LENGTH - 1;

                unused -= (skip + JUMP_LENGTH);
                skip = (skip + JUMP_LENGTH) * delta;

                i = modInc(i, modInc(JUMP_LENGTH, skip, imagePixels), imagePixels);
                pattern.append(getJump(i));
            }
        }

        return pattern.append(d).toString();
    }

    private static String getJump(int to) {
        return "<j:" + to +  ">";
    }

    private static int modInc(int i, int delta, int mod) {
        return (mod + i + delta) % mod;
    }
}
