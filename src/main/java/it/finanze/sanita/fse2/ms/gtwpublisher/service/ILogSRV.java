package it.finanze.sanita.fse2.ms.gtwpublisher.service;

import java.io.Serializable;
 

public interface ILogSRV extends Serializable {

	void sendLoggerStatus(String log);
}
