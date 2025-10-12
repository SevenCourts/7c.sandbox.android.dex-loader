package com.sevencourts.sandbox.app1loader;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "App1Loader";
    /**
     * This must be a class member, so that the GC does not collect it and we do not get
     * a "Failed to close dex file in finalizer." AssertionError.
     */
    private DexClassLoader dexClassLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loadButton = findViewById(R.id.loadButton);
        loadButton.setOnClickListener(v -> {
            try {
                loadAndRunDex();
            } catch (Exception e) {
                Log.e(TAG, "Failed to load and run DEX", e);
            }
        });
    }

    private void loadAndRunDex() throws Exception {
        String apkFileName = "app-payload-debug.apk";

        File dexFile = new File(getCacheDir(), apkFileName);

        // --- 1. Copy the APK from assets to the app's private cache directory ---
        if (dexFile.exists()) {
            boolean deleted = dexFile.delete();
            Log.d(TAG,"DEX file already exists. Deleting it: " + (deleted ? "OK" : "NOK"));
        }

        Log.d(TAG, "Copying DEX file from assets...");
        try (InputStream is = getAssets().open(apkFileName);
             OutputStream os = new FileOutputStream(dexFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
        Log.d(TAG, "Copied APK to: " + dexFile.getAbsolutePath());

        // Make the file read-only after copying
        boolean success = dexFile.setReadOnly();

        Log.println(success ? Log.DEBUG : Log.WARN, TAG, "Set DEX file to read-only.");


        // --- 2. Define the path for the optimized DEX (ODEX) file ---
        final File optimizedDexOutputPath = getDir("dex", Context.MODE_PRIVATE);

        // --- 3. Initialize DexClassLoader ---
        dexClassLoader = new DexClassLoader(
                dexFile.getAbsolutePath(),
                optimizedDexOutputPath.getAbsolutePath(),
                null, // No native libraries
                getClassLoader() // Parent classloader
        );

        // --- 4. Use reflection to load the class and invoke the methods

        // --- non-ui class
        String ping = (String) callStaticMethod("com.sevencourts.sandbox.app2payload.PingPong", "ping", null, null);

        // --- ui class

        Class<?>[] parameterTypes = new Class<?>[]{Context.class, String.class};
        Object[] args = new Object[]{this, ping};
        callStaticMethod("com.sevencourts.sandbox.app2payload.ui.Alert", "show", parameterTypes, args);
    }

    private Object callStaticMethod(String className, String methodName, Class<?>[] parameterTypes, Object[] args) {
        final Object result;
        try {
            Log.d(TAG, "Attempting to load class " + className);
            Class<?> loadedClass = dexClassLoader.loadClass(className);
            Log.d(TAG, "Attempting to invoke method " + className + " #" + methodName);

            Method method;
            // The first #invoke argument is null because it's a static method.
            if (parameterTypes!=null) {
                method = loadedClass.getMethod(methodName, parameterTypes);
                result = method.invoke(null, args);
            } else {
                method = loadedClass.getMethod(methodName);
                result = method.invoke(null);
            }

            Log.d(TAG, "Method invoked with result: " + result);

        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
