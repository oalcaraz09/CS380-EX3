

/*  Oscar Alcaraz
	CS 380 Networks
	Exercise 3
*/

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;


public class Ex3Client {
	
	
	private static ArrayList<Integer> bytesIn = new ArrayList<>();
	private static ArrayList<Integer> bufferBytes = new ArrayList<>();
	
	
	public static void main(String[] args) {
		
		try {
			
			Socket socket = new Socket("18.221.102.182",38103);

			System.out.println("\nConnected to Server!\n");

			InputStream inStream = socket.getInputStream();

			// Determine bytes read in
			int count = inStream.read();
			
			System.out.println("Reading "+count+" bytes:\n");

			//Read the Bytes
			int columnSize = 0; 
			System.out.print("   ");
			
			while(true) {
				
				if (count == 0) { 
					
					break;
				}

				int bytes = inStream.read();
				System.out.print(formatBytes(bytes));
				
				bytesIn.add(bytes); 
				
				columnSize++;

				//Making sure the size of the bytes 
				//is exact to that of the prompt
				if (columnSize % 10 == 0){
					
					System.out.print("\n   ");
					
				}

				count--;
			}
			
			System.out.println();

			convertTo16Bits(); 

			
			byte[] chkSum = new byte[2]; 
			short sum = (short) checkSum();

			System.out.println("\nChecksum Calculated: 0x"+Integer.toHexString(sum & 0xFFFF).toUpperCase());

			chkSum[0] = (byte) ((sum >> 8) & 0xFF);
			chkSum[1] = (byte)(sum & 0xFF);

			PrintStream outStream = new PrintStream(socket.getOutputStream(),  true); 
			outStream.write(chkSum, 0, chkSum.length); 

			byte response = (byte) inStream.read(); 
			
			if (response == 1) 
				
				System.out.println("\nResponse Good.");
			
			else
				
				System.out.println("\nResponse Bad.");
			
			//Close Socket and Disconnect from Server.
            socket.close();
            System.out.println("\nDisconnected from Server!");


		} catch (Exception e) { e.printStackTrace();} 
		
	} 
	
	//Calculate the Checksum
	public static long checkSum() {
		
		int bytesLength = bufferBytes.size();
		int i = 0;
		long sum = 0;

		while (bytesLength > 0) {
			
			sum += bufferBytes.get(i);
			
			if ((sum & 0xFFFF0000) > 0) {
				
				sum = sum & 0xFFFF;
				sum++;
			} 

			i++;
			bytesLength--;
			
		}
		
		sum = ~sum;
		sum = sum & 0xFFFF;
		
		return sum;
	}

	
	public static void convertTo16Bits() {
		
		int bytesLength = bytesIn.size();
		int i = 0;

		while (bytesLength > 1){
			
			int bytesOne = bytesIn.get(i);
			
			//Shift bits to the left
			bytesOne = bytesOne << 8;
			
			int bytesTwo = bytesIn.get(i + 1);
			int mergedBytes = bytesOne | bytesTwo;

			bufferBytes.add(mergedBytes);
			
			i +=2;
			bytesLength -=2;
			
		}

		//put the last bit if odd
		if (bytesLength > 0) {
			
			int bytesOdd = bytesIn.get(bytesIn.size()-1);
			
			//Shift the bits to the left
			bytesOdd = bytesOdd << 8;
			
			bufferBytes.add(bytesOdd);
			
		}
	}


	// Pad the bytes to match the format of the prompt
	public static String formatBytes(int bits) {
		
		String fragment = Integer.toHexString(bits & 0xFF).toUpperCase();
		int size = fragment.length(); 
		
		if (size == 1) 
			
			return "0" + fragment;
		
		return fragment;
	}

}