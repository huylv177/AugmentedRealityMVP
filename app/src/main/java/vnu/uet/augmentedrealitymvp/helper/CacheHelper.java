package vnu.uet.augmentedrealitymvp.helper;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import vnu.uet.augmentedrealitymvp.common.util.VarUtils;

/**
 * Created by huylv on 21-Mar-16.
 */
public class CacheHelper {
    static final int READ_BLOCK_SIZE = 100;
    public static CacheHelper instance;
    private ArrayList<String> contentDat;
    private int numOfMarker;
    private ArrayList<String> availableMarkers;

    private CacheHelper() {
    }

    public static CacheHelper getInstance() {
        if (instance == null) instance = new CacheHelper();
        return instance;
    }

    public void cacheDataNFT(Context ctx, String markerName) {
        File cacheFolder = new File(ctx.getCacheDir().getAbsolutePath() + "/" + "DataNFT");
        boolean success = true;
        if (!cacheFolder.exists()) {
            success = cacheFolder.mkdir();
        } else {
            Log.e(getClass().getName(), "cache folder exist");
        }
        if (success) {
            Log.e(getClass().getName(), "cache folder created");
        } else {
            Log.e(getClass().getName(), "create cache error");
        }


        File fsetPath = new File(VarUtils.PATH_AR + File.separator + markerName + ".fset");
        File fset3Path = new File(VarUtils.PATH_AR + File.separator + markerName + ".fset3");
        File isetPath = new File(VarUtils.PATH_AR + File.separator + markerName + ".iset");

        if (fsetPath.exists() && fset3Path.exists() && isetPath.exists()) {
            Log.e(getClass().getName(), "fset exists");
            File fsetDest = new File(cacheFolder + File.separator + markerName + ".fset");
            if (fsetDest.exists()) {
                Log.e(getClass().getName(), "fset dest exists");
            } else {
                Log.e(getClass().getName(), "fset dest not exists");
                copyFile(fsetPath.toString(), cacheFolder.toString());
            }

            File fset3Dest = new File(cacheFolder + File.separator + markerName + ".fset3");
            if (fset3Dest.exists()) {
                Log.e(getClass().getName(), "fset3 dest exists");
            } else {
                Log.e(getClass().getName(), "fset3 dest not exists");
                copyFile(fset3Path.toString(), cacheFolder.toString());
            }

            File isetDest = new File(cacheFolder + File.separator + markerName + ".iset");
            if (isetDest.exists()) {
                Log.e(getClass().getName(), "iset dest exists");
            } else {
                Log.e(getClass().getName(), "iset dest not exists");
                copyFile(isetPath.toString(), cacheFolder.toString());
            }
        } else {
            Log.e(getClass().getName(), " fset not exists");
        }

        addMarkerNameInDat(ctx, markerName);
    }

    private void addMarkerNameInDat(Context ctx, String markerName) {
        //change marker name in marker.dat
        File markersdat = new File(ctx.getCacheDir().getAbsolutePath() + "/Data/markers.dat");
        contentDat = new ArrayList<>();
        availableMarkers = new ArrayList<>();

        //reading text from file
        try {
            FileInputStream fileIn = new FileInputStream(markersdat);
            InputStreamReader inputStreamReader = new InputStreamReader(fileIn);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String line = "";
            boolean nextLine = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (nextLine) {
                    numOfMarker = Integer.valueOf(line);
                    Log.e(getClass().getName(), "num of marker:" + numOfMarker);
                    line = String.valueOf(numOfMarker);
                    nextLine = false;
                }
                if (line.contains("# Number of markers")) {
                    nextLine = true;
                }
                if (line.contains("/DataNFT/")) {
                    availableMarkers.add(line);
                }
                contentDat.add(line);
            }

            //add new marker //test multi book
            boolean markerExist = false;
            for (String marker : availableMarkers) {
                if (marker.equals("../DataNFT/" + markerName)) {
                    markerExist = true;
                }
            }
            if (!markerExist) {
                contentDat.add("");
                contentDat.add("../DataNFT/" + markerName);
                contentDat.add("NFT");
                contentDat.add("FILTER 15.0");
                numOfMarker += 1;
                contentDat.set(1, String.valueOf(numOfMarker));
            }

            inputStreamReader.close();

            new RandomAccessFile(markersdat,"rw").setLength(0);

            FileOutputStream fOut = new FileOutputStream(markersdat);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            for(String s : contentDat){
                myOutWriter.append(s+"\n");
            }
            myOutWriter.close();
            fOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMarker(Context context, String markerName) {
        //change marker name in marker.dat
        File markersdat = new File(context.getCacheDir().getAbsolutePath() + "/Data/markers.dat");
        contentDat = new ArrayList<>();

        //reading text from file
        try {
            FileInputStream fileIn = new FileInputStream(markersdat);
            InputStreamReader inputStreamReader = new InputStreamReader(fileIn);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String line = "";
            boolean nextLine = false;
            boolean next4Lines = false;
            int count = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (nextLine) {
                    numOfMarker = Integer.valueOf(line);
                    numOfMarker -= 1;
                    line = String.valueOf(numOfMarker);
                    nextLine = false;
                }
                if (line.contains("# Number of markers")) {
                    nextLine = true;
                }
                if (line.contains("/DataNFT/" + markerName)) {
                    next4Lines = true;
                }
                if (count == 4) next4Lines = false;
                if (!next4Lines) contentDat.add(line);
                if (next4Lines) count += 1;
            }

            inputStreamReader.close();

            new RandomAccessFile(markersdat, "rw").setLength(0);

            FileOutputStream fOut = new FileOutputStream(markersdat);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            for (String s : contentDat) {
                myOutWriter.append(s + "\n");
            }
            myOutWriter.close();
            fOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean copyFile(String from, String to) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                int end = from.toString().lastIndexOf("/");
                String str1 = from.toString().substring(0, end);
                String str2 = from.toString().substring(end + 1, from.length());
                File source = new File(str1, str2);
                File destination = new File(to, str2);
                if (source.exists()) {
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
