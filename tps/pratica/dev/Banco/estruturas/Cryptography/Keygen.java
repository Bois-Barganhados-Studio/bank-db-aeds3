package estruturas.Cryptography;

/**
 * @author Leon Jr Martins Ferreira
 */
public class Keygen {

    /**
     * Função para gerar bitstream de dados a partir de uma seed
     * 
     * @param size tamanho do bloco alocado no momento
     * @param seed String alvo para virar um bitstream
     * @return uma chave em formato de byte
     */
    public static byte[] generateKey(int size, String seed) {
        byte[] key = new byte[size];
        if (seed.length() < size) {
            int alvo = size - seed.length();
            seed += String.valueOf(seed.charAt(0)).repeat(alvo);
        } else if (seed.length() > size) {
            seed = seed.substring(0, size);
        }
        key = seed.getBytes();
        byte[] stream = String.valueOf(seed.charAt(seed.length() - 1)).repeat(size).getBytes();
        for (int i = 0; i < size; i++) {
            key[i] = (byte) (key[i] ^ stream[i]);
        }
        return key;
    }
}
