package xyz.parti.catan.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dalikim on 2017. 3. 30..
 */

public class DateHelper {
    public static Date toDate(String value) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
}
