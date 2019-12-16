package com.throvn;

/**
 * @author T. Hammersen
 * @version Juli 2019
 * Die eigentliche Server-Anwendung muss als Kindklasse von dieser Klasse erstellt
 * werden.
 */

public abstract class Server
{
    private List<ServerConnection> zVerbindungen;//speichert alle akt. Verbindungen
    private ServerSchleife zSchleife;//die Server-Schleife (wartet auf Clients)
    private int zPort;//die TCP-Portnummer dieses Servers

    /**
     * @param pPortNr Port, auf der dieser Server mit Clients kommunizieren soll
     */
    public Server(int pPortNr)
    {
        try
        {
            zPort = pPortNr;
            zVerbindungen = new List<ServerConnection>();
            zSchleife = new ServerSchleife(this,pPortNr,zVerbindungen);
            zSchleife.start();//ab jetzt arbeitet die Server-Schleife
        }
        catch (Exception pFehler)
        {
            System.err.println("Fehler beim Starten des Servers: " + pFehler);
        }
    }

    public boolean isOpen()
    {
        return zSchleife.istAktiv();
    }

    public boolean isConnectedTo(String pClientIP, int pClientPort)
    {
        ServerConnection lVerbindung = gibVerbindung (pClientIP, pClientPort);
        if (lVerbindung == null) return false;
        else return lVerbindung.isConnected();
    }

    /**
     * Liefert die Serververbindung zum Client mit der angegebenen IP und dem
     * angegebenen Port bzw. null, falls sie nicht vorhanden ist.
     * @param pClientIP IP-Adresse des Clients
     * @param pClientPort Port-Nummer des Clients
     * @return die gesuchte Verbindung bzw. null
     */
    private ServerConnection gibVerbindung(String pClientIP, int pClientPort)
    {
        ServerConnection lSerververbindung;
        synchronized(zVerbindungen)
        {
            zVerbindungen.toFirst();
            while (zVerbindungen.hasAccess())
            {
                lSerververbindung = zVerbindungen.getContent();
                if (lSerververbindung.getRemoteIP().equals(pClientIP) &&
                        lSerververbindung.getRemotePort() == pClientPort)
                    return lSerververbindung;
                zVerbindungen.next();
            }
        }
        return null; //Die IP-Adresse und Portnummer wurde nicht gefunden
    }

    /**
     * Eine Nachricht (pMessage) wird an einen Client gesendet.
     * @param pClientIP IP-Nummer des Empf�ngers
     * @param pClientPort Port-Nummer des Empfaengers
     * @param pMessage die verschickte Nachricht
     */
    public void send(String pClientIP, int pClientPort, String pMessage)
    {
        ServerConnection lVerbindung = gibVerbindung(pClientIP, pClientPort);
        if (lVerbindung != null) lVerbindung.send(pMessage);
        else
            System.err.println("Fehler beim Senden: IP " + pClientIP +
                    " mit Port " + pClientPort + " ist nicht vorhanden.");
    }

    /**
     * Eine Nachricht wird an alle verbundenen Clients gesendet.
     * @param pMessage die zu verschickende Nachricht
     */
    public void sendToAll(String pMessage)
    {
        ServerConnection lSerververbindung;
        synchronized(zVerbindungen)
        {
            zVerbindungen.toFirst();
            while (zVerbindungen.hasAccess())
            {
                lSerververbindung = zVerbindungen.getContent();
                lSerververbindung.send(pMessage);
                zVerbindungen.next();
            }
        }
    }

    /**
     * Die Verbindung mit der angegebenen IP und dem angegebenen Port wird geschlossen.
     * @param pClientIP IP-Adresse des Clients der zu beendenden Verbindung
     * @param pClientPort Port-Nummer des Clients der zu beendenden Verbindung
     */
    public void closeConnection (String pClientIP, int pClientPort)
    {
        ServerConnection lSerververbindung = gibVerbindung(pClientIP, pClientPort);
        if (lSerververbindung != null)
        {
            this.processClosingConnection(pClientIP, pClientPort);
            lSerververbindung.close();
            this.loescheVerbindung(lSerververbindung);
        }
        else
            System.err.println("Fehler beim Schliessen der Verbindung: IP "
                    + pClientIP + " mit Port " + pClientPort + " nicht vorhanden.");

    }

    /**
     * Eine Verbindung wird aus der Empfaengerliste geloescht.
     * @param pVerbindung die zu loeschende Verbindung
     */
    private void loescheVerbindung(ServerConnection pVerbindung)
    {
        ServerConnection lVerbindung;
        synchronized(zVerbindungen)
        {
            zVerbindungen.toFirst();
            while (zVerbindungen.hasAccess())
            {
                lVerbindung = zVerbindungen.getContent();
                if (lVerbindung == pVerbindung)
                {
                    zVerbindungen.remove();
                    zVerbindungen.toLast();
                }
                zVerbindungen.next();
            }
        }
    }

    /**
     * Ein neuer Client hat sich angemeldet.
     * Diese leere Methode kann in einer Unterklasse implementiert werden.
     * @param pClientIP IP-Adresse des Clients, der neu angemeldet wird.
     * @param pClientPort Port-Nummer des Clients, der neu angemeldet ist.
     */
    public abstract void processNewConnection(String pClientIP, int pClientPort);

    /**
     * Eine Nachricht von einem Client wird bearbeitet. Diese Methode sollte auf jeden
     * Fall von der Kindklasse (dem eigentlichen Server) implementiert werden.
     * @param pClientIP IP-Nummer des Clients, der die Nachricht geschickt hat
     * @param pClientPort Port-Nummer des Clients, der die Nachricht geschickt hat
     * @param pMessage Die empfangene Nachricht, die bearbeitet werden soll
     */
    public abstract void processMessage(String pClientIP, int pClientPort,
                                        String pMessage);
    /**
     * Die Verbindung mit einem Client wird beendet.
     * Diese leere Methode kann in einer Unterklasse implementiert werden.
     * @param pClientIP IP-Adress des Clients, mit dem die Verbindung beendet wurde.
     * @param pClientPort Port des Clients, mit dem die Verbindung beendet wurde.
     */
    public abstract void processClosingConnection(String pClientIP, int pClientPort);

    /**
     * Alle Verbindungen werden getrennt.
     * Der Server wird geschlossen und nicht mehr nutzbar.
     */
    public void close()
    {
        try
        {
            zSchleife.close();
            ServerConnection lVerbindung;
            synchronized(zVerbindungen)
            {
                zVerbindungen.toFirst();
                while (zVerbindungen.hasAccess())
                {
                    lVerbindung = zVerbindungen.getContent();
                    processClosingConnection(lVerbindung.getRemoteIP(),
                            lVerbindung.getRemotePort());
                    lVerbindung.close();
                    zVerbindungen.next();
                }
                zVerbindungen = new List<ServerConnection>();//Liste leeren
            }
            System.exit(0);
        }

        catch (Exception e)
        {
            System.err.println("Fehler beim Schliessen des Servers: " + e);
            System.exit(0);
        }
    }
}