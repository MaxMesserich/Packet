package client;

import java.util.Arrays;
import java.util.Scanner;

import client.Packet;

public class AwesomePacket extends Packet {

	/**
	 * The kind of packet.
	 * @see PacketKind
	 */
	PacketKind kind;
	
	/**
	 * An integer argument of the packet.
	 */
	int arg;
	
	/**
	 * The actual data to be send.
	 */
	byte[] dataBytes;
	
	/**
	 * Use this to create a packet to be send later.
	 * @param kind kind of packet
	 * @param arg an integer argument. Either the packet number if it is a
	 * data-packet, or the number of packets the message contains of if it is a start-packet. 
	 * @param data the actual data
	 */
	public AwesomePacket(PacketKind kind, int arg, byte[] data) {
		super((kind.toString() + " " + arg + " " + byteArrayToString(data)).getBytes());
		this.kind = kind;
		this.arg = arg;
		this.dataBytes = data;
	}
	
	/**
	 * Use this to read a transmitted packet.
	 * @param fullData all bytes received
	 */
	public AwesomePacket(byte[] fullData) {
		super(fullData);
		int index = 0;
		
		// extract packet kind
		String sKind = "";
		for (int p = 0; index < fullData.length; index++) {
			char c = (char) fullData[index];
			if (c == ' ') {
				index++;
				break;
			} else {
				sKind += c;
			}	
		}
		kind = PacketKind.packetKindFromString(sKind);
		
		// extract packet args
		String Sarg = "";
		for (int p = 0; index < fullData.length; index++) {
			char c = (char) fullData[index];
			if (c == ' ') {
				index++;
				break;
			} else {
				Sarg += c;
			}	
		}
		arg = Integer.valueOf(Sarg);
		
		// extract packet data
		dataBytes = Arrays.copyOfRange(fullData, index, fullData.length);		
	}
	
	private static String byteArrayToString(byte[] array) {
		String string = "";
		string += array[0];
		for (int i = 1; i < array.length; i++) {
			string += " " + array[i];
		}
		return string;
	}
	public static AwesomePacket[] file2packets(int packetSize,byte[] file){
		int fileSize = file.length;
		int packetCount = (fileSize/packetSize) +1;
		AwesomePacket[] a;
		return null;
	}
	/**
	 * Returns the kind of the packet.
	 */
	public PacketKind getKind() {
		return kind;
	}
	
	/**
	 * Returns the integer argument of the packet.
	 */
	public int getArg() {
		return arg;
	}
	
	/**
	 * Returns the actual data of the packet.
	 */
	public byte[] getDataBytes() {
		return dataBytes;
	}
		
}
