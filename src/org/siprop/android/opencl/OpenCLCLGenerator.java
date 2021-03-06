/*
   OpenCL CL files header Generator for Android NDK
   Copyright (c) 2006-2015 SIProp Project http://www.siprop.org/
   This software is provided 'as-is', without any express or implied warranty.
   In no event will the authors be held liable for any damages arising from the use of this software.
   Permission is granted to anyone to use this software for any purpose,
   including commercial applications, and to alter it and redistribute it freely,
   subject to the following restrictions:
   1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
   2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
   3. This notice may not be removed or altered from any source distribution.
*/
package org.siprop.android.opencl;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.*;
import java.util.Date;

public class OpenCLCLGenerator extends AnAction {

    static final public String DEFAULT_JNI_PATH = "./app/src/main/jni/";
    static final public String DEFAULT_HEADER_NAME = "opencl_cl_files.h";
    static final public String DEFAULT_PREFIX_CL = "CLCL_";
    static final public String DEFAULT_POSTFIX_SIZE = "__SIZE";


    private String JNI_PATH = DEFAULT_JNI_PATH;
    private String HEADER_NAME = DEFAULT_HEADER_NAME;
    private String PREFIX_CL = DEFAULT_PREFIX_CL;
    private String POSTFIX_SIZE = DEFAULT_POSTFIX_SIZE;

    private BufferedWriter clHeaderWriter = null;


    public void actionPerformed(AnActionEvent event) {
        PluginConfig config = PluginConfig.getInstance(event.getProject());
        if(!config.JNIPath.isEmpty()) {
            JNI_PATH = config.JNIPath;
        }
        if(!config.GeneratedHeaderName.isEmpty()) {
            HEADER_NAME = config.GeneratedHeaderName;
        }
        if(!config.Prefix4Variable.isEmpty()) {
            PREFIX_CL = config.Prefix4Variable;
        }


        boolean isOK = false;
        String exceptionMessage = null;
        try {
            String projectPath = event.getProject().getBasePath();
            File clHeader = new File(projectPath + JNI_PATH + HEADER_NAME);
            if(!clHeader.exists()) {
                clHeader.createNewFile();
            }
            clHeaderWriter = new BufferedWriter(new FileWriter(clHeader));

            writeHeader();

            File jniDir = new File(projectPath + JNI_PATH);
            readDir(jniDir);

            writeFooter();
            clHeaderWriter.flush();

            isOK = true;
        } catch (Exception e) {
            e.printStackTrace();
            exceptionMessage = e.getMessage();
        } finally {
            if( clHeaderWriter != null ) {
                try {
                    clHeaderWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(isOK) {
                Notifications.Bus.notify(
                        new Notification("OpenCLCLGenerator", "Success!", "Finish to generate OpenCL's CL files header.", NotificationType.INFORMATION)
                );
            } else {
                Notifications.Bus.notify(
                        new Notification("OpenCLCLGenerator", "Failure!", "Fail to generate OpenCL's CL files header.\nError:" + exceptionMessage, NotificationType.INFORMATION)
                );
            }
        }
    }

    private void writeHeader() throws FileNotFoundException, IOException {
        if(clHeaderWriter == null) {
            throw new FileNotFoundException("Not Found CL Header's file.");
        }
        clHeaderWriter.write("/* ###### AutoGenerated File. Generated Data:" + new Date() + " ###### */\n");
        clHeaderWriter.newLine();

        // Define Name = CL file name
        String defineName = HEADER_NAME.toUpperCase();
        defineName = defineName.replace(".", "_");
        clHeaderWriter.write("#ifndef __" + defineName + "__\n");
        clHeaderWriter.write("#define __" + defineName + "__\n");
        clHeaderWriter.newLine();
        clHeaderWriter.write("#include <stddef.h>\n");
        clHeaderWriter.newLine();
        clHeaderWriter.flush();



    }
    private void writeFooter() throws FileNotFoundException, IOException {
        if(clHeaderWriter == null) {
            throw new FileNotFoundException("Not Found CL Header's file.");
        }
        clHeaderWriter.write("#endif\n");
        clHeaderWriter.newLine();
        clHeaderWriter.write("/* ###### End AutoGenerated File. ###### */\n");
        clHeaderWriter.flush();
    }


    private void readDir(File dir) throws FileNotFoundException, IOException {
        File[] files = dir.listFiles();
        if( files == null )
            return;
        for( File file : files ) {
            if (!file.exists()) {
                continue;
            } else if (file.isDirectory()) {
                readDir(file);
            } else if( file.isFile() ) {
                // check extention
                String fileName = file.getPath().toUpperCase();
                if (fileName.endsWith(".CL")) {
                    generateCharValue(file);
                    generateSizeValue(file);
                } else {
                    continue;
                }
            }
        }
    }

    private void generateCharValue(File clFile) throws FileNotFoundException, IOException {

        if(clHeaderWriter == null) {
            throw new FileNotFoundException("Not Found CL Header's file.");
        }

        // Create field name "char *file_name"
        String clFileName = clFile.getName().toUpperCase();
        // replace . -> _
        clFileName = clFileName.replace(".", "_");
        StringBuilder clFileNameSB = new StringBuilder(clFileName);
        clFileNameSB.delete(clFileName.length() - 3, clFileName.length());
        // Variable Name = Prefix + CL file name
        clHeaderWriter.write("static const char *" + PREFIX_CL + clFileNameSB + " = ");
        clHeaderWriter.newLine();

        BufferedReader clFileReader = new BufferedReader(new FileReader(clFile));
        String readText = null;
        while ( (readText = clFileReader.readLine()) != null ){
            readText = replaceSpecialCharactors(readText);

            StringBuilder writeText = new StringBuilder();
            // insert " to begin of line
            writeText.append("\t\"");
            writeText.append(readText);
            // insert \n to end of line
            writeText.append("\\n\"");

            writeText.append("\n");

            clHeaderWriter.write(writeText.toString());
        }
        clFileReader.close();

        // insert ; to begin of this file
        clHeaderWriter.write("\t\"\";");
        clHeaderWriter.newLine();
        clHeaderWriter.newLine();
        clHeaderWriter.newLine();
        clHeaderWriter.flush();
    }
    private void generateSizeValue(File clFile) throws FileNotFoundException, IOException {

        if (clHeaderWriter == null) {
            throw new FileNotFoundException("Not Found CL Header's file.");
        }

        // Create field name "const size_t "
        String clFileName = clFile.getName().toUpperCase();
        // replace . -> _
        clFileName = clFileName.replace(".", "_");
        StringBuilder clFileNameSB = new StringBuilder(clFileName);
        clFileNameSB.delete(clFileName.length() - 3, clFileName.length());
        // Variable Name = Prefix + CL file name + Postfix
        clHeaderWriter.write("static const size_t " + PREFIX_CL + clFileNameSB + POSTFIX_SIZE + " = ");

        BufferedReader clFileReader = new BufferedReader(new FileReader(clFile));
        String readText = null;
        int textCounter = 0;
        while ((readText = clFileReader.readLine()) != null) {
            textCounter += readText.length() + 1;
        }
        clHeaderWriter.write(textCounter + ";");
        clHeaderWriter.newLine();
        clHeaderWriter.newLine();
        clHeaderWriter.newLine();
        clHeaderWriter.flush();
    }

    private String replaceSpecialCharactors(String text) {
        String replacedText = text;
        // replace \ -> \\
        replacedText = replacedText.replace("\\", "\\\\");
        // replace " -> \"
        replacedText = replacedText.replace("\"", "\\\"");

        return replacedText;
    }

}
