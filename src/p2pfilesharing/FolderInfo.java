/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharing;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Luís Maia
 */
public class FolderInfo {

    private static final String PASTA_RAIZ = "shared";

    private static final HashMap< String, ArrayList<FileInfo>> mapa_Servidor_Ficheiros = new HashMap<>();

    private static ArrayList<FileInfo> listaFicheiros = new ArrayList<>();

    /**
     * Devolve a string do caminho da pasta raiz para a frente
     *
     * @param file Ficheiro a devolver o caminho
     * @return String com o caminho do ficheiro a partir da pasta raiz
     */
    private static String preparaCaminho(File file) {

        String caminhoFinal = "";

        String caminho = file.toString();

        String[] split = caminho.replace('\\', '/').split("/"); // Verificar se funciona em Linux. Por causa da barras

        int tamanho = split.length;

        for (int i = 0; i < tamanho; i++) {

            if (split[i].equals(PASTA_RAIZ)) {

                i++; // Para passar o nome da pasta Raiz a frente

                for (; i < tamanho; i++) {
                    caminhoFinal = "/" + split[i];
                }
                break;
            }
        }
        return caminhoFinal;
    }

    private static void listaPasta(File path, String prefixo, ArrayList<String> listaCaminhos) {
        File files[];

        files = path.listFiles();

        for (File file : files) {

            String caminho = prefixo + preparaCaminho(file);

            if (file.isDirectory()) {

                listaPasta(file, caminho, listaCaminhos);

            } else if (caminho.length() > 0) {

                listaCaminhos.add(caminho);
            }
        }
    }

    /**
     * Adiciona os ficheiros locais a lista dos ficheiros existentes no Cluster.
     *
     * @throws java.net.UnknownHostException
     */
    public static void addFicheirosLocais() throws UnknownHostException {

        String servidor_local = null;

        servidor_local = P2PFileSharing.peerAddress[0].getHostAddress();

        if (servidor_local == null) {

            InetAddress localHost = null;
            try {

                localHost = InetAddress.getLocalHost();

            } catch (UnknownHostException ex) {
                System.out.println(ex);
            }
        }
        ArrayList<String> listaCaminhos = new ArrayList<>();

        listaPasta(new File("."), "", listaCaminhos);// Lista a todos os ficheiros e diretorios a partir do diretorio atual

        ArrayList<FileInfo> ficheiros = new ArrayList<>();

        ArrayList<FileInfo> listaFicheiros_Locais = getListaFicheiros_Locais();

        listaFicheiros.removeAll(listaFicheiros_Locais);

        for (String caminho : listaCaminhos) {
            caminho= caminho.replace("/", "");
            FileInfo fich = new FileInfo(servidor_local, caminho, true); // Ficheiro local

            fich.toString();

            ficheiros.add(fich);

            addFicheiro(fich);
        }

        if (mapa_Servidor_Ficheiros.containsKey(servidor_local)) {
            mapa_Servidor_Ficheiros.remove(servidor_local);
        }
        mapa_Servidor_Ficheiros.put(servidor_local, ficheiros);
    }

    /**
     * Adiciona um ficheiro a lista e verifica se este ja existe na lista. Se o
     * ficheiro a introduzir for local e o que se encontra na lista nao for
     * local, o ficheiro vai ser substituido.
     *
     * @param ficheiro Ficheiro a adicionar a lista
     */
    public static void addFicheiro(FileInfo ficheiro) {

        if (listaFicheiros.contains(ficheiro) == false) {

            listaFicheiros.add(ficheiro);

        } else if (ficheiro.isFicheiroLocal()) {

            listaFicheiros.remove(ficheiro);

            listaFicheiros.add(ficheiro);
        }
    }

    /**
     * @return the listaFicheiros
     */
    public static ArrayList<FileInfo> getListaFicheiros_Locais() {

        String servidorLocal = null;

        if (servidorLocal == null) {
            return new ArrayList<>();
        }

        ArrayList<FileInfo> array = mapa_Servidor_Ficheiros.get(servidorLocal);

        return array;
    }

    public static ArrayList<FileInfo> getListaFicheiros() {
        return listaFicheiros;
    }

    public static void setListaFicheiros(ArrayList<FileInfo> listaFicheiros) {
        FolderInfo.listaFicheiros = listaFicheiros;
    }

}
