//
// Copyright (c) ZeroC, Inc. All rights reserved.
//
//
// Ice version 3.7.5
//
// <auto-generated>
//
// Generated from file `Server.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package m1.projet.emusik.Server;

public class Song implements java.lang.Cloneable,
                             java.io.Serializable
{
    public int id;

    public String filename;

    public String filepath;

    public String title;

    public String artist;

    public String genre;

    public Song()
    {
        this.filename = "";
        this.filepath = "";
        this.title = "";
        this.artist = "";
        this.genre = "";
    }

    public Song(int id, String filename, String filepath, String title, String artist, String genre)
    {
        this.id = id;
        this.filename = filename;
        this.filepath = filepath;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        Song r = null;
        if(rhs instanceof Song)
        {
            r = (Song)rhs;
        }

        if(r != null)
        {
            if(this.id != r.id)
            {
                return false;
            }
            if(this.filename != r.filename)
            {
                if(this.filename == null || r.filename == null || !this.filename.equals(r.filename))
                {
                    return false;
                }
            }
            if(this.filepath != r.filepath)
            {
                if(this.filepath == null || r.filepath == null || !this.filepath.equals(r.filepath))
                {
                    return false;
                }
            }
            if(this.title != r.title)
            {
                if(this.title == null || r.title == null || !this.title.equals(r.title))
                {
                    return false;
                }
            }
            if(this.artist != r.artist)
            {
                if(this.artist == null || r.artist == null || !this.artist.equals(r.artist))
                {
                    return false;
                }
            }
            if(this.genre != r.genre)
            {
                if(this.genre == null || r.genre == null || !this.genre.equals(r.genre))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::Server::Song");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, id);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, filename);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, filepath);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, title);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, artist);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, genre);
        return h_;
    }

    public Song clone()
    {
        Song c = null;
        try
        {
            c = (Song)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeInt(this.id);
        ostr.writeString(this.filename);
        ostr.writeString(this.filepath);
        ostr.writeString(this.title);
        ostr.writeString(this.artist);
        ostr.writeString(this.genre);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.id = istr.readInt();
        this.filename = istr.readString();
        this.filepath = istr.readString();
        this.title = istr.readString();
        this.artist = istr.readString();
        this.genre = istr.readString();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, Song v)
    {
        if(v == null)
        {
            _nullMarshalValue.ice_writeMembers(ostr);
        }
        else
        {
            v.ice_writeMembers(ostr);
        }
    }

    static public Song ice_read(com.zeroc.Ice.InputStream istr)
    {
        Song v = new Song();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Song> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, Song v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<Song> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(Song.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final Song _nullMarshalValue = new Song();

    /** @hidden */
    public static final long serialVersionUID = -6842903373545227698L;
}
