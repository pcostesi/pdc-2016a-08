package ar.edu.itba.protos.transport.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.inject.Singleton;

import ar.edu.itba.protos.transport.support.Attachment;

@Singleton
public class Metrics {
	
	private static ConcurrentMap<Attachment, AtomicLong> accesses = new ConcurrentHashMap<Attachment, AtomicLong>();
	private static ConcurrentMap<Attachment, AtomicLong> bytesTransfered = new ConcurrentHashMap<Attachment, AtomicLong>();
	
	public static void logAccess(Attachment attachment) {
		AtomicLong atomicAccesses = accesses.getOrDefault(attachment, new AtomicLong());
		atomicAccesses.incrementAndGet();
		accesses.putIfAbsent(attachment, atomicAccesses);
	}
	
	public static void logTransferedBytes(Attachment attachment, Long bytesNumber) {
		AtomicLong atomicTransferedBytes = bytesTransfered.getOrDefault(attachment, new AtomicLong());
		atomicTransferedBytes.addAndGet(bytesNumber);
		bytesTransfered.putIfAbsent(attachment, atomicTransferedBytes);
	}
	
	public Long accesses(Attachment attachment) {
		return accesses.getOrDefault(attachment, new AtomicLong()).longValue();
	}
	
	public Long bytesTransfered(Attachment attachment) {
		return bytesTransfered.getOrDefault(attachment, new AtomicLong()).longValue();
	}
	
}
