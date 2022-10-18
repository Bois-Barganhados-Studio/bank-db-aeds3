package conta;

import java.io.EOFException;

/*
 * CRUD para a conta
 * Objeto de acesso aos dados (DAO)
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import estruturas.ArvoreBPlus;
import estruturas.HashConta;

public class DAOConta {

    private static RandomAccessFile dataArq;
    private static ArvoreBPlus arvore;
    private HashConta hash;
    private int lastId, sizeReg, total;
    private static long lastPointer, inicio;
    private Conta conta;
    private byte[] bytearray;

    // Construtor padrão de contas do banco
    public DAOConta() throws Exception {
        try {
            dataArq = new RandomAccessFile("db/conta_banco.db", "rw");
            conta = null;
            inicio = dataArq.getFilePointer();
            lastPointer = dataArq.length();
            lastId = dataArq.readInt();
            total = dataArq.readInt();
            // createBPlusTree();
            hash = new HashConta(4);
            // hash.getPointer(1);
        } catch (EOFException eof) {
            lastId = 0;
            total = 0;
        }
    }

    // metodo para inserir no banco de dados de contas
    public boolean inserir(Conta obj) throws IOException, Exception {
        boolean first = false;
        dataArq.seek(inicio);
        if (lastId == 0) {
            first = true;
            dataArq.writeInt(lastId);
            dataArq.writeInt(total);
            lastId = 1;
            total = 1;
        } else {
            lastId++;
            total++;
        }
        lastPointer = dataArq.length();
        if (obj.getIdConta() == 0 || obj.getIdConta() == -1) {
            obj.setIdConta(lastId);
        } else {
            if (obj.getIdConta() > lastId) {
                lastId = obj.getIdConta() + 1;
            }
        }
        if (first == false && ler(obj.getNomeUsuario()) != null) {
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
        hash.adicionar(obj.getIdConta(), lastPointer);
        return true;
    }

    // metodo para alterar dados de um objeto do banco de dados
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
                        hash.atualizar(obj.getIdConta(), lastPointer);
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

    // leitura de dados a partir de um ID
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
    public Conta ler(String nomeUser) throws IOException, Exception {
        conta = null;
        dataArq.seek(inicio);
        int lastId = dataArq.readInt();
        int total = dataArq.readInt();
        do {
            char lapide = dataArq.readChar();
            sizeReg = dataArq.readInt();
            bytearray = new byte[sizeReg];
            dataArq.read(bytearray);
            if (lapide != '*') {
                conta = new Conta();
                conta.fromByteArray(bytearray);
                if (conta.getNomeUsuario().equalsIgnoreCase(nomeUser)) {
                    break;
                } else {
                    conta = null;
                }
            }
        } while (dataArq.getFilePointer() != dataArq.length());
        return conta;
    }

    // leitura de dados a partir de um ID
    public Conta lerHash(int id) throws IOException, Exception {
        conta = null;
        dataArq.seek(inicio);
        long pointer = hash.localizar(id);
        dataArq.seek(pointer);
        char lapide = dataArq.readChar();
        sizeReg = dataArq.readInt();
        bytearray = new byte[sizeReg];
        dataArq.read(bytearray);
        if (lapide != '*') {
            conta = new Conta();
            conta.fromByteArray(bytearray);
        }
        return conta;
    }

    // Metodo para deletar objeto do banco de dados
    public boolean delete(int id) throws IOException, Exception {
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
                    hash.remover(id);
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

    public static void close() throws IOException {
        dataArq.close();
    }

    public void createBPlusTree() throws Exception {
        arvore = new ArvoreBPlus(5);
        long pointer = 0;
        dataArq.seek(inicio);
        lastId = dataArq.readInt();
        total = dataArq.readInt();
        int idAtual = 0;
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
                arvore.inserir(idAtual, pointer);
            }
        } while (idAtual != lastId);
    }

    public static void open() throws IOException {
        dataArq = new RandomAccessFile("db/conta_banco.db", "rw");
        inicio = dataArq.getFilePointer();
        lastPointer = dataArq.length();
        dataArq.seek(lastPointer);
    }

}
