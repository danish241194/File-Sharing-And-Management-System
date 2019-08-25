import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
public class MyClient {
    public static int SERVER_PORT = 2002;
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static String SERVER_HOST = "127.0.0.1";
    public static int DATASIZE = 1024;
    public static int MAX_MSG_SIZE=1000;
    public static void recieveData(Socket sock, String path,String file_des_path) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            sendMsg(file_des_path,os);
            String message = recvMsg(is);
            String final_path = path;
            int filesize = (int) Long.parseLong(message);
            byte[] mybytearray = new byte[DATASIZE];
            fos = new FileOutputStream(final_path.toString());
            bos = new BufferedOutputStream(fos);
            int count = 1;
            while (filesize >= DATASIZE) {
                int bytesRead = -1;
                bytesRead = is.read(mybytearray, 0, mybytearray.length);
                int current = bytesRead;
                bos.write(mybytearray, 0, current);
                bos.flush();
                count++;
                filesize -= DATASIZE;
            }
            if (filesize > 0) {
//                print("--Data Recieved--" + count);
                int bytesRead = -1;
                bytesRead = is.read(mybytearray, 0, filesize);

                // System.out.println(bytesRead);

                int current = bytesRead;

                bos.write(mybytearray, 0, current);
                bos.flush();
            }
            sendMsg("Data uploaded Successfully",os);
            print("Downloaded successfully");
            bos.close();
            is.close();
            fos.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void print(String s) {
        System.out.println(s);

    }

    public static String recvMsg(InputStream is) throws Exception {
        byte[] arr = new byte[MAX_MSG_SIZE+1];
        int bytesRead = is.read(arr, 0, MAX_MSG_SIZE);
        String s = new String(arr);
        int siz = s.split(" ").length;
        String ss = "";
        for(int i = 0 ; i<siz-2 ; i++){
            ss+=s.split(" ")[i]+" ";
        }
        ss+=s.split(" ")[siz-2];
        return ss;
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
    public static void sendData(String filepath, Socket sock, String user) {
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
            String[] path_split = filepath.split("/");
            message = "" + file.length() + " " + user+" "+path_split[path_split.length-1];
            print("sharing : "+path_split[path_split.length-1]);
            sendMsg(message, os);
            byte[] mybytearray = new byte[DATASIZE];
            int file_length = (int) file.length();
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
//                  sock.setSoTimeout(5000);
            while (file_length >= DATASIZE) {
                try {
                    bis.read(mybytearray, 0, mybytearray.length);
                    os.write(mybytearray, 0, mybytearray.length);
                    os.flush();
                    file_length -= DATASIZE;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (file_length > 0) {
                bis.read(mybytearray, 0, file_length);
                os.write(mybytearray, 0, file_length);
                os.flush();
//                String ack = recvMsg(is);
//                System.out.println("--" + ack + "--");
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

    public static void sendQuery(Socket sock, String query) throws Exception {
        OutputStream os = sock.getOutputStream();
        sendMsg(query, os);
    }
    public static void create_necessary_dirs(){
        File newFolder = new File("downloads");
        newFolder.mkdir();

    }
    public static void main(String args[]) {
        Socket sock = null;
        String cmd = "";
        String user = "";


        create_necessary_dirs();



        while (!cmd.equals("quit")) {
            print("Enter command : ");
            Scanner scanner = new Scanner(System.in);
            String tokens[] = scanner.nextLine().split(" ");
            cmd = tokens[0];
            if (!cmd.equals("create_user") && user.equals("")) {
                print("please create user account first");
                continue;
            }
            if (cmd.equals("upload")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "upload");
                    sendData(tokens[1], sock, user);
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    print(e.getMessage());
                }
            } else if (cmd.equals("create_user")) {
                try {
                    user = tokens[1];
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
//                    sendQuery(sock, "create_user " + tokens[1]);
                    sendMsg("create_user " + tokens[1],sock.getOutputStream());

                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("create_folder")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "create_folder " + user + "/" + tokens[1]);
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("move_file")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "move_file " + user + "/" + tokens[1] + " " + user + "/" + tokens[2]);
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("ls")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "ls " + user + " " + tokens[1]);
                    String result = recvMsg(sock.getInputStream());
                    String R[] = result.split(";;;");
                    print("\n\nList of files\n");

                    for(String r : R){
                        String f_name = r.split("-")[0];
                        String dir_or_not = r.split("-")[1];

                        if(dir_or_not.equals("D")){
                            System.out.println(ANSI_BLUE + f_name + ANSI_RESET);

                        }else{
                            System.out.println(ANSI_WHITE + f_name + ANSI_RESET);

                        }
                    }
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("create_group")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "create_group" + " " + tokens[1]);

                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("list_groups")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "list_groups");
                    InputStream is = sock.getInputStream();
                    String result = recvMsg(is);
                    print("\nGroups\n");
                    String res[] = result.split(" ");
                    for (String r : res) {
                        print(r);
                    }
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("join_group")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "join_group" + " " + tokens[1] + " " + user);
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("leave_group")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "leave_group" + " " + tokens[1] + " " + user);
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("list_detail")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "list_detail " + tokens[1]);
                    InputStream is = sock.getInputStream();
                    String result = recvMsg(is);
                    print("\nDetails for Group : " + tokens[1] + "\n");
                    String res[] = result.split(";");
                    for (String r : res) {
                        String[] all_paths = r.split(" ");
                        int count=0;
                        for(String a : all_paths){
                            if(count==0){
                                count=1;
                                print("User name : "+a+"\nUplaoded Files : ");
                            }else{
                                print(a);
                            }
                        }
                    }
                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("share_msg")) {//share_msg token
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock,"share_msg "+tokens[1]);
                    Scanner scanner1 = new Scanner(System.in);
                    print("Please Enter Your Message : ");
                    String date = dtf.format(now);
                    String MSG = user+" [" +date.split(" ")[0]+"-"+date.split(" ")[1]+"]"+";;"+scanner1.nextLine();
                    sendMsg(MSG,sock.getOutputStream());
                    print("Message sent successfully to "+tokens[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cmd.equals("get_file")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);

                    sendQuery(sock,"get_file");
                    String[] filenamepath = tokens[1].split("/");
                    String filename = filenamepath[filenamepath.length-1];
                    String file_des_path  = "serverside/";
                    for(int i = 1; i < filenamepath.length-1 ; i++){
                        file_des_path+=filenamepath[i]+"/";
                    }
                    file_des_path+=filename;
                    recieveData(sock,"downloads/"+filename,file_des_path);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (cmd.equals("inbox")) {
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);
                    sendQuery(sock, "inbox "+tokens[1]);
                    InputStream is = sock.getInputStream();
                    String result = recvMsg(is);
                    String res[] = result.split(",;,");
                    for (String r : res) {
                        if(user.equals(r.split(";;")[0].split(" ")[0])){
                            System.out.println(ANSI_GREEN +"You "+ r.split(";;")[0].split(" ")[1]+" : ");

                        }else {
                            System.out.println(ANSI_WHITE + r.split(";;")[0] + " : ");
                        }
                        System.out.print(ANSI_BLUE +r.split(";;")[1]+"");
                        print(ANSI_RESET);
                    }

                    if (sock != null) sock.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(cmd.equals("upload_udp")){
                try {
                    sock = new Socket(SERVER_HOST, SERVER_PORT);

                    sendQuery(sock,"upload_udp");
                    String res = recvMsg(sock.getInputStream());
                    if(res.equals("ok")){
                        send_udp(sock,tokens[1],user);
                    }
                } catch (Exception e) {
                    print("here");
                    e.printStackTrace();
                }
            }
        }
        try {
            Scanner scanner = new Scanner(System.in);
            print("Please Enter Password to shut_down server");
            String password = scanner.nextLine();
            if(password.equals("danish")){
                sock = new Socket(SERVER_HOST, SERVER_PORT);
                sendQuery(sock, "quit");
                if (sock != null) sock.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void send_udp(Socket sock,String filepath,String user) {
        try {
            String message = "";
            File file = new File(filepath);
            if (!file.exists()) {
                System.out.println("File Does Not Exist");
            }
            String[] path_split = filepath.split("/");
            message = "" + file.length() + " " + user+" "+path_split[path_split.length-1];
            print("sharing : "+path_split[path_split.length-1]);
            sendMsg(message, sock.getOutputStream());

            FileInputStream fis = null;
            BufferedInputStream bis = null;
            if (!file.exists()) {
                System.out.println("File Does Not Exist");
            }
            int DATASIZE = 50000;

            byte[] mybytearray = new byte[DATASIZE];
            int file_length = (int) file.length();
            sendMsg(""+file_length,sock.getOutputStream());
            String res = recvMsg(sock.getInputStream());
            if(res.equals("ok")){
                print("Server ready to get data");
            }
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
//                  sock.setSoTimeout(5000);
            DatagramSocket dsoc = new DatagramSocket(4000);
            byte[] b = new byte[3];
            while (file_length >= DATASIZE) {
                try {
                    bis.read(mybytearray, 0, mybytearray.length);
                    dsoc.send(new DatagramPacket(mybytearray, mybytearray.length, InetAddress.getLocalHost(), 2000));
                    DatagramPacket dp=new DatagramPacket(b,b.length);
                    dsoc.receive(dp);
                    file_length -= DATASIZE;
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                Thread.sleep(10);
            }
            if (file_length > 0) {
                bis.read(mybytearray, 0, file_length);
                dsoc.send(new DatagramPacket(mybytearray, file_length, InetAddress.getLocalHost(), 2000));

            }
            print(recvMsg(sock.getInputStream()));

            dsoc.close();
            if (bis != null) bis.close();
            if (fis != null) fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}