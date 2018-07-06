package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by 10353 on 2018/1/6.
 * FTP服务器工具类
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;



    public static boolean upload(List<File> files) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, ftpUser, ftpPass, 21);
        logger.info("beginning upload images to FTP : " + ftpIp);
        boolean result = ftpUtil.upload(files, "image");
        return result;

    }

    /**
     * 上传文件逻辑
     * @param files
     * @param remoteFile ftp服务器在Linux服务器上是一个文件夹，如果要在它的下级创建文件，就需要这个文件夹
     * @return
     */
    private boolean upload(List<File> files, String remoteFile) throws IOException {

        boolean uploaded = false;
        FileInputStream fileInputStream = null;
        //1.连接服务器
        if(conectFTPServer(this.ip,this.port,this.user,this.pwd)){
            try {
                //将工作目录切换到image文件下
                ftpClient.changeWorkingDirectory(remoteFile);
                logger.info("change work {} success", remoteFile);
                ftpClient.setBufferSize(1024*512);
                ftpClient.setControlEncoding("utf-8");
                //设置文件格式(采用二进制流格式)
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                //开启被动模式
                ftpClient.enterLocalPassiveMode();
                for(File file : files){
                    fileInputStream = new FileInputStream(file);
                    ftpClient.storeFile(file.getName(), fileInputStream);
                }
                uploaded = true;
                logger.info("file upload FTP: {} success", ip);
            } catch (IOException e) {
               logger.error("file upload FTP {} fail : " + e.getMessage(), ip);
                e.printStackTrace();
            }finally {

                    if(fileInputStream != null)
                        fileInputStream.close();
                    ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    /**
     * FTP服务器登录
     * @param ip
     * @param port
     * @param user
     * @param pwd
     * @return
     */
    private boolean conectFTPServer(String ip, int port, String user, String pwd) {
        boolean isSucess = false;
        ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(1000*20);
        try {
            ftpClient.connect(ip,port);
            isSucess = ftpClient.login(user,pwd);
            logger.info("ftp server login success");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isSucess;
    }


    public FTPUtil(String ip, String user, String password, int port){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

//    public static void main(String[] args) {
//        String ss = PropertiesUtil.getProperty("ftp.server.ip");
//        System.out.print(ss);
//    }
}
