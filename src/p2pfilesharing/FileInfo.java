/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharing;

/**
 *
 * @author Luís Maia
 */
public class FileInfo implements Comparable<FileInfo> {

    private String endereco_Servidor;

    private String nome_Ficheiro;

    private boolean ficheiroLocal;

    public FileInfo(String servidor, String nome_Ficheiro, boolean ficheiroLocal) {
        this.endereco_Servidor = servidor;

        this.nome_Ficheiro = nome_Ficheiro;

        this.ficheiroLocal = ficheiroLocal;
    }

    public String getEndereco_Servidor() {
        return endereco_Servidor;
    }

    public void setEndereco_Servidor(String endereco_Servidor) {
        this.endereco_Servidor = endereco_Servidor;
    }

    /**
     * @return the nome_Ficheiro
     */
    public String getNome_Ficheiro() {
        return nome_Ficheiro;
    }

    /**
     * @param nome_Ficheiro the nome_Ficheiro to set
     */
    public void setNome_Ficheiro(String nome_Ficheiro) {
        this.nome_Ficheiro = nome_Ficheiro;
    }

    /**
     * @return the ficheiroLocal
     */
    public boolean isFicheiroLocal() {
        return ficheiroLocal;
    }

    /**
     * @param ficheiroLocal the ficheiroLocal to set
     */
    public void setFicheiroLocal(boolean ficheiroLocal) {
        this.ficheiroLocal = ficheiroLocal;
    }

    @Override
    public int compareTo(FileInfo o) {

        return this.nome_Ficheiro.compareTo(o.nome_Ficheiro);
    }

    @Override
    public boolean equals(Object o) {

        FileInfo f;

        if (o instanceof FileInfo) {
            f = (FileInfo) o;
        } else {
            return false;
        }

        return this.nome_Ficheiro.equals(f.nome_Ficheiro);
    }

    @Override
    public String toString() {
        return P2PFileSharing.port + "|" + this.nome_Ficheiro;
    }
}
