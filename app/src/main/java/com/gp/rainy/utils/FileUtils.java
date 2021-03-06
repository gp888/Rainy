package com.gp.rainy.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.Formatter;

import com.gp.rainy.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.gp.rainy.App.globalContext;


public class FileUtils {

    private static DownloadManager downloadManager;

    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteFile(String filename) {
        return new File(filename).delete();
    }

    public static void deleteFileByDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }
    }

    public static boolean isFileExist(String filePath) {
        return new File(filePath).exists();
    }

    //将字符串写入到sdcard文件
    public static boolean writeSDCardFile(String filename, String content, boolean append) {
        boolean isSuccess = false;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(filename, append));
            bufferedWriter.write(content);
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(bufferedWriter);
        }
        return isSuccess;
    }

    public static String readFile(String filename) {
        File file = new File(filename);
        BufferedReader bufferedReader = null;
        String str = null;
        try {
            if (file.exists()) {
                bufferedReader = new BufferedReader(new FileReader(filename));
                str = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(bufferedReader);
        }
        return str;
    }

    ///data/data/<包名>/files - 应用内文件
    public static boolean writeInnerFile(Context context, String strSave, String fileName) {
        boolean isSuccess = false;
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            //设置文件名称，以及存储方式
            out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            //创建一个OutputStreamWriter对象，传入BufferedWriter的构造器中
            writer = new BufferedWriter(new OutputStreamWriter(out));
            //向文件中写入数据
            writer.write(strSave);
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(writer);
        }
        return isSuccess;
    }

    // /data/data/<包名>/files/ 目录下根据fileName去加载文件
    public static String readInnerFile(Context context, String fileName) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            //设置将要打开的存储文件名称
            in = context.openFileInput(fileName);
            //FileInputStream -> InputStreamReader ->BufferedReader
            reader = new BufferedReader(new InputStreamReader(in));
            String line = new String();
            //读取每一行数据，并追加到StringBuilder对象中，直到结束
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(reader);
        }
        return content.toString();
    }

    //从文件中读取字符串(可设置编码)
    public static StringBuilder readFile(File file, String charsetName) {
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            closeIO(reader);
        }
    }

    public static void copyFile(InputStream in, OutputStream out) {
        try {
            byte[] b = new byte[2 * 1024 * 1024]; //2M memory
            int len = -1;
            while ((len = in.read(b)) > 0) {
                out.write(b, 0, len);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(in, out);
        }
    }

    public static void copyFileFast(File in, File out) {
        FileChannel filein = null;
        FileChannel fileout = null;
        try {
            filein = new FileInputStream(in).getChannel();
            fileout = new FileOutputStream(out).getChannel();
            filein.transferTo(0, filein.size(), fileout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(filein, fileout);
        }
    }

    public static void shareFile(Context context, String title, String filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse("file://" + filePath);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static void zip(InputStream is, OutputStream os) {
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(os);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                gzip.write(buf, 0, len);
                gzip.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(is, gzip);
        }
    }

    public static void unzip(InputStream is, OutputStream os) {
        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(is);
            byte[] buf = new byte[1024];
            int len;
            while ((len = gzip.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(gzip, os);
        }
    }

    public static String formatFileSize(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }

    //将输入流写入到文件
    public static void Stream2File(InputStream is, File file) {
        byte[] b = new byte[1024];
        int len;
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(is, os);
        }
    }

    public static boolean createFolder(String filePath) {
        return createFolder(filePath, false);
    }

    public static boolean createFolder(String filePath, boolean recreate) {
        String folderName = getFolderName(filePath);
        if (folderName == null || folderName.length() == 0 || folderName.trim().length() == 0) {
            return false;
        }
        File folder = new File(folderName);
        if (folder.exists()) {
            if (recreate) {
                deleteFile(folderName);
                return folder.mkdirs();
            } else {
                return true;
            }
        } else {
            return folder.mkdirs();
        }
    }

    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    public static long getFileSize(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            return -1;
        }
        File file = new File(filepath);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    public static boolean rename(String filepath, String newName) {
        File file = new File(filepath);
        return file.exists() && file.renameTo(new File(newName));
    }

    public static String getFolderName(String filePath) {
        if (filePath == null || filePath.length() == 0 || filePath.trim().length() == 0) {
            return filePath;
        }
        int filePos = filePath.lastIndexOf(File.separator);
        return (filePos == -1) ? "" : filePath.substring(0, filePos);
    }

    public static ArrayList<File> getFilesArray(String path) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<File> listFile = new ArrayList<File>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    listFile.add(files[i]);
                }
                if (files[i].isDirectory()) {
                    listFile.addAll(getFilesArray(files[i].toString()));
                }
            }
        }
        return listFile;
    }

    public static boolean deleteFiles(String folder) {
        if (folder == null || folder.length() == 0 || folder.trim().length() == 0) {
            return true;
        }
        File file = new File(folder);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    //openImage
    public static void openImage(Context mContext, String imagePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(imagePath));
        intent.setDataAndType(uri, "image/*");
        mContext.startActivity(intent);
    }

    public static void openVideo(Context mContext, String videoPath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(videoPath));
        intent.setDataAndType(uri, "video/*");
        mContext.startActivity(intent);
    }

    public static void openURL(Context mContext, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);
    }

    //下载文件
    private static Long startDown(String fileurl, String path) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileurl));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(true);
        //        设置在通知栏是否显示下载通知(下载进度), 有 3 个值可选:
        //        VISIBILITY_VISIBLE:                   下载过程中可见, 下载完后自动消失 (默认)
        //        VISIBILITY_VISIBLE_NOTIFY_COMPLETED:  下载过程中和下载完成后均可见
        //        VISIBILITY_HIDDEN:                    始终不显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String fileName = fileurl.substring(fileurl.lastIndexOf("/") + 1);

        //设置Notification的标题和描述
//        request.setTitle("正在下载文件...");
//        request.setDescription("描述");
        request.setVisibleInDownloadsUi(true);
        if (TextUtils.isEmpty(path)) {
            path = "/rainy/download/";
        } else {
            path = "/rainy/" + path + "/";
        }

        File filePath = new File(Environment.getExternalStorageDirectory().getPath() + path);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        File myFile = new File(Environment.getExternalStorageDirectory().getPath() + path + fileName);
        if (myFile.exists()) {
            if(myFile.delete()) {
                ToastUtil.showToastShort("删除成功");
            }
        }
        request.setDestinationInExternalPublicDir(path, fileName + ".zip");
//将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        long downloadId = getDownloadManager().enqueue(request);
        PreferenceUtils.setPreferenceLong(globalContext, Constants.DOWNLOAD_ID, downloadId);
        return downloadId;
    }

    public static void download(String url, String path){
        long downloadId = PreferenceUtils.getPreferenceLong(globalContext, Constants.DOWNLOAD_ID, -1L);
        if(downloadId != -1L){
            int status = getDownloadStatus(downloadId);
            if(status == DownloadManager.STATUS_SUCCESSFUL){
//                Uri uri = getDownloadUri(downloadId);
                removeDownloadId(downloadId);
                startDown(url, path);
            }else if(status == DownloadManager.STATUS_FAILED){
                startDown(url, path);
            }
        }else{
            startDown(url, path);
        }
    }

    //当使用外部存储时，必须检查外部存储的可用性
    public static boolean isSDCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    //获取应用在SDCard上的工作路径
    public static String getAppExternalPath(Context context) {
/*        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append("Android/data/");
        sb.append(packageName);
        return sb.toString();*/
        return context.getObbDir().getAbsolutePath();
    }

    //获取SDCard上目录的路径
    @Deprecated
    public static String getExtraPath(String folder) {
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + folder;
        File file = new File(storagePath);
        if (!file.exists()) {
            file.mkdir();
        }
        return storagePath;
    }

    public static void createProjectSdcardFile() {
        try {
            if (!FileUtils.isSDCardAvailable()) {
                return;
            }
            File file = new File(Constants.DIR_PROJECT);
//            if (!file.exists()) {
//                file.mkdirs();
//            }

            file = new File(Constants.DIR_DOWNLOAD);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Constants.DIR_MEDIA);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Constants.DIR_VOICE);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Constants.DIR_IMAGE);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Constants.DIR_VIDEO);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Constants.DIR_FILE);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Constants.DIR_LOG);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Constants.DIR_IMAGE_TEMP);
            if (!file.exists()) {
                file.mkdirs();
            }

            // 临时图片禁止系统读取
            file = new File(Constants.DIR_IMAGE_TEMP_NOMEDIA);
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.') + 1;
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot, filename.length());
            }
        }
        return filename;
    }

    public static int getDownloadStatus(long downloadid){
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(downloadid);

        Cursor c = getDownloadManager().query(query);
        try {
            if (c.moveToFirst()) {
                return c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        }finally {
            c.close();
        }

        return -1;
    }

    public static DownloadManager getDownloadManager() {
        if (downloadManager == null) {
            downloadManager = (DownloadManager) globalContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        return downloadManager;
    }

    /**
     * 根据downloadID 获取获取本地文件存储的uri
     *
     * @param downloadId
     * @return
     */
    public static Uri getDownloadUri(long downloadId) {
        Uri downloadFileUri = getDownloadManager().getUriForDownloadedFile(downloadId);
        //适配不同的手机，有的手机不能识别，所以再转一遍
        Uri uri = Uri.fromFile(new File(getRealPathFromURI(downloadFileUri, globalContext)));
        return uri;
    }

    public static String getRealPathFromURI(Uri contentUri,Context context) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor!=null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            res = cursor.getString(columnIndex);
            cursor.close();
        }
        else {
            res = contentUri.getPath();
        }
        return res;
    }

    /**
     * 移除本地存储的downloadid 和相关文件
     *
     * @param downloadId
     */
    public static void removeDownloadId(long downloadId) {
        getDownloadManager().remove(downloadId);
        PreferenceUtils.remove(Constants.DOWNLOAD_ID);
    }

    /**
     * 根据downloadID获取本地存储的文件path
     *
     * @param downloadId
     * @return
     */
    public static String getDownloadPath(long downloadId) {
        String downloadPath = new File(getRealPathFromURI(getDownloadManager().getUriForDownloadedFile(downloadId), globalContext)).toString();
        return downloadPath;
    }
}