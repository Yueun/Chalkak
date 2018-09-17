package kr.ac.pusan.chalkak;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

/**
 * Save Chat data
 * Created by nakayama on 2017/01/13.
 */
public class AppData {

    /**
     * Save keys
     */
    public enum Key {
        MessageList
    }

    /**
     * Save object data as json
     * @param context application context
     * @param key save key
     * @param object save object
     */
    private static void putObjectData(@NotNull Context context, String key, Object object) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson =new Gson();
        String jsonData = gson.toJson(object);
        editor.putString(key, jsonData);
        editor.apply();
    }

    /**
     * Load object data
     * @param context application context
     * @param key saved key
     * @param classOfT saved type
     * @return
     */
    @Nullable
    private static Object getObjectData(@NotNull Context context, String key, Class classOfT) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonData = sharedPreferences.getString(key, "");
        if (jsonData.equals("")) {
            return null;
        } else {
            return gson.fromJson(jsonData, classOfT);
        }
    }

    /**
     * Save Message list
     * @param context application context
     * @param messages receive and sent messages
     */
    public static void putMessageList(Context context, MessageList messages) {
        putObjectData(context, Key.MessageList.name(), messages);
    }

    /**
     * Load saved messages
     * @param context application context
     * @return saved messages
     */
    public static MessageList getMessageList(Context context) {
        return (MessageList) getObjectData(context, Key.MessageList.name(), MessageList.class);
    }

    public static void reset(@NotNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

}
