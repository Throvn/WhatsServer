package com.throvn; /**
 * Teil des Servers - wartet lediglich permanent auf neue Clients, die sich anmelden
 * @author T. Hammersen
 * @version Juli 2019
 */

import java.net.ServerSocket;
import java.net.Socket;

public class ServerSchleife extends Thread
{
    private Server zServer;
    private boolean zAktiv;
    private ServerSocket zServerSocket;//TCP-Socket, der auf Verbindungen wartet
    private List<ServerConnection> zVerbindungen;//speichert alle Verbindungen    

    public ServerSchleife(Server pServer, int pPort, List<ServerConnection> pVerbindungen)
    {
        zServer = pServer;
        zAktiv = true;
        zVerbindungen = pVerbindungen;
        try
        {
            zServerSocket = new ServerSocket (pPort);
        }
        catch(Exception e)
        {
            zAktiv = false;
            System.err.println("Fehler beim Anlegen der Serverschleife" + e);
        }
    }

    public boolean istAktiv()
    {
        return zAktiv;
    }

    public void run()
    {
        try
        {
            while (zAktiv)//die Endlosschleife - der Server arbeitet ab jetzt ewig
            {
                Socket lClientSocket = zServerSocket.accept();// warten auf neuen Client
                ServerConnection lNeueVerbindung = new ServerConnection(lClientSocket, zServer);
                // Der Client laeuft in einem eigenen Thread, damit mehrere
                // Clients gleichzeitig auf den Server zugreifen koennen.
                zVerbindungen.append(lNeueVerbindung);
                zServer.processNewConnection(lNeueVerbindung.getRemoteIP(),
                        lNeueVerbindung.getRemotePort());
                lNeueVerbindung.start();//neuen Thread fuer die Verbindung starten ...
            }
        }
        catch (Exception e){zAktiv = false;}
    }

    public void close()
    {
        zAktiv = false;
        try
        {
            if (zServerSocket != null)
                if (! zServerSocket.isClosed())
                    zServerSocket.close();//Socket ggf. noch geordnet schliessen
        }
        catch(Exception e)
        {
            System.err.println("Fehler beim Schlieesen der Serverschleife: " + e);
        }
    }
}