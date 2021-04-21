/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hawad
 */
public class ReaderThread extends Thread {

    private final Socket socket;
    private final ArrayList<PrintWriter> writers;
    private final Frame frame;
    private PrintWriter writer;
    private String clientName;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader reader;
    private boolean IsStopped;

    public ReaderThread(Socket socket, ArrayList<PrintWriter> writers, Frame frame) {
        this.socket = socket;
        this.writers = writers;
        this.frame = frame;
    }

    @Override
    public void run() {
        try {
            is = socket.getInputStream();
            isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            writer = new PrintWriter(socket.getOutputStream(), true);
            clientName = reader.readLine();
            send(clientName + " connected");
            writers.add(writer);
            sendClientList();
            frame.model.addElement(clientName);
            while (true) {
                String receivedMessage = reader.readLine();
                if (receivedMessage == null) {
                    send(clientName + " disconnected");
                    frame.model.removeElement(clientName);
                    break;
                }
                String sentMessage = clientName + ": " + receivedMessage;
                send(sentMessage);
            }
        } catch (IOException ex) {
            Logger.getLogger(clientName.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        if (!IsStopped) {
            closeEverything();
        }
    }

    private void send(String message) {
        writers.forEach((pw) -> {
            pw.println(message);
        });
        frame.appendChat("\n" + message);
    }

    public void stopReading() {
        closeEverything();
    }

    private void closeEverything() {
        try {
            socket.close();
            is.close();
            isr.close();
            reader.close();
            writer.close();
            writers.remove(writer);
        } catch (IOException ex) {
            Logger.getLogger(ReaderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendClientList() {
        for (int i = 0; i < frame.model.getSize(); i++)
        {
            String name = frame.model.get(i);
            if (!name.equals(clientName))
            {
                writer.println(name);
            }
        }
        writer.println("/");//indicates that it finished sending active clients to client
    }
}
