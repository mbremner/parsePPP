package parsePPP;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.LinkedList;
import java.io.File;
import java.lang.Math;
import java.nio.ByteBuffer;

public class ParsePPP {

	Scanner scanner;
	LinkedList<Observation> obsList;
	LinkedList<String> header;
	Long lineNum = (long)1;
	int headerlines;
	int timezone;
	LinkedList<Integer> flagEvents; 
	String outpath;
	int output;
	int count;
	File csv;

	public static void main(String[] args) {

		//if (checkUsage(args)){
		 String current = null;
	
	        System.out.println("Current dir:"+current);
			
			ParsePPP parser = new ParsePPP(args);

			System.out.println("Parsing File");
			for(int i = 0 ; i < parser.headerlines ; i++){
				parser.header.add(parser.scanner.nextLine());
			}
			//System.out.println(parser.scanner.hasNextLine());
			while(parser.scanner.hasNextLine()){
				parser.lineNum++;

				try{
					parser.nextLine();
				}catch( IOException e){
					System.out.println("IOException");

				}catch(ParseException e){
					System.out.println("line:" + parser.lineNum  + "ParseException");
				}
			}

			if(parser.output == 1){
				parser.printTideFile();
			}
			if(parser.output == 2){
				try {
					parser.printNavFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		}
	//}

	public ParsePPP(String[] args){

		headerlines = 1;
		timezone = 0;
		count = 10;
		//csv = new File(args[args.length-1]);
		csv = new File("140611.csv");
		outpath = (csv.getParent() + "/" );
		outpath = "";
		output = 2;
		for (int i = 0 ; i < args.length ; i++){
			if (args[i].contentEquals("-headerlines") ){
				headerlines = Integer.parseInt(args[i+1]);
			}
			if (args[i].contentEquals("-decimate") ){
				count = Integer.parseInt(args[i+1]);
			}
			if (args[i].contentEquals("-outpath") ){
				outpath = (args[i+1]);
			}
			if (args[i].contentEquals("-tide") ){
				output = 1;
			}
			if (args[i].contentEquals("-nav") ){
				output = 2;
			}
		

		}


		//System.out.println(outpath);
		
		try {
			scanner = new Scanner(csv).useDelimiter(",");
		} catch (FileNotFoundException e) {
			System.out.println("404 File Not Found");
			//e.printStackTrace();
			System.exit(0);
		}
		header = new LinkedList<String>();
		obsList = new LinkedList<Observation>();
		flagEvents = new LinkedList<Integer>();
	}

	public boolean printTideFile(){
		Observation obs;
		PrintWriter printy;
		String outFileName;
		SimpleDateFormat form = new SimpleDateFormat("yyMMdd");
		outFileName = (form.format(obsList.getFirst().getDate()) + ".ascii");
		System.out.println(outpath + "/" + outFileName);
		//	System.out.println(outpath + "/" + outFileName);
		File outFile = new File(outpath + outFileName);
		
		System.out.println("Creating File: " + outFileName);
		if (outFile.exists()){
			System.out.println("File: " + outFileName + " Exists, Removing");
			outFile.delete();
		}
		try {
			outFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		try {
			printy = new PrintWriter(outFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		System.out.println("Writing File: " + outFileName);
		for (int i = 0 ; i < obsList.size() ; i = i + count){
			obs = obsList.get(i);
			if (!obs.isFlagged()){
				printy.println(obs.outputTide());
			}
		}
		printy.close();
		return true;
	}

	public boolean printNavFile() throws IOException{
		Observation obs;
		String outFileName;
		SimpleDateFormat form = new SimpleDateFormat("yyMMdd");
		outFileName = (form.format(obsList.getFirst().getDate()) + ".nav");
		File outFile = new File  (outpath + outFileName );
		//outFile.setExecutable(true);
		//outFile.setWritable(true);
		//System.out.println(outFile.getAbsolutePath());
		ByteBuffer bytes;
		FileOutputStream printy = null;
		printy = new FileOutputStream(outFile);
		System.out.println("Creating File: " + outFileName);
		if (outFile.exists()){
			System.out.println("File: " + outFileName + " Exists, Removing");
			outFile.delete();
		}
		outFile.createNewFile();
		System.out.println("Writing File: " + outFileName);
		for (int i = 0 ; i < obsList.size() ; i = i + count){
			obs = obsList.get(i);
			if (!obs.isFlagged()){
				bytes = obs.outputNav();				
					printy.write(bytes.array());
					//System.out.println(bytes.array());
					printy.flush();
					//System.out.println(obs.swap(bytes.getDouble(0)));
						}
			}
			printy.close();
		return true;
	}
	
	public void nextLine() throws IOException , IndexOutOfBoundsException, ParseException{
		Units units = new Units(1,1);	
		Double latitude = units.dd2DMS(scanner.nextDouble());
		Double longitude = units.dd2DMS(scanner.nextDouble());
		Double ellipHeight = scanner.nextDouble();
		int[] time = units.dd2HMS(scanner.nextDouble() - timezone) ; 
		Integer day = scanner.nextInt();
		Integer year = scanner.nextInt();
		Double orthoHeight = scanner.nextDouble() - 2.131;
		String ns = scanner.nextLine();

		String formattedDate = ("GMT+00:00 " + year.toString() + " " + day.toString() + " " + time[0] + " " + time[1] + " " + time[2] );
		SimpleDateFormat form = new SimpleDateFormat("zzzzzzzzz yyyy DDD HH mm ss");

		Date date = form.parse(formattedDate);

		Observation obs = new Observation(latitude, longitude, ellipHeight, orthoHeight, date);
		obsList.add(obs); 
	}


	class Observation {

		private Double latitude;
		private Double longitude;
		private Double ellipHeight;
		private Double orthHeight;
		private Date date;
		private Integer flagged;

		public Observation(Double latitude, Double longitude, Double ellipHeight,
				Double orthHeight, Date date) {

			this.latitude = latitude;
			this.longitude = longitude;
			this.ellipHeight = ellipHeight;
			this.orthHeight = orthHeight;
			this.date = date;
			this.flagged = 0;
		}

		public Double getEllipHeight() {
			return ellipHeight;
		}

		public Double getOrthHeight() {
			return orthHeight;
		}

		public Date getDate() {
			return date;
		}

		public String toString(){
			SimpleDateFormat out = new SimpleDateFormat("yyyy MM dd HH.mmss");
			return (latitude.toString() + " " +  longitude.toString() + " " + ellipHeight.toString() + " " + orthHeight.toString() + " " + out.format(date)  );
		}

		public boolean isFlagged(){
			if (flagged>0){
				return true;
			}else return false;
		}

		public void flag(int flagValue){
			flagged = flagValue;
		}
		
		public Integer getTime(){
			return (int)(getDate().getTime() / 1000);
		}

		public String outputTide(){
			return ((date.getTime() / 1000) + " " + orthHeight);
		}
		
		
		
		public ByteBuffer outputNav(){
			
			SimpleDateFormat yearform = new SimpleDateFormat("yyyy");
			SimpleDateFormat dayform = new SimpleDateFormat("DDD");
			SimpleDateFormat hourform = new SimpleDateFormat("HH");
			SimpleDateFormat minuteform = new SimpleDateFormat("mm");
			
			short year = Short.parseShort(yearform.format(getDate()));
			short day = Short.parseShort(dayform.format(getDate()));
			short hour = Short.parseShort(hourform.format(getDate()));
			short minute = Short.parseShort(minuteform.format(getDate()));
		
			ByteBuffer bytes = ByteBuffer.allocate(48);
		
			
				bytes.putDouble(swap(latitude));
				bytes.putDouble(swap(longitude));
				bytes.putInt(swap(getTime()));
				bytes.putShort((short)swap(0));
				bytes.putDouble(swap(0));
				bytes.putDouble(swap(0));
				bytes.putShort((short)swap(0));
				bytes.putShort((short)swap(year));
				bytes.putShort((short)swap(day));
				bytes.putShort((short)swap(hour));
				bytes.putShort((short)swap(minute));
		
			return bytes;
		}
		
		//the Next four methods are from a utility I found online, I have copied them into this code for ease of posting
		//http://www.java2s.com/Code/Java/Language-Basics/Utilityforbyteswappingofalljavadatatypes.htm
		
		/*
		 * (C) 2004 - Geotechnical Software Services
		 * 
		 * This code is free software; you can redistribute it and/or
		 * modify it under the terms of the GNU Lesser General Public 
		 * License as published by the Free Software Foundation; either 
		 * version 2.1 of the License, or (at your option) any later version.
		 *
		 * This code is distributed in the hope that it will be useful,
		 * but WITHOUT ANY WARRANTY; without even the implied warranty of
		 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
		 * GNU Lesser General Public License for more details.
		 *
		 * You should have received a copy of the GNU Lesser General Public 
		 * License along with this program; if not, write to the Free 
		 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
		 * MA  02111-1307, USA.
		 */
		//package no.geosoft.cc.util;
		
		/**
		   * Byte swap a single short value.
		   * 
		   * @param value  Value to byte swap.
		   * @return       Byte swapped representation.
		   */
		  public short swap (short value)
		  {
		    int b1 = value & 0xff;
		    int b2 = (value >> 8) & 0xff;

		    return (short) (b1 << 8 | b2 << 0);
		  }

		  

		  /**
		   * Byte swap a single int value.
		   * 
		   * @param value  Value to byte swap.
		   * @return       Byte swapped representation.
		   */
		  public int swap (int value)
		  {
		    int b1 = (value >>  0) & 0xff;
		    int b2 = (value >>  8) & 0xff;
		    int b3 = (value >> 16) & 0xff;
		    int b4 = (value >> 24) & 0xff;

		    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
		  }

		  /**
		   * Byte swap a single double value.
		   * 
		   * @param value  Value to byte swap.
		   * @return       Byte swapped representation.
		   */
		  public double swap (double value)
		  {
		    long longValue = Double.doubleToLongBits (value);
		    longValue = swap(longValue);
		    return Double.longBitsToDouble (longValue);
		  }
		  
		  /**
		   * Byte swap a single long value.
		   * 
		   * @param value  Value to byte swap.
		   * @return       Byte swapped representation.
		   */
		  public  long swap (long value)
		  {
		    long b1 = (value >>  0) & 0xff;
		    long b2 = (value >>  8) & 0xff;
		    long b3 = (value >> 16) & 0xff;
		    long b4 = (value >> 24) & 0xff;
		    long b5 = (value >> 32) & 0xff;
		    long b6 = (value >> 40) & 0xff;
		    long b7 = (value >> 48) & 0xff;
		    long b8 = (value >> 56) & 0xff;

		    return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
		           b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
		  }
	}


	class Units {

		private Integer linearUnit; // 1 = meters , 2 = international feet , 3 = us feet
		private Integer angleUnit; // 1 = ddd.mmss , 2 = ddd.dddd , 3 = rad , 4 = gons
		private Double unitFactor;

		public Units( Integer linearUnitIn , Integer angleUnitIn){
			linearUnit = linearUnitIn;
			angleUnit = angleUnitIn;
			switch (linearUnit){
			case 1:
				unitFactor = 1.0;
				break;
			case 2:
				unitFactor = 0.3048;
				break;
			case 3:
				unitFactor = (1200.0 / 3937.0);
				break;
			default:
			}
		}

		public Double getMean(LinkedList<Observation> sample){
			double sum = 0;
			int used = 0;
			for( int p = 0 ; p < sample.size() ; p++){
				if(!sample.get(p).isFlagged() ){
					sum = sum + sample.get(p).getOrthHeight();
					used++;
				}
			}
			return sum / used;
		}
	
		public Double getStdDev(LinkedList<Observation> sample , Double mean){
			//System.out.println("Size: " + sample.size() );
			double sumResiduals = 0;
			int used = 0;
			for( int p = 0 ; p < sample.size() ; p++){
				sumResiduals = sumResiduals + Math.pow((sample.get(p).getOrthHeight()- mean ),2);
				used++;
			}
			double stdDev = Math.sqrt(sumResiduals/(used-1));
			//System.out.println("StdDev: " + stdDev);
			return stdDev;
		}
		
		public Double dd2DMS(Double ddIn){

			Double dd;
			Double ddOut;		
			Integer deg; 
			Integer min; 
			Double sec;

			if(ddIn<0){
				dd = ddIn * -1;
			}else{
				dd = ddIn; 
			}

			deg = ((int)(double)dd);	
			min = (int)(mod(dd,1)*60.0);
			sec = ((mod((dd),1)*60.0) - min)*60;
			if (sec >= 59.9999999){
				sec = sec - 59.9999999;
				min = min + 1;
			}
			if (min >= 59.99999){
				min = min - 60;
				deg = deg + 1;
			}
			if(ddIn<0){
				ddOut = (deg + (min / 100.0) + (sec / 10000)) * -1;
			}else{
				ddOut =  (deg + (min / 100.0) + (sec / 10000)); 
			}		
			return ddOut;
		}

		public int[] dd2HMS(Double ddIn){

			Double dd;
			int[] ddOut = new int[3];		
			Integer deg; 
			Integer min; 
			Double sec;

			if(ddIn<0){
				dd = ddIn * -1;
			}else{
				dd = ddIn; 
			}

			deg = ((int)(double)dd);	
			min = (int)(mod(dd,1)*60.0);
			sec = ((mod((dd),1)*60.0) - min)*60;
			if (sec >= 59.9999999){
				sec = sec - 59.9999999;
				min = min + 1;
			}
			if (min >= 59.99999){
				min = min - 60;
				deg = deg + 1;
			}

			int intSec = (int)Math.round(sec);


			if(ddIn<0){
				ddOut[0] = (deg * -1); 
			}else{
				ddOut[0] =  deg ;
			}		
			ddOut[1] = min;
			ddOut[2] = intSec; 


			return ddOut;
		}

		public Double mod(Double arg1 , Double arg2){
			Integer whole = (int)Math.floor(arg1/arg2); 
			return (arg1 - (whole * arg2));
		}

		public Double mod(Double arg1 , Integer arg2){
			Integer whole = (int)Math.floor(arg1/arg2); 
			return (arg1 - (whole * arg2));
		}
		public Integer mod(Integer arg1 , Integer arg2){

			Integer whole = (int)Math.floor((double)arg1/arg2); 
			return (arg1 - (whole * arg2));
		}

	}

}
