package estruturas.LZW;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * @author Edmar Oliveira
 * @author Leon Junio Martins
 */
public class LZW {
  private File srcFile;
  private RandomAccessFile file;
  private RandomAccessFile output;
  private ArrayList<Byte[]> dicionario;
  public static final String VERSION = "V1.0-";
  private boolean path = false;

  /**
   * Contrutor do LZW para informa se o path dos caminhos é fixo ou posicional de
   * acordo com o local de
   * execução
   * 
   * @param srcFile arquivo de fonte para realizar ações de compressão
   * @param path    booleano para definir se o caminho de execução é local ou fixo
   */
  public LZW(File srcFile, boolean path) throws IOException {
    this.srcFile = srcFile;
    file = new RandomAccessFile(srcFile, "r");
    dicionario = new ArrayList<Byte[]>();
    this.path = path;
  }

  /**
   * Metodo de compressão do LZW
   * 
   * @throws IOException
   */
  public File compress() throws IOException {
    ArrayList<Integer> compressed = new ArrayList<Integer>(); // Lista de indices do dicionario
    int maiorIndice = 0; // Maior indice do dicionario para calcular quantos bits serao necessarios
    output = new RandomAccessFile(outputCompressFile(srcFile), "rw");

    initialDictionary(); // Cria o dicionario inicial
    writeDictionary(); // Escreve o dicionario no arquivo de saida

    file.seek(0);

    // Para cada byte do arquivo
    do {
      ArrayList<Byte> palavra = new ArrayList<Byte>(); // Palavra a ser adicionada ao dicionario
      long posInicial = file.getFilePointer();
      int aproveitamentos = 0; // Quantidade de bytes que foram aproveitados (para pular no arquivo)
      do { // Enquanto a palavra estiver no dicionario, concatenar com o proximo byte
        if (file.getFilePointer() < file.length()) {
          palavra.add(file.readByte());
          if (containsByteArray(dicionario, palavra))
            aproveitamentos++;
        }
      } while (containsByteArray(dicionario, palavra) && file.getFilePointer() < file.length());

      addToDictionary(palavra); // Adiciona a palavra ao dicionario

      file.seek(posInicial + aproveitamentos); // Pula os bytes que foram aproveitados

      palavra.remove(palavra.size() - 1); // Remove o ultimo byte da palavra (que nao esta no dicionario)

      int index = indexOfByteArray(dicionario, palavra);
      compressed.add(index); // Adiciona o indice da palavra no dicionario a lista de indices
      if (index > maiorIndice)
        maiorIndice = index;
    } while (file.getFilePointer() < file.length());

    // A partir desse momento a saida está pronta na lista de indices
    // Mas é necessario escrever bit a bit no arquivo de saida
    int bits = (int) Math.ceil(Math.log(maiorIndice) / Math.log(2)); // Quantidade de bits necessarios para representar
                                                                     // o maior indice
    output.writeByte(bits); // Escreve a quantidade de bits de cada indice no arquivo de saida
    output.writeInt(compressed.size()); // Escreve a quantidade de indices no arquivo de saida

    String binario = ""; // String que vai armazenar os bits que serao escritos no arquivo
    for (int i = 0; i < compressed.size(); i++) { // Para cada indice
      binario += toBinary(compressed.get(i), bits);
    }
    // Adicionar 0s no final da string para completar o ultimo byte
    int resto = binario.length() % 8;
    if (resto != 0) {
      for (int i = 0; i < 8 - resto; i++) {
        binario += "0";
      }
    }
    // Escrever os bytes no arquivo
    for (int i = 0; i < binario.length(); i += 8) {
      output.writeByte(Integer.parseInt(binario.substring(i, i + 8), 2));
    }

    file.close();
    return srcFile;
  }

  private File outputCompressFile(File srcFile) {
    return new File(
        !path ? srcFile.getParent() : "db" + File.separator + "Compress-LZW_" + VERSION + srcFile.getName());
  }

  private File outputDecompressFile(File srcFile) {
    return new File(
        !path ? srcFile.getParent() : "db" + File.separator + "Descompress-LZW_" + VERSION + srcFile.getName());
  }

  private void initialDictionary() throws IOException {
    file.seek(0); // Volta para o inicio do arquivo
    do { // Para cada byte do arquivo
      Byte[] b = { file.readByte() };
      if (!containsByteArray(dicionario, b)) // Se o byte nao estiver no dicionario
        dicionario.add(b);
    } while (file.getFilePointer() < file.length());
  }

  private void addToDictionary(ArrayList<Byte> palavra) {
    Byte[] b = new Byte[palavra.size()];
    for (int i = 0; i < palavra.size(); i++)
      b[i] = palavra.get(i);
    dicionario.add(b);
  }

  private void writeDictionary() throws IOException {
    output.writeByte(dicionario.size());
    for (Byte[] b : dicionario) {
      output.writeByte(b[0]);
    }
  }

  private String toBinary(int num, int bits) {
    String binary = Integer.toBinaryString(num);
    int length = binary.length();
    if (length > bits)
      return binary.substring(length - bits);
    else if (length < bits) {
      String zeros = "";
      for (int i = 0; i < bits - length; i++)
        zeros += "0";
      return zeros + binary;
    } else
      return binary;
  }

  private boolean containsByteArray(ArrayList<Byte[]> list, Byte[] b) {
    for (Byte[] b2 : list) {
      if (b2.length == b.length) {
        boolean equals = true;
        for (int i = 0; i < b.length; i++) {
          if (b2[i] != b[i]) {
            equals = false;
            break;
          }
        }
        if (equals)
          return true;
      }
    }
    return false;
  }

  private boolean containsByteArray(ArrayList<Byte[]> list, ArrayList<Byte> b) {
    for (Byte[] b2 : list) {
      if (b2.length == b.size()) {
        boolean equals = true;
        for (int i = 0; i < b.size(); i++) {
          if (b2[i] != b.get(i)) {
            equals = false;
            break;
          }
        }
        if (equals)
          return true;
      }
    }
    return false;
  }

  private int indexOfByteArray(ArrayList<Byte[]> list, ArrayList<Byte> b) {
    for (int i = 0; i < list.size(); i++) {
      Byte[] b2 = list.get(i);
      if (b2.length == b.size()) {
        boolean equals = true;
        for (int j = 0; j < b.size(); j++) {
          if (b2[j] != b.get(j)) {
            equals = false;
            break;
          }
        }
        if (equals)
          return i;
      }
    }
    return -1;
  }

  /**
   * Metodo de descompressão do LZW percorrendo pré dicionario
   * 
   * @throws IOException
   */
  public File decompress() throws IOException {
    File outputFile = outputDecompressFile(srcFile);
    try (RandomAccessFile outputDecompress = new RandomAccessFile(outputFile, "rw")) {
      output = new RandomAccessFile(outputCompressFile(srcFile), "rw");
      dicionario = new ArrayList<Byte[]>();
      output.seek(0);
      readDictionary(); // Le o dicionario inicial do arquivo ja comprimido

      int bits = output.readByte(); // Le a quantidade de bits de cada indice
      int size = output.readInt(); // Le a quantidade de indices

      String binario = ""; // String que vai armazenar os bits lidos do arquivo

      while (output.getFilePointer() < output.length()) {
        binario += toBinary(output.readByte(), 8); // Preenche a string com os bits lidos do arquivo
      }

      binario = binario.substring(0, size * bits); // Remove os bits que nao sao indices (0s adicionados no final)

      // Para cada indice
      for (int i = 0; i < binario.length(); i += bits) {
        int index = Integer.parseInt(binario.substring(i, i + bits), 2); // Converte o indice de binario para decimal

        Byte[] b = dicionario.get(index); // Pega o byte correspondente ao indice
        // Sempre deverá existir o primeiro byte do dicionario

        // Escreve o byte no arquivo de saida
        for (int j = 0; j < b.length; j++) {
          outputDecompress.writeByte(b[j]);
        }

        // Agora, tratar o caso em que o indice nao existe no dicionario
        // E começar a criar novos indices
        if (i + bits < binario.length()) {
          int nextIndex = Integer.parseInt(binario.substring(i + bits, i + bits + bits), 2); // Pega o proximo indice

          if (nextIndex < dicionario.size()) { // Se o proximo indice existir no dicionario
            Byte[] next = dicionario.get(nextIndex); // Pega o byte correspondente ao proximo indice
            Byte[] newWord = new Byte[b.length + 1];

            // Cria uma nova palavra com o primeiro byte da palavra atual e o primeiro byte
            // da proxima palavra
            for (int j = 0; j < b.length; j++) {
              newWord[j] = b[j];
            }
            newWord[b.length] = next[0];

            dicionario.add(newWord);
          } else { // Se o proximo indice nao existir no dicionario (caso especial)
            // Apenas cria uma nova palavra a partir da palavra atual repetida
            Byte[] newWord = new Byte[b.length + 1];
            for (int j = 0; j < b.length; j++) {
              newWord[j] = b[j];
            }
            newWord[b.length] = b[0];
            dicionario.add(newWord);
          }
        }
      }
    }
    return outputFile;
  }

  private void readDictionary() throws IOException {
    int size = output.readByte();
    for (int i = 0; i < size; i++) {
      Byte[] b = { output.readByte() };
      dicionario.add(b);
    }
  }

  public void close() throws IOException {
    output.close();
    file.close();
    dicionario.clear();
    dicionario = null;
  }

  public boolean doTeste() throws IOException {
    boolean sit = false;
    sit = this.compress().isFile() && this.decompress().isFile();
    this.close();
    return sit;
  }
}
