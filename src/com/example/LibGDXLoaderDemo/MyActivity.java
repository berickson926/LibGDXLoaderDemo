package com.example.LibGDXLoaderDemo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import android.widget.FrameLayout;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Clipboard;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.lang.reflect.InvocationTargetException;


public class MyActivity extends AndroidApplication
{
    View childView;
    Game childGame;
    FrameLayout libgdxFrame;
    Screen loadedScreen;


    Class<?> loadedClass;
    Context childAppCtx;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        libgdxFrame = (FrameLayout) findViewById(R.id.libgdxFrame);

        try
        {
            Log.d("MainActivity", ">>>>>>>>>>Initializing Class Loader");
            childAppCtx = getApplicationContext().createPackageContext("net.obviam.starassault", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

            Log.d("MainActivity", "Using class loader to find entrypoint of libgdx app.");

            loadedClass =  Class.forName("net.obviam.starassault.StarAssault", true, childAppCtx.getClassLoader());

            Log.d("MainActivity", "Instantiating child app & setting up views.");

            //getDeclaredConstructor looks for the non-default constructor that matches the parameters.
            //      Example Constructor:  sampleClass(int a, SomeCustomClassParam b){...}
            //      would be found using: getDeclaredConstructor(Integer.class, SomeCustomClassParam.class)
            //      haven't tested detection of primitives, so going from Integer to int may not work right.
            //final Game renderer = (GLSurfaceView.Renderer) loadedClass.getDeclaredConstructor(ApplicationListener.class, AndroidApplication.class).newInstance(getApplicationContext());

            //type testing
            Log.d("MainActivity", "Loaded Class is: " + loadedClass.getConstructor().newInstance().getClass().toString());
            Log.d("MainActivity", "Loaded Super Class is: "+loadedClass.getConstructor().newInstance().getClass().getSuperclass().toString());

            //Config setup boilerplate code
            final AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
            cfg.useGL20 = false;
            cfg.useWakelock = false; //when set to true we get some exceptions thrown. Probably better to fix in android manifest
            cfg.useAccelerometer = false;
            cfg.useCompass = false;


            //This doesn't work-generates a class cast exception.
            childGame = (Game) loadedClass.getConstructor().newInstance();

            Log.d("MainActivity", "Initializing child view");

            childView = initializeForView(childGame, cfg);

            Log.d("MainActivity", "Attaching view to screen layout");

            libgdxFrame.addView(childView);

            Log.d("MainActivity", "Startup sequence complete!<<<<<<<<<<<<<<<<");
        }
        catch(Exception ex)
        {
            Log.e("MainActivity", "Error in main activity onCreate()!");
            Log.e("MainActivity", ex.toString());
        }
    }

    //TODO: not sure of what to do about these guys below just yet, or if anything needs to be done.

    @Override
    public ApplicationListener getApplicationListener()
    {
        return null;
    }

    @Override
    public Net getNet()
    {
        return null;
    }

    @Override
    public Clipboard getClipboard()
    {
        return null;
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {}

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {}
}


