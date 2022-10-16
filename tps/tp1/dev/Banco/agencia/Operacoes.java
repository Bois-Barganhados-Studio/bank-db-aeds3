package agencia;

/*
 * Classe para armazenar operações e ações do nosso banco quanto agencia
 * Tratar exceções de operação DAO nessa classe
 * Não pedir dados por essa classe, todas as operações já devem ter os dados
 * passados por parametro ou por chamada
 * @author Leon Junio
 * @author Edmar Oliveira
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

    /**
     * Operação do banco para criar uma conta
     * 
     * @param conta
     * @return
     */
    public boolean criarConta(Conta conta) {
        try {
            if (conta != null)
                status = dao.inserir(conta);
            else
                status = false;
        } catch (IOException io) {
            System.err
                    .println("Falha ao criar conta, erro de I/O foi encontrado internamente\nErro: " + io.getMessage());
            io.printStackTrace();
            status = false;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao criar conta encontrado: " + e.getMessage());
            status = false;
        }
        return status;
    }

    /**
     * Operação do banco para realizar uma transferencia
     * 
     * @param ctDebito  conta de debito do saldo
     * @param ctCredito conta de credito do saldo
     * @param valor     valor para ser creditado e debitado
     * @return booleano caso a transferencia seja realizada
     * @throws InternalError
     * @throws Exception
     */
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

    /**
     * Operação de Leitura para retornar dados de uma conta especifica
     * 
     * @param id id para procurar no banco de dados
     * @return
     */
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

    /**
     * Operação para atualizar conta e alterar os dados
     * 
     * @param conta conta com os novos dados
     * @return
     */
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

    /**
     * Operação para deletar conta do banco de dados
     * 
     * @param id id da conta para ser deletada
     * @return
     */
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

    /**
     * Forma de finalizar o DAO para encerrar a conexão com os arquivos
     * 
     * @throws Exception
     */
    public void finalizar() throws Exception {
        DAOConta.close();
        dao = null;
    }
}
