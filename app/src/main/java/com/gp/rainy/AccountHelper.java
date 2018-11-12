package com.gp.rainy;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.gp.rainy.App.globalContext;

public class AccountHelper {

//     "userList":[
//    {
//        "account":"user1",
//            "password":"password1"
//    },
//    {
//        "account":"user1",
//            "password":"password1"
//    }
//                ],

    private static AccountHelper ins;

    private AccountHelper(){

    }

    public static AccountHelper getIns() {
        if (ins == null) {
            ins = new AccountHelper();
        }
        return ins;
    }

    public void cache(String acc, String pass) {
        String str = PreferenceUtils.getPreferenceString(globalContext, Constants.accountArray, "");

        Gson gson = new Gson();
        List<Account> userList = gson.fromJson(str, new TypeToken<List<Account>>() {}.getType());


        if (userList == null) {
            userList = new ArrayList<>();
        }

        Account account = new Account();
        account.account = acc;
        account.password = pass;

        userList.add(account);

        PreferenceUtils.setPreferenceString(globalContext, Constants.accountArray, gson.toJson(userList));
        Log.d("gpdata:", gson.toJson(userList));
    }

    public String delete(String acc){
        String str = PreferenceUtils.getPreferenceString(globalContext, Constants.accountArray, "");
        Gson gson = new Gson();
        List<Account> userList = gson.fromJson(str, new TypeToken<List<Account>>() {}.getType());

//        List<Account> userLis1 = gson.fromJson(array, new TypeToken<List<Account>>() {}.getType());

        if (userList != null && userList.size() > 0) {
            Iterator<Account> iterator = userList.iterator();
            while(iterator.hasNext()) {
                Account account = iterator.next();
                if (acc.equals(account.account)) {
                    iterator.remove();
                }
            }
        }

        PreferenceUtils.setPreferenceString(globalContext, Constants.accountArray, gson.toJson(userList));
        Log.d("gpdata:", gson.toJson(userList));

        return gson.toJson(userList);
    }
}
