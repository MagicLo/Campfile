package tw.binary.dipper.model;

import com.orm.SugarRecord;

// Created by eason on 2015/4/30.
//繼承sugarORM
public class Weather extends SugarRecord<Weather> {
    public String Country; //國家
    public String City;    //城市
    public String Code;
    public String Date;    //日期
    public String Day;     //星期
    public String High;    //溫度高
    public String Low;     //溫度低
    public String Text;    //Mostly Clear, Sunny, Mostly Sunny, Partly Cloudy .......

    public Weather(String pCountry, String pCity, String pCode, String pDate, String pDay, String pHigh, String pLow, String pText) {
        Country = pCountry;
        City = pCity;
        Code = pCode;
        Date = pDate;
        Day = pDay;
        High = pHigh;
        Low = pLow;
        Text = pText;
    }

    public Weather() {
    }
}
