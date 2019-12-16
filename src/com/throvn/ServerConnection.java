package com.throvn; /**
 * Kindklasse von Thread
 * @author T. Hammersen
 * @version Juli 2019
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection extends Thread
{
    private Socket zSocket;//Verbindungsobjekt zum Client
    private BufferedReader zEingang;//Objekt zum Lesen
    private PrintWriter zAusgang;//Objekt zum Schreiben
    private Server zServer;//zum Aufrufen der Server-Methoden

    public ServerConnection(Socket pSocket, Server pServer)
    {
        zSocket = pSocket;//das Socket-Objekt merken
        zServer = pServer;//das Server-Objekt merken        
        //zServerName = ""+zSocket.getLocalAddress();
        //zPort = zSocket.getLocalPort();
        try
        {
            zAusgang = new PrintWriter(zSocket.getOutputStream(), true);
            zEingang = new BufferedReader(new InputStreamReader(zSocket.getInputStream()));
        }
        catch (Exception e){e.printStackTrace();}
    }

    /**
     * Solange der Client Nachrichten sendet, werden diese 
     * empfangen und an die Server weitergereicht.
     * Abgebrochene Verbindungen werden erkannt.            
     */
    public void run()
    {
        String lNachricht;
        while (isConnected() && !isClosed())
        {
            lNachricht = this.receive();//Warten auf eine Nachricht vom Client
            if (lNachricht == null)//keine Verbindung mehr zum Client?
            {
                if (isConnected() && !isClosed())
                {
                    zServer.closeConnection(getRemoteIP(), getRemotePort());
                }
            }
            else//Verbindung zum Client steht also noch
                zServer.processMessage(getRemoteIP(), getRemotePort(), lNachricht);
        }
    }

    /** Es wird auf eine eingehende Nachricht vom Server gewartet und diese
     * Nachricht zurueckgegeben, wobei der vom Server angehaengte Zeilentrenner
     * entfernt wird. Waehrend des Wartens ist der ausfuehrende Prozess blockiert.
     */
    public String receive()
    {
        try
        {
            return zEingang.readLine();
        }
        catch (IOException e)
        {
            System.out.println("Verbindung zu " + getRemoteIP() + " "
                    + getRemotePort() + " ist unterbrochen.");
        }
        return null;
    }

    /** Die angegebene Nachricht pMessage wird - um einen Zeilentrenner erweitert -
     * an den Server gesendet.
     */
    public void send(String pNachricht)
    {
        if (zAusgang != null)
        {
            zAusgang.println(pNachricht);
            zAusgang.flush();//noch gepufferte Daten werden in den Stream geschrieben
        }
    }

    public void close()
    {
        try
        {
            if (zSocket != null)
            {
                if (! zSocket.isClosed())
                {
                    zAusgang.close();zEingang.close();zSocket.close();
                }
            }
        }
        catch (IOException e){e.printStackTrace();}
    }

    public boolean isConnected() {return zSocket.isConnected();}

    public boolean isClosed() {return zSocket.isClosed();}

    public String getRemoteIP() {return "" + zSocket.getInetAddress();}

    public int getRemotePort() {return zSocket.getPort();}
}