package client;

public enum PacketKind {

	START, DATA, STOP, ACK;
	
	public static final String STA = "sa";
	public static final String DAT = "da";
	public static final String STO = "so";
	public static final String AC = "ac";
	
	public String toString() {
		if (this.equals(START)) {
			return STA;
		} else if (this.equals(DATA)) {
			return DAT;
		} else if (this.equals(STOP)) {
			return STO;
		} else if (this.equals(ACK)) {
			return AC;
		}
		return null;
	}
	
	public static PacketKind packetKindFromString(String kind) {
		if (kind.equals(STA)) {
			return START;
		} else if (kind.equals(DAT)) {
			return DATA;
		} else if (kind.equals(STO)) {
			return STOP;
		} else if (kind.equals(AC)) {
			return ACK;
		}
		return null;
	}
	
	

}
