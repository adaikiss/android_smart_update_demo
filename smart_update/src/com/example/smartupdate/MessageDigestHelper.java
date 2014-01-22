/**
 * 
 */
package com.example.smartupdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import android.util.Log;

/**
 * @author hlw
 * 
 */
public class MessageDigestHelper {
	public static boolean confirmSHA1Sum(File file, byte[] digestb) {
		FileInputStream in = null;
		try {
			MessageDigest digester = MessageDigest.getInstance("SHA1");
			byte[] bytes = new byte[8192];
			int byteCount;
			in = new FileInputStream(file);
			while ((byteCount = in.read(bytes)) > 0) {
				digester.update(bytes, 0, byteCount);
			}
			byte[] digest = digester.digest();
			return MessageDigest.isEqual(digest, digestb);
		} catch (Exception e) {
			Log.e("SHA1", file.getAbsolutePath() + " sha1sum failed!", e);
		} finally {
			if(null != in){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}
}
