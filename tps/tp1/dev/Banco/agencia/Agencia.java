package agencia;

import java.util.InputMismatchException;
import java.util.Scanner;

/*
 * Classe para fazer interface com o usuário e realizar ações de IO do banco
 */

public class Agencia {

    public static Scanner scan = new Scanner(System.in);
    public static boolean running = false;

    public Agencia() {
        running = true;
    }

    public static void main(String[] args) {
        int op = 0;
        try {
            System.out.println("============================================================");
            System.out.println("                       BANCO MAGICO                         ");
            System.out.println("                      THE MAGIC BANK                        ");
            System.out.println("============================================================");
            running = true;
            while (running) {
                try {
                    System.out.println("\nBem Vindo ao banco mágico, escolha uma opção:");
                    System.out.println("1- Criar conta no banco magic:");
                    System.out.println("2- Transferência entre contas:");
                    System.out.println("3- Verificar conta e ler dados:");
                    System.out.println("4- Atualizar dados de conta:");
                    System.out.println("5- Deletar conta do banco:");
                    System.out.println("6- Ordenar arquivo de contas:");
                    System.out.println("0- Sair do sistema e finalizar operações");
                    System.out.println("Digite a opção de operação:");
                    op = scan.nextInt();
                    if (op != 0)
                        menu(op);
                    else
                        running = false;
                    System.out.println("\n---------------------------------------------------------------");
                } catch (Exception e) {
                    System.err.println("Erro no loop da interface com usuário");
                    e.printStackTrace();
                }
            }
            System.out.println();
            System.out.println("===============================================================");
            System.out.println("                       OBRIGADO POR USAR                       ");
            System.out.println("                         THE MAGIC BANK                        ");
            System.out.println("===============================================================");
        } catch (Exception e) {
            System.err.println("Erro no main da agencia");
            e.printStackTrace();
        }
    }

    public static void menu(int op) {
        try {
            switch (op) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                default:
                    throw new InputMismatchException(op + " não é válido para executar no sistema.");
            }
        } catch (InputMismatchException im) {
            System.err.println("Opção inválida para o menu!");
        }
    }

}
