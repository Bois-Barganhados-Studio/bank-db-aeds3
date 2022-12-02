package estruturas.Huffman;

import java.io.*;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/**
 * @author Vinicius G Santos
 */
public class HuffmanTree {
    private Node root;

    public Node getRoot() {
        return root;
    }

    public HuffmanTree(byte[] ba) throws IOException {
        fromByteArray(ba);
    }
    public HuffmanTree(TreeSet<Node> nodePool) {
        this.root = CreateTree(nodePool);
    }

    public HuffmanTree() {
        this.root = null;
    }

    /**
     * Creates a huffman tree given a set of the leaf nodes.
     * @param nodePool A set with all the leaf nodes.
     * @return The root node of the created Tree.
     */
    private Node CreateTree(TreeSet<Node> nodePool) {
        if (nodePool == null)
            throw new IllegalArgumentException("Parameter must not be null");
        // Pops the first two nodes in the set
        // creates a parent node from them and inserts it
        // util there's only the root left
        while (nodePool.size() > 1) {
            nodePool.add(new Node(
                    nodePool.pollFirst(),
                    nodePool.pollFirst()
                )
            );
        }
        return nodePool.pollFirst();
    }

    /**
     * Creates a Hashtable with the Occurrence value as the key of the table and
     * the binary path of the tree in a string as the value of the table.
     * @return A hashtable containing the occurrences (key) and their string binary encode (value).
     */
    public Hashtable<Byte, String> genEncodeTable() {
        char[] path = new char[128];
        Hashtable<Byte, String> encodeTable = new Hashtable<>();
        consumeValPath(root, path, -1, encodeTable::put);
        return encodeTable;
    }

    /**
     * Creates a Hashtable with the Occurrence value as the value of the table and
     * the binary path of the tree in a string as the Key of the table.
     * @return A hashtable containing the occurrences (value) and their string binary encode (key).
     */
    public Hashtable<String, Byte> getDecodeTable() {
        char[] path = new char[128];
        Hashtable<String, Byte> decodeTable = new Hashtable<>();
        consumeValPath(root, path, -1, (b, str) -> {
            decodeTable.put(str, b);
        });
        return decodeTable;
    }

    /**
     * Navigates in the tree to find the path of all occurrences and
     * use it with a BiConsumer function
     * @param node The node of the tree to be visited
     * @param path A char array to trace the binary path
     * @param len The current length used in the char array
     * @param consumer A BiConsumer function to use the node value and its binary path
     */
    private void consumeValPath(Node node, char[] path, int len, BiConsumer<Byte, String> consumer) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            String aux = "";
            for (int i = 0; i <= len; i++)
                aux += path[i];
            consumer.accept(node.getValue(), aux);
        } else {
            path[++len] = '0';
            consumeValPath(node.left, path, len, consumer);
            path[len] = '1';
            consumeValPath(node.right, path, len, consumer);
        }
    }

    /**
     * Converts the instanced tree into a byte array
     * containing a leaf value (byte) followed by its weight (int)
     * @param nodePool A set with all the occurrences.
     * @return A byte array containing the leaf node's data
     * @throws IOException I/O error caused by DataOutputStream
     */
    public static byte[] toByteArray(TreeSet<Node> nodePool) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        // Writes the occurrences value and weight in the byte array
        for (Node n : nodePool) {
            dos.writeByte(n.getValue());
            dos.writeInt(n.getWeight());
        }
        return bos.toByteArray();
    }

    /**
     * Populates the instanced tree with the data contained in the given byte array
     * @param ba The given byte array with the data to be loaded
     * @throws IOException I/O error caused by DataInputStream
     */
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bis);
        TreeSet<Node> nodePool = new TreeSet<>(Node::TreeSetCompareTo);
        int n = (ba.length / 5);
        // Creates a set with the leaf nodes
        for (int i = 0; i < n; i++)
            nodePool.add(new Node(dis.readByte(), dis.readInt()));
        // Builds the tree with the leaf nodes
        this.root = CreateTree(nodePool);
    }

}
