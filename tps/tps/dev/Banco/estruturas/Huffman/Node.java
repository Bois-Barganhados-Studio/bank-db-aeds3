package estruturas.Huffman;

/**
 * @author Vinicius G Santos
 */
class Node implements Comparable<Node> {
    private Byte value;
    private int weight;
    public Node left;
    public Node right;

    private static byte nextBranchVal = -128;

    public Byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        if (weight > 0)
            this.weight = weight;
        else throw new IllegalArgumentException("Value of parameter must be a positive integer");
    }

    /**
     * Creates a new leaf node.
     * @param value The value of the occurrence.
     * @param weight The amount of occurrences.
     */
    public Node(byte value, int weight) {
        setValue(value);
        setWeight(weight);
        this.left = this.right = null;
    }

    /**
     * Creates a new branch node.
     * @param left The left descendent node.
     * @param right The right descendent node.
     */
    public Node(Node left, Node right) {
        if (left == null || right == null)
            throw new IllegalArgumentException("Parameters must not be null");
        this.value = Node.nextBranchVal++;
        setWeight(left.weight + right.weight);
        this.left = left;
        this.right = right;
    }

    // Resets the branch value counter
    public static void restartCount() {
        nextBranchVal = -128;
    }

    public boolean isLeaf() {
        return (this.left == null &&
                this.right == null);
    }

    @Override
    public int compareTo(Node n) {
        if (n == null) return -1;
        int result = 0;
        if (this.weight > n.weight) result = 1;
        else if (this.weight < n.weight) result = -1;
        else if (!this.isLeaf() && n.isLeaf()) result = 1;
        else if (this.isLeaf() && !n.isLeaf()) result = -1;
        return result;
    }

    /**
     * CompareTo method dedicated to be used in a TreeSet
     * as its items must not be equal.
     * @param n The other node to be compared with.
     * @return 1 if this node is greater than the other
     * or -1 if lower. They should never be equal.
     */
    public int TreeSetCompareTo(Node n) {
        if (n == null) return -1;
        int result = -1;
        if (this.weight > n.weight) result = 1;
        else if (this.weight == n.weight) {
            if (!this.isLeaf() && n.isLeaf()) result = 1;
            else if ((this.isLeaf() == n.isLeaf()) && (this.value > n.value)) result = 1;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node n)) return false;
        return this.compareTo(n) == 0 && this.value.equals(n.value);
    }

    @Override
    public String toString() {
        return ("{ Value: " + this.value + " Weight: " + this.weight + " }");
    }
}
