/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import static p2pfilesharing.FileTransferServer.ssock;
import p2pfilesharing.UI.MainFrame;

/**
 *
 * @author Luís Maia
 */
public class P2PFileSharing {

    public static final int MAXCLI = 100;
    public static Boolean[] peerActive = new Boolean[MAXCLI];
    public static InetAddress[] peerAddress = new InetAddress[MAXCLI];

    public static Semaphore changeLock = new Semaphore(1);
    static InetAddress bcastAddress;
    static DatagramSocket sock;
    static int port = 32008;
    static int porto = 32005;
    public static String nick, frase = "";
    static byte[] data = new byte[300];
    static byte[] fraseData;
    static int i;
    static DatagramPacket udpPacket;
    static MainFrame frame;
    static Thread udpReceiver;
    static Thread fileTransferServer;
    static Timer t;

    public static void main(String args[]) throws Exception {
        try {
            sock = new DatagramSocket(port);
        } catch (IOException ex) {
            System.out.println("Failed to open local port");
            System.exit(1);
        }

        // BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        nick = JOptionPane.showInputDialog(null, "Nickname: ", "Welcome", JOptionPane.QUESTION_MESSAGE);
        if (nick == null) {
            return;
        }
        //System.out.print("Nickname: ");
        //nick = in.readLine();
        frame = new MainFrame(nick);
        frame.setVisible(true);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    exit();
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(P2PFileSharing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        for (i = 0; i < MAXCLI; i++) {
            peerActive[i] = false;
        }

        bcastAddress = InetAddress.getByName("255.255.255.255");
        sock.setBroadcast(true);
        data[0] = 1;
        udpPacket = new DatagramPacket(data, 1, bcastAddress, port);
        sock.send(udpPacket);

        udpReceiver = new Thread(new UdpPeerReceive(sock));
        udpReceiver.start();
        fileTransferServer = new Thread(new FileTransferServer(bcastAddress, porto));
        fileTransferServer.start();
        // Every 30s sends update from local files list
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("my announcement");
                try {
                    P2PFileSharing.sendAnnouncement();
                } catch (InterruptedException | IOException ex) {
                    Logger.getLogger(P2PFileSharing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 0, 30000);
    }

    /*while (true) {*/
    public static void exit() throws IOException, InterruptedException {
        t.cancel();
        UdpPeerReceive.t45.cancel();
        data[0] = 0;
        udpPacket.setData(data);
        udpPacket.setLength(1);
        for (i = 0; i < MAXCLI; i++) {
            if (peerActive[i]) {
                udpPacket.setAddress(peerAddress[i]);
                sock.send(udpPacket);
            }
        }
        ssock.close();
        sock.close();
        udpReceiver.join();
        fileTransferServer.join();
    }

    public static void list() throws InterruptedException {
        System.out.print("Active peers:");
        frame.addtoLog("Active peers:" + "\n");
        changeLock.acquire();
        for (i = 0; i < MAXCLI; i++) {
            if (peerActive[i]) {
                frame.addtoLog(peerAddress[i].getHostAddress() + "\n");
                System.out.print(" " + peerAddress[i].getHostAddress() + "\n");
            }
        }
        changeLock.release();
    }

    public static void download() throws InterruptedException {
        //System.out.println("entrei");//DEBUG
        //System.out.print("Select User ip:");
        changeLock.acquire();
        String ip = frame.getDwnIp();//in.readLine();
        //System.out.print("Select file to download:");
        String file = frame.getDwnFileName();//in.readLine();
        Thread fileTransferClient = null;
        for (i = 0; i < MAXCLI; i++) {
            if (peerActive[i] && ip.equals(peerAddress[i].getHostAddress())) {
                fileTransferClient = new Thread(new FileTransferClient(peerAddress[i], porto, file));
                fileTransferClient.start();
                break;
            }
        }
        changeLock.release();
    }

    public static void sendAnnouncement() throws InterruptedException, IOException {
        FolderInfo.addFicheirosLocais();
        ArrayList<FileInfo> ListaFicheiros = FolderInfo.getListaFicheiros();
        frase = "";
        for (FileInfo ficheiro : ListaFicheiros) {
            frase += ficheiro.getEndereco_Servidor() + "," + ficheiro.getNome_Ficheiro() + "\n"; //TODO: imprimir a lista de ficheiros 
        }
        fraseData = frase.getBytes();
        udpPacket.setData(fraseData);
        udpPacket.setLength(frase.length());
        changeLock.acquire();
        for (i = 0; i < MAXCLI; i++) {
            if (peerActive[i]) {
                udpPacket.setAddress(peerAddress[i]);
                sock.send(udpPacket);
            }
        }
        changeLock.release();
    }
}
