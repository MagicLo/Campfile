package tw.binary.dipper.model;


import com.orm.SugarRecord;

//繼承sugarORM
public class Quote extends SugarRecord<Quote> {
    private String name;
    private String price;
    private String symbol;
    private String type;
    private String utctime;

    public Quote() {
        this.name = "";
        this.price = "";
        this.symbol = "";
        this.type = "";
        this.utctime = "";
    }

    public Quote(String name, String price, String symbol, String type, String utctime) {
        this.name = name;
        this.price = price;
        this.symbol = symbol;
        this.type = type;
        this.utctime = utctime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUtctime() {
        return utctime;
    }

    public void setUtctime(String utctime) {
        this.utctime = utctime;
    }

}