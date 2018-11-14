package de.allmaennitta.streams;

import de.allmaennitta.streams.model.Client;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.summingDouble;

public class SequentialStreamProcessing {

	public static void main(String[] args) {
		new SequentialStreamProcessing().start();
	}

	private void start() {
		List<String> ids = Arrays.asList(
				"C01", "C02", "C03", "C04", "C05", "C06", "C07", "C08", "C09", "C10", 
				"C11", "C12", "C13", "C14", "C15", "C16", "C17", "C18", "C19", "C20");
		
		long startTime = System.nanoTime();
		double totalPurchases = ids.stream()
			.map(id -> new Client(id, 18d))
			.collect(summingDouble(Client::getPurchases));
		
		long endTime = (System.nanoTime() - startTime) / 1_000_000;
		System.out.println("Sequential | Total time: " + endTime + " ms");
		System.out.println("Total purchases: " + totalPurchases);
	}
}
