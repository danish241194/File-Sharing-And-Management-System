# File-Sharing-And-Management-System
Following features added
1- ​ Account creation
cmd: create_user `username`
2- ​ File management 
2.1: ​ upload files to server
cmd: upload `filename`
cmd : upload_udp `filename`

2.2: ​ create folder on server
cmd: create_folder `foldername` 
2.3: ​ move files to/among folder
cmd: move_file `source_path` `dest_path`

3- ​ Group management 
3.1 ​ Create group
cmd: create_group `groupname`
3.2 ​ List groups
cmd: list_groups
3.3 ​ Join/leave group
cmd: join_group `groupname`
cmd: leave_group `groupname`

3.3 ​ List users and user files with path in a group
cmd: list_detail `groupname`
3.4 ​ Share message in group
cmd: share_msg `message text` 
3.5 Inbox Group Name
cd: inbox g_name
3.5 ​ Download file from any user in group
cmd: get_file `groupname/username/file_path`

4.1 list all files in your directory on server
cmd : ls . for main path
cmd : ls dirname1
