package estruturas.Cryptography;

public class BlockCipher implements Cryptable {

    public static final int BLOCK_SIZE = 6; // 6 Bytes
    public static final String DEFAULT_KEY = "teste";
    private int indexCipher = 0;

    @Override
    public String crypt(String base) {
        return blockEngine(base.getBytes(), true);
    }

    @Override
    public String decrypt(String base) {
        return blockEngine(base.getBytes(), false);
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
    private String blockEngine(byte[] data, boolean isCrypt) {
        String cipherText = "";
        byte[] key = Keygen.generateKey(data.length > BLOCK_SIZE ? BLOCK_SIZE : data.length, DEFAULT_KEY), block,
                resp = new byte[data.length];
        indexCipher = 0;
        int index = 0;
        do {
            block = getBlock(data);
            for (int i = 0; i < block.length; i++) {
                resp[index] = isCrypt ? ((byte) (block[i] ^ key[i])) : ((byte) (key[i] ^ block[i]));
                index++;
            }
            // usando bloco para criar nova chave
            if (indexCipher < data.length) {
                String newkey = isCrypt ? new String(block)
                        : new String(resp).substring((index - block.length), index);
                key = Keygen.generateKey(data.length > BLOCK_SIZE ? BLOCK_SIZE : data.length, newkey);
            }
        } while (indexCipher < data.length);
        cipherText = new String(resp);
        indexCipher = 0;
        return cipherText;
    }

    /**
     * Função para buscar blocos de tamanho dinamico dentro de um texto
     * 
     * @param data
     * @return Um bloco de byte contendo informações para criptografar
     */
    private byte[] getBlock(byte[] data) {
        int rows = BLOCK_SIZE;
        if (((data.length - indexCipher) - BLOCK_SIZE) < 0) {
            rows = BLOCK_SIZE - (BLOCK_SIZE - (data.length - indexCipher));
        }
        byte[] value = new byte[rows];
        for (int j = 0; j < rows && indexCipher < data.length; j++, indexCipher++) {
            value[j] = data[indexCipher];
        }
        return value;
    }

}
