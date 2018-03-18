package com.github.kovacstamas.srtrepairer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SrtRepairer {
	public static List<String> lines = new ArrayList<String>();
	public static List<String> currBlock = new ArrayList<String>();
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("You have to use at least one parameter.");
		} else {
		    try {
		    	File f = new File(args[0]);
		        FileReader fr = new  FileReader(f);
		        BufferedReader br = new BufferedReader(fr);
		        String line;

		        while ((line = br.readLine()) != null) {
		        	if (line.matches(".*-->.*")) {
		        		String[] interval = line.split("-->");
		        		String start = interval[0].trim();
		        		String timestamp = currBlock.get(0).replaceAll("-->.*","--> " + start);
		        		currBlock.set(0, timestamp);
		        		lines.addAll(currBlock);
		        		currBlock = new ArrayList<String>();
		        		currBlock.add(line + "\n");
		        	} else {
		        		currBlock.add(line + "\n");
		        	}
		        }
		        lines.addAll(currBlock);
		        br.close();
		        fr.close();
		        
	            FileWriter fw = new FileWriter(f);
	            BufferedWriter bw = new BufferedWriter(fw);
	            for(String s : lines)
	                 bw.write(s);
	            bw.flush();
	            bw.close();
	            fw.close();
	            System.out.println(f + " conversion is done.");
		    }catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}

}
