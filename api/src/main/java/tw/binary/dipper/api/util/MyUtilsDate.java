package tw.binary.dipper.api.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Created by eason on 2015/3/31.
public class MyUtilsDate {

    public static String CurrentDateTime() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        return simpleDate.format(new Date());
    }

    public static String getDisplayTime(String datetime) {
        if (datetime == null) return null;
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        final DateFormat[] df = new DateFormat[]{
                DateFormat.getDateInstance(), DateFormat.getTimeInstance(DateFormat.SHORT)};
        Date now = new Date();
        try {
            Date dt = sdf.parse(datetime);
            if (now.getYear() == dt.getYear() && now.getMonth() == dt.getMonth() && now.getDate() == dt.getDate()) {
                return df[1].format(dt);
            }
            return df[0].format(dt);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
