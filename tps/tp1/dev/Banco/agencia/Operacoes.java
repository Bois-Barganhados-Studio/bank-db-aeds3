package agencia;

/*
 * Classe para armazenar operações e ações do nosso banco quanto agencia
 * Tratar exceções de operação DAO nessa classe
 * Não pedir dados por essa classe, todas as operações já devem ter os dados
 * passados por parametro ou por chamada
 */

import java.io.IOException;
import conta.Conta;
import conta.DAOConta;

public class Operacoes {

    private boolean status;
    private DAOConta dao;

    public Operacoes() throws IOException {
        status = false;
        dao = new DAOConta();
    }

    public boolean criarConta(Conta conta) {
        try {
            if (conta != null)
                status = dao.inserir(conta);
            else
                status = false;
        } catch (IOException io) {
            System.err
                    .println("Falha ao criar conta, erro de I/O foi encontrado internamente\nErro: " + io.getMessage());
            status = false;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao criar conta encontrado: " + e.getMessage());
            status = false;
        }
        return status;
    }

    public boolean transferencia(Conta ctDebito, Conta ctCredito, float valor) throws InternalError, Exception {
        if (ctCredito == null) {
            throw new InternalError("Conta para creditar valor não foi localizada internamente!");
        }
        if (ctDebito == null) {
            throw new InternalError("Conta para debitar valor não foi localizada internamante!");
        }
        if (ctDebito.getSaldoConta() < valor) {
            throw new InternalError("Saldo indisponível para realizar transferencia entre contas!");
        }
        ctDebito.setSaldoConta(ctDebito.getSaldoConta() - valor);
        ctCredito.setSaldoConta(ctCredito.getSaldoConta() + valor);
        ctDebito.setTransferenciasRealizadas(ctDebito.getTransferenciasRealizadas() + 1);
        ctCredito.setTransferenciasRealizadas(ctCredito.getTransferenciasRealizadas() + 1);
        status = (dao.alterar(ctDebito) && dao.alterar(ctCredito));
        return status;
    }

    public Conta ler(int id) {
        try {
            return dao.ler(id);
        } catch (IOException io) {
            System.err.println("Erro de IO durante tentativa de leitura\nErro: " + io.getMessage());
            io.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Erro inesperado durante tentativa de leitura\nErro: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean atualizarConta(Conta conta) {
        try {
            if (conta != null)
                status = dao.alterar(conta);
            else
                status = false;
        } catch (IOException io) {
            System.err.println(
                    "Falha ao alterar conta, erro de I/O foi encontrado internamente\nErro: " + io.getMessage());
            status = false;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao alterar conta encontrado: " + e.getMessage());
            status = false;
        }
        return status;
    }

    public boolean deletarConta(int id) {
        try {
            return dao.delete(id);
        } catch (IOException io) {
            System.err.println("Erro de IO durante tentativa de deletar conta\nErro: " + io.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Erro inesperado durante tentativa de deletar conta\nErro: " + e.getMessage());
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

    public void finalizar() throws Exception {
        dao.close();
        dao = null;
    }
}
