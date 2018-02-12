/*
 * Copyright 2018 Alessandro Patriarca.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.scarabya.shuffledprojectsupdater.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import xyz.scarabya.shuffledprojectsupdater.domain.Package;
import xyz.scarabya.shuffledprojectsupdater.domain.DuplicateFileFoundException;
import xyz.scarabya.shuffledprojectsupdater.domain.Operation;
import xyz.scarabya.shuffledprojectsupdater.domain.OriginalFile;
import xyz.scarabya.shuffledprojectsupdater.domain.SubDirNotFoundException;
import xyz.scarabya.shuffledprojectsupdater.domain.TooManyDirectoriesException;

/**
 *
 * @author Alessandro Patriarca
 */
public class Engine
{
    private final Map<String, Package> sourceDirs;
    private final Level INFO_LOG = Level.INFO;
    private final Level WARNING_LOG = Level.WARNING;
    private final static Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final String UPDATING_MSG = "Updating {0} into {1}";    
    private final String MOVED_MSG = "{0} moved from {1} to {2}";
    private final String UPDATING_MOVED_MSG = MOVED_MSG +"! Updating from {1}";

    public Engine()
    {
        sourceDirs = new HashMap<>();
    }
    
    public void doOperation(final File rootDirectory,
            final String sourceRootName, final int sourceLevel,
            final Operation operation) throws TooManyDirectoriesException,
            SubDirNotFoundException, DuplicateFileFoundException, IOException
    {
        for (File projectDir : rootDirectory.listFiles())
            if (projectDir.isDirectory())
            {
                final String projectName = projectDir.getName();
                final File sourceDir = Walker.walkInto(Walker
                        .getSourceDir(projectDir, sourceRootName), sourceLevel);
                final String sourceDirName = sourceDir.getName();
                
                if(operation == Operation.CREATE)
                    addPackage(sourceDirName, sourceDir, projectName);
                else
                    walkAndDoOperation(sourceDir, projectName,
                            sourceDirs.get(sourceDirName), operation);
            }
    }
    
    private void addPackage(final String sourceDirName, final File sourceDir,
            final String projectName) throws TooManyDirectoriesException,
            SubDirNotFoundException, DuplicateFileFoundException
    {
        if(sourceDirs.containsKey(sourceDirName))
            sourceDirs.get(sourceDirName)
                    .mergeUsingPkg(getPackage(sourceDir, projectName));
        else
            sourceDirs.put(sourceDirName,getPackage(sourceDir, projectName));
    }
    
    private void walkAndDoOperation(final File pkgToProcess,
            final String projectName, final Package originalPkg,
            final Operation operation) throws IOException
    {
        OriginalFile originalFile;
        String processingName, originalProjectName;
        String[] logParams = new String[3];
        logParams[2] = projectName;
        for (File processing : pkgToProcess.listFiles())
        {
            processingName = processing.getName();
            logParams[0] = processingName;
            if (processing.isDirectory())
            {
                walkAndDoOperation(processing, projectName,
                        originalPkg.getSubPackage(processingName), operation);
            }
            else
            {
                originalFile = originalPkg.getOriginalFile(processingName);
                originalProjectName = originalFile.getProjectName();
                logParams[1] = originalProjectName;
                logAndOperate(processing, projectName, originalProjectName,
                        originalFile, logParams, operation);
            }
        }
    }
    
    private void logAndOperate(final File updating, final String projectName,
            final String originalProjectName, final OriginalFile originalFile,
            final String[] logParams, final Operation operation)
            throws IOException
    {
        switch(operation)
        {
            case CHECK:
                if(!projectName.equals(originalProjectName))
                    LOGGER.log(WARNING_LOG, MOVED_MSG, logParams);
                break;
            case UPDATE:
                if(projectName.equals(originalProjectName))
                    LOGGER.log(INFO_LOG, UPDATING_MSG, logParams);
                else
                    LOGGER.log(WARNING_LOG, UPDATING_MOVED_MSG, logParams);
                Files.copy(new File(originalFile.getAbsName()).toPath(),
                        updating.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
                break;                
        }
    }
    
    private Package getPackage(final File rootDirectory,
            final String projectName)
    {
        Package newPackage = new Package();
        for (File file : rootDirectory.listFiles())
            if (file.isDirectory())
                newPackage.addPackage(file.getName(),
                        getPackage(file, projectName));
            else
                newPackage.addFile(new OriginalFile(file, projectName));
        
        return newPackage;
    }
}
