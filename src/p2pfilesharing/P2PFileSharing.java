/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2pfilesharing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Luís Maia
 */
public class P2PFileSharing {

    public static int MAXCLI = 100;
    public static Boolean[] peerActive = new Boolean[MAXCLI];
    public static InetAddress[] peerAddress = new InetAddress[MAXCLI];

    public static Semaphore changeLock = new Semaphore(1);

    static InetAddress bcastAddress;
    static DatagramSocket sock;
    static int port = 32008;
    
    public static void main(String args[]) throws Exception {
        String nick, frase;
        byte[] data = new byte[300];
        byte[] fraseData;
        int i;
        DatagramPacket udpPacket;

        try {
            sock = new DatagramSocket(port);
        } catch (IOException ex) {
            System.out.println("Failed to open local port");
            System.exit(1);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Nickname: ");
        nick = in.readLine();

        for (i = 0; i < MAXCLI; i++) {
            peerActive[i] = false;
        }

        bcastAddress = InetAddress.getByName("255.255.255.255");
        sock.setBroadcast(true);
        data[0] = 1;
        udpPacket = new DatagramPacket(data, 1, bcastAddress, port);
        sock.send(udpPacket);

        Thread udpReceiver = new Thread(new UdpPeerReceive(sock));
        udpReceiver.start();

        while (true) {
            frase = in.readLine();
            if (frase.compareTo("EXIT") == 0) {
                break;
            }
            if (frase.compareTo("LIST") == 0) {
                System.out.print("Active peers:");
                changeLock.acquire();
                for (i = 0; i < MAXCLI; i++) {
                    if (peerActive[i]) {
                        System.out.print(" " + peerAddress[i].getHostAddress());
                    }
                }
                changeLock.release();
                System.out.println("");
            } else {
                frase = "(" + nick + ") " + frase; //TODO: imprimir a lista de ficheiros 
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

        data[0] = 0;
        udpPacket.setData(data);
        udpPacket.setLength(1);
        for (i = 0; i < MAXCLI; i++) {
            if (peerActive[i]) {
                udpPacket.setAddress(peerAddress[i]);
                sock.send(udpPacket);
            }
        }
        sock.close();
        udpReceiver.join();
    }
}