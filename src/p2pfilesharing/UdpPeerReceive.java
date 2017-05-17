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

/**
 *
 * @author Luís Maia
 */
public class UdpPeerReceive implements Runnable {

    private DatagramSocket s;

    public UdpPeerReceive(DatagramSocket udp_s) {
        s = udp_s;
    }

    public void run() {
        int i;
        byte[] data = new byte[300];
        String frase;
        DatagramPacket p;
        InetAddress currPeerAddress;

        p = new DatagramPacket(data, data.length);

        while (true) {
            p.setLength(data.length);
            try {
                s.receive(p);
            } catch (IOException ex) {
                return;
            }
            currPeerAddress = p.getAddress();
            switch (data[0]) {
                case 1:
                    try {
                        P2PFileSharing.changeLock.acquire();
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted");
                    }   for (i = 0; i < P2PFileSharing.MAXCLI; i++) {
                        if (P2PFileSharing.peerActive[i]) {
                            if (currPeerAddress.equals(P2PFileSharing.peerAddress[i])) {
                                break;
                            }
                        }
                    }   if (i == P2PFileSharing.MAXCLI) // new peer
                    {
                        for (i = 0; i < P2PFileSharing.MAXCLI; i++) {
                            if (!P2PFileSharing.peerActive[i]) {
                                break;
                            }
                        }
                        if (i == P2PFileSharing.MAXCLI) {
                            System.out.println("Sorry, no room for more peers");
                        } else {
                            P2PFileSharing.peerActive[i] = true;
                            P2PFileSharing.peerAddress[i] = currPeerAddress;
                        }
                    }   P2PFileSharing.changeLock.release();
                    data[0] = 1;
                    p.setAddress(currPeerAddress); // send back an announcement
                    p.setLength(1);
                    try {
                        s.send(p);
                    } catch (IOException ex) {
                        return;
                    }   break;
                case 0:
                    try {
                        P2PFileSharing.changeLock.acquire();
                    } catch (InterruptedException ex) {
                        System.out.println("Interrupted");
                    }   for (i = 0; i < P2PFileSharing.MAXCLI; i++) {
                        if (P2PFileSharing.peerActive[i]) {
                            if (currPeerAddress.equals(P2PFileSharing.peerAddress[i])) {
                                break;
                            }
                        }
                    }   if (i < P2PFileSharing.MAXCLI) {
                        P2PFileSharing.peerActive[i] = false;
                    }   P2PFileSharing.changeLock.release();
                    break;
                default:
                    frase = new String(p.getData(), 0, p.getLength()); 
                    String [] files = frase.split("|");
                    if(!currPeerAddress.equals(P2PFileSharing.peerAddress[0])){
                        System.out.println(frase);
                    }
                    break;
            }
        }
    }
}
