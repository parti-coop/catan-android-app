package xyz.parti.catan.models;

import org.parceler.Parcel;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class FileSource {
    public String attachment_url;
    public String attachment_filename;
    public String name;
    public String file_type;
    public String file_size;
    public String human_file_size;
    public Integer seq_no;

    public boolean isImage() {
        if (file_type == null) { return false; }
        return file_type.startsWith("image/");
    }

    public boolean isDoc() {
        return !isImage();
    }
}
