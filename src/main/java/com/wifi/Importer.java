package com.wifi;

import java.util.Map;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

public class Importer {

    private final int RANGE = 600;
    private final String NULL_MAC = "00:00:00:00:00:00";
    private final String DIR_PATH = "./dump";
    private Map<String, Device> listOfDevices = new HashMap<>();
    private MongoService mongo = new MongoService();

    private FilenameFilter filenameFilter = (dir, name) -> StringUtils.contains(name,"dumpcat");

    private boolean isShouldSkipMac(String mac) {
        if (mac.isEmpty()) {
            return true;
        }
        if (mac.length() != 17) {
            return true;
        }
        if (mac.equalsIgnoreCase(NULL_MAC)) {
            return true;
        }
        String secondSymbol = String.valueOf(mac.charAt(1));
        if (StringUtils.containsAny(secondSymbol, "26AEae")) {
            return true;
        }
        return false;
    }

    private String extractTs(String name) {
        return name.substring(8, 18);
    }

    private void isDeviceEndSession (Long ts) {

        Iterator<Map.Entry<String, Device>> iterator = listOfDevices.entrySet().iterator();
         while(iterator.hasNext()){
            Map.Entry<String, Device> device = iterator.next();
            Long offline = ts - device.getValue().getEndSession();
            if(offline >= RANGE){
                Device currentDevice = device.getValue();
                if(currentDevice.showDuration() != 0) {
                    mongo.putInToDB(currentDevice.createDocument());
                }
                iterator.remove();
            }
        }
    }

    private void addOrUpdateDevice (BufferedReader fileReader, Long ts) {
        fileReader.lines().forEach(mac -> {
            if (!isShouldSkipMac(mac)) {
                if(listOfDevices.containsKey(mac)){
                    Device current = listOfDevices.get(mac);
                    current.setEndSession(ts);
                    listOfDevices.replace(mac , current);
                } else {
                    listOfDevices.put(mac, new Device(mac,ts));
                }
            }
        });
    }

    private void importFileContent(File file, Long ts) {

        isDeviceEndSession(ts);
        BufferedReader fileReader = null;

        try {
            fileReader = new BufferedReader(new FileReader(file));
            addOrUpdateDevice(fileReader, ts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fileReader!=null)
                    fileReader.close();
                    file.delete();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void importData() {
        int counter = 0;
        File dumpDirectory = new File(DIR_PATH);
        File[] files = dumpDirectory.listFiles(filenameFilter);
        Arrays.sort(files, Comparator.comparing(File::getName));
        mongo.extractHistory(listOfDevices);

        for (File file : files) {
            counter++;
            Long ts = Long.valueOf(extractTs(file.getName()));
            importFileContent(file , ts);
            if(counter == files.length) {
                mongo.createHistory(listOfDevices);
                mongo.closeConnect();
            }
        }
    }
}
