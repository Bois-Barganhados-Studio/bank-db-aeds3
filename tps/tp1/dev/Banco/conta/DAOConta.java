package conta;

import java.io.EOFException;

/*
 * CRUD para a conta
 * Objeto de acesso aos dados (DAO)
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DAOConta {

    private RandomAccessFile dataArq;
    private int lastId, sizeReg, total;
    private long lastPointer, inicio;
    private Conta conta;
    private byte[] bytearray;

    public DAOConta() throws IOException {
        try {
            dataArq = new RandomAccessFile("db/conta_banco.db", "rw");
            conta = null;
            inicio = dataArq.getFilePointer();
            lastPointer = dataArq.length();
            lastId = dataArq.readInt();
            total = dataArq.readInt();
        } catch (EOFException eof) {
            lastId = 0;
            total = 0;
        }
    }

    public boolean inserir(Conta obj) throws IOException, Exception {
        dataArq.seek(inicio);
        if (lastId == 0) {
            dataArq.writeInt(lastId);
            dataArq.writeInt(total);
            lastId = 1;
            total = 1;
        } else {
            lastId++;
            total++;
        }
        lastPointer = dataArq.length();
        obj.setIdConta(lastId);
        if (ler(obj.getNomeUsuario()) != null) {
            throw new Exception(
                    "O nome de usuário já existe cadastrado na base de dados\nUsuario: " + obj.getNomeUsuario());
        }
        bytearray = obj.toByteArray();
        dataArq.seek(lastPointer);
        dataArq.writeChar(' ');
        dataArq.writeInt(bytearray.length);
        dataArq.write(bytearray);
        dataArq.seek(inicio);
        dataArq.writeInt(lastId);
        dataArq.writeInt(total);
        return true;
    }

    public boolean alterar(Conta obj) throws IOException, Exception {
        int idAtual = 0;
        boolean status = false;
        conta = null;
        dataArq.seek(inicio);
        lastId = dataArq.readInt();
        total = dataArq.readInt();
        long pointer = 0;
        do {
            pointer = dataArq.getFilePointer();
            char lapide = dataArq.readChar();
            sizeReg = dataArq.readInt();
            bytearray = new byte[sizeReg];
            dataArq.read(bytearray);
            if (lapide != '*') {
                conta = new Conta();
                conta.fromByteArray(bytearray);
                idAtual = conta.getIdConta();
                if (obj.getIdConta() == conta.getIdConta()) {
                    bytearray = obj.toByteArray();
                    if (bytearray.length <= sizeReg) {
                        dataArq.seek(pointer);
                        dataArq.writeChar(' ');
                        dataArq.writeInt(sizeReg);
                        dataArq.write(bytearray);
                    } else {
                        dataArq.seek(pointer);
                        dataArq.writeChar('*');
                        lastPointer = dataArq.length();
                        dataArq.seek(lastPointer);
                        dataArq.writeChar(' ');
                        dataArq.writeInt(obj.getIdConta());
                        dataArq.write(bytearray);
                    }
                    idAtual = lastId;
                    status = true;
                } else {
                    conta = null;
                }
            }
        } while (idAtual != lastId);
        return status;
    }

    public Conta ler(int id) throws IOException, Exception {
        int idAtual = 0;
        conta = null;
        dataArq.seek(inicio);
        lastId = dataArq.readInt();
        total = dataArq.readInt();
        do {
            char lapide = dataArq.readChar();
            sizeReg = dataArq.readInt();
            bytearray = new byte[sizeReg];
            dataArq.read(bytearray);
            if (lapide != '*') {
                conta = new Conta();
                conta.fromByteArray(bytearray);
                idAtual = conta.getIdConta();
                if (idAtual == id) {
                    idAtual = lastId;
                } else {
                    conta = null;
                }
            }
        } while (idAtual != lastId);
        return conta;
    }

    // para fazer conferência de nomes iguais
    public Conta ler(String nomeUser) throws IOException {
        int idAtual = 0;
        conta = null;
        dataArq.seek(inicio);
        lastId = dataArq.readInt();
        total = dataArq.readInt();
        do {
            char lapide = dataArq.readChar();
            sizeReg = dataArq.readInt();
            bytearray = new byte[sizeReg];
            dataArq.read(bytearray);
            if (lapide != '*') {
                conta = new Conta();
                conta.fromByteArray(bytearray);
                idAtual = conta.getIdConta();
                if (conta.getNomeUsuario().equalsIgnoreCase(nomeUser)) {
                    idAtual = lastId;
                } else {
                    conta = null;
                }
            }
        } while (idAtual != lastId);
        return conta;
    }

    public boolean delete(int id) throws IOException {
        boolean status = false;
        int idAtual = 0;
        conta = null;
        dataArq.seek(inicio);
        lastId = dataArq.readInt();
        total = dataArq.readInt();
        do {
            long pointer = dataArq.getFilePointer();
            char lapide = dataArq.readChar();
            sizeReg = dataArq.readInt();
            bytearray = new byte[sizeReg];
            dataArq.read(bytearray);
            if (lapide != '*') {
                conta = new Conta();
                conta.fromByteArray(bytearray);
                idAtual = conta.getIdConta();
                if (idAtual == id) {
                    dataArq.seek(pointer);
                    dataArq.writeChar('*');
                    idAtual = lastId;
                    status = true;
                    conta = null;
                } else {
                    conta = null;
                }
            }
        } while (idAtual != lastId);
        return status;
    }

    public void close() throws IOException {
        dataArq.close();
    }

}
