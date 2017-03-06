package br.com.versalius.iking.utils;

import android.content.Context;

import br.com.versalius.iking.model.User;

/**
 * Created by jn18 on 13/01/2017.
 */

public class SessionHelper {
    private static SessionHelper sessionHelper;
    private static Context context;

    private SessionHelper(Context context) {
        this.context = context;
    }

    //O contexto é setado é o da aplicação na LoginActivity ou na MainActivity e persiste até o fim da aplicação
    public static synchronized SessionHelper getInstance(Context context) {
        if(sessionHelper == null){
            sessionHelper = new SessionHelper(context);
        }

        return sessionHelper;
    }

    public static boolean isLogged() {
        //Se houver algum id salvo, então exsite um operador logado
        return !Preferences.getInstance(context).load(Preferences.USER_ID).isEmpty();
    }

    public static void logout() {
        Preferences.getInstance(context).clearAll();
    }

    public static String getUserName() {
        return Preferences.getInstance(context).load(Preferences.USER_NAME);
    }

    public static String getUserEmail() {
        return Preferences.getInstance(context).load(Preferences.USER_EMAIL);
    }

    public static String getAvatar() {
        return Preferences.getInstance(context).load(Preferences.USER_AVATAR);
    }

    public static long getUserId() {
        return Long.valueOf(Preferences.getInstance(context).load(Preferences.USER_ID));
    }

    public static int getCityId() {
        return Integer.parseInt(Preferences.getInstance(context).load(Preferences.USER_CITY_ID));
    }

    public static String getUserKey() {
        return Preferences.getInstance(context).load(Preferences.USER_KEY);
    }

    public static String getUserPassword() {
        return Preferences.getInstance(context).load(Preferences.USER_PASSWORD);
    }

    public static void saveUser(User user) {
        try {
            Preferences.getInstance(context).save(
                    Preferences.USER_NAME, user.getName());
            Preferences.getInstance(context).save(
                    Preferences.USER_EMAIL, user.getEmail());
            Preferences.getInstance(context).save(
                    Preferences.USER_AVATAR, user.getAvatar());
            Preferences.getInstance(context).save(
                    Preferences.USER_ID, user.getId());
            Preferences.getInstance(context).save(
                    Preferences.USER_KEY, user.getKey());
            Preferences.getInstance(context).save(
                    Preferences.USER_CITY_ID, user.getCity_id());
            Preferences.getInstance(context).save(
                    Preferences.USER_PASSWORD, user.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
