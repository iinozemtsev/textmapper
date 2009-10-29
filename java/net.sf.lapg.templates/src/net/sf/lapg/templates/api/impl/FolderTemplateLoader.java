package net.sf.lapg.templates.api.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.api.TemplatesPackage;

/**
 * Loads templates from specified folders;
 */
public class FolderTemplateLoader implements ITemplateLoader {

	private final File[] myFolders;
	private final String charsetName;

	public FolderTemplateLoader(File[] folders, String charsetName) {
		this.myFolders = folders;
		this.charsetName = charsetName;
	}

	private static String getFileContents(String file, String charsetName) {
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(new FileInputStream(file), charsetName);
			try {
				while ((count = in.read(buffer)) > 0) {
					contents.append(buffer, 0, count);
				}
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			return null;
		}
		return contents.toString();
	}

	public TemplatesPackage load(String containerName, IProblemCollector collector) {
		String fileName = containerName +  CONTAINER_EXT;

		for( File f : myFolders ) {
			File file = new File(f, fileName);
			if( file.exists() ) {
				String name = file.toString();
				return new TemplatesPackage(name, TemplatesPackage.parse(name, getFileContents(name, charsetName), containerName, collector));
			}
		}
		return null;
	}
}
