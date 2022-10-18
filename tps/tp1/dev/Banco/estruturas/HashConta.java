package estruturas;

import java.io.IOException;
import java.io.RandomAccessFile;

public class HashConta {

    public RandomAccessFile getPonteirosEnd() {
        return ponteirosEnd;
    }

    public void setPonteirosEnd(RandomAccessFile ponteirosEnd) {
        this.ponteirosEnd = ponteirosEnd;
    }

    public RandomAccessFile getBucketsList() {
        return bucketsList;
    }

    public void setBucketsList(RandomAccessFile bucketsList) {
        this.bucketsList = bucketsList;
    }

    public int getTamBuck() {
        return tamBuck;
    }

    public void setTamBuck(int tamBuck) {
        this.tamBuck = tamBuck;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getTamBuckBytes() {
        return tamBuckBytes;
    }

    public HashConta(int tamBuck) throws IOException, Exception {
        this.tamBuck = tamBuck;
        this.p = 2;
        ponteirosEnd = new RandomAccessFile("db/hasher.db", "rw");
        bucketsList = new RandomAccessFile("db/buckets.db", "rw");
        // definindo bytes do bucket
        tamBuckBytes = tamBuck * TAM_INT + TAM_INT_2;
        if (!avaliable()) {
            System.out.println("Criando novo HASH");
            startHash();
        } else {
            System.out.println("sistema de hash prontos para operar!");
            ponteirosEnd.seek(0);
            this.p = ponteirosEnd.readInt();
        }
    }

    /**
     * Iniciando hash e inserindo valores iniciais para o bucket e posições
     * 
     * @throws IOException
     */
    public void startHash() throws IOException {
        p = 2;
        ponteirosEnd.setLength((int) Math.pow(2, p) * Long.BYTES);
        bucketsList.setLength(tamBuckBytes * (int) Math.pow(2, p));
        bucketsList.seek(0);
        ponteirosEnd.seek(0);
        ponteirosEnd.writeInt(p);
        int value = (int) Math.pow(2, p);
        createData(value);
    }

    /**
     * Função para dar start nas linhas dos bucket
     * 
     * @param value valor de repetição de linhas
     * @throws IOException
     */
    private void createData(int value) throws IOException {
        System.out.println("valor de repetição " + value);
        for (int i = 0; i < value; i++) {
            ponteirosEnd.writeLong(bucketsList.getFilePointer());
            bucketsList.writeInt(p);
            bucketsList.writeInt(0);
            // bucketsList.writeLong(0);
            // if(value)
            // bucketsList.writeLong(0);
            for (int j = 0; j < tamBuck; j++) {
                bucketsList.writeInt(-1);
                bucketsList.writeLong(-1);
            }
        }
    }

    /**
     * Função que verifica se o o hash foi criado e esta funcionando no momento para
     * receber novos dados e consultas
     * 
     * @return
     * @throws IOException
     */
    public boolean avaliable() throws IOException {
        if (ponteirosEnd.length() > 0 && bucketsList.length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Adição no bucket disponivel
     * 
     * @param qtd
     * @param index
     * @param pointer
     * @throws IOException
     */
    private void minusTamBkt(int qtd, int index, long pointer) throws IOException {
        bucketsList.seek(bucketsList.getFilePointer() - Integer.BYTES);
        bucketsList.writeInt(qtd + 1);
        for (int i = 0; i < tamBuck; i++) {
            int chave = bucketsList.readInt();
            long ptrWaiter = bucketsList.readLong();
            if (chave == -1) {
                bucketsList.seek(bucketsList.getFilePointer() - (TAM_INT));
                bucketsList.writeInt(index);
                bucketsList.writeLong(pointer);
                i = tamBuck;
            }
        }
    }

    @Override
    public String toString() {
        return "HashConta [ponteirosEnd=" + ponteirosEnd + ", bucketsList=" + bucketsList + ", tamBuck=" + tamBuck
                + ", p=" + p + ", tamBuckBytes=" + tamBuckBytes + ", TAM_INT=" + TAM_INT + ", TAM_INT_2=" + TAM_INT_2
                + ", CONST_MATH=" + CONST_MATH + "]";
    }

    public HashConta(RandomAccessFile ponteirosEnd, RandomAccessFile bucketsList, int tamBuck, int p,
            int tamBuckBytes) {
        this.ponteirosEnd = ponteirosEnd;
        this.bucketsList = bucketsList;
        this.tamBuck = tamBuck;
        this.p = p;
        this.tamBuckBytes = tamBuckBytes;
    }

    /**
     * O método que adiciona um novo elemento no hash já criado
     * Função responsavel por reparticionar um bucket e realocar caso o mesmo esteja
     * cheio
     * 
     * @param index   index do elemento
     * @param pointer ptr para o elemento
     */
    public void adicionar(int index, long pointer) {
        try {
            System.out.println("INDEX ALVO: " + index);
            long posHash = (index % (int) Math.pow(2, p)) * CONST_MATH;
            System.out.println("Hash pos conta: " + posHash);
            ponteirosEnd.seek(posHash);
            long posBkt = ponteirosEnd.readLong();
            bucketsList.seek(posBkt);
            int pLoc = bucketsList.readInt();
            int qtd = bucketsList.readInt();
            if (qtd < tamBuck) {
                minusTamBkt(qtd, index, pointer);
            } else {
                // bucket cheio com P igual
                if (pLoc == p) {
                    p++;
                    ponteirosEnd.setLength((int) Math.pow(2, p) * Long.BYTES);
                    ponteirosEnd.seek(0);
                    ponteirosEnd.writeInt(p);
                    int value = (int) Math.pow(2, p);
                    for (int i = 0; i < value / 2; i++) {
                        ponteirosEnd.writeLong(i * tamBuckBytes);
                    }
                    for (int i = 0; i < value / 2; i++) {
                        ponteirosEnd.writeLong(i * tamBuckBytes);
                    }
                    bucketsList.setLength(bucketsList.length() + tamBuckBytes);
                    posHash = (index % (int) Math.pow(2, p - 1)) * CONST_MATH + ((int) Math.pow(2, p - 1) * Long.BYTES);
                    ponteirosEnd.seek(posHash);
                    ponteirosEnd.writeLong(bucketsList.length() - tamBuckBytes);
                    int[] keyStore = new int[tamBuck];
                    long[] ptrStore = new long[tamBuck];
                    bucketsList.seek(posBkt);
                    bucketsList.writeInt(pLoc + 1);
                    bucketsList.writeInt(0);
                    for (int i = 0; i < tamBuck; i++) {
                        keyStore[i] = bucketsList.readInt();
                        ptrStore[i] = bucketsList.readLong();
                        bucketsList.seek(bucketsList.getFilePointer() - (TAM_INT));
                        bucketsList.writeInt(-1);
                        bucketsList.writeLong(-1);
                    }
                    bucketsList.seek(bucketsList.length() - tamBuckBytes);
                    bucketsList.writeInt(pLoc + 1);
                    bucketsList.writeInt(0);
                    for (int i = 0; i < tamBuck; i++) {
                        bucketsList.writeInt(-1);
                        bucketsList.writeLong(-1);
                    }
                    for (int i = 0; i < tamBuck; i++) {
                        if (keyStore[i] != -1) {
                            adicionar(keyStore[i], ptrStore[i]);
                        }
                    }
                    adicionar(index, pointer);
                } else {
                    // Bucket cheio e p diferente
                    bucketsList.setLength(bucketsList.length() + tamBuckBytes);
                    posHash = (index % (int) Math.pow(2, p - 1)) * CONST_MATH + ((int) Math.pow(2, p - 1) * Long.BYTES);
                    ponteirosEnd.seek(posHash);
                    ponteirosEnd.writeLong(bucketsList.length() - tamBuckBytes);
                    int[] keyStore = new int[tamBuck];
                    long[] ptrStore = new long[tamBuck];
                    bucketsList.seek(posBkt);
                    bucketsList.writeInt(pLoc + 1);
                    bucketsList.writeInt(0);
                    for (int i = 0; i < tamBuck; i++) {
                        keyStore[i] = bucketsList.readInt();
                        ptrStore[i] = bucketsList.readLong();
                        bucketsList.seek(bucketsList.getFilePointer() - (TAM_INT));
                        bucketsList.writeInt(-1);
                        bucketsList.writeLong(-1);
                    }
                    bucketsList.seek(bucketsList.length() - tamBuckBytes);
                    bucketsList.writeInt(pLoc + 1);
                    bucketsList.writeInt(0);
                    for (int i = 0; i < tamBuck; i++) {
                        bucketsList.writeInt(-1);
                        bucketsList.writeLong(-1);
                    }
                    for (int i = 0; i < tamBuck; i++) {
                        if (keyStore[i] != -1) {
                            adicionar(keyStore[i], ptrStore[i]);
                        }
                    }
                    adicionar(index, pointer);
                }
            }
        } catch (Exception e) {
            System.err.println("Falha ao inserir no HASH\nerro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * FUnção para atualizar um registro de dentro do hash automaticamente após sua
     * atualização dentro da DAO
     * 
     * @param index   indice do objeto de conta
     * @param novoEnd novo endereço dentro do arquivo de contas
     */
    public void atualizar(int index, long novoEnd) {
        try {
            int posHash = (index % (int) Math.pow(2, p)) * CONST_MATH;
            ponteirosEnd.seek(posHash);
            long posBkt = ponteirosEnd.readLong();
            bucketsList.seek(posBkt);
            bucketsList.readInt();
            doMoving(novoEnd, index);
        } catch (Exception e) {
            System.err.println("Falha ao alterar no HASH\nerro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Realiza a mudança de ponteiros dentro do Hash
     * 
     * @param novoEnd endereço alvo que foi recem alterado
     * @param index   indice da conta
     * @throws IOException
     */
    private void doMoving(long novoEnd, int index) throws IOException {
        int qtd = bucketsList.readInt();
        for (int i = 0; i < qtd; i++) {
            int chave = bucketsList.readInt();
            long ptrWaiter = bucketsList.readLong();
            if (chave == index) {
                bucketsList.seek(bucketsList.getFilePointer() - Long.BYTES);
                bucketsList.writeLong(novoEnd);
                i = qtd;
            }
        }
    }

    public int getTAM_INT() {
        return TAM_INT;
    }

    public int getTAM_INT_2() {
        return TAM_INT_2;
    }

    public int getCONST_MATH() {
        return CONST_MATH;
    }

    /**
     * Função para remover contas do hash quando uma mesma for deletada ou quando
     * houver força bruta para deletar
     * 
     * @param index da conta alvo para deletar
     * @return referente ao endereço de remoção
     */
    public long remover(int index) {
        long pointer = -1;
        try {
            int posHash = (index % (int) Math.pow(2, p)) * CONST_MATH;
            ponteirosEnd.seek(posHash);
            long posBkt = ponteirosEnd.readLong();
            bucketsList.seek(posBkt);
            bucketsList.readInt();
            int qtd = bucketsList.readInt();
            int lidos = 0;
            for (int i = 0; i < qtd; i++) {
                int chave = bucketsList.readInt();
                long ptr = bucketsList.readLong();
                lidos++;
                if (chave == index) {
                    pointer = ptr;
                    for (int j = 0; j < tamBuck - lidos; j++) {
                        int tmpChave = bucketsList.readInt();
                        long tmpPonteiro = bucketsList.readLong();
                        bucketsList.seek(bucketsList.getFilePointer() - 2 * (TAM_INT));
                        bucketsList.writeInt(tmpChave);
                        bucketsList.writeLong(tmpPonteiro);
                        bucketsList.skipBytes(TAM_INT);
                    }
                    i = qtd;
                }
            }
        } catch (Exception e) {
            System.err.println("Falha ao remover do HASH\nerro: " + e.getMessage());
            e.printStackTrace();
        }
        return pointer;
    }

    /**
     * Função para localizar indice dentro do hash e retornar seu endereço dentro do
     * arquivo de dados do sistema
     * 
     * @param index para localizar baseado em objeto de Conta
     * @return Endereço relativo ao arquivo de dados de contas
     */
    public long localizar(int index) {
        long pointer = -1;
        try {
            int posHash = (index % (int) Math.pow(2, p)) * CONST_MATH;
            ponteirosEnd.seek(posHash);
            long posBkt = ponteirosEnd.readLong();
            bucketsList.seek(posBkt);
            bucketsList.readInt();
            int qtd = bucketsList.readInt();
            for (int i = 0; i < qtd; i++) {
                int chave = bucketsList.readInt();
                long ptr = bucketsList.readLong();
                if (chave == index) {
                    pointer = ptr;
                    i = qtd;
                }
            }
        } catch (Exception e) {
            System.err.println("Falha ao recuperar dado no HASH\nerro: " + e.getMessage());
            e.printStackTrace();
        }
        return pointer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ponteirosEnd == null) ? 0 : ponteirosEnd.hashCode());
        result = prime * result + ((bucketsList == null) ? 0 : bucketsList.hashCode());
        result = prime * result + tamBuck;
        result = prime * result + p;
        result = prime * result + tamBuckBytes;
        result = prime * result + TAM_INT;
        result = prime * result + TAM_INT_2;
        result = prime * result + CONST_MATH;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HashConta other = (HashConta) obj;
        if (ponteirosEnd == null) {
            if (other.ponteirosEnd != null)
                return false;
        } else if (!ponteirosEnd.equals(other.ponteirosEnd))
            return false;
        if (bucketsList == null) {
            if (other.bucketsList != null)
                return false;
        } else if (!bucketsList.equals(other.bucketsList))
            return false;
        if (tamBuck != other.tamBuck)
            return false;
        if (p != other.p)
            return false;
        if (tamBuckBytes != other.tamBuckBytes)
            return false;
        if (TAM_INT != other.TAM_INT)
            return false;
        if (TAM_INT_2 != other.TAM_INT_2)
            return false;
        if (CONST_MATH != other.CONST_MATH)
            return false;
        return true;
    }

    private RandomAccessFile ponteirosEnd;
    private RandomAccessFile bucketsList;
    private int tamBuck;
    private int p;
    private final int tamBuckBytes;
    private final int TAM_INT = (Integer.BYTES + Long.BYTES);
    private final int TAM_INT_2 = (2 * Integer.BYTES);
    private final int CONST_MATH = 8 + 4;

}