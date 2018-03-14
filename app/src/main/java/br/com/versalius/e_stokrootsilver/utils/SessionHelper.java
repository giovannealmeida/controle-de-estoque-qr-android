package br.com.versalius.e_stokrootsilver.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import br.com.versalius.e_stokrootsilver.model.User;

/**
 * Created by Giovanne on 30/06/2016.
 */
public class SessionHelper {

    private Context context;

    public SessionHelper(Context context) {
        this.context = context;
    }

    /**
     * Verifica o ID do usuário salvo no banco. Se o ID não existir, não há usuário salvo.
     *
     * @return true se o usuário estiver logado e for válido, false caso contrário
     */
    public boolean isLogged() {
        //Se houver algum id no banco, está logado.
        //Se não houver id algum, não está logado. O logout é forçado por motivos de segurança
        Integer id = getUserId();
        if (id == null) {
            //O logout é forçado pra limpar o banco
            logout();
            return false;
        }

        return true;
    }

    public void logout() {
        DBHelper.getInstance(context).clearAll();
    }

    public String getUserFirstName() {
        String userFirstName = "";
        String userId = getUserId().toString();

        DBHelper helper = DBHelper.getInstance(context);
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"first_name"}, "user_id = ?", new String[]{userId}, null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            userFirstName = cursor.getString(0);
        }
        cursor.close();
        helper.close();
        return userFirstName;
    }

    public String getUserLastName() {
        String userLastName = "";
        String userId = getUserId().toString();

        DBHelper helper = DBHelper.getInstance(context);
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"last_name"}, "user_id = ?", new String[]{userId}, null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            userLastName = cursor.getString(0);
        }
        cursor.close();
        helper.close();
        return userLastName;
    }

    public String getUserFullName() {
        return getUserFirstName() + " " + getUserLastName();
    }

    public Integer getUserId() {
        Integer userId = null;
        DBHelper helper = DBHelper.getInstance(context);
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"user_id"}, null, null, null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            userId = cursor.getInt(0);
        }
        cursor.close();
        helper.close();
        return userId;
    }

    public Integer getUserTypeSaleId() {
        Integer userTypeSaleId = null;
        String userId = getUserId().toString();

        DBHelper helper = DBHelper.getInstance(context);
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"type_sale_id"}, "user_id = ?", new String[]{userId}, null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            userTypeSaleId = cursor.getInt(0);
        }
        cursor.close();
        helper.close();
        if (userTypeSaleId == null) {
            return 0;
        }
        return userTypeSaleId;
    }

    public String getUserEmail() {
        String userEmail = "";
        String userId = getUserId().toString();

        DBHelper helper = DBHelper.getInstance(context);
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"email"}, "user_id = ?", new String[]{userId}, null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            userEmail = cursor.getString(0);
        }
        cursor.close();
        helper.close();
        return userEmail;
    }

    public String getUserToken() {
        String token = "";
        try {
            String userId = getUserId().toString();
            DBHelper helper = DBHelper.getInstance(context);
            Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"token"}, "user_id = ?", new String[]{userId}, null, null, null, null);

            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                token = cursor.getString(0);
            }
            cursor.close();
            helper.close();
        } catch (Exception e) {
            //No login sempre vai dar NullPointerException pois getUserId() vai retornar NULL
            e.printStackTrace();
        }
        return token;
    }

    /**
     * Salva o usuário passado no banco. As informações sensíveis (passoword, email e id) são salvas
     * no banco para maior segurança.
     *
     * @param user - Usuário a ser salvo
     */
    public void saveUser(User user, String token) {
        try {
            DBHelper helper = DBHelper.getInstance(context);

            //Salva no banco os dados sensíveis
            ContentValues values = new ContentValues();

            values.put("user_id", user.getId());
            values.put("first_name", user.getFirstName());
            values.put("last_name", user.getLastName());
            values.put("email", user.getEmail());
            values.put("password", user.getPassword());
            values.put("level_id", user.getLevelId());
            values.put("type_sale_id", user.getTypeSaleId());
            values.put("token", token);

            helper.getDatabase().insert(DBHelper.TBL_SESSION, null, values);

            helper.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Atualiza dados de seção já existentes
     *
     * @param user - Usuário a ser atualizado
     */
    public void updateUser(User user) {
        try {
            DBHelper helper = DBHelper.getInstance(context);
            ContentValues values = new ContentValues();

            values.put("email", user.getEmail());
            values.put("password", user.getPassword());

            helper.getDatabase().update(DBHelper.TBL_SESSION, values, "user_id=" + user.getId(), null);
            helper.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
