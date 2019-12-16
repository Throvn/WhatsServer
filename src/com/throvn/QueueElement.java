package com.throvn;

/**
 * @author T. Hammersen
 * @version Januar 2019
 */
public class QueueElement<ContentType>//Element einer Queue
{
    private QueueElement<ContentType> zNachfolger;
    private ContentType zInhalt;

    public QueueElement()//Standardkonstruktor
    {
        zInhalt = null;zNachfolger = null;
    }

    public ContentType gibInhalt()
    {
        return zInhalt;
    }
    public QueueElement<ContentType> gibNachfolger()
    {
        return zNachfolger;
    }

    public void setzeNachfolger(QueueElement<ContentType> pNachfolger)
    {
        zNachfolger = pNachfolger;
    }

    public void setzeInhalt(ContentType pInhalt)
    {
        zInhalt = pInhalt;
    }
}
