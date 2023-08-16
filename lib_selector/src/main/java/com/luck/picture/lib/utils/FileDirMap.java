package com.luck.picture.lib.utils;

import android.content.Context;
import android.os.Environment;

import com.luck.picture.lib.config.SelectMimeType;

import java.io.File;
import java.util.HashMap;

/**
 * @author：luck
 * @date：2022/9/20 7:57 下午
 * @describe：FileDirMap
 */
public final class FileDirMap {
    private static final HashMap<Integer, String> dirMap = new HashMap<>();

    public static void init(Context context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        if (null == dirMap.get(SelectMimeType.TYPE_IMAGE)) {
            dirMap.put(SelectMimeType.TYPE_IMAGE, getFilePathOrCreateDirectory(context, Environment.DIRECTORY_PICTURES));
        }
        if (null == dirMap.get(SelectMimeType.TYPE_VIDEO)) {
            dirMap.put(SelectMimeType.TYPE_VIDEO, getFilePathOrCreateDirectory(context, Environment.DIRECTORY_MOVIES));
        }
        if (null == dirMap.get(SelectMimeType.TYPE_AUDIO)) {
            dirMap.put(SelectMimeType.TYPE_AUDIO, getFilePathOrCreateDirectory(context, Environment.DIRECTORY_MUSIC));
        }
    }

    /**
    * @Desc TODO(根据上下文获取外部公开目录的绝对地址path,修复直接获取path空指针)
    * @author 彭石林
    * @parame [context, type]
    * @return java.lang.String
    * @Date 2023/8/16
    */
    public static String getFilePathOrCreateDirectory(Context context, String type){
        File externalFile = context.getExternalFilesDir(type);
        if(!externalFile.exists()){
            externalFile.mkdirs();
        }else{
            if(!externalFile.isDirectory()){
                externalFile.delete();
                externalFile.mkdirs();
            }
        }
        return externalFile.getPath();
    }

    public static String getFileDirPath(Context context, int type) {
        String dir = dirMap.get(type);
        if (null == dir) {
            init(context);
            dir = dirMap.get(type);
        }
        return dir;
    }

    public static void clear() {
        dirMap.clear();
    }
}
