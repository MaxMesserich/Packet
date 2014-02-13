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
	AwesomePacket currentPacket;
	
	int sleepTimeTotal = 2000;
	int sleepTime = 5;
	int sleepCount = sleepTime / sleepTime;
	
	int currentPackageGot = 0;

	public GoodTransferProtocol() {
		System.out.println("DERP DERP");
		ackReceived = true;
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
			//System.out.println("TICK SEND");
			if (!SendData(ackReceived)) {
				return true;
			}			
			int counter = 0;
			while (!(ackReceived = ReceiveData())) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				counter++;
				if (counter >= sleepCount) {
					break;
				}				
			}
			return true;

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
	private boolean SendData(boolean resend) {
		if (transferMode == TransferMode.Send) {
			if (resend) {
				System.out.println("RE-SENDING");
				// We re-send the packet.
				if (networkLayer.Transmit(currentPacket) == TransmissionResult.Failure) {
					System.out.println("Failure transmitting");
					return true;
				}
			} else {
				currentIndex++;
				System.out.println("SENDING");
				byte[] dataToSend = new byte[AwesomePacket.howMuchLeft(1024, PacketKind.DATA, currentIndex)];
				try {
					int readSize = inputStream.read(dataToSend);		
					if (readSize >= 0) {
						// We read some bytes, send the packet
						currentPacket = new AwesomePacket(PacketKind.DATA, currentIndex, dataToSend);
						if (networkLayer.Transmit(currentPacket) == TransmissionResult.Failure) {
							System.out.println("Failure transmitting");
							return true;
						}
					} else {
						// readSize == -1 means End-Of-File
						try {
							// Send stop packet, to signal transmission end.
							currentPacket = new AwesomePacket(PacketKind.STOP, 0, null);
							networkLayer.Transmit(currentPacket);

							// Close the file
							inputStream.close();

						} catch (IOException e) {
							e.printStackTrace();
						}
						
						// Return true to signal work done
						return true;
					}
				} catch (IOException e) {
					// We encountered an error while reading the file. Stop work.
					System.out.println("Error reading the file: " + e.getMessage());
					return true;
				}
			}
		} else {
			System.out.println("Sending ACK to Sender");
			networkLayer.Transmit(new AwesomePacket(PacketKind.ACK, currentPackageGot, null));
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
			System.out.println(packet.getKind() + " : " + packet.getArg());
			if (transferMode == TransferMode.Send) {
				if (packet.getKind().equals(PacketKind.ACK)) {
					System.out.println("ACK RECEIVED FROM CLIENT");
				}
				return true;
			} else {	
				if (packet.getKind().equals(PacketKind.STOP)) {
					System.out.println("RECIEVED STOP COMMAND");
					// we are done!!
					try {
						// Close the file
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				} else {
					if (packet.getArg() == currentPackageGot) {
						System.out.println("RECIEVED DATA");
						currentPackageGot++;
						byte[] data = packet.getDataBytes();
						// Write the data to the output file
						try {
							outputStream.write(data, 0, data.length);
							outputStream.flush();
						} catch (IOException e) {
							System.out.println("Failure writing to file: " + e.getMessage());
							return true;
						}
					} else {
						System.out.println("RECIEVED ALREADY GOT DATA");
					}
					SendData(true);					
				}
			}
			return true;
		}
		return false;
	}
}
