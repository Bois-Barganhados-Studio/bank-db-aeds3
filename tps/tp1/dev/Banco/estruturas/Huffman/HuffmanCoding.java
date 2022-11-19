package estruturas.Huffman;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

/**
 * @author Vinicius G Santos
 * @author Leon Junio Martins
 */
public class HuffmanCoding {
    private RandomAccessFile src;
    private RandomAccessFile output;

    private static final int MAX_MEM_BYTES = 10000;
    public static final String VERSION = "Compressao-Huffman";

    public HuffmanCoding() {
    }

    /**
     * Closes and dereference RandomAccessFile attributes.
     * 
     * @throws IOException IO error caused by RandoAccessFile.
     */
    public void close() throws IOException {
        if (src != null) {
            src.close();
            src = null;
        }
        if (output != null) {
            output.close();
            output = null;
        }
    }

    /**
     * Generates a compression target file based on the given source file.
     * 
     * @param srcFile The source file to be compressed.
     * @return The target file of the compression.
     */
    private File genCompressionFile(File srcFile) {
        // Adds the compression version
        return new File(srcFile.getParent() + File.separator + VERSION + srcFile.getName());
    }

    /**
     * Reads all the bytes from source file, counts their occurrence amount then
     * returns a TreeSet.
     * 
     * @return A TreeSet of the occurrences and their weights.
     * @throws IOException IO error caused by RandoAccessFile.
     */
    private TreeSet<Node> getOccurrences() throws IOException {
        if (src == null || src.length() == 0)
            throw new IOException("Cannot read from source file");
        Hashtable<Byte, Integer> occurrenceTable = new Hashtable<>();
        // Rewinds source file pointer
        src.seek(0);
        // Reads from file by parts as the full file may not fit entirely in RAM
        long bytesLeft;
        byte[] ba = new byte[MAX_MEM_BYTES];
        for (bytesLeft = src.length(); bytesLeft > MAX_MEM_BYTES; bytesLeft -= MAX_MEM_BYTES) {
            src.read(ba);
            for (byte b : ba)
                // If the byte is not already present in the table adds it with a value 1
                // else sums 1 to the value already in the table
                occurrenceTable.merge(b, 1, Integer::sum);
        }
        // Reads the remaining bytes in the file
        if (bytesLeft > 0) {
            src.read(ba, 0, (int) bytesLeft);
            for (int i = 0; i < bytesLeft; i++)
                occurrenceTable.merge(ba[i], 1, Integer::sum);
        }
        // Gets the occurrences (keys) in the hashtable
        ArrayList<Byte> values = new ArrayList<>(occurrenceTable.keySet());
        // Gets the weights (amount of appearances) is position relative to its
        // occurrence
        ArrayList<Integer> weights = new ArrayList<>(occurrenceTable.values());
        // Creates TreeSet with custom ordering
        TreeSet<Node> occurrences = new TreeSet<>(Node::TreeSetCompareTo);
        // Inserts in TreeSet
        for (int i = 0; i < values.size(); i++)
            occurrences.add(new Node(values.get(i), weights.get(i)));
        // Rewinds source file pointer
        src.seek(0);

        return occurrences;
    }

    /**
     * Compresses the given source file based on the Huffman coding algorithm.
     *
     * @param srcFile The name of the source file.
     * @throws IOException If the File is not found or IO error caused by
     *                     RandomAccessFile.
     */
    public File compress(File srcFile) throws IOException {
        // Close possible open raf attributes then prep new raf
        close();
        src = new RandomAccessFile(srcFile, "r");
        File outFile = genCompressionFile(srcFile);
        output = new RandomAccessFile(outFile, "rw");
        // Gets occurrences and their weights
        // then saves it in the output file
        TreeSet<Node> occurrences = getOccurrences();
        byte[] ba = HuffmanTree.toByteArray(occurrences);
        output.writeInt(ba.length);
        output.write(ba);
        // Creates the tree then generates encode table containing
        // the occurrences and their binary representation string
        HuffmanTree tree = new HuffmanTree(new TreeSet<>(occurrences));
        Hashtable<Byte, String> encodeTable = tree.genEncodeTable();
        // Calculates the length of the data compressed
        long bitLen = getEncodedBitLen(occurrences, encodeTable);
        // Writes in the file 'x' first bits to be ignored
        byte fillerBits = (byte) (bitLen % 8);
        output.write(fillerBits);
        // Creates string builder to append the binary representation
        StringBuilder bin = new StringBuilder();
        // Inserts the first 'x' filler bits
        bin.append("0".repeat(fillerBits));
        // Reads from file by parts as the full file may not fit entirely in RAM
        long len;
        ba = new byte[MAX_MEM_BYTES];
        // Read, encode, write loop
        for (len = src.length(); len > MAX_MEM_BYTES; len -= MAX_MEM_BYTES) {
            src.read(ba);
            output.write(encode(encodeTable, ba, len, bin));
        }
        // Read, encode, write the remaining bytes
        if (len > 0) {
            src.read(ba, 0, (int) len);
            output.write(encode(encodeTable, ba, len, bin));
        }
        // Closes raf attributes, restart branch ID counter
        close();
        Node.restartCount();
        // Return string full path of the compressed file
        return outFile;
    }

    /**
     * Calculates the length in bits of the compressed data.
     * 
     * @param occurrences TreeSet with the occurrence and its amount.
     * @param encodeTable A hashtable containing each occurrence and its binary code
     *                    as a string generated from the Huffman tree.
     * @return The length in bits of the compressed data.
     */
    private long getEncodedBitLen(TreeSet<Node> occurrences, Hashtable<Byte, String> encodeTable) {
        long bitLen = 0;
        for (Node it : occurrences) {
            // Length of the binary representation string
            int len = encodeTable.get(it.getValue()).length();
            bitLen += ((long) it.getWeight() * len);
        }
        return bitLen;
    }

    /**
     * Encodes the given byte array using the given encode hash table
     * that contains the encoded binary (generated from the Huffman tree)
     * as a string for each occurrence of the byte.
     * 
     * @param encodeTable A hashtable containing each occurrence and its binary code
     *                    as a string generated from the Huffman tree.
     * @param ba          The byte array to be encoded.
     * @param len         The length of the byte array to be used.
     * @param bin         A string builder to append the binary representation of
     *                    the encoded data.
     */
    private byte[] encode(Hashtable<Byte, String> encodeTable, byte[] ba, long len, StringBuilder bin)
            throws IOException {
        // For each byte up to 'len' position in the byte array
        // appends its string binary representation to the string builder
        for (int i = 0; i < (int) len; i++) {
            bin.append(encodeTable.get(ba[i]));
        }
        // Trying to free some memory
        ba = null;
        // Keeps reading and encoding new bytes util the length is divisible by 8
        // so that it won't produce gaps in the compressed file
        while ((bin.length() % 8) != 0) {
            if (src.getFilePointer() >= src.length())
                break;
            else
                bin.append(encodeTable.get(src.readByte()));
        }
        // Converts string binary representation to actual binary
        // then returns the encoded byte array
        String aux = bin.toString();
        bin.delete(0, bin.length());
        BigInteger bg = new BigInteger(aux, 2);
        return bg.toByteArray();
    }

    /**
     * Decompresses the given source file based on the Huffman coding algorithm.
     *
     * @param srcFile The name of the source file.
     * @throws IOException If the File is not found or IO error caused by
     *                     RandomAccessFile.
     */
    public File decompress(File srcFile) throws IOException, Exception {
        close();
        srcFile = genCompressionFile(srcFile);
        // define arquivo de saida da descompressão
        File outfile = new File(srcFile.getPath().replace("Compressao", "Descompressao"));
        if (outfile.isFile()) {
            outfile.delete();
            outfile.createNewFile();
        }
        // Uso de sistema de try autoclose para acessar os dados do arquivo comprimido
        try (RandomAccessFile srcCompressed = new RandomAccessFile(srcFile, "r")) {
            // inicia escrita e leitura no arquivo de saida
            output = new RandomAccessFile(outfile, "rw");
            // Arvore em bytes salva no arquivo
            byte[] byteTree = new byte[srcCompressed.readInt()];
            srcCompressed.read(byteTree);
            // tabela de decodificação em estilo hash
            Hashtable<String, Byte> decodeTable;
            HuffmanTree tree = new HuffmanTree(byteTree);
            decodeTable = tree.getDecodeTable();
            // byte de exclusão para remover bits de lixo
            byte byteExclusao = srcCompressed.readByte();
            // ler bytes comprimidos para iniciar sistema de descompressão
            byte bytesCompressed[] = new byte[(int) (srcCompressed.length() - srcCompressed.getFilePointer())];
            srcCompressed.read(bytesCompressed);
            String strBinario = "";
            // Locomover pelos bytes lidos para criar uma string de caminhos pela arvore
            for (byte bt : bytesCompressed) {
                String num = Integer.toBinaryString(bt);
                if (num.length() > 8) {
                    num = num.substring(num.length() - 8, num.length());
                } else {
                    num = "0".repeat(8 - num.length()) + num;
                }
                if (!num.equals("00000000")) {
                    strBinario += num;
                }
            }
            String caminhoTree = "";
            // remove bits extras no inicio da string de locomoção
            strBinario = strBinario.substring(byteExclusao);
            // deslocamento pela arvore para reescrever arquivo original
            for (int ind = 0; ind < strBinario.length(); ind++) {
                caminhoTree += strBinario.charAt(ind);
                if (decodeTable.containsKey(caminhoTree)) {
                    // escrita na saida do arquivo descomprimido
                    output.write(decodeTable.get(caminhoTree));
                    caminhoTree = "";
                }
            }
        }
        output.close();
        return outfile;
    }

    /**
     * Gets the decompression target file
     * based on the given compressed file.
     * 
     * @param srcFile The source (compressed) file.
     * @return A decompression target file.
     */
    public File getDecompressionFile(File srcFile) throws IOException {
        // Removes the compression version from the string
        return new File(srcFile.getParent() + File.separator + srcFile.getName().substring(VERSION.length()));
    }

    // Basic Testing
    public boolean doTest(File in) throws IOException {
        byte[] ba = { 1, 1, 1, 1, 2, 2, 7, 7, 7, 7, 7, 14, 14, 16, 17 };
        boolean sit = false;
        try (RandomAccessFile raf = new RandomAccessFile(in, "rw")) {
            raf.write(ba);
            sit = this.decompress(this.compress(in)).isFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sit;
    }
}
