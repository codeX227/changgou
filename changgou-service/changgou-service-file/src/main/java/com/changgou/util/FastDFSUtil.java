package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * 实现 FastDFS文件管理
 */
public class FastDFSUtil {

    /**
     * 加载 tracker链接信息
     */
    static {
        try {
            //查找classpath下文件路径
            String filename = new ClassPathResource("fdfs_client.conf").getPath();
            ClientGlobal.init(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param fastDFSFile 上传的文件信息封装
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception {
        //附加参数
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author",fastDFSFile.getAuthor());

        //获取 TrackerServer
        TrackerServer trackerServer = getTrackerServer();

        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        //通过StorageClient访问storage，实现文件上传，并获取文件上传后信息
        String[] uploads = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);

        return uploads;
    }

    /**
     * 获取文件信息
     * @param group_name 文件组名
     * @param remote_filename 文件存储路径名
     */
    public static FileInfo getFile(String group_name,String remote_filename) throws Exception {
        //获取 TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        //获取文件信息
        return storageClient.get_file_info(group_name,remote_filename);
    }

    /**
     * 文件下载
     * @param group_name 文件组名
     * @param remote_filename 文件存储路径名
     */
    public static InputStream downloadFIle(String group_name,String remote_filename) throws Exception {
        //获取 TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        //文件下载
        byte[] buffer = storageClient.download_file(group_name, remote_filename);
        return new ByteArrayInputStream(buffer);
    }

    /**
     * 文件删除
     * @param group_name 文件组名
     * @param remote_filename 文件存储路径名
     */
    public static void deleteFile(String group_name,String remote_filename) throws Exception{
        //获取 TrackerServer
        TrackerServer trackerServer = getTrackerServer();
        //获取StorageClient
        StorageClient storageClient = getStorageClient(trackerServer);

        //删除文件
        storageClient.delete_file(group_name, remote_filename);
    }

    /**
     * 获取Storage信息
     */
    public static StorageServer getStorages() throws Exception{
        //创建一个 tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取Storage信息
        return trackerClient.getStoreStorage(trackerServer);
    }

    /**
     * 获取 Storage ip和端口号
     */
    public static ServerInfo[] getServerInfo(String group_name,String remote_filename) throws Exception{
        //创建一个 tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取 Storage ip和端口号
        return trackerClient.getFetchStorages(trackerServer,group_name,remote_filename);
    }

    /**
     * 获取 Tracker 信息
     */
    public static String getTrackerInfo()throws Exception{
        //获取 TrackerServer
        TrackerServer trackerServer = getTrackerServer();

        //Tracker的ip、http端口
        String ip = trackerServer.getInetSocketAddress().getHostString();
        int tracker_http_port = ClientGlobal.getG_tracker_http_port();

        String url = "http://"+ip+":"+tracker_http_port;
        return url;
    }

    /**
     * 获取 TrackerServer
     * @return
     * @throws Exception
     */
    public static TrackerServer getTrackerServer() throws Exception{
        //创建一个 tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取连接信息
        TrackerServer trackerServer = trackerClient.getConnection();

        return trackerServer;
    }

    /**
     * 获取 StorageClient
     * @param trackerServer
     * @return
     */
    public static StorageClient getStorageClient(TrackerServer trackerServer){
        //通过trackerServer连接信息获取storage的连接信息，创建StorageClient对象存储storage连接信息
        StorageClient storageClient = new StorageClient(trackerServer, null);

        return storageClient;
    }
}
