package com.example.LibGDXLoaderDemo;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import android.widget.FrameLayout;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Clipboard;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.enplug.utilities.Utilities;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

public class MyActivity extends AndroidApplication implements DialogInterface.OnDismissListener
{
    //Used with reflection to grab class objects from a child app
    private Class<?> loadedClass;
    private Context childAppCtx;

    //Used to initialize views and objects after reflection
    private View childView;
    private Game childGame;
    private FrameLayout libgdxFrame;
    private Screen loadedScreen;

    //Simple dialog popup used to notify the main activity that the child app downloading is complete
    private ProgressDialog progressDialog;

    public class Test extends Game
    {
        @Override
        public void create()
        {
            setScreen(null);
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        libgdxFrame = (FrameLayout) findViewById(R.id.libgdxFrame);


        Log.d("MainActivity", "Showing download progress dialog");
        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = ProgressDialog.show(this, "Hold on...", "Loading your apps...", true);
        progressDialog.setOnDismissListener(this);

        Log.d("MainActivity", "Downloading Child apk");
        //downloadUpdate("EnplugPlayer.apk", "http://enplug.com/packages/player/40/EnplugPlayer.apk");
        downloadUpdate("star-assault-android.apk", "http://dl.dropboxusercontent.com/sh/xt7xpa15401ru11/BHawa9XIMC/star-assault-android.apk?token_hash=AAGgjL8F9eDn9LhCbMPBOBeztZeRo-4Un923YFoLrUcEyA&dl=1");

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = false;
        cfg.useWakelock = true;
        cfg.useAccelerometer = false;
        cfg.useCompass = false;

        initialize(new Test(), cfg);
    }

    //For simplicity, we only start to load files after the progress dialog has been dismissed, indicating the child apk has been downloaded & installed.
    @Override
    public void onDismiss(final DialogInterface dialog)
    {
        Log.d("MainActivity", "Dynamically loading child app");
        try
        {
            Log.d("MainActivity", ">>>>>>>>>>Initializing Class Loader");
            childAppCtx = getApplicationContext().createPackageContext("net.obviam.starassault", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

            Log.d("MainActivity", "Using class loader to find entrypoint of libgdx app.");

            loadedClass =  Class.forName("net.obviam.starassault.StarAssault", true, childAppCtx.getClassLoader());

            //type testing
            Log.d("MainActivity", "Loaded Class is: " + loadedClass.getConstructor().newInstance().getClass().toString());
            Log.d("MainActivity", "Loaded Super Class is: "+loadedClass.getConstructor().newInstance().getClass().getSuperclass().toString());

            //Config setup boilerplate code
            final AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
            cfg.useGL20 = false;
            cfg.useWakelock = false; //when set to true we get some exceptions thrown. Probably better to fix in android manifest
            cfg.useAccelerometer = false;
            cfg.useCompass = false;

            //This doesn't work. Generates a class cast exception.
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
    //-------------------------------------------------------------------------------------------------

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

    //---------------------------------------------------------------------------------------------------
    //Utility Methods borrowed from EnpugUtilities.jar (thanks Justin!) to download and install a child apk from a remote location.

    private void installUpdate(final String filename)
    {
        Utilities.logI("MainActivity", "Status - Starting install for: " + filename + ".");
        Command command = new Command(0, "pm install -r /sdcard/Download/"+filename)
        {
            @Override
            public void output(int id, String line)
            {
                if(line.toLowerCase().contains("success"))
                {
                    progressDialog.dismiss();
                }
            }
        };
        try
        {
            RootTools.getShell(true).add(command).waitForFinish();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void downloadUpdate(final String filename, final String url)
    {
        Utilities.logI("MainActivity", "Starting download of: " + filename + " from " + url);

        //http://enplug.com/packages/player/40/EnplugPlayer.apk

        Command command = new Command(0, "wget -O /sdcard/Download/"+filename + " " + url)
        {
            @Override
            public void output(int id, String line)
            {
                if(line.contains("100% |*******************************|"))
                {
                    installUpdate(filename);
                }
            }
        };
        try
        {
            RootTools.getShell(true).add(command).waitForFinish();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}


