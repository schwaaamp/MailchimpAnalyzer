package com.econome.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.econome.domain.Subscriber;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ReportGenerator {

	static final String rootFolder = "/Users/schwaaamp/Documents/Econome/Econome_Mailchimp_export_102921/46727/";
	static final File membersFolder = new File(rootFolder.concat("lists/members/"));
	static final File openedEmailsFolder = new File(rootFolder.concat("aggregate_activity/opened/"));
	static final File unopenedEmailsFolder = new File(rootFolder.concat("aggregate_activity/not_opened/"));
	static final File openedTimestampFolder = new File(rootFolder.concat("granular_activity/opens/"));
	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public enum OpenStatus {

		OPENED, UNOPENED;

	}

	public static void main(String[] args) throws IOException, ParserConfigurationException {
		List<Subscriber> subscribers = retrieveActiveMembers("members_47741_econome.csv");
		countEmails(subscribers, OpenStatus.OPENED);
		countEmails(subscribers, OpenStatus.UNOPENED);
		setMostRecentOpenTimestamp(subscribers);
		writeFile(subscribers);

	}

	private static List<Subscriber> retrieveActiveMembers(String filename) throws FileNotFoundException, IOException {
		Multimap<String, String> memberMap = ArrayListMultimap.create();
		List<Subscriber> subscribers = new ArrayList<Subscriber>();
		Reader csvData = new FileReader(membersFolder.getAbsolutePath().concat("/").concat(filename));
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvData);
		for (CSVRecord record : records) {
			String emailAddress = record.get("Email Address");
			String joinedTs = record.get("OPTIN_TIME");
			memberMap.put(emailAddress, joinedTs);
			Subscriber s = new Subscriber(emailAddress, LocalDate.parse(joinedTs, formatter));
			subscribers.add(s);
		}
		return subscribers;
	}

	public static List<String> listFilesForFolder(final File folder) {
		List<String> filenames = new LinkedList<String>();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				if (fileEntry.getName().contains(".csv"))
					filenames.add(fileEntry.getName());
			}
		}

		return filenames;
	}

	private static void countEmails(List<Subscriber> subscribers, OpenStatus status)
			throws FileNotFoundException, IOException {

		List<String> filenames = null;
		String filenamePath = "";

		if (status.equals(OpenStatus.OPENED)) {
			filenames = listFilesForFolder(openedEmailsFolder);
			filenamePath = openedEmailsFolder.getAbsolutePath();
		} else if (status.equals(OpenStatus.UNOPENED)) {
			filenames = listFilesForFolder(unopenedEmailsFolder);
			filenamePath = unopenedEmailsFolder.getAbsolutePath();
		}

		for (String f : filenames) {
			Reader csvData = new FileReader(filenamePath.concat("/").concat(f));
			Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvData);
			for (CSVRecord r : records) {
				String emailAddress = r.get("Email Address");
				for (Subscriber s : subscribers) {
					// each time email address of member exists, add to total open count
					if (emailAddress.equals(s.getEmailAddress())) {
						if (status.equals(OpenStatus.OPENED)) {
							s.setOpenCount(s.getOpenCount() + 1);
						} else if (status.equals(OpenStatus.UNOPENED)) {
							s.setUnopenedCount(s.getUnopenedCount() + 1);
						}
					}
				}
			}
		}
	}

	private static void setMostRecentOpenTimestamp(List<Subscriber> subscribers)
			throws FileNotFoundException, IOException {
		List<String> openTsFilenames = listFilesForFolder(openedTimestampFolder);

		// forEach file, add to count of total opens for member
		for (String f : openTsFilenames) {
			Reader openTsCsvData = new FileReader(openedTimestampFolder.getAbsolutePath().concat("/").concat(f));
			Iterable<CSVRecord> opensTs = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(openTsCsvData);
			for (CSVRecord o : opensTs) {
				String emailAddress = o.get("Email");
				String tsString = o.get("Timestamp");
				LocalDate ts = LocalDate.parse(tsString, formatter);

				for (Subscriber s : subscribers) {
					// each time email address of member exists, add to total open count
					if (emailAddress.equals(s.getEmailAddress())) {
						if (s.getLatestOpenDate() == null || s.getLatestOpenDate().compareTo(ts) < 0) {
							s.setLatestOpenDate(ts);
						}
					}
				}
			}
		}
		//testing commit with personal access token
		System.out.println("Finished determining the most recent open email timestamp by subscriber");
	}

	private static void writeFile(List<Subscriber> subscribers) throws IOException {
		System.out.println("Writing the file...");
		FileWriter csvOutputFile = new FileWriter(rootFolder.concat("Subscriber_Engagement.csv"));
		String[] HEADERS = { "Email address", "Subscription Date", "Most Recent Open", "Unique Open Count",
				"Total Emails Delivered", "Open Rate" };

		try (CSVPrinter printer = new CSVPrinter(csvOutputFile, CSVFormat.DEFAULT.withHeader(HEADERS))) {
			for (Subscriber s : subscribers) {
				int totalEmails = s.getOpenCount() + s.getUnopenedCount();
				double openRate = (double) s.getOpenCount() / totalEmails;
				printer.printRecord(s.getEmailAddress(), s.getSubscriptionDate(), s.getLatestOpenDate(),
						s.getOpenCount(), totalEmails, String.valueOf(openRate));
			}
		}
		System.out.println("File has been written to " + rootFolder);
	}

}
