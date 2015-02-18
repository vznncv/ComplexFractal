package local.fractal.util.Pair;

/**
 * It's helper class contains pair of the elements
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Pair<F, S> {
    /**
     * First element.
     */
    private F first;
    /**
     * Second element;
     */
    private S second;

    /**
     * Default constructor. Set elements to null.
     */
    public Pair() {
        first = null;
        second = null;
    }

    /**
     * Constructor
     *
     * @param first  first element.
     * @param second second element.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Get first element.
     *
     * @return first element
     */
    public F getFirst() {
        return first;
    }

    /**
     * Set first element.
     *
     * @param first first element
     */
    public void setFirst(F first) {
        this.first = first;
    }

    /**
     * Get second element.
     *
     * @return second element
     */
    public S getSecond() {
        return second;
    }

    /**
     * Set second element
     *
     * @param second second element
     */
    public void setSecond(S second) {
        this.second = second;
    }
}
