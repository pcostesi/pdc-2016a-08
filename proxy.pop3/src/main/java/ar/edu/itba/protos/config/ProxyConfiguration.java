package ar.edu.itba.protos.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProxyConfiguration {
	private String listenAddr = "0.0.0.0";
	private String adminListenAddr = "127.0.0.1";
	private int listenPort = 1110;
	private int adminListenPort = 1666;
	
	
	public String getListenAddr() {
		return listenAddr;
	}
	
	@XmlElement
	public void setListenAddr(String listenAddr) {
		this.listenAddr = listenAddr;
	}
	
	public String getAdminListenAddr() {
		return adminListenAddr;
	}
	
	@XmlElement
	public void setAdminListenAddr(String adminListenAddr) {
		this.adminListenAddr = adminListenAddr;
	}
	
	public int getListenPort() {
		return listenPort;
	}
	
	@XmlElement
	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}
	
	public int getAdminListenPort() {
		return adminListenPort;
	}
	
	@XmlElement
	public void setAdminListenPort(int adminListenPort) {
		this.adminListenPort = adminListenPort;
	}

}
