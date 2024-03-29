package com.google.cloud.android.speech.Util;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * Created by Matthew on 10/25/2016.
 */

public class DrawableMutlipleStates {
    ArrayList<Drawable> myDrawables;
    ArrayList<Integer> checkpoint = new ArrayList<Integer>();
    int currentNum=0;
    int Max=0;
    public DrawableMutlipleStates(ArrayList<Drawable> drawables, int MaxNum){
        myDrawables=drawables;
        for(int i=0;i<myDrawables.size();i++){
            checkpoint.add((int)((float)(MaxNum)*((float)i/(float)myDrawables.size())));
        }
        checkpoint.add(MaxNum);
        Max=MaxNum-1;
    }
    public Drawable update(){
        currentNum++;
        for(int i=checkpoint.size()-1;i>0;i--){

            if(currentNum<checkpoint.get(i)&&currentNum>checkpoint.get(i-1)){
                return myDrawables.get(i-1);
            }
            if(currentNum>=Max){
                return myDrawables.get(myDrawables.size()-1);
            }
        }
        return myDrawables.get(0);
    }

    public float getProgress(){
        return (float)currentNum/(float)checkpoint.get(checkpoint.size()-1);
    }
    public boolean allComplete(){
        return currentNum>=checkpoint.get(checkpoint.size()-2);
    }
}
