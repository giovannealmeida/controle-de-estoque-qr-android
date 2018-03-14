package br.com.versalius.e_stokrootsilver.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Giovanne on 01/07/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    //Constantes do banco
    private static final String DB_NAME = "estok_root_silver_db";
    private static final int DB_VERSION = 3;

    //Constantes das tabelas
    public static final String TBL_SESSION = "session";

    private SQLiteDatabase database;
    private static DBHelper instance;

    public static DBHelper getInstance(Context context){
        if(instance == null){
            instance = new DBHelper(context);
        }

        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public SQLiteDatabase getDatabase() {
        if(database == null || !database.isOpen()){
            database = getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Criação da tabela de sessão
        db.execSQL("CREATE TABLE " + TBL_SESSION + " (" +
                " email TEXT NOT NULL," +
                " first_name TEXT NOT NULL," +
                " last_name TEXT NOT NULL," +
                " password TEXT NOT NULL," +
                " level_id INTEGER," +
                " type_sale_id INTEGER," +
                " token TEXT," +
                " user_id INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_SESSION);

        onCreate(db);
    }

    /**
     * Deleta todos os registros de todas as tabelas do banco
     */
    public void clearAll() {
        getDatabase().execSQL("DELETE FROM " + DBHelper.TBL_SESSION);
    }

}
