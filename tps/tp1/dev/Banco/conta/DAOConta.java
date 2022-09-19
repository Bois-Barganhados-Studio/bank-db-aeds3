package conta;

/*
 * CRUD para a conta
 * Objeto de acesso aos dados (DAO)
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DAOConta {

    private RandomAccessFile dataArq;
    private int lastId;
    private Conta conta;

    public DAOConta() throws IOException {
        dataArq = new RandomAccessFile("../db/conta_banco.db", "rw");
        conta = null;
        lastId = dataArq.readInt();
    }

    public boolean inserir(Conta obj) throws IOException, Exception {
        return false;
    }

    public boolean alterar(Conta obj) throws IOException, Exception {
        return false;
    }

    public Conta ler(int id) throws IOException {
        return conta;
    }

    // para fazer conferência de nomes iguais
    public Conta ler(String nomeUser) throws IOException {
        return conta;
    }

    public boolean delete(int id) throws IOException {
        return false;
    }

    public ArrayList<Conta> listar() throws IOException {
        ArrayList<Conta> lista = new ArrayList<>();
        return lista;
    }

}
