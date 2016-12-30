package com.sok.mphone.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by user on 2016/12/19.
 */

public class IFragmentsFactory {

    public interface Type{
        int login_page = 0x00;
        int show_page = 0x01;
    }

    public static Fragment getInstans(int tag){
        Fragment frag = null;
        if(tag == IFragmentsFactory.Type.login_page){
         frag = new LoginFragments();
        }

        if(tag == IFragmentsFactory.Type.show_page){
            frag = new ShowFragments();
        }

        return frag;
    }


    public static void removeFragment(FragmentTransaction ft,Fragment frament){
        ft.remove(frament);
//        ft.commit();
        ft.commitAllowingStateLoss();
    }

    public static void repeateFragment(FragmentTransaction ft,int layout_rid,Fragment frament){
        ft.replace(layout_rid,frament);
//        ft.commit();
        ft.commitAllowingStateLoss();
    }

    public static void addFragment(FragmentTransaction ft,int layout_rid,Fragment frament,String tag){
        ft.add(layout_rid,frament,tag);
//        ft.commit();
        ft.commitAllowingStateLoss();
    }
    public static void addFragment(FragmentTransaction ft,int layout_rid,Fragment frament){
        ft.add(layout_rid,frament);
//        ft.commit();
        ft.commitAllowingStateLoss();
    }

}
