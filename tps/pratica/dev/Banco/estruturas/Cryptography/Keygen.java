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
     * @param pass Uma senha para ajudar na formação aleatoria do bitStream
     * @return uma chave em formato de byte
     */
    public static char[] generateKey(int size, String seed, String pass) throws Exception {
        char[] key = new char[size];
        if (seed.length() < size) {
            int alvo = size - seed.length();
            seed += String.valueOf(seed).repeat(alvo);
        } else if (seed.length() > size) {
            seed = seed.substring(0, size);
        }
        key = seed.toCharArray();
        pass = pass.repeat(size);
        int[] stream = pass.chars().map(t -> t = t + size + 7).toArray();
        for (int i = 0; i < size; i++) {
            key[i] = ((char) (key[i] ^ stream[i]));
        }
        return key;
    }

}
