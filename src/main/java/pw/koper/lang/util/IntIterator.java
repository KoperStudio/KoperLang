package pw.koper.lang.util;

public class IntIterator {
    private int it;

    public IntIterator() {
        this(1);
    }

    public IntIterator(int start) {
        this.it = start;
    }

    public int next() {
        return it++;
    }
}
