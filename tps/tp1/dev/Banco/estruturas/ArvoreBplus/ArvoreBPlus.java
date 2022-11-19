package estruturas.ArvoreBplus;

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

  public void print() {
    System.out.print("[");
    for (int i = 0; i < nChaves; i++) {
      System.out.print(chaves[i] == null ? "null" : chaves[i].id);
      if (i < nChaves - 1) {
        System.out.print(", ");
      }
    }
    System.out.print("]");
  }

  public No clone() {
    No novo = new No(ordem);
    novo.nChaves = nChaves;
    novo.folha = folha;
    novo.irmao = irmao;
    for (int i = 0; i < nChaves; i++) {
      novo.chaves[i] = chaves[i];
    }
    for (int i = 0; i <= nChaves; i++) {
      novo.filhos[i] = filhos[i];
    }
    return novo;
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

  public void showOrdemElementos() {
    System.out.println(this.ordem + " - " + this.nElementos);
  }

  public void inserir(int id, long pointer) {
    this.raiz = inserir(this.raiz, null, id, pointer);
  }

  private No inserir(No no, No pai, int id, long pointer) {

    // Se o nó for folha, insere a chave
    if (no.folha) {
      // Se o nó estiver cheio, divide o nó
      if (no.nChaves + 1 == no.ordem) {
        int i = 0;
        while (pai != null && i < pai.nChaves && id > pai.chaves[i].id) {
          i++; // Posição do filho
        }
        if (pai == null) {
          pai = dividir(no, pai, i, id, pointer);
          this.nElementos++;
          return pai;
        } else if (pai.nChaves + 1 == pai.ordem) {

          pai = dividir(no, pai, i, id, pointer);
          this.nElementos++;
          return pai;
        } else {
          dividir(no, pai, i, id, pointer);
          this.nElementos++;

        }
      } else {
        no = inserirChave(no, id, pointer);
        this.nElementos++;
      }
    } else {
      // Se o nó não for folha, procura o filho onde a chave deve ser inserida
      int i = 0;
      while (i < no.nChaves && id > no.chaves[i].id) {
        i++;
      }
      if (no != null && no.nChaves + 1 == no.ordem && no.filhos[i].folha
          && no.filhos[i].nChaves + 1 == no.filhos[i].ordem) {
        no = inserir(no.filhos[i], no, id, pointer);
      } else {
        no.filhos[i] = inserir(no.filhos[i], no, id, pointer);
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

  private No dividir(No no, No pai, int i, int id, long pointer) {
    // Cria um novo nó
    No novo = new No(no.ordem);
    // Copia a metade das chaves para o novo nó
    int meio = no.nChaves / 2;

    for (int j = meio; j < no.ordem - 1; j++) {
      novo.chaves[j - meio] = no.chaves[j];
      novo.nChaves++;
      no.nChaves--;
    }
    // Insere a nova chave no nó correto
    if (id < no.chaves[meio].id) {
      no = inserirChave(no, id, pointer);
    } else {
      novo = inserirChave(novo, id, pointer);
    }

    // Se o nó não for folha, copia a metade dos filhos para o novo nó
    if (!no.folha) {
      for (int j = meio; j < no.ordem; j++) {
        novo.filhos[j - meio] = no.filhos[j];
      }
    }

    // Se o nó for folha, atualiza o ponteiro para o próximo nó
    if (no.folha) {
      no.irmao = novo;
    }
    // Se o nó não tiver pai, cria um novo nó pai
    if (pai == null) {
      pai = new No(no.ordem);
      pai.filhos[0] = no;
      pai.folha = false;
    }
    // Insere a chave do meio do nó no pai
    if (pai.nChaves + 1 == pai.ordem) {
      No tmp = dividir(pai.clone(), null, 0, no.chaves[meio].id, no.chaves[meio].pointer);

      for (int j = 0; j < tmp.ordem; j++) {
        if (tmp.filhos[j] != null) {
          for (int k = 0; k < tmp.ordem; k++) {
            if (tmp.filhos[j].chaves[tmp.filhos[j].nChaves - 1].id >= pai.filhos[k].chaves[0].id &&
                (j == 0 || tmp.filhos[j - 1].chaves[tmp.filhos[j - 1].nChaves - 1].id < pai.filhos[k].chaves[0].id)) {
              tmp.filhos[j].filhos[k - (3 * j)] = pai.filhos[k];
              if (k == tmp.ordem - 1) {
                tmp.filhos[j].filhos[(k + 1) - (3 * j)] = novo;
              }
            }
          }
        }
      }

      pai = tmp;

      return pai;
    } else {
      pai = inserirChave(pai, no.chaves[meio].id, no.chaves[meio].pointer);
      // Insere o novo nó no pai
      for (int j = pai.nChaves - 1; j > i; j--) {
        pai.filhos[j + 1] = pai.filhos[j];
      }
      pai.filhos[i + 1] = novo;

      return pai;
    }
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
    System.out.println("Imprimindo arvore:");
    imprimir(this.raiz, 0);
    System.out.println("\n----\n");
  }

  private void imprimir(No no, int nivel) {
    if (no != null) {
      System.out.print(nivel + ": ");
      no.print();
      System.out.println("");
      for (int i = 0; i < no.ordem; i++) {
        imprimir(no.filhos[i], nivel + 1);
      }
    }
  }
}
