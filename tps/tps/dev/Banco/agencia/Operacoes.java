package agencia;

/*
 * Classe para armazenar operações e ações do nosso banco quanto agencia
 * Tratar exceções de operação DAO nessa classe
 * Não pedir dados por essa classe, todas as operações já devem ter os dados
 * passados por parametro ou por chamada
 * @author Leon Junio
 * @author Edmar Oliveira
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import conta.Conta;
import estruturas.ExternalSort.ExternalSort;
import conta.DAOConta;
import estruturas.Huffman.HuffmanCoding;
import estruturas.LZW.LZW;

public class Operacoes {

    private boolean status;
    private DAOConta dao;

    // CAMINHOS DOS ARQUIVOS DO BD
    private Path arq;
    public static String DATABASE;
    public static String HASHER;
    public static String BUCKETS;
    public static String TEMP;
    public static String DATABASE_SORTED;
    public static String ARQTEMP_SORT;
    public static String LISTA_INVERTIDA_NOME;
    public static String LISTA_INVERTIDA_CITY;
    public static final boolean CREATEFILE = true;
    public static final String TESTE = "db/pic.bmp";

    public Operacoes() {
        try {
            status = false;
            arq = Paths.get(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            String root = "";
            root = arq.toString().endsWith(".jar")
                    ? arq.toString().substring(0, arq.toString().lastIndexOf(File.separator))
                    : arq.toString();
            Operacoes.DATABASE = root + "" + File.separator + "db" + File.separator + "conta_banco.db";
            HASHER = root + "" + File.separator + "db" + File.separator + "hasher.db";
            BUCKETS = root + "" + File.separator + "db" + File.separator + "buckets.db";
            TEMP = root + "" + File.separator + "db" + File.separator + "tempBkp.db";
            DATABASE_SORTED = root + "" + File.separator + "conta_banco_sorted.db";
            ARQTEMP_SORT = root + "" + File.separator + "db" + File.separator + "ArqTemp";
            LISTA_INVERTIDA_NOME = root + "" + File.separator + "db" + File.separator + "listaInvertidaNome.dat";
            LISTA_INVERTIDA_CITY = root + "" + File.separator + "db" + File.separator + "listaInvertidaCidade.dat";
            dao = new DAOConta();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * Sistema que chama o metodo de ordenação externa
     * 
     * @param m        total de registros
     * @param n        total de caminhos
     * @param dataSave opcional para criar novo arquivo ou não
     * @return booleano caso o arquivo seja ordenado com sucesso
     */
    public boolean ordenarArq(int m, int n, int dataSave) throws Exception {
        ExternalSort sorter = new ExternalSort(DATABASE, m, n, dataSave == 1);
        boolean sit = sorter.sortExternal();
        freeRam();
        return sit;
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
     * Operação de Leitura para retornar dados de uma conta especifica usando hash
     * 
     * @param id id para procurar no banco de dados
     * @return
     */
    public Conta lerHash(int id) {
        try {
            return dao.lerHash(id);
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
     * Chama função de compressão dos algoritmos LZW e Huffman
     * 
     * @return
     */
    public boolean compress(int op) throws Exception {
        long start = System.currentTimeMillis(), end = 0;
        File out = null;
        if (op == 1) {
            HuffmanCoding huff = new HuffmanCoding(true);
            out = huff.compress(new File(TESTE));
            freeRam();
            end = System.currentTimeMillis();
        } else {
            LZW lzw = new LZW(new File(TESTE), true);
            out = lzw.compress();
            freeRam();
            end = System.currentTimeMillis();
        }
        comparador(out);
        System.out.println((end - start) + "ms de execução do algoritmo de compressão");
        return out.isFile() && out.length() > 0;
    }

    /**
     * Compara um arquivo com o arquivo de dados criado
     * 
     * @param f arquivo qualquer alvo da comparação
     */
    public void comparador(File f) throws Exception {
        File db = new File(TESTE);
        long bytesComp = f.length();
        long bytesDb = db.length();
        System.out.println(f.getAbsolutePath());
        System.out.println(db.getAbsolutePath());
        System.out.println("Comparação de arquivos:");
        System.out.println("Arquivo gerado pelo algoritmo: " + bytesComp + " bytes");
        System.out.println("Arquivo original do banco de dados: " + bytesDb + " bytes");
        System.out
                .println("Taxa de diferença entre arquivos: " + ((float) bytesDb / bytesComp) + " vezes de diferença");
    }

    /**
     * Chama função de compressão dos algoritmos LZW e Huffman
     * 
     * @return
     */
    public boolean decompress(int op) throws Exception {
        long start = System.currentTimeMillis(), end = 0;
        File out = null;
        if (op == 2) {
            HuffmanCoding huff = new HuffmanCoding(true);
            out = huff.decompress(new File(TESTE), CREATEFILE);
            freeRam();
            end = System.currentTimeMillis();
        } else {
            LZW lzw = new LZW(new File(TESTE), true);
            out = lzw.decompress(CREATEFILE);
            freeRam();
            end = System.currentTimeMillis();
        }
        System.out.println((end - start) + "ms de execução do algoritmo de descompressão");
        return out.isFile() && out.length() > 0;
    }

    /**
     * Função que chama o teste global de LZW e huffman para comprimir e
     * descomprimir com todos os metodos
     */
    public boolean testarCompressoes() throws Exception {
        boolean sit = false;
        LZW lzw = new LZW(new File(DATABASE), true);
        HuffmanCoding huff = new HuffmanCoding(true);
        sit = lzw.doTeste() && huff.doTest(new File("db" + File.separator + "testehuff.db"));
        if (sit) {
            System.out.println("LZW testado com sucesso!");
            System.out.println("Huffman testado com sucesso!");
        }
        return sit;
    }

    /**
     * Metodo para limpar a ram pos operações
     */
    public void freeRam() {
        System.gc();
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
