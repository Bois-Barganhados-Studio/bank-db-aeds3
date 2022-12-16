package estruturas.PatternSearch;

import conta.Conta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.function.BiFunction;

public class PatternSearch {
    private final RandomAccessFile src;
    private static final int MAX_MEM_CONTA = 100;
    private static final int FILE_HEADER_SIZE = 8;

    /**
     * Pattern Search class wrapper constructor.
     * 
     * @param srcFile the file containing the registers to be read.
     * @throws FileNotFoundException I/O errors caused by RandomAccessFile.
     */
    public PatternSearch(File srcFile) throws FileNotFoundException {
        src = new RandomAccessFile(srcFile, "r");
    }

    /**
     * Wrapper function to read file, convert registers to string
     * then call the desired pattern searching method.
     * 
     * @param pattern  The string pattern to be searched in the file.
     * @param searcher The desired pattern searching function to be used.
     * @throws Exception I/O errors caused by RandomAccessFile.
     */
    public void search(String pattern, BiFunction<String, String, PatternSearchInfo> searcher) throws Exception {
        src.seek(FILE_HEADER_SIZE);
        Conta[] regs = getContas();
        PatternSearchInfo info = new PatternSearchInfo();
        while (regs[0] != null) {
            for (int i = 0; regs[i] != null;) {
                info.sum(searcher.apply(pattern, regs[i++].toString()));
            }
            regs = getContas();
        }
        System.out.println("O padrão '" + pattern + "' foi encontrado " + info.getFoundCount() + " vezes no arquivo.");
        System.out.println("Total de comparações: " + info.getNumComp());
    }

    /**
     * Reads up to MAX_MEM_CONTA valid Conta instances from the file.
     * 
     * @return Array of contas read from file.
     * @throws Exception I/O errors caused by RandomAccessFile.
     */
    private Conta[] getContas() throws Exception {
        Conta[] regs = new Conta[MAX_MEM_CONTA];
        byte[] ba = null;
        for (int i = 0; i < MAX_MEM_CONTA && src.getFilePointer() < src.length();) {
            if (src.readChar() != '*') {
                ba = new byte[src.readInt()];
                src.read(ba);
                regs[i] = new Conta();
                regs[i].fromByteArray(ba);
                i++;
            } else {
                src.skipBytes(src.readInt());
            }
        }
        return regs;
    }
}
