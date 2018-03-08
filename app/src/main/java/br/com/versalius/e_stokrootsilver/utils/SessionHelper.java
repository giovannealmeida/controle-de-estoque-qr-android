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
     * Verifica o ID do usuário salvo no Shared Preferences. Se o ID não existir, não há usuário salvo,
     * se o ID existir, busca o email associado a este ID no banco. Após obter o email, verifica se
     * é igual ao email salvo no Shared Preferences. Se for igual, o usuário está logado. Se não for
     * igual, o usuário está logado mas não é válido e o logout é forçado.
     *
     * @return true se o usuário estiver logado e for válido, false caso contrário
     */
    public boolean isLogged() {
        //Se houver algum id salvo, verifica se é o email salvo nas Preferences é válidoo comparando com o email do banco. Se for, está logado.
        //Se não houver id algum, não está logado.
        String id = getUserId();
        if (id.isEmpty()) {
            //O usuário pode ter uma sessão válida no banco mas ter apagado o Shared Preferences
            //O logout é forçado pra limpar o banco
            logout();
            return false;
        }

        DBHelper helper = DBHelper.getInstance(context);
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"email"}, "user_id = ?", new String[]{id}, null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            if (cursor.getString(0).equals(getUserEmail())) { //O email do banco é igual ao salvo. Usuário válido e logado
                return true;
            }
        }
        cursor.close();
        helper.close();
        //O email salvo não é válido, força logout.
        logout();
        return false;
    }

    public void logout() {
        PreferencesHelper.getInstance(context).clearAll();
        DBHelper.getInstance(context).clearAll();
    }

    public String getUserFirstName() {
        return PreferencesHelper.getInstance(context).load(PreferencesHelper.USER_FIRST_NAME);
    }

    public String getUserLastName() {
        return PreferencesHelper.getInstance(context).load(PreferencesHelper.USER_LAST_NAME);
    }

    public String getUserFullName(){
        return getUserFirstName()+" "+getUserLastName();
    }

    public String getUserId() {
//        return PreferencesHelper.getInstance(context).load(PreferencesHelper.USER_ID);
        String userId = "";
        DBHelper helper = DBHelper.getInstance(context);
        Cursor cursor = helper.getDatabase().query(DBHelper.TBL_SESSION, new String[]{"user_id"}, null, null, null, null, null, null);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
                userId = String.valueOf(cursor.getInt(0));
        }
        cursor.close();
        helper.close();
        return userId;
    }

    public String getUserEmail() {
        return PreferencesHelper.getInstance(context).load(PreferencesHelper.USER_EMAIL);
    }

    /**
     * Salva o usuário passado no banco. As informações sensíveis (passoword, email e id) são salvas
     * no banco para maior segurança.
     *
     * @param user - Usuário a ser salvo
     */
    public void saveUser(User user) {
        try {
            DBHelper helper = DBHelper.getInstance(context);

            //Salva no banco os dados sensíveis
            ContentValues values = new ContentValues();

            values.put("user_id", user.getId());
            values.put("email", user.getEmail());
            values.put("password", user.getPassword());
            values.put("level_id", user.getLevelId());

            helper.getDatabase().insert(DBHelper.TBL_SESSION, null, values);

            helper.close();

            //Salva no Shared Preferences dados de acesso rápido

            PreferencesHelper.getInstance(context).save(PreferencesHelper.USER_FIRST_NAME, user.getFirstName());
            PreferencesHelper.getInstance(context).save(PreferencesHelper.USER_LAST_NAME, user.getLastName());
            PreferencesHelper.getInstance(context).save(PreferencesHelper.USER_ID, user.getId()); //TODO: verificar necessidade já que getUserId() pega id do banco
            PreferencesHelper.getInstance(context).save(PreferencesHelper.USER_EMAIL, user.getEmail());
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

            //Salva no Shared Preferences dados de acesso rápido
            PreferencesHelper.getInstance(context).save(PreferencesHelper.USER_FIRST_NAME, user.getFirstName());
            PreferencesHelper.getInstance(context).save(PreferencesHelper.USER_LAST_NAME, user.getLastName());
            PreferencesHelper.getInstance(context).save(PreferencesHelper.USER_EMAIL, user.getEmail());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
