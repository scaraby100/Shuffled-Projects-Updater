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
import xyz.scarabya.shuffledprojectsupdater.domain.OriginalFile;
import xyz.scarabya.shuffledprojectsupdater.domain.SubDirNotFoundException;
import xyz.scarabya.shuffledprojectsupdater.domain.TooManyDirectoriesException;

/**
 *
 * @author Alessandro Patriarca
 */
public class Engine
{
    private Map<String, Package> sourceDirs;
    private final Level INFO_LOG = Level.INFO;
    private final Level WARNING_LOG = Level.WARNING;
    private final static Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final String UPDATING_MSG = "Updating {0} into {1}";    
    private final String MOVED_MSG = "{0} moved from {1} to {2}";
    private final String UPDATING_MOVED_MSG = MOVED_MSG +"! Updating from {1}";
    
    public void createProjectsTree(File rootDirectory, String sourceRootName,
            int sourceLevel) throws TooManyDirectoriesException,
            SubDirNotFoundException, DuplicateFileFoundException
    {
        sourceDirs = new HashMap<>();
        for (File projectDir : rootDirectory.listFiles())
            if (projectDir.isDirectory())
            {
                final String projectName = projectDir.getName();
                final File sourceDir = Walker.walkInto(Walker
                        .getSourceDir(projectDir, sourceRootName), sourceLevel);
                final String sourceDirName = sourceDir.getName();
                if(sourceDirs.containsKey(sourceDirName))
                    sourceDirs.get(sourceDirName)
                            .mergeUsingPkg(getPackage(sourceDir, projectName));
                else
                    sourceDirs.put(sourceDirName,
                            getPackage(sourceDir, projectName));
            }
    }
    
    public void walkAndUpdateFiles(final File dirToUpdate,
            final String projectName, final Package originalPkg)
            throws IOException
    {
        OriginalFile originalFile;
        String updatingName, originalProjectName;
        String[] logParams = new String[3];
        logParams[2] = projectName;
        for (File updating : dirToUpdate.listFiles())
        {            
            updatingName = updating.getName();
            logParams[0] = updatingName;
            if (updating.isDirectory())
            {
                walkAndUpdateFiles(dirToUpdate, projectName,
                        originalPkg.getSubPackage(updatingName));
            }
            else
            {
                originalFile = originalPkg.getOriginalFile(updatingName);
                originalProjectName = originalFile.getProjectName();
                logParams[1] = originalProjectName;
                if(projectName.equals(originalProjectName))
                    LOGGER.log(INFO_LOG, UPDATING_MSG, logParams);
                else
                    LOGGER.log(WARNING_LOG, UPDATING_MOVED_MSG, logParams);
                Files.copy(new File(originalFile.getAbsName()).toPath(),
                        updating.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    public void walkAndCheckMovedFiles(final File dirToCheck,
            final String projectName, final Package originalPkg)
    {
        OriginalFile originalFile;
        String checkingName, originalProjectName, msg;
        String[] logParams = new String[3];
        logParams[2] = projectName;
        for (File checking : dirToCheck.listFiles())
        {
            checkingName = checking.getName();
            logParams[0] = checkingName;
            if (checking.isDirectory())
            {
                walkAndCheckMovedFiles(checking, projectName,
                        originalPkg.getSubPackage(checkingName));
            }
            else
            {
                originalFile = originalPkg.getOriginalFile(checkingName);
                originalProjectName = originalFile.getProjectName();
                logParams[1] = originalProjectName;
                
                if(!projectName.equals(originalProjectName))
                    LOGGER.log(WARNING_LOG, MOVED_MSG, logParams);
            }
        }
    }
    
    public void updateFiles(File rootDirectory, String sourceRootName,
            int sourceLevel) throws TooManyDirectoriesException,
            SubDirNotFoundException, IOException
    {
        for (File projectDir : rootDirectory.listFiles())
            if (projectDir.isDirectory())
            {
                final String projectName = projectDir.getName();
                final File sourceDir = Walker.walkInto(Walker
                        .getSourceDir(projectDir, sourceRootName), sourceLevel);
                final String sourceDirName = sourceDir.getName();
                walkAndUpdateFiles(sourceDir, projectName,
                        sourceDirs.get(sourceDirName));
            }
    }
    
    public void verifyMovedFiles(File rootDirectory, String sourceRootName,
            int sourceLevel) throws TooManyDirectoriesException,
            SubDirNotFoundException
    {
        for (File projectDir : rootDirectory.listFiles())
            if (projectDir.isDirectory())
            {
                final String projectName = projectDir.getName();
                final File sourceDir = Walker.walkInto(Walker
                        .getSourceDir(projectDir, sourceRootName), sourceLevel);
                final String sourceDirName = sourceDir.getName();
                walkAndCheckMovedFiles(sourceDir, projectName,
                        sourceDirs.get(sourceDirName));
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
