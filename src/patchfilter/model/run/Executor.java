package patchfilter.model.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;

@Slf4j
public class Executor {
	private final static String __name__ = "@Executor ";

	public static List<String> execute(String[] command) {
		Process process = null;
		final List<String> message = new ArrayList<String>();
		try {
			ProcessBuilder builder = getProcessBuilder(command);
			builder.redirectErrorStream(true);
			process = builder.start();
			final InputStream inputStream = process.getInputStream();

			Thread processReader = new Thread() {
				public void run() {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					try {
						while ((line = reader.readLine()) != null) {
							message.add(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			processReader.start();
			try {
				processReader.join();
				process.waitFor();
			} catch (InterruptedException e) {
				return new LinkedList<>();
			}
		} catch (IOException e) {
		} finally {
			if (process != null) {
				process.destroy();
			}
			process = null;
		}
		return message;
	}

	private static ProcessBuilder getProcessBuilder(String[] command) {
		ProcessBuilder builder = new ProcessBuilder(command);
		Map<String, String> evn = builder.environment();
		evn.put("JAVA_HOME", Constant.JAVA_HOME);
		if (System.getProperty("os.name").startsWith("Windows")) {
			evn.put("PERL_HOME", Constant.PERL_HOME);
			evn.put("PATH", Constant.PERL_HOME + "/bin;" + Constant.JAVA_HOME + "/bin;" + evn.get("PATH"));
		} else {
			evn.put("PATH", Constant.JAVA_HOME + "/bin:" + evn.get("PATH"));
		}
		return builder;
	}

}
