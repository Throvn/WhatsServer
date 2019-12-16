package com.throvn;

/**
 * @author T. Hammersen
 * @version Januar 2019
 */
public class Queue<ContentType>
{
    private QueueElement<ContentType> zKopf;
    private QueueElement<ContentType> zEnde;

    public Queue()
    {
        zKopf = new QueueElement<ContentType>();
        zEnde = new QueueElement<ContentType>();
        zKopf.setzeNachfolger(zEnde);
    }

    public boolean isEmpty()
    {
        return zKopf.gibNachfolger() == zEnde;
    }

    public ContentType front()
    {
        if (isEmpty()) return null;
        else return zKopf.gibNachfolger().gibInhalt();
    }

    public void dequeue()
    {
        if (! isEmpty())
            zKopf.setzeNachfolger(zKopf.gibNachfolger().gibNachfolger());
    }

    public void enqueue(ContentType pContent)
    {
        if (pContent != null)
        {
            QueueElement<ContentType> lNeu = new QueueElement<ContentType>();
            zEnde.setzeInhalt(pContent);
            zEnde.setzeNachfolger(lNeu);
            zEnde = lNeu;
        }
    }
}