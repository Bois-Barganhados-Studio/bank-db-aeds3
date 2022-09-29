package conta;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
            createTmpFiles(n);
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
            HashMap<Integer, Integer> hashInter = new HashMap<>();
            HashMap<Integer, Integer> hashControl = new HashMap<>();
            // start hash de controle
            for (int i = 0; i < arqs.size(); i++) {
                hashInter.put(i, -1);
                hashControl.put(i, 0);
                arqs.get(i).seek(0);
            }
            count = 1;
            int indexBloco = m, fileTmp = 0;
            boolean isIntercaleted = false, goUp = false;
            RandomAccessFile intercaleted = null;
            lista.clear();
            do {
                if (goUp) {
                    if (count < n) {
                        count++;
                    } else {
                        count = 1;
                    }
                }
                intercaleted = new RandomAccessFile("db/tmp" + n + count + ".tmp", "rw");
                for (Map.Entry<Integer, Integer> et : hashInter.entrySet()) {
                    if (hashControl.get(et.getKey()) < indexBloco && et.getValue() == -1) {
                        Conta c1 = next(arqs.get(et.getKey()));
                        lista.add(et.getKey(), c1);
                    }
                }
                int menor = Integer.MAX_VALUE, pos = 0;
                for (Conta c : lista) {
                    if (c.getIdConta() < menor) {
                        menor = c.getIdConta();
                        pos = lista.indexOf(c);
                    }
                }
                bytearray = lista.get(pos).toByteArray();
                intercaleted.seek(intercaleted.length());
                intercaleted.writeChar(' ');
                intercaleted.writeInt(bytearray.length);
                intercaleted.write(bytearray);
                hashControl.put(pos, hashControl.get(pos) + 1);
                hashInter.put(pos, arqs.get(pos).getFilePointer() == arqs.get(pos).length() ? -2 : -1);
                lista.remove(pos);
            } while (!isIntercaleted);

            intercaleted.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public Conta next(RandomAccessFile rf) throws Exception {
        conta = null;
        char lapide = rf.readChar();
        sizeReg = rf.readInt();
        bytearray = new byte[sizeReg];
        rf.read(bytearray);
        if (lapide != '*') {
            conta = new Conta();
            conta.fromByteArray(bytearray);
        }
        return conta;
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
