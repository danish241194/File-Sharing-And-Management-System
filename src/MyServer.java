import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class MyServer {
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static int MYPORT = 2002;
    public static int DATASIZE = 1024;
    public static Map<String, String> user;
    public static Map<String, Vector> groups;
    public static Map<String, String> group_message;
    public static int MAX_MSG_SIZE=1000;

    public static boolean shutdown_server = false;
    public static void print(String s) {
        System.out.println(s);
    }
    public static void sendMsg(String message,OutputStream os) throws  Exception{
        message = message+" ";
        int i = message.length();
        for(;i<MAX_MSG_SIZE;i++){
            message+="x";
        }
        byte[] Arr = message.getBytes();
        os.write(Arr, 0, Arr.length);
        os.flush();
    }

    public static String recvMsg(InputStream is) throws Exception {
        byte[] arr = new byte[MAX_MSG_SIZE+1];
//        int bytesRead = is.read(arr, 0, MAX_MSG_SIZE);

        int bytesRead = -1;
        bytesRead = is.read(arr,0,MAX_MSG_SIZE);
        int current = bytesRead;

        do {
            bytesRead =
                    is.read(arr, current, (MAX_MSG_SIZE-current));
            if(bytesRead >= 0) current += bytesRead;
        }while(bytesRead > 0);


        String s = new String(arr);
        int siz = s.split(" ").length;
        String ss = "";
        for(int i = 0 ; i<siz-2 ; i++){
            ss+=s.split(" ")[i]+" ";
        }
        ss+=s.split(" ")[siz-2];
        return ss;
    }

    public static void sendData(String filepath, Socket sock) {
        try {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            OutputStream os = null;
            os = sock.getOutputStream();
            InputStream is = sock.getInputStream();
            String message = "";
            File file = new File(filepath);
            if (!file.exists()) {
                System.out.println("File Does Not Exist");
            }
            message = "" + file.length() ;
            sendMsg(message, os);
            byte[] mybytearray = new byte[DATASIZE];
            int file_length = (int) file.length();
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            while (file_length >= DATASIZE) {
                try {
                    bis.read(mybytearray, 0, mybytearray.length);
                    os.write(mybytearray, 0, mybytearray.length);
                    os.flush();
                    recvMsg(sock.getInputStream());

                    file_length -= DATASIZE;
                } catch (Exception e) {
                    print("timeout");
                }
            }
            if (file_length > 0) {
                bis.read(mybytearray, 0, file_length);
                os.write(mybytearray, 0, file_length);
                os.flush();
                recvMsg(sock.getInputStream());
            }
            print(recvMsg(is));

            if (bis != null) bis.close();
            if (os != null) os.close();
            if (fis != null) fis.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void recieveData(Socket sock, String path) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        String filename = "";

        try {
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            String message = recvMsg(is);
            filename = message.split(" ")[2];
            String username = message.split(" ")[1];

            String all_paths_of_user = user.get(username);
            if (all_paths_of_user.equals(""))
                all_paths_of_user += "/" + username + "/" + filename;
            else
                all_paths_of_user += " " + "/" + username + "/" + filename;

            user.put(username, all_paths_of_user);

            String final_path = path.concat(message.split(" ")[1] + "/" + filename);
            int filesize = (int) Long.parseLong(message.split(" ")[0]);
            byte[] mybytearray = new byte[DATASIZE];
            fos = new FileOutputStream(final_path.toString());
            bos = new BufferedOutputStream(fos);
            int count = 1;
            while (filesize >= DATASIZE) {
                int bytesRead = -1;
                bytesRead = is.read(mybytearray,0,mybytearray.length);
                int current = bytesRead;

                do {
                    bytesRead =
                            is.read(mybytearray, current, (mybytearray.length-current));
                    if(bytesRead >= 0) current += bytesRead;
                }while(bytesRead > 0);
//                bytesRead = is.read(mybytearray, 0, mybytearray.length);
//                int current = bytesRead;
                bos.write(mybytearray, 0, current);
                bos.flush();
                sendMsg("ack",os);
                count++;
                filesize -= DATASIZE;
            }
            if (filesize > 0) {
                int bytesRead = -1;
                bytesRead = is.read(mybytearray,0,filesize);
                int current = bytesRead;

                do {
                    bytesRead =
                            is.read(mybytearray, current, (filesize-current));
                    if(bytesRead > 0) current += bytesRead;
                }while(bytesRead > 0);
                sendMsg("ack",os);

                bos.write(mybytearray, 0, current);
                bos.flush();
            }
            sendMsg("Data uploaded Successfully",os);
            bos.close();
            is.close();
            fos.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void create_necessary_dirs(){
        File newFolder = new File("serverside");
        newFolder.mkdir();

    }
    public static class ServerThread extends Thread{
        Socket sock;
        ServerThread(Socket sock){
            this.sock = sock;
        }
        @Override
        public void run() {
            super.run();
            try {
                print("Accepted");
                InputStream is = sock.getInputStream();
                String query = recvMsg(is);
                    print(query.split(" ")[0]);
                if (query.split(" ")[0].equals("create_user")) {
                    File newFolder = new File("serverside/" + query.split(" ")[1]);
                    boolean created = newFolder.mkdir();
                    user.put(query.split(" ")[1], new String(""));
                    if (created)
                        System.out.println("Account Created !");
                    else
                        System.out.println("Already");
                } else if (query.split(" ")[0].equals("upload")) {
                    recieveData(sock, "serverside/");
                } else if (query.split(" ")[0].equals("create_folder")) {
                    File newFolder = new File("serverside/" + query.split(" ")[1]);
                    boolean created = newFolder.mkdir();
                    if (created)
                        System.out.println("Folder was created !");
                    else
                        System.out.println("Unable to create folder");
                } else if (query.split(" ")[0].equals("move_file")) {
                    String path1 = "serverside/" + query.split(" ")[1];
                    String path2 = "serverside/" + query.split(" ")[2];
                    String username = query.split(" ")[1].split("/")[0];
                    String[] all_files_uploaded = user.get(username).split(" ");
                    String res="";
                    for(int i  = 0 ; i< all_files_uploaded.length ; i++){
                        if(all_files_uploaded[i].equals("/"+query.split(" ")[1])){
                            if(res.equals("")){
                             res =  "/"+query.split(" ")[2];
                            }else{
                                res+= " /"+query.split(" ")[2];
                            }
                        }
                        else {
                            if(res.equals("")){
                                res = all_files_uploaded[i];
                            }else{
                                res += " "+all_files_uploaded[i];
                            }
                        }
                    }
                    user.put(username,res);
                    Files.move(Paths.get(path1), Paths.get(path2), StandardCopyOption.REPLACE_EXISTING);

                } else if (query.split(" ")[0].equals("ls")) {
                    File folder = new File("serverside/" + query.split(" ")[1] + "/" + query.split(" ")[2]);

                    File[] files = folder.listFiles();
                    String res_ = "";
                    for (File file : files) {
                        boolean isDirectory = file.isDirectory();
                        if(res_.equals("")){
                            res_ = file.getName();
                        }
                        else{
                            res_+=";;;"+file.getName();
                        }
                        if (isDirectory)
                            res_+="-D";
                        else
                            res_+="-F";
                    }
                    sendMsg(res_,sock.getOutputStream());

                } else if (query.split(" ")[0].equals("create_group")) {
                    groups.put(query.split(" ")[1], new Vector());
                    group_message.put(query.split(" ")[1], "");
                } else if (query.split(" ")[0].equals("list_groups")) {
                    Set<Map.Entry<String, Vector>> st = groups.entrySet();
                    String result = "";

                    for (Map.Entry<String, Vector> me : st) {
                        result += me.getKey() + " ";
                    }
                    print(result);
                    OutputStream os = sock.getOutputStream();
                    sendMsg(result, os);
                } else if (query.split(" ")[0].equals("join_group")) {
                    Vector myvec = groups.get(query.split(" ")[1]);
                    String usernm = query.split(" ")[2];
                    myvec.add(usernm);
                    groups.put(query.split(" ")[1], myvec);
                } else if (query.split(" ")[0].equals("leave_group")) {
                    String grp_name = query.split(" ")[1];
                    Vector myvec = groups.get(grp_name);
                    String usernm = query.split(" ")[2];
                    myvec.remove(usernm);
                    if (myvec.size() == 0) {
                        groups.remove(grp_name);
                    } else {
                        groups.put(grp_name, myvec);
                    }
                } else if (query.split(" ")[0].equals("list_detail")) {
                    try {
                        Vector vec = groups.get(query.split(" ")[1]);

                        Enumeration enu = vec.elements();
                        String res = "";
                        while (enu.hasMoreElements()) {
                            String user_name = enu.nextElement() + "";
                            if (res.equals(""))
                                res = user_name;
                            else
                                res += ";" + user_name;
                            if (user.containsKey(user_name)) {
                                res += " " + user.get(user_name);
                            }

                        }
                        OutputStream os = sock.getOutputStream();
                        sendMsg(res, os);
                    }catch (Exception e){
                        sendMsg("",sock.getOutputStream());
                    }
                } else if (query.split(" ")[0].equals("quit")) {
                    sock.close();
                    shutdown_server = true;
                    return;

                } else if (query.split(" ")[0].equals("share_msg")) {
                    String g_name = query.split(" ")[1];
                    String messages = group_message.get(g_name);
                    if (messages.equals("")) {
                        group_message.put(g_name, recvMsg(sock.getInputStream()));

                    } else {
                        group_message.put(g_name, messages + ",;," + recvMsg(sock.getInputStream()));

                    }
                    print(group_message.get(g_name));
                } else if (query.split(" ")[0].equals("get_file")) {
                    try {
                        String file_des_path = recvMsg(sock.getInputStream());
                        sendData(file_des_path, sock);
                        if (sock != null) sock.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (query.split(" ")[0].equals("inbox")) {
                    try {
                        String resul = group_message.get(query.split(" ")[1]);
                        sendMsg(resul, sock.getOutputStream());
                        if (sock != null) sock.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if(query.split(" ")[0].equals("upload_udp")){
                    receive_udp();
                }

                if (sock != null) sock.close();
            }
            catch (Exception e){
                print("Here");
                e.printStackTrace();
            }
        }

        public synchronized  void receive_udp() {
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            String filename="";
            try {
                sendMsg("ok",sock.getOutputStream());

                String message = recvMsg(sock.getInputStream());
                filename = message.split(" ")[2];
                String username = message.split(" ")[1];

                String all_paths_of_user = user.get(username);
                if (all_paths_of_user.equals(""))
                    all_paths_of_user += "/" + username + "/" + filename;
                else
                    all_paths_of_user += " " + "/" + username + "/" + filename;

                user.put(username, all_paths_of_user);

                String final_path = "serverside/".concat(message.split(" ")[1] + "/" + filename);


                int filesize = (int) Long.parseLong(recvMsg(sock.getInputStream()));
                sendMsg("ok",sock.getOutputStream());
                DatagramSocket dsoc=new DatagramSocket(2000);
                fos = new FileOutputStream(final_path);
                bos = new BufferedOutputStream(fos);
                int DATASIZE = 50000;

                byte[] b = new byte[DATASIZE];
                String ss = "ack";
                int count=0;

                while (filesize >= DATASIZE) {
                    DatagramPacket dp=new DatagramPacket(b,b.length);
                    dsoc.receive(dp);
                    dsoc.send(new DatagramPacket(ss.getBytes(), ss.length(), InetAddress.getLocalHost(), 4000));

                    bos.write(dp.getData(), 0, dp.getLength());
                    bos.flush();
                    filesize -= DATASIZE;
//                    print(count+"");
                    if(count%50==0){
                        print(((count/50)*2.6)+" mb uploaded");
                    }
                    count++;
                }

                if (filesize > 0) {
                    DatagramPacket dp=new DatagramPacket(b,b.length);
                    dsoc.receive(dp);

                    bos.write(dp.getData(), 0, dp.getLength());
                    bos.flush();
                    filesize -= DATASIZE;
                }
                sendMsg("Uploaded Successfully",sock.getOutputStream());
                bos.close();
                dsoc.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }

    public static void main(String args[]) {


            create_necessary_dirs();

            user = new HashMap<String, String>();
            groups = new HashMap<String, Vector>();
            group_message = new HashMap<String,String>();
            ServerSocket servsock = null;
        try {
            servsock = new ServerSocket(MYPORT);
            while (true) {

                print("Waiting for user");
                Socket sock = null;
                sock = servsock.accept();
                ServerThread serverThread = new ServerThread(sock);
                serverThread.start();
                serverThread.join();
                if(shutdown_server)break;

            }
            servsock.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}