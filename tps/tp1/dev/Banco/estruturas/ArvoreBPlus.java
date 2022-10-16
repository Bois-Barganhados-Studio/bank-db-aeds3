package estruturas;

class Chave {
  public int id;
  public long pointer;

  public Chave(int id, long pointer) {
    this.id = id;
    this.pointer = pointer;
  }
}

class No {
  public int ordem;
  public Chave[] chaves;
  public No[] filhos;
  public int nChaves;
  public boolean folha;
  public No irmao;

  public No(int ordem) {
    this.ordem = ordem;
    this.chaves = new Chave[ordem - 1];
    this.filhos = new No[ordem];
    this.nChaves = 0;
    this.folha = true;
    this.irmao = null;
  }
}

public class ArvoreBPlus {
  private int ordem;
  private No raiz;
  private int nElementos;

  public ArvoreBPlus(int ordem) {
    this.ordem = ordem;
    this.raiz = new No(ordem);
    this.nElementos = 0;
  }

  public void inserir(int id, long pointer) {
    this.raiz = inserir(this.raiz, null, id, pointer);
  }

  private No inserir(No no, No pai, int id, long pointer) {

    // Se o nó for folha, insere a chave
    if (no.folha) {
      no = inserirChave(no, id, pointer);
      imprimir();
      this.nElementos++;
      // Se o nó estiver cheio, divide o nó
      if (no.nChaves == no.ordem - 1) {
        int i = 0;
        if (pai != null) {
          while (i < pai.nChaves && id > pai.chaves[i].id) {
            i++;
          }
          pai = dividir(no, pai, i);
        } else {
          pai = dividir(no, pai, i);
          return pai;
        }
      }
    } else {
      // Se o nó não for folha, procura o filho onde a chave deve ser inserida
      int i = 0;
      while (i < no.nChaves && id > no.chaves[i].id) {
        i++;
      }
      no.filhos[i] = inserir(no.filhos[i], no, id, pointer);
      // Se o filho estiver cheio, divide o filho
      if (no.filhos[i].nChaves == no.ordem - 1) {
        no = dividir(no.filhos[i], no, i);
      }
    }
    return no;
  }

  private No inserirChave(No no, int id, long pointer) {
    int i = no.nChaves - 1;
    while (i >= 0 && no.chaves[i] != null && id < no.chaves[i].id) {
      no.chaves[i + 1] = no.chaves[i];
      i--;
    }
    no.chaves[i + 1] = new Chave(id, pointer);
    no.nChaves++;

    return no;
  }

  private No dividir(No no, No pai, int i) {
    // Cria um novo nó
    No novo = new No(no.ordem);
    // Copia a metade das chaves para o novo nó
    int meio = no.nChaves / 2;

    System.out.println("meio: " + meio);
    System.out.println("no.chaves[meio]: " + no.chaves[meio].id);
    for (int j = meio; j < no.ordem - 1; j++) {
      novo.chaves[j - meio] = no.chaves[j];
      novo.nChaves++;
      no.nChaves--;
    }
    // Se o nó não for folha, copia a metade dos filhos para o novo nó
    if (!no.folha) {
      for (int j = meio; j < no.ordem; j++) {
        novo.filhos[j - meio] = no.filhos[j];
      }
    }
    // Atualiza o número de chaves do nó
    no.nChaves = meio;

    // Se o nó for folha, atualiza o ponteiro para o próximo nó
    if (no.folha) {
      no.irmao = novo;
    }
    // Se o nó não tiver pai, cria um novo nó pai
    if (pai == null) {
      pai = new No(no.ordem);
      pai.filhos[0] = no;
      pai.nChaves++;
    }
    // Insere a chave do meio do nó no pai
    pai = inserirChave(pai, no.chaves[meio].id, no.chaves[meio].pointer);
    // Insere o novo nó no pai
    for (int j = pai.nChaves - 1; j > i; j--) {
      pai.filhos[j + 1] = pai.filhos[j];
    }
    pai.filhos[i + 1] = novo;
    // Atualiza o número de filhos do pai
    pai.nChaves++;
    return pai;
  }

  public long buscar(int id) {
    return buscar(this.raiz, id);
  }

  private long buscar(No no, int id) {
    // Se o nó for folha, retorna o ponteiro da chave
    if (no.folha) {
      for (int i = 0; i < no.nChaves; i++) {
        if (no.chaves[i].id == id) {
          return no.chaves[i].pointer;
        }
      }
      return -1;
    } else {
      // Se o nó não for folha, procura o filho onde a chave deve estar
      int i = 0;
      while (i < no.nChaves && id > no.chaves[i].id) {
        i++;
      }
      return buscar(no.filhos[i], id);
    }
  }

  public long[] buscar(int id, int tamanho) {
    return buscar(this.raiz, id, tamanho);
  }

  private long[] buscar(No no, int id, int tamanho) {
    // Se o nó for folha, retorna os ponteiros das chaves
    if (no.folha) {
      long[] ponteiros = new long[tamanho];
      int j = 0;
      // Percorre as chaves do nó e dos irmãos
      for (No n = no; n != null; n = n.irmao) {
        for (int i = 0; i < n.nChaves; i++) {
          if (n.chaves[i].id <= id) {
            ponteiros[j] = n.chaves[i].pointer;
            j++;
            if (j == tamanho) {
              return ponteiros;
            }
          }
        }
      }
      return ponteiros;
    } else {
      // Se o nó não for folha, procura o filho onde a chave deve estar
      int i = 0;
      while (i < no.nChaves && id > no.chaves[i].id) {
        i++;
      }
      return buscar(no.filhos[i], id, tamanho);
    }
  }

  public void imprimir() {
    imprimir(this.raiz, 0);
    System.out.println("");
  }

  private void imprimir(No no, int nivel) {
    if (no != null) {
      for (int i = 0; i < no.nChaves; i++) {
        imprimir(no.filhos[i], nivel + 1);
        for (int j = 0; j < nivel; j++) {
          System.out.print("  ");
        }
        if (no.chaves[i] != null) {
          System.out.print(no.chaves[i].id + " ");
        }
      }
      System.out.println("");
      imprimir(no.filhos[no.nChaves], nivel + 1);
    }
  }
}
