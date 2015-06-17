package tw.binary.dipper.model;

import com.orm.SugarRecord;

public class Symbol extends SugarRecord<Symbol> {

    private String country;
    private String name;
    private String code;
    private boolean isTracked;

    // constructors
    public Symbol(String country, String name, String code) {
        this.country = country;
        this.name = name;
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

 /*
    public long insert(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("Country", Country);
        cv.put("name", name);
        cv.put("Code", Code);
        cv.put("isTracked", 0);
        return db.insert(TABLE_NAME, null, cv);
    }

    public boolean update(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put("isTracked", isTracked ? 1 : 0);
        return db.update(TABLE_NAME, cv, "_id = ?", new String[]{String.valueOf(_id)})
                == 1 ? true : false;
    }

    public boolean load(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, null, "Code = ?", new String[]{Code}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                _id = cursor.getLong(cursor.getColumnIndex("_id"));
                Country = cursor.getString(cursor.getColumnIndex("Country"));
                name = cursor.getString(cursor.getColumnIndex("name"));
                Code = cursor.getString(cursor.getColumnIndex("Code"));
                isTracked = cursor.getInt(cursor.getColumnIndex("isTracked")) == 1 ? true : false;
                return true;
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    // Close the db
    public void close() {
        db.close();
    }

    public void delete(int Id) {
        // Delete from DB where id match
        db.delete("symbol", "_id = " + Id, null);
    }
*/

}