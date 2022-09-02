import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;

public class Main {
    public static void main(String[] args){

        Jogador j1= new Jogador(25, "Conceição", 49.90F);
        Jogador j2= new Jogador(37, "José Carlos", 62.50F);
        Jogador j3= new Jogador(291, "Pedro", 53.45F);

        FileOutputStream arq;
        DataOutputStream dos;

        FileInputStream arq2;
        DataInputStream dis;

        try {

            arq = new FileOutputStream("../dados/jogadores_ds.db");
            dos = new DataOutputStream(arq);      

            dos.writeInt(j1.idJogador);
            dos.writeUTF(j1.nome);
            dos.writeFloat(j1.pontos);

            dos.writeInt(j2.idJogador);
            dos.writeUTF(j2.nome);
            dos.writeFloat(j2.pontos);

            dos.writeInt(j3.idJogador);
            dos.writeUTF(j3.nome);
            dos.writeFloat(j3.pontos);

            dos.close();
            arq.close();

            Jogador j_temp= new Jogador();

            arq2 =  new FileInputStream("../dados/jogadores_ds.db");
            dis = new DataInputStream(arq2);

            j_temp.idJogador= dis.readInt();
            j_temp.nome=dis.readUTF();  
            j_temp.pontos=dis.readFloat();
            System.out.println(j_temp); 
            
            j_temp.idJogador= dis.readInt();
            j_temp.nome=dis.readUTF();  
            j_temp.pontos=dis.readFloat();
            System.out.println(j_temp); 
           
            j_temp.idJogador= dis.readInt();
            j_temp.nome=dis.readUTF();  
            j_temp.pontos=dis.readFloat();
            System.out.println(j_temp); 
           
            

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    
}
