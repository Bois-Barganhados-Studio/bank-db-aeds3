package agencia;

import java.io.IOException;

import conta.Conta;
import conta.DAOConta;

//Tratar exceções de operação DAO nesse arquivo
//Não pedir dados por esse arquivo, todas as operações já devem ter os dados

public class Operacoes {

    private boolean status;
    private DAOConta dao;

    public Operacoes() throws IOException {
        status = false;
        dao = new DAOConta();
    }

    public boolean criarConta(Conta conta) {
        try {
            status = dao.inserir(conta);
        } catch (IOException io) {
            status = false;
        } catch (Exception e) {
            status = false;
        }
        return status;
    }

    public boolean transferencia(Conta debito, Conta credito, float valor) {
        return status;
    }

    public Conta ler(int id) {
        try {
            return dao.ler(id);
        } catch (IOException io) {
            return null;
        }
    }

    public boolean atualizarConta(Conta conta) {
        // Duas possibilidades de alteração
        // Deve checar se o valor do tamanho do registro diminuiu ou aumentou
        return status;
    }

    public boolean deletarConta(int id) {
        try {
            return dao.delete(id);
        } catch (IOException io) {
            return false;
        }
    }

    /*
     * m = registros
     * n = caminhos
     * tipo = qual ordenação
     */
    public void ordenarArquivo(int m, int n, int tipo) {

    }

}
