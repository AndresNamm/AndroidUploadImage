Tutorials This Code Is Based On 

This is where I started
http://www.coderefer.com/android-upload-file-to-server/

But this does not have Image taking part, also it does not implement the network in 
AsyncTask, so Some modifications needed to be made. 


AsyncTask addition is based purely on this:
https://developer.android.com/training/basics/network-ops/connecting.html
I did not change the HttpUrlConnection related functions at all, only functions which from 
asyncctask refered to the parent functions. 


Android Image Taking 
Started with the general: 
https://developer.android.com/training/camera/photobasics.html , but this is only relevant 
until the part where you do file saving/retrieving as some other info is somewhat inaccurate.
Like the part, where you retrieve files based on absolute path. That will give you so 
much inaccurate headace
Things which helped me a lot in regards to the file storing/retrieving part
and overall enforced me to use clear good practices 

 
https://androidkennel.org/android-camera-access-tutorial/  
https://developer.android.com/reference/android/os/Environment.html#getExternalStoragePublicDirectory
Part where thee is the getExternalStoragePublicDirectory example. 
Just a lot of reading about: 
Uri-s, FileProvider, How Android directories are files. How Uris are used both inside
outside apps, because Android focuses it to be something similiar to http as I heard from
interente. 


Different methods to retrieve directories. 

new File(Enviroment.getExternalStorageDirectory() + CAMERA_DIR +albumName)
After Froyo
return new File(
		  Environment.getExternalStoragePublicDirectory(
		    Environment.DIRECTORY_PICTURES
		  ), 
		  albumName
		);
My school example 
new File(Enviroment.getExternalStorageDirectory.getPath)
From good example 
https://developer.android.com/reference/android/os/Environment.html#getExternalStoragePublicDirectory(java.lang.String)
File path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);


Also, on serverside. For example, if you use xampp on windows..
To make the current code work you have to have structure:
php_file.php  -- or watever the name was
uploads\ -- I think this was the name of the folder


