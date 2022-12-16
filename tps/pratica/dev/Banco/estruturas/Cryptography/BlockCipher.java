package estruturas.Cryptography;

/**
 * @author Leon Jr Martins Ferreira
 */
public class BlockCipher implements Cryptable {

    public static final int BLOCK_SIZE = 6; // 6 Bytes
    public static String DEFAULT_KEY;
    private int indexCipher = 0;
    private static final String VI = "_asdqwe123456*.*";

    public void setKey(String key) {
        DEFAULT_KEY = key;
    }

    public BlockCipher(String key) {
        DEFAULT_KEY = key;
    }

    public BlockCipher() {
        DEFAULT_KEY = "teste*.*123";
    }

    @Override
    public String crypt(String base) {
        if (base != null && base.length() > 0 && !base.equals(" "))
            return blockEngine(base.toCharArray(), true);
        else
            return "";
    }

    @Override
    public String decrypt(String base) {
        if (base != null && base.length() > 0 && !base.equals(" "))
            return blockEngine(base.toCharArray(), false);
        else
            return "";
    }

    /**
     * Motor de criptografia baseada em blocos
     * Cada chave criada para criptografar é baseada no bloco anterior
     * É utilizada uma chave inicial padronizada que pode ser alterada
     * 
     * @param data    mensagem para ser criptografada
     * @param isCrypt true - Se for criptografia false - se for descriptografia
     * @return Uma string total criptografada
     */
    private String blockEngine(char[] data, boolean isCrypt) {
        try {
            int indexResposta = 0;
            int ascCode;
            String newkey = "";
            String cipherText = "";
            char[] key = Keygen.generateKey(data.length > BLOCK_SIZE ? BLOCK_SIZE : data.length, DEFAULT_KEY,
                    VI), block,
                    resp = new char[data.length];
            indexCipher = 0;
            do {
                block = getBlock(data);
                for (int i = 0; i < block.length; i++) {
                    ascCode = isCrypt ? ((block[i] ^ key[i]))
                            : (key[i] ^ ((block[i] > 255) ? (char) (block[i] - 256) : block[i]));
                    resp[indexResposta] = isCrypt ? (ascCode < 16 ? ((char) (256 + ascCode))
                            : ((char) ascCode)) : ((char) ascCode);
                    indexResposta++;
                }
                // usando bloco para criar nova chave
                if (indexCipher < data.length) {
                    newkey = isCrypt ? new String(block)
                            : new String(resp).substring((indexResposta - block.length), indexResposta);
                    key = Keygen.generateKey(data.length > BLOCK_SIZE ? BLOCK_SIZE : data.length, newkey, DEFAULT_KEY);
                }
            } while (indexCipher < data.length);
            cipherText = new String(resp);
            indexCipher = 0;
            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Função para buscar blocos de tamanho dinamico dentro de um texto
     * 
     * @param data
     * @return Um bloco de byte contendo informações para criptografar
     */
    private char[] getBlock(char[] data) throws Exception {
        int rows = BLOCK_SIZE;
        if (((data.length - indexCipher) - BLOCK_SIZE) < 0) {
            rows = BLOCK_SIZE - (BLOCK_SIZE - (data.length - indexCipher));
        }
        char[] value = new char[rows];
        for (int j = 0; j < rows && indexCipher < data.length; j++, indexCipher++) {
            value[j] = data[indexCipher];
        }
        return value;
    }

}