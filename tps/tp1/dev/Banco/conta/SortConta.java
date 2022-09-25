package conta;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;

public class SortConta {

    private RandomAccessFile dataArq;
    private int lastId, sizeReg, total;
    private long lastPointer, inicio;
    private Conta conta;
    private byte[] bytearray;

    public SortConta() throws IOException {
        dataArq = new RandomAccessFile("db/conta_banco.db", "rw");
        conta = null;
        inicio = dataArq.getFilePointer();
        lastPointer = dataArq.length();
        lastId = dataArq.readInt();
        total = dataArq.readInt();
    }

    public boolean intercalacaoBal(int m, int n) {
        boolean status = false;
        try {
            dataArq.seek(inicio);
            lastId = dataArq.readInt();
            total = dataArq.readInt();
            createTmpFiles(2 * n);
            ArrayList<Conta> lista = new ArrayList<>();
            ArrayList<RandomAccessFile> arqs = new ArrayList<>();
            int idAtual = 0, count = 0, fileNow = 0;
            do {
                // distribuição
                char lapide = dataArq.readChar();
                sizeReg = dataArq.readInt();
                bytearray = new byte[sizeReg];
                dataArq.read(bytearray);
                if (lapide != '*') {
                    conta = new Conta();
                    idAtual = conta.getIdConta();
                    conta.fromByteArray(bytearray);
                    lista.add(conta);
                    count++;
                }
                if (count == m || idAtual == lastId) {
                    count = 0;
                    fileNow++;
                    if (fileNow > n) {
                        fileNow = 1;
                    }
                    RandomAccessFile raTmp = new RandomAccessFile("db/tmp" + fileNow + ".tmp", "rw");
                    Collections.sort(lista, (Conta c1, Conta c2) -> {
                        if (c1.getIdConta() > c2.getIdConta()) {
                            return 1;
                        } else if (c1.getIdConta() < c2.getIdConta()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });
                    for (Conta ct : lista) {
                        bytearray = ct.toByteArray();
                        raTmp.seek(raTmp.length());
                        raTmp.writeChar(' ');
                        raTmp.writeInt(bytearray.length);
                        raTmp.write(bytearray);
                    }
                    raTmp.seek(0);
                    arqs.add(raTmp);
                    lista.clear();
                    // fim distribuição
                }
            } while (idAtual != lastId);
            int i = m;
            boolean isIntercaleted = false;
            do {
                i *= 2;
                RandomAccessFile intercaleted = new RandomAccessFile("db/tmp" + arqs.size() + 1 + ".tmp", "rw");
                for(int j=0;j<i;j++){
                    arqs.get(j);
                    //chamar função next passando o raf como parametro e retornando uma conta
                }
            } while (!isIntercaleted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public boolean intercalacaoTamVar(int m, int n) {
        return false;
    }

    public boolean intercalacaoSelec(int m, int n) {
        return false;
    }

    public boolean intercalacaoBalNMO(int m, int n) {
        return false;
    }

    public boolean intercalacaoPoli(int m, int n) {
        return false;
    }

    public void createTmpFiles(int files) throws IOException {
        for (int i = 0; i < files; i++) {
            File fl = new File("db/tmp" + i + 1 + ".tmp");
            fl.createNewFile();
        }
    }
}
