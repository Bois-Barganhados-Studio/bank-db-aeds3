import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;

public class Main {
    public static void main(String[] args){

        Jogador j1= new Jogador(25, "Conceição", 49.90F);
        Jogador j2= new Jogador(37, "José Carlos", 62.50F);
        Jogador j3= new Jogador(291, "Pedro", 53.45F);
        Jogador j_temp= new Jogador();

        FileOutputStream arq;
        DataOutputStream dos;

        FileInputStream arq2;
        DataInputStream dis;

        byte[] ba;
        int len;


        try {

            arq = new FileOutputStream("../dados/jogadores_ds_ba.db");
            dos = new DataOutputStream(arq);

            ba = j1.toByteArray();
            dos.writeInt(ba.length); //Tamano do registro em bytes
            dos.write(ba);
            
            ba = j2.toByteArray();
            dos.writeInt(ba.length);
            dos.write(ba);
            
            ba = j3.toByteArray();
            dos.writeInt(ba.length);
            dos.write(ba);
            
            dos.close();

            arq2 =  new FileInputStream("../dados/jogadores_ds_ba.db");
            dis = new DataInputStream(arq2);
  
            len = dis.readInt(); //Tamano do registro em bytes
            ba = new byte[len];
            dis.read(ba);
            j_temp.fromByteArray(ba);
            System.out.println(j_temp);

            len = dis.readInt();
            ba = new byte[len];
            dis.read(ba);
            j_temp.fromByteArray(ba);
            System.out.println(j_temp);

            len = dis.readInt();
            ba = new byte[len];
            dis.read(ba);
            j_temp.fromByteArray(ba);
            System.out.println(j_temp);


            

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    
}
