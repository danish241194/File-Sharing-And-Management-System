import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
class MyServer{
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static int MYPORT = 2002;
	public static int DATASIZE = 1024;
	public static void print(String s){
            System.out.println(s);
     }
	public static String recieveMessage(InputStream is) throws Exception{
      	BufferedReader socketInput = new BufferedReader(new InputStreamReader(is));
      	String message = socketInput.readLine();
      	return message;
	}
	public static void sendMessage(String message,OutputStream os){
            PrintWriter socketOutput = new PrintWriter(new OutputStreamWriter(os));
            socketOutput.println(message);
            socketOutput.flush();
    }
	public static void recieveData(Socket sock,String path){
    	FileOutputStream fos = null;
    	BufferedOutputStream bos = null;
    	String s ="newpd.pdf";

		try {
			System.out.println("Recieving Data");
      		InputStream is = sock.getInputStream();
      		OutputStream os = sock.getOutputStream();
      		String message = recieveMessage(is);
      		String final_path = path.concat(message.split(" ")[1]+"/"+s);

      		System.out.println("Rec Msg : "+message);
      		int filesize =(int)Long.parseLong(message.split(" ")[0]);
		    byte [] mybytearray  = new byte [DATASIZE];
      		fos = new FileOutputStream(final_path.toString());
      		bos = new BufferedOutputStream(fos);
      		
      		while(filesize>=DATASIZE){
	      		int bytesRead=-1;
				bytesRead = is.read(mybytearray,0,mybytearray.length);

	      		System.out.println(bytesRead);

	      		int current = bytesRead;

	      		bos.write(mybytearray, 0 , current);
	      		bos.flush();
	      		// sendMessage("Data sent",os);	
	      		filesize-=DATASIZE;
      		}
      		if(filesize>0){
      			int bytesRead=-1;
				bytesRead = is.read(mybytearray,0,mybytearray.length);

	      		System.out.println(bytesRead);

	      		int current = bytesRead;

	      		bos.write(mybytearray, 0 , current);
	      		bos.flush();
	      		// sendMessage("Data sent",os);	
      		}
      		
			bos.close();
			is.close();
            fos.close();
      	}
      	catch(Exception e){
      		e.printStackTrace();
      	}

	}
	public static void main(String args[]){
		Map< String,String> user;
		Map< String,Vector> groups;
		Map< String,String> group_message;

		try{

			user = new HashMap< String,String>();
			groups = new HashMap< String,Vector>();

			while(true){


				ServerSocket servsock = null;
		    	Socket sock = null;
		  		servsock = new ServerSocket(MYPORT);
		  		sock = servsock.accept();
		  		
		  		InputStream is = sock.getInputStream();
		  		String query = recieveMessage(is);
		  		print(query +" rec");

		  		if(query.split(" ")[0].equals("create_user")){
		  			File newFolder = new File("serverside/"+query.split(" ")[1]);
			        boolean created =  newFolder.mkdir();
					user.put(query.split(" ")[1], new String("user"));
			        if(created)
			            System.out.println("Folder was created !");
			        else
			            System.out.println("Unable to create folder");
		  		}
		  		else if(query.split(" ")[0].equals("upload"))
		  		{	
		  			recieveData(sock,"serverside/");
				}
		  		else if(query.split(" ")[0].equals("create_folder"))
		  		{
		  			File newFolder = new File("serverside/"+query.split(" ")[1]);
			        boolean created =  newFolder.mkdir();
			        if(created)
			            System.out.println("Folder was created !");
			        else
			            System.out.println("Unable to create folder");
		  		}
		  		else if(query.split(" ")[0].equals("move_file"))
		  		{
		  			String path1 = "serverside/"+query.split(" ")[1];
		  			String path2 = "serverside/"+query.split(" ")[2];		  			
        			Files.move(Paths.get(path1), Paths.get(path2), StandardCopyOption.REPLACE_EXISTING);
		  		}
		  		else if(query.split(" ")[0].equals("ls")){
		  			File folder =	new File("serverside/"+query.split(" ")[1]+"/"+query.split(" ")[2]);
					print("serverside/"+query.split(" ")[1]+"/"+query.split(" ")[2]);
		  			
					File[] files = folder.listFiles();
 					print("\n\nList of files\n\n");
        			for (File file : files)
        			{
        				boolean isDirectory = file.isDirectory();
						if(isDirectory)
							System.out.println(ANSI_BLUE + file.getName() + ANSI_RESET);
						else
							System.out.println(ANSI_WHITE + file.getName() + ANSI_RESET);
        			}
		  		}
		  		else if(query.split(" ")[0].equals("create_group")){
		  			groups.put(query.split(" ")[1],new Vector());
		  		}
		  		else if(query.split(" ")[0].equals("list_groups")){
		  			Set< Map.Entry< String,Vector> > st = groups.entrySet();    
  					String result = "";

       				for (Map.Entry< String,Vector> me:st) 
       				{ 
				        result += me.getKey()+" ";
				    } 
				    print(result);
				    OutputStream os = sock.getOutputStream();
            		sendMessage(result,os);
		  		}

		  		else if(query.split(" ")[0].equals("join_group")){
		  			Vector myvec = groups.get(query.split(" ")[1]);
		  			String usernm = query.split(" ")[2];
		  			myvec.add(usernm);
		  			groups.put(query.split(" ")[1],myvec);
		  		}
		  		else if(query.split(" ")[0].equals("leave_group")){
		  			String grp_name = query.split(" ")[1];
		  			Vector myvec = groups.get(grp_name);
		  			String usernm = query.split(" ")[2];
		  			myvec.remove(usernm);
		  			if(myvec.size()==0){
		  				groups.remove(grp_name);
		  			}
		  			else{
		  				groups.put(grp_name,myvec);
		  			}
		  		}
		  		else if(query.split(" ")[0].equals("list_detail")){
		  			Vector vec = groups.get(query.split(" ")[1]);
				    Enumeration enu = vec.elements();
				    String res = "";
				    while (enu.hasMoreElements()) { 
            			res+=enu.nextElement(); 
        			} 
				    OutputStream os = sock.getOutputStream();
            		sendMessage(res,os);
		  		}





		  		sock.close();
		  		servsock.close();
		  	}
  		}
  		catch(Exception e){
  			e.printStackTrace();
  		}
	}
}