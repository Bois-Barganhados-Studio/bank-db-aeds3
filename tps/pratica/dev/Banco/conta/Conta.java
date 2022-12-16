package conta;

/*
 * Objeto de conta do banco
 * Descrição dos campos de Conta:
 * idConta (deve ser incremental à medida que novos registros forem adicionados)
 * (int)
 * nomePessoa (string de tamanho variável)
 * email (1 ou mais) (strings de tamanhos variáveis com indicador de quantidade)
 * nomeUsuario (string de tamanho variável)
 * senha (string de tamanho variável)
 * cpf (string de tamanho fixo igual a 11)
 * cidade (string de tamanho variável)
 * transferenciasRealizadas (int)
 * saldoConta (float)
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import estruturas.Cryptography.BlockCipher;

public class Conta implements Comparable<Conta>, Serializable {
    private static final BlockCipher CIPHER = new BlockCipher();
    private int idConta, transferenciasRealizadas;
    private String nomePessoa, nomeUsuario, senha, cpf, cidade, email[];
    private float saldoConta;

    public Conta() {
        setIdConta(-1);
        this.transferenciasRealizadas = 0;
        this.nomePessoa = "";
        this.nomeUsuario = "";
        this.senha = "";
        this.cpf = "";
        this.cidade = "";
        this.email = null;
        this.saldoConta = 0f;
    }

    @Override
    public int compareTo(Conta u) {
        int result = 0;
        if (this.idConta > u.idConta) {
            result = 1;
        } else if (this.idConta < u.idConta) {
            result = -1;
        }
        return result;
    }

    public Conta(int transferenciasRealizadas, String nomePessoa, String nomeUsuario, String senha,
            String cpf, String cidade, String[] email, float saldoConta) throws Exception {
        setTransferenciasRealizadas(transferenciasRealizadas);
        setNomePessoa(nomePessoa);
        setNomeUsuario(nomeUsuario);
        setSenha(senha);
        setCpf(cpf);
        setCidade(cidade);
        setEmail(email);
        setSaldoConta(saldoConta);
    }

    public Conta(int id, int transferenciasRealizadas, String nomePessoa, String nomeUsuario, String senha,
            String cpf, String cidade, String[] email, float saldoConta) {
        this.idConta = id;
        this.transferenciasRealizadas = transferenciasRealizadas;
        this.nomePessoa = nomePessoa;
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.cpf = cpf;
        this.cidade = cidade;
        this.email = email;
        this.saldoConta = saldoConta;
    }

    public byte[] toByteArray() throws IOException {
        byte[] data;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(getIdConta());
        dos.writeUTF(getNomePessoa());
        dos.writeInt(getEmail().length);
        for (String email : getEmail()) {
            dos.writeUTF(email);
        }
        dos.writeUTF(getNomeUsuario());
        dos.writeUTF(getSenha());
        dos.writeUTF(getCpf());
        dos.writeUTF(getCidade());
        dos.writeInt(getTransferenciasRealizadas());
        dos.writeFloat(getSaldoConta());
        data = baos.toByteArray();
        baos.close();
        dos.close();
        return data;
    }

    public void fromByteArray(byte ba[]) throws IOException, Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        setIdConta(dis.readInt());
        setNomePessoa(dis.readUTF());
        this.email = new String[dis.readInt()];
        for (int i = 0; i < this.email.length; i++) {
            this.email[i] = dis.readUTF();
        }
        setNomeUsuario(dis.readUTF());
        setSenha(dis.readUTF());
        setCpf(dis.readUTF());
        setCidade(dis.readUTF());
        setTransferenciasRealizadas(dis.readInt());
        setSaldoConta(dis.readFloat());
        bais.close();
        dis.close();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Conta other = (Conta) obj;
        if (cpf == null) {
            if (other.cpf != null)
                return false;
        } else if (!cpf.equals(other.cpf))
            return false;
        if (idConta != other.idConta)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "cidade=" + cidade + ", cpf=" + cpf + ", email=" + Arrays.toString(email) + ", idConta=" + idConta
                + ", nomePessoa=" + nomePessoa + ", nomeUsuario=" + nomeUsuario + ", saldoConta=" + saldoConta
                + ", senha=" + senha + ", transferenciasRealizadas=" + transferenciasRealizadas;
    }

    public int getIdConta() {
        return idConta;
    }

    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }

    public int getTransferenciasRealizadas() {
        return transferenciasRealizadas;
    }

    public void setTransferenciasRealizadas(int transferenciasRealizadas) throws Exception {
        if (transferenciasRealizadas < 0)
            throw new Exception("As transferencias não podem ter valor negativo");
        this.transferenciasRealizadas = transferenciasRealizadas;
    }

    public String getNomePessoa() {
        return nomePessoa.replace("\n", "");
    }

    public void setNomePessoa(String nomePessoa) {
        this.nomePessoa = nomePessoa;
    }

    public String getNomeUsuario() {
        return nomeUsuario.replace("\n", "");
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getSenha() {
        return CIPHER.decrypt(senha).replace("\n", "");
    }

    public void setSenha(String senha) {
        this.senha = CIPHER.crypt(senha);
    }

    public String getCpf() {
        return cpf.replace("\n", "");
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCidade() {
        return cidade.replace("\n", "");
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String[] getEmail() {
        return email;
    }

    public void setEmail(String[] email) {
        this.email = email;
    }

    public float getSaldoConta() {
        return saldoConta;
    }

    public void setSaldoConta(float saldoConta) throws Exception {
        if (saldoConta < 0) {
            throw new Exception("Saldo negativo inválido para criação de conta!");
        }
        this.saldoConta = saldoConta;
    }

}