package protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import protocol.IDataTransferProtocol.TransferMode;
import client.INetworkLayerAPI;
import client.Packet;
import client.INetworkLayerAPI.TransmissionResult;

public class GoodTransferProtocol implements IDataTransferProtocol {
	private INetworkLayerAPI networkLayer;
	private int bytesSent = 0;
	private TransferMode transferMode;
	FileInputStream inputStream;
	FileOutputStream outputStream;
	private boolean init;
	public GoodTransferProtocol() {
		System.out.println("DERP DERP");
		init = true;
	}

	@Override
	public void TimeoutElapsed(Object tag) {
	}

	@Override
	public void SetNetworkLayerAPI(INetworkLayerAPI networkLayer) {
		this.networkLayer = networkLayer;
	}

	@Override
	public void Initialize(TransferMode transferMode) {
		this.transferMode = transferMode;
		System.out.println("TransferMode init");
		// Send mode
		if (this.transferMode == TransferMode.Send) {
			System.out.println("Starting protocol as Sender");
			try {
				// Open the input file
				inputStream = new FileInputStream(Paths.get("")
						.toAbsolutePath() + "/tobesent.dat");
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("File not found");
			}

			// Receive mode
		} else {
			try {
				System.out.println("Starting protocol as reciever!");
				// Open the output file
				outputStream = new FileOutputStream(Paths.get("")
						.toAbsolutePath() + "/received.dat");
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("File could not be created");
			}
		}
	}

	@Override
	public boolean Tick() {
		if (this.transferMode == TransferMode.Send) {
			// Send mode
//			System.out.println("TICK SEND");
			if(!init){
				if(ReceiveData()){
					return true;
				}
			}
			init = false;
			return SendData();
		} else {
			// Receive mode
//			System.out.println("TICK RECEIVE");
			return ReceiveData();
		}
	}

	/**
	 * Handles sending of data from the input file
	 * 
	 * @return whether work has been completed
	 */
	private boolean SendData() {
		if (transferMode == TransferMode.Send) {
			System.out.println("SENDING");
			// Max packet size is 1024
			byte[] readData = new byte[1024];
			byte[] readData1 = new byte[1024];
			byte[] readData2 = new byte[1024];

			byte[][] data = { readData, readData1, readData2 };
			int currentIndex = 0;

			try {
				int fileSize = inputStream.read(data[currentIndex]);
				if (fileSize > 0) {
					System.out.println("Sending packet " + currentIndex + " of"
							+ data.length);
					if (networkLayer.Transmit(new Packet(readData)) == TransmissionResult.Failure) {
						System.out.println("Failure to transmit");
						// Mark current packet as not transmitted.
					} else {
						networkLayer.Transmit(new Packet(readData));
						networkLayer.Transmit(new Packet(readData));
						networkLayer.Transmit(new Packet(readData));
						networkLayer.Transmit(new Packet(readData));
						currentIndex++;
						if (currentIndex > data.length) {
							return true;
						}
					}

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		} else {
			System.out.println("Sending ACK to Sender");
			String ack = "ACK";
			byte[] ackBytes = ack.getBytes();
			networkLayer.Transmit(new Packet(ackBytes));
		}
		return true;
	}

	/**
	 * Handles receiving of data packets and writing data to the output file
	 * 
	 * @return Whether work has been completed
	 */
	private boolean ReceiveData() {
		Packet receivedPacket = networkLayer.Receive();
//		
		if (receivedPacket != null) {
			if (transferMode == TransferMode.Send) {
				// Create a new String to read the message;
				String message = "";
				byte[] packetBytes = receivedPacket.GetData();
				for (int i = 0; i < packetBytes.length; i++) {
					message += (char) (packetBytes[i]);
				}
				if (message.equals("ACK")) {
					
					System.out.println("ACK RECEIVED FROM CLIENT");
				}
			} else {
				System.out.println("RECIEVED DATA");
				byte[] data = receivedPacket.GetData();

				// If the data packet was empty, we are done
				if (data.length == 0) {
					try {
						// Close the file
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("SENNDING ACK");
					SendData();
					// Signal that work is done
					return true;
				}else{
					//Send ACK TO THE SERVER THAT A PACKET HAS BEEN RECEIVED
					SendData();
				}
			}
		}
		return false;
	}
}
