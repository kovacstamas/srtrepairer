package com.github.kovacstamas.srtrepairer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SrtRepairer {
	private File dir;
	
	public SrtRepairer(File dir) {
		this.dir = dir;
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("You have to use at least one parameter.");
		} else {
		    try {
		    	File dir = new File(args[0]);
		    	if (dir.isDirectory()) {
			    	SrtRepairer inst = new SrtRepairer(dir);
			    	inst.start();	
		    	} else {
		    		System.out.println("The parameter must be the directory containing the mkv files.");
		    	}
		    }catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	}
	
	private void start() throws Exception {
		System.out.println("Starting to process " + dir.getAbsolutePath());
		createSrts(dir);
		System.out.println("Finished with every mkv file.");
		processSrts(dir);
		System.out.println("SrtRepairer finished");
	}
	
	private void createSrts(File dir) throws Exception {
		for (final File file : dir.listFiles()) {
			String fileName = file.getAbsolutePath();
			if (file.isDirectory()) {
				System.out.println("File " + file  + " is a directory, Recursion!.");
				SrtRepairer rec = new SrtRepairer(file);
				rec.start();
			} else if (fileName.endsWith("mkv")) {
				String[] infoCommand = {"mkvinfo",  fileName};
				Process p;
				p = Runtime.getRuntime().exec(infoCommand);
				p.waitFor();
				BufferedReader reader = 
                        new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				int trackNo = getTrackNumberFromOutput(reader);
				System.out.println(file.getName() + " subtitle track number: " + trackNo);
				
				String srtName = fileName.substring(0, fileName.length()-4) + ".srt";
				File srtFile = new File(srtName);
				if (srtFile.exists()) {
					srtFile.delete();
					System.out.println(srtName + " deleted.");
				}
				if (trackNo > -1) {
					String[] extractCommand = {"mkvextract", "tracks", fileName, trackNo + ":" + srtName};
					p = Runtime.getRuntime().exec(extractCommand);
					p.waitFor();
					System.out.println(srtName + " has been created.");
				} else {
					System.out.println("No subtitle track found in the mkv file");;
				}

			}
		}
	}
	
	private void processSrts(File dir) throws IOException{
		for (final File file : dir.listFiles()) {
			if (!file.isDirectory() && file.getName().endsWith("srt")) {
				processFile(file);	
			}
		}
	}
	
	private int getTrackNumberFromOutput(BufferedReader reader) throws IOException{
		int trackNumber = -1;
		int currTrackNumber = -1; 
		String line = "";
		while ((line = reader.readLine())!= null) {
			if (line.matches(".*Track number: [0-9].*")) {
				currTrackNumber++;
			}
			if (line.matches(".*Track type: subtitles.*")) {
				trackNumber = currTrackNumber;
			}
		}
		return trackNumber;
	}

	private void processFile (File f) throws IOException{
		List<String> lines = new ArrayList<String>();
		List<String> currBlock = new ArrayList<String>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new  FileReader(f);
			br = new BufferedReader(fr);
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
		} finally {
			br.close();
			fr.close();
		}

		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			for(String s : lines)
				bw.write(s);
			bw.flush();
			System.out.println(f + " conversion is done.");
		} finally {
			bw.close();
			fw.close();
		}
	}
}
