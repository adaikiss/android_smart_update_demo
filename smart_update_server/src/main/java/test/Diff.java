package test;

import ie.wombat.jbdiff.JBDiff;

import java.io.File;

public class Diff {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		JBDiff.bsdiff(new File("D:/SmartUpdate-1.0.apk"), new File("D:/SmartUpdate-1.1.apk"), new File("D:/1.0-1.1.patch"));
	}

}
