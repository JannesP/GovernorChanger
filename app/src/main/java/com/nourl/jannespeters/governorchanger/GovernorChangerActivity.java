package com.nourl.jannespeters.governorchanger;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nourl.jannespeters.governorchanger.root.RootUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class GovernorChangerActivity extends ActionBarActivity implements RadioGroup.OnCheckedChangeListener {

    private List<RadioButton> radioButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_governor_changer);

        RadioGroup governorGroup = (RadioGroup)findViewById(R.id.radioGroupGovernors);
        List<String> availableGovernors = getAvailableGovernors();
        String activeGov = getActiveGovernor();
        for (String gov : availableGovernors) {
            RadioButton rb = createRadioButton(gov);
            governorGroup.addView(rb);
            if (gov.equals(activeGov)) governorGroup.check(rb.getId());
        }
        governorGroup.setOnCheckedChangeListener(this);
    }

    private int createdRadioButtonCount = 0;
    /**
     * Creates a new RadioButton for the governors
     * @param text The displayed text on the button
     * @return the new RadioButton
     */
    private RadioButton createRadioButton(String text) {
        RadioButton rb = new RadioButton(getApplicationContext());
        rb.setId(createdRadioButtonCount++);
        rb.setText(text);
        rb.setTextColor(Color.DKGRAY);
        radioButtons.add(rb);
        return rb;
    }

    /**
     * Changes the governor of all cpu cores to the given one.
     * Careful, it doesn't check if the governor is valid!
     * @param governor the new cpu governor
     * @return true if it was changed successful, false otherwise
     */
    public boolean setGovernor(String governor) {
        int coreCount = getCPUCoreCount();
        String currGovernor = getActiveGovernor();
        if (currGovernor.equals(governor)) {    //check if we're trying to change to the active gov (mainly to prevent unnecessary toasts :P)
            return true;
        }
        ArrayList<String> commands = new ArrayList<>(coreCount);
        for (int i = 0; i < coreCount; i++) {
            commands.add("echo " + governor + " > /sys/devices/system/cpu/cpu" + String.valueOf(i) + "/cpufreq/scaling_governor");
        }
        boolean res = RootUtil.executeMultiple(commands);
        if (!res) {
            Toast.makeText(getApplicationContext(), "Failed getting root! Didn't change anything.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            Toast.makeText(getApplicationContext(), "Successfully changed the governor to: " + governor + "!", Toast.LENGTH_SHORT).show();
            return true;
        }

    }

    /**
     * Determines the active governor from the first cpu core (cpu0).
     * @return the current governor
     */
    public static String getActiveGovernor() {
        return readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
    }

    /**
     * Builds a list of all governors which are available on all cores.
     * @return the list of all available governors
     */
    public static List<String> getAvailableGovernors() {
        int cores = getCPUCoreCount();
        List<List<String>> availableGovernorsAllCores = new ArrayList<>();
        List<String> availableGovernors = new ArrayList<>();
        for (int i = 0; i < cores; i++) {
            availableGovernorsAllCores.add(Arrays.asList(readFile("/sys/devices/system/cpu/cpu" + String.valueOf(i) + "/cpufreq/scaling_available_governors").split(" ")));
        }

        next : for (int i = 0; i < availableGovernorsAllCores.get(0).size(); i++) {
            String currCheck = availableGovernorsAllCores.get(0).get(i);    //check all governor entries

            for (int n = 1; n < availableGovernorsAllCores.size(); n++) {   //with all cpu cores
                if (!availableGovernorsAllCores.get(n).contains(currCheck)) {
                    continue next;
                }
            }

            availableGovernors.add(currCheck);
        }

        return availableGovernors;
    }

    /**
     * Reads one line files. Mainly for system values like cpu freqs, governors, etc.
     * @param path the path of the file
     * @return the line which was read
     */
    public static String readFile(String path) {
        String res = null;
        RandomAccessFile reader = null;
        try {
            reader = new RandomAccessFile(path, "r");

            res = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        Log.v("FileRead", "Read: \"" + res + "\" from \"" + path + "\"");
        return res;
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getCPUCoreCount() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        List<String> availableGovernors = getAvailableGovernors();
        RadioButton rb = findRadioButtonById(group.getCheckedRadioButtonId());
        if (rb == null) {
            Log.d("NoRadioButtonFound", "The radio button was not found for the id: " + group.getCheckedRadioButtonId() + "!");
            return;
        }
        if (availableGovernors.contains(rb.getText().toString())) {
            Log.d("SelectedSomething", "Selected gov: " + rb.getText().toString() + " with id: " + checkedId);
            setGovernor(rb.getText().toString());
        }
    }

    /**
     * Easily retrieve a code created RadioButton
     * @param id the ID which shuld be checked
     * @return null if the id was not found
     */
    private RadioButton findRadioButtonById(int id) {
        if (id == -1) return null;
        for (RadioButton rb : radioButtons) {
            if (rb.getId() == id) {
                return rb;
            }
        }
        return null;
    }
}
