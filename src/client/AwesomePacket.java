package client;

import java.util.Scanner;

import client.Packet;

public class AwesomePacket extends Packet {

	PacketKind kind;
	int arg;
	byte[] dataBytes;
	
	/**
	 * Use this to read a transmitted packet.
	 * @param fullData all bytes received
	 */
	public AwesomePacket(byte[] fullData) {
		super(fullData);
		for ()
		String fullDataString = new String(fullData);

			
		}
		if (scanner.hasNext()) {
			kind = PacketKind.packetKindFromString(scanner.next());
			
		}
		if (scanner.hasNext()) {
			arg = Integer.valueOf(scanner.next());
			
		}
		if (scanner.hasNext()) {
			dataBytes = scanner.next().getBytes();
			
		}
		
	}
	
	/**
	 * Use this to create a packet to be sent later.
	 * @param kind kind of packet
	 * @param arg an integer argument. Either the packet number if it is a
	 * data-packet, or the number of packets the message contains of if it is a start-packet. 
	 * @param data the actual data
	 */
	public AwesomePacket(PacketKind kind, int arg, byte[] data) {
		super(data);
	}
	
	public byte[] buildPacket() {
		return null;
	}
	
	
}
