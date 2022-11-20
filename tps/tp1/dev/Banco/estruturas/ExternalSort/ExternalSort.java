package estruturas.ExternalSort;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import agencia.Operacoes;
import conta.Conta;
import conta.DAOConta;

/*
 * Classe para realizar a ordenação externa do arquivo de contas do banco de dados
 * Algoritmo utilizado: Intercalação Balanceada de N caminhos
 * @author Leon Junio
 * @author Edmar Oliveira
 */

public class ExternalSort {

    private RandomAccessFile databaseFile;
    private List<Conta> registros;
    private HashMap<Integer, Conta> hashContas;
    private RandomAccessFile[] saidaTemporaria;
    private RandomAccessFile[] entradaTemporaria;
    private long[] bytesRestantesTmp;
    private int[] totalRegistros;
    private Conta auxRegistro;
    private int totalArquivos, registroMemoria, numPrimRead, numPrimWrite, indexMenorRegistro, numTmpPrim, numTmpSec;
    private String fileTmp;
    private String tipoTmp;
    private String databaseFileName, databaseSaida;
    private boolean NEWFILE = false;
    private int lastId, total;

    /**
     * Construtor da classe de ordenação externa do sistema de distribuição e
     * intercalação
     * 
     * @param databaseFileName Arquivo alvo para realizar a distribuição e
     *                         intercalação (base de dados)
     * @param totalArquivos    Total de arquivos que vão ser criados para ordenar
     *                         (N)
     * @param registroMemoria  Tamanho do bloco de memoria alocado para realizar as
     *                         operações
     * @param NEWFILE          booleano que define se um novo arquivo de dados vai
     *                         ser gerado no final da ordenação
     * @throws IOException Erro interno ao tentar ler os dados de I/O
     */
    public ExternalSort(String databaseFileName, int registroMemoria, int totalArquivos, boolean NEWFILE)
            throws IOException {
        this.NEWFILE = NEWFILE;
        this.totalArquivos = totalArquivos;
        this.registroMemoria = registroMemoria;
        bytesRestantesTmp = new long[totalArquivos];
        totalRegistros = new int[totalArquivos];
        hashContas = new HashMap<Integer, Conta>(totalArquivos);
        this.databaseFileName = databaseFileName;
        databaseFile = new RandomAccessFile(databaseFileName, "rw");
        entradaTemporaria = new RandomAccessFile[totalArquivos];
        saidaTemporaria = new RandomAccessFile[totalArquivos];
        fileTmp = Operacoes.ARQTEMP_SORT;
        tipoTmp = ".tmp";
        databaseSaida = Operacoes.DATABASE_SORTED;
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalArquivos() {
        return totalArquivos;
    }

    public void setTotalArquivos(int totalArquivos) {
        this.totalArquivos = totalArquivos;
    }

    public String getDatabaseSaida() {
        return databaseSaida;
    }

    public void setDatabaseSaida(String databaseSaida) {
        this.databaseSaida = databaseSaida;
    }

    public int getRegistroMemoria() {
        return registroMemoria;
    }

    public void setRegistroMemoria(int registroMemoria) {
        this.registroMemoria = registroMemoria;
    }

    /**
     * Função para chamar e iniciar os submetodos de ordenação para distribuir e
     * intercalação
     * 
     * @return Booleano caso a ordenação dê certos
     * @throws Exception
     */
    public boolean sortExternal() throws Exception {
        System.out.println("Distribuindo");
        distribuicao();
        System.out.println("Realizando Intercalação");
        intercalacao();
        System.out.println("Ordenação externa completa com sucesso!");
        return true;
    }

    /**
     * Metodo para iniciar arquivos temporários
     */
    private void iniciarSaidaTemps() {
        try {
            for (int i = 0; i < totalArquivos; i++) {
                saidaTemporaria[i] = new RandomAccessFile(fileTmp + (i + numPrimWrite) + tipoTmp, "rw");
            }
        } catch (IOException e) {
            System.err.println("Falha ao iniciar arquivos temporários");
            e.printStackTrace();
        }
    }

    /**
     * Função para ler os dados de dentro de um arquivo com acesso randomico
     * 
     * @param rf Arquivo randomico para ler dados de uma conta
     * @return Conta que foi lida do arquivo
     * @throws Exception
     */
    public Conta readConta(RandomAccessFile rf) throws Exception {
        Conta reg = null;
        char lapide = rf.readChar();
        int sizeReg = rf.readInt();
        byte[] bytearray = new byte[sizeReg];
        rf.read(bytearray);
        if (lapide != '*') {
            reg = new Conta();
            reg.fromByteArray(bytearray);
        }
        return reg;
    }

    /**
     * Função para resgatar dados de um arquivo temporário e salvar no hash de
     * controle e na lista de registros atuais do arquivo temporário
     * 
     * @throws Exception
     */
    private void resgatarDadosTmp() throws Exception {
        Conta registro = null;
        for (int i = 0; i < totalArquivos; i++) {
            registro = getRegistroTmp(i);
            if (registro != null) {
                hashContas.put(i, registro);
            }
            totalRegistros[i]++;
        }
    }

    /**
     * Função para receber uma conta de dentro de um arquivo temporário
     * 
     * @param index indice do arquivo temporário para procurar da lista de arquivos
     * @return Conta localizada dentro do arquivo
     * @throws Exception
     */
    private Conta getRegistroTmp(int index) throws Exception {
        Conta registro = null;
        if (entradaTemporaria[index].getFilePointer() != entradaTemporaria[index].length())
            registro = readConta(entradaTemporaria[index]);
        return registro;
    }

    /**
     * Função que retorna um numero de arquivos para ler internamente
     * 
     * @return total de arquivos disponiveis para ler
     */
    private int numeroArqsLer() {
        int totFiles = 0;
        try {
            for (int i = 0; i < totalArquivos; i++) {
                bytesRestantesTmp[i] = entradaTemporaria[i].length() - entradaTemporaria[i].getFilePointer();
                if (bytesRestantesTmp[i] > 0) {
                    totFiles++;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao verificar os dados de bytes nas temps");
            e.printStackTrace();
        }
        return totFiles;
    }

    /**
     * Alternar entre arquivos temporarios que estão sendo ordenados internamente
     * 
     * @throws Exception
     */
    private void alternarTmpFiles() throws Exception {
        for (int i = 0; i < totalArquivos; i++) {
            entradaTemporaria[i].close();
            saidaTemporaria[i].close();
        }
        numPrimRead = numPrimRead == 0 ? totalArquivos : 0;
        numPrimWrite = numPrimWrite == 0 ? totalArquivos : 0;
        for (int i = 0; i < totalArquivos; i++) {
            entradaTemporaria[i] = new RandomAccessFile(fileTmp + (i + numPrimRead) + tipoTmp, "rw");
            saidaTemporaria[i] = new RandomAccessFile(fileTmp + (i + numPrimWrite) + tipoTmp, "rw");
        }
    }

    /**
     * Função que pega um indentificador do indice de um arquivo temporário para
     * acessar arquivos dentro de um array ou dentro de funções
     * 
     * @param index parametro de indice do arquivo
     * @return
     */
    private int getIdentificadorArq(int index) {
        if (index == 0) {
            return ((totalArquivos - 1) + numPrimRead);
        } else {
            return ((index - 1) + numPrimRead);
        }
    }

    /**
     * Função para adicionar registros dentro de uma lista baseado em um arquivo
     * temporário
     * 
     * @throws Exception
     */
    private void lerRegistros() throws Exception {
        try {
            for (int i = 0; i < registroMemoria; i++) {
                if (!isAvaliable())
                    registros.add(readConta(databaseFile));
                else
                    i = registroMemoria;
            }
        } catch (Exception e) {
            System.err.println("Erro interno ao ler registros e salvar internamente");
            e.printStackTrace();
        }
    }

    /**
     * Função que verifica se os dados estão disponiveis para leitura dentro do
     * arquivo original de dados (database)
     */
    private boolean isAvaliable() throws Exception {
        return databaseFile.getFilePointer() == databaseFile.length();
    }

    /**
     * Função para finalizar as conexões com arquivos temporários
     */
    private void finalizarSaidaTmp() {
        try {
            for (int i = 0; i < totalArquivos; i++) {
                saidaTemporaria[i].close();
            }
        } catch (IOException e) {
            System.err.println("Falha ao finalizar conexão com arquivos temporários");
            e.printStackTrace();
        }
    }

    /**
     * Método principal para iniciar o processo de distribuição no qual chama e
     * inicia todos os arquivos temporários enquanto distribui e ordena os dados
     * dentro de cada arquivo temporário
     * 
     * @throws Exception
     */
    private void distribuicao() throws Exception {
        int indexInsercao = 0;
        setLastId(databaseFile.readInt());
        setTotal(databaseFile.readInt());
        numPrimWrite = 0;
        iniciarSaidaTemps();
        registros = new ArrayList<>(registroMemoria);
        while (!isAvaliable()) {
            registros.clear();
            lerRegistros();
            Collections.sort(registros);
            for (Conta item : registros) {
                saidaTemporaria[indexInsercao].writeChar(' ');
                saidaTemporaria[indexInsercao].writeInt(item.toByteArray().length);
                saidaTemporaria[indexInsercao].write(item.toByteArray());
            }
            indexInsercao = (indexInsercao + 1) % totalArquivos;
        }
        finalizarSaidaTmp();
        databaseFile.close();
    }

    /**
     * Método de intercalação principal que chama as funções principais para acessar
     * os arquivos temporários gerados pela distribuição e mesclar os dados de cada
     * arquivo temporário até restar um arquivo apenas
     * 
     * @throws Exception
     */
    private void intercalacao() throws Exception {
        int indexInsercao = 0;
        numPrimRead = 0;
        numPrimWrite = totalArquivos;
        for (int i = 0; i < totalArquivos; i++) {
            entradaTemporaria[i] = new RandomAccessFile(
                    fileTmp + (i + numPrimRead) + tipoTmp, "rw");
            saidaTemporaria[i] = new RandomAccessFile(
                    fileTmp + (i + numPrimWrite) + tipoTmp, "rw");
        }
        while (!(numTmpPrim == 1 && numTmpSec == 0 || numTmpPrim == 0 && numTmpSec == 1)) {
            mesclarRegistros(indexInsercao);
            numTmpPrim = numeroArqsLer();
            if (numTmpPrim == 0) {
                alternarTmpFiles();
                registroMemoria = registroMemoria * totalArquivos;
                numTmpSec = numeroArqsLer();
            }
            indexInsercao = (indexInsercao + 1) % totalArquivos;
        }
        for (int i = 0; i < totalArquivos; i++) {
            entradaTemporaria[i].close();
            saidaTemporaria[i].close();
        }
        int numArq = getIdentificadorArq(indexInsercao);
        if (NEWFILE) {
            // adicionar em um arquivo novo
            if (databaseSaida.equals(databaseFileName)) {
                throw new Exception("Nome de arquivo alvo inválido para transferir dados ordenados.");
            }
            File alvo = new File(databaseSaida);
            if (alvo.exists()) {
                alvo.delete();
            }
            RandomAccessFile tempFile = new RandomAccessFile(fileTmp + numArq + tipoTmp, "rw");
            RandomAccessFile alvoFinal = new RandomAccessFile(databaseSaida, "rw");
            addCabecalho(tempFile, alvoFinal);
            tempFile.close();
            alvoFinal.close();
        } else {
            // adicionar no mesmo arquivo
            String tempBkp = Operacoes.TEMP;
            File fl = new File(tempBkp);
            if (fl.exists()) {
                fl.delete();
            }
            RandomAccessFile tempFile = new RandomAccessFile(fileTmp + numArq + tipoTmp, "rw");
            RandomAccessFile alvoFinal = new RandomAccessFile(tempBkp, "rw");
            addCabecalho(tempFile, alvoFinal);
            tempFile.close();
            alvoFinal.close();
            DAOConta.close();
            File flf = new File(databaseFileName);
            if (flf.exists() && flf.delete()) {
                Files.move(fl.toPath(), flf.toPath());
                DAOConta.open();
            } else
                System.err.println("Arquivo de dados esta em uso no momento não pode ser apagado!");
        }
        // finalizando arquivos temporários e excluindo dados temporários
        for (int i = 0; i < 2 * totalArquivos; i++) {
            File f = new File(fileTmp + i + tipoTmp);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * Função para adicionar informações de ultimo ID e total de registros dentro do
     * novo arquivo que acabou de ser ordenado
     * 
     * @param tmp  Arquivo temporario pre ordenado
     * @param alvo Arquivo ordenado que vai ser o alvo para inserção do cabeçalho
     * @throws Exception
     */
    private void addCabecalho(RandomAccessFile tmp, RandomAccessFile alvo) throws Exception {
        alvo.writeInt(getLastId());
        alvo.writeInt(getTotal());
        while (tmp.getFilePointer() != tmp.length()) {
            Conta c = readConta(tmp);
            alvo.writeChar(' ');
            alvo.writeInt(c.toByteArray().length);
            alvo.write(c.toByteArray());
        }
    }

    /**
     * Função para mesclar os registros dos arquivos temporários em um unico arquivo
     * e realizar a intercalação propriamente dita
     * 
     * @param index
     * @throws Exception
     */
    private void mesclarRegistros(int index) throws Exception {
        Conta registro = null;
        hashContas.clear();
        for (int i = 0; i < totalArquivos; i++) {
            totalRegistros[i] = 0;
        }
        resgatarDadosTmp();
        while (hashContas.size() > 0) {
            auxRegistro = Collections.min(hashContas.values());
            for (Entry<Integer, Conta> dados : hashContas.entrySet()) {
                if (auxRegistro.compareTo(dados.getValue()) == 0) {
                    indexMenorRegistro = dados.getKey();
                    break;
                }
            }
            // escrevendo dados dentro do arquivo temporario
            saidaTemporaria[index].writeChar(' ');
            saidaTemporaria[index].writeInt(auxRegistro.toByteArray().length);
            saidaTemporaria[index].write(auxRegistro.toByteArray());
            if (totalRegistros[indexMenorRegistro] < registroMemoria) {
                registro = getRegistroTmp(indexMenorRegistro);
                if (registro != null) {
                    hashContas.put(indexMenorRegistro, registro);
                    totalRegistros[indexMenorRegistro]++;
                } else {
                    totalRegistros[indexMenorRegistro] = registroMemoria;
                    hashContas.remove(indexMenorRegistro);
                }
            } else {
                hashContas.remove(indexMenorRegistro);
            }
        }
    }

}
