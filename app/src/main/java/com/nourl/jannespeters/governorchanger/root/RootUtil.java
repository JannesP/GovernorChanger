package com.nourl.jannespeters.governorchanger.root;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Mostly copied from: http://muzikant-android.blogspot.de/2011/02/how-to-get-root-access-and-execute.html
 * Edited by XneofuX@xda 23.07.2015
 */
public class RootUtil {

    /**
     * Executes multiple commands from a shell with root privileges.
     * @param commands a list of commands to be executed.
     * @return true if the execution was successful, otherwise false
     */
    public static boolean executeMultiple(ArrayList<String> commands) {
        boolean retval = false;

        try
        {
            if (null != commands && commands.size() > 0)
            {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands)
                {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                    Log.d("SU", "Executed: " + currCommand);
                }

                os.writeBytes("exit\n");
                os.flush();

                try
                {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval)
                    {
                        // Root access granted
                        retval = true;
                    }
                    else
                    {
                        // Root access denied
                        retval = false;
                    }
                }
                catch (Exception ex)
                {
                    Log.e("ROOT", "Error executing root action", ex);
                }
            }
        }
        catch (IOException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);
        }
        catch (SecurityException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);
        }
        catch (Exception ex)
        {
            Log.w("ROOT", "Error executing internal operation", ex);
        }

        return retval;
    }

    /**
     * Executes the command in a shell with root privileges.
     * Note: When using a few commands in rapidly use (@code executeMultiple(ArrayList<String> commands))
     *  because it uses only one root request!
     * @param command the command to execute
     * @return true if the execution was successful, otherwise false
     */
    public static boolean execute(String command) {
        boolean retval = false;

        try
        {
            if (null != command && !"".equals(command))
            {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                os.writeBytes(command + "\n");
                os.flush();

                os.writeBytes("exit\n");
                os.flush();

                try
                {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval)
                    {
                        // Root access granted
                        retval = true;
                    }
                    else
                    {
                        // Root access denied
                        retval = false;
                    }
                }
                catch (Exception ex)
                {
                    Log.e("ROOT", "Error executing root action", ex);
                }
            }
        }
        catch (IOException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);
        }
        catch (SecurityException ex)
        {
            Log.w("ROOT", "Can't get root access", ex);
        }
        catch (Exception ex)
        {
            Log.w("ROOT", "Error executing internal operation", ex);
        }

        return retval;
    }

}
