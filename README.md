Android Smart Update Demo.
=============================================
Use java binary patcher--jbdiff[http://sourceforge.net/p/jbdiff/code/ci/master/tree/src/main/java/ie/wombat/jbdiff/](based on bspatch) for smart update.

Download progress is shown in notification.

#smart_update
	the android sample project.
#smart_update_server
	the server that smart_update project communicates with.

1. export android_sample's apk with version 1.0 as "SmartUpdate-1.0.apk".
2. change android_sample's version to 1.1, and add/change/remove some file, then export the apk as "SmartUpdate-1.1.apk".
3. run smart_update_server/test.Diff with three params oldPath, newPath, patchPath.
4. in step 3 you got a .patch file and 3 string(new file's sha1sum, patch file's sha1sum, patch file's size), place the .patch file in smart_update_server/src/main/webapp/, and modify smart_update_server/src/main/java/com.example.smartupdate.server.UpgradeServlet, replace the patch_sha1, sha1, size to the 3 string, and change the url's host to your own. Change the host in smart_update/src/com.example.smartupdate.upgrade.UpgradeManager#VERSION_URL as well.
5. open smart_update_server/jetty-run.bat to start the server, install SmartUpdate-1.0.apk to your device.
6. open SmartUpdate on your device, click "Check Upgrade" button to start upgrade.