package com.throvn;

public class Element<ContentType>
{
    private ContentType zInhalt;
    private Element<ContentType> zNachfolger;
    private Element<ContentType> zVorgaenger;

    public Element()
    {
        zInhalt = null;
        zNachfolger = null;
        zVorgaenger = null;
    }

    public void setzeInhalt(ContentType pInhalt)
    {
        zInhalt = pInhalt;
    }

    public ContentType gibInhalt()
    {
        return zInhalt;
    }

    public void setzeNachfolger(Element<ContentType> pNachfolger)
    {
        zNachfolger = pNachfolger;
    }

    public Element<ContentType> gibNachfolger()
    {
        return zNachfolger;
    }

    public void setzeVorgaenger(Element<ContentType> pVorgaenger)
    {
        zVorgaenger = pVorgaenger;
    }

    public Element<ContentType> gibVorgaenger()
    {
        return zVorgaenger;
    }
}