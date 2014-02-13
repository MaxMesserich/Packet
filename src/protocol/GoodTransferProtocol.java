package protocol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import client.AwesomePacket;
import client.INetworkLayerAPI;
import client.INetworkLayerAPI.TransmissionResult;
import client.Packet;
import client.PacketKind;

public class GoodTransferProtocol implements IDataTransferProtocol {
	private INetworkLayerAPI networkLayer;
	private int currentIndex = 0;
	private TransferMode transferMode;
	FileInputStream inputStream;
	FileOutputStream outputStream;
	private boolean init, ackReceived;
	AwesomePacket[] transmit;

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

	private void initFile(FileInputStream input) {
		// input.
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
				byte[] file = new byte[21222];

				transmit = AwesomePacket.file2packets(500, file);
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
			// System.out.println("TICK SEND");

			if (init || ackReceived) {
				System.out.println("Sending packet " + currentIndex);
				init = false;
				return SendData();
			}
			ackReceived = ReceiveData();
			if (ackReceived) {
				currentIndex++;

			}
			return false;

		} else {
			// Receive mode
			// System.out.println("TICK RECEIVE");
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
			ackReceived = false;
			// Max packet size is 1024

			if (currentIndex < transmit.length) {
				// AwesomePacket aws = new AwesomePacket(PacketKind.DATA, 0, w);
				// Packet p = new Packet(w);
				System.out.println(transmit[currentIndex].getKind() + ", "
						+ transmit[currentIndex].getArg());
				if (networkLayer.Transmit(transmit[currentIndex]) == TransmissionResult.Failure) {
					System.out.println("Failure to transmit");
					return true;
					// Mark current packet as not transmitted.
				}
				return false;
			}

		} else {
			System.out.println("Sending ACK to Sender");
			networkLayer.Transmit(new AwesomePacket(PacketKind.ACK, 0, null));
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
		if (receivedPacket != null) {
			AwesomePacket packet = new AwesomePacket(receivedPacket.GetData());

			if (transferMode == TransferMode.Send) {
				// Create a new String to read the message;
				if (packet.getKind().equals(PacketKind.ACK)) {
					System.out.println("ACK from client");
					this.ackReceived = true;
				}
			} else {
				System.out.println("RECIEVED DATA");
				byte[] data = receivedPacket.GetData();

				System.out.println(packet.getKind() + " : " + packet.getArg());
				// If the data packet was empty, we are done
				if (data.length == 0) {
					try {
						// Close the file
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					// Signal that work is done
					return true;

				}
				SendData();
			}

		}
		return false;
	}
}
