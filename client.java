import java.io.*;
import java.net.*;
import java.util.Scanner; 
class MyClient{
      public static int SERVER_PORT = 2002;
      public static String SERVER_HOST = "127.0.0.1";
      public static int DATASIZE = 1024;
      public static void print(String s){
            System.out.println(s);
      }
      public static void sendMessage(String message,OutputStream os){
            PrintWriter socketOutput = new PrintWriter(new OutputStreamWriter(os));
            socketOutput.println(message);
            socketOutput.flush();
      }
      public static String recieveMessage(InputStream is) throws Exception{
            BufferedReader socketInput = new BufferedReader(new InputStreamReader(is));
            String message = socketInput.readLine();
            return message;
      }
      public static void sendData(String filepath,Socket sock,String user){
            try{
                  FileInputStream fis = null;
                  BufferedInputStream bis = null;
                  OutputStream os = null;
                  os = sock.getOutputStream();
                  InputStream is = sock.getInputStream();
                  String message = "";
                  File file = new File(filepath);
                  if(!file.exists()){
                       System.out.println("File Does Not Exist");
                  }
                  message = ""+file.length()+" "+user;
                  sendMessage(message,os);
                  byte [] mybytearray  = new byte [DATASIZE];
                  int file_length = (int)file.length();
                  fis = new FileInputStream(file);
                  bis = new BufferedInputStream(fis);
                  while(file_length >= DATASIZE){
                        bis.read(mybytearray,0,mybytearray.length);
                        os.write(mybytearray,0,mybytearray.length);
                        os.flush();
                        // String ack = recieveMessage(is);
                        // System.out.println("--"+ack+"--");
                        file_length-=DATASIZE;
                        // System.out.println("--"+file_length+"--");
                  }
                  if(file_length>0){
                        bis.read(mybytearray,0,file_length);
                        os.write(mybytearray,0,file_length);
                        os.flush();
                        // String ack = recieveMessage(is);
                        // System.out.println("--"+ack+"--");
                  }


                  if (bis != null) bis.close();
                  if (os != null) os.close();
                  if (fis != null) fis.close();
            }
            catch(Exception e){
                  e.printStackTrace();
            }
      }
      public static void sendQuery(Socket sock,String query) throws Exception{
            OutputStream os = sock.getOutputStream();
            sendMessage(query,os);
      }
	public static void main(String args[]){
    	      Socket sock = null;
            String cmd = "";
            String user="";
            while(!cmd.equals("quit")){

                  Scanner scanner = new Scanner(System.in);
                  String tokens[] = scanner.nextLine().split(" ");
                  cmd = tokens[0];
                  if(!cmd.equals("create_user") && user.equals("")){
                        print("please create user account first");
                        continue;
                  }

                  if(cmd.equals("upload")){
            		try {
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendData("clientside/pd.pdf",sock,user);
                        	if (sock!=null) sock.close();
                  	}
                  	catch(Exception e){
                  		e.printStackTrace();
                  	}
                  }



                  else if(cmd.equals("create_user")){
                        try {
                              user = tokens[1];
                              print("create user");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"create_user " + tokens[1]);
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }




                  else if(cmd.equals("create_folder")){
                        try {
                              print("create folder");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"create_folder "+user+"/" + tokens[1]);
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }
                  else if(cmd.equals("move_file")){
                        try {
                              print("move_file");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"move_file "+user+"/" + tokens[1]+" "+user+"/"+tokens[2]);
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }


                  else if(cmd.equals("ls")){
                        try {
                              print("ls");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"ls "+user+" "+tokens[1]);
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }


                  else if(cmd.equals("create_group")){
                        try {
                              print("create_group");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"create_group"+" "+tokens[1]);
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }
                  else if(cmd.equals("list_groups")){
                        try {
                              print("list_group");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"list_groups");
                              InputStream is = sock.getInputStream();
                              String result = recieveMessage(is);
                              print("\nGroups\n");
                              String res[] = result.split(" ");
                              for(String r : res){
                                    print(r);
                              }
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }
                  else if(cmd.equals("join_group")){
                        try {
                              print("join_group");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"join_group"+" "+tokens[1]+" "+user);
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }
                  else if(cmd.equals("leave_group")){
                        try {
                              print("leave_group");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"leave_group"+" "+tokens[1]+" "+user);
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }
                  else if(cmd.equals("list_detail")){
                        try {
                              print("list_detail");
                              sock = new Socket(SERVER_HOST, SERVER_PORT);
                              sendQuery(sock,"list_detail "+tokens[1]);
                              InputStream is = sock.getInputStream();
                              String result = recieveMessage(is);
                              print("\nDetails for Group : "+tokens[1]+"\n");
                              String res[] = result.split(" ");
                              for(String r : res){
                                    print(r);
                              }
                              if (sock!=null) sock.close();
                        }
                        catch(Exception e){
                              e.printStackTrace();
                        }
                  }
                  else if(cmd == "share_msg"){
                        
                  }
                  else if(cmd == "get_file"){

                  }
            }
            
	}
}